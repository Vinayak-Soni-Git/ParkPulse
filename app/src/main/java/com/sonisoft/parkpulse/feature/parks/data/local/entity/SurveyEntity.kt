package com.sonisoft.parkpulse.feature.parks.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sonisoft.parkpulse.feature.parks.domain.model.CleanlinessRating
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey

@Entity(
    tableName = "surveys",
    foreignKeys = [
        ForeignKey(
            entity = ParkEntity::class,
            parentColumns = ["id"],
            childColumns = ["parkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parkId")
    ]
)
data class SurveyEntity(
    @PrimaryKey
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
    val isPendingSync: Boolean
) {
    fun toDomainModel(): Survey {
        return Survey(
            id = id,
            parkId = parkId,
            checkInTimestamp = checkInTimestamp,
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracyMeters,
            treeCount = treeCount,
            hasLighting = hasLighting,
            cleanliness = cleanliness,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isPendingSync = isPendingSync
        )
    }

    companion object {
        fun fromDomainModel(survey: Survey): SurveyEntity {
            return SurveyEntity(
                id = survey.id,
                parkId = survey.parkId,
                checkInTimestamp = survey.checkInTimestamp,
                latitude = survey.latitude,
                longitude = survey.longitude,
                accuracyMeters = survey.accuracyMeters,
                treeCount = survey.treeCount,
                hasLighting = survey.hasLighting,
                cleanliness = survey.cleanliness,
                notes = survey.notes,
                createdAt = survey.createdAt,
                updatedAt = survey.updatedAt,
                isPendingSync = survey.isPendingSync
            )
        }
    }
}
