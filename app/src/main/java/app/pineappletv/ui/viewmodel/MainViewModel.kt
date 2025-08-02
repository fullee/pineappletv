package app.pineappletv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pineappletv.data.repository.VideoRepository
import app.pineappletv.database.Collections
import app.pineappletv.database.GetRecentPlaybackHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            combine(
                videoRepository.getAllCollections(),
                videoRepository.getRecentPlaybackHistory(10)
            ) { collections, recentPlayback ->
                _uiState.value = _uiState.value.copy(
                    collections = collections,
                    recentPlayback = recentPlayback,
                    isLoading = false
                )
            }
        }
    }
    
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadData()
    }
}

data class MainUiState(
    val collections: List<Collections> = emptyList(),
    val recentPlayback: List<GetRecentPlaybackHistory> = emptyList(),
    val isLoading: Boolean = true
)