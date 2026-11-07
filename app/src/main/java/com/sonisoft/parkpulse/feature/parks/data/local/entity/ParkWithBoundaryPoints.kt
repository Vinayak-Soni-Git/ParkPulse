package com.sonisoft.parkpulse.feature.parks.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkSource

data class ParkWithBoundaryPoints(
    @Embedded val park: ParkEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "parkId"
    )
    val boundaryPoints: List<ParkBoundaryPointEntity>
) {
    fun toDomainModel(): Park {
        val sortedPoints = boundaryPoints.sortedBy { it.pointIndex }.map {
            GeoPoint(latitude = it.latitude, longitude = it.longitude)
        }

        val source = if (park.sourceProvider != null && park.sourceAttribution != null && park.sourceLicense != null) {
            ParkSource(
                provider = park.sourceProvider,
                osmType = park.sourceOsmType,
                osmId = park.sourceOsmId,
                attribution = park.sourceAttribution,
                license = park.sourceLicense
            )
        } else null

        return Park(
            id = park.id,
            name = park.name,
            type = park.type,
            areaInSquareMeters = park.areaInSquareMeters,
            perimeterInMeters = park.perimeterInMeters,
            status = park.status,
            lastSurveyDate = park.lastSurveyDate,
            isPendingSync = park.isPendingSync,
            boundaryPoints = sortedPoints,
            boundaryQuality = park.boundaryQuality,
            statusIsDemo = park.statusIsDemo,
            source = source,
            syncStatus = com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.fromString(park.syncStatus),
            remoteId = park.remoteId
        )
    }
}
