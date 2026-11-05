package com.sonisoft.parkpulse.feature.parks.domain.location

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationService {
    fun requestLocationUpdates(): Flow<GeoPoint?>
    fun hasLocationPermission(): Boolean
}