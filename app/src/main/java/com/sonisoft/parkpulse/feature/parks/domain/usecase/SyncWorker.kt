package com.sonisoft.parkpulse.feature.parks.domain.usecase

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Because syncManager is a singleton and manages its own state
        syncManager.syncNow()

        // After syncNow completes, we check if there are still pending items
        // If there are failures, we might want to retry, but syncManager logs its own errors
        val state = syncManager.syncState.first()

        return if (state.failed > 0) {
            Result.retry() // Retry according to WorkManager's backoff policy
        } else {
            Result.success()
        }
    }
}
