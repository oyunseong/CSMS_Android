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
import com.verywords.csms_android.utils.log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
) : ViewModel() {
    val serialManager: SerialCommunicationManager = SerialCommunicationManager

//    private val _uiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())

    init {
        log(message = "HomeViewModel init")

        viewModelScope.launch {
            serialManager.deviceState.collect {
                it.forEach {
//                    log(message = "deviceState: $it")
                }
            }
        }
    }


    fun searchDevice(context: Context) {
        viewModelScope.launch {
            serialManager.searchConnectableUSBDevice(context = context)
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

    fun updateBaudRate(deviceInfo: DeviceInfo) {
        viewModelScope.launch {
            serialManager.updateState(targetDevice = deviceInfo)
        }
    }

    companion object {
        private val INTENT_ACTION_GRANT_USB: String = "com.example.myapplication" + ".GRANT_USB"
    }
}