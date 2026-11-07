package com.sonisoft.parkpulse.feature.parks.presentation.survey

import androidx.lifecycle.SavedStateHandle
import com.sonisoft.parkpulse.feature.parks.data.camera.WatermarkProcessor
import com.sonisoft.parkpulse.feature.parks.domain.location.LocationService
import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import com.sonisoft.parkpulse.feature.parks.domain.model.SurveyPhoto
import com.sonisoft.parkpulse.feature.parks.domain.model.UserLocation
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import com.sonisoft.parkpulse.feature.parks.domain.usecase.GenerateTestCoordinatesUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.IsPointInPolygonUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ValidateCheckInUseCase
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
class SurveyPhotoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val parkRepository: ParkRepository = mockk(relaxed = true)
    private val locationService: LocationService = mockk()
    private val watermarkProcessor: WatermarkProcessor = mockk()
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
        every { parkRepository.getPhotosForPark("park_1") } returns flowOf(emptyList())
        every { locationService.hasLocationPermission() } returns true
        every { locationService.requestDetailedLocationUpdates() } returns flowOf(
            UserLocation(
                latitude = 26.9050,
                longitude = 75.7050,
                accuracyMeters = 5.0f,
                timestamp = System.currentTimeMillis(),
                isSimulated = false
            )
        )

        viewModel = SurveyViewModel(
            savedStateHandle,
            parkRepository,
            locationService,
            validateCheckInUseCase,
            generateTestCoordinatesUseCase,
            watermarkProcessor,
            mockk(relaxed = true)
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `openCamera opens camera when real validated check-in exists`() {
        viewModel.performCheckIn()
        viewModel.openCamera()

        assertTrue(viewModel.uiState.value.isCameraOpen)
    }

    @Test
    fun `openCamera IS BLOCKED when simulated location is active`() {
        viewModel.applySimulation(SimulationScenario.INSIDE_PARK, 3f)

        viewModel.openCamera()

        assertFalse(viewModel.uiState.value.isCameraOpen)
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `removeAttachedPhoto removes photo from list`() {
        val photo = SurveyPhoto(
            id = "photo_1",
            surveyId = "survey_1",
            parkId = "park_1",
            localFilePath = "/tmp/dummy.jpg",
            capturedAt = System.currentTimeMillis(),
            latitude = 26.9050,
            longitude = 75.7050,
            accuracyMeters = 5.0f,
            createdAt = System.currentTimeMillis()
        )

        viewModel.performCheckIn()
        // Simulate attaching photo
        viewModel.acceptPendingPhoto()

        viewModel.removeAttachedPhoto(photo)
        assertTrue(viewModel.uiState.value.attachedPhotos.isEmpty())
    }
}
