package com.verywords.csms_android.feat

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.HexDump
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.verywords.csms_android.App
import com.verywords.csms_android.feat.model.DeviceInfo
import com.verywords.csms_android.feat.model.ReceiveData
import com.verywords.csms_android.utils.convertMillisToDateTime
import com.verywords.csms_android.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

const val INTENT_ACTION_GRANT_USB: String = "com.example.myapplication" + ".GRANT_USB"

object SerialCommunicationManager {
    private val _deviceState: MutableStateFlow<List<DeviceInfo>> = MutableStateFlow(emptyList())
    val deviceState = _deviceState.asStateFlow()

    val messages = MutableStateFlow<List<ReceiveData>>(emptyList())
    private var usbPermission: UsbPermission = UsbPermission.Unknown

    suspend fun searchConnectableUSBDevice(context: Context = App.context) {
        _deviceState.emit(emptyList())
        val connectableDevices = mutableListOf<DeviceInfo>()
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDefaultProber = UsbSerialProber.getDefaultProber()
        connectableDevices.clear()

        manager.deviceList.forEach {
            val driver = usbDefaultProber.probeDevice(it.value)
            if (driver != null) {
                for (port in 0 until driver.ports.size) {
                    connectableDevices.add(
                        DeviceInfo(
                            device = it.value,
                            port = port,
                            driver = driver,
                            usbSerialPort = driver.ports[port]
                        )
                    )
                }
            }
        }
        _deviceState.update {
            connectableDevices
        }
    }

    private fun requestDeviceUsbPermission(
        context: Context = App.context,
        usbManager: UsbManager,
        deviceInfo: DeviceInfo
    ) {
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_MUTABLE else 0
        val intent = Intent(INTENT_ACTION_GRANT_USB).run {
            setPackage(context.packageName)
        }
        val usbPermissionIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
        usbManager.requestPermission(deviceInfo.device, usbPermissionIntent)
    }

    fun connect(
        context: Context = App.context,
        connectDevice: DeviceInfo,
    ) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val usbConnection: UsbDeviceConnection? = usbManager.openDevice(connectDevice.device)

        if (usbConnection == null
            && usbPermission != UsbPermission.Granted
            && !usbManager.hasPermission(connectDevice.device)
        ) {
            usbPermission = UsbPermission.Requested

            requestDeviceUsbPermission(
                context = context,
                usbManager = usbManager,
                deviceInfo = connectDevice
            )
            return
        }

        try {
            val usbSerialPort = connectDevice.usbSerialPort
            usbSerialPort.open(usbConnection)
            try {
                usbSerialPort.dtr = true
                usbSerialPort.rts = true
                usbSerialPort.setParameters(
                    connectDevice.baudRate,
                    connectDevice.dataBits,
                    connectDevice.stopBits,
                    connectDevice.parity
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (connectDevice.withIoManager) {
                val usbIoManager = SerialInputOutputManager(
                    usbSerialPort,
                    object : SerialInputOutputManager.Listener {
                        override fun onNewData(data: ByteArray?) {
                            deviceState.value.forEach {
                                if (it.device == connectDevice.device) {
                                    receiveData(
                                        data = data ?: byteArrayOf(),
                                        deviceInfo = it
                                    )
                                }
                            }

                            deviceState.value.forEach {
                                if (it.device == connectDevice.device) {
                                    receiveData(
                                        data = data ?: byteArrayOf(),
                                        deviceInfo = it
                                    )
                                }
                            }
                        }

                        override fun onRunError(e: java.lang.Exception) {
                            e.printStackTrace()
                            disconnect(connectedDevice = connectDevice)
                        }
                    })
                usbIoManager.start()
                updateState(
                    targetDevice = connectDevice.copy(
                        isConnected = true,
                        usbIoManager = usbIoManager
                    )
                )
            }
        } catch (e: Exception) {
            disconnect(connectedDevice = connectDevice)
        }
    }

    fun disconnect(connectedDevice: DeviceInfo) {
        if (!connectedDevice.isConnected) return
        try {
            val usbIoManager = connectedDevice.usbIoManager
            if (usbIoManager != null) {
                usbIoManager.listener = null
                usbIoManager.stop()
            }
            connectedDevice.usbSerialPort.close()
            updateState(
                targetDevice = connectedDevice.copy(
                    isConnected = false,
                    usbIoManager = usbIoManager
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun receiveData(data: ByteArray, deviceInfo: DeviceInfo) {
        if (deviceInfo.usbIoManager?.state != SerialInputOutputManager.State.RUNNING) return

        val message = ReceiveData(
            time = System.currentTimeMillis(),
            size = data.size,
            data = HexDump.dumpHexString(data) ?: "known data",
        )
        updateState(
            targetDevice = deviceInfo.copy(
                message = deviceInfo.message + message,
                lastReceiveMessage = message
            )
        )

        messages.update { messages ->
            messages + message
        }
    }

    //    private var job: Job? = null
    fun sendData(
//        request: ByteArray,
        requestASCII: String,
        usbSerialPort: UsbSerialPort
    ) {
        var job: Job? = null
        job = CoroutineScope(Dispatchers.IO).launch {
            val request = (requestASCII + "\r").toByteArray()
//            val data = ("e1806d1f48ffff01fffffffff" +"\r").toByteArray()
//            val data2 = ("e1806d1f48ffff08fffffffff" +"\r").toByteArray()
            try {
                usbSerialPort.write(request, 2000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        job.invokeOnCompletion {
            job?.cancel()
            job = null
            log(message = "job cancel")
        }
    }


    fun clearMessages() {
        messages.update {
            emptyList()
        }
    }

    fun updateState(targetDevice: DeviceInfo) {
        _deviceState.update { deviceInfo ->
            deviceInfo.map {
                if (it.device == targetDevice.device) {
                    targetDevice
                } else {
                    it
                }
            }
        }
    }

}


