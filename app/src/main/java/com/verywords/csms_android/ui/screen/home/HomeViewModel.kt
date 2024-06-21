package com.verywords.csms_android.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verywords.csms_android.data.local.repository.MessageRepository
import com.verywords.csms_android.core.serial.SerialCommunicationManager
import com.verywords.csms_android.core.serial.model.SerialData
import com.verywords.csms_android.core.serial.model.SerialDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    val serialManager: SerialCommunicationManager = SerialCommunicationManager
    val messages: MutableStateFlow<List<SerialData>> = MutableStateFlow(emptyList())

    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.e(TAG, "CoroutineExceptionHandler : ${throwable.message}")
        }

    init {
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

    fun toggleDeviceConnection(serialDevice: SerialDevice) {
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
        viewModelScope.launch(coroutineExceptionHandler) {
            serialManager.disconnect(connectedDevice = serialDevice)
        }
    }

    /**
     * 키보드로 명령어를 입력하는 경우, 사용하는 함수
     * 사용 방법
     * 1. CAN D-Board 연결
     * 2. e1806d1f48ffff01fffffffff 입력 (LED 빨간불)
     * 3. e1806d1f48ffff08fffffffff 입력 (LED 파란불)
     */
    fun sendAsciiMessage(serialDevice: SerialDevice) {
        viewModelScope.launch(coroutineExceptionHandler) {
            serialManager.sendAsciiData(
                requestASCII = serialDevice.inputText,
                usbSerialPort = serialDevice.usbSerialPort
            )
        }
    }

    /**
     * 헥사 값으로 명령어를 입력하는 경우
     * ex.
     *   val val1: Byte = 49
     *   val request = byteArrayOf(
     *                 101, 49, 56, 48, 54, 100, val1,
     *                 102, 52, 56, 102, 102, 102, 102,
     *                 48, 49, 102, 102, 102, 102, 102,
     *                 102, 102, 102, 102, 102, 13
     *                 )
     *
     *    sendHexMessage(request, serialDevice)
     *
     */
    fun sendHexMessage(
        request: ByteArray,
        serialDevice: SerialDevice
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            serialManager.sendHexData(
                request = request,
                usbSerialPort = serialDevice.usbSerialPort
            )
        }
    }

    fun clearReceivedMessages() {
        viewModelScope.launch(coroutineExceptionHandler) {
            serialManager.clearMessages()
        }
    }

    fun updateDeviceState(serialDevice: SerialDevice) {
        viewModelScope.launch(coroutineExceptionHandler) {
            serialManager.updateState(targetDevice = serialDevice)
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}