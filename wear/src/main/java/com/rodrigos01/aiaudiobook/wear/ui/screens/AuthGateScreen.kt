package com.rodrigos01.aiaudiobook.wear.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import com.rodrigos01.aiaudiobook.wear.auth.WearAuthUiState
import com.rodrigos01.aiaudiobook.wear.auth.WearAuthViewModel

@Composable
fun AuthGateScreen(authViewModel: WearAuthViewModel, modifier: Modifier = Modifier) {
    val state by authViewModel.uiState.collectAsState()

    // Try syncing automatically as soon as the gate is shown, so the user only has to tap
    // "sync with phone" manually if this attempt fails.
    LaunchedEffect(Unit) {
        if (state is WearAuthUiState.SignedOut) {
            authViewModel.requestSync()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val current = state) {
            is WearAuthUiState.SignedOut -> {
                Text("Sign in required", textAlign = TextAlign.Center)
                Button(onClick = { authViewModel.requestSync() }) {
                    Text("Sync with phone")
                }
            }
            is WearAuthUiState.Syncing -> Text("Syncing with phone…", textAlign = TextAlign.Center)
            is WearAuthUiState.Authenticated -> Text("Signed in")
            is WearAuthUiState.Error -> {
                Text(current.message, textAlign = TextAlign.Center)
                Button(onClick = { authViewModel.requestSync() }) {
                    Text("Retry")
                }
            }
        }
    }
}
