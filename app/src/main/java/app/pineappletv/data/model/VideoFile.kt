package app.pineappletv.data.model

data class VideoFile(
    val name: String,
    val path: String,
    val size: Long,
    val duration: Long = 0L
)

data class CollectionInfo(
    val name: String,
    val path: String,
    val videos: List<VideoFile>
)