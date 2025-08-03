package app.pineappletv.ui.screen

import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.pineappletv.ui.viewmodel.FolderBrowserViewModel
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderBrowserScreen(
    onBackClick: () -> Unit,
    onFolderSelected: () -> Unit,
    viewModel: FolderBrowserViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        // 初始化为外部存储根目录
        val rootPath = Environment.getExternalStorageDirectory().absolutePath
        viewModel.navigateToFolder(rootPath)
    }
    
    // 监听选择完成状态
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onFolderSelected()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部导航栏
        TopAppBar(
            title = { 
                Text(
                    text = "选择文件夹",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                // 选择当前文件夹按钮
                IconButton(
                    onClick = { 
                        uiState.currentPath?.let { path ->
                            viewModel.selectFolder(path)
                        }
                    },
                    enabled = uiState.currentPath != null && !uiState.isLoading && uiState.scanProgress?.isScanning != true
                ) {
                    if (uiState.isLoading || uiState.scanProgress?.isScanning == true) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = "选择此文件夹")
                    }
                }
            }
        )
        
        // 当前路径显示
        uiState.currentPath?.let { path ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "当前路径: $path",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // 扫描进度显示
        uiState.scanProgress?.let { progress ->
            if (progress.isScanning) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "正在扫描视频文件...",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (progress.total > 0) {
                            LinearProgressIndicator(
                                progress = { progress.current.toFloat() / progress.total.toFloat() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${progress.current}/${progress.total}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = progress.currentItem,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.scanProgress?.isScanning != true) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 返回上级目录选项（除了根目录）
                uiState.currentPath?.let { currentPath ->
                    val parentFile = File(currentPath).parentFile
                    if (parentFile != null && parentFile.exists()) {
                        item {
                            FolderItem(
                                name = ".. (返回上级)",
                                path = parentFile.absolutePath,
                                isParent = true,
                                onClick = { viewModel.navigateToFolder(parentFile.absolutePath) }
                            )
                        }
                    }
                }
                
                // 子文件夹列表
                items(uiState.folders) { folder ->
                    FolderItem(
                        name = folder.name,
                        path = folder.absolutePath,
                        isParent = false,
                        onClick = { viewModel.navigateToFolder(folder.absolutePath) }
                    )
                }
                
                // 空文件夹提示
                if (uiState.folders.isEmpty() && !uiState.isLoading) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "此文件夹中没有子文件夹",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // 错误提示
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun FolderItem(
    name: String,
    path: String,
    isParent: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isParent) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = null,
                tint = if (isParent) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isParent) {
                    Text(
                        text = path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}