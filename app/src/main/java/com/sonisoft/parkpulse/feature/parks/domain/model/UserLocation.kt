package com.sonisoft.parkpulse.feature.parks.domain.model

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestamp: Long
)
