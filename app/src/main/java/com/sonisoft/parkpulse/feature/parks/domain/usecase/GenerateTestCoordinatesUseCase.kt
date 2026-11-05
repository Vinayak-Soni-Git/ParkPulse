package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import javax.inject.Inject

class GenerateTestCoordinatesUseCase @Inject constructor(
    private val isPointInPolygonUseCase: IsPointInPolygonUseCase
) {

    fun generateInsidePoint(park: Park): GeoPoint? {
        val boundary = park.boundaryPoints
        if (boundary.size < 3) return null

        // 1. Try arithmetic centroid
        val avgLat = boundary.map { it.latitude }.average()
        val avgLon = boundary.map { it.longitude }.average()
        val centroid = GeoPoint(avgLat, avgLon)

        if (isPointInPolygonUseCase(centroid, boundary)) {
            return centroid
        }

        // 2. Try midpoints between centroid and each vertex
        for (pt in boundary) {
            val candidate = GeoPoint(
                latitude = (centroid.latitude + pt.latitude) / 2.0,
                longitude = (centroid.longitude + pt.longitude) / 2.0
            )
            if (isPointInPolygonUseCase(candidate, boundary)) {
                return candidate
            }
        }

        // 3. Fallback: try midpoints between consecutive vertices
        for (i in boundary.indices) {
            val p1 = boundary[i]
            val p2 = boundary[(i + 1) % boundary.size]
            val mid = GeoPoint((p1.latitude + p2.latitude) / 2.0, (p1.longitude + p2.longitude) / 2.0)
            if (isPointInPolygonUseCase(mid, boundary)) {
                return mid
            }
        }

        // If no candidate passes, return first vertex as an boundary point fallback
        return boundary.firstOrNull()
    }

    fun generateOutsidePoint(park: Park): GeoPoint? {
        val boundary = park.boundaryPoints
        if (boundary.isEmpty()) return null

        val maxLat = boundary.maxOf { it.latitude }
        val maxLon = boundary.maxOf { it.longitude }

        // Offset safely outside bounding box
        val outsidePoint = GeoPoint(maxLat + 0.01, maxLon + 0.01)

        return if (!isPointInPolygonUseCase(outsidePoint, boundary)) {
            outsidePoint
        } else {
            GeoPoint(maxLat + 0.1, maxLon + 0.1)
        }
    }
}
