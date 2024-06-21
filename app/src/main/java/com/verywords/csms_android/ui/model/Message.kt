package com.verywords.csms_android.ui.model

import com.verywords.csms_android.data.local.entity.MessageEntity

data class Message(
    val id: Int = 0,
    val createAt: Long,
    val message: String,
    val errorMessage: String = "",
)

fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        createAt = createAt,
        message = message,
        errorMessage = errorMessage
    )
}

fun List<MessageEntity>.toUiModel(): List<Message> {
    return map {
        Message(
            id = it.id,
            createAt = it.createAt,
            message = it.message,
            errorMessage = it.errorMessage
        )
    }
}