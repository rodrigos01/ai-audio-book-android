package com.rodrigos01.aiaudiobook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigos01.aiaudiobook.data.FirestoreRepository
import com.rodrigos01.aiaudiobook.data.Chapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

sealed interface ChaptersUiState {
    object Idle : ChaptersUiState
    object Loading : ChaptersUiState
    data class Success(val chapters: List<Chapter>) : ChaptersUiState
    data class Error(val message: String) : ChaptersUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class ChaptersViewModel(private val firestoreRepository: FirestoreRepository = FirestoreRepository()) : ViewModel() {

    private val _titleId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChaptersUiState> = _titleId
        .flatMapLatest { titleId ->
            if (titleId == null) {
                flowOf(ChaptersUiState.Idle)
            } else {
                firestoreRepository.getChapters(titleId)
                    .map<List<Chapter>, ChaptersUiState> { ChaptersUiState.Success(it) }
                    .catch { emit(ChaptersUiState.Error(it.localizedMessage ?: "Failed to fetch chapters")) }
                    .onStart { emit(ChaptersUiState.Loading) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChaptersUiState.Idle
        )

    fun fetchChapters(titleId: String) {
        _titleId.value = titleId
    }

    fun clearChapters() {
        _titleId.value = null
    }
}
