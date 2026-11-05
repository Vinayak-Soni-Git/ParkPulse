package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateAreaAndPerimeterUseCaseTest {

    private lateinit var useCase: CalculateAreaAndPerimeterUseCase

    @Before
    fun setup() {
        useCase = CalculateAreaAndPerimeterUseCase()
    }

    @Test
    fun `fewer than three vertices returns zero measurements`() {
        val points = listOf(
            GeoPoint(26.9124, 75.7873),
            GeoPoint(26.9130, 75.7880)
        )

        val result = useCase(points)

        assertEquals(0.0, result.areaInSquareMeters, 0.0)
        assertEquals(0.0, result.areaInAcres, 0.0)
        assertEquals(0.0, result.perimeterInMeters, 0.0)
        assertEquals(0.0, result.perimeterInKilometers, 0.0)
    }

    @Test
    fun `three-point polygon calculates area and perimeter including closing edge`() {
        // Jaipur Central Park approximate triangle points
        val points = listOf(
            GeoPoint(26.9124, 75.7873),
            GeoPoint(26.9130, 75.7880),
            GeoPoint(26.9110, 75.7890)
        )

        val result = useCase(points)

        assertTrue(result.areaInSquareMeters > 0)
        assertTrue(result.areaInAcres > 0)
        assertTrue(result.perimeterInMeters > 0)
        assertEquals(result.areaInSquareMeters * 0.000247105, result.areaInAcres, 0.00001)
        assertEquals(result.perimeterInMeters / 1000.0, result.perimeterInKilometers, 0.00001)
    }

    @Test
    fun `four-point polygon area and perimeter calculation`() {
        // A simple square-like box near Jaipur
        val points = listOf(
            GeoPoint(26.9000, 75.7000),
            GeoPoint(26.9000, 75.7100),
            GeoPoint(26.9100, 75.7100),
            GeoPoint(26.9100, 75.7000)
        )

        val result = useCase(points)

        assertTrue(result.areaInSquareMeters > 1000000) // approx 1 sq km
        assertTrue(result.perimeterInMeters > 4000)     // approx 4 km perimeter
    }
}
