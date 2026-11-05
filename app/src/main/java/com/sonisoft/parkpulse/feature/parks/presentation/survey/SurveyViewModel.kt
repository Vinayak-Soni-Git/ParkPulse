package com.sonisoft.parkpulse.feature.parks.presentation.survey

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonisoft.parkpulse.feature.parks.domain.location.LocationService
import com.sonisoft.parkpulse.feature.parks.domain.model.CleanlinessRating
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
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
    private val validateCheckInUseCase: ValidateCheckInUseCase
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
                _uiState.update { it.copy(park = park, isLoading = false) }
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
                    val checkInRes = state.park?.let { park ->
                        validateCheckInUseCase(
                            userLocation = location,
                            hasPermission = state.hasLocationPermission,
                            parkBoundary = park.boundaryPoints
                        )
                    }

                    state.copy(
                        userLocation = location,
                        checkInResult = checkInRes
                    )
                }
            }
        }
    }

    fun performCheckIn() {
        val state = _uiState.value
        val res = state.checkInResult
        if (res is ValidateCheckInUseCase.Result.Success) {
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
        val park = state.park ?: return
        val loc = state.validatedLocation ?: return
        val trees = state.treeCountText.toIntOrNull() ?: 0
        val lighting = state.hasLighting ?: return
        val cleanliness = state.cleanliness ?: return

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
