package com.sonisoft.parkpulse.feature.parks.domain.usecase

import android.location.Location
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import javax.inject.Inject

class CalculateDistanceAndBearingUseCase @Inject constructor() {

    data class Result(
        val distanceInMeters: Float,
        val bearing: Float,
        val compassDirection: String
    )

    operator fun invoke(userLocation: GeoPoint, targetLocation: GeoPoint): Result {
        val results = FloatArray(3)
        Location.distanceBetween(
            userLocation.latitude, userLocation.longitude,
            targetLocation.latitude, targetLocation.longitude,
            results
        )

        val distance = results[0]
        val bearing = if (results.size > 1) results[1] else 0f

        val compassDirection = getCompassDirection(bearing)

        return Result(distance, bearing, compassDirection)
    }

    private fun getCompassDirection(bearing: Float): String {
        val normalizedBearing = (bearing % 360 + 360) % 360
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((normalizedBearing + 22.5) / 45.0).toInt() % 8
        return directions[index]
    }
}
