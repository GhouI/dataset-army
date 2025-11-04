package com.bodyparts.vitals.network

import com.bodyparts.vitals.model.AIResponse
import com.bodyparts.vitals.model.BodyPartsVitalsSubmission
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AIService {
    @POST("api/analyze")
    suspend fun submitVitals(@Body submission: BodyPartsVitalsSubmission): Response<AIResponse>
}
