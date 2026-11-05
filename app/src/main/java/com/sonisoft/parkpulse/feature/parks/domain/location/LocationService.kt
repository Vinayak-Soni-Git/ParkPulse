package com.sonisoft.parkpulse.feature.parks.domain.location

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow

interface LocationService {
    fun requestLocationUpdates(): Flow<GeoPoint?>
    fun requestDetailedLocationUpdates(): Flow<UserLocation?>
    fun hasLocationPermission(): Boolean
}