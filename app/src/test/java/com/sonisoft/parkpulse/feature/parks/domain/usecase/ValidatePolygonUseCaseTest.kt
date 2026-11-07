package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatePolygonUseCaseTest {

    private val validatePolygonUseCase = ValidatePolygonUseCase()

    @Test
    fun `less than 3 vertices is invalid`() {
        val result = validatePolygonUseCase(listOf(GeoPoint(0.0, 0.0), GeoPoint(1.0, 1.0)))
        assertTrue(result is ValidatePolygonUseCase.Result.Invalid)
    }

    @Test
    fun `valid polygon returns valid`() {
        val points = listOf(
            GeoPoint(0.0, 0.0),
            GeoPoint(0.0, 1.0),
            GeoPoint(1.0, 1.0),
            GeoPoint(1.0, 0.0)
        )
        val result = validatePolygonUseCase(points)
        assertEquals(ValidatePolygonUseCase.Result.Valid, result)
    }

    @Test
    fun `duplicate consecutive points is invalid`() {
        val points = listOf(
            GeoPoint(0.0, 0.0),
            GeoPoint(0.0, 1.0),
            GeoPoint(0.0, 1.0),
            GeoPoint(1.0, 0.0)
        )
        val result = validatePolygonUseCase(points)
        assertTrue(result is ValidatePolygonUseCase.Result.Invalid)
        assertEquals("Polygon contains duplicate consecutive vertices or zero-length edges.", (result as ValidatePolygonUseCase.Result.Invalid).reason)
    }

    @Test
    fun `self intersecting polygon is invalid`() {
        // Bowtie shape
        val points = listOf(
            GeoPoint(0.0, 0.0),
            GeoPoint(1.0, 1.0),
            GeoPoint(0.0, 1.0),
            GeoPoint(1.0, 0.0)
        )
        val result = validatePolygonUseCase(points)
        assertTrue(result is ValidatePolygonUseCase.Result.Invalid)
        assertEquals("Polygon edges must not intersect.", (result as ValidatePolygonUseCase.Result.Invalid).reason)
    }

    @Test
    fun `overlapping adjacent edges is invalid`() {
        val points = listOf(
            GeoPoint(0.0, 0.0),
            GeoPoint(2.0, 0.0),
            GeoPoint(1.0, 0.0) // Triangle that is basically a line
        )
        val result = validatePolygonUseCase(points)
        assertTrue(result is ValidatePolygonUseCase.Result.Invalid)
        assertEquals("Polygon has overlapping adjacent edges (folds back on itself).", (result as ValidatePolygonUseCase.Result.Invalid).reason)
    }
}
