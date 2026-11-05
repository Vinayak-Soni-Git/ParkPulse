package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateCheckInUseCaseTest {

    private lateinit var isPointInPolygonUseCase: IsPointInPolygonUseCase
    private lateinit var validateCheckInUseCase: ValidateCheckInUseCase

    private val samplePark = Park(
        id = "park_1",
        name = "A block park",
        type = ParkType.GARDEN,
        areaInSquareMeters = 5578.0,
        perimeterInMeters = 800.0,
        status = ParkStatus.OPEN,
        lastSurveyDate = null,
        isPendingSync = false,
        boundaryPoints = listOf(
            GeoPoint(26.9138498, 75.734482),
            GeoPoint(26.9138952, 75.7357185),
            GeoPoint(26.9135126, 75.735748),
            GeoPoint(26.9135867, 75.7366654),
            GeoPoint(26.9139359, 75.7366734),
            GeoPoint(26.9139933, 75.7376122),
            GeoPoint(26.9138187, 75.7376336),
            GeoPoint(26.9137972, 75.7368075),
            GeoPoint(26.9134982, 75.7367941),
            GeoPoint(26.9134241, 75.7356488),
            GeoPoint(26.9137254, 75.73563),
            GeoPoint(26.9136895, 75.7345089),
            GeoPoint(26.9138498, 75.734482)
        ),
        boundaryQuality = BoundaryQuality.VERIFIED,
        statusIsDemo = false,
        source = null
    )

    @Before
    fun setup() {
        isPointInPolygonUseCase = IsPointInPolygonUseCase()
        validateCheckInUseCase = ValidateCheckInUseCase(isPointInPolygonUseCase)
    }

    @Test
    fun `point inside with accuracy 20m succeeds`() {
        val loc = UserLocation(
            latitude = 26.9137,
            longitude = 75.7350,
            accuracyMeters = 20.0f,
            timestamp = System.currentTimeMillis()
        )

        val result = validateCheckInUseCase(loc, true, samplePark)
        assertEquals(ValidateCheckInUseCase.ValidationStatus.INSIDE_PARK, result.status)
        assertTrue(result.isEligibleForCheckIn)
    }

    @Test
    fun `point inside with accuracy exactly 25m succeeds`() {
        val loc = UserLocation(
            latitude = 26.9137,
            longitude = 75.7350,
            accuracyMeters = 25.0f,
            timestamp = System.currentTimeMillis()
        )

        val result = validateCheckInUseCase(loc, true, samplePark)
        assertEquals(ValidateCheckInUseCase.ValidationStatus.INSIDE_PARK, result.status)
        assertTrue(result.isEligibleForCheckIn)
    }

    @Test
    fun `point inside with accuracy 26m is rejected`() {
        val loc = UserLocation(
            latitude = 26.9137,
            longitude = 75.7350,
            accuracyMeters = 26.0f,
            timestamp = System.currentTimeMillis()
        )

        val result = validateCheckInUseCase(loc, true, samplePark)
        assertEquals(ValidateCheckInUseCase.ValidationStatus.GPS_ACCURACY_INSUFFICIENT, result.status)
        assertFalse(result.isEligibleForCheckIn)
    }

    @Test
    fun `faraway point 90km away with 3m accuracy is rejected as OUTSIDE_PARK`() {
        val loc = UserLocation(
            latitude = 27.24248,
            longitude = 76.61408,
            accuracyMeters = 3.0f,
            timestamp = System.currentTimeMillis()
        )

        val result = validateCheckInUseCase(loc, true, samplePark)
        assertEquals(ValidateCheckInUseCase.ValidationStatus.OUTSIDE_PARK, result.status)
        assertFalse(result.isEligibleForCheckIn)
        assertEquals("You're outside this park boundary. Move inside the park to start your survey.", result.message)
    }

    @Test
    fun `park boundary marked NEEDS_REVIEW is rejected`() {
        val unverifiedPark = samplePark.copy(boundaryQuality = BoundaryQuality.NEEDS_REVIEW)
        val loc = UserLocation(
            latitude = 26.9137,
            longitude = 75.7350,
            accuracyMeters = 10.0f,
            timestamp = System.currentTimeMillis()
        )

        val result = validateCheckInUseCase(loc, true, unverifiedPark)
        assertEquals(ValidateCheckInUseCase.ValidationStatus.BOUNDARY_REQUIRES_REVIEW, result.status)
        assertFalse(result.isEligibleForCheckIn)
    }

    @Test
    fun `stale location is rejected`() {
        val staleLoc = UserLocation(
            latitude = 26.9137,
            longitude = 75.7350,
            accuracyMeters = 10.0f,
            timestamp = System.currentTimeMillis() - 120_000L // 2 mins old
        )

        val result = validateCheckInUseCase(staleLoc, true, samplePark)
        assertEquals(ValidateCheckInUseCase.ValidationStatus.LOCATION_STALE, result.status)
        assertFalse(result.isEligibleForCheckIn)
    }

    @Test
    fun `missing location permission is rejected`() {
        val loc = UserLocation(
            latitude = 26.9137,
            longitude = 75.7360,
            accuracyMeters = 10.0f,
            timestamp = System.currentTimeMillis()
        )

        val result = validateCheckInUseCase(loc, false, samplePark)
        assertEquals(ValidateCheckInUseCase.ValidationStatus.LOCATION_PERMISSION_REQUIRED, result.status)
        assertFalse(result.isEligibleForCheckIn)
    }

    @Test
    fun `null park is rejected`() {
        val loc = UserLocation(
            latitude = 26.9137,
            longitude = 75.7360,
            accuracyMeters = 10.0f,
            timestamp = System.currentTimeMillis()
        )

        val result = validateCheckInUseCase(loc, true, null)
        assertEquals(ValidateCheckInUseCase.ValidationStatus.PARK_NOT_FOUND, result.status)
        assertFalse(result.isEligibleForCheckIn)
    }
}
