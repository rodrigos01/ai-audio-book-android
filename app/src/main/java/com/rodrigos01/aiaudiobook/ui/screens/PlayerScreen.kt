package com.rodrigos01.aiaudiobook.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.rodrigos01.aiaudiobook.common.media.PlaybackStatus
import com.rodrigos01.aiaudiobook.theme.AIAudioBookTheme
import com.rodrigos01.aiaudiobook.ui.toDurationString
import com.rodrigos01.aiaudiobook.ui.viewmodel.PlayerUiState
import com.rodrigos01.aiaudiobook.ui.viewmodel.PlayerViewModel
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    PlayerScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onPlayPauseClick = playerViewModel::onPlayPause,
        onSeek = playerViewModel::onSeek,
        onPreviousClick = playerViewModel::onPrevious,
        onNextClick = playerViewModel::onNext,
        modifier = modifier
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    uiState: PlayerUiState,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    uiState.chapterName.orEmpty(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    uiState.titleName.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiState.isOffline) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DownloadDone,
                            contentDescription = "Playing offline",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Playing offline",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                var sliderPosition by remember(uiState.playbackProgress) {
                    mutableFloatStateOf(
                        uiState.playbackProgress
                    )
                }
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val progressOffset = constraints.maxWidth * sliderPosition
                    var isDragging by remember { mutableStateOf(false) }
                    var dragOffset by remember { mutableFloatStateOf(0F) }
                    val anchorSize by animateDpAsState(if (isDragging) 16.dp else 8.dp)
                    LinearWavyProgressIndicator(
                        progress = { if (isDragging) dragOffset / constraints.maxWidth else sliderPosition },
                        modifier = Modifier.fillMaxWidth(),
                        amplitude = {
                            when (uiState.playbackStatus) {
                                PlaybackStatus.PLAYING -> 1F
                                else -> 0F
                            }
                        },
                    )
                    Box(
                        modifier = Modifier
                            .size(anchorSize)
                            .offset {
                                IntOffset(
                                    x = (if (isDragging) dragOffset else progressOffset).roundToInt(),
                                    y = 0,
                                )
                            }
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape,
                            )
                            .draggable(
                                state = rememberDraggableState { delta ->
                                    dragOffset += delta
                                },
                                orientation = Orientation.Horizontal,
                                onDragStarted = {
                                    isDragging = true
                                    dragOffset = progressOffset
                                },
                                onDragStopped = {
                                    isDragging = false
                                    sliderPosition = dragOffset / constraints.maxWidth
                                    onSeek(sliderPosition)
                                }
                            )

                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val positionString = if (sliderPosition > 0) {
                        uiState.duration * sliderPosition.toDouble()
                    } else {
                        uiState.position
                    }.toDurationString()
                    Text(
                        positionString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        uiState.duration.toDurationString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onPreviousClick) {
                        Icon(
                            Icons.Default.SkipPrevious, contentDescription = "Previous",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    when (uiState.playbackStatus) {
                        PlaybackStatus.PAUSED -> PlayPauseButton(
                            Icons.Default.PlayArrow, onPlayPauseClick
                        )

                        PlaybackStatus.BUFFERING -> CircularProgressIndicator(modifier = Modifier.size(72.dp))
                        else -> PlayPauseButton(Icons.Default.Pause, onPlayPauseClick)
                    }
                    IconButton(onClick = onNextClick) {
                        Icon(
                            Icons.Default.SkipNext, contentDescription = "Previous",
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayPauseButton(imageVector: ImageVector, onPlayPauseClick: () -> Unit) {
    FilledIconButton(
        onClick = onPlayPauseClick, modifier = Modifier.size(72.dp)
    ) {
        Icon(imageVector, contentDescription = "Play/Pause", modifier = Modifier.size(32.dp))
    }
}

@Composable
@PreviewLightDark
fun PlayerScreenPreview() {
    AIAudioBookTheme {
        PlayerScreen(
            PlayerUiState(
                "Chapter 1",
                "A Book",
                PlaybackStatus.PLAYING,
                0.3F,
                43.seconds,
                3.minutes + 43.seconds
            ),
            onBackClick = {},
            onPlayPauseClick = {},
            onSeek = {},
            onPreviousClick = {},
            onNextClick = {}
        )
    }
}

@Composable
@PreviewLightDark
fun PlayerScreenPausedPreview() {
    AIAudioBookTheme {
        PlayerScreen(
            PlayerUiState(
                "Chapter 2: The Journey",
                "Great Adventure",
                PlaybackStatus.PAUSED,
                0.6F,
                2.minutes + 15.seconds,
                4.minutes
            ),
            onBackClick = {},
            onPlayPauseClick = {},
            onSeek = {},
            onPreviousClick = {},
            onNextClick = {}
        )
    }
}

@Composable
@PreviewLightDark
fun PlayerScreenOfflinePreview() {
    AIAudioBookTheme {
        PlayerScreen(
            PlayerUiState(
                "Chapter 3: Offline Mode",
                "Great Adventure",
                PlaybackStatus.PLAYING,
                0.1F,
                30.seconds,
                5.minutes,
                isOffline = true
            ),
            onBackClick = {},
            onPlayPauseClick = {},
            onSeek = {},
            onPreviousClick = {},
            onNextClick = {}
        )
    }
}

@Composable
@PreviewLightDark
fun PlayerScreenLoadingPreview() {
    AIAudioBookTheme {
        PlayerScreen(
            PlayerUiState(
                "Chapter 3: Offline Mode",
                "Great Adventure",
                PlaybackStatus.BUFFERING,
                0.0F,
                0.seconds,
                5.minutes,
            ),
            onBackClick = {},
            onPlayPauseClick = {},
            onSeek = {},
            onPreviousClick = {},
            onNextClick = {}
        )
    }
}