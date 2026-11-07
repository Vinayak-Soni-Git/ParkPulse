package com.sonisoft.parkpulse.feature.parks.data.repository

import com.sonisoft.parkpulse.feature.parks.data.local.dao.ParkDao
import com.sonisoft.parkpulse.feature.parks.data.local.dao.SurveyDao
import com.sonisoft.parkpulse.feature.parks.data.local.dao.SurveyPhotoDao
import com.sonisoft.parkpulse.feature.parks.data.local.entity.SurveyEntity
import com.sonisoft.parkpulse.feature.parks.data.local.entity.SurveyPhotoEntity
import com.sonisoft.parkpulse.feature.parks.data.seed.ParkSeedImporter
import com.sonisoft.parkpulse.feature.parks.domain.model.DashboardStatistics
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey
import com.sonisoft.parkpulse.feature.parks.domain.model.SurveyPhoto
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkRepositoryImpl @Inject constructor(
    private val parkDao: ParkDao,
    private val surveyDao: SurveyDao,
    private val surveyPhotoDao: SurveyPhotoDao,
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

    override suspend fun savePark(park: Park) {
        val parkEntity = com.sonisoft.parkpulse.feature.parks.data.local.entity.ParkEntity(
            id = park.id,
            name = park.name,
            type = park.type,
            areaInSquareMeters = park.areaInSquareMeters,
            perimeterInMeters = park.perimeterInMeters,
            status = park.status,
            lastSurveyDate = park.lastSurveyDate,
            isPendingSync = park.isPendingSync,
            boundaryQuality = park.boundaryQuality,
            statusIsDemo = park.statusIsDemo,
            sourceProvider = park.source?.provider,
            sourceOsmType = park.source?.osmType,
            sourceOsmId = park.source?.osmId,
            sourceAttribution = park.source?.attribution,
            sourceLicense = park.source?.license,
            syncStatus = park.syncStatus.name,
            remoteId = park.remoteId
        )
        
        val pointEntities = park.boundaryPoints.mapIndexed { index, point ->
            com.sonisoft.parkpulse.feature.parks.data.local.entity.ParkBoundaryPointEntity(
                parkId = park.id,
                latitude = point.latitude,
                longitude = point.longitude,
                pointIndex = index
            )
        }
        
        parkDao.insertParksWithBoundaries(listOf(parkEntity), pointEntities)
    }

    override suspend fun saveSurvey(survey: Survey) {
        surveyDao.insertSurvey(SurveyEntity.fromDomainModel(survey))
        parkDao.updateParkLastSurveyDate(survey.parkId, survey.createdAt)
    }

    override fun getSurveysForPark(parkId: String): Flow<List<Survey>> {
        return surveyDao.getSurveysForPark(parkId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun saveSurveyPhoto(photo: SurveyPhoto) {
        surveyPhotoDao.insertSurveyPhoto(SurveyPhotoEntity.fromDomainModel(photo))
    }

    override suspend fun saveSurveyPhotos(photos: List<SurveyPhoto>) {
        surveyPhotoDao.insertSurveyPhotos(photos.map { SurveyPhotoEntity.fromDomainModel(it) })
    }

    override fun getPhotosForSurvey(surveyId: String): Flow<List<SurveyPhoto>> {
        return surveyPhotoDao.getPhotosForSurvey(surveyId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getPhotosForPark(parkId: String): Flow<List<SurveyPhoto>> {
        return surveyPhotoDao.getPhotosForPark(parkId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getUnsyncedParks(): Flow<List<Park>> {
        return parkDao.getUnsyncedParks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateParkSyncStatus(parkId: String, syncStatus: com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus, remoteId: String?) {
        parkDao.updateParkSyncStatus(parkId, syncStatus.name, remoteId)
    }

    override fun getUnsyncedSurveys(): Flow<List<Survey>> {
        return surveyDao.getUnsyncedSurveys().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateSurveySyncStatus(surveyId: String, syncStatus: com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus, remoteId: String?) {
        surveyDao.updateSurveySyncStatus(surveyId, syncStatus.name, remoteId)
    }

    override fun getUnsyncedPhotos(): Flow<List<SurveyPhoto>> {
        return surveyPhotoDao.getUnsyncedPhotos().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updatePhotoSyncStatus(photoId: String, syncStatus: com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus, remoteId: String?) {
        surveyPhotoDao.updatePhotoSyncStatus(photoId, syncStatus.name, remoteId)
    }
}
