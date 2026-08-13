package com.rodrigos01.aiaudiobook.common.media

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MediaPlaybackService(context: Context) {
    private val appContext = context.applicationContext
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val controllerState = MutableStateFlow<MediaController?>(null)

    private var currentMediaUri: Uri? = null

    init {
        val sessionToken =
            SessionToken(appContext, ComponentName(appContext, AudioPlaybackService::class.java))
        val future = MediaController.Builder(appContext, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener({
                               try {
                                   val mediaController = future.get()
                                   controller = mediaController
                                   controllerState.value = mediaController
                               } catch (e: Exception) {
                                   e.printStackTrace()
                               }
                           }, ContextCompat.getMainExecutor(appContext))
    }

    fun playerState(): Flow<PlaybackState> = callbackFlow {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                trySend(player.toState())
            }
        }

        var activePlayer: Player? = null

        val job = scope.launch {
            controllerState.collect { mediaController ->
                activePlayer?.removeListener(listener)
                activePlayer = mediaController
                if (mediaController != null) {
                    mediaController.addListener(listener)
                    trySend(mediaController.toState())

                    while (isActive) {
                        if (mediaController.isPlaying && mediaController.duration > mediaController.currentPosition) {
                            trySend(mediaController.toState())
                        }
                        delay(1000)
                    }
                } else {
                    trySend(PlaybackState(PlaybackStatus.STOPPED, 0f, 0L, 0L))
                }
            }
        }

        awaitClose {
            job.cancel()
            activePlayer?.removeListener(listener)
        }
    }

    fun setMedia(
        uri: Uri, title: String? = null, artist: String? = null, playWhenReady: Boolean = true
    ) {
        val ctrl = controller
        if (currentMediaUri == uri && ctrl?.playbackState != Player.STATE_ENDED) return
        currentMediaUri = uri

        val metadata = MediaMetadata.Builder().setTitle(title).setArtist(artist).build()

        val mediaItem = MediaItem.Builder().setUri(uri).setMediaMetadata(metadata).build()

        val executeSetMedia = { targetCtrl: MediaController ->
            targetCtrl.playWhenReady = playWhenReady
            targetCtrl.setMediaItem(mediaItem)
            targetCtrl.prepare()
        }

        controller?.let {
            executeSetMedia(it)
        } ?: scope.launch {
            val targetCtrl = controllerState.filterNotNull().first()
            executeSetMedia(targetCtrl)
        }
    }

    fun play() {
        controller?.let { ctrl ->
            if (ctrl.playbackState == Player.STATE_ENDED) {
                ctrl.seekTo(0)
            }
            ctrl.play()
        }
    }

    fun pause() {
        controller?.pause()
    }

    fun setPosition(position: Float) {
        controller?.let { ctrl ->
            if (ctrl.duration > 0) {
                ctrl.seekTo((ctrl.duration * position).toLong())
            }
        }
    }

    fun seekForward() {
        controller?.seekForward()
    }

    fun seekBack() {
        controller?.seekBack()
    }

    fun stop() {
        controller?.stop()
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        controllerState.value = null
    }


    private fun Player.toState(): PlaybackState {
        val isEnded = playbackState == Player.STATE_ENDED
        val position = if (isEnded && duration > 0) duration else currentPosition
        val progress =
            if (isEnded) 1f else if (duration > 0) position.toFloat() / duration.toFloat() else 0f

        return PlaybackState(
            status = when {
                isPlaying -> PlaybackStatus.PLAYING
                playbackState == Player.STATE_READY -> PlaybackStatus.PAUSED
                isLoading || playbackState == Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
                playerError != null -> PlaybackStatus.ERROR
                else -> PlaybackStatus.STOPPED
            }, progress = progress, positionMillis = position, durationMillis = duration
        )
    }
}