package com.rodrigos01.aiaudiobook.ui.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodrigos01.aiaudiobook.data.ApiRepository
import com.rodrigos01.aiaudiobook.data.FirestoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class PlayerUiState(
    val title: String?,
    val mediaURI: Uri,
)

class PlayerViewModel(
    private val titleId: String,
    private val chapterId: String,
    private val apiRepository: ApiRepository = ApiRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = firestoreRepository.getChapters(titleId)
        .map { chapters -> chapters.first { it.id == chapterId } }.map { chapter ->
            PlayerUiState(
                title = chapter.name,
                mediaURI = "https://ai-audio-book-api-883622140264.us-central1.run.app/api/chapters/${chapter.id}/stream".toUri()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PlayerUiState("", Uri.EMPTY))

    companion object {
        fun Factory(titleId: String, chapterId: String) =
            viewModelFactory { initializer { PlayerViewModel(titleId, chapterId) } }
    }
}