package com.sonisoft.parkpulse.feature.parks.presentation.survey

import androidx.lifecycle.SavedStateHandle
import com.sonisoft.parkpulse.feature.parks.domain.location.LocationService
import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import com.sonisoft.parkpulse.feature.parks.domain.usecase.GenerateTestCoordinatesUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.IsPointInPolygonUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ValidateCheckInUseCase
import io.mockk.coVerify
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SurveyViewModelSimulationTest {

    private val testDispatcher = StandardTestDispatcher()

    private val parkRepository: ParkRepository = mockk(relaxed = true)
    private val locationService: LocationService = mockk()
    private val isPointInPolygonUseCase = IsPointInPolygonUseCase()
    private val validateCheckInUseCase = ValidateCheckInUseCase(isPointInPolygonUseCase)
    private val generateTestCoordinatesUseCase = GenerateTestCoordinatesUseCase(isPointInPolygonUseCase)

    private lateinit var viewModel: SurveyViewModel

    private val samplePark = Park(
        id = "park_1",
        name = "Jaipur Square Park",
        type = ParkType.GARDEN,
        areaInSquareMeters = 10000.0,
        perimeterInMeters = 400.0,
        status = ParkStatus.OPEN,
        lastSurveyDate = null,
        isPendingSync = false,
        boundaryPoints = listOf(
            GeoPoint(26.9000, 75.7000),
            GeoPoint(26.9000, 75.7100),
            GeoPoint(26.9100, 75.7100),
            GeoPoint(26.9100, 75.7000)
        ),
        boundaryQuality = BoundaryQuality.VERIFIED,
        statusIsDemo = false,
        source = null
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val savedStateHandle = SavedStateHandle(mapOf("parkId" to "park_1"))
        every { parkRepository.getParkById("park_1") } returns flowOf(samplePark)
        every { parkRepository.getSurveysForPark("park_1") } returns flowOf(emptyList())
        every { locationService.hasLocationPermission() } returns true
        every { locationService.requestDetailedLocationUpdates() } returns flowOf(null)

        viewModel = SurveyViewModel(
            savedStateHandle,
            parkRepository,
            locationService,
            validateCheckInUseCase,
            generateTestCoordinatesUseCase,
            mockk(relaxed = true),
            mockk(relaxed = true)
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `applySimulation INSIDE_PARK with 3m accuracy yields INSIDE_PARK status`() {
        viewModel.applySimulation(SimulationScenario.INSIDE_PARK, 3f)

        val state = viewModel.uiState.value
        assertTrue(state.isSimulationActive)
        assertEquals(ValidateCheckInUseCase.ValidationStatus.INSIDE_PARK, state.checkInResult?.status)
        assertTrue(state.checkInResult?.isEligibleForCheckIn == true)
    }

    @Test
    fun `applySimulation INSIDE_PARK with 26m accuracy yields GPS_ACCURACY_INSUFFICIENT`() {
        viewModel.applySimulation(SimulationScenario.INSIDE_PARK, 26f)

        val state = viewModel.uiState.value
        assertTrue(state.isSimulationActive)
        assertEquals(ValidateCheckInUseCase.ValidationStatus.GPS_ACCURACY_INSUFFICIENT, state.checkInResult?.status)
        assertFalse(state.checkInResult?.isEligibleForCheckIn == true)
    }

    @Test
    fun `applySimulation OUTSIDE_PARK with 3m accuracy yields OUTSIDE_PARK`() {
        viewModel.applySimulation(SimulationScenario.OUTSIDE_PARK, 3f)

        val state = viewModel.uiState.value
        assertTrue(state.isSimulationActive)
        assertEquals(ValidateCheckInUseCase.ValidationStatus.OUTSIDE_PARK, state.checkInResult?.status)
        assertFalse(state.checkInResult?.isEligibleForCheckIn == true)
    }

    @Test
    fun `performCheckIn IS BLOCKED when simulation is active`() {
        viewModel.applySimulation(SimulationScenario.INSIDE_PARK, 3f)
        assertTrue(viewModel.uiState.value.isSimulationActive)

        viewModel.performCheckIn()

        assertEquals(SurveyStep.CHECK_IN, viewModel.uiState.value.step) // Did NOT advance
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `saveSurvey IS BLOCKED when simulation is active`() {
        viewModel.applySimulation(SimulationScenario.INSIDE_PARK, 3f)

        viewModel.saveSurvey()

        coVerify(exactly = 0) { parkRepository.saveSurvey(any()) }
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `resetToRealGps clears simulation active state`() {
        viewModel.applySimulation(SimulationScenario.INSIDE_PARK, 3f)
        assertTrue(viewModel.uiState.value.isSimulationActive)

        viewModel.resetToRealGps()

        assertFalse(viewModel.uiState.value.isSimulationActive)
        assertEquals(SimulationScenario.REAL_GPS, viewModel.uiState.value.simulationScenario)
    }
}
