package com.sonisoft.parkpulse.feature.parks.presentation

import com.sonisoft.parkpulse.feature.parks.domain.model.DashboardStatistics
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus

data class ParksUiState(
    val parks: List<Park> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: ParkStatus? = null,
    val statistics: DashboardStatistics = DashboardStatistics(),
    val isLoading: Boolean = true,
    val isInitializingDatabase: Boolean = true,
    val errorMessage: String? = null
)
