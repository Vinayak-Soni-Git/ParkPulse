package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IsPointInPolygonUseCaseTest {

    private lateinit var useCase: IsPointInPolygonUseCase

    @Before
    fun setup() {
        useCase = IsPointInPolygonUseCase()
    }

    @Test
    fun `point inside rectangular polygon returns true`() {
        val square = listOf(
            GeoPoint(26.9000, 75.7000),
            GeoPoint(26.9000, 75.7100),
            GeoPoint(26.9100, 75.7100),
            GeoPoint(26.9100, 75.7000)
        )
        val insidePoint = GeoPoint(26.9050, 75.7050)

        assertTrue(useCase(insidePoint, square))
    }

    @Test
    fun `point outside rectangular polygon returns false`() {
        val square = listOf(
            GeoPoint(26.9000, 75.7000),
            GeoPoint(26.9000, 75.7100),
            GeoPoint(26.9100, 75.7100),
            GeoPoint(26.9100, 75.7000)
        )
        val outsidePoint = GeoPoint(26.9200, 75.7200)

        assertFalse(useCase(outsidePoint, square))
    }

    @Test
    fun `point 90km away from park returns false even with closed ring polygon`() {
        // "A block park" closed OSM ring
        val aBlockParkRing = listOf(
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
            GeoPoint(26.9138498, 75.734482) // duplicated tail point
        )

        val farAwayUserPoint = GeoPoint(27.24248, 76.61408)

        assertFalse("Faraway user point must be rejected", useCase(farAwayUserPoint, aBlockParkRing))
    }

    @Test
    fun `point inside irregular polygon returns true`() {
        val LShape = listOf(
            GeoPoint(0.0, 0.0),
            GeoPoint(0.0, 2.0),
            GeoPoint(1.0, 2.0),
            GeoPoint(1.0, 1.0),
            GeoPoint(2.0, 1.0),
            GeoPoint(2.0, 0.0)
        )
        val insidePoint = GeoPoint(0.5, 1.5)
        val outsideHolePoint = GeoPoint(1.5, 1.5)

        assertTrue(useCase(insidePoint, LShape))
        assertFalse(useCase(outsideHolePoint, LShape))
    }

    @Test
    fun `fewer than 3 vertices returns false`() {
        val points = listOf(
            GeoPoint(26.9000, 75.7000),
            GeoPoint(26.9100, 75.7100)
        )
        assertFalse(useCase(GeoPoint(26.9050, 75.7050), points))
    }
}
