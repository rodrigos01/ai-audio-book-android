package com.rodrigos01.aiaudiobook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.rodrigos01.aiaudiobook.common.media.PlaybackStatus
import com.rodrigos01.aiaudiobook.theme.AIAudioBookTheme
import com.rodrigos01.aiaudiobook.theme.TextPrimary
import com.rodrigos01.aiaudiobook.ui.toDurationString
import com.rodrigos01.aiaudiobook.ui.viewmodel.PlayerUiState
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
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
                Text(uiState.chapterName.orEmpty(), style = MaterialTheme.typography.headlineLarge)
                Text(uiState.titleName.orEmpty(), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(24.dp))
                var sliderPosition by remember { mutableFloatStateOf(uiState.playbackProgress) }
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = { onSeek(sliderPosition) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val positionString = if (sliderPosition > 0) {
                        uiState.duration * sliderPosition.toDouble()
                    } else {
                        uiState.position
                    }.toDurationString()
                    Text(positionString)
                    Text(uiState.duration.toDurationString())
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    IconButton(onClick = onPreviousClick) {
                        Icon(
                            Icons.Default.SkipPrevious, contentDescription = "Previous"
                        )
                    }
                    when (uiState.playbackStatus) {
                        PlaybackStatus.PAUSED -> PlayPauseButton(
                            Icons.Default.PlayArrow, onPlayPauseClick
                        )

                        PlaybackStatus.BUFFERING -> CircularProgressIndicator()
                        else -> PlayPauseButton(Icons.Default.Pause, onPlayPauseClick)
                    }
                    IconButton(onClick = onNextClick) {
                        Icon(
                            Icons.Default.SkipNext, contentDescription = "Previous"
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
        onClick = onPlayPauseClick, modifier = Modifier.size(56.dp)
    ) {
        Icon(imageVector, contentDescription = "Play/Pause")
    }
}

@Composable
@Preview
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