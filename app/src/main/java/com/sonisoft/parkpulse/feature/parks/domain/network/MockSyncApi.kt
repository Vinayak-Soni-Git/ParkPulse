package com.sonisoft.parkpulse.feature.parks.domain.network

import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey
import com.sonisoft.parkpulse.feature.parks.domain.model.SurveyPhoto
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockSyncApi @Inject constructor() {

    // Simulates an API call with realistic latency and occasional failure
    private suspend fun <T> simulateApiCall(onSuccessRemoteId: String): SyncResult<T> {
        delay(Random.nextLong(300, 1200)) // 300ms to 1.2s delay

        // 10% chance of random network/server failure for realism
        val shouldFail = Random.nextFloat() < 0.10f
        return if (shouldFail) {
            SyncResult.Error("Simulated server timeout or 503 error.")
        } else {
            SyncResult.Success(onSuccessRemoteId)
        }
    }

    suspend fun uploadPark(park: Park): SyncResult<Park> {
        // Mock backend returns a prefixed remote ID mapping to the local UUID
        return simulateApiCall("remote_park_${park.id}")
    }

    suspend fun uploadSurvey(survey: Survey): SyncResult<Survey> {
        return simulateApiCall("remote_survey_${survey.id}")
    }

    suspend fun uploadSurveyPhoto(photo: SurveyPhoto): SyncResult<SurveyPhoto> {
        // Simulating payload upload overhead
        delay(500)
        return simulateApiCall("remote_photo_${photo.id}")
    }
}
