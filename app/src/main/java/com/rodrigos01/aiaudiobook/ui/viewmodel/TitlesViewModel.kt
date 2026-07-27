package com.rodrigos01.aiaudiobook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigos01.aiaudiobook.data.FirestoreRepository
import com.rodrigos01.aiaudiobook.data.Title
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

sealed interface TitlesUiState {
    object Loading : TitlesUiState
    data class Success(val titles: List<Title>) : TitlesUiState
    data class Error(val message: String) : TitlesUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class TitlesViewModel(private val firestoreRepository: FirestoreRepository = FirestoreRepository()) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TitlesUiState> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            firestoreRepository.getTitles(userId)
                .map<List<Title>, TitlesUiState> { TitlesUiState.Success(it) }
                .catch { emit(TitlesUiState.Error(it.localizedMessage ?: "Failed to fetch titles")) }
                .onStart { emit(TitlesUiState.Loading) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TitlesUiState.Loading
        )

    fun fetchTitles(userId: String) {
        _userId.value = userId
    }
}
