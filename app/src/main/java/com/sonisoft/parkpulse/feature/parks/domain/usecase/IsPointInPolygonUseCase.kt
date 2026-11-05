package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class IsPointInPolygonUseCase @Inject constructor() {

    operator fun invoke(point: GeoPoint, polygon: List<GeoPoint>): Boolean {
        // Sanitize polygon: filter out invalid coordinates and consecutive duplicate points
        val cleanPolygon = sanitizePolygon(polygon)
        if (cleanPolygon.size < 3) return false

        // Check if point is directly on any vertex or edge
        for (i in cleanPolygon.indices) {
            val a = cleanPolygon[i]
            val b = cleanPolygon[(i + 1) % cleanPolygon.size]
            if (isPointOnSegment(point, a, b)) {
                return true
            }
        }

        // Standard Ray Casting algorithm (ray extending along positive longitude)
        var isInside = false
        val n = cleanPolygon.size
        var j = n - 1

        for (i in 0 until n) {
            val p1 = cleanPolygon[i]
            val p2 = cleanPolygon[j]

            // Check if ray crosses the edge p1-p2
            if ((p1.latitude > point.latitude) != (p2.latitude > point.latitude)) {
                val intersectLon = (p2.longitude - p1.longitude) * (point.latitude - p1.latitude) / (p2.latitude - p1.latitude) + p1.longitude
                if (point.longitude < intersectLon) {
                    isInside = !isInside
                }
            }
            j = i
        }

        return isInside
    }

    private fun sanitizePolygon(polygon: List<GeoPoint>): List<GeoPoint> {
        if (polygon.isEmpty()) return emptyList()
        val result = mutableListOf<GeoPoint>()
        for (pt in polygon) {
            if (pt.latitude.isNaN() || pt.longitude.isNaN() || pt.latitude < -90 || pt.latitude > 90 || pt.longitude < -180 || pt.longitude > 180) {
                continue
            }
            if (result.isEmpty() || !arePointsEqual(result.last(), pt)) {
                result.add(pt)
            }
        }
        // If first and last points are duplicates (e.g. closed ring), remove the duplicate tail
        if (result.size > 1 && arePointsEqual(result.first(), result.last())) {
            result.removeAt(result.size - 1)
        }
        return result
    }

    private fun arePointsEqual(p1: GeoPoint, p2: GeoPoint, tolerance: Double = 1e-9): Boolean {
        return abs(p1.latitude - p2.latitude) < tolerance && abs(p1.longitude - p2.longitude) < tolerance
    }

    private fun isPointOnSegment(p: GeoPoint, a: GeoPoint, b: GeoPoint, tolerance: Double = 1e-8): Boolean {
        if (arePointsEqual(a, b, tolerance)) {
            return arePointsEqual(p, a, tolerance)
        }

        // Bounding box check first
        if (p.latitude < min(a.latitude, b.latitude) - tolerance ||
            p.latitude > max(a.latitude, b.latitude) + tolerance ||
            p.longitude < min(a.longitude, b.longitude) - tolerance ||
            p.longitude > max(a.longitude, b.longitude) + tolerance
        ) {
            return false
        }

        // Cross product check for collinearity
        val crossProduct = (p.latitude - a.latitude) * (b.longitude - a.longitude) -
                (p.longitude - a.longitude) * (b.latitude - a.latitude)

        if (abs(crossProduct) > tolerance) {
            return false
        }

        // Dot product check to verify point lies within the segment bounds
        val dotProduct = (p.latitude - a.latitude) * (b.latitude - a.latitude) +
                (p.longitude - a.longitude) * (b.longitude - a.longitude)
        val squaredLength = (b.latitude - a.latitude) * (b.latitude - a.latitude) +
                (b.longitude - a.longitude) * (b.longitude - a.longitude)

        return dotProduct in -tolerance..(squaredLength + tolerance)
    }
}
