package com.verywords.csms_android.core.serial

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.HexDump
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.verywords.csms_android.App
import com.verywords.csms_android.core.serial.model.SerialDevice
import com.verywords.csms_android.core.serial.model.SerialData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.NullPointerException

const val INTENT_ACTION_GRANT_USB: String = "com.example.myapplication" + ".GRANT_USB"

object SerialCommunicationManager {
    private val _deviceState: MutableStateFlow<List<SerialDevice>> = MutableStateFlow(emptyList())
    val deviceState = _deviceState.asStateFlow()

    private val _messages = MutableStateFlow<List<SerialData>>(emptyList())
    val messages = _messages.asStateFlow()

    private const val TAG = "SerialCommunicationManager"
    private const val SERIAL_COMMUNICATION_TIMEOUT = 2000


    fun searchConnectableUSBDevices(context: Context = App.context) {
        val connectableDevices = mutableListOf<SerialDevice>()
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDefaultProber = UsbSerialProber.getDefaultProber()

        usbManager.deviceList.forEach {
            val driver = usbDefaultProber.probeDevice(it.value)
            if (driver != null) {
                for (port in 0 until driver.ports.size) {
                    connectableDevices.add(
                        SerialDevice(
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
        serialDevice: SerialDevice
    ) {
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_MUTABLE else 0
        val intent = Intent(INTENT_ACTION_GRANT_USB).run {
            setPackage(context.packageName)
        }
        val usbPermissionIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
        usbManager.requestPermission(serialDevice.device, usbPermissionIntent)
    }

    fun connect(
        context: Context = App.context,
        connectDevice: SerialDevice,
    ) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        try {
            val usbConnection = openUsbConnection(
                usbManager = usbManager,
                device = connectDevice.device
            )
            val usbSerialPort = setupSerialPort(
                device = connectDevice,
                usbConnection = usbConnection
            )
            val usbIoManager = SerialInputOutputManager(
                usbSerialPort,
                getSerialDataListener(connectDevice)
            )
            usbIoManager.start()
            updateState(
                connectDevice.copy(
                    isConnected = true,
                    usbIoManager = usbIoManager
                )
            )
        } catch (e: Exception) {
            handleError(e, context, usbManager, connectDevice)
        }
    }

    private fun openUsbConnection(
        usbManager: UsbManager,
        device: UsbDevice
    ): UsbDeviceConnection {
        return usbManager.openDevice(device)
    }

    private fun setupSerialPort(
        device: SerialDevice,
        usbConnection: UsbDeviceConnection
    ): UsbSerialPort {
        return device.usbSerialPort.also {
            it.open(usbConnection)
            it.dtr = true
            it.rts = true
            it.setParameters(
                device.baudRate,
                device.dataBits,
                device.stopBits,
                device.parity
            )
        }
    }

    private fun handleError(
        e: Exception,
        context: Context,
        usbManager: UsbManager,
        connectDevice: SerialDevice
    ) {
        e.printStackTrace()
        if (e is NullPointerException) {
            requestDeviceUsbPermission(
                context = context,
                usbManager = usbManager,
                serialDevice = connectDevice
            )
        } else {
            disconnect(connectedDevice = connectDevice)
        }
    }

    private fun getSerialDataListener(connectedDevice: SerialDevice): SerialInputOutputManager.Listener {
        return object : SerialInputOutputManager.Listener {
            override fun onNewData(data: ByteArray?) {
                if (data == null) {
                    Log.d(TAG, "${connectedDevice.device.deviceId} : data is null")
                } else {
                    deviceState.value.forEach {
                        if (it.device == connectedDevice.device) {
                            receiveData(
                                data = data,
                                serialDevice = it
                            )
                        }
                    }
                }
            }

            override fun onRunError(e: Exception) {
                e.printStackTrace()
                disconnect(connectedDevice = connectedDevice)
            }
        }
    }

    fun disconnect(connectedDevice: SerialDevice) {
        if (!connectedDevice.isConnected) {
            Log.e(TAG, "disconnect : already disconnected")
            return
        }
        try {
            val usbIoManager = connectedDevice.usbIoManager
            if (usbIoManager != null) {
                usbIoManager.listener = null
                usbIoManager.stop()
                connectedDevice.usbSerialPort.close()
                updateState(
                    targetDevice = connectedDevice.copy(
                        isConnected = false,
                        usbIoManager = usbIoManager
                    )
                )
            } else {
                Log.e(TAG, "disconnect : usbIoManager is null")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun receiveData(data: ByteArray, serialDevice: SerialDevice) {
        if (serialDevice.usbIoManager?.state != SerialInputOutputManager.State.RUNNING) {
            Log.e(TAG, "receiveData : usbIoManager is not running")
            return
        }

        val lastMessage = SerialData(
            time = System.currentTimeMillis(),
            size = data.size,
            data = HexDump.dumpHexString(data) ?: "known data",
        )

        _messages.update { messages ->
            messages + lastMessage
        }
    }

    // 기기에서 Text를 직접 입력해서 시리얼 데이터를 보낼 때 사용
    fun sendAsciiData(
        requestASCII: String,
        usbSerialPort: UsbSerialPort
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            val request = (requestASCII + "\r").toByteArray()
            try {
                usbSerialPort.write(request, SERIAL_COMMUNICATION_TIMEOUT)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendHexData(
        request: ByteArray,
        usbSerialPort: UsbSerialPort
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                usbSerialPort.write(request, SERIAL_COMMUNICATION_TIMEOUT)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun clearMessages() {
        _messages.update {
            emptyList()
        }
    }

    fun updateState(targetDevice: SerialDevice) {
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


