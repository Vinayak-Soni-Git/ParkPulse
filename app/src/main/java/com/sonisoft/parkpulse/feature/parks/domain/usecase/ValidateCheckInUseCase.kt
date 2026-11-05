package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation
import javax.inject.Inject

class ValidateCheckInUseCase @Inject constructor(
    private val isPointInPolygonUseCase: IsPointInPolygonUseCase
) {
    enum class ValidationStatus {
        LOCATION_PERMISSION_REQUIRED,
        LOCATION_UNAVAILABLE,
        LOCATION_STALE,
        INVALID_LOCATION,
        GPS_ACCURACY_INSUFFICIENT,
        PARK_NOT_FOUND,
        INVALID_PARK_BOUNDARY,
        OUTSIDE_PARK,
        INSIDE_PARK,
        BOUNDARY_REQUIRES_REVIEW
    }

    data class Result(
        val status: ValidationStatus,
        val isEligibleForCheckIn: Boolean,
        val message: String,
        val userLocation: UserLocation? = null,
        val park: Park? = null
    )

    operator fun invoke(
        userLocation: UserLocation?,
        hasPermission: Boolean,
        park: Park?,
        maxAgeMillis: Long = 60_000L,
        maxAccuracyMeters: Float = 25.0f
    ): Result {
        if (!hasPermission) {
            return Result(
                status = ValidationStatus.LOCATION_PERMISSION_REQUIRED,
                isEligibleForCheckIn = false,
                message = "Location permission is required to start a survey.",
                userLocation = userLocation,
                park = park
            )
        }

        if (park == null) {
            return Result(
                status = ValidationStatus.PARK_NOT_FOUND,
                isEligibleForCheckIn = false,
                message = "Selected park details could not be loaded.",
                userLocation = userLocation,
                park = null
            )
        }

        if (park.boundaryPoints.size < 3) {
            return Result(
                status = ValidationStatus.INVALID_PARK_BOUNDARY,
                isEligibleForCheckIn = false,
                message = "Selected park has insufficient or invalid boundary coordinates.",
                userLocation = userLocation,
                park = park
            )
        }

        if (park.boundaryQuality == BoundaryQuality.NEEDS_REVIEW) {
            return Result(
                status = ValidationStatus.BOUNDARY_REQUIRES_REVIEW,
                isEligibleForCheckIn = false,
                message = "Park boundary requires quality review before survey check-in can be permitted.",
                userLocation = userLocation,
                park = park
            )
        }

        if (userLocation == null) {
            return Result(
                status = ValidationStatus.LOCATION_UNAVAILABLE,
                isEligibleForCheckIn = false,
                message = "Waiting for a reliable GPS location...",
                userLocation = null,
                park = park
            )
        }

        if (userLocation.latitude.isNaN() || userLocation.longitude.isNaN() ||
            userLocation.latitude < -90 || userLocation.latitude > 90 ||
            userLocation.longitude < -180 || userLocation.longitude > 180
        ) {
            return Result(
                status = ValidationStatus.INVALID_LOCATION,
                isEligibleForCheckIn = false,
                message = "Received invalid GPS coordinates.",
                userLocation = userLocation,
                park = park
            )
        }

        val currentTime = System.currentTimeMillis()
        if (userLocation.timestamp > 0 && (currentTime - userLocation.timestamp) > maxAgeMillis) {
            return Result(
                status = ValidationStatus.LOCATION_STALE,
                isEligibleForCheckIn = false,
                message = "GPS location fix is stale. Waiting for fresh location...",
                userLocation = userLocation,
                park = park
            )
        }

        if (userLocation.accuracyMeters > maxAccuracyMeters) {
            return Result(
                status = ValidationStatus.GPS_ACCURACY_INSUFFICIENT,
                isEligibleForCheckIn = false,
                message = "GPS accuracy is currently ${userLocation.accuracyMeters.toInt()} metres. Please wait for accuracy <= 25m.",
                userLocation = userLocation,
                park = park
            )
        }

        val point = GeoPoint(userLocation.latitude, userLocation.longitude)
        val isInside = isPointInPolygonUseCase(point, park.boundaryPoints)

        if (!isInside) {
            return Result(
                status = ValidationStatus.OUTSIDE_PARK,
                isEligibleForCheckIn = false,
                message = "You're outside this park boundary. Move inside the park to start your survey.",
                userLocation = userLocation,
                park = park
            )
        }

        return Result(
            status = ValidationStatus.INSIDE_PARK,
            isEligibleForCheckIn = true,
            message = "Location verified inside park boundary.",
            userLocation = userLocation,
            park = park
        )
    }
}
