package com.sonisoft.parkpulse.feature.parks.data.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WatermarkProcessorTest {

    @Test
    fun `WatermarkMetadata holds correct values`() {
        val metadata = WatermarkProcessor.WatermarkMetadata(
            parkName = "Central Park",
            latitude = 26.9124,
            longitude = 75.7873,
            accuracyMeters = 5.0f,
            capturedAt = 1700000000000L
        )

        assertEquals("Central Park", metadata.parkName)
        assertEquals(26.9124, metadata.latitude, 0.0001)
        assertEquals(75.7873, metadata.longitude, 0.0001)
        assertEquals(5.0f, metadata.accuracyMeters, 0.01f)
        assertEquals(1700000000000L, metadata.capturedAt)
    }

    @Test
    fun `Watermark metadata formatting`() {
        val metadata = WatermarkProcessor.WatermarkMetadata(
            parkName = "A block park",
            latitude = 26.9138,
            longitude = 75.7344,
            accuracyMeters = 3.5f,
            capturedAt = 1700000000000L
        )

        assertTrue(metadata.parkName.isNotBlank())
        assertTrue(metadata.latitude > 0)
        assertTrue(metadata.accuracyMeters <= 25.0f)
    }
}
