package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation
import javax.inject.Inject

class ValidateCheckInUseCase @Inject constructor(
    private val isPointInPolygonUseCase: IsPointInPolygonUseCase
) {
    sealed class Result {
        data class Success(
            val userLocation: UserLocation,
            val message: String = "Location verified inside park boundary."
        ) : Result()

        sealed class Failure(val reason: String) : Result() {
            data class OutsideBoundary(
                val msg: String = "You are outside this park boundary."
            ) : Failure(msg)

            data class LowAccuracy(
                val accuracyMeters: Float,
                val msg: String = "GPS accuracy is currently ${accuracyMeters.toInt()} metres. Please wait for accuracy <= 25m."
            ) : Failure(msg)

            data class StaleLocation(
                val msg: String = "Location fix is stale. Waiting for fresh GPS location..."
            ) : Failure(msg)

            data class LocationUnavailable(
                val msg: String = "Waiting for a reliable GPS location..."
            ) : Failure(msg)

            data class PermissionRequired(
                val msg: String = "Location permission is required to start a survey."
            ) : Failure(msg)

            data class InvalidParkBoundary(
                val msg: String = "Selected park has insufficient boundary coordinates."
            ) : Failure(msg)
        }
    }

    operator fun invoke(
        userLocation: UserLocation?,
        hasPermission: Boolean,
        parkBoundary: List<GeoPoint>,
        maxAgeMillis: Long = 60_000L,
        maxAccuracyMeters: Float = 25.0f
    ): Result {
        if (!hasPermission) {
            return Result.Failure.PermissionRequired()
        }

        if (userLocation == null) {
            return Result.Failure.LocationUnavailable()
        }

        if (parkBoundary.size < 3) {
            return Result.Failure.InvalidParkBoundary()
        }

        val currentTime = System.currentTimeMillis()
        if (userLocation.timestamp > 0 && (currentTime - userLocation.timestamp) > maxAgeMillis) {
            return Result.Failure.StaleLocation()
        }

        if (userLocation.accuracyMeters > maxAccuracyMeters) {
            return Result.Failure.LowAccuracy(userLocation.accuracyMeters)
        }

        val point = GeoPoint(userLocation.latitude, userLocation.longitude)
        val isInside = isPointInPolygonUseCase(point, parkBoundary)

        if (!isInside) {
            return Result.Failure.OutsideBoundary()
        }

        return Result.Success(userLocation)
    }
}
