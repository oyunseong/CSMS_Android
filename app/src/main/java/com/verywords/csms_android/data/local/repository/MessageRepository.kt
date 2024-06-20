package com.verywords.csms_android.data.local.repository

import com.verywords.csms_android.data.local.database.AppDatabase
import com.verywords.csms_android.data.local.entity.MessageEntity

interface MessageRepository {

    suspend fun getMessages() :List<MessageEntity>

}