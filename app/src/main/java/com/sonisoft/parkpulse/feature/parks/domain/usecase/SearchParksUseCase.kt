package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchParksUseCase @Inject constructor(
    private val repository: ParkRepository
) {
    operator fun invoke(query: String, status: ParkStatus?): Flow<List<Park>> {
        return if (query.isBlank() && status == null) {
            repository.getAllParks()
        } else if (query.isBlank() && status != null) {
            repository.filterParksByStatus(status)
        } else if (query.isNotBlank() && status == null) {
            repository.searchParks(query)
        } else {
            repository.searchAndFilterParks(query, status!!)
        }
    }
}
