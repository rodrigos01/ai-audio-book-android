package com.rodrigos01.aiaudiobook.wear.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.placeholderShimmer
import androidx.wear.compose.material.rememberPlaceholderState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import com.rodrigos01.aiaudiobook.data.Chapter
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.ui.viewmodel.ChaptersUiState
import com.rodrigos01.aiaudiobook.ui.viewmodel.ChaptersViewModel

/**
 * Unlike the phone, this only surfaces the AI-casting-status indicator (the one badge that
 * actually blocks playback - a chapter still being cast has no audio yet) and drops every other
 * badge the phone shows, per the Wear OS plan's simplified-UI decision.
 */
private fun isCastingBlocked(title: Title?, chapter: Chapter): Boolean =
    title?.ai_casting_enabled == true && chapter.ai_casting_status != null && chapter.ai_casting_status != "completed"

private fun castingStatusLabel(chapter: Chapter): String = when (chapter.ai_casting_status) {
    "completed" -> "Ready"
    "in_progress" -> "Casting…"
    "failed" -> "Casting failed"
    else -> "Pending"
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun WearChaptersScreen(
    titleId: String,
    chaptersViewModel: ChaptersViewModel,
    onChapterClick: (Chapter) -> Unit,
    modifier: Modifier = Modifier
) {
    val title by chaptersViewModel.title.collectAsState()
    val state by chaptersViewModel.uiState.collectAsState()

    LaunchedEffect(titleId) { chaptersViewModel.fetchChapters(titleId) }
    DisposableEffect(Unit) { onDispose { chaptersViewModel.clearChapters() } }

    when (val current = state) {
        is ChaptersUiState.Idle, is ChaptersUiState.Loading -> Box(
            modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading…")
        }

        is ChaptersUiState.Error -> Box(
            modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(current.message)
        }

        is ChaptersUiState.Success -> {
            val listState = rememberScalingLazyListState()
            ScalingLazyColumn(modifier = modifier.fillMaxSize(), state = listState) {
                item { ListHeader { Text(title?.name.orEmpty()) } }
                items(current.chapters, key = { it.id }) { chapter ->
                    val blocked = isCastingBlocked(title, chapter)
                    Button(
                        onClick = { onChapterClick(chapter) },
                        enabled = !blocked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .placeholderShimmer(rememberPlaceholderState { blocked })
                    ) {
                        Text("${chapter.order_index} - ${chapter.name}")
                    }
                }
            }
        }
    }
}
