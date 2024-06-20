package com.verywords.csms_android.feat.model

import android.hardware.usb.UsbDevice
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialPort.Parity
import com.hoho.android.usbserial.util.SerialInputOutputManager

data class SerialDevice(
    val port: Int,
    val device: UsbDevice,
    val driver: UsbSerialDriver,
    val usbSerialPort: UsbSerialPort,
    val withIoManager: Boolean = true, // // read_modes[0]=event/io-manager, read_modes[1]=direct 필요시 추가
    val isConnected: Boolean = false,
    val usbIoManager: SerialInputOutputManager? = null,
    val inputText: String = "",
    val baudRate: Int = 19200,
    val dataBits: Int = 8,
    val stopBits: Int = 1,
    @Parity val parity: Int = UsbSerialPort.PARITY_NONE,
)