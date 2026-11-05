package com.sonisoft.parkpulse.feature.parks.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonisoft.parkpulse.feature.parks.domain.location.LocationService
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.usecase.CalculateDistanceAndBearingUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.CalculateAreaAndPerimeterUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.GetAllParksUseCase
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getAllParksUseCase: GetAllParksUseCase,
    private val parkRepository: ParkRepository,
    private val locationService: LocationService,
    private val calculateDistanceAndBearingUseCase: CalculateDistanceAndBearingUseCase,
    private val calculateAreaAndPerimeterUseCase: CalculateAreaAndPerimeterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadParks()
        checkLocationPermission()
    }

    private fun loadParks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getAllParksUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Failed to load parks")
                    }
                }
                .collectLatest { parks ->
                    _uiState.update { it.copy(isLoading = false, parks = parks) }
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

    private var locationJob: kotlinx.coroutines.Job? = null

    private fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationService.requestLocationUpdates().collectLatest { location ->
                _uiState.update { state ->
                    val distanceAndBearing = state.selectedPark?.let { park ->
                        calculateDistanceToPark(location, park)
                    }

                    state.copy(
                        userLocation = location ?: state.userLocation,
                        distanceToSelectedPark = distanceAndBearing?.distanceInMeters,
                        bearingToSelectedPark = distanceAndBearing?.compassDirection
                    )
                }
            }
        }
    }

    fun selectPark(park: Park?) {
        val location = _uiState.value.userLocation
        val distanceAndBearing = if (park != null) calculateDistanceToPark(location, park) else null

        _uiState.update {
            it.copy(
                selectedPark = park,
                distanceToSelectedPark = distanceAndBearing?.distanceInMeters,
                bearingToSelectedPark = distanceAndBearing?.compassDirection
            )
        }
    }

    private fun calculateDistanceToPark(
        userLocation: GeoPoint?,
        park: Park
    ): CalculateDistanceAndBearingUseCase.Result? {
        if (userLocation == null || park.boundaryPoints.isEmpty()) return null

        // Use centroid or first point for distance calculation
        val centerLat = park.boundaryPoints.map { it.latitude }.average()
        val centerLon = park.boundaryPoints.map { it.longitude }.average()
        val parkCenter = GeoPoint(centerLat, centerLon)

        return calculateDistanceAndBearingUseCase(userLocation, parkCenter)
    }

    // Phase 4: Drawing Mode Methods
    fun toggleDrawingMode() {
        _uiState.update { 
            it.copy(
                isDrawingMode = !it.isDrawingMode,
                drawingPoints = emptyList(),
                drawingArea = 0.0,
                drawingPerimeter = 0.0,
                showCreateParkForm = false,
                selectedPark = if (it.isDrawingMode) it.selectedPark else null // Deselect if entering
            )
        }
    }

    fun addDrawingPoint(point: GeoPoint) {
        if (!_uiState.value.isDrawingMode) return
        
        // Prevent duplicate consecutive points
        val currentPoints = _uiState.value.drawingPoints
        if (currentPoints.isNotEmpty()) {
            val lastPoint = currentPoints.last()
            if (lastPoint.latitude == point.latitude && lastPoint.longitude == point.longitude) {
                return
            }
        }
        
        updateDrawingPoints(currentPoints + point)
    }

    fun updateDrawingPoint(index: Int, newPoint: GeoPoint) {
        if (!_uiState.value.isDrawingMode) return
        val currentPoints = _uiState.value.drawingPoints.toMutableList()
        if (index in currentPoints.indices) {
            currentPoints[index] = newPoint
            updateDrawingPoints(currentPoints)
        }
    }

    fun undoLastDrawingPoint() {
        val currentPoints = _uiState.value.drawingPoints
        if (currentPoints.isNotEmpty()) {
            updateDrawingPoints(currentPoints.dropLast(1))
        }
    }

    fun clearDrawing() {
        updateDrawingPoints(emptyList())
    }

    private fun updateDrawingPoints(newPoints: List<GeoPoint>) {
        val measurements = calculateAreaAndPerimeterUseCase(newPoints)
        _uiState.update {
            it.copy(
                drawingPoints = newPoints,
                drawingArea = measurements.areaInSquareMeters,
                drawingPerimeter = measurements.perimeterInMeters
            )
        }
    }

    fun setShowCreateParkForm(show: Boolean) {
        _uiState.update { it.copy(showCreateParkForm = show) }
    }

    fun saveNewPark(name: String, type: ParkType) {
        val state = _uiState.value
        if (state.drawingPoints.size < 3) return
        if (name.isBlank()) return
        
        viewModelScope.launch {
            val newPark = Park(
                id = UUID.randomUUID().toString(),
                name = name,
                type = type,
                areaInSquareMeters = state.drawingArea,
                perimeterInMeters = state.drawingPerimeter,
                status = ParkStatus.OPEN,
                lastSurveyDate = null,
                isPendingSync = true,
                boundaryPoints = state.drawingPoints,
                boundaryQuality = BoundaryQuality.VERIFIED, // It's manually drawn by user
                statusIsDemo = false,
                source = null
            )
            
            parkRepository.savePark(newPark)
            toggleDrawingMode() // Closes mode and clears points
        }
    }
}
