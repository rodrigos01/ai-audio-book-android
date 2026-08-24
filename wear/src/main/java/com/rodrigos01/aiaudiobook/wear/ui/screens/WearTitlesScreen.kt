package com.rodrigos01.aiaudiobook.wear.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.ui.viewmodel.TitlesUiState
import com.rodrigos01.aiaudiobook.ui.viewmodel.TitlesViewModel
import com.rodrigos01.aiaudiobook.wear.auth.WearAuthViewModel

@Composable
fun WearTitlesScreen(
    authViewModel: WearAuthViewModel,
    titlesViewModel: TitlesViewModel,
    onTitleClick: (Title) -> Unit,
    onDownloadsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser = authViewModel.currentUser
    val state by titlesViewModel.uiState.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let { titlesViewModel.fetchTitles(it.uid) }
    }

    when (val current = state) {
        is TitlesUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading…")
        }
        is TitlesUiState.Error -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(current.message)
        }
        is TitlesUiState.Success -> {
            val listState = rememberScalingLazyListState()
            ScalingLazyColumn(modifier = modifier.fillMaxSize(), state = listState) {
                item { ListHeader { Text("Your titles") } }
                items(current.titles, key = { it.id }) { title ->
                    Button(onClick = { onTitleClick(title) }, modifier = Modifier.fillMaxWidth()) {
                        Text(title.name)
                    }
                }
                item { Button(onClick = onDownloadsClick) { Text("Downloads") } }
            }
        }
    }
}
