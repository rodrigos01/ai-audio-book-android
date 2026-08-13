package com.rodrigos01.aiaudiobook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigos01.aiaudiobook.data.ApiRepository
import com.rodrigos01.aiaudiobook.data.Chapter
import com.rodrigos01.aiaudiobook.data.FirestoreRepository
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.data.Voice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ChaptersUiState {
    object Idle : ChaptersUiState
    object Loading : ChaptersUiState
    data class Success(val chapters: List<Chapter>) : ChaptersUiState
    data class Error(val message: String) : ChaptersUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class ChaptersViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val apiRepository: ApiRepository = ApiRepository()
) : ViewModel() {

    private val _titleId = MutableStateFlow<String?>(null)

    // Observes the full Title document from Firestore, since navigation only carries the
    // title id/name and would otherwise leave fields like ai_casting_enabled at their defaults.
    val title: StateFlow<Title?> = _titleId
        .flatMapLatest { titleId ->
            if (titleId == null) {
                flowOf<Title?>(null)
            } else {
                firestoreRepository.getTitle(titleId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

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

    var isBottomSheetOpen = MutableStateFlow(false)
        private set

    var editingChapter = MutableStateFlow<Chapter?>(null)
        private set

    var isSubmitting = MutableStateFlow(false)
        private set

    var actionError = MutableStateFlow<String?>(null)
        private set

    var chapterToDelete = MutableStateFlow<Chapter?>(null)
        private set

    var voices = MutableStateFlow<List<Voice>>(emptyList())
        private set

    var isLoadingVoices = MutableStateFlow(false)
        private set

    fun fetchChapters(titleId: String) {
        _titleId.value = titleId
    }

    fun clearChapters() {
        _titleId.value = null
    }

    fun loadVoices() {
        if (voices.value.isNotEmpty()) return
        viewModelScope.launch {
            isLoadingVoices.value = true
            val result = apiRepository.getVoices()
            isLoadingVoices.value = false
            result.onSuccess { voiceList ->
                voices.value = voiceList
            }
        }
    }

    fun showCreateBottomSheet(aiCastingEnabled: Boolean) {
        editingChapter.value = null
        actionError.value = null
        isBottomSheetOpen.value = true
        if (!aiCastingEnabled) {
            loadVoices()
        }
    }

    fun showEditBottomSheet(chapter: Chapter) {
        editingChapter.value = chapter
        actionError.value = null
        isBottomSheetOpen.value = true
    }

    fun dismissBottomSheet() {
        isBottomSheetOpen.value = false
        editingChapter.value = null
        actionError.value = null
    }

    fun showDeleteConfirmation(chapter: Chapter) {
        chapterToDelete.value = chapter
    }

    fun dismissDeleteConfirmation() {
        chapterToDelete.value = null
    }

    fun createChapter(titleId: String, name: String, content: String, voiceId: String) {
        viewModelScope.launch {
            isSubmitting.value = true
            actionError.value = null
            val result = apiRepository.createChapter(
                titleId = titleId,
                name = name,
                content = content,
                voiceId = voiceId
            )
            isSubmitting.value = false
            result.onSuccess {
                dismissBottomSheet()
            }.onFailure { error ->
                actionError.value = error.localizedMessage ?: "Failed to create chapter"
            }
        }
    }

    fun updateChapter(chapterId: String, name: String, content: String) {
        viewModelScope.launch {
            isSubmitting.value = true
            actionError.value = null
            val result = apiRepository.updateChapter(
                chapterId = chapterId,
                name = name,
                content = content
            )
            isSubmitting.value = false
            result.onSuccess {
                dismissBottomSheet()
            }.onFailure { error ->
                actionError.value = error.localizedMessage ?: "Failed to update chapter"
            }
        }
    }

    fun deleteChapter(chapterId: String) {
        viewModelScope.launch {
            isSubmitting.value = true
            val result = apiRepository.deleteChapter(chapterId)
            isSubmitting.value = false
            result.onSuccess {
                dismissDeleteConfirmation()
            }.onFailure { error ->
                actionError.value = error.localizedMessage ?: "Failed to delete chapter"
            }
        }
    }
}

