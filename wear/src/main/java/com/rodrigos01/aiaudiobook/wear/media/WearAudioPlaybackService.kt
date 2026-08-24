package com.rodrigos01.aiaudiobook.wear.media

import com.rodrigos01.aiaudiobook.common.media.BaseAudioPlaybackService
import com.rodrigos01.aiaudiobook.wear.WearMainActivity

/**
 * The watch's own playback service - shares [BaseAudioPlaybackService] with the phone's
 * [com.rodrigos01.aiaudiobook.common.media.AudioPlaybackService], but runs as a separate
 * service/process on the watch so playback works standalone (own Bluetooth/network connection,
 * no phone required), per the Wear OS plan.
 */
class WearAudioPlaybackService : BaseAudioPlaybackService() {
    override val sessionActivityClass = WearMainActivity::class.java
}
