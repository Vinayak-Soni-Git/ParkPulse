package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ValidatePolygonUseCase @Inject constructor() {

    sealed class Result {
        data object Valid : Result()
        data class Invalid(val reason: String) : Result()
    }

    operator fun invoke(points: List<GeoPoint>): Result {
        if (points.size < 3) return Result.Invalid("Polygon must have at least 3 vertices.")

        val n = points.size

        // 1. Check for duplicate consecutive points & zero-length edges
        for (i in 0 until n) {
            val p1 = points[i]
            val p2 = points[(i + 1) % n]
            if (p1.latitude == p2.latitude && p1.longitude == p2.longitude) {
                return Result.Invalid("Polygon contains duplicate consecutive vertices or zero-length edges.")
            }
        }

        // 2. Check for self-intersections
        for (i in 0 until n) {
            val p1 = points[i]
            val q1 = points[(i + 1) % n]

            for (j in i + 1 until n) {
                val p2 = points[j]
                val q2 = points[(j + 1) % n]

                val isAdjacent = (i == j) || (i == (j + 1) % n) || (j == (i + 1) % n)

                if (!isAdjacent) {
                    if (doIntersect(p1, q1, p2, q2)) {
                        return Result.Invalid("Polygon edges must not intersect.")
                    }
                } else {
                    if (areCollinearAndOverlapping(p1, q1, p2, q2)) {
                        return Result.Invalid("Polygon has overlapping adjacent edges (folds back on itself).")
                    }
                }
            }
        }

        return Result.Valid
    }

    private fun doIntersect(p1: GeoPoint, q1: GeoPoint, p2: GeoPoint, q2: GeoPoint): Boolean {
        val o1 = orientation(p1, q1, p2)
        val o2 = orientation(p1, q1, q2)
        val o3 = orientation(p2, q2, p1)
        val o4 = orientation(p2, q2, q1)

        // General case
        if (o1 != o2 && o3 != o4) return true

        // Special Cases
        // p1, q1 and p2 are collinear and p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1)) return true
        if (o2 == 0 && onSegment(p1, q2, q1)) return true
        if (o3 == 0 && onSegment(p2, p1, q2)) return true
        if (o4 == 0 && onSegment(p2, q1, q2)) return true

        return false
    }

    private fun areCollinearAndOverlapping(p1: GeoPoint, q1: GeoPoint, p2: GeoPoint, q2: GeoPoint): Boolean {
        if (orientation(p1, q1, p2) == 0 && orientation(p1, q1, q2) == 0) {
            val shared = when {
                p1 == p2 -> p1
                p1 == q2 -> p1
                q1 == p2 -> q1
                else -> q1
            }
            val (other1, other2) = when (shared) {
                p1 -> Pair(q1, if (p2 == shared) q2 else p2)
                q1 -> Pair(p1, if (p2 == shared) q2 else p2)
                else -> return false
            }
            val v1Lat = other1.latitude - shared.latitude
            val v1Lon = other1.longitude - shared.longitude
            val v2Lat = other2.latitude - shared.latitude
            val v2Lon = other2.longitude - shared.longitude

            val dot = (v1Lat * v2Lat) + (v1Lon * v2Lon)
            if (dot < 0) return true
        }
        return false
    }

    private fun onSegment(p: GeoPoint, q: GeoPoint, r: GeoPoint): Boolean {
        if (q.longitude <= max(p.longitude, r.longitude) && q.longitude >= min(p.longitude, r.longitude) &&
            q.latitude <= max(p.latitude, r.latitude) && q.latitude >= min(p.latitude, r.latitude)
        ) {
            return true
        }
        return false
    }

    private fun orientation(p: GeoPoint, q: GeoPoint, r: GeoPoint): Int {
        val v = (q.latitude - p.latitude) * (r.longitude - q.longitude) -
                (q.longitude - p.longitude) * (r.latitude - q.latitude)

        val epsilon = 1e-9
        if (abs(v) < epsilon) return 0 // collinear
        return if (v > 0) 1 else 2 // clock or counterclock wise
    }
}
