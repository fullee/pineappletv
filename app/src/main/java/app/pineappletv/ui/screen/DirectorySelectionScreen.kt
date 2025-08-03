package app.pineappletv.ui.screen

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import app.pineappletv.ui.viewmodel.DirectorySelectionViewModel
import org.koin.androidx.compose.koinViewModel
import java.io.File


@Composable
fun DirectorySelectionScreen(
    onDirectorySelected: () -> Unit,
    viewModel: DirectorySelectionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showDirectoryList by remember { mutableStateOf(false) }
    var commonDirectories by remember { mutableStateOf(emptyList<File>()) }
    
    LaunchedEffect(Unit) {
        // 获取常用目录
        val directories = mutableListOf<File>()
        
        // 外部存储
        val externalStorage = Environment.getExternalStorageDirectory()
        if (externalStorage.exists()) {
            directories.add(externalStorage)
        }
        
        // Movies目录
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        if (moviesDir.exists()) {
            directories.add(moviesDir)
        }
        
        // Downloads目录
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir.exists()) {
            directories.add(downloadsDir)
        }

        directories.add(File("/sdcard/DCIM/Camera"))
        
        commonDirectories = directories
    }
    
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onDirectorySelected()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "欢迎使用 PineappleTV",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "请选择视频文件夹来创建媒体库",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (uiState.isScanning) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在扫描视频文件...",
                style = MaterialTheme.typography.bodyMedium.copy(MaterialTheme.colorScheme.error)
            )
        } else {
            if (!showDirectoryList) {
                Button(
                    onClick = {
                        Toast.makeText(context, "选择文件夹", Toast.LENGTH_SHORT).show()
                        showDirectoryList = true },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("选择文件夹")
                }
            } else {
                Text(
                    text = "常用目录:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(commonDirectories) { directory ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectDirectory(directory.absolutePath)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = directory.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = directory.absolutePath,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
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