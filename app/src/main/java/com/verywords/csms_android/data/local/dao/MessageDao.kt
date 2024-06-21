package com.verywords.csms_android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.verywords.csms_android.data.local.entity.MessageEntity

@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM message_table")
    suspend fun getAllMessages(): List<MessageEntity>

    @Query("DELETE FROM message_table")
    suspend fun clearMessages()

    // id 값을 기준으로 데이터 삭제하는 함수
    @Query("DELETE FROM message_table WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Int)
}