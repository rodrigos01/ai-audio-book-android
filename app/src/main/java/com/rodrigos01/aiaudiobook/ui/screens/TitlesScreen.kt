package com.rodrigos01.aiaudiobook.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.theme.AccentGreen
import com.rodrigos01.aiaudiobook.theme.BorderColor
import com.rodrigos01.aiaudiobook.theme.CardBackground
import com.rodrigos01.aiaudiobook.theme.DarkBackground
import com.rodrigos01.aiaudiobook.theme.DarkSurface
import com.rodrigos01.aiaudiobook.theme.Indigo500
import com.rodrigos01.aiaudiobook.theme.Pink500
import com.rodrigos01.aiaudiobook.theme.TextPrimary
import com.rodrigos01.aiaudiobook.theme.TextSecondary
import com.rodrigos01.aiaudiobook.theme.Typography
import com.rodrigos01.aiaudiobook.theme.Violet500
import com.rodrigos01.aiaudiobook.ui.components.TitleBottomSheet
import com.rodrigos01.aiaudiobook.ui.viewmodel.AuthViewModel
import com.rodrigos01.aiaudiobook.ui.viewmodel.TitlesUiState
import com.rodrigos01.aiaudiobook.ui.viewmodel.TitlesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Audiobooks",
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { authViewModel.signOut(context) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Pink500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { titlesViewModel.showCreateBottomSheet() },
                containerColor = Indigo500,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Title"
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
            // Background gradient elements
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-50).dp, y = 50.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Violet500.copy(alpha = 0.1f), Color.Transparent
                            )
                        )
                    )
            )

            when (val state = titlesState) {
                is TitlesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Indigo500)
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
                            style = Typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { currentUser?.let { titlesViewModel.fetchTitles(it.uid) } },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
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
                                color = TextSecondary,
                                style = Typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Tap the '+' button below to create your first audiobook project.",
                                color = TextSecondary.copy(alpha = 0.7f),
                                style = Typography.bodyMedium,
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
                                    onEditClick = { titlesViewModel.showEditBottomSheet(title) },
                                    onDeleteClick = { titlesViewModel.showDeleteConfirmation(title) }
                                )
                            }
                        }
                    }
                }
            }

            // BottomSheet for creation / editing
            if (isBottomSheetOpen) {
                TitleBottomSheet(
                    editingTitle = editingTitle,
                    onDismiss = { titlesViewModel.dismissBottomSheet() },
                    onSubmit = { name, aiCastingEnabled ->
                        if (editingTitle != null) {
                            titlesViewModel.updateTitle(editingTitle!!.id, name)
                        } else {
                            titlesViewModel.createTitle(name, aiCastingEnabled)
                        }
                    },
                    isSubmitting = isSubmitting,
                    errorMessage = actionError
                )
            }

            // Delete confirmation dialog
            titleToDelete?.let { title ->
                AlertDialog(
                    onDismissRequest = { titlesViewModel.dismissDeleteConfirmation() },
                    title = {
                        Text(
                            text = "Delete Audiobook?",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to delete \"${title.name}\"? This action cannot be undone.",
                            color = TextSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { titlesViewModel.deleteTitle(title.id) },
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
                            onClick = { titlesViewModel.dismissDeleteConfirmation() },
                            enabled = !isSubmitting
                        ) {
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
fun TitleCard(
    title: Title,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = CardBackground.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.name,
                    style = Typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Casting Mode Indicator Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (title.ai_casting_enabled) AccentGreen.copy(alpha = 0.15f)
                                else TextSecondary.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (title.ai_casting_enabled) "AI Casting" else "Solo Voice",
                            color = if (title.ai_casting_enabled) AccentGreen else TextSecondary,
                            style = Typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Title",
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
                            contentDescription = "Delete Title",
                            tint = Pink500.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Narrator voice information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = Violet500,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Narrator: ${title.narrator_voice ?: "Default"}",
                    style = Typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Created: ${formatTimestamp(title.created_at)}",
                    style = Typography.bodyMedium,
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "Unknown date"
    val date = timestamp.toDate()
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(date)
}

