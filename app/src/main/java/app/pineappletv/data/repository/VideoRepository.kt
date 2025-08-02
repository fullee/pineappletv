package app.pineappletv.data.repository

import android.util.Log
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.pineappletv.data.database.DatabaseManager
import app.pineappletv.data.model.CollectionInfo
import app.pineappletv.data.scanner.VideoScanner
import app.pineappletv.database.Collections
import app.pineappletv.database.GetRecentPlaybackHistory
import app.pineappletv.database.PlaybackHistory
import app.pineappletv.database.SearchVideos
import app.pineappletv.database.Videos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VideoRepository(
    private val databaseManager: DatabaseManager,
    private val videoScanner: VideoScanner
) {
    
    private val database = databaseManager.database
    
    // 合集相关操作
    suspend fun scanAndSaveCollections(directoryPath: String) = withContext(Dispatchers.IO) {
        val collections = videoScanner.scanDirectory(directoryPath)
        Log.d("VideoRepository", "Found ${collections.size} collections")
        collections.forEach { collectionInfo ->
            // 插入合集
            val currentTime = System.currentTimeMillis()
            database.collectionsQueries.insertCollection(
                name = collectionInfo.name,
                path = collectionInfo.path,
                cover_image = null,
                created_at = currentTime,
                updated_at = currentTime
            )
            
            // 获取刚插入的合集ID
            val collection = database.collectionsQueries.getCollectionByPath(collectionInfo.path).executeAsOne()
            
            // 插入视频
            collectionInfo.videos.forEach { video ->
                database.videosQueries.insertVideo(
                    collection_id = collection.id,
                    name = video.name,
                    file_path = video.path,
                    cover_image = null,
                    duration = video.duration,
                    file_size = video.size,
                    created_at = currentTime,
                    updated_at = currentTime
                )
            }
        }
    }
    
    fun getAllCollections(): Flow<List<Collections>> {
        return database.collectionsQueries.getAllCollections()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    suspend fun getCollectionById(id: Long): Collections? = withContext(Dispatchers.IO) {
        try {
            database.collectionsQueries.getCollectionById(id).executeAsOne()
        } catch (e: Exception) {
            null
        }
    }
    
    // 视频相关操作
    fun getVideosByCollectionId(collectionId: Long): Flow<List<Videos>> {
        return database.videosQueries.getVideosByCollectionId(collectionId)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    suspend fun getVideoById(id: Long): Videos? = withContext(Dispatchers.IO) {
        try {
            database.videosQueries.getVideoById(id).executeAsOne()
        } catch (e: Exception) {
            null
        }
    }
    
    fun searchVideos(query: String): Flow<List<SearchVideos>> {
        return database.videosQueries.searchVideos(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    // 播放历史相关操作
    suspend fun savePlaybackHistory(videoId: Long, position: Long, duration: Long) = withContext(Dispatchers.IO) {
        database.playbackHistoryQueries.insertOrUpdatePlaybackHistory(
            video_id = videoId,
            position = position,
            duration = duration,
            last_played_at = System.currentTimeMillis()
        )
    }
    
    fun getRecentPlaybackHistory(limit: Long = 20): Flow<List<GetRecentPlaybackHistory>> {
        return database.playbackHistoryQueries.getRecentPlaybackHistory(limit)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    suspend fun getPlaybackHistoryByVideoId(videoId: Long): PlaybackHistory? = withContext(Dispatchers.IO) {
        try {
            database.playbackHistoryQueries.getPlaybackHistoryByVideoId(videoId).executeAsOne()
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun deletePlaybackHistory(videoId: Long) = withContext(Dispatchers.IO) {
        database.playbackHistoryQueries.deletePlaybackHistory(videoId)
    }
}