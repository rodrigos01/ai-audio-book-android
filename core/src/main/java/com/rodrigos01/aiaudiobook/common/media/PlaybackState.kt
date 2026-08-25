package com.rodrigos01.aiaudiobook.common.media

data class PlaybackState(
    val status: PlaybackStatus,
    val progress: Float,
    val positionMillis: Long,
    val durationMillis: Long,
)

enum class PlaybackStatus {
    PLAYING,
    PAUSED,
    STOPPED,
    BUFFERING,
    ERROR
}
