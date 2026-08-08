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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigos01.aiaudiobook.data.Chapter
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.theme.AIAudioBookTheme
import com.rodrigos01.aiaudiobook.theme.AccentOrange
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
    val chaptersState by chaptersViewModel.uiState.collectAsState()

    // Fetch chapters when screen is loaded
    LaunchedEffect(title.id) {
        chaptersViewModel.fetchChapters(title.id)
    }

    ChaptersScreen(
        title,
        chaptersState,
        onBackClick,
        onChapterClick,
        onReloadClick = { chaptersViewModel.fetchChapters(title.id) },
        modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    title: Title,
    chaptersState: ChaptersUiState,
    onBackClick: () -> Unit,
    onChapterClick: (Chapter) -> Unit,
    onReloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {

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
            }, navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkBackground,
                titleContentColor = TextPrimary,
                navigationIconContentColor = TextPrimary
            )
            )
        }, containerColor = DarkBackground, modifier = modifier
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
                is ChaptersUiState.Idle -> {
                    // Stay blank or default
                }

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
                            onClick = onReloadClick,
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
                                style = Typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                        ) {
                            items(chapters) { chapter ->
                                ChapterItemCard(
                                    chapter = chapter, modifier = Modifier.clickable(onClick = {
                                        onChapterClick(chapter)
                                    })
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterItemCard(
    chapter: Chapter, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chapter.order_index.toString(),
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

            // SSML format indicator
            if (chapter.is_ssml) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentOrange.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SSML Enabled",
                        color = AccentOrange,
                        style = Typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChaptersScreenPreview() {
    AIAudioBookTheme {
        Scaffold { innerPadding ->
            ChaptersScreen(
                title = Title(id = "123", name = "Sample Title"),
                onBackClick = {},
                onChapterClick = {},
                onReloadClick = {},
                chaptersState = ChaptersUiState.Success(
                    chapters = listOf(
                        Chapter(order_index = 0, id = "1", name = "Chapter 1", content = "Chapter content 1"),
                        Chapter(order_index = 1, id = "2", name = "Chapter 2", content = "Chapter content 2", is_ssml = true)
                    )
                ),
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
