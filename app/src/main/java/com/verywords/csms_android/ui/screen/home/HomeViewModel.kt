package com.verywords.csms_android.ui.screen.home

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verywords.csms_android.feat.SerialCommunicationManager
import com.verywords.csms_android.feat.UsbPermission
import com.verywords.csms_android.feat.model.DeviceInfo
import com.verywords.csms_android.feat.model.ReceiveData
import com.verywords.csms_android.utils.log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
) : ViewModel() {
    val serialManager: SerialCommunicationManager = SerialCommunicationManager

    //    private val _uiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())
    val messages: MutableStateFlow<List<ReceiveData>> = MutableStateFlow(emptyList())

    init {
        log(message = "HomeViewModel init")
        searchDevice()
        viewModelScope.launch {
            serialManager.messages.collect {
                messages.emit(it)
            }
        }
    }

    fun toggleConnectDevice(deviceInfo: DeviceInfo) {
        if (deviceInfo.isConnected) {   // 연결 돼 있으면 해제.
            disconnectDevice(deviceInfo)
        } else {
            connectDevice(deviceInfo = deviceInfo)
        }
    }

    fun onClearMessages() {
        viewModelScope.launch {
            serialManager.clearMessages()
        }
    }

    fun searchDevice() {
        viewModelScope.launch {
            serialManager.searchConnectableUSBDevice()
        }
    }

    fun connectDevice(deviceInfo: DeviceInfo) {
        viewModelScope.launch {
            serialManager.connect(
                connectDevice = deviceInfo,
            )
        }
    }

    fun disconnectDevice(deviceInfo: DeviceInfo) {
        viewModelScope.launch {
            serialManager.disconnect(connectedDevice = deviceInfo)
        }
    }


    fun sendMessage(deviceInfo: DeviceInfo) {
        viewModelScope.launch {
            val val1: Byte = 49
            val bytes = byteArrayOf(
                101,
                49,
                56,
                48,
                54,
                100,
                val1,
                102,
                52,
                56,
                102,
                102,
                102,
                102,
                48,
                49,
                102,
                102,
                102,
                102,
                102,
                102,
                102,
                102,
                102,
                102,
                13
            )
            serialManager.sendData(
//                request = bytes,
                requestASCII = deviceInfo.inputText,
                usbSerialPort = deviceInfo.usbSerialPort
            )
        }
    }

    fun updateDeviceInfo(deviceInfo: DeviceInfo) {
        viewModelScope.launch {
            serialManager.updateState(targetDevice = deviceInfo)
        }
    }
}