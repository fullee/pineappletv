package app.pineappletv.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import app.pineappletv.database.Videos
import app.pineappletv.ui.viewmodel.VideoListViewModel
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(
    collectionId: Long,
    onBackClick: () -> Unit,
    onVideoClick: (Long) -> Unit,
    viewModel: VideoListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(collectionId) {
        viewModel.loadVideos(collectionId)
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部导航栏
        TopAppBar(
            title = {
                Text(uiState.collection?.name ?: "视频列表")
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            }
        )
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("123")
                CircularProgressIndicator()
            }
        } else {
            val gridState = rememberLazyGridState()
            
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 200.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                items(uiState.videos.size) { index ->
                    val video = uiState.videos[index]
                    VideoCard(
                        video = video,
                        onClick = { onVideoClick(video.id) },
                        gridState = gridState,
                        index = index
                    )
                }
            }
        }
        
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
private fun VideoCard(
    video: Videos,
    onClick: () -> Unit,
    gridState: LazyGridState,
    index: Int
) {
    var isFocused by remember { mutableStateOf(false) }
    
    // 当卡片获得焦点时自动滚动
    LaunchedEffect(isFocused) {
        if (isFocused) {
            gridState.animateScrollToItem(index)
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .clickable { onClick() },
        border = if (isFocused) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        elevation = if (isFocused) {
            CardDefaults.cardElevation(defaultElevation = 8.dp)
        } else {
            CardDefaults.cardElevation()
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = video.cover_image,
                contentDescription = video.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = video.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (video.duration!! > 0) {
                Text(
                    text = formatDuration(video.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}