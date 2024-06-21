package com.verywords.csms_android.ui.screen.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verywords.csms_android.data.local.repository.MessageRepository
import com.verywords.csms_android.domain.usecase.SendEventWithPendingMessagesUseCase
import com.verywords.csms_android.ui.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatabaseScreenViewModel @Inject constructor(
    private val sendEventWithPendingMessagesUseCase: SendEventWithPendingMessagesUseCase,
    private val messageRepository: MessageRepository,
) : ViewModel() {

    val messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())

    init {
        getMessages()
    }

    fun sendEventMessage(message: Message) {
        viewModelScope.launch {
            sendEventWithPendingMessagesUseCase.invoke(
                message = message,
                action = {
                    getMessages()
                }
            )
        }
    }

    fun insertMessageToDatabase() {
        viewModelScope.launch {
            messageRepository.insertEventMessage(
                message = Message(
                    createAt = System.currentTimeMillis(),
                    message = "Message",
                )
            )
            getMessages()
        }
    }

    // DB에 저장된 메시지를 가져옵니다.
    private fun getMessages() {
        viewModelScope.launch {
            messages.emit(messageRepository.getAllEventMessages())
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            messageRepository.deleteAllEventMessage()
            getMessages()
        }
    }

    companion object {
        private const val TAG = "DatabaseScreenViewModel"
    }
}