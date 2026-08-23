package com.rodrigos01.aiaudiobook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodrigos01.aiaudiobook.data.ApiRepository
import com.rodrigos01.aiaudiobook.data.Chapter
import com.rodrigos01.aiaudiobook.data.FirestoreRepository
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.data.Voice
import com.rodrigos01.aiaudiobook.data.offline.ChapterDownloadState
import com.rodrigos01.aiaudiobook.data.offline.OfflineDownloadRepository
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
    private val offlineDownloadRepository: OfflineDownloadRepository,
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

    // Per-chapter offline download state, recomputed whenever the chapter list changes.
    val downloadStates: StateFlow<Map<String, ChapterDownloadState>> = uiState
        .flatMapLatest { state ->
            val chapters = (state as? ChaptersUiState.Success)?.chapters.orEmpty()
            if (chapters.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(chapters.map { chapter ->
                    offlineDownloadRepository.observeDownloadState(chapter.id).map { chapter.id to it }
                }) { pairs -> pairs.toMap() }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Set when the user tapped download while not on Wi-Fi, awaiting confirmation to use
    // mobile data.
    var pendingMeteredDownloadChapter = MutableStateFlow<Chapter?>(null)
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

    /** Starts a download, or asks for confirmation first if [isOnWifi] is false. */
    fun requestDownload(chapter: Chapter, isOnWifi: Boolean) {
        if (isOnWifi) {
            offlineDownloadRepository.downloadChapter(chapter.id)
        } else {
            pendingMeteredDownloadChapter.value = chapter
        }
    }

    fun confirmMeteredDownload() {
        pendingMeteredDownloadChapter.value?.let { offlineDownloadRepository.downloadChapter(it.id) }
        pendingMeteredDownloadChapter.value = null
    }

    fun dismissMeteredDownloadConfirmation() {
        pendingMeteredDownloadChapter.value = null
    }

    fun deleteDownload(chapterId: String) {
        viewModelScope.launch {
            offlineDownloadRepository.deleteDownload(chapterId)
        }
    }

    var isFetchingDoc = MutableStateFlow(false)
        private set

    fun fetchGoogleDoc(
        documentId: String,
        googleAccessToken: String,
        onSuccess: (title: String, content: String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isFetchingDoc.value = true
            val result = apiRepository.fetchGoogleDoc(documentId, googleAccessToken)
            isFetchingDoc.value = false
            result.onSuccess { res ->
                onSuccess(res.title, res.content)
            }.onFailure { err ->
                onError(err.localizedMessage ?: "Failed to fetch Google Doc content.")
            }
        }
    }

    fun createChapter(
        titleId: String,
        name: String,
        content: String,
        voiceId: String,
        googleDocId: String? = null,
        googleAccessToken: String? = null
    ) {
        viewModelScope.launch {
            isSubmitting.value = true
            actionError.value = null
            val result = apiRepository.createChapter(
                titleId = titleId,
                name = name.ifBlank { null },
                content = content.ifBlank { null },
                voiceId = voiceId.ifBlank { null },
                googleDocId = googleDocId,
                googleAccessToken = googleAccessToken
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
                // Edited content means the server regenerates sections, so any previously
                // downloaded audio for this chapter is now stale.
                offlineDownloadRepository.deleteDownload(chapterId)
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
                offlineDownloadRepository.deleteDownload(chapterId)
                dismissDeleteConfirmation()
            }.onFailure { error ->
                actionError.value = error.localizedMessage ?: "Failed to delete chapter"
            }
        }
    }

    companion object {
        fun Factory(offlineDownloadRepository: OfflineDownloadRepository) = viewModelFactory {
            initializer {
                ChaptersViewModel(offlineDownloadRepository = offlineDownloadRepository)
            }
        }
    }
}

