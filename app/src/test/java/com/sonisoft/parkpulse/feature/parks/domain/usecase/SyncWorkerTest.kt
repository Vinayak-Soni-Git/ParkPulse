package com.sonisoft.parkpulse.feature.parks.domain.usecase

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncWorkerTest {

    private val context: Context = mockk(relaxed = true)
    private val workerParams: WorkerParameters = mockk(relaxed = true)
    private val syncManager: SyncManager = mockk(relaxed = true)

    private val worker = SyncWorker(context, workerParams, syncManager)

    @Test
    fun `doWork calls syncNow and returns success when no failures`() = runTest {
        val stateFlow = kotlinx.coroutines.flow.MutableStateFlow(SyncState(failed = 0))
        coEvery { syncManager.syncNow() } returns Unit
        every { syncManager.syncState } returns stateFlow

        val result = worker.doWork()

        coVerify { syncManager.syncNow() }
        assertEquals(Result.success(), result)
    }

    @Test
    fun `doWork returns retry when sync has failures`() = runTest {
        val stateFlow = kotlinx.coroutines.flow.MutableStateFlow(SyncState(failed = 1))
        coEvery { syncManager.syncNow() } returns Unit
        every { syncManager.syncState } returns stateFlow

        val result = worker.doWork()

        coVerify { syncManager.syncNow() }
        assertEquals(Result.retry(), result)
    }
}
