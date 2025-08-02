package app.pineappletv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pineappletv.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DirectorySelectionViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DirectorySelectionUiState())
    val uiState: StateFlow<DirectorySelectionUiState> = _uiState.asStateFlow()
    
    fun selectDirectory(directoryPath: String) {
        _uiState.value = _uiState.value.copy(
            selectedDirectory = directoryPath,
            isScanning = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                videoRepository.scanAndSaveCollections(directoryPath)
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    isCompleted = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message ?: "扫描失败"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class DirectorySelectionUiState(
    val selectedDirectory: String? = null,
    val isScanning: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)