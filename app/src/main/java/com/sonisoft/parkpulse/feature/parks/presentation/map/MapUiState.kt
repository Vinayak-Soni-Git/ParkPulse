package com.sonisoft.parkpulse.feature.parks.presentation.map

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park

data class MapUiState(
    val isLoading: Boolean = false,
    val parks: List<Park> = emptyList(),
    val error: String? = null,
    val selectedPark: Park? = null,
    val userLocation: com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation? = null,
    val hasLocationPermission: Boolean = false,
    val distanceToSelectedPark: Float? = null,
    val bearingToSelectedPark: String? = null,
    
    // Phase 4 & 7 Drawing State
    val isDrawingMode: Boolean = false,
    val drawingPoints: List<GeoPoint> = emptyList(),
    val drawingArea: Double = 0.0,
    val drawingPerimeter: Double = 0.0,
    val drawingError: String? = null,
    val isWalkMode: Boolean = false,
    val isWalking: Boolean = false,
    val showCreateParkForm: Boolean = false,
    val editingParkId: String? = null // if non-null, we are editing an existing park
)
