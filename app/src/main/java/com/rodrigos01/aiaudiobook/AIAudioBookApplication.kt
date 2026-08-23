package com.rodrigos01.aiaudiobook

import android.app.Application
import com.rodrigos01.aiaudiobook.core.CoreConfig

class AIAudioBookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreConfig.serverUrl = BuildConfig.SERVER_URL
    }
}
