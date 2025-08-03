package app.pineappletv.data.scanner

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import app.pineappletv.data.model.CollectionInfo
import app.pineappletv.data.model.VideoFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class VideoScanner(private val context: Context) {
    
    private val videoExtensions = setOf(
        "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v", "3gp", "ts", "m2ts"
    )
    
    suspend fun scanDirectory(
        directoryPath: String,
        onProgress: ((current: Int, total: Int, currentItem: String) -> Unit)? = null
    ): List<CollectionInfo> = withContext(Dispatchers.IO) {
        val rootDir = File(directoryPath)
        if (!rootDir.exists() || !rootDir.isDirectory) {
            return@withContext emptyList()
        }
        
        val collections = mutableListOf<CollectionInfo>()
        Log.d("VideoScanner", "Scanning directory: $directoryPath")
        
        // 收集所有需要扫描的目录
        val dirsToScan = mutableListOf<File>()
        rootDir.listFiles { file -> file.isDirectory }?.let { dirsToScan.addAll(it) }
        
        // 如果根目录包含视频文件，也加入扫描列表
        if (hasVideoFiles(rootDir)) {
            dirsToScan.add(0, rootDir) // 添加到开头
        }
        
        var processedCount = 0
        val totalDirs = dirsToScan.size
        
        // 扫描所有目录
        dirsToScan.forEach { dir ->
            onProgress?.invoke(processedCount, totalDirs, "扫描文件夹: ${dir.name}")
            
            val videos = scanVideosInDirectory(dir) { current, total, videoName ->
                onProgress?.invoke(processedCount, totalDirs, "处理视频: $videoName ($current/$total)")
            }
            
            if (videos.isNotEmpty()) {
                collections.add(
                    CollectionInfo(
                        name = if (dir == rootDir) rootDir.name else dir.name,
                        path = dir.absolutePath,
                        videos = videos
                    )
                )
            }
            
            processedCount++
        }
        
        collections
    }
    
    private fun hasVideoFiles(directory: File): Boolean {
        return directory.listFiles()?.any { file ->
            file.isFile && isVideoFile(file)
        } ?: false
    }
    
    private suspend fun scanVideosInDirectory(
        directory: File,
        onVideoProgress: ((current: Int, total: Int, videoName: String) -> Unit)? = null
    ): List<VideoFile> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoFile>()
        
        val videoFiles = directory.listFiles { file ->
            file.isFile && isVideoFile(file)
        } ?: emptyArray()
        
        val totalVideos = videoFiles.size
        var processedVideos = 0
        
        videoFiles.forEach { videoFile ->
            onVideoProgress?.invoke(processedVideos + 1, totalVideos, videoFile.nameWithoutExtension)
            
            val duration = getVideoDuration(videoFile)
            val thumbnailPath = generateVideoThumbnail(videoFile.absolutePath)
            
            videos.add(
                VideoFile(
                    name = videoFile.nameWithoutExtension,
                    path = videoFile.absolutePath,
                    size = videoFile.length(),
                    duration = duration,
                    thumbnailPath = thumbnailPath
                )
            )
            
            processedVideos++
        }
        
        videos.sortedBy { it.name }
    }
    
    private fun isVideoFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in videoExtensions
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
    
    private suspend fun generateVideoThumbnail(videoPath: String): String? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val bitmap = retriever.getFrameAtTime(1000000) // 1秒处的帧
            retriever.release()
            
            if (bitmap != null) {
                // 创建缩略图目录
                val thumbnailDir = File(context.filesDir, "thumbnails")
                if (!thumbnailDir.exists()) {
                    thumbnailDir.mkdirs()
                }
                
                // 生成缩略图文件名
                val videoFile = File(videoPath)
                val thumbnailFile = File(thumbnailDir, "${videoFile.nameWithoutExtension}_thumb.jpg")
                
                // 保存缩略图
                FileOutputStream(thumbnailFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                bitmap.recycle()
                
                return@withContext thumbnailFile.absolutePath
            }
            
            null
        } catch (e: Exception) {
            Log.e("VideoScanner", "Failed to generate thumbnail for $videoPath", e)
            null
        }
    }
}