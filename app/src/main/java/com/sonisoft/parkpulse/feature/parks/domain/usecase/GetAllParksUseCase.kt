package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllParksUseCase @Inject constructor(
    private val repository: ParkRepository
) {
    operator fun invoke(): Flow<List<Park>> {
        return repository.getAllParks()
    }
}
