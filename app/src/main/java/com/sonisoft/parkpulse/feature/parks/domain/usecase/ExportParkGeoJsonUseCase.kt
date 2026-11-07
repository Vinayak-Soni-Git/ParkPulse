package com.sonisoft.parkpulse.feature.parks.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportParkGeoJsonUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun invoke(park: Park): Uri {
        val geoJsonString = buildGeoJson(park)

        val exportsDir = File(context.cacheDir, "exports")
        if (!exportsDir.exists()) exportsDir.mkdirs()

        // Clean old exports to avoid hoarding
        exportsDir.listFiles()?.forEach { it.delete() }

        val fileName = "park_${park.name.replace(Regex("[^a-zA-Z0-9]"), "_")}.geojson"
        val file = File(exportsDir, fileName)

        file.writeText(geoJsonString, Charsets.UTF_8)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun buildGeoJson(park: Park): String {
        val sb = java.lang.StringBuilder()
        sb.append("{\n")
        sb.append("  \"type\": \"FeatureCollection\",\n")
        sb.append("  \"features\": [\n")
        sb.append("    {\n")
        sb.append("      \"type\": \"Feature\",\n")
        sb.append("      \"properties\": {\n")
        sb.append("        \"id\": \"${park.id}\",\n")
        sb.append("        \"name\": \"${park.name}\",\n")
        sb.append("        \"type\": \"${park.type.name}\",\n")
        sb.append("        \"status\": \"${park.status.name}\",\n")
        sb.append("        \"areaInSquareMeters\": ${park.areaInSquareMeters},\n")
        sb.append("        \"perimeterInMeters\": ${park.perimeterInMeters}\n")
        sb.append("      },\n")
        sb.append("      \"geometry\": {\n")
        sb.append("        \"type\": \"Polygon\",\n")
        sb.append("        \"coordinates\": [\n")
        sb.append("          [\n")

        val points = park.boundaryPoints
        if (points.isNotEmpty()) {
            val coords = points.map { "[${it.longitude}, ${it.latitude}]" }.toMutableList()
            // Close the polygon ring
            if (points.first() != points.last()) {
                coords.add("[${points.first().longitude}, ${points.first().latitude}]")
            }
            sb.append("            " + coords.joinToString(",\n            "))
            sb.append("\n")
        }

        sb.append("          ]\n")
        sb.append("        ]\n")
        sb.append("      }\n")
        sb.append("    }\n")
        sb.append("  ]\n")
        sb.append("}")

        return sb.toString()
    }
}
