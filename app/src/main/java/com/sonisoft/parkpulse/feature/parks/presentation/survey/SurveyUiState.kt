package com.sonisoft.parkpulse.feature.parks.presentation.survey

import com.sonisoft.parkpulse.feature.parks.domain.model.CleanlinessRating
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey
import com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ValidateCheckInUseCase

enum class SurveyStep {
    CHECK_IN,
    FORM,
    REVIEW,
    SUCCESS,
    HISTORY
}

data class SurveyUiState(
    val step: SurveyStep = SurveyStep.CHECK_IN,
    val park: Park? = null,
    val isLoading: Boolean = false,
    val error: String? = null,

    // Location & Check-in
    val userLocation: UserLocation? = null,
    val hasLocationPermission: Boolean = false,
    val checkInResult: ValidateCheckInUseCase.Result? = null,
    val validatedLocation: UserLocation? = null,
    val checkInTimestamp: Long = 0L,

    // Form Data
    val treeCountText: String = "0",
    val treeCountError: String? = null,
    val hasLighting: Boolean? = null,
    val lightingError: String? = null,
    val cleanliness: CleanlinessRating? = null,
    val cleanlinessError: String? = null,
    val notes: String = "",

    // History
    val pastSurveys: List<Survey> = emptyList()
)
