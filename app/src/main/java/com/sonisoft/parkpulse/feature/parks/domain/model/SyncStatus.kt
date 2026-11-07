package com.sonisoft.parkpulse.feature.parks.domain.model

enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED;

    companion object {
        fun fromString(value: String?): SyncStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}
