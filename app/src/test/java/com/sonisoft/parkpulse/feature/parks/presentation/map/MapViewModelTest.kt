package com.sonisoft.parkpulse.feature.parks.presentation.map

import com.sonisoft.parkpulse.feature.parks.domain.location.LocationService
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import com.sonisoft.parkpulse.feature.parks.domain.usecase.CalculateAreaAndPerimeterUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.CalculateDistanceAndBearingUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.GetAllParksUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getAllParksUseCase: GetAllParksUseCase = mockk()
    private val parkRepository: ParkRepository = mockk()
    private val locationService: LocationService = mockk()
    private val calculateDistanceAndBearingUseCase: CalculateDistanceAndBearingUseCase = mockk()
    private val calculateAreaAndPerimeterUseCase = CalculateAreaAndPerimeterUseCase()

    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { getAllParksUseCase() } returns flowOf(emptyList())
        every { locationService.hasLocationPermission() } returns false

        viewModel = MapViewModel(
            getAllParksUseCase,
            parkRepository,
            locationService,
            calculateDistanceAndBearingUseCase,
            calculateAreaAndPerimeterUseCase,
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleDrawingMode clears drawing points and toggles state`() {
        assertFalse(viewModel.uiState.value.isDrawingMode)

        viewModel.toggleDrawingMode()
        assertTrue(viewModel.uiState.value.isDrawingMode)

        viewModel.addDrawingPoint(GeoPoint(26.9, 75.7))
        assertEquals(1, viewModel.uiState.value.drawingPoints.size)

        viewModel.toggleDrawingMode()
        assertFalse(viewModel.uiState.value.isDrawingMode)
        assertTrue(viewModel.uiState.value.drawingPoints.isEmpty())
    }

    @Test
    fun `addDrawingPoint ignores duplicate consecutive points`() {
        viewModel.toggleDrawingMode()

        val p1 = GeoPoint(26.9000, 75.7000)
        val p2 = GeoPoint(26.9000, 75.7000) // Duplicate
        val p3 = GeoPoint(26.9100, 75.7100)

        viewModel.addDrawingPoint(p1)
        viewModel.addDrawingPoint(p2)
        assertEquals(1, viewModel.uiState.value.drawingPoints.size)

        viewModel.addDrawingPoint(p3)
        assertEquals(2, viewModel.uiState.value.drawingPoints.size)
    }

    @Test
    fun `updateDrawingPoint updates vertex location`() {
        viewModel.toggleDrawingMode()
        viewModel.addDrawingPoint(GeoPoint(26.9000, 75.7000))
        viewModel.addDrawingPoint(GeoPoint(26.9100, 75.7100))

        val newPoint = GeoPoint(26.9500, 75.7500)
        viewModel.updateDrawingPoint(0, newPoint)

        assertEquals(newPoint, viewModel.uiState.value.drawingPoints[0])
    }

    @Test
    fun `undoLastDrawingPoint removes the last vertex`() {
        viewModel.toggleDrawingMode()
        viewModel.addDrawingPoint(GeoPoint(26.9000, 75.7000))
        viewModel.addDrawingPoint(GeoPoint(26.9100, 75.7100))

        viewModel.undoLastDrawingPoint()
        assertEquals(1, viewModel.uiState.value.drawingPoints.size)
    }

    @Test
    fun `saveNewPark creates park with PENDING status`() {
        val slot = io.mockk.slot<com.sonisoft.parkpulse.feature.parks.domain.model.Park>()
        io.mockk.coEvery { parkRepository.savePark(capture(slot)) } returns Unit

        viewModel.toggleDrawingMode()
        viewModel.addDrawingPoint(GeoPoint(26.9000, 75.7000))
        viewModel.addDrawingPoint(GeoPoint(26.9100, 75.7000))
        viewModel.addDrawingPoint(GeoPoint(26.9100, 75.7100))

        viewModel.saveNewPark("Central Park", com.sonisoft.parkpulse.feature.parks.domain.model.ParkType.GARDEN)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(slot.isCaptured)
        assertEquals(com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.PENDING, slot.captured.syncStatus)
        assertEquals("Central Park", slot.captured.name)
    }

    @Test
    fun `editing existing park preserves its ID and marks PENDING`() {
        val existingPark = com.sonisoft.parkpulse.feature.parks.domain.model.Park(
            id = "existing_id",
            name = "Old Name",
            type = com.sonisoft.parkpulse.feature.parks.domain.model.ParkType.GARDEN,
            areaInSquareMeters = 0.0,
            perimeterInMeters = 0.0,
            status = com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus.OPEN,
            lastSurveyDate = null,
            isPendingSync = false,
            boundaryPoints = listOf(
                GeoPoint(26.9, 75.7), GeoPoint(26.91, 75.7), GeoPoint(26.91, 75.71)
            ),
            boundaryQuality = com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality.VERIFIED,
            statusIsDemo = false,
            source = null,
            syncStatus = com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.SYNCED,
            remoteId = "remote_1",
            serverVersion = 5
        )

        io.mockk.every { parkRepository.getParkById("existing_id") } returns flowOf(existingPark)

        val slot = io.mockk.slot<com.sonisoft.parkpulse.feature.parks.domain.model.Park>()
        io.mockk.coEvery { parkRepository.savePark(capture(slot)) } returns Unit

        viewModel.toggleDrawingMode(existingPark)
        
        viewModel.saveNewPark("New Name", com.sonisoft.parkpulse.feature.parks.domain.model.ParkType.PLAYGROUND)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(slot.isCaptured)
        assertEquals("existing_id", slot.captured.id)
        assertEquals("New Name", slot.captured.name)
        assertEquals("remote_1", slot.captured.remoteId)
        assertEquals(5, slot.captured.serverVersion)
        assertEquals(com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.PENDING, slot.captured.syncStatus)
    }
}
