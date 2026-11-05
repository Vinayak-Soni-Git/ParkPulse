package com.sonisoft.parkpulse.feature.parks.data.seed

import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import org.junit.Assert.assertEquals
import org.junit.Test

class ParkSeedParserTest {

    private val parser = ParkSeedParser()

    @Test
    fun `parse valid json string successfully`() {
        val jsonString = """
            {
              "parks": [
                {
                  "id": "osm-way-816236337",
                  "name": "A block park",
                  "type": "GARDEN",
                  "areaInSquareMeters": 5578.64,
                  "perimeterInMeters": 800.23,
                  "status": "OPEN",
                  "lastSurveyDate": null,
                  "isPendingSync": false,
                  "boundaryQuality": "OSM_MAPPED_UNVERIFIED",
                  "statusIsDemo": true,
                  "boundaryPoints": [
                    { "latitude": 26.9, "longitude": 75.7 },
                    { "latitude": 26.91, "longitude": 75.71 },
                    { "latitude": 26.92, "longitude": 75.72 }
                  ]
                }
              ]
            }
        """.trimIndent()

        val (parks, points) = parser.parse(jsonString)

        assertEquals(1, parks.size)
        assertEquals(3, points.size)

        val park = parks.first()
        assertEquals("osm-way-816236337", park.id)
        assertEquals("A block park", park.name)
        assertEquals(ParkType.GARDEN, park.type)
        assertEquals(5578.64, park.areaInSquareMeters, 0.0)
        assertEquals(null, park.lastSurveyDate)
        assertEquals(ParkStatus.OPEN, park.status)
        assertEquals(BoundaryQuality.OSM_MAPPED_UNVERIFIED, park.boundaryQuality)
    }

    @Test
    fun `parse handles missing or invalid points safely`() {
        val jsonString = """
            {
              "parks": [
                {
                  "id": "test-1",
                  "name": "Invalid Points Park",
                  "type": "GARDEN",
                  "boundaryPoints": [
                    { "latitude": 200.0, "longitude": 75.7 },
                    { "latitude": 26.91, "longitude": 75.71 }
                  ]
                }
              ]
            }
        """.trimIndent()

        val (parks, points) = parser.parse(jsonString)

        assertEquals(0, parks.size) // Less than 3 valid points structure means it shouldn't even add points
    }
}
