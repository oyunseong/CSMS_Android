package com.verywords.csms_android.data.local.repository

import com.verywords.csms_android.ui.model.Message

interface MessageRepository {

    suspend fun getAllEventMessages() :List<Message>

    suspend fun insertEventMessage(message: Message)

    suspend fun deleteAllEventMessage()

    suspend fun deleteEventMessageById(messageId: Int)

}