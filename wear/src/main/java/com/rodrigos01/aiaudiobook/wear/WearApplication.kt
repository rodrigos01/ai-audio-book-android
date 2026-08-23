package com.rodrigos01.aiaudiobook.wear

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.rodrigos01.aiaudiobook.core.CoreConfig

class WearApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreConfig.serverUrl = BuildConfig.SERVER_URL

        // TEMPORARY for local dev/testing: no wear/google-services.json exists yet (that
        // requires registering a Firebase Android app for com.rodrigos01.aiaudiobook.wear in the
        // console - a manual, one-time step - see wear/build.gradle.kts). Until then, initialize
        // manually with the same Firebase project's values as the phone app's (gitignored)
        // app/google-services.json. Firebase Auth only validates the API key against the
        // project, not the package name, so this works for testing the pairing/sign-in flow.
        // Replace with the google-services plugin + a real wear/google-services.json before
        // shipping.
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(
                this,
                FirebaseOptions.Builder()
                    .setApplicationId("1:883622140264:android:72536d483b951be5e61edd")
                    .setApiKey("AIzaSyA9ptOZ_L9SHpLTWuxZCBWC5C1ghY-10_8")
                    .setProjectId("ai-audio-book")
                    .setStorageBucket("ai-audio-book.firebasestorage.app")
                    .build()
            )
        }
    }
}
