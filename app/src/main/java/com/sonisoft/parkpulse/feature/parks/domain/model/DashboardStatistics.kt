package com.sonisoft.parkpulse.feature.parks.domain.model

data class DashboardStatistics(
    val totalParks: Int = 0,
    val openParks: Int = 0,
    val needsRepairParks: Int = 0,
    val closedParks: Int = 0
)
