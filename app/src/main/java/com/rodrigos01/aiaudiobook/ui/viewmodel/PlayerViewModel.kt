package com.rodrigos01.aiaudiobook.ui.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodrigos01.aiaudiobook.BuildConfig
import com.rodrigos01.aiaudiobook.common.media.MediaPlaybackService
import com.rodrigos01.aiaudiobook.common.media.PlaybackStatus
import com.rodrigos01.aiaudiobook.data.ApiRepository
import com.rodrigos01.aiaudiobook.data.Chapter
import com.rodrigos01.aiaudiobook.data.FirestoreRepository
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.data.offline.OfflineDownloadRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class PlayerUiState(
    val chapterName: String?,
    val titleName: String?,
    val playbackStatus: PlaybackStatus,
    val playbackProgress: Float,
    val position: Duration,
    val duration: Duration,
    val isOffline: Boolean = false,
)

private data class ChapterPlaybackInfo(
    val title: Title?,
    val chapter: Chapter,
    val estimatedDurationMs: Long,
    val isOffline: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModel(
    private val titleId: String,
    private val chapterId: String,
    private val playbackService: MediaPlaybackService,
    private val offlineDownloadRepository: OfflineDownloadRepository,
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
        }.map { (title, chapter, estimatedDurationMs) ->
            // Prefer a downloaded local file so playback works fully offline; fall back to the
            // live HLS stream otherwise.
            val localFilePath = offlineDownloadRepository.localFilePath(chapter.id)
            val uri: Uri = localFilePath?.let { Uri.fromFile(File(it)) }
                ?: "${BuildConfig.SERVER_URL}api/chapters/${chapter.id}/hls/playlist.m3u8".toUri()

            playbackService.setMedia(
                uri = uri,
                title = chapter.name,
                artist = title?.name,
            )

            ChapterPlaybackInfo(title, chapter, estimatedDurationMs, isOffline = localFilePath != null)
        }.combine(playbackState) { info, playerState ->
            val totalDurationMs = playerState.durationMillis
            val progress = if (totalDurationMs > 0) {
                (playerState.positionMillis.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            PlayerUiState(
                chapterName = info.chapter.name,
                titleName = info.title?.name,
                playbackStatus = playerState.status,
                playbackProgress = progress,
                position = playerState.positionMillis.milliseconds,
                duration = totalDurationMs.milliseconds,
                isOffline = info.isOffline,
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
        fun Factory(
            titleId: String,
            chapterId: String,
            playbackService: MediaPlaybackService,
            offlineDownloadRepository: OfflineDownloadRepository,
        ) = viewModelFactory {
            initializer {
                PlayerViewModel(
                    titleId, chapterId, playbackService, offlineDownloadRepository
                )
            }
        }
    }
}
