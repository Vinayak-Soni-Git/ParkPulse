package com.sonisoft.parkpulse.feature.parks.presentation.map

import com.sonisoft.parkpulse.feature.parks.domain.location.LocationService
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import com.sonisoft.parkpulse.feature.parks.domain.usecase.CalculateAreaAndPerimeterUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.CalculateDistanceAndBearingUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ExportParkGeoJsonUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.GetAllParksUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ValidatePolygonUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelWalkModeTest {

    private val testDispatcher = StandardTestDispatcher()

    private val locationFlow = MutableStateFlow<UserLocation?>(null)
    private val locationService: LocationService = mockk {
        every { requestLocationUpdates() } returns flowOf(null)
        every { requestDetailedLocationUpdates() } returns locationFlow
        every { hasLocationPermission() } returns true
    }

    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        viewModel = MapViewModel(
            mockk(relaxed = true) { every { this@mockk.invoke() } returns flowOf(emptyList()) },
            mockk(relaxed = true),
            locationService,
            CalculateDistanceAndBearingUseCase(),
            CalculateAreaAndPerimeterUseCase(),
            ValidatePolygonUseCase(),
            mockk(relaxed = true),
            mockk(relaxed = true)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `walk mode only adds points with 20m or better accuracy`() {
        viewModel.toggleDrawingMode()
        viewModel.toggleWalkMode()
        viewModel.toggleWalking()

        // Push a bad location
        locationFlow.value = UserLocation(26.9, 75.7, 50f, 1000)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.drawingPoints.size)

        // Push a good location
        locationFlow.value = UserLocation(26.9, 75.7, 10f, 2000)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.drawingPoints.size)

        // Push another bad location
        locationFlow.value = UserLocation(26.91, 75.71, 25f, 3000)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.drawingPoints.size)
    }
}
