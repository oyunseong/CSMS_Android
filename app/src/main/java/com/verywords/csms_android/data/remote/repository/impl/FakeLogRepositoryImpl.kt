package com.verywords.csms_android.data.remote.repository.impl

import com.verywords.csms_android.data.remote.repository.LogRepository
import com.verywords.csms_android.ui.model.Message
import com.verywords.csms_android.utils.log
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class FakeLogRepositoryImpl @Inject constructor() : LogRepository {
    var cnt = 0
    override suspend fun sendEvent(message: Message): Result<Boolean> {
        return try {
            cnt++
            withTimeout(100) {
                if(cnt % 3 == 0) delay(200) // 2초 뒤에 실패 전송
                val isSuccessful = true
                if (isSuccessful) {
                    log(message = "Success to send message with id ${message.id}")
                    Result.success(true)
                } else {
                    Result.failure(Exception("API request failed"))
                }
            }
        } catch (e: Exception) {
            log(message = "Failed to send message with id ${message.id}: ${e.message}")
            Result.failure(e)
        }
    }
}