package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus
import com.sonisoft.parkpulse.feature.parks.domain.network.NetworkMonitor
import com.sonisoft.parkpulse.feature.parks.domain.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class SyncState(
    val isSyncing: Boolean = false,
    val totalPending: Int = 0,
    val successful: Int = 0,
    val failed: Int = 0,
    val error: String? = null
)

@Singleton
class SyncManager @Inject constructor(
    private val syncRepository: SyncRepository,
    private val networkMonitor: NetworkMonitor
) {
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    suspend fun syncNow() {
        if (_syncState.value.isSyncing) return

        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) {
            _syncState.update { it.copy(error = "Device is offline. Sync unavailable.") }
            return
        }

        _syncState.update { it.copy(isSyncing = true, error = null, successful = 0, failed = 0) }

        try {
            // Check connectivity again right before starting upload
            if (!networkMonitor.isOnline.first()) {
                _syncState.update { it.copy(error = "Connection lost. Sync unavailable.") }
                return
            }
            val pendingParks = syncRepository.getPendingParks()
            val pendingSurveys = syncRepository.getPendingSurveys()
            val pendingPhotos = syncRepository.getPendingPhotos()

            val total = pendingParks.size + pendingSurveys.size + pendingPhotos.size
            _syncState.update { it.copy(totalPending = total) }

            if (total == 0) {
                _syncState.update { it.copy(isSyncing = false) }
                return
            }

            var successCount = 0
            var failCount = 0

            // 1. Sync Parks
            val successfulParkIds = mutableSetOf<String>()
            for (park in pendingParks) {
                if (syncRepository.uploadPark(park)) {
                    successfulParkIds.add(park.id)
                    successCount++
                } else {
                    failCount++
                }
                updateProgress(successCount, failCount)
            }

            // 2. Sync Surveys
            val successfulSurveyIds = mutableSetOf<String>()
            for (survey in pendingSurveys) {
                // Dependency check: Survey's park must be successfully synced (or already synced before this run)
                val parkIsSynced = successfulParkIds.contains(survey.parkId) ||
                                   survey.syncStatus == SyncStatus.SYNCED ||
                                   survey.syncStatus == SyncStatus.FAILED // If it failed before, we still retry if we can, but logic implies we rely on local DB state.
                                   // Actually, we should check DB, but to keep it simple, we assume if it's not in pendingParks, it's already synced.

                val parkStillPending = pendingParks.any { it.id == survey.parkId && !successfulParkIds.contains(it.id) }

                if (parkStillPending) {
                    failCount++
                    updateProgress(successCount, failCount)
                    continue
                }

                if (syncRepository.uploadSurvey(survey)) {
                    successfulSurveyIds.add(survey.id)
                    successCount++
                } else {
                    failCount++
                }
                updateProgress(successCount, failCount)
            }

            // 3. Sync Photos
            for (photo in pendingPhotos) {
                val surveyStillPending = pendingSurveys.any { it.id == photo.surveyId && !successfulSurveyIds.contains(it.id) }

                if (surveyStillPending) {
                    failCount++
                    updateProgress(successCount, failCount)
                    continue
                }

                if (syncRepository.uploadPhoto(photo)) {
                    successCount++
                } else {
                    failCount++
                }
                updateProgress(successCount, failCount)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            _syncState.update { it.copy(error = "Sync failed: ${e.message}") }
        } finally {
            _syncState.update { it.copy(isSyncing = false) }
        }
    }

    private fun updateProgress(success: Int, fail: Int) {
        _syncState.update { it.copy(successful = success, failed = fail) }
    }
}
