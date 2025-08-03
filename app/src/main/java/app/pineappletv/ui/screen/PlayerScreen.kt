package app.pineappletv.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.pineappletv.ui.viewmodel.PlayerViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayerScreen(
    videoId: Long,
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    
    LaunchedEffect(videoId) {
        viewModel.loadVideo(videoId)
    }
    
    LaunchedEffect(uiState.currentVideo) {
        uiState.currentVideo?.let { video ->
            val mediaItem = MediaItem.fromUri(Uri.parse(video.file_path))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            
            // 恢复播放位置
            if (uiState.lastPosition > 0) {
                exoPlayer.seekTo(uiState.lastPosition)
            }
            
            exoPlayer.play()
            isPlaying = true
            
            // 加载播放列表
            viewModel.loadPlaylistVideos(video.collection_id)
        }
    }
    
    // 定期保存播放进度
    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            isPlaying = exoPlayer.isPlaying
            
            if (currentPosition > 0 && duration > 0) {
                viewModel.savePlaybackProgress(currentPosition, duration)
            }
            
            kotlinx.coroutines.delay(1000) // 每秒更新一次
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ExoPlayer视图
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // 使用自定义控制器
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // 点击视频区域显示/隐藏控制器（不包括控制器本身）
        if (!showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        showControls = true
                    }
            )
        } else {
            // 点击视频播放区域（中间部分）隐藏控制器
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, bottom = 200.dp) // 避开顶部和底部控制器区域
                    .clickable {
                        showControls = false
                    }
            )
        }
        
        // 自定义控制器
        if (showControls) {
            PlayerControls(
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                videoName = uiState.currentVideo?.name ?: "",
                autoPlayNext = uiState.autoPlayNext,
                hasNext = uiState.playlistVideos.let { playlist ->
                    val currentIndex = playlist.indexOfFirst { it.id == uiState.currentVideo?.id }
                    currentIndex >= 0 && currentIndex < playlist.size - 1
                },
                hasPrevious = uiState.playlistVideos.let { playlist ->
                    val currentIndex = playlist.indexOfFirst { it.id == uiState.currentVideo?.id }
                    currentIndex > 0
                },
                onPlayPause = {
                    if (isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                },
                onSeek = { position ->
                    exoPlayer.seekTo(position)
                },
                onNext = {
                    viewModel.playNext()
                },
                onPrevious = {
                    viewModel.playPrevious()
                },
                onBack = onBackClick,
                onAutoPlayToggle = { enabled ->
                    viewModel.setAutoPlayNext(enabled)
                }
            )
        }
    }
    
    // 监听播放完成事件
    LaunchedEffect(exoPlayer) {
        // 这里需要监听ExoPlayer的播放完成事件
        // 如果启用了自动播放下一集，则自动播放下一个视频
        // 否则返回到列表界面
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    videoName: String,
    autoPlayNext: Boolean,
    hasNext: Boolean,
    hasPrevious: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onBack: () -> Unit,
    onAutoPlayToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部控制栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
            
            Text(
                text = videoName,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            )
            
            IconButton(onClick = { /* 设置 */ }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 底部控制栏
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .padding(16.dp)
        ) {
            // 进度条
            Slider(
                value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                onValueChange = { progress ->
                    val position = (progress * duration).toLong()
                    onSeek(position)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 时间显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = formatTime(duration),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 播放控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPrevious,
                    enabled = hasPrevious
                ) {
                    Text("上一个", color = if (hasPrevious) Color.White else Color.Gray)
                }
                
                Button(onClick = onPlayPause) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        tint = Color.White
                    )
                }
                
                Button(
                    onClick = onNext,
                    enabled = hasNext
                ) {
                    Text("下一个", color = if (hasNext) Color.White else Color.Gray)
                }
            }
            
            // 自动播放下一集开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = autoPlayNext,
                    onCheckedChange = onAutoPlayToggle
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "自动播放下一集",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}