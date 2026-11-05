package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateTestCoordinatesUseCaseTest {

    private lateinit var isPointInPolygonUseCase: IsPointInPolygonUseCase
    private lateinit var generateTestCoordinatesUseCase: GenerateTestCoordinatesUseCase

    private val samplePark = Park(
        id = "park_1",
        name = "Jaipur Square Park",
        type = ParkType.GARDEN,
        areaInSquareMeters = 10000.0,
        perimeterInMeters = 400.0,
        status = ParkStatus.OPEN,
        lastSurveyDate = null,
        isPendingSync = false,
        boundaryPoints = listOf(
            GeoPoint(26.9000, 75.7000),
            GeoPoint(26.9000, 75.7100),
            GeoPoint(26.9100, 75.7100),
            GeoPoint(26.9100, 75.7000)
        ),
        boundaryQuality = BoundaryQuality.VERIFIED,
        statusIsDemo = false,
        source = null
    )

    @Before
    fun setup() {
        isPointInPolygonUseCase = IsPointInPolygonUseCase()
        generateTestCoordinatesUseCase = GenerateTestCoordinatesUseCase(isPointInPolygonUseCase)
    }

    @Test
    fun `generateInsidePoint produces a point inside the polygon`() {
        val insidePoint = generateTestCoordinatesUseCase.generateInsidePoint(samplePark)

        assertNotNull(insidePoint)
        assertTrue(isPointInPolygonUseCase(insidePoint!!, samplePark.boundaryPoints))
    }

    @Test
    fun `generateOutsidePoint produces a point outside the polygon`() {
        val outsidePoint = generateTestCoordinatesUseCase.generateOutsidePoint(samplePark)

        assertNotNull(outsidePoint)
        assertTrue(!isPointInPolygonUseCase(outsidePoint!!, samplePark.boundaryPoints))
    }
}
