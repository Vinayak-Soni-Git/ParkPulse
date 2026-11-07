package com.sonisoft.parkpulse.feature.parks.domain.network

sealed class SyncResult<out T> {
    data class Success<out T>(val remoteId: String, val serverVersion: Int = 0) : SyncResult<T>()
    data class Error(val message: String) : SyncResult<Nothing>()
    data class Conflict(val message: String) : SyncResult<Nothing>()
}
