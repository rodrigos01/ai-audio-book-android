package com.rodrigos01.aiaudiobook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodrigos01.aiaudiobook.data.ApiRepository
import com.rodrigos01.aiaudiobook.data.FirestoreRepository
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.data.offline.OfflineDownloadRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface TitlesUiState {
    object Loading : TitlesUiState
    data class Success(val titles: List<Title>) : TitlesUiState
    data class Error(val message: String) : TitlesUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class TitlesViewModel(
    private val offlineDownloadRepository: OfflineDownloadRepository,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val apiRepository: ApiRepository = ApiRepository()
) : ViewModel() {

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

    var isBottomSheetOpen = MutableStateFlow(false)
        private set

    var editingTitle = MutableStateFlow<Title?>(null)
        private set

    var isSubmitting = MutableStateFlow(false)
        private set

    var actionError = MutableStateFlow<String?>(null)
        private set

    var titleToDelete = MutableStateFlow<Title?>(null)
        private set

    fun fetchTitles(userId: String) {
        _userId.value = userId
    }

    fun showCreateBottomSheet() {
        editingTitle.value = null
        actionError.value = null
        isBottomSheetOpen.value = true
    }

    fun showEditBottomSheet(title: Title) {
        editingTitle.value = title
        actionError.value = null
        isBottomSheetOpen.value = true
    }

    fun dismissBottomSheet() {
        isBottomSheetOpen.value = false
        editingTitle.value = null
        actionError.value = null
    }

    fun showDeleteConfirmation(title: Title) {
        titleToDelete.value = title
    }

    fun dismissDeleteConfirmation() {
        titleToDelete.value = null
    }

    fun createTitle(name: String, aiCastingEnabled: Boolean, ttsTier: String) {
        viewModelScope.launch {
            isSubmitting.value = true
            actionError.value = null
            val result = apiRepository.createTitle(
                name = name,
                aiCastingEnabled = aiCastingEnabled,
                ttsTier = ttsTier
            )
            isSubmitting.value = false
            result.onSuccess {
                dismissBottomSheet()
            }.onFailure { error ->
                actionError.value = error.localizedMessage ?: "Failed to create title"
            }
        }
    }

    fun updateTitle(id: String, name: String) {
        viewModelScope.launch {
            isSubmitting.value = true
            actionError.value = null
            val result = apiRepository.updateTitle(id = id, name = name)
            isSubmitting.value = false
            result.onSuccess {
                dismissBottomSheet()
            }.onFailure { error ->
                actionError.value = error.localizedMessage ?: "Failed to update title"
            }
        }
    }

    fun deleteTitle(id: String) {
        viewModelScope.launch {
            isSubmitting.value = true
            // Capture chapter ids before the title (and its chapters) are gone server-side, so
            // any offline downloads for them can be cleaned up too.
            val chapterIds = runCatching { firestoreRepository.getChapters(id).first().map { it.id } }
                .getOrDefault(emptyList())
            val result = apiRepository.deleteTitle(id)
            isSubmitting.value = false
            result.onSuccess {
                chapterIds.forEach { chapterId -> offlineDownloadRepository.deleteDownload(chapterId) }
                dismissDeleteConfirmation()
            }.onFailure { error ->
                actionError.value = error.localizedMessage ?: "Failed to delete title"
            }
        }
    }

    companion object {
        fun Factory(offlineDownloadRepository: OfflineDownloadRepository) = viewModelFactory {
            initializer {
                TitlesViewModel(offlineDownloadRepository = offlineDownloadRepository)
            }
        }
    }
}

