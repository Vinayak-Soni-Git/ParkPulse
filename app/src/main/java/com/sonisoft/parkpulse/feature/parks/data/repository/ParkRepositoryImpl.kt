package com.sonisoft.parkpulse.feature.parks.data.repository

import com.sonisoft.parkpulse.feature.parks.data.local.dao.ParkDao
import com.sonisoft.parkpulse.feature.parks.data.seed.ParkSeedImporter
import com.sonisoft.parkpulse.feature.parks.domain.model.DashboardStatistics
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkRepositoryImpl @Inject constructor(
    private val parkDao: ParkDao,
    private val parkSeedImporter: ParkSeedImporter
) : ParkRepository {

    override fun getAllParks(): Flow<List<Park>> {
        return parkDao.getAllParks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun searchParks(query: String): Flow<List<Park>> {
        return parkDao.searchParks(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun filterParksByStatus(status: ParkStatus): Flow<List<Park>> {
        return parkDao.filterParksByStatus(status).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun searchAndFilterParks(query: String, status: ParkStatus): Flow<List<Park>> {
        return parkDao.searchAndFilterParks(query, status).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getParkById(id: String): Flow<Park?> {
        return parkDao.getParkById(id).map { it?.toDomainModel() }
    }

    override fun getDashboardStatistics(): Flow<DashboardStatistics> {
        return combine(
            parkDao.getParksCountFlow(),
            parkDao.getOpenParksCountFlow(),
            parkDao.getNeedsRepairParksCountFlow(),
            parkDao.getClosedParksCountFlow()
        ) { total, open, repair, closed ->
            DashboardStatistics(
                totalParks = total,
                openParks = open,
                needsRepairParks = repair,
                closedParks = closed
            )
        }
    }

    override suspend fun initializeDatabaseIfNeeded() {
        parkSeedImporter.seedDatabaseIfNeeded()
    }
}
