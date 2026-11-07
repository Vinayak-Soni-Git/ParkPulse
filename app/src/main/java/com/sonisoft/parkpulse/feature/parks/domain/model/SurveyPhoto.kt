package com.sonisoft.parkpulse.feature.parks.domain.model

data class SurveyPhoto(
    val id: String,
    val surveyId: String,
    val parkId: String,
    val localFilePath: String,
    val capturedAt: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val createdAt: Long,
    val isPendingSync: Boolean = true,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val remoteId: String? = null
)
