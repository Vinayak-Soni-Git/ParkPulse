package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import javax.inject.Inject
import kotlin.math.abs

class IsPointInPolygonUseCase @Inject constructor() {

    operator fun invoke(point: GeoPoint, polygon: List<GeoPoint>): Boolean {
        if (polygon.size < 3) return false

        var isInside = false
        val n = polygon.size
        var j = n - 1

        for (i in 0 until n) {
            val p1 = polygon[i]
            val p2 = polygon[j]

            // Check if point is directly on an edge or vertex
            if (isPointOnSegment(point, p1, p2)) {
                return true
            }

            val intersect = ((p1.latitude > point.latitude) != (p2.latitude > point.latitude)) &&
                    (point.longitude < (p2.longitude - p1.longitude) * (point.latitude - p1.latitude) / (p2.latitude - p1.latitude) + p1.longitude)

            if (intersect) {
                isInside = !isInside
            }

            j = i
        }

        return isInside
    }

    private fun isPointOnSegment(p: GeoPoint, a: GeoPoint, b: GeoPoint): Boolean {
        val crossProduct = (p.latitude - a.latitude) * (b.longitude - a.longitude) - (p.longitude - a.longitude) * (b.latitude - a.latitude)
        if (abs(crossProduct) > 1e-9) return false

        val dotProduct = (p.latitude - a.latitude) * (b.latitude - a.latitude) + (p.longitude - a.longitude) * (b.longitude - a.longitude)
        if (dotProduct < 0) return false

        val squaredLength = (b.latitude - a.latitude) * (b.latitude - a.latitude) + (b.longitude - a.longitude) * (b.longitude - a.longitude)
        if (dotProduct > squaredLength) return false

        return true
    }
}
