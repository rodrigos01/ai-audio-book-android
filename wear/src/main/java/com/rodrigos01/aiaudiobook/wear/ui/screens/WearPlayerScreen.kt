package com.rodrigos01.aiaudiobook.wear.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import com.rodrigos01.aiaudiobook.common.media.PlaybackStatus
import com.rodrigos01.aiaudiobook.ui.viewmodel.PlayerViewModel
import kotlin.time.Duration

@Composable
fun WearPlayerScreen(playerViewModel: PlayerViewModel, modifier: Modifier = Modifier) {
    val state by playerViewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(state.chapterName.orEmpty())
        Text(state.titleName.orEmpty())

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { playerViewModel.onPrevious() }) { Text("«") }
            Button(onClick = { playerViewModel.onPlayPause() }) {
                Text(if (state.playbackStatus == PlaybackStatus.PLAYING) "Pause" else "Play")
            }
            Button(onClick = { playerViewModel.onNext() }) { Text("»") }
        }

        Text("${formatDuration(state.position)} / ${formatDuration(state.duration)}")
    }
}

private fun formatDuration(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
