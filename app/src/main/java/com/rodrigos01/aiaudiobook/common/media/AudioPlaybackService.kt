package com.rodrigos01.aiaudiobook.common.media

import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import com.google.android.gms.cast.framework.CastContext
import com.rodrigos01.aiaudiobook.MainActivity

class AudioPlaybackService : BaseAudioPlaybackService() {
    override val sessionActivityClass = MainActivity::class.java

    private var castPlayer: CastPlayer? = null

    override fun onSessionCreated(session: MediaSession, localPlayer: Player) {
        val cast = runCatching { CastPlayer(CastContext.getSharedInstance(this)) }.getOrNull() ?: return
        castPlayer = cast
        cast.setSessionAvailabilityListener(object : SessionAvailabilityListener {
            override fun onCastSessionAvailable() = switchPlayer(session, localPlayer, cast)
            override fun onCastSessionUnavailable() = switchPlayer(session, cast, localPlayer)
        })
        if (cast.isCastSessionAvailable) switchPlayer(session, localPlayer, cast)
    }

    /** Moves the current item, position and play state from [from] to [to]. */
    private fun switchPlayer(session: MediaSession, from: Player, to: Player) {
        if (session.player === to) return
        val items = List(from.mediaItemCount) { from.getMediaItemAt(it) }
        val index = from.currentMediaItemIndex
        val position = from.currentPosition
        val playWhenReady = from.playWhenReady
        from.stop()
        from.clearMediaItems()
        session.player = to
        if (items.isNotEmpty()) {
            to.setMediaItems(items, index, position)
            to.playWhenReady = playWhenReady
            to.prepare()
        }
    }

    override fun onDestroy() {
        castPlayer?.setSessionAvailabilityListener(null)
        castPlayer?.release()
        castPlayer = null
        super.onDestroy()
    }
}
