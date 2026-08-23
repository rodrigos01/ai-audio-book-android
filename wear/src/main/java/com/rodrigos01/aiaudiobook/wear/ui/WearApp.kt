package com.rodrigos01.aiaudiobook.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.rodrigos01.aiaudiobook.common.media.MediaPlaybackService
import com.rodrigos01.aiaudiobook.data.offline.OfflineDownloadRepository
import com.rodrigos01.aiaudiobook.ui.viewmodel.ChaptersViewModel
import com.rodrigos01.aiaudiobook.ui.viewmodel.PlayerViewModel
import com.rodrigos01.aiaudiobook.ui.viewmodel.TitlesViewModel
import com.rodrigos01.aiaudiobook.wear.auth.WearAuthUiState
import com.rodrigos01.aiaudiobook.wear.auth.WearAuthViewModel
import com.rodrigos01.aiaudiobook.wear.media.WearAudioPlaybackService
import com.rodrigos01.aiaudiobook.wear.ui.screens.AuthGateScreen
import com.rodrigos01.aiaudiobook.wear.ui.screens.WearChapterActionScreen
import com.rodrigos01.aiaudiobook.wear.ui.screens.WearChaptersScreen
import com.rodrigos01.aiaudiobook.wear.ui.screens.WearDownloadsScreen
import com.rodrigos01.aiaudiobook.wear.ui.screens.WearPlayerScreen
import com.rodrigos01.aiaudiobook.wear.ui.screens.WearTitlesScreen

@Composable
fun WearApp() {
    val context = LocalContext.current.applicationContext

    val offlineDownloadRepository = remember { OfflineDownloadRepository(context) }
    val playbackService = remember { MediaPlaybackService(context, WearAudioPlaybackService::class.java) }
    DisposableEffect(Unit) {
        onDispose { playbackService.release() }
    }

    val authViewModel: WearAuthViewModel = viewModel(factory = WearAuthViewModel.Factory(context))
    val titlesViewModel: TitlesViewModel = viewModel(factory = TitlesViewModel.Factory(offlineDownloadRepository))
    val chaptersViewModel: ChaptersViewModel = viewModel(factory = ChaptersViewModel.Factory(offlineDownloadRepository))

    val navController = rememberSwipeDismissableNavController()
    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is WearAuthUiState.Authenticated) {
            val current = navController.currentBackStackEntry?.destination?.route
            if (current == null || current == WearDestinations.AUTH_GATE) {
                navController.navigate(WearDestinations.TITLES) {
                    popUpTo(WearDestinations.AUTH_GATE) { inclusive = true }
                }
            }
        }
    }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = if (authViewModel.isAuthenticated) WearDestinations.TITLES else WearDestinations.AUTH_GATE
    ) {
        composable(WearDestinations.AUTH_GATE) {
            AuthGateScreen(authViewModel = authViewModel)
        }

        composable(WearDestinations.TITLES) {
            WearTitlesScreen(
                authViewModel = authViewModel,
                titlesViewModel = titlesViewModel,
                onTitleClick = { title ->
                    navController.navigate(WearDestinations.chapters(title.id, title.name))
                },
                onDownloadsClick = { navController.navigate(WearDestinations.DOWNLOADS) }
            )
        }

        composable(WearDestinations.DOWNLOADS) {
            WearDownloadsScreen(offlineDownloadRepository = offlineDownloadRepository)
        }

        composable(
            route = WearDestinations.CHAPTERS_TEMPLATE,
            arguments = listOf(
                navArgument(WearDestinations.ARG_TITLE_ID) {},
                navArgument(WearDestinations.ARG_TITLE_NAME) {}
            )
        ) { backStackEntry ->
            val titleId = backStackEntry.arguments?.getString(WearDestinations.ARG_TITLE_ID).orEmpty()
            WearChaptersScreen(
                titleId = titleId,
                chaptersViewModel = chaptersViewModel,
                onChapterClick = { chapter ->
                    navController.navigate(WearDestinations.chapterAction(titleId, chapter.id))
                }
            )
        }

        composable(
            route = WearDestinations.CHAPTER_ACTION_TEMPLATE,
            arguments = listOf(
                navArgument(WearDestinations.ARG_TITLE_ID) {},
                navArgument(WearDestinations.ARG_CHAPTER_ID) {}
            )
        ) { backStackEntry ->
            val titleId = backStackEntry.arguments?.getString(WearDestinations.ARG_TITLE_ID).orEmpty()
            val chapterId = backStackEntry.arguments?.getString(WearDestinations.ARG_CHAPTER_ID).orEmpty()
            WearChapterActionScreen(
                chapterId = chapterId,
                chaptersViewModel = chaptersViewModel,
                onPlayClick = { navController.navigate(WearDestinations.player(titleId, chapterId)) }
            )
        }

        composable(
            route = WearDestinations.PLAYER_TEMPLATE,
            arguments = listOf(
                navArgument(WearDestinations.ARG_TITLE_ID) {},
                navArgument(WearDestinations.ARG_CHAPTER_ID) {}
            )
        ) { backStackEntry ->
            val titleId = backStackEntry.arguments?.getString(WearDestinations.ARG_TITLE_ID).orEmpty()
            val chapterId = backStackEntry.arguments?.getString(WearDestinations.ARG_CHAPTER_ID).orEmpty()
            val playerViewModel: PlayerViewModel = viewModel(
                factory = PlayerViewModel.Factory(titleId, chapterId, playbackService, offlineDownloadRepository)
            )
            WearPlayerScreen(playerViewModel = playerViewModel)
            DisposableEffect(Unit) {
                onDispose { playerViewModel.onExit() }
            }
        }
    }
}
