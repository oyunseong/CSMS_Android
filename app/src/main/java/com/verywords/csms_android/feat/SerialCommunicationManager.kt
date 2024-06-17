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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object SerialCommunicationManager {

    private val INTENT_ACTION_GRANT_USB: String = "com.example.myapplication" + ".GRANT_USB"


    private val _deviceState: MutableStateFlow<List<DeviceInfo>> = MutableStateFlow(emptyList())
    val deviceState = _deviceState.asStateFlow()

    private var job: Job? = null
    private var usbPermission: UsbPermission = UsbPermission.Unknown

    suspend fun searchConnectableUSBDevice(context: Context = App.context) {
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

        connectableDevices.forEach {
            log(message = "items: $it")
        }
        _deviceState.update {
            connectableDevices
        }
    }

//    private fun hasUsbPermission(
//        deviceInfo: DeviceInfo
//    ) {
//        object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (HomeViewModel.INTENT_ACTION_GRANT_USB == intent!!.action) {
////                    val usbPermission: UsbPermission = UsbPermission.Unknown
//                    val usbPermission =
//                        if (intent.getBooleanExtra(
//                                UsbManager.EXTRA_PERMISSION_GRANTED,
//                                false,
//                            )
//                        ) {
//                            UsbPermission.Granted
//                        } else {
//                            UsbPermission.Denied
//                        }
//
//                    if(usbPermission != UsbPermission.Granted){
//                        val flags =
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_MUTABLE else 0
//                        val intent = Intent(HomeViewModel.INTENT_ACTION_GRANT_USB)
//                        intent.setPackage(context.packageName)
//                        val usbPermissionIntent: PendingIntent =
//                            PendingIntent.getBroadcast(context, 0, intent, flags)
//                        usbManager.requestPermission(connectDevice.device, usbPermissionIntent)
//                    }
//                }
//            }
//        }
//    }

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


    suspend fun connect(
        context: Context = App.context,
        connectDevice: DeviceInfo,
        dataBits: Int = 8,
        stopBits: Int = 1,
        parity: Int = UsbSerialPort.PARITY_NONE
    ) {
        log(message = "connect call")
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val usbConnection: UsbDeviceConnection? = usbManager.openDevice(connectDevice.device)

        if (usbConnection == null
            && usbPermission != UsbPermission.Granted
            && !usbManager.hasPermission(connectDevice.device)
        ) {
            usbPermission = UsbPermission.Requested
            val time = System.currentTimeMillis()
            log(message = "request permission start $time")
            requestDeviceUsbPermission(
                context = context,
                usbManager = usbManager,
                deviceInfo = connectDevice
            )
            log(message = "request permission end ${time - System.currentTimeMillis()}")
            return
        }

        try {
            val usbSerialPort = connectDevice.usbSerialPort
            usbSerialPort.open(usbConnection)
            try {
                usbSerialPort.setParameters(connectDevice.baudRate, dataBits, stopBits, parity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (connectDevice.withIoManager) {
                val usbIoManager = SerialInputOutputManager(
                    usbSerialPort,
                    object : SerialInputOutputManager.Listener {
                        override fun onNewData(data: ByteArray?) {
                            log(message = "byteArray ${data.contentToString()}")
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
            "disconnect success device : ${connectedDevice.device.productName}"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun receiveData(data: ByteArray, deviceInfo: DeviceInfo) {
        val state = deviceInfo.usbIoManager?.state
        if (state != SerialInputOutputManager.State.RUNNING) return
        val message = ReceiveData(
            time = System.currentTimeMillis(),
            size = data.size,
            data = HexDump.dumpHexString(data) ?: "known data",
        )
        log(message = "receiveData: ${convertMillisToDateTime(message.time)}  message : ${message.data}")

        updateState(
            targetDevice = deviceInfo.copy(
                message = deviceInfo.message + message
            )
        )
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


