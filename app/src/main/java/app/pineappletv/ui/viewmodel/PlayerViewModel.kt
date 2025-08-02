package app.pineappletv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pineappletv.data.repository.VideoRepository
import app.pineappletv.database.Videos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    
    fun loadVideo(videoId: Long) {
        viewModelScope.launch {
            try {
                val video = videoRepository.getVideoById(videoId)
                val playbackHistory = videoRepository.getPlaybackHistoryByVideoId(videoId)
                
                _uiState.value = _uiState.value.copy(
                    currentVideo = video,
                    lastPosition = playbackHistory?.position ?: 0L,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载视频失败"
                )
            }
        }
    }
    
    fun savePlaybackProgress(position: Long, duration: Long) {
        val video = _uiState.value.currentVideo ?: return
        
        viewModelScope.launch {
            try {
                videoRepository.savePlaybackHistory(video.id, position, duration)
            } catch (e: Exception) {
                // 静默处理保存失败
            }
        }
    }
    
    fun loadPlaylistVideos(collectionId: Long) {
        viewModelScope.launch {
            try {
                videoRepository.getVideosByCollectionId(collectionId).collect { videos ->
                    _uiState.value = _uiState.value.copy(
                        playlistVideos = videos
                    )
                }
            } catch (e: Exception) {
                // 静默处理加载失败
            }
        }
    }
    
    fun playNext() {
        val current = _uiState.value.currentVideo ?: return
        val playlist = _uiState.value.playlistVideos
        val currentIndex = playlist.indexOfFirst { it.id == current.id }
        
        if (currentIndex >= 0 && currentIndex < playlist.size - 1) {
            val nextVideo = playlist[currentIndex + 1]
            loadVideo(nextVideo.id)
        }
    }
    
    fun playPrevious() {
        val current = _uiState.value.currentVideo ?: return
        val playlist = _uiState.value.playlistVideos
        val currentIndex = playlist.indexOfFirst { it.id == current.id }
        
        if (currentIndex > 0) {
            val previousVideo = playlist[currentIndex - 1]
            loadVideo(previousVideo.id)
        }
    }
    
    fun setAutoPlayNext(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoPlayNext = enabled)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class PlayerUiState(
    val currentVideo: Videos? = null,
    val playlistVideos: List<Videos> = emptyList(),
    val lastPosition: Long = 0L,
    val autoPlayNext: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)