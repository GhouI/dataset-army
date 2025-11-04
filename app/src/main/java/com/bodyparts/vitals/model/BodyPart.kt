package com.bodyparts.vitals.model

import android.graphics.RectF

data class BodyPart(
    val id: String,
    val name: String,
    val region: RectF,
    var isSelected: Boolean = false,
    var vitals: Vitals? = null
)

data class Vitals(
    val bodyPartId: String,
    val temperature: String = "",
    val heartRate: String = "",
    val bloodPressure: String = "",
    val painLevel: String = "",
    val notes: String = ""
)

data class BodyPartsVitalsSubmission(
    val timestamp: Long,
    val selectedBodyParts: List<BodyPart>,
    val vitalsData: List<Vitals>
)

data class AIResponse(
    val success: Boolean,
    val message: String,
    val diagnosis: String? = null,
    val recommendations: List<String>? = null
)
