package com.verywords.csms_android.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verywords.csms_android.data.local.repository.MessageRepository
import com.verywords.csms_android.feat.SerialCommunicationManager
import com.verywords.csms_android.feat.model.SerialDevice
import com.verywords.csms_android.feat.model.ReceiveData
import com.verywords.csms_android.utils.log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {
    val serialManager: SerialCommunicationManager = SerialCommunicationManager
    val messages: MutableStateFlow<List<ReceiveData>> = MutableStateFlow(emptyList())
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.e("HomeViewModel", "CoroutineExceptionHandler : ${throwable.message}")
        }

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            val a = messageRepository.getMessages()
            if(a.isNotEmpty()){
                a.forEach {
                    log(message = "it : $it")
                }
            }else{
                log(message = "a is empty")
            }
        }

        searchDevice()
        collectMessages(delayDuration = 1000)
    }

    private fun collectMessages(delayDuration: Long = 0L) {
        viewModelScope.launch(coroutineExceptionHandler) {
            serialManager.messages.collect {
                delay(delayDuration)
                messages.emit(it)
            }
        }
    }

    fun searchDevice() {
        viewModelScope.launch(coroutineExceptionHandler) {
            serialManager.searchConnectableUSBDevices()
        }
    }

    fun toggleConnectDevice(serialDevice: SerialDevice) {
        if (serialDevice.isConnected) {   // 연결 돼 있으면 해제.
            disconnectDevice(serialDevice)
        } else {
            connectDevice(serialDevice = serialDevice)
        }
    }

    private fun connectDevice(serialDevice: SerialDevice) {
        viewModelScope.launch(coroutineExceptionHandler) {
            serialManager.connect(connectDevice = serialDevice)
        }
    }

    private fun disconnectDevice(serialDevice: SerialDevice) {
        viewModelScope.launch {
            serialManager.disconnect(connectedDevice = serialDevice)
        }
    }


    fun sendHexMessage(
        serialDevice: SerialDevice,
    ) {
        val val1: Byte = 49
        val request = byteArrayOf(
            101, 49, 56, 48, 54, 100, val1, 102, 52, 56, 102, 102, 102, 102, 48, 49, 102, 102, 102,
            102, 102, 102, 102, 102, 102, 102, 13
        )
        serialManager.sendHexData(
            request = request,
            usbSerialPort = serialDevice.usbSerialPort
        )
    }


    fun sendAsciiMessage(serialDevice: SerialDevice) {
        //            val data = ("e1806d1f48ffff01fffffffff" +"\r").toByteArray()
        viewModelScope.launch(coroutineExceptionHandler) {
            serialManager.sendAsciiData(
                requestASCII = serialDevice.inputText,
                usbSerialPort = serialDevice.usbSerialPort
            )
        }
    }


    fun clearMessages() {
        viewModelScope.launch(coroutineExceptionHandler) {
            serialManager.clearMessages()
        }
    }


    fun updateSerialDevice(serialDevice: SerialDevice) {
        viewModelScope.launch(coroutineExceptionHandler) {
            serialManager.updateState(targetDevice = serialDevice)
        }
    }
}