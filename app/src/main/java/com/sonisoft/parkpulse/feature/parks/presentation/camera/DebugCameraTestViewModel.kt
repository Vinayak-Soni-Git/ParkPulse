package com.sonisoft.parkpulse.feature.parks.presentation.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonisoft.parkpulse.feature.parks.data.camera.WatermarkProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class DebugCameraTestUiState(
    val isCameraOpen: Boolean = false,
    val applyWatermark: Boolean = true,
    val isProcessing: Boolean = false,
    val pendingPhoto: File? = null,
    val gallery: List<File> = emptyList()
)

@HiltViewModel
class DebugCameraTestViewModel @Inject constructor(
    private val watermarkProcessor: WatermarkProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebugCameraTestUiState())
    val uiState: StateFlow<DebugCameraTestUiState> = _uiState.asStateFlow()

    fun openCamera(applyWatermark: Boolean) {
        _uiState.update { it.copy(isCameraOpen = true, applyWatermark = applyWatermark) }
    }

    fun closeCamera() {
        _uiState.update { it.copy(isCameraOpen = false) }
    }

    fun onPhotoCaptured(tempFile: File) {
        if (!_uiState.value.applyWatermark) {
            _uiState.update {
                it.copy(
                    isCameraOpen = false,
                    pendingPhoto = tempFile
                )
            }
            return
        }

        _uiState.update { it.copy(isProcessing = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val metadata = WatermarkProcessor.WatermarkMetadata(
                    parkName = "DEMO PARK [DEBUG]",
                    latitude = 99.9999,
                    longitude = 99.9999,
                    accuracyMeters = 999.0f,
                    capturedAt = System.currentTimeMillis()
                )

                val processedFile = watermarkProcessor.processAndSaveWatermarkedPhoto(tempFile, metadata)

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        isCameraOpen = false,
                        pendingPhoto = processedFile
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isProcessing = false, isCameraOpen = false) }
            }
        }
    }

    fun acceptPhoto() {
        val file = _uiState.value.pendingPhoto ?: return
        _uiState.update {
            it.copy(
                gallery = it.gallery + file,
                pendingPhoto = null
            )
        }
    }

    fun retakePhoto() {
        val file = _uiState.value.pendingPhoto
        if (file?.exists() == true) {
            file.delete()
        }
        _uiState.update { it.copy(pendingPhoto = null, isCameraOpen = true) }
    }

    fun cancelPhoto() {
        val file = _uiState.value.pendingPhoto
        if (file?.exists() == true) {
            file.delete()
        }
        _uiState.update { it.copy(pendingPhoto = null) }
    }

    fun deletePhoto(file: File) {
        if (file.exists()) {
            file.delete()
        }
        _uiState.update { it.copy(gallery = it.gallery.filter { f -> f.absolutePath != file.absolutePath }) }
    }
}
