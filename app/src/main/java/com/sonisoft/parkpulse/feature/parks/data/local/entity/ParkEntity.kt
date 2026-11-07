package com.sonisoft.parkpulse.feature.parks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType

@Entity(tableName = "parks")
data class ParkEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: ParkType,
    val areaInSquareMeters: Double,
    val perimeterInMeters: Double,
    val status: ParkStatus,
    val lastSurveyDate: Long?,
    val isPendingSync: Boolean,
    val boundaryQuality: BoundaryQuality,
    val statusIsDemo: Boolean,
    // Source fields embedded flatly
    val sourceProvider: String?,
    val sourceOsmType: String?,
    val sourceOsmId: Long?,
    val sourceAttribution: String?,
    val sourceLicense: String?,
    val syncStatus: String = "PENDING",
    val remoteId: String? = null
)
