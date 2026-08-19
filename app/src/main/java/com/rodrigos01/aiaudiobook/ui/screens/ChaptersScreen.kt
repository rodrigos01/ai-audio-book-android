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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigos01.aiaudiobook.common.network.NetworkMonitor
import com.rodrigos01.aiaudiobook.data.Chapter
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.data.Voice
import com.rodrigos01.aiaudiobook.data.offline.ChapterDownloadState
import com.rodrigos01.aiaudiobook.theme.AIAudioBookTheme
import com.rodrigos01.aiaudiobook.ui.components.ChapterBottomSheet
import com.rodrigos01.aiaudiobook.ui.viewmodel.ChaptersUiState
import com.rodrigos01.aiaudiobook.ui.viewmodel.ChaptersViewModel

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
    val currentTitle = observedTitle ?: title

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
    LaunchedEffect(currentTitle.id) {
        chaptersViewModel.fetchChapters(currentTitle.id)
    }

    ChaptersScreen(
        title = currentTitle,
        chaptersState = chaptersState,
        isBottomSheetOpen = isBottomSheetOpen,
        editingChapter = editingChapter,
        isSubmitting = isSubmitting,
        actionError = actionError,
        chapterToDelete = chapterToDelete,
        voices = voices,
        isLoadingVoices = isLoadingVoices,
        downloadStates = downloadStates,
        pendingMeteredDownloadChapter = pendingMeteredDownloadChapter,
        onBackClick = onBackClick,
        onChapterClick = onChapterClick,
        onRetryClick = { chaptersViewModel.fetchChapters(currentTitle.id) },
        onShowCreateBottomSheet = { chaptersViewModel.showCreateBottomSheet(currentTitle.ai_casting_enabled) },
        onShowEditBottomSheet = { chaptersViewModel.showEditBottomSheet(it) },
        onShowDeleteConfirmation = { chaptersViewModel.showDeleteConfirmation(it) },
        onRequestDownload = { chapter -> chaptersViewModel.requestDownload(chapter, NetworkMonitor.isOnWifi(context)) },
        onDeleteDownload = { chapterId -> chaptersViewModel.deleteDownload(chapterId) },
        onDismissBottomSheet = { chaptersViewModel.dismissBottomSheet() },
        onSubmitChapter = { name, content, voiceId, googleDocId, googleAccessToken ->
            val currentEditing = editingChapter
            if (currentEditing != null) {
                chaptersViewModel.updateChapter(currentEditing.id, name, content)
            } else {
                chaptersViewModel.createChapter(
                    titleId = currentTitle.id,
                    name = name,
                    content = content,
                    voiceId = voiceId ?: "",
                    googleDocId = googleDocId,
                    googleAccessToken = googleAccessToken
                )
            }
        },
        onConfirmDeleteChapter = { chapterId -> chaptersViewModel.deleteChapter(chapterId) },
        onDismissDeleteConfirmation = { chaptersViewModel.dismissDeleteConfirmation() },
        onConfirmMeteredDownload = { chaptersViewModel.confirmMeteredDownload() },
        onDismissMeteredDownloadConfirmation = { chaptersViewModel.dismissMeteredDownloadConfirmation() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    title: Title,
    chaptersState: ChaptersUiState,
    isBottomSheetOpen: Boolean,
    editingChapter: Chapter?,
    isSubmitting: Boolean,
    actionError: String?,
    chapterToDelete: Chapter?,
    voices: List<Voice>,
    isLoadingVoices: Boolean,
    downloadStates: Map<String, ChapterDownloadState>,
    pendingMeteredDownloadChapter: Chapter?,
    onBackClick: () -> Unit,
    onChapterClick: (Chapter) -> Unit,
    onRetryClick: () -> Unit,
    onShowCreateBottomSheet: () -> Unit,
    onShowEditBottomSheet: (Chapter) -> Unit,
    onShowDeleteConfirmation: (Chapter) -> Unit,
    onRequestDownload: (Chapter) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onDismissBottomSheet: () -> Unit,
    onSubmitChapter: (name: String, content: String, voiceId: String?, googleDocId: String?, googleAccessToken: String?) -> Unit,
    onConfirmDeleteChapter: (chapterId: String) -> Unit,
    onDismissDeleteConfirmation: () -> Unit,
    onConfirmMeteredDownload: () -> Unit,
    onDismissMeteredDownloadConfirmation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Chapters List",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onShowCreateBottomSheet,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Chapter"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = chaptersState) {
                is ChaptersUiState.Idle -> {}

                is ChaptersUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = onRetryClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Tap '+' below to add your first chapter.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
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
                                    onEditClick = { onShowEditBottomSheet(chapter) },
                                    onDeleteClick = { onShowDeleteConfirmation(chapter) },
                                    onDownloadClick = { onRequestDownload(chapter) },
                                    onDeleteDownloadClick = { onDeleteDownload(chapter.id) }
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
                    onDismiss = onDismissBottomSheet,
                    onSubmit = onSubmitChapter,
                    isSubmitting = isSubmitting,
                    errorMessage = actionError
                )
            }

            // Delete confirmation dialog
            chapterToDelete?.let { chapter ->
                AlertDialog(
                    onDismissRequest = onDismissDeleteConfirmation,
                    title = {
                        Text(
                            text = "Delete Chapter?",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to delete \"${chapter.name ?: "Untitled Chapter"}\"? This action cannot be undone.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { onConfirmDeleteChapter(chapter.id) },
                            enabled = !isSubmitting
                        ) {
                            Text(
                                text = if (isSubmitting) "Deleting..." else "Delete",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onDismissDeleteConfirmation,
                            enabled = !isSubmitting
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }

            // Confirmation before downloading over mobile data
            pendingMeteredDownloadChapter?.let { chapter ->
                AlertDialog(
                    onDismissRequest = onDismissMeteredDownloadConfirmation,
                    title = {
                        Text(
                            text = "Not on Wi-Fi",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = "You're not connected to Wi-Fi. Download \"${chapter.name ?: "this chapter"}\" using mobile data?",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = onConfirmMeteredDownload) {
                            Text("Download", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismissMeteredDownloadConfirmation) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = chapter.name ?: "Untitled Chapter",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
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
                    val (badgeText, badgeBg, badgeFg) = when (chapter.ai_casting_status) {
                        "completed" -> Triple("Ready", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                        "in_progress" -> Triple("Casting...", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                        "failed" -> Triple("Casting Failed", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                        else -> Triple("Pending", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
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
                                    tint = badgeFg,
                                    modifier = Modifier.size(11.dp)
                                )
                                "in_progress" -> CircularProgressIndicator(
                                    modifier = Modifier.size(11.dp),
                                    color = badgeFg,
                                    strokeWidth = 1.5.dp
                                )
                                else -> {}
                            }
                            Text(
                                text = badgeText,
                                color = badgeFg,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                } else if (chapter.is_ssml) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SSML",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelLarge,
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        tint = MaterialTheme.colorScheme.error,
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )

            is ChapterDownloadState.Preparing -> {
                val fraction = downloadState.totalSections.takeIf { it > 0 }
                    ?.let { downloadState.generatedSections.toFloat() / it }
                if (fraction != null) {
                    CircularProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }

            is ChapterDownloadState.Downloading -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )

            is ChapterDownloadState.Downloaded -> Icon(
                imageVector = Icons.Default.DownloadDone,
                contentDescription = "Downloaded for offline listening, tap to remove",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp)
            )

            is ChapterDownloadState.Failed -> Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Download failed, tap to retry",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
fun ChaptersScreenSuccessPreview() {
    val sampleTitle = Title(id = "1", name = "The Great Gatsby", ai_casting_enabled = true)
    val sampleChapters = listOf(
        Chapter(id = "c1", name = "Chapter 1: Arrival", order_index = 0, ai_casting_status = "completed"),
        Chapter(id = "c2", name = "Chapter 2: The Dinner Party", order_index = 1, ai_casting_status = "in_progress")
    )
    AIAudioBookTheme {
        ChaptersScreen(
            title = sampleTitle,
            chaptersState = ChaptersUiState.Success(sampleChapters),
            isBottomSheetOpen = false,
            editingChapter = null,
            isSubmitting = false,
            actionError = null,
            chapterToDelete = null,
            voices = emptyList(),
            isLoadingVoices = false,
            downloadStates = emptyMap(),
            pendingMeteredDownloadChapter = null,
            onBackClick = {},
            onChapterClick = {},
            onRetryClick = {},
            onShowCreateBottomSheet = {},
            onShowEditBottomSheet = {},
            onShowDeleteConfirmation = {},
            onRequestDownload = {},
            onDeleteDownload = {},
            onDismissBottomSheet = {},
            onSubmitChapter = { _, _, _, _, _ -> },
            onConfirmDeleteChapter = {},
            onDismissDeleteConfirmation = {},
            onConfirmMeteredDownload = {},
            onDismissMeteredDownloadConfirmation = {}
        )
    }
}

@PreviewLightDark
@Composable
fun ChaptersScreenEmptyPreview() {
    val sampleTitle = Title(id = "1", name = "The Great Gatsby", ai_casting_enabled = false)
    AIAudioBookTheme {
        ChaptersScreen(
            title = sampleTitle,
            chaptersState = ChaptersUiState.Success(emptyList()),
            isBottomSheetOpen = false,
            editingChapter = null,
            isSubmitting = false,
            actionError = null,
            chapterToDelete = null,
            voices = emptyList(),
            isLoadingVoices = false,
            downloadStates = emptyMap(),
            pendingMeteredDownloadChapter = null,
            onBackClick = {},
            onChapterClick = {},
            onRetryClick = {},
            onShowCreateBottomSheet = {},
            onShowEditBottomSheet = {},
            onShowDeleteConfirmation = {},
            onRequestDownload = {},
            onDeleteDownload = {},
            onDismissBottomSheet = {},
            onSubmitChapter = { _, _, _, _, _ -> },
            onConfirmDeleteChapter = {},
            onDismissDeleteConfirmation = {},
            onConfirmMeteredDownload = {},
            onDismissMeteredDownloadConfirmation = {}
        )
    }
}

@PreviewLightDark
@Composable
fun ChaptersScreenLoadingPreview() {
    val sampleTitle = Title(id = "1", name = "The Great Gatsby", ai_casting_enabled = false)
    AIAudioBookTheme {
        ChaptersScreen(
            title = sampleTitle,
            chaptersState = ChaptersUiState.Loading,
            isBottomSheetOpen = false,
            editingChapter = null,
            isSubmitting = false,
            actionError = null,
            chapterToDelete = null,
            voices = emptyList(),
            isLoadingVoices = false,
            downloadStates = emptyMap(),
            pendingMeteredDownloadChapter = null,
            onBackClick = {},
            onChapterClick = {},
            onRetryClick = {},
            onShowCreateBottomSheet = {},
            onShowEditBottomSheet = {},
            onShowDeleteConfirmation = {},
            onRequestDownload = {},
            onDeleteDownload = {},
            onDismissBottomSheet = {},
            onSubmitChapter = { _, _, _, _, _ -> },
            onConfirmDeleteChapter = {},
            onDismissDeleteConfirmation = {},
            onConfirmMeteredDownload = {},
            onDismissMeteredDownloadConfirmation = {}
        )
    }
}


