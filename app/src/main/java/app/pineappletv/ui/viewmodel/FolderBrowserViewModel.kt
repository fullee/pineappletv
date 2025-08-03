package app.pineappletv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pineappletv.data.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FolderBrowserViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FolderBrowserUiState())
    val uiState: StateFlow<FolderBrowserUiState> = _uiState.asStateFlow()
    
    fun navigateToFolder(path: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val folders = loadFoldersInPath(path)
                _uiState.value = _uiState.value.copy(
                    currentPath = path,
                    folders = folders,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "无法访问此文件夹"
                )
            }
        }
    }
    
    fun selectFolder(path: String) {
        viewModelScope.launch {
            try {
                // 扫描并保存选中的文件夹作为合集
                videoRepository.scanAndSaveCollections(path)
                _uiState.value = _uiState.value.copy(
                    isCompleted = true,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "创建合集失败"
                )
            }
        }
    }
    
    private suspend fun loadFoldersInPath(path: String): List<File> = withContext(Dispatchers.IO) {
        try {
            val directory = File(path)
            if (!directory.exists() || !directory.isDirectory) {
                return@withContext emptyList()
            }
            
            // 获取所有子文件夹，过滤掉隐藏文件夹和系统文件夹
            val folders = directory.listFiles { file ->
                file.isDirectory && 
                !file.isHidden && 
                file.canRead() &&
                !isSystemFolder(file.name)
            }?.toList() ?: emptyList()
            
            // 按名称排序
            folders.sortedBy { it.name.lowercase() }
        } catch (e: Exception) {
            throw Exception("无法读取文件夹内容: ${e.message}")
        }
    }
    
    private fun isSystemFolder(folderName: String): Boolean {
        val systemFolders = setOf(
            "Android", "android", "system", "System", "proc", "dev", 
            "sys", "cache", "lost+found", ".android_secure", ".thumbnails"
        )
        return systemFolders.contains(folderName) || folderName.startsWith(".")
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class FolderBrowserUiState(
    val currentPath: String? = null,
    val folders: List<File> = emptyList(),
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)