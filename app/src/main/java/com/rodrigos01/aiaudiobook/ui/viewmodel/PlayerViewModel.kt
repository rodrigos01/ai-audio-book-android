package com.rodrigos01.aiaudiobook.ui.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
    val positionString: String,
    val durationString: String,
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
        .onEach { (title, chapter) ->
            playbackService.setMedia(
                uri = "https://ai-audio-book-api-883622140264.us-central1.run.app/api/chapters/${chapter.id}/stream".toUri(),
                title = chapter.name,
                artist = title?.name,
            )
        }.combine(playbackState) { (title, chapter), playerState ->
            PlayerUiState(
                chapterName = chapter.name,
                titleName = title?.name,
                playbackStatus = playerState.status,
                playbackProgress = playerState.progress,
                positionString = playerState.positionMillis.milliseconds.toDurationString(),
                durationString = playerState.durationMillis.milliseconds.toDurationString(),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            PlayerUiState("", "", PlaybackStatus.STOPPED, 0F, "", "")
        )

    fun onPlayPause() {
        when (uiState.value.playbackStatus) {
            PlaybackStatus.PLAYING -> playbackService.pause()
            PlaybackStatus.PAUSED -> playbackService.play()
            else -> Unit
        }
    }

    fun onSeek(position: Float) {
        playbackService.setPosition(position)
    }

    fun Duration.toDurationString() = toComponents { hours, minutes, seconds, _ ->
        StringBuilder().apply {
            if (hours > 0) {
                append("${hours}:")
            }
            append("$minutes:${seconds.toString().padStart(2, '0')}")
        }.toString()
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