package com.sonisoft.parkpulse.feature.parks.domain.repository

import com.sonisoft.parkpulse.feature.parks.domain.model.DashboardStatistics
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey
import com.sonisoft.parkpulse.feature.parks.domain.model.SurveyPhoto
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
    suspend fun saveSurvey(survey: Survey)
    fun getSurveysForPark(parkId: String): Flow<List<Survey>>
    suspend fun saveSurveyPhoto(photo: SurveyPhoto)
    suspend fun saveSurveyPhotos(photos: List<SurveyPhoto>)
    fun getPhotosForSurvey(surveyId: String): Flow<List<SurveyPhoto>>
    fun getPhotosForPark(parkId: String): Flow<List<SurveyPhoto>>
}
