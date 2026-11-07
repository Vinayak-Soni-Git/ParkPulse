package com.sonisoft.parkpulse.feature.parks.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonisoft.parkpulse.feature.parks.domain.location.LocationService
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.usecase.CalculateDistanceAndBearingUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.CalculateAreaAndPerimeterUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.GetAllParksUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ValidatePolygonUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ExportParkGeoJsonUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ScheduleSyncUseCase
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getAllParksUseCase: GetAllParksUseCase,
    private val parkRepository: ParkRepository,
    private val locationService: LocationService,
    private val calculateDistanceAndBearingUseCase: CalculateDistanceAndBearingUseCase,
    private val calculateAreaAndPerimeterUseCase: CalculateAreaAndPerimeterUseCase,
    private val validatePolygonUseCase: ValidatePolygonUseCase,
    private val exportParkGeoJsonUseCase: ExportParkGeoJsonUseCase,
    private val scheduleSyncUseCase: ScheduleSyncUseCase
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
            locationService.requestDetailedLocationUpdates().collectLatest { location ->
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
                
                // Bonus 3: Walk mode updates
                location?.let {
                    val state = _uiState.value
                    if (state.isDrawingMode && state.isWalkMode && state.isWalking) {
                        if (it.accuracyMeters <= 20f) {
                            val newPoint = GeoPoint(it.latitude, it.longitude)
                            // Prevent extremely close duplicate points (e.g. < 2 meters distance approx)
                            val last = state.drawingPoints.lastOrNull()
                            if (last == null || calculateDistanceAndBearingUseCase(last, newPoint).distanceInMeters > 2.0) {
                                updateDrawingPoints(state.drawingPoints + newPoint)
                            }
                        }
                    }
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
        userLocation: com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation?,
        park: Park
    ): CalculateDistanceAndBearingUseCase.Result? {
        if (userLocation == null || park.boundaryPoints.isEmpty()) return null

        val userGeo = GeoPoint(userLocation.latitude, userLocation.longitude)
        // Use centroid or first point for distance calculation
        val centerLat = park.boundaryPoints.map { it.latitude }.average()
        val centerLon = park.boundaryPoints.map { it.longitude }.average()
        val parkCenter = GeoPoint(centerLat, centerLon)

        return calculateDistanceAndBearingUseCase(userGeo, parkCenter)
    }

    // Phase 4: Drawing Mode Methods
    fun toggleDrawingMode(parkToEdit: Park? = null) {
        val initialPoints = parkToEdit?.boundaryPoints ?: emptyList()
        val measurements = if (initialPoints.size >= 3) {
            calculateAreaAndPerimeterUseCase(initialPoints)
        } else null

        _uiState.update { 
            it.copy(
                isDrawingMode = !it.isDrawingMode,
                drawingPoints = initialPoints,
                drawingArea = measurements?.areaInSquareMeters ?: 0.0,
                drawingPerimeter = measurements?.perimeterInMeters ?: 0.0,
                drawingError = null,
                isWalkMode = false,
                isWalking = false,
                showCreateParkForm = false,
                editingParkId = parkToEdit?.id,
                selectedPark = if (it.isDrawingMode) it.selectedPark else null // Deselect if entering
            )
        }
    }

    fun toggleWalkMode() {
        _uiState.update { 
            it.copy(
                isWalkMode = true,
                isWalking = false
            )
        }
    }

    fun toggleWalking() {
        _uiState.update { 
            it.copy(isWalking = !it.isWalking)
        }
    }

    fun setManualMode() {
        _uiState.update { 
            it.copy(
                isWalkMode = false,
                isWalking = false
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
        val validationResult = validatePolygonUseCase(newPoints)
        val error = if (validationResult is ValidatePolygonUseCase.Result.Invalid) validationResult.reason else null
        
        _uiState.update {
            it.copy(
                drawingPoints = newPoints,
                drawingArea = measurements.areaInSquareMeters,
                drawingPerimeter = measurements.perimeterInMeters,
                drawingError = error
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
        if (state.drawingError != null) return
        
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val editingId = state.editingParkId
            
            val baseServerVer = if (editingId != null) {
                // If editing, preserve the server version as the base
                val existingPark = parkRepository.getParkById(editingId).first()
                existingPark?.serverVersion ?: 0
            } else {
                0
            }
            
            val existingRemoteId = if (editingId != null) {
                val existingPark = parkRepository.getParkById(editingId).first()
                existingPark?.remoteId
            } else null

            val parkToSave = Park(
                id = editingId ?: UUID.randomUUID().toString(),
                name = name,
                type = type,
                areaInSquareMeters = state.drawingArea,
                perimeterInMeters = state.drawingPerimeter,
                status = ParkStatus.OPEN,
                lastSurveyDate = null,
                isPendingSync = true,
                boundaryPoints = state.drawingPoints,
                boundaryQuality = BoundaryQuality.VERIFIED,
                statusIsDemo = false,
                source = null,
                syncStatus = com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.PENDING,
                remoteId = existingRemoteId,
                serverVersion = baseServerVer,
                baseServerVersion = baseServerVer,
                localModifiedAt = now
            )
            
            parkRepository.savePark(parkToSave)
            scheduleSyncUseCase()
            toggleDrawingMode() // Closes mode and clears points
        }
    }

    fun exportParkGeoJson(park: Park, context: android.content.Context) {
        val uri = exportParkGeoJsonUseCase.invoke(park)
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/geo+json"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = android.content.Intent.createChooser(intent, "Export GeoJSON")
        context.startActivity(chooser)
    }
}
