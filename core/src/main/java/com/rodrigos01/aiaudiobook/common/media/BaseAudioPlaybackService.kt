package com.rodrigos01.aiaudiobook.common.media

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Shared ExoPlayer + MediaSession setup for the phone and watch playback services - both need an
 * identically configured player (offline file:// playback, foreground-service lifecycle), differing
 * only in which activity resumes the session.
 */
abstract class BaseAudioPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private var sessionPlayer: Player? = null

    protected abstract val sessionActivityClass: Class<out Activity>

    override fun onCreate() {
        super.onCreate()

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(30_000)
            .setReadTimeoutMs(30_000)
            .setAllowCrossProtocolRedirects(true)

        // Wrap the HTTP factory so file:// URIs (downloaded chapters played offline) are also
        // resolved correctly instead of being routed through the HTTP data source.
        val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(dataSourceFactory)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                Int.MAX_VALUE,
                1000,
                2000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()

        exoPlayer = player

        val intent = Intent(this, sessionActivityClass).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        sessionPlayer = createSessionPlayer(player)
        mediaSession = MediaSession.Builder(this, sessionPlayer!!)
            .setSessionActivity(pendingIntent)
            .build()
    }

    /**
     * The player the session drives. Defaults to the local [ExoPlayer]; subclasses can wrap it
     * (e.g. in a CastPlayer that hands playback to other output devices).
     */
    protected open fun createSessionPlayer(localPlayer: ExoPlayer): Player = localPlayer

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        // The session player may wrap the ExoPlayer (e.g. CastPlayer); release both.
        sessionPlayer?.release()
        sessionPlayer = null
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }
}
