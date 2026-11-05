package com.sonisoft.parkpulse.feature.parks.domain.model

enum class CleanlinessRating {
    GOOD,
    FAIR,
    POOR;

    companion object {
        fun fromString(value: String?): CleanlinessRating {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: GOOD
        }
    }
}
