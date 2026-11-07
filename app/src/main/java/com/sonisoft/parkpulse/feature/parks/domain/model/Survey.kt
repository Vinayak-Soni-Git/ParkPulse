package com.sonisoft.parkpulse.feature.parks.domain.model

data class Survey(
    val id: String,
    val parkId: String,
    val checkInTimestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val treeCount: Int,
    val hasLighting: Boolean,
    val cleanliness: CleanlinessRating,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isPendingSync: Boolean = true,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val remoteId: String? = null
)
