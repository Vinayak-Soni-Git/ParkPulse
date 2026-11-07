package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExportParkGeoJsonUseCaseTest {

    @Test
    fun `geojson builds correctly with coordinate order lon lat and closed ring`() {
        val useCase = ExportParkGeoJsonUseCase(io.mockk.mockk())

        val park = Park(
            id = "test_park_id",
            name = "Test Park",
            type = ParkType.GARDEN,
            areaInSquareMeters = 100.0,
            perimeterInMeters = 40.0,
            status = ParkStatus.OPEN,
            lastSurveyDate = null,
            isPendingSync = false,
            boundaryPoints = listOf(
                GeoPoint(latitude = 26.9, longitude = 75.7),
                GeoPoint(latitude = 26.91, longitude = 75.7),
                GeoPoint(latitude = 26.91, longitude = 75.71)
            ),
            boundaryQuality = BoundaryQuality.VERIFIED,
            statusIsDemo = false,
            source = null
        )

        val buildGeoJsonMethod = ExportParkGeoJsonUseCase::class.java.getDeclaredMethod("buildGeoJson", Park::class.java)
        buildGeoJsonMethod.isAccessible = true
        val json = buildGeoJsonMethod.invoke(useCase, park) as String

        assertTrue(json.contains("\"type\": \"FeatureCollection\""))
        assertTrue(json.contains("\"name\": \"Test Park\""))

        // Check longitude comes first in coordinate lists
        assertTrue(json.contains("[75.7, 26.9]"))

        // Ensure the polygon ring is closed (first point matches last point)
        val matches = Regex("\\[75\\.7, 26\\.9]").findAll(json)
        assertTrue("The first point should appear twice (at start and end of ring)", matches.count() >= 2)
    }
}
