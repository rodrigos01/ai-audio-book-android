package com.rodrigos01.aiaudiobook.common.media

import com.rodrigos01.aiaudiobook.MainActivity

class AudioPlaybackService : BaseAudioPlaybackService() {
    override val sessionActivityClass = MainActivity::class.java
}
