package com.sonisoft.parkpulse.feature.parks.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sonisoft.parkpulse.feature.parks.domain.model.SurveyPhoto

@Entity(
    tableName = "survey_photos",
    foreignKeys = [
        ForeignKey(
            entity = SurveyEntity::class,
            parentColumns = ["id"],
            childColumns = ["surveyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("surveyId"),
        Index("parkId")
    ]
)
data class SurveyPhotoEntity(
    @PrimaryKey
    val id: String,
    val surveyId: String,
    val parkId: String,
    val localFilePath: String,
    val capturedAt: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val createdAt: Long,
    val isPendingSync: Boolean,
    val syncStatus: String,
    val remoteId: String? = null
) {
    fun toDomainModel(): SurveyPhoto {
        return SurveyPhoto(
            id = id,
            surveyId = surveyId,
            parkId = parkId,
            localFilePath = localFilePath,
            capturedAt = capturedAt,
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracyMeters,
            createdAt = createdAt,
            isPendingSync = isPendingSync,
            syncStatus = com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.fromString(syncStatus),
            remoteId = remoteId
        )
    }

    companion object {
        fun fromDomainModel(photo: SurveyPhoto): SurveyPhotoEntity {
            return SurveyPhotoEntity(
                id = photo.id,
                surveyId = photo.surveyId,
                parkId = photo.parkId,
                localFilePath = photo.localFilePath,
                capturedAt = photo.capturedAt,
                latitude = photo.latitude,
                longitude = photo.longitude,
                accuracyMeters = photo.accuracyMeters,
                createdAt = photo.createdAt,
                isPendingSync = photo.isPendingSync,
                syncStatus = photo.syncStatus.name,
                remoteId = photo.remoteId
            )
        }
    }
}
