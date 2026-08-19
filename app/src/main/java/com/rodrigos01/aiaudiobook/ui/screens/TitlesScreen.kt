package com.rodrigos01.aiaudiobook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.theme.AIAudioBookTheme
import com.rodrigos01.aiaudiobook.ui.components.TitleBottomSheet
import com.rodrigos01.aiaudiobook.ui.viewmodel.AuthViewModel
import com.rodrigos01.aiaudiobook.ui.viewmodel.TitlesUiState
import com.rodrigos01.aiaudiobook.ui.viewmodel.TitlesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TitlesScreen(
    authViewModel: AuthViewModel,
    titlesViewModel: TitlesViewModel,
    onTitleClick: (Title) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser = authViewModel.currentUser
    val titlesState by titlesViewModel.uiState.collectAsState()

    val isBottomSheetOpen by titlesViewModel.isBottomSheetOpen.collectAsState()
    val editingTitle by titlesViewModel.editingTitle.collectAsState()
    val isSubmitting by titlesViewModel.isSubmitting.collectAsState()
    val actionError by titlesViewModel.actionError.collectAsState()
    val titleToDelete by titlesViewModel.titleToDelete.collectAsState()

    // Fetch titles when screen is first loaded
    LaunchedEffect(currentUser) {
        currentUser?.let {
            titlesViewModel.fetchTitles(it.uid)
        }
    }

    TitlesScreen(
        titlesState = titlesState,
        isBottomSheetOpen = isBottomSheetOpen,
        editingTitle = editingTitle,
        isSubmitting = isSubmitting,
        actionError = actionError,
        titleToDelete = titleToDelete,
        onTitleClick = onTitleClick,
        onSignOutClick = { authViewModel.signOut(context) },
        onRetryClick = { currentUser?.let { titlesViewModel.fetchTitles(it.uid) } },
        onShowCreateBottomSheet = { titlesViewModel.showCreateBottomSheet() },
        onShowEditBottomSheet = { titlesViewModel.showEditBottomSheet(it) },
        onShowDeleteConfirmation = { titlesViewModel.showDeleteConfirmation(it) },
        onDismissBottomSheet = { titlesViewModel.dismissBottomSheet() },
        onSubmitTitle = { name, aiCastingEnabled, ttsTier ->
            val currentEditing = editingTitle
            if (currentEditing != null) {
                titlesViewModel.updateTitle(currentEditing.id, name)
            } else {
                titlesViewModel.createTitle(name, aiCastingEnabled, ttsTier)
            }
        },
        onConfirmDelete = { titleId -> titlesViewModel.deleteTitle(titleId) },
        onDismissDeleteConfirmation = { titlesViewModel.dismissDeleteConfirmation() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitlesScreen(
    titlesState: TitlesUiState,
    isBottomSheetOpen: Boolean,
    editingTitle: Title?,
    isSubmitting: Boolean,
    actionError: String?,
    titleToDelete: Title?,
    onTitleClick: (Title) -> Unit,
    onSignOutClick: () -> Unit,
    onRetryClick: () -> Unit,
    onShowCreateBottomSheet: () -> Unit,
    onShowEditBottomSheet: (Title) -> Unit,
    onShowDeleteConfirmation: (Title) -> Unit,
    onDismissBottomSheet: () -> Unit,
    onSubmitTitle: (name: String, aiCastingEnabled: Boolean, ttsTier: String) -> Unit,
    onConfirmDelete: (titleId: String) -> Unit,
    onDismissDeleteConfirmation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
        TopAppBar(
            title = {
            Text(
                text = "My Audiobooks",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }, actions = {
            IconButton(onClick = onSignOutClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
        )
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = onShowCreateBottomSheet,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add, contentDescription = "Add New Title"
            )
        }
    }, containerColor = MaterialTheme.colorScheme.background, modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = titlesState) {
                is TitlesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is TitlesUiState.Error -> {
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

                is TitlesUiState.Success -> {
                    val titles = state.titles
                    if (titles.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No audiobooks found.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Tap the '+' button below to create your first audiobook project.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                        ) {
                            items(titles) { title ->
                                TitleCard(
                                    title = title,
                                          onClick = { onTitleClick(title) },
                                          onEditClick = { onShowEditBottomSheet(title) },
                                          onDeleteClick = { onShowDeleteConfirmation(title) })
                            }
                        }
                    }
                }
            }

            // BottomSheet for creation / editing
            if (isBottomSheetOpen) {
                TitleBottomSheet(
                    editingTitle = editingTitle,
                    onDismiss = onDismissBottomSheet,
                    onSubmit = onSubmitTitle,
                    isSubmitting = isSubmitting,
                    errorMessage = actionError
                )
            }

            // Delete confirmation dialog
            titleToDelete?.let { title ->
                AlertDialog(
                    onDismissRequest = onDismissDeleteConfirmation, title = {
                    Text(
                        text = "Delete Audiobook?",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }, text = {
                    Text(
                        text = "Are you sure you want to delete \"${title.name}\"? This action cannot be undone.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }, confirmButton = {
                    TextButton(
                        onClick = { onConfirmDelete(title.id) }, enabled = !isSubmitting
                    ) {
                        Text(
                            text = if (isSubmitting) "Deleting..." else "Delete",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }, dismissButton = {
                    TextButton(
                        onClick = onDismissDeleteConfirmation, enabled = !isSubmitting
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }
        }
    }
}

@Composable
fun TitleCard(
    title: Title, onClick: () -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        supportingContent = {
            Text(
                text = formatTimestamp(title.created_at),
                style = MaterialTheme.typography.bodySmall,
            )
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (title.ai_casting_enabled) "AI Casting" else "Solo Voice",
                    color = if (title.ai_casting_enabled) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (title.ai_casting_enabled) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )

                IconButton(
                    onClick = onEditClick, modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Title",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick, modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Title",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
    ) {
        Text(text = title.name, style = MaterialTheme.typography.titleLarge)
    }
}

private fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "Unknown date"
    val date = timestamp.toDate()
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(date)
}

@PreviewLightDark
@Composable
fun TitlesScreenSuccessPreview() {
    val sampleTitles = listOf(
        Title(
            id = "1",
            name = "The Great Gatsby",
            ai_casting_enabled = true,
            narrator_voice = "en-US-Journey-F"
        ), Title(
            id = "2", name = "1984", ai_casting_enabled = false, narrator_voice = "en-US-Standard-A"
        )
    )
    AIAudioBookTheme {
        TitlesScreen(
            titlesState = TitlesUiState.Success(sampleTitles),
            isBottomSheetOpen = false,
            editingTitle = null,
            isSubmitting = false,
            actionError = null,
            titleToDelete = null,
            onTitleClick = {},
            onSignOutClick = {},
            onRetryClick = {},
            onShowCreateBottomSheet = {},
            onShowEditBottomSheet = {},
            onShowDeleteConfirmation = {},
            onDismissBottomSheet = {},
            onSubmitTitle = { _, _, _ -> },
            onConfirmDelete = {},
            onDismissDeleteConfirmation = {})
    }
}

@PreviewLightDark
@Composable
fun TitlesScreenEmptyPreview() {
    AIAudioBookTheme {
        TitlesScreen(
            titlesState = TitlesUiState.Success(emptyList()),
            isBottomSheetOpen = false,
            editingTitle = null,
            isSubmitting = false,
            actionError = null,
            titleToDelete = null,
            onTitleClick = {},
            onSignOutClick = {},
            onRetryClick = {},
            onShowCreateBottomSheet = {},
            onShowEditBottomSheet = {},
            onShowDeleteConfirmation = {},
            onDismissBottomSheet = {},
            onSubmitTitle = { _, _, _ -> },
            onConfirmDelete = {},
            onDismissDeleteConfirmation = {})
    }
}

@PreviewLightDark
@Composable
fun TitlesScreenLoadingPreview() {
    AIAudioBookTheme {
        TitlesScreen(
            titlesState = TitlesUiState.Loading,
            isBottomSheetOpen = false,
            editingTitle = null,
            isSubmitting = false,
            actionError = null,
            titleToDelete = null,
            onTitleClick = {},
            onSignOutClick = {},
            onRetryClick = {},
            onShowCreateBottomSheet = {},
            onShowEditBottomSheet = {},
            onShowDeleteConfirmation = {},
            onDismissBottomSheet = {},
            onSubmitTitle = { _, _, _ -> },
            onConfirmDelete = {},
            onDismissDeleteConfirmation = {})
    }
}


