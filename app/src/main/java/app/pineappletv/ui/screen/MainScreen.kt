package app.pineappletv.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import app.pineappletv.database.Collections
import app.pineappletv.database.GetRecentPlaybackHistory
import app.pineappletv.ui.viewmodel.MainViewModel
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel


@Composable
fun MainScreen(
    onCollectionClick: (Long) -> Unit,
    onVideoClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onDirectorySelectionClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("加载中...")
            }
        }
    } else if (uiState.collections.isEmpty()) {
        // 没有合集数据时显示引导界面
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "欢迎使用 PineappleTV",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "还没有视频合集，请先选择视频文件夹",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                Button(
                    onClick = onDirectorySelectionClick
                ) {
                    Text("选择视频文件夹")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题栏
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PineappleTV",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = onSearchClick) {
                            Text("搜索")
                        }
                        Button(onClick = onSettingsClick) {
                            Text("设置")
                        }
                    }
                }
            }
            
            // 最近播放
            if (uiState.recentPlayback.isNotEmpty()) {
                item {
                    Text(
                        text = "最近播放",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                item {
                    val recentPlaybackListState = rememberLazyListState()
                    
                    LazyRow(
                        state = recentPlaybackListState,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.height(280.dp) // 固定高度避免测量问题
                    ) {
                        items(uiState.recentPlayback.size) { index ->
                            val playback = uiState.recentPlayback[index]
                            RecentPlaybackCard(
                                playback = playback,
                                onClick = { onVideoClick(playback.video_id) },
                                listState = recentPlaybackListState,
                                index = index
                            )
                        }
                    }
                }
            }
            
            // 合集列表标题
            item {
                Text(
                    text = "媒体库",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            // 合集网格 - 使用固定高度
            item {
                val rows = (uiState.collections.size + 1) / 2
                val gridHeight = (rows * 200).dp // 估算网格高度
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.height(gridHeight),
                    userScrollEnabled = false // 禁用内部滚动，使用外部LazyColumn滚动
                ) {
                    items(uiState.collections.size) { index ->
                        val collection = uiState.collections[index]
                        CollectionCard(
                            collection = collection,
                            onClick = { onCollectionClick(collection.id) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun RecentPlaybackCard(
    playback: GetRecentPlaybackHistory,
    onClick: () -> Unit,
    listState: LazyListState,
    index: Int
) {
    var isFocused by remember { mutableStateOf(false) }
    
    // 当卡片获得焦点时自动滚动
    LaunchedEffect(isFocused) {
        if (isFocused) {
            // 滚动最近播放列表到当前项
            listState.animateScrollToItem(index)
        }
    }
    
    Card(
        modifier = Modifier
            .width(300.dp)
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
                model = playback.cover_image,
                contentDescription = playback.video_name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(169.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = playback.video_name ?: "",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = playback.collection_name ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // 播放进度条
            val progress = if (playback.duration > 0) {
                playback.position.toFloat() / playback.duration.toFloat()
            } else {
                0f
            }
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}


@Composable
private fun CollectionCard(
    collection: Collections,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
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
                model = collection.cover_image,
                contentDescription = collection.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = collection.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}