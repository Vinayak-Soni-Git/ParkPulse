package com.sonisoft.parkpulse.feature.parks.domain.model

enum class ParkType {
    GARDEN,
    PLAYGROUND,
    FOREST_PARK,
    LAKE_FRONT,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ParkType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

enum class ParkStatus {
    OPEN,
    NEEDS_REPAIR,
    CLOSED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ParkStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

enum class BoundaryQuality {
    OSM_MAPPED_UNVERIFIED,
    NEEDS_REVIEW,
    VERIFIED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): BoundaryQuality {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
