package com.verywords.csms_android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.verywords.csms_android.data.local.entity.MessageEntity

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM message_table")
    suspend fun getAllMessages(): List<MessageEntity>
}