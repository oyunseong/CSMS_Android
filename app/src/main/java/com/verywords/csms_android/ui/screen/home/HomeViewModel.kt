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
            serialManager.messages.collect{
                messages.emit(it)
            }
        }


//        viewModelScope.launch {
//            serialManager.deviceState.collect { deviceStates ->
//                deviceStates.forEach { deviceState ->
//                    deviceState.lastReceiveMessage.let { newMessage ->
//                        val updatedMessages = messages.value.toMutableList()
//                        updatedMessages.add(newMessage)
//                        messages.emit(updatedMessages)
//                    }
//                }
//            }
//        }
    }

    fun onClearMessages(){
        viewModelScope.launch {
            serialManager.clearMessages()
        }
    }

    fun searchDevice() {
        viewModelScope.launch {
            serialManager.searchConnectableUSBDevice()
        }
    }

    fun connectDevice(
        context: Context,
        deviceInfo: DeviceInfo,
    ) {
        viewModelScope.launch {
            serialManager.connect(
                context = context,
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
            serialManager.sendData(
                sendData = "101,49,56,48,54,100,D0,102,52,56,102,102,102,102,102,102,48,49,102,102,102,102,48,48,102,102,13",
                usbSerialPort = deviceInfo.usbSerialPort
            )
        }
    }

    fun updateDeviceInfo(deviceInfo: DeviceInfo) {
        viewModelScope.launch {
            serialManager.updateState(targetDevice = deviceInfo)
        }
    }

    companion object {
        private val INTENT_ACTION_GRANT_USB: String = "com.example.myapplication" + ".GRANT_USB"
    }
}