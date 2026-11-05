package com.sonisoft.parkpulse.feature.parks.presentation.survey

import com.sonisoft.parkpulse.feature.parks.domain.model.CleanlinessRating
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey
import com.sonisoft.parkpulse.feature.parks.domain.model.SurveyPhoto
import com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ValidateCheckInUseCase
import java.io.File

enum class SurveyStep {
    CHECK_IN,
    FORM,
    REVIEW,
    SUCCESS,
    HISTORY
}

enum class SimulationScenario {
    REAL_GPS,
    INSIDE_PARK,
    OUTSIDE_PARK,
    CUSTOM
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

    // Simulation State (DEBUG ONLY)
    val isSimulationActive: Boolean = false,
    val simulationScenario: SimulationScenario = SimulationScenario.REAL_GPS,
    val selectedAccuracyMeters: Float = 3.0f,
    val customLatitudeText: String = "",
    val customLongitudeText: String = "",

    // Form Data
    val treeCountText: String = "0",
    val treeCountError: String? = null,
    val hasLighting: Boolean? = null,
    val lightingError: String? = null,
    val cleanliness: CleanlinessRating? = null,
    val cleanlinessError: String? = null,
    val notes: String = "",

    // Camera & Survey Photos
    val isCameraOpen: Boolean = false,
    val isProcessingPhoto: Boolean = false,
    val pendingWatermarkedPhotoFile: File? = null,
    val attachedPhotos: List<SurveyPhoto> = emptyList(),
    val selectedPhotoForViewer: SurveyPhoto? = null,

    // History
    val pastSurveys: List<Survey> = emptyList(),
    val pastPhotos: List<SurveyPhoto> = emptyList()
)
