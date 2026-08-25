package com.rodrigos01.aiaudiobook.wear.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import com.rodrigos01.aiaudiobook.data.offline.OfflineDownloadRepository
import kotlinx.coroutines.launch

/**
 * Lets the user see and manually delete downloaded chapters - the v1 answer to low-storage
 * handling on the watch (warn-and-block on new downloads, manual cleanup here), instead of an
 * automatic eviction policy, per the Wear OS plan.
 */
@Composable
fun WearDownloadsScreen(
    offlineDownloadRepository: OfflineDownloadRepository,
    modifier: Modifier = Modifier
) {
    val downloads by offlineDownloadRepository.observeAllDownloads().collectAsState(initial = emptyMap())
    val scope = rememberCoroutineScope()

    if (downloads.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No downloads")
        }
        return
    }

    val listState = rememberScalingLazyListState()
    ScalingLazyColumn(modifier = modifier.fillMaxSize(), state = listState) {
        item { ListHeader { Text("Downloads") } }
        items(downloads.values.toList(), key = { it.chapterId }) { entry ->
            Button(onClick = {
                scope.launch { offlineDownloadRepository.deleteDownload(entry.chapterId) }
            }) {
                Column {
                    Text(entry.chapterId)
                    Text(formatSize(entry.sizeBytes))
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String = "%.1f MB".format(bytes / (1024.0 * 1024.0))
