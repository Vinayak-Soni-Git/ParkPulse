package com.sonisoft.parkpulse.feature.parks.domain.model

data class ParkSource(
    val provider: String,
    val osmType: String?,
    val osmId: Long?,
    val attribution: String,
    val license: String
)
