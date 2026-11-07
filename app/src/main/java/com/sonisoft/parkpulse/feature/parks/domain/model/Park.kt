package com.sonisoft.parkpulse.feature.parks.domain.model

data class Park(
    val id: String,
    val name: String,
    val type: ParkType,
    val areaInSquareMeters: Double,
    val perimeterInMeters: Double,
    val status: ParkStatus,
    val lastSurveyDate: Long?,
    val isPendingSync: Boolean,
    val boundaryPoints: List<GeoPoint>,
    val boundaryQuality: BoundaryQuality,
    val statusIsDemo: Boolean,
    val source: ParkSource?,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val remoteId: String? = null,
    val serverVersion: Int = 0,
    val baseServerVersion: Int = 0,
    val localModifiedAt: Long = 0
)
