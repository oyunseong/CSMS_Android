package com.verywords.csms_android.domain.usecase

import android.util.Log
import com.verywords.csms_android.data.local.repository.MessageRepository
import com.verywords.csms_android.data.remote.repository.LogRepository
import com.verywords.csms_android.ui.model.Message
import com.verywords.csms_android.utils.log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class SendEventWithPendingMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val logRepository: LogRepository,
) {
    private val mutex = Mutex()
    suspend operator fun invoke(
        message: Message,
        action: () -> Unit = {}
    ) {
        mutex.withLock {
            val remainMessages = messageRepository.getAllEventMessages()
            if (remainMessages.isNotEmpty()) {
                remainMessages.forEach {
                    sendMessage(message = it, action = action)
                }
            } else {
                Log.d(TAG, "remainMessages is empty")
            }
            // Try to send the new message
            sendMessage(message, action)
        }
    }

    private suspend fun sendMessage(
        message: Message,
        action: () -> Unit
    ) {
        val result = runCatching {
            logRepository.sendEvent(message)
        }

        result.onSuccess { sendResult ->
            if (sendResult.isSuccess) {
                log(message = "++Success to send message with id ${message.id}")
                deleteMessage(
                    messageId = message.id,
                    action = action
                )
            } else {
                log(message = "Failed to send message with id ${message.id}: ${sendResult.exceptionOrNull()?.message}")
                insertMessage(message, sendResult.exceptionOrNull()?.message ?: "Unknown error")
            }
        }.onFailure { exception ->
            log(message = "Failed to send message with id ${message.id}: ${exception.message}")
            insertMessage(message, exception.message ?: "Unknown error")
        }
    }


    private suspend fun insertMessage(message: Message, errorMessage: String) {
        messageRepository.insertEventMessage(
            message = Message(
                createAt = System.currentTimeMillis(),
                message = message.message,
                errorMessage = errorMessage
            )
        )
    }

    private suspend fun deleteMessage(
        messageId: Int,
        action: () -> Unit = {}
    ) {
        log(message = "Delete message with id $messageId")
        messageRepository.deleteEventMessageById(messageId)
        action.invoke()
    }

    companion object {
        private const val TAG = "DatabaseScreenViewModel"
    }
}