package com.rodrigos01.aiaudiobook.wear.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import com.rodrigos01.aiaudiobook.common.network.NetworkMonitor
import com.rodrigos01.aiaudiobook.data.offline.ChapterDownloadState
import com.rodrigos01.aiaudiobook.ui.viewmodel.ChaptersUiState
import com.rodrigos01.aiaudiobook.ui.viewmodel.ChaptersViewModel
import com.rodrigos01.aiaudiobook.wear.offline.WearStorageGuard

/**
 * Shown when a chapter is tapped, instead of playing it immediately like the phone does - lets
 * the user choose to play now or download for offline, per the Wear OS plan's simplified-UI
 * decision.
 */
@Composable
fun WearChapterActionScreen(
    chapterId: String,
    chaptersViewModel: ChaptersViewModel,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by chaptersViewModel.uiState.collectAsState()
    val downloadStates by chaptersViewModel.downloadStates.collectAsState()
    val pendingMeteredChapter by chaptersViewModel.pendingMeteredDownloadChapter.collectAsState()

    val chapter = (state as? ChaptersUiState.Success)?.chapters?.firstOrNull { it.id == chapterId }
    val downloadState = downloadStates[chapterId] ?: ChapterDownloadState.NotDownloaded
    var showLowStorageWarning by remember { mutableStateOf(false) }

    fun startDownload() {
        val target = chapter ?: return
        if (WearStorageGuard.hasSpaceForDownload(context)) {
            showLowStorageWarning = false
            chaptersViewModel.requestDownload(target, NetworkMonitor.isOnWifi(context))
        } else {
            showLowStorageWarning = true
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(chapter?.name ?: "Chapter")

        Button(onClick = onPlayClick) {
            Text("Play")
        }

        when (downloadState) {
            is ChapterDownloadState.Downloaded -> Text("Downloaded")
            is ChapterDownloadState.Preparing -> Text("Preparing…")
            is ChapterDownloadState.Downloading -> Text("Downloading…")
            is ChapterDownloadState.Failed -> Button(onClick = { startDownload() }) {
                Text("Retry download")
            }
            ChapterDownloadState.NotDownloaded -> Button(onClick = { startDownload() }) {
                Text("Download")
            }
        }

        if (showLowStorageWarning) {
            Text("Not enough storage on watch. Free up space in Downloads.")
        }

        if (pendingMeteredChapter != null) {
            Text("Not on Wi-Fi. Download using mobile data?")
            Button(onClick = { chaptersViewModel.confirmMeteredDownload() }) {
                Text("Download anyway")
            }
            Button(onClick = { chaptersViewModel.dismissMeteredDownloadConfirmation() }) {
                Text("Cancel")
            }
        }
    }
}
