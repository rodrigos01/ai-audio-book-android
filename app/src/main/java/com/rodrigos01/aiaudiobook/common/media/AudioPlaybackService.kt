package com.rodrigos01.aiaudiobook.common.media

import android.util.Log
import androidx.media3.cast.CastPlayer
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.rodrigos01.aiaudiobook.MainActivity

class AudioPlaybackService : BaseAudioPlaybackService() {
    override val sessionActivityClass = MainActivity::class.java

    /**
     * CastPlayer owns the local ExoPlayer and moves playback between it and whichever output the
     * user picks in the system Output Switcher (this phone, Bluetooth, a Cast device). Cast is
     * optional, so if it can't be set up we just play locally.
     */
    override fun createSessionPlayer(localPlayer: ExoPlayer): Player =
        runCatching { CastPlayer.Builder(this).setLocalPlayer(localPlayer).build() }
            .onFailure { Log.w("AudioPlaybackService", "Cast unavailable, playing locally", it) }
            .getOrDefault(localPlayer)
}
