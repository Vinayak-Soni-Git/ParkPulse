package com.sonisoft.parkpulse.feature.parks.data.repository

import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey
import com.sonisoft.parkpulse.feature.parks.domain.model.SurveyPhoto
import com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus
import com.sonisoft.parkpulse.feature.parks.domain.network.MockSyncApi
import com.sonisoft.parkpulse.feature.parks.domain.network.SyncResult
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import com.sonisoft.parkpulse.feature.parks.domain.repository.SyncRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val parkRepository: ParkRepository,
    private val syncApi: MockSyncApi
) : SyncRepository {

    override suspend fun getPendingParks(): List<Park> {
        return parkRepository.getUnsyncedParks().first()
    }

    override suspend fun getPendingSurveys(): List<Survey> {
        return parkRepository.getUnsyncedSurveys().first()
    }

    override suspend fun getPendingPhotos(): List<SurveyPhoto> {
        return parkRepository.getUnsyncedPhotos().first()
    }

    override suspend fun uploadPark(park: Park): Boolean {
        parkRepository.updateParkSyncStatus(park.id, SyncStatus.SYNCING, park.remoteId, park.serverVersion)
        val result = syncApi.uploadPark(park)

        return when (result) {
            is SyncResult.Success -> {
                parkRepository.updateParkSyncStatus(park.id, SyncStatus.SYNCED, result.remoteId, result.serverVersion)
                true
            }
            is SyncResult.Conflict -> {
                parkRepository.updateParkSyncStatus(park.id, SyncStatus.CONFLICT, park.remoteId, park.serverVersion)
                false
            }
            is SyncResult.Error -> {
                parkRepository.updateParkSyncStatus(park.id, SyncStatus.FAILED, park.remoteId, park.serverVersion)
                false
            }
        }
    }

    override suspend fun uploadSurvey(survey: Survey): Boolean {
        parkRepository.updateSurveySyncStatus(survey.id, SyncStatus.SYNCING, survey.remoteId)
        val result = syncApi.uploadSurvey(survey)

        return if (result is SyncResult.Success) {
            parkRepository.updateSurveySyncStatus(survey.id, SyncStatus.SYNCED, result.remoteId)
            true
        } else {
            parkRepository.updateSurveySyncStatus(survey.id, SyncStatus.FAILED, survey.remoteId)
            false
        }
    }

    override suspend fun uploadPhoto(photo: SurveyPhoto): Boolean {
        parkRepository.updatePhotoSyncStatus(photo.id, SyncStatus.SYNCING, photo.remoteId)
        val result = syncApi.uploadSurveyPhoto(photo)

        return if (result is SyncResult.Success) {
            parkRepository.updatePhotoSyncStatus(photo.id, SyncStatus.SYNCED, result.remoteId)
            true
        } else {
            parkRepository.updatePhotoSyncStatus(photo.id, SyncStatus.FAILED, photo.remoteId)
            false
        }
    }
}
