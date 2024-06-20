package com.verywords.csms_android.data.local.repository.impl

import android.util.Log
import com.verywords.csms_android.data.local.dao.MessageDao
import com.verywords.csms_android.data.local.entity.MessageEntity
import com.verywords.csms_android.data.local.repository.MessageRepository
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {
    override suspend fun getMessages(): List<MessageEntity> {
        Log.d("++##", "getMessage!!")
        return messageDao.getAllMessages()
    }
}