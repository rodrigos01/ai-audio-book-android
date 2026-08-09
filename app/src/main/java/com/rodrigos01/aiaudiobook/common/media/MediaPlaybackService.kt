package com.rodrigos01.aiaudiobook.common.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MediaPlaybackService(context: Context) {
    private val player = ExoPlayer.Builder(context).build()

    fun playerState() = callbackFlow {
        player.listen {
            trySend(player.toState())
            launch {
                while (player.isPlaying && player.availableDuration > player.currentPosition) {
                    trySend(player.toState())
                    delay(1000)
                }
            }
        }
    }

    fun setMedia(uri: Uri, mimeType: String = MimeTypes.AUDIO_MPEG, playWhenReady: Boolean = true) {
        player.playWhenReady = playWhenReady
        player.setMediaItem(
            MediaItem.Builder().setUri(uri).setMimeType(mimeType).build()
        )
        player.prepare()
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun setPosition(position: Float) {
        player.seekTo((player.availableDuration * position).toLong())
    }

    fun stop() {
        player.stop()
    }

    fun release() {
        player.release()
    }

    private val Player.availableDuration: Long
        get() = duration.takeIf { it > 0 } ?: (currentPosition + totalBufferedDuration)

    private fun Player.toState() = PlaybackState(
        status = when {
            isPlaying -> PlaybackStatus.PLAYING
            playbackState == Player.STATE_READY -> PlaybackStatus.PAUSED
            isLoading -> PlaybackStatus.BUFFERING
            playerError != null -> PlaybackStatus.ERROR
            else -> PlaybackStatus.STOPPED
        },
        progress = currentPosition.toFloat() / availableDuration.toFloat(),
        positionMillis = currentPosition,
        durationMillis = availableDuration
    )
}