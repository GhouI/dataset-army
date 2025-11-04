package com.bodyparts.vitals.repository

import com.bodyparts.vitals.model.AIResponse
import com.bodyparts.vitals.model.BodyPartsVitalsSubmission
import com.bodyparts.vitals.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VitalsRepository {
    private val aiService = RetrofitClient.aiService

    suspend fun submitToAI(submission: BodyPartsVitalsSubmission): Result<AIResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = aiService.submitVitals(submission)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to submit: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
