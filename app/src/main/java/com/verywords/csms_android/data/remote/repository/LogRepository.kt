package com.verywords.csms_android.data.remote.repository

import com.verywords.csms_android.ui.model.Message

interface LogRepository {

    suspend fun sendEvent(message: Message): Result<Boolean>

}