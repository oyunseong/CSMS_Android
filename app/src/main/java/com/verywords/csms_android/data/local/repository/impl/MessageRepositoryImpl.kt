package com.verywords.csms_android.data.local.repository.impl

import com.verywords.csms_android.data.local.dao.MessageDao
import com.verywords.csms_android.data.local.repository.MessageRepository
import com.verywords.csms_android.ui.model.Message
import com.verywords.csms_android.ui.model.toEntity
import com.verywords.csms_android.ui.model.toUiModel
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {
    override suspend fun getAllEventMessages(): List<Message> {
        return messageDao.getAllMessages().toUiModel()
    }

    override suspend fun insertEventMessage(message: Message) {
        messageDao.insertMessage(message = message.toEntity())
    }

    override suspend fun deleteAllEventMessage() {
        messageDao.clearMessages()
    }

    override suspend fun deleteEventMessageById(messageId: Int) {
        messageDao.deleteMessageById(messageId)
    }
}