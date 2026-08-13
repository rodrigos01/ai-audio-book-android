package com.rodrigos01.aiaudiobook.ui.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodrigos01.aiaudiobook.BuildConfig
import com.rodrigos01.aiaudiobook.common.media.MediaPlaybackService
import com.rodrigos01.aiaudiobook.common.media.PlaybackStatus
import com.rodrigos01.aiaudiobook.data.ApiRepository
import com.rodrigos01.aiaudiobook.data.FirestoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class PlayerUiState(
    val chapterName: String?,
    val titleName: String?,
    val playbackStatus: PlaybackStatus,
    val playbackProgress: Float,
    val position: Duration,
    val duration: Duration,
)

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModel(
    private val titleId: String,
    private val chapterId: String,
    private val playbackService: MediaPlaybackService,
    private val apiRepository: ApiRepository = ApiRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
) : ViewModel() {

    private val playbackState = playbackService.playerState()
    val uiState: StateFlow<PlayerUiState> = firestoreRepository.getTitle(titleId)
        .flatMapLatest { title -> firestoreRepository.getChapters(titleId).map { title to it } }
        .map { (title, chapters) -> title to chapters.first { it.id == chapterId } }
        .flatMapLatest { (title, chapter) ->
            firestoreRepository.getChapterTotalDurationSeconds(chapter.id).map { durationSec ->
                Triple(title, chapter, (durationSec * 1000).toLong())
            }
        }.onEach { (title, chapter, _) ->
            playbackService.setMedia(
                uri = "${BuildConfig.SERVER_URL}api/chapters/${chapter.id}/hls/playlist.m3u8".toUri(),
                title = chapter.name,
                artist = title?.name,
            )
        }.combine(playbackState) { (title, chapter, estimatedDurationMs), playerState ->
            val totalDurationMs = playerState.durationMillis
            val progress = if (totalDurationMs > 0) {
                (playerState.positionMillis.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            PlayerUiState(
                chapterName = chapter.name,
                titleName = title?.name,
                playbackStatus = playerState.status,
                playbackProgress = progress,
                position = playerState.positionMillis.milliseconds,
                duration = totalDurationMs.milliseconds,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            PlayerUiState("", "", PlaybackStatus.STOPPED, 0F, 0.milliseconds, 0.milliseconds)
        )

    fun onPlayPause() {
        when (uiState.value.playbackStatus) {
            PlaybackStatus.PLAYING -> playbackService.pause()
            PlaybackStatus.PAUSED -> playbackService.play()
            PlaybackStatus.STOPPED -> playbackService.play()
            else -> Unit
        }
    }

    fun onSeek(position: Float) {
        playbackService.setPosition(position)
    }

    fun onPrevious() {
        playbackService.seekBack()
    }

    fun onNext() {
        playbackService.seekForward()
    }

    fun onExit() {
        playbackService.stop()
    }

    companion object {
        fun Factory(titleId: String, chapterId: String, playbackService: MediaPlaybackService) =
            viewModelFactory {
                initializer {
                    PlayerViewModel(
                        titleId, chapterId, playbackService
                    )
                }
            }
    }
}