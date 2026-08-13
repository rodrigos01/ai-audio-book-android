package com.rodrigos01.aiaudiobook

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.firebase.FirebaseApp
import com.rodrigos01.aiaudiobook.common.media.MediaPlaybackService
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.data.offline.OfflineDownloadRepository
import com.rodrigos01.aiaudiobook.theme.AIAudioBookTheme
import com.rodrigos01.aiaudiobook.ui.screens.ChaptersScreen
import com.rodrigos01.aiaudiobook.ui.screens.LoginScreen
import com.rodrigos01.aiaudiobook.ui.screens.PlayerScreen
import com.rodrigos01.aiaudiobook.ui.screens.TitlesScreen
import com.rodrigos01.aiaudiobook.ui.viewmodel.AuthUiState
import com.rodrigos01.aiaudiobook.ui.viewmodel.AuthViewModel
import com.rodrigos01.aiaudiobook.ui.viewmodel.ChaptersViewModel
import com.rodrigos01.aiaudiobook.ui.viewmodel.PlayerViewModel
import com.rodrigos01.aiaudiobook.ui.viewmodel.TitlesViewModel
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    object Login : Route

    @Serializable
    object Titles : Route

    @Serializable
    data class Chapters(val titleId: String, val titleName: String) : Route

    @Serializable
    data class Player(val titleId: String, val chapterId: String)
}

class MainActivity : ComponentActivity() {

    private val offlineDownloadRepository by lazy { OfflineDownloadRepository(this) }

    private val authViewModel: AuthViewModel by viewModels()
    private val titlesViewModel: TitlesViewModel by viewModels {
        TitlesViewModel.Factory(offlineDownloadRepository)
    }
    private val chaptersViewModel: ChaptersViewModel by viewModels {
        ChaptersViewModel.Factory(offlineDownloadRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT // Transparent allows your app background to shine through
            ), navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            AIAudioBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current.applicationContext
                    val playbackService = remember { MediaPlaybackService(context) }
                    DisposableEffect(Unit) {
                        onDispose { playbackService.release() }
                    }

                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { _ -> }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    val navController = rememberNavController()
                    val authState by authViewModel.uiState.collectAsState()

                    // Sync screen navigation with authentication state
                    LaunchedEffect(authState) {
                        val currentDestination = navController.currentBackStackEntry?.destination

                        if (authState is AuthUiState.Authenticated) {
                            if (currentDestination?.route?.contains("Titles") != true) {
                                navController.navigate(Route.Titles) {
                                    popUpTo(Route.Login) { inclusive = true }
                                }
                            }
                        } else if (authState is AuthUiState.Unauthenticated) {
                            if (currentDestination?.route?.contains("Login") != true) {
                                navController.navigate(Route.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = if (authViewModel.currentUser != null) Route.Titles else Route.Login
                    ) {
                        composable<Route.Login> {
                            LoginScreen(
                                authViewModel = authViewModel, modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable<Route.Titles> {
                            TitlesScreen(
                                authViewModel = authViewModel,
                                titlesViewModel = titlesViewModel,
                                onTitleClick = { title ->
                                    navController.navigate(
                                        Route.Chapters(
                                            titleId = title.id, titleName = title.name
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable<Route.Chapters> { backStackEntry ->
                            val route = backStackEntry.toRoute<Route.Chapters>()
                            val title = Title(id = route.titleId, name = route.titleName)
                            ChaptersScreen(
                                title = title,
                                chaptersViewModel = chaptersViewModel,
                                onBackClick = {
                                    chaptersViewModel.clearChapters()
                                    navController.popBackStack()
                                },
                                onChapterClick = { chapter ->
                                    navController.navigate(
                                        Route.Player(
                                            titleId = route.titleId, chapterId = chapter.id
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable<Route.Player> { backStackEntry ->
                            val route = backStackEntry.toRoute<Route.Player>()
                            val viewModel: PlayerViewModel = viewModel(
                                factory = PlayerViewModel.Factory(
                                    route.titleId, route.chapterId, playbackService, offlineDownloadRepository,
                                )
                            )
                            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                            PlayerScreen(
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize(),
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onSeek = viewModel::onSeek,
                                onPreviousClick = viewModel::onPrevious,
                                onNextClick = viewModel::onNext,
                                onPlayPauseClick = viewModel::onPlayPause,
                            )
                            DisposableEffect(Unit) {
                                onDispose {
                                    viewModel.onExit()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
