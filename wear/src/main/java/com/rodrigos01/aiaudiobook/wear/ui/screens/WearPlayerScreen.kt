package com.rodrigos01.aiaudiobook.wear.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.rodrigos01.aiaudiobook.common.media.PlaybackStatus
import com.rodrigos01.aiaudiobook.ui.toDurationString
import com.rodrigos01.aiaudiobook.ui.viewmodel.PlayerViewModel
import kotlin.time.Duration

@Composable
fun WearPlayerScreen(playerViewModel: PlayerViewModel, modifier: Modifier = Modifier) {
    val state by playerViewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(state.chapterName.orEmpty(), style = MaterialTheme.typography.displayMedium)
        Text(state.titleName.orEmpty(), style = MaterialTheme.typography.titleSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = playerViewModel::onPrevious) {
                Icon(
                    Icons.Default.SkipPrevious, contentDescription = "Previous",
                )
            }
            when (state.playbackStatus) {
                PlaybackStatus.PAUSED -> PlayPauseButton(
                    Icons.Default.PlayArrow, playerViewModel::onPlayPause
                )

                PlaybackStatus.BUFFERING -> CircularProgressIndicator()
                else -> PlayPauseButton(Icons.Default.Pause, playerViewModel::onPlayPause)
            }
            IconButton(onClick = playerViewModel::onNext) {
                Icon(
                    Icons.Default.SkipNext, contentDescription = "Previous",
                )
            }
        }

        Text("${state.position.toDurationString()} / ${state.duration.toDurationString()}")
    }
}

@Composable
fun PlayPauseButton(imageVector: ImageVector, onPlayPauseClick: () -> Unit) {
    FilledIconButton(
        onClick = onPlayPauseClick
    ) {
        Icon(
            imageVector,
            contentDescription = "Play/Pause",
        )
    }
}
