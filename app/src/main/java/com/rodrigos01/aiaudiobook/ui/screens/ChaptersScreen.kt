package com.rodrigos01.aiaudiobook.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigos01.aiaudiobook.common.network.NetworkMonitor
import com.rodrigos01.aiaudiobook.data.Chapter
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.data.offline.ChapterDownloadState
import com.rodrigos01.aiaudiobook.theme.AIAudioBookTheme
import com.rodrigos01.aiaudiobook.theme.AccentGreen
import com.rodrigos01.aiaudiobook.theme.AccentOrange
import com.rodrigos01.aiaudiobook.theme.BorderColor
import com.rodrigos01.aiaudiobook.theme.DarkBackground
import com.rodrigos01.aiaudiobook.theme.DarkSurface
import com.rodrigos01.aiaudiobook.theme.Indigo500
import com.rodrigos01.aiaudiobook.theme.Pink500
import com.rodrigos01.aiaudiobook.theme.TextPrimary
import com.rodrigos01.aiaudiobook.theme.TextSecondary
import com.rodrigos01.aiaudiobook.theme.Typography
import com.rodrigos01.aiaudiobook.theme.Violet500
import com.rodrigos01.aiaudiobook.ui.components.ChapterBottomSheet
import com.rodrigos01.aiaudiobook.ui.viewmodel.ChaptersUiState
import com.rodrigos01.aiaudiobook.ui.viewmodel.ChaptersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    title: Title,
    chaptersViewModel: ChaptersViewModel,
    onBackClick: () -> Unit,
    onChapterClick: (Chapter) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chaptersState by chaptersViewModel.uiState.collectAsState()
    val observedTitle by chaptersViewModel.title.collectAsState()
    // Navigation only carries the title id/name, so fall back to the stub until the full
    // Firestore document (ai_casting_enabled, casting_map, etc.) has loaded.
    val title = observedTitle ?: title

    val isBottomSheetOpen by chaptersViewModel.isBottomSheetOpen.collectAsState()
    val editingChapter by chaptersViewModel.editingChapter.collectAsState()
    val isSubmitting by chaptersViewModel.isSubmitting.collectAsState()
    val actionError by chaptersViewModel.actionError.collectAsState()
    val chapterToDelete by chaptersViewModel.chapterToDelete.collectAsState()

    val voices by chaptersViewModel.voices.collectAsState()
    val isLoadingVoices by chaptersViewModel.isLoadingVoices.collectAsState()

    val downloadStates by chaptersViewModel.downloadStates.collectAsState()
    val pendingMeteredDownloadChapter by chaptersViewModel.pendingMeteredDownloadChapter.collectAsState()

    // Fetch chapters when screen is loaded
    LaunchedEffect(title.id) {
        chaptersViewModel.fetchChapters(title.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title.name,
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Chapters List",
                            style = Typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { chaptersViewModel.showCreateBottomSheet(title.ai_casting_enabled) },
                containerColor = Indigo500,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Chapter"
                )
            }
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            // Background glow
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 50.dp, y = (-50).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Pink500.copy(alpha = 0.08f), Color.Transparent
                            )
                        )
                    )
            )

            when (val state = chaptersState) {
                is ChaptersUiState.Idle -> {}

                is ChaptersUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Indigo500)
                    }
                }

                is ChaptersUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = Typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { chaptersViewModel.fetchChapters(title.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
                        ) {
                            Text("Retry")
                        }
                    }
                }

                is ChaptersUiState.Success -> {
                    val chapters = state.chapters
                    if (chapters.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No chapters found for this title.",
                                color = TextSecondary,
                                style = Typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Tap '+' below to add your first chapter.",
                                color = TextSecondary.copy(alpha = 0.7f),
                                style = Typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                        ) {
                            items(chapters) { chapter ->
                                ChapterItemCard(
                                    chapter = chapter,
                                    aiCastingEnabled = title.ai_casting_enabled,
                                    downloadState = downloadStates[chapter.id] ?: ChapterDownloadState.NotDownloaded,
                                    onClick = {
                                        if (title.ai_casting_enabled && chapter.ai_casting_status != "completed") {
                                            val statusMsg = when (chapter.ai_casting_status) {
                                                "in_progress" -> "AI Casting is currently in progress for this chapter."
                                                "failed" -> "AI Casting failed for this chapter."
                                                else -> "Chapter is pending AI casting."
                                            }
                                            Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                                        } else {
                                            onChapterClick(chapter)
                                        }
                                    },
                                    onEditClick = { chaptersViewModel.showEditBottomSheet(chapter) },
                                    onDeleteClick = { chaptersViewModel.showDeleteConfirmation(chapter) },
                                    onDownloadClick = {
                                        chaptersViewModel.requestDownload(chapter, NetworkMonitor.isOnWifi(context))
                                    },
                                    onDeleteDownloadClick = { chaptersViewModel.deleteDownload(chapter.id) }
                                )
                            }
                        }
                    }
                }
            }

            // BottomSheet for creation / editing
            if (isBottomSheetOpen) {
                ChapterBottomSheet(
                    editingChapter = editingChapter,
                    aiCastingEnabled = title.ai_casting_enabled,
                    voices = voices,
                    isLoadingVoices = isLoadingVoices,
                    onDismiss = { chaptersViewModel.dismissBottomSheet() },
                    onSubmit = { name, content, voiceId ->
                        if (editingChapter != null) {
                            chaptersViewModel.updateChapter(editingChapter!!.id, name, content)
                        } else {
                            chaptersViewModel.createChapter(title.id, name, content, voiceId)
                        }
                    },
                    isSubmitting = isSubmitting,
                    errorMessage = actionError
                )
            }

            // Delete confirmation dialog
            chapterToDelete?.let { chapter ->
                AlertDialog(
                    onDismissRequest = { chaptersViewModel.dismissDeleteConfirmation() },
                    title = {
                        Text(
                            text = "Delete Chapter?",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to delete \"${chapter.name ?: "Untitled Chapter"}\"? This action cannot be undone.",
                            color = TextSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { chaptersViewModel.deleteChapter(chapter.id) },
                            enabled = !isSubmitting
                        ) {
                            Text(
                                text = if (isSubmitting) "Deleting..." else "Delete",
                                color = Pink500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { chaptersViewModel.dismissDeleteConfirmation() },
                            enabled = !isSubmitting
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    containerColor = DarkSurface
                )
            }

            // Confirmation before downloading over mobile data
            pendingMeteredDownloadChapter?.let { chapter ->
                AlertDialog(
                    onDismissRequest = { chaptersViewModel.dismissMeteredDownloadConfirmation() },
                    title = {
                        Text(
                            text = "Not on Wi-Fi",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    },
                    text = {
                        Text(
                            text = "You're not connected to Wi-Fi. Download \"${chapter.name ?: "this chapter"}\" using mobile data?",
                            color = TextSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { chaptersViewModel.confirmMeteredDownload() }) {
                            Text("Download", color = Indigo500, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { chaptersViewModel.dismissMeteredDownloadConfirmation() }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    containerColor = DarkSurface
                )
            }
        }
    }
}

@Composable
fun ChapterItemCard(
    chapter: Chapter,
    aiCastingEnabled: Boolean = false,
    downloadState: ChapterDownloadState = ChapterDownloadState.NotDownloaded,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onDeleteDownloadClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${chapter.order_index + 1}.",
                style = Typography.titleMedium,
                color = Violet500,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = chapter.name ?: "Untitled Chapter",
                style = Typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // AI Casting Status badge (when title has AI casting enabled)
                if (aiCastingEnabled) {
                    val (badgeText, badgeColor) = when (chapter.ai_casting_status) {
                        "completed" -> "Ready" to AccentGreen
                        "in_progress" -> "Casting..." to AccentOrange
                        "failed" -> "Casting Failed" to Pink500
                        else -> "Pending" to TextSecondary
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            when (chapter.ai_casting_status) {
                                "completed" -> Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Casting complete",
                                    tint = badgeColor,
                                    modifier = Modifier.size(11.dp)
                                )
                                "in_progress" -> CircularProgressIndicator(
                                    modifier = Modifier.size(11.dp),
                                    color = badgeColor,
                                    strokeWidth = 1.5.dp
                                )
                                else -> {}
                            }
                            Text(
                                text = badgeText,
                                color = badgeColor,
                                style = Typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                } else if (chapter.is_ssml) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentOrange.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SSML",
                            color = AccentOrange,
                            style = Typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }

                DownloadButton(
                    downloadState = downloadState,
                    onDownloadClick = onDownloadClick,
                    onDeleteDownloadClick = onDeleteDownloadClick
                )

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Chapter",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Chapter",
                        tint = Pink500.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadButton(
    downloadState: ChapterDownloadState,
    onDownloadClick: () -> Unit,
    onDeleteDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = {
            when (downloadState) {
                is ChapterDownloadState.NotDownloaded, is ChapterDownloadState.Failed -> onDownloadClick()
                is ChapterDownloadState.Downloaded -> onDeleteDownloadClick()
                is ChapterDownloadState.Preparing, is ChapterDownloadState.Downloading -> Unit
            }
        },
        modifier = modifier.size(32.dp)
    ) {
        when (downloadState) {
            is ChapterDownloadState.NotDownloaded -> Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download for offline listening",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )

            is ChapterDownloadState.Preparing, is ChapterDownloadState.Downloading -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Indigo500,
                strokeWidth = 2.dp
            )

            is ChapterDownloadState.Downloaded -> Icon(
                imageVector = Icons.Default.DownloadDone,
                contentDescription = "Downloaded for offline listening, tap to remove",
                tint = AccentGreen,
                modifier = Modifier.size(18.dp)
            )

            is ChapterDownloadState.Failed -> Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Download failed, tap to retry",
                tint = Pink500,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

