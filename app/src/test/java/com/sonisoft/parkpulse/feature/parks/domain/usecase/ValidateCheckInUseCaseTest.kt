package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateCheckInUseCaseTest {

    private lateinit var isPointInPolygonUseCase: IsPointInPolygonUseCase
    private lateinit var validateCheckInUseCase: ValidateCheckInUseCase

    private val parkSquare = listOf(
        GeoPoint(26.9000, 75.7000),
        GeoPoint(26.9000, 75.7100),
        GeoPoint(26.9100, 75.7100),
        GeoPoint(26.9100, 75.7000)
    )

    @Before
    fun setup() {
        isPointInPolygonUseCase = IsPointInPolygonUseCase()
        validateCheckInUseCase = ValidateCheckInUseCase(isPointInPolygonUseCase)
    }

    @Test
    fun `accuracy 25m or lower inside boundary succeeds`() {
        val loc = UserLocation(
            latitude = 26.9050,
            longitude = 75.7050,
            accuracyMeters = 20.0f,
            timestamp = System.currentTimeMillis()
        )

        val result = validateCheckInUseCase(loc, true, parkSquare)
        assertTrue(result is ValidateCheckInUseCase.Result.Success)
    }

    @Test
    fun `accuracy above 25m fails`() {
        val loc = UserLocation(
            latitude = 26.9050,
            longitude = 75.7050,
            accuracyMeters = 30.0f, // > 25m
            timestamp = System.currentTimeMillis()
        )

        val result = validateCheckInUseCase(loc, true, parkSquare)
        assertTrue(result is ValidateCheckInUseCase.Result.Failure.LowAccuracy)
    }

    @Test
    fun `outside boundary fails`() {
        val loc = UserLocation(
            latitude = 26.9500, // outside
            longitude = 75.7500,
            accuracyMeters = 10.0f,
            timestamp = System.currentTimeMillis()
        )

        val result = validateCheckInUseCase(loc, true, parkSquare)
        assertTrue(result is ValidateCheckInUseCase.Result.Failure.OutsideBoundary)
    }

    @Test
    fun `missing permission fails`() {
        val loc = UserLocation(
            latitude = 26.9050,
            longitude = 75.7050,
            accuracyMeters = 10.0f,
            timestamp = System.currentTimeMillis()
        )

        val result = validateCheckInUseCase(loc, false, parkSquare)
        assertTrue(result is ValidateCheckInUseCase.Result.Failure.PermissionRequired)
    }
}
