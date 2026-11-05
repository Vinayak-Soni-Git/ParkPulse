package com.sonisoft.parkpulse.feature.parks.presentation.survey

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonisoft.parkpulse.feature.parks.domain.location.LocationService
import com.sonisoft.parkpulse.feature.parks.domain.model.CleanlinessRating
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey
import com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import com.sonisoft.parkpulse.feature.parks.domain.usecase.GenerateTestCoordinatesUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ValidateCheckInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val parkRepository: ParkRepository,
    private val locationService: LocationService,
    private val validateCheckInUseCase: ValidateCheckInUseCase,
    private val generateTestCoordinatesUseCase: GenerateTestCoordinatesUseCase
) : ViewModel() {

    val parkId: String = checkNotNull(savedStateHandle["parkId"])

    private val _uiState = MutableStateFlow(SurveyUiState())
    val uiState: StateFlow<SurveyUiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null

    init {
        loadParkAndHistory()
        checkLocationPermission()
    }

    private fun loadParkAndHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            parkRepository.getParkById(parkId).collectLatest { park ->
                _uiState.update { state ->
                    val currentLocation = if (state.isSimulationActive) state.userLocation else state.userLocation
                    val checkInRes = validateCheckInUseCase(
                        userLocation = currentLocation,
                        hasPermission = state.hasLocationPermission || state.isSimulationActive,
                        park = park
                    )

                    logGeofenceValidation(park?.id, park?.name, currentLocation, checkInRes)

                    state.copy(
                        park = park,
                        isLoading = false,
                        checkInResult = checkInRes
                    )
                }
            }
        }

        viewModelScope.launch {
            parkRepository.getSurveysForPark(parkId).collectLatest { surveys ->
                _uiState.update { it.copy(pastSurveys = surveys) }
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
        if (granted) {
            startLocationUpdates()
        }
    }

    fun checkLocationPermission() {
        val granted = locationService.hasLocationPermission()
        _uiState.update { it.copy(hasLocationPermission = granted) }
        if (granted) {
            startLocationUpdates()
        }
    }

    fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationService.requestDetailedLocationUpdates().collectLatest { location ->
                _uiState.update { state ->
                    if (state.isSimulationActive) {
                        // Don't overwrite active simulation with real GPS
                        state
                    } else {
                        val checkInRes = validateCheckInUseCase(
                            userLocation = location,
                            hasPermission = state.hasLocationPermission,
                            park = state.park
                        )

                        logGeofenceValidation(state.park?.id, state.park?.name, location, checkInRes)

                        state.copy(
                            userLocation = location,
                            checkInResult = checkInRes
                        )
                    }
                }
            }
        }
    }

    // DEBUG SIMULATION METHODS
    fun applySimulation(
        scenario: SimulationScenario,
        accuracyMeters: Float,
        customLatText: String = "",
        customLonText: String = ""
    ) {
        val park = _uiState.value.park

        val targetPoint: GeoPoint? = when (scenario) {
            SimulationScenario.REAL_GPS -> {
                resetToRealGps()
                return
            }
            SimulationScenario.INSIDE_PARK -> {
                if (park != null) generateTestCoordinatesUseCase.generateInsidePoint(park) else null
            }
            SimulationScenario.OUTSIDE_PARK -> {
                if (park != null) generateTestCoordinatesUseCase.generateOutsidePoint(park) else null
            }
            SimulationScenario.CUSTOM -> {
                val lat = customLatText.toDoubleOrNull()
                val lon = customLonText.toDoubleOrNull()
                if (lat != null && lon != null) GeoPoint(lat, lon) else null
            }
        }

        if (targetPoint == null) {
            _uiState.update { it.copy(error = "Could not generate coordinates for selected scenario.") }
            return
        }

        val simLocation = UserLocation(
            latitude = targetPoint.latitude,
            longitude = targetPoint.longitude,
            accuracyMeters = accuracyMeters,
            timestamp = System.currentTimeMillis(),
            isSimulated = true
        )

        _uiState.update { state ->
            val checkInRes = validateCheckInUseCase(
                userLocation = simLocation,
                hasPermission = true, // Simulation bypasses permission check for testing eligibility
                park = state.park
            )

            logGeofenceValidation(state.park?.id, state.park?.name, simLocation, checkInRes)

            state.copy(
                isSimulationActive = true,
                simulationScenario = scenario,
                selectedAccuracyMeters = accuracyMeters,
                customLatitudeText = customLatText,
                customLongitudeText = customLonText,
                userLocation = simLocation,
                checkInResult = checkInRes,
                error = null
            )
        }
    }

    fun resetToRealGps() {
        _uiState.update { state ->
            state.copy(
                isSimulationActive = false,
                simulationScenario = SimulationScenario.REAL_GPS,
                error = null
            )
        }
        startLocationUpdates()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun logGeofenceValidation(
        parkId: String?,
        parkName: String?,
        location: UserLocation?,
        result: ValidateCheckInUseCase.Result
    ) {
        try {
            Log.d("SurveyGeofence", "--- Geofence Validation Diagnostic ---")
            Log.d("SurveyGeofence", "Selected Park ID: $parkId ($parkName)")
            Log.d("SurveyGeofence", "User Location: lat=${location?.latitude}, lon=${location?.longitude}, acc=${location?.accuracyMeters}m, sim=${location?.isSimulated}")
            Log.d("SurveyGeofence", "Validation Status: ${result.status}, Eligible: ${result.isEligibleForCheckIn}")
            Log.d("SurveyGeofence", "Message: ${result.message}")
        } catch (_: Throwable) {
            // Ignored during JVM unit tests
        }
    }

    fun performCheckIn() {
        val state = _uiState.value
        val res = state.checkInResult

        // DEFENSIVE SAFETY GUARD: Block simulated check-ins
        if (state.isSimulationActive || res?.userLocation?.isSimulated == true) {
            _uiState.update {
                it.copy(error = "Simulated location cannot be used for genuine check-in. Reset to Real GPS first.")
            }
            return
        }

        if (res?.isEligibleForCheckIn == true && res.userLocation != null) {
            _uiState.update {
                it.copy(
                    validatedLocation = res.userLocation,
                    checkInTimestamp = System.currentTimeMillis(),
                    step = SurveyStep.FORM
                )
            }
        }
    }

    fun onTreeCountChanged(value: String) {
        val error = when {
            value.isBlank() -> "Tree count is required"
            value.toIntOrNull() == null -> "Please enter a valid number"
            (value.toIntOrNull() ?: -1) < 0 -> "Tree count cannot be negative"
            else -> null
        }
        _uiState.update { it.copy(treeCountText = value, treeCountError = error) }
    }

    fun onLightingSelected(hasLighting: Boolean) {
        _uiState.update { it.copy(hasLighting = hasLighting, lightingError = null) }
    }

    fun onCleanlinessSelected(rating: CleanlinessRating) {
        _uiState.update { it.copy(cleanliness = rating, cleanlinessError = null) }
    }

    fun onNotesChanged(notes: String) {
        if (notes.length <= 500) {
            _uiState.update { it.copy(notes = notes) }
        }
    }

    fun proceedToReview() {
        val state = _uiState.value
        val count = state.treeCountText.toIntOrNull()
        var hasError = false

        val treeError = when {
            count == null -> "Please enter a valid number"
            count < 0 -> "Tree count cannot be negative"
            else -> null
        }
        if (treeError != null) hasError = true

        val lightingError = if (state.hasLighting == null) "Please select lighting status" else null
        if (lightingError != null) hasError = true

        val cleanlinessError = if (state.cleanliness == null) "Please select cleanliness rating" else null
        if (cleanlinessError != null) hasError = true

        if (hasError) {
            _uiState.update {
                it.copy(
                    treeCountError = treeError,
                    lightingError = lightingError,
                    cleanlinessError = cleanlinessError
                )
            }
            return
        }

        _uiState.update { it.copy(step = SurveyStep.REVIEW) }
    }

    fun saveSurvey() {
        val state = _uiState.value

        // DEFENSIVE SAFETY GUARD: Block simulated survey persistence
        if (state.isSimulationActive || state.userLocation?.isSimulated == true || state.validatedLocation?.isSimulated == true) {
            _uiState.update { it.copy(error = "Simulated location cannot create a real survey record.") }
            return
        }

        val park = state.park ?: return
        val loc = state.validatedLocation ?: return
        val trees = state.treeCountText.toIntOrNull() ?: 0
        val lighting = state.hasLighting ?: return
        val cleanliness = state.cleanliness ?: return

        if (state.checkInResult?.isEligibleForCheckIn != true) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val now = System.currentTimeMillis()
            val survey = Survey(
                id = UUID.randomUUID().toString(),
                parkId = park.id,
                checkInTimestamp = state.checkInTimestamp,
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracyMeters = loc.accuracyMeters,
                treeCount = trees,
                hasLighting = lighting,
                cleanliness = cleanliness,
                notes = state.notes.ifBlank { null },
                createdAt = now,
                updatedAt = now,
                isPendingSync = true
            )

            parkRepository.saveSurvey(survey)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    step = SurveyStep.SUCCESS
                )
            }
        }
    }

    fun setStep(step: SurveyStep) {
        _uiState.update { it.copy(step = step) }
    }
}
