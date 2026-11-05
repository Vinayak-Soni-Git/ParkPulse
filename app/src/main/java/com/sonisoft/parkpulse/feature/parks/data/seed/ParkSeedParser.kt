package com.sonisoft.parkpulse.feature.parks.data.seed

import com.sonisoft.parkpulse.feature.parks.data.local.entity.ParkBoundaryPointEntity
import com.sonisoft.parkpulse.feature.parks.data.local.entity.ParkEntity
import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class ParkSeedParser @Inject constructor() {

    fun parse(jsonString: String): Pair<List<ParkEntity>, List<ParkBoundaryPointEntity>> {
        val parks = mutableListOf<ParkEntity>()
        val allPoints = mutableListOf<ParkBoundaryPointEntity>()

        val rootObject = JSONObject(jsonString)
        val parksArray = rootObject.optJSONArray("parks") ?: JSONArray()

        for (i in 0 until parksArray.length()) {
            val parkObj = parksArray.optJSONObject(i) ?: continue

            val id = parkObj.optString("id")
            val name = parkObj.optString("name")
            if (id.isBlank() || name.isBlank()) continue // Skip malformed

            val type = ParkType.fromString(parkObj.optString("type"))
            val areaInSquareMeters = parkObj.optDouble("areaInSquareMeters", 0.0)
            val perimeterInMeters = parkObj.optDouble("perimeterInMeters", 0.0)
            val status = ParkStatus.fromString(parkObj.optString("status"))

            // lastSurveyDate can be null in the JSON
            val lastSurveyDate = if (parkObj.isNull("lastSurveyDate")) null else parkObj.optLong("lastSurveyDate")
            val isPendingSync = parkObj.optBoolean("isPendingSync", false)
            val statusIsDemo = parkObj.optBoolean("statusIsDemo", false)
            val boundaryQuality = BoundaryQuality.fromString(parkObj.optString("boundaryQuality"))

            // Parse source
            val sourceObj = parkObj.optJSONObject("source")
            val sourceProvider = sourceObj?.optString("provider")
            val sourceOsmType = sourceObj?.optString("osmType")
            val sourceOsmId = if (sourceObj?.isNull("osmId") == false) sourceObj.optLong("osmId") else null
            val sourceAttribution = sourceObj?.optString("attribution")
            val sourceLicense = sourceObj?.optString("license")

            // Parse boundary points
            val pointsArray = parkObj.optJSONArray("boundaryPoints") ?: JSONArray()
            if (pointsArray.length() < 3) continue // Skip invalid geometries

            val parkEntity = ParkEntity(
                id = id,
                name = name,
                type = type,
                areaInSquareMeters = areaInSquareMeters,
                perimeterInMeters = perimeterInMeters,
                status = status,
                lastSurveyDate = lastSurveyDate,
                isPendingSync = isPendingSync,
                boundaryQuality = boundaryQuality,
                statusIsDemo = statusIsDemo,
                sourceProvider = sourceProvider,
                sourceOsmType = sourceOsmType,
                sourceOsmId = sourceOsmId,
                sourceAttribution = sourceAttribution,
                sourceLicense = sourceLicense
            )

            parks.add(parkEntity)

            for (j in 0 until pointsArray.length()) {
                val pointObj = pointsArray.optJSONObject(j) ?: continue
                val lat = pointObj.optDouble("latitude")
                val lon = pointObj.optDouble("longitude")

                if (lat.isNaN() || lon.isNaN() || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                    continue // Skip invalid point (but if enough points are valid, the polygon might still be okay - though ideally we'd invalidate the whole polygon if a point is corrupt. In OSM exports, coordinates are usually strictly validated).
                }

                allPoints.add(
                    ParkBoundaryPointEntity(
                        parkId = id,
                        latitude = lat,
                        longitude = lon,
                        pointIndex = j
                    )
                )
            }
        }

        return Pair(parks, allPoints)
    }
}
