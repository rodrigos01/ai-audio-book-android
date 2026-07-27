package com.rodrigos01.aiaudiobook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.rodrigos01.aiaudiobook.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Unauthenticated : AuthUiState
    object Loading : AuthUiState
    data class Authenticated(val user: FirebaseUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(
        authRepository.currentUser?.let { AuthUiState.Authenticated(it) } ?: AuthUiState.Unauthenticated
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun login(email: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.loginWithEmail(email, password).fold(
                onSuccess = { authResult ->
                    _uiState.value = AuthUiState.Authenticated(authResult.user!!)
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState.Error(exception.localizedMessage ?: "Login failed.")
                }
            )
        }
    }

    fun register(email: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.registerWithEmail(email, password).fold(
                onSuccess = { authResult ->
                    _uiState.value = AuthUiState.Authenticated(authResult.user!!)
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState.Error(exception.localizedMessage ?: "Registration failed.")
                }
            )
        }
    }

    fun loginWithGoogle(context: android.content.Context) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val webClientId = "883622140264-bvabb03s21b4vq0hul9jlo73f7h4vj5l.apps.googleusercontent.com"
            authRepository.signInWithGoogle(context, webClientId).fold(
                onSuccess = { authResult ->
                    _uiState.value = AuthUiState.Authenticated(authResult.user!!)
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState.Error(exception.localizedMessage ?: "Google Sign-In failed.")
                }
            )
        }
    }

    fun signOut(context: android.content.Context) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.signOut(context)
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }
}
