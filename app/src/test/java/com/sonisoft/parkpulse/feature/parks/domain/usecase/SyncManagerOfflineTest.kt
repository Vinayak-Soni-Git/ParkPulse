package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import com.sonisoft.parkpulse.feature.parks.domain.network.NetworkMonitor
import com.sonisoft.parkpulse.feature.parks.domain.repository.SyncRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerOfflineTest {

    private val testDispatcher = StandardTestDispatcher()
    private val syncRepository: SyncRepository = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk()

    private lateinit var syncManager: SyncManager

    private val samplePark = Park(
        id = "park_offline",
        name = "Offline Test Park",
        type = ParkType.GARDEN,
        areaInSquareMeters = 50000.0,
        perimeterInMeters = 1000.0,
        status = ParkStatus.OPEN,
        lastSurveyDate = null,
        isPendingSync = true,
        boundaryPoints = listOf(GeoPoint(26.9, 75.7)),
        boundaryQuality = BoundaryQuality.VERIFIED,
        statusIsDemo = false,
        source = null,
        syncStatus = com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.PENDING,
        remoteId = null
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        syncManager = SyncManager(syncRepository, networkMonitor)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `syncNow does not call repository when offline`() = runTest {
        every { networkMonitor.isOnline } returns flowOf(false)
        coEvery { syncRepository.getPendingParks() } returns listOf(samplePark)

        syncManager.syncNow()

        assertEquals("Device is offline. Sync unavailable.", syncManager.syncState.value.error)

        // Assert no network uploads happened
        coVerify(exactly = 0) { syncRepository.uploadPark(any()) }
        coVerify(exactly = 0) { syncRepository.uploadSurvey(any()) }
        coVerify(exactly = 0) { syncRepository.uploadPhoto(any()) }
    }
}
