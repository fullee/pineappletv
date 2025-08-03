package app.pineappletv.data.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import app.pineappletv.data.model.CollectionInfo
import app.pineappletv.data.model.VideoFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VideoScanner(private val context: Context) {
    
    private val videoExtensions = setOf(
        "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v", "3gp", "ts", "m2ts"
    )
    
    suspend fun scanDirectory(directoryPath: String): List<CollectionInfo> = withContext(Dispatchers.IO) {
        val rootDir = File(directoryPath)
        if (!rootDir.exists() || !rootDir.isDirectory) {
            return@withContext emptyList()
        }
        
        val collections = mutableListOf<CollectionInfo>()
        Log.d("VideoScanner", "Scanning directory: $directoryPath")
        // 扫描根目录下的所有子文件夹
        rootDir.listFiles { file -> file.isDirectory }?.forEach { collectionDir ->
            val videos = scanVideosInDirectory(collectionDir)
            if (videos.isNotEmpty()) {
                collections.add(
                    CollectionInfo(
                        name = collectionDir.name,
                        path = collectionDir.absolutePath,
                        videos = videos
                    )
                )
            }
        }
        
        // 如果根目录直接包含视频文件，创建一个默认合集
        val rootVideos = scanVideosInDirectory(rootDir)
        Log.d("VideoScanner", "Root videos: $rootVideos")
        if (rootVideos.isNotEmpty()) {
            collections.add(
                CollectionInfo(
                    name = rootDir.name,
                    path = rootDir.absolutePath,
                    videos = rootVideos
                )
            )
        }
        
        collections
    }
    
    private suspend fun scanVideosInDirectory(directory: File): List<VideoFile> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoFile>()
        
        directory.listFiles{ file ->
            file.isFile && isVideoFile(file.name)
        }?.forEach { videoFile ->
            val duration = getVideoDuration(videoFile)
            videos.add(
                VideoFile(
                    name = videoFile.nameWithoutExtension,
                    path = videoFile.absolutePath,
                    size = videoFile.length(),
                    duration = duration
                )
            )
        }
        
        videos.sortedBy { it.name }
    }
    
    private fun isVideoFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in videoExtensions
    }
    
    private suspend fun getVideoDuration(videoFile: File): Long = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoFile.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()
            duration
        } catch (e: Exception) {
            0L
        }
    }
    
    suspend fun generateVideoThumbnail(videoPath: String): String? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val bitmap = retriever.getFrameAtTime(1000000) // 1秒处的帧
            retriever.release()
            
            // 这里可以将bitmap保存到内部存储并返回路径
            // 为简化，暂时返回null
            null
        } catch (e: Exception) {
            null
        }
    }
}