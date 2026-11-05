package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import javax.inject.Inject

class InitializeParkDatabaseUseCase @Inject constructor(
    private val repository: ParkRepository
) {
    suspend operator fun invoke() {
        repository.initializeDatabaseIfNeeded()
    }
}
