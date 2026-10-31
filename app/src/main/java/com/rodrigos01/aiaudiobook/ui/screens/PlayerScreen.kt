package com.rodrigos01.aiaudiobook.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.fragment.app.FragmentActivity
import androidx.mediarouter.app.MediaRouteChooserDialogFragment
import androidx.mediarouter.app.MediaRouteControllerDialogFragment
import androidx.mediarouter.app.SystemOutputSwitcherDialogController
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import com.google.android.gms.cast.CastMediaControlIntent
import androidx.mediarouter.media.MediaRouterParams
import androidx.media3.common.util.UnstableApi
import com.rodrigos01.aiaudiobook.common.media.PlaybackStatus
import com.rodrigos01.aiaudiobook.theme.AIAudioBookTheme
import com.rodrigos01.aiaudiobook.ui.toDurationString
import com.rodrigos01.aiaudiobook.ui.viewmodel.PlayerUiState
import com.rodrigos01.aiaudiobook.ui.viewmodel.PlayerViewModel
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    PlayerScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onPlayPauseClick = playerViewModel::onPlayPause,
        onSeek = playerViewModel::onSeek,
        onPreviousClick = playerViewModel::onPrevious,
        onNextClick = playerViewModel::onNext,
        modifier = modifier
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    uiState: PlayerUiState,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = { CastButton() },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val availableWidth = constraints.maxWidth
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    uiState.chapterName.orEmpty(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    uiState.titleName.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiState.isOffline) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DownloadDone,
                            contentDescription = "Playing offline",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Playing offline",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                var sliderPosition by remember(uiState.playbackProgress) {
                    mutableFloatStateOf(
                        uiState.playbackProgress
                    )
                }
                var isDragging by remember { mutableStateOf(false) }
                var dragOffset by remember { mutableFloatStateOf(0F) }
                val progress = if (isDragging) dragOffset / availableWidth else sliderPosition
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val progressOffset = availableWidth * sliderPosition
                    val anchorSize by animateDpAsState(if (isDragging) 16.dp else 8.dp)
                    LinearWavyProgressIndicator(
                        progress = { progress },
                        amplitude = {
                            when (uiState.playbackStatus) {
                                PlaybackStatus.PLAYING -> 1F
                                else -> 0F
                            }
                        },
                        modifier = Modifier.fillMaxWidth().draggable(
                            state = rememberDraggableState { delta ->
                                dragOffset += delta
                            },
                            orientation = Orientation.Horizontal,
                            startDragImmediately = true,
                            onDragStarted = { startOffset ->
                                isDragging = true
                                dragOffset = startOffset.x
                            },
                            onDragStopped = {
                                isDragging = false
                                sliderPosition = dragOffset / availableWidth
                                onSeek(sliderPosition)
                            }
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .size(anchorSize)
                            .offset {
                                IntOffset(
                                    x = (if (isDragging) dragOffset else progressOffset).roundToInt(),
                                    y = 0,
                                )
                            }
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape,
                            )

                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val positionString = if (sliderPosition > 0) {
                        uiState.duration * progress.toDouble()
                    } else {
                        uiState.position
                    }.toDurationString()
                    Text(
                        positionString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        uiState.duration.toDurationString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onPreviousClick) {
                        Icon(
                            Icons.Default.SkipPrevious, contentDescription = "Previous",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    when (uiState.playbackStatus) {
                        PlaybackStatus.PAUSED -> PlayPauseButton(
                            Icons.Default.PlayArrow, onPlayPauseClick
                        )

                        PlaybackStatus.BUFFERING -> CircularProgressIndicator(modifier = Modifier.size(72.dp))
                        else -> PlayPauseButton(Icons.Default.Pause, onPlayPauseClick)
                    }
                    IconButton(onClick = onNextClick) {
                        Icon(
                            Icons.Default.SkipNext, contentDescription = "Previous",
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }
        }
    }
}

private const val OUTPUT_PICKER_TAG = "output_picker"

/** Where audio is currently going, as reported by the media router. */
private data class OutputRoute(val name: String, val deviceType: Int, val isLocal: Boolean, val isBluetooth: Boolean)

private fun OutputRoute.icon(): ImageVector = when {
    isLocal -> Icons.Default.Devices
    isBluetooth -> Icons.Default.BluetoothAudio
    else -> when (deviceType) {
        RouteInfo.DEVICE_TYPE_TV, RouteInfo.DEVICE_TYPE_HDMI, RouteInfo.DEVICE_TYPE_HDMI_ARC,
        RouteInfo.DEVICE_TYPE_HDMI_EARC, RouteInfo.DEVICE_TYPE_AUDIO_VIDEO_RECEIVER -> Icons.Default.Tv

        RouteInfo.DEVICE_TYPE_SPEAKER, RouteInfo.DEVICE_TYPE_REMOTE_SPEAKER,
        RouteInfo.DEVICE_TYPE_GROUP -> Icons.Default.Speaker

        RouteInfo.DEVICE_TYPE_WIRED_HEADSET, RouteInfo.DEVICE_TYPE_WIRED_HEADPHONES,
        RouteInfo.DEVICE_TYPE_USB_HEADSET, RouteInfo.DEVICE_TYPE_BLE_HEADSET,
        RouteInfo.DEVICE_TYPE_HEARING_AID -> Icons.Default.Headphones

        RouteInfo.DEVICE_TYPE_CAR -> Icons.Default.DirectionsCar
        RouteInfo.DEVICE_TYPE_COMPUTER -> Icons.Default.Computer
        RouteInfo.DEVICE_TYPE_SMARTWATCH -> Icons.Default.Watch
        else -> Icons.Default.CastConnected
    }
}

private fun RouteInfo.toOutputRoute() = OutputRoute(name, deviceType, isDefault, isBluetooth)

/**
 * "Devices" button: shows where audio is playing (this phone, a Bluetooth device, a Cast device...)
 * and opens the system Output Switcher to change it. Hidden if Cast can't initialize (e.g. no
 * Play Services).
 */
@Composable
fun CastButton(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) {
        Icon(Icons.Default.Devices, contentDescription = "Output", modifier = modifier.padding(12.dp))
        return
    }
    val context = LocalContext.current
    val castContext = remember(context) {
        runCatching { CastContext.getSharedInstance(context) }
            .onFailure { Log.w("CastButton", "Cast unavailable", it) }
            .getOrNull()
    } ?: return
    val selector = remember(castContext) {
        castContext.mergedSelector ?: MediaRouteSelector.Builder()
            .addControlCategory(
                CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
            )
            .build()
    }
    val route = rememberSelectedOutputRoute(selector)

    IconButton(onClick = { openOutputPicker(context, selector) }, modifier = modifier) {
        Icon(
            imageVector = route.icon(),
            contentDescription = "Playing on ${route.name}. Change output",
            tint = if (route.isLocal) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * The router's currently selected output, kept up to date. Asks for active discovery while on
 * screen (it is otherwise only passive).
 */
@Composable
private fun rememberSelectedOutputRoute(selector: MediaRouteSelector): OutputRoute {
    val context = LocalContext.current
    val router = remember(context) { MediaRouter.getInstance(context) }
    var route by remember { mutableStateOf(router.selectedRoute.toOutputRoute()) }

    DisposableEffect(router, selector) {
        // The router only opens the system switcher (instead of the legacy dialog) when it's asked to.
        val params = router.routerParams?.let { MediaRouterParams.Builder(it) } ?: MediaRouterParams.Builder()
        router.routerParams = params.setOutputSwitcherEnabled(true).setTransferToLocalEnabled(true).build()

        fun refresh() {
            route = router.selectedRoute.toOutputRoute()
        }

        val callback = object : MediaRouter.Callback() {
            override fun onRouteAdded(router: MediaRouter, route: RouteInfo) = refresh()
            override fun onRouteRemoved(router: MediaRouter, route: RouteInfo) = refresh()
            override fun onRouteChanged(router: MediaRouter, route: RouteInfo) = refresh()
            override fun onRouteSelected(router: MediaRouter, route: RouteInfo, reason: Int) = refresh()
            override fun onRouteUnselected(router: MediaRouter, route: RouteInfo, reason: Int) = refresh()
        }
        router.addCallback(selector, callback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
        refresh()
        onDispose { router.removeCallback(callback) }
    }
    return route
}

/** Opens the system Output Switcher, or the legacy Cast dialogs on devices that don't have it. */
private fun openOutputPicker(context: Context, selector: MediaRouteSelector) {
    if (SystemOutputSwitcherDialogController.showDialog(context)) return
    val activity = context.findFragmentActivity() ?: return
    val fragmentManager = activity.supportFragmentManager
    if (fragmentManager.findFragmentByTag(OUTPUT_PICKER_TAG) != null) return
    val connected = !MediaRouter.getInstance(context).selectedRoute.isDefaultOrBluetooth
    val dialog = if (connected) {
        MediaRouteControllerDialogFragment()
    } else {
        MediaRouteChooserDialogFragment().also { it.routeSelector = selector }
    }
    dialog.show(fragmentManager, OUTPUT_PICKER_TAG)
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

@Composable
fun PlayPauseButton(imageVector: ImageVector, onPlayPauseClick: () -> Unit) {
    FilledIconButton(
        onClick = onPlayPauseClick, modifier = Modifier.size(72.dp)
    ) {
        Icon(imageVector, contentDescription = "Play/Pause", modifier = Modifier.size(32.dp))
    }
}

@Composable
@PreviewLightDark
fun PlayerScreenPreview() {
    AIAudioBookTheme {
        PlayerScreen(
            PlayerUiState(
                "Chapter 1",
                "A Book",
                PlaybackStatus.PLAYING,
                0.3F,
                43.seconds,
                3.minutes + 43.seconds
            ),
            onBackClick = {},
            onPlayPauseClick = {},
            onSeek = {},
            onPreviousClick = {},
            onNextClick = {}
        )
    }
}

@Composable
@PreviewLightDark
fun PlayerScreenPausedPreview() {
    AIAudioBookTheme {
        PlayerScreen(
            PlayerUiState(
                "Chapter 2: The Journey",
                "Great Adventure",
                PlaybackStatus.PAUSED,
                0.6F,
                2.minutes + 15.seconds,
                4.minutes
            ),
            onBackClick = {},
            onPlayPauseClick = {},
            onSeek = {},
            onPreviousClick = {},
            onNextClick = {}
        )
    }
}

@Composable
@PreviewLightDark
fun PlayerScreenOfflinePreview() {
    AIAudioBookTheme {
        PlayerScreen(
            PlayerUiState(
                "Chapter 3: Offline Mode",
                "Great Adventure",
                PlaybackStatus.PLAYING,
                0.1F,
                30.seconds,
                5.minutes,
                isOffline = true
            ),
            onBackClick = {},
            onPlayPauseClick = {},
            onSeek = {},
            onPreviousClick = {},
            onNextClick = {}
        )
    }
}

@Composable
@PreviewLightDark
fun PlayerScreenLoadingPreview() {
    AIAudioBookTheme {
        PlayerScreen(
            PlayerUiState(
                "Chapter 3: Offline Mode",
                "Great Adventure",
                PlaybackStatus.BUFFERING,
                0.0F,
                0.seconds,
                5.minutes,
            ),
            onBackClick = {},
            onPlayPauseClick = {},
            onSeek = {},
            onPreviousClick = {},
            onNextClick = {}
        )
    }
}