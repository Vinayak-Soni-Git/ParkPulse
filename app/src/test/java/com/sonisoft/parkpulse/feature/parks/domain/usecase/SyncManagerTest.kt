package com.sonisoft.parkpulse.feature.parks.domain.usecase

import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.CleanlinessRating
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey
import com.sonisoft.parkpulse.feature.parks.domain.model.SurveyPhoto
import com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus
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
class SyncManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val syncRepository: SyncRepository = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk()

    private lateinit var syncManager: SyncManager

    private val samplePark = Park(
        id = "park_1",
        name = "Jaipur Central Park",
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
        syncStatus = SyncStatus.PENDING,
        remoteId = null
    )

    private val sampleSurvey = Survey(
        id = "survey_1",
        parkId = "park_1",
        checkInTimestamp = 12345L,
        latitude = 26.9,
        longitude = 75.7,
        accuracyMeters = 5f,
        treeCount = 10,
        hasLighting = true,
        cleanliness = CleanlinessRating.GOOD,
        notes = null,
        createdAt = 12345L,
        updatedAt = 12345L,
        isPendingSync = true,
        syncStatus = SyncStatus.PENDING,
        remoteId = null
    )

    private val samplePhoto = SurveyPhoto(
        id = "photo_1",
        surveyId = "survey_1",
        parkId = "park_1",
        localFilePath = "path.jpg",
        capturedAt = 12345L,
        latitude = 26.9,
        longitude = 75.7,
        accuracyMeters = 5f,
        createdAt = 12345L,
        isPendingSync = true,
        syncStatus = SyncStatus.PENDING,
        remoteId = null
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.isOnline } returns flowOf(true)
        syncManager = SyncManager(syncRepository, networkMonitor)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `syncNow aborts when offline`() = runTest {
        every { networkMonitor.isOnline } returns flowOf(false)

        syncManager.syncNow()

        assertEquals("Device is offline. Sync unavailable.", syncManager.syncState.value.error)
        coVerify(exactly = 0) { syncRepository.getPendingParks() }
    }

    @Test
    fun `syncNow uploads successfully in dependency order`() = runTest(testDispatcher) {
        coEvery { syncRepository.getPendingParks() } returns listOf(samplePark)
        coEvery { syncRepository.getPendingSurveys() } returns listOf(sampleSurvey)
        coEvery { syncRepository.getPendingPhotos() } returns listOf(samplePhoto)

        coEvery { syncRepository.uploadPark(any()) } returns true
        coEvery { syncRepository.uploadSurvey(any()) } returns true
        coEvery { syncRepository.uploadPhoto(any()) } returns true

        syncManager.syncNow()

        coVerify(exactly = 1) { syncRepository.uploadPark(samplePark) }
        coVerify(exactly = 1) { syncRepository.uploadSurvey(sampleSurvey) }
        coVerify(exactly = 1) { syncRepository.uploadPhoto(samplePhoto) }

        assertEquals(3, syncManager.syncState.value.successful)
        assertEquals(0, syncManager.syncState.value.failed)
    }

    @Test
    fun `syncNow blocks dependent survey and photo when park fails`() = runTest(testDispatcher) {
        coEvery { syncRepository.getPendingParks() } returns listOf(samplePark)
        coEvery { syncRepository.getPendingSurveys() } returns listOf(sampleSurvey)
        coEvery { syncRepository.getPendingPhotos() } returns listOf(samplePhoto)

        coEvery { syncRepository.uploadPark(any()) } returns false // Park fails

        syncManager.syncNow()

        coVerify(exactly = 1) { syncRepository.uploadPark(samplePark) }
        coVerify(exactly = 0) { syncRepository.uploadSurvey(any()) }
        coVerify(exactly = 0) { syncRepository.uploadPhoto(any()) }

        assertEquals(0, syncManager.syncState.value.successful)
        assertEquals(3, syncManager.syncState.value.failed)
    }

    @Test
    fun `syncNow detects version conflict on park edit`() = runTest(testDispatcher) {
        val conflictPark = samplePark.copy(id = "conflict_park", baseServerVersion = 2, serverVersion = 2)
        coEvery { syncRepository.getPendingParks() } returns listOf(conflictPark)
        coEvery { syncRepository.getPendingSurveys() } returns emptyList()
        coEvery { syncRepository.getPendingPhotos() } returns emptyList()

        // Simulate conflict from repository/api
        coEvery { syncRepository.uploadPark(any()) } answers {
            val p = firstArg<Park>()
            if (p.id == "conflict_park") false else true
        }

        syncManager.syncNow()

        coVerify(exactly = 1) { syncRepository.uploadPark(conflictPark) }
        assertEquals(0, syncManager.syncState.value.successful)
        assertEquals(1, syncManager.syncState.value.failed)
    }
}
