package com.sonisoft.parkpulse.feature.parks.presentation.survey

import androidx.lifecycle.SavedStateHandle
import com.sonisoft.parkpulse.feature.parks.domain.location.LocationService
import com.sonisoft.parkpulse.feature.parks.domain.model.CleanlinessRating
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import com.sonisoft.parkpulse.feature.parks.domain.usecase.GenerateTestCoordinatesUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.IsPointInPolygonUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ValidateCheckInUseCase
import io.mockk.coEvery
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SurveyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val parkRepository: ParkRepository = mockk()
    private val locationService: LocationService = mockk()
    private val validateCheckInUseCase = ValidateCheckInUseCase(IsPointInPolygonUseCase())

    private lateinit var viewModel: SurveyViewModel

    private val samplePark = Park(
        id = "park_1",
        name = "Jaipur Central Park",
        type = ParkType.GARDEN,
        areaInSquareMeters = 50000.0,
        perimeterInMeters = 1000.0,
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
        every { locationService.hasLocationPermission() } returns false

        viewModel = SurveyViewModel(
            savedStateHandle,
            parkRepository,
            locationService,
            validateCheckInUseCase,
            GenerateTestCoordinatesUseCase(IsPointInPolygonUseCase())
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tree count validation rejects negative values`() {
        viewModel.onTreeCountChanged("-5")
        assertNotNull(viewModel.uiState.value.treeCountError)

        viewModel.onTreeCountChanged("12")
        assertNull(viewModel.uiState.value.treeCountError)
    }

    @Test
    fun `proceedToReview validates required form fields`() {
        viewModel.onTreeCountChanged("10")
        viewModel.proceedToReview() // Missing lighting & cleanliness

        assertNotNull(viewModel.uiState.value.lightingError)
        assertNotNull(viewModel.uiState.value.cleanlinessError)
        assertEquals(SurveyStep.CHECK_IN, viewModel.uiState.value.step) // Step didn't advance

        viewModel.onLightingSelected(true)
        viewModel.onCleanlinessSelected(CleanlinessRating.GOOD)
        viewModel.proceedToReview()

        assertEquals(SurveyStep.REVIEW, viewModel.uiState.value.step)
    }
}
