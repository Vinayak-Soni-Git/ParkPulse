package com.sonisoft.parkpulse.feature.parks.domain.repository

import com.sonisoft.parkpulse.feature.parks.domain.model.DashboardStatistics
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import kotlinx.coroutines.flow.Flow

interface ParkRepository {
    fun getAllParks(): Flow<List<Park>>
    fun searchParks(query: String): Flow<List<Park>>
    fun filterParksByStatus(status: ParkStatus): Flow<List<Park>>
    fun searchAndFilterParks(query: String, status: ParkStatus): Flow<List<Park>>
    fun getParkById(id: String): Flow<Park?>
    fun getDashboardStatistics(): Flow<DashboardStatistics>
    suspend fun initializeDatabaseIfNeeded()
    suspend fun savePark(park: Park)
}
