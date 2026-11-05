package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.DashboardStatistics
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetParkStatisticsUseCase @Inject constructor(
    private val repository: ParkRepository
) {
    operator fun invoke(): Flow<DashboardStatistics> {
        return repository.getDashboardStatistics()
    }
}
