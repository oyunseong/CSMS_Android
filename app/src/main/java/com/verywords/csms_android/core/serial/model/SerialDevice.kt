package com.verywords.csms_android.core.serial.model

import android.hardware.usb.UsbDevice
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialPort.Parity
import com.hoho.android.usbserial.util.SerialInputOutputManager

data class SerialDevice(
    val device: UsbDevice,
    val driver: UsbSerialDriver,
    val usbSerialPort: UsbSerialPort,
    val usbIoManager: SerialInputOutputManager? = null,
    val port: Int,
    val baudRate: Int = 19200,
    val dataBits: Int = 8,
    val stopBits: Int = 1,
    @Parity val parity: Int = UsbSerialPort.PARITY_NONE,
    val isConnected: Boolean = false,
    val inputText: String = "", // 입력받은 텍스트
)