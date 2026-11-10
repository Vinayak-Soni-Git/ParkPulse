package com.sonisoft.parkpulse.feature.parks.domain.usecase

import android.location.Location
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CalculateAreaAndPerimeterUseCase @Inject constructor() {

    data class Result(
        val areaInSquareMeters: Double,
        val areaInAcres: Double,
        val perimeterInMeters: Double,
        val perimeterInKilometers: Double
    )

    operator fun invoke(points: List<GeoPoint>): Result {
        if (points.size < 3) {
            return Result(0.0, 0.0, 0.0, 0.0)
        }

        val perimeter = calculatePerimeter(points)
        val area = calculateArea(points)

        return Result(
            areaInSquareMeters = area,
            areaInAcres = area * 0.000247105,
            perimeterInMeters = perimeter,
            perimeterInKilometers = perimeter / 1000.0
        )
    }

    private fun calculatePerimeter(points: List<GeoPoint>): Double {
        var perimeter = 0.0
        for (i in points.indices) {
            val p1 = points[i]
            val p2 = points[(i + 1) % points.size]
            perimeter += haversineDistance(p1, p2)
        }
        return perimeter
    }

    private fun haversineDistance(p1: GeoPoint, p2: GeoPoint): Double {
        val earthRadius = 6378137.0 // meters
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val lat1 = Math.toRadians(p1.latitude)
        val lat2 = Math.toRadians(p2.latitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun calculateArea(points: List<GeoPoint>): Double {
        // Spherical polygon area calculation using the Earth radius
        val earthRadius = 6378137.0 // meters

        var area = 0.0
        if (points.size > 2) {
            for (i in points.indices) {
                val p1 = points[i]
                val p2 = points[(i + 1) % points.size]

                // Convert to radians
                val lat1 = Math.toRadians(p1.latitude)
                val lon1 = Math.toRadians(p1.longitude)
                val lat2 = Math.toRadians(p2.latitude)
                val lon2 = Math.toRadians(p2.longitude)

                // Spherical excess component
                area += (lon2 - lon1) * (2.0 + sin(lat1) + sin(lat2))
            }
            area = area * earthRadius * earthRadius / 2.0
        }
        return abs(area)
    }
}
