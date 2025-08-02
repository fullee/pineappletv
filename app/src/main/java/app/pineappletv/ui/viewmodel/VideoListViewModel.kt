package app.pineappletv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pineappletv.data.repository.VideoRepository
import app.pineappletv.database.Collections
import app.pineappletv.database.Videos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoListViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VideoListUiState())
    val uiState: StateFlow<VideoListUiState> = _uiState.asStateFlow()
    
    fun loadVideos(collectionId: Long) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val collection = videoRepository.getCollectionById(collectionId)
                videoRepository.getVideosByCollectionId(collectionId).collect { videos ->
                    _uiState.value = _uiState.value.copy(
                        collection = collection,
                        videos = videos,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class VideoListUiState(
    val collection: Collections? = null,
    val videos: List<Videos> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)