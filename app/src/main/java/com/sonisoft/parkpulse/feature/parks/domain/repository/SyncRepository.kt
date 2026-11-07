package com.sonisoft.parkpulse.feature.parks.domain.repository

import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey
import com.sonisoft.parkpulse.feature.parks.domain.model.SurveyPhoto
import com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus

interface SyncRepository {
    suspend fun getPendingParks(): List<Park>
    suspend fun getPendingSurveys(): List<Survey>
    suspend fun getPendingPhotos(): List<SurveyPhoto>

    suspend fun uploadPark(park: Park): Boolean
    suspend fun uploadSurvey(survey: Survey): Boolean
    suspend fun uploadPhoto(photo: SurveyPhoto): Boolean
}
