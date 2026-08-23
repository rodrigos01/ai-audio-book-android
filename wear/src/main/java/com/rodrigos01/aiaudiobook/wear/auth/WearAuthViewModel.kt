package com.rodrigos01.aiaudiobook.wear.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseUser
import com.rodrigos01.aiaudiobook.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed interface WearAuthUiState {
    object SignedOut : WearAuthUiState
    object Syncing : WearAuthUiState
    object Authenticated : WearAuthUiState
    data class Error(val message: String) : WearAuthUiState
}

private const val PAIRING_TIMEOUT_MS = 10_000L

class WearAuthViewModel(
    private val wearAuthSyncClient: WearAuthSyncClient,
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WearAuthUiState>(
        if (authRepository.currentUser != null) WearAuthUiState.Authenticated else WearAuthUiState.SignedOut
    )
    val uiState: StateFlow<WearAuthUiState> = _uiState.asStateFlow()

    val isAuthenticated: Boolean
        get() = authRepository.currentUser != null

    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    /** Asks the phone to relay a sign-in token; call from [com.rodrigos01.aiaudiobook.wear.ui.screens.AuthGateScreen]. */
    fun requestSync() {
        _uiState.value = WearAuthUiState.Syncing
        viewModelScope.launch {
            wearAuthSyncClient.requestPairing().onFailure { error ->
                _uiState.value = WearAuthUiState.Error(error.localizedMessage ?: "Could not reach phone")
                return@launch
            }

            val result = withTimeoutOrNull(PAIRING_TIMEOUT_MS) { WearAuthResultBus.results.first() }
            val error = result?.error
            _uiState.value = when {
                result == null -> WearAuthUiState.Error("Timed out waiting for phone. Make sure it's nearby and signed in.")
                error != null -> WearAuthUiState.Error(error)
                else -> WearAuthUiState.Authenticated
            }
        }
    }

    companion object {
        fun Factory(context: Context) = viewModelFactory {
            initializer {
                WearAuthViewModel(WearAuthSyncClient(context))
            }
        }
    }
}
