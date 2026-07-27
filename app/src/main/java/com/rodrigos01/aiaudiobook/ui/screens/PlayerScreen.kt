package com.rodrigos01.aiaudiobook.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.indicators.ProgressIndicator
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberProgressStateWithTickCount
import com.rodrigos01.aiaudiobook.theme.AIAudioBookTheme
import com.rodrigos01.aiaudiobook.theme.TextPrimary
import com.rodrigos01.aiaudiobook.ui.viewmodel.PlayerUiState

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    uiState: PlayerUiState, onBackClick: () -> Unit, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = uiState.title ?: "") }, navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            })
        },
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PlayerSurface(
                player = player
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
            ) {
                val progressState = rememberProgressStateWithTickCount(player)
                LinearProgressIndicator(progress = { progressState.currentPositionProgress })
                val playPauseState = rememberPlayPauseButtonState(player)
                FilledIconButton(
                    onClick = { playPauseState.onClick() },
                ) {
                    Icon(
                        if (playPauseState.showPlay) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Play/Pause"
                    )
                }
            }
        }
    }
    DisposableEffect(uiState.mediaURI) {
        if (uiState.mediaURI != Uri.EMPTY) {
            player.setMediaItem(
                MediaItem.Builder().setUri(uiState.mediaURI).setMimeType(MimeTypes.AUDIO_MPEG)
                    .build()
            )
            player.prepare()
            player.play()
        }
        onDispose {
            if (player.currentMediaItem != null) {
                player.release()
            }
        }
    }
}

@Composable
@Preview
fun PlayerScreenPreview() {
    AIAudioBookTheme {
        PlayerScreen(PlayerUiState("", Uri.EMPTY), onBackClick = {})
    }
}