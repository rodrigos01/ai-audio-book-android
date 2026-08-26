package com.rodrigos01.aiaudiobook

import android.app.Application
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.rodrigos01.aiaudiobook.core.CoreConfig
import com.rodrigos01.aiaudiobook.wear.WearAuthRelayHandler

private const val TAG = "AIAudioBookApp"

class AIAudioBookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreConfig.init(this)

        // Belt-and-suspenders alongside the manifest-declared WearAuthRelayListenerService:
        // manifest delivery to a non-running app is best-effort and subject to Android's
        // background-execution restrictions, so also listen live while this process is alive,
        // which covers the common case of the phone app being open or merely backgrounded.
        Wearable.getMessageClient(this).addListener { messageEvent ->
            Log.d(TAG, "live listener onMessageReceived: ${messageEvent.path}")
            WearAuthRelayHandler.handle(this, messageEvent)
        }.addOnSuccessListener {
            Log.d(TAG, "live MessageClient listener registered successfully")
        }.addOnFailureListener {
            Log.e(TAG, "live MessageClient listener registration FAILED", it)
        }
    }
}
