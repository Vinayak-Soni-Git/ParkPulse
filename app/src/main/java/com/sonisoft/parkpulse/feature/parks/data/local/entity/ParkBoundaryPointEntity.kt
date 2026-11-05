package com.sonisoft.parkpulse.feature.parks.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "park_boundary_points",
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
data class ParkBoundaryPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parkId: String,
    val latitude: Double,
    val longitude: Double,
    val pointIndex: Int
)
