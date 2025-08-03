package app.pineappletv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pineappletv.data.repository.VideoRepository
import app.pineappletv.database.Collections
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CollectionManagementViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CollectionManagementUiState())
    val uiState: StateFlow<CollectionManagementUiState> = _uiState.asStateFlow()
    
    fun loadCollections() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                videoRepository.getAllCollections().collect { collections ->
                    _uiState.value = _uiState.value.copy(
                        collections = collections,
                        isLoading = false,
                        error = null
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
    
    fun deleteCollection(collectionId: Long) {
        viewModelScope.launch {
            try {
                videoRepository.deleteCollection(collectionId)
                // 重新加载列表
                loadCollections()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "删除失败"
                )
            }
        }
    }
    
    fun refreshCollection(collectionId: Long) {
        viewModelScope.launch {
            try {
                val collection = videoRepository.getCollectionById(collectionId)
                if (collection != null) {
                    // 重新扫描该合集的目录
                    videoRepository.refreshCollection(collection.path)
                    // 重新加载列表
                    loadCollections()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "重新索引失败"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class CollectionManagementUiState(
    val collections: List<Collections> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)