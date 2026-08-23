package com.rodrigos01.aiaudiobook.wear

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.android.gms.wearable.Wearable
import com.rodrigos01.aiaudiobook.core.CoreConfig
import com.rodrigos01.aiaudiobook.wear.auth.WearAuthResultHandler

class WearApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreConfig.serverUrl = BuildConfig.SERVER_URL

        // Now that :wear shares :app's applicationId (required for the Wearable Data Layer API
        // to route messages between them - see wear/build.gradle.kts), the phone's existing
        // Firebase Android app registration and google-services.json cover this module too, so
        // the google-services plugin generates the resources FirebaseApp needs here directly.
        FirebaseApp.initializeApp(this)

        // Belt-and-suspenders alongside the manifest-declared WearAuthResultListenerService:
        // manifest delivery to a non-running app is best-effort and subject to Android's
        // background-execution restrictions, so also listen live while this process is alive.
        Wearable.getMessageClient(this).addListener { messageEvent ->
            WearAuthResultHandler.handle(messageEvent)
        }
    }
}
