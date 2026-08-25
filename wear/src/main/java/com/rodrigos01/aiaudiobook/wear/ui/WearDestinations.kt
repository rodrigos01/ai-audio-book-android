package com.rodrigos01.aiaudiobook.wear.ui

import android.net.Uri

/**
 * String-route navigation destinations for the watch app. Uses classic string/argument routes
 * (rather than the phone's type-safe @Serializable Route) since androidx.wear.compose:compose-
 * navigation's SwipeDismissableNavHost operates on androidx.navigation.NavGraphBuilder via its own
 * String-route `composable(...)` extension, not the reified `composable<T>` one.
 */
object WearDestinations {
    const val ARG_TITLE_ID = "titleId"
    const val ARG_TITLE_NAME = "titleName"
    const val ARG_CHAPTER_ID = "chapterId"

    const val AUTH_GATE = "auth_gate"
    const val TITLES = "titles"
    const val DOWNLOADS = "downloads"
    const val CHAPTERS_TEMPLATE = "chapters/{$ARG_TITLE_ID}/{$ARG_TITLE_NAME}"
    const val CHAPTER_ACTION_TEMPLATE = "chapter_action/{$ARG_TITLE_ID}/{$ARG_CHAPTER_ID}"
    const val PLAYER_TEMPLATE = "player/{$ARG_TITLE_ID}/{$ARG_CHAPTER_ID}"

    fun chapters(titleId: String, titleName: String) = "chapters/$titleId/${Uri.encode(titleName)}"
    fun chapterAction(titleId: String, chapterId: String) = "chapter_action/$titleId/$chapterId"
    fun player(titleId: String, chapterId: String) = "player/$titleId/$chapterId"
}
