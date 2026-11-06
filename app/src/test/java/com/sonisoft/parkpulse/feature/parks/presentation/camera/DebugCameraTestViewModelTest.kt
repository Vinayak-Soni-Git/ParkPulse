package com.sonisoft.parkpulse.feature.parks.presentation.camera

import com.sonisoft.parkpulse.feature.parks.data.camera.WatermarkProcessor
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DebugCameraTestViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val watermarkProcessor: WatermarkProcessor = mockk(relaxed = true)

    private lateinit var viewModel: DebugCameraTestViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DebugCameraTestViewModel(watermarkProcessor)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `openCamera toggles state correctly`() {
        viewModel.openCamera(applyWatermark = true)
        assertTrue(viewModel.uiState.value.isCameraOpen)
        assertTrue(viewModel.uiState.value.applyWatermark)

        viewModel.closeCamera()
        assertFalse(viewModel.uiState.value.isCameraOpen)
    }

    @Test
    fun `acceptPhoto adds pending photo to gallery`() {
        val mockFile = File("mock/path.jpg")

        viewModel.openCamera(applyWatermark = false)
        viewModel.onPhotoCaptured(mockFile) // Sets pendingPhoto because applyWatermark = false

        assertEquals(mockFile, viewModel.uiState.value.pendingPhoto)

        viewModel.acceptPhoto()

        assertTrue(viewModel.uiState.value.pendingPhoto == null)
        assertTrue(viewModel.uiState.value.gallery.contains(mockFile))
    }
}
