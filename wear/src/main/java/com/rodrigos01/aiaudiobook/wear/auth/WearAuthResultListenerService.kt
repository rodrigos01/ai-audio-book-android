package com.rodrigos01.aiaudiobook.wear.auth

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.rodrigos01.aiaudiobook.data.AuthRepository
import com.rodrigos01.aiaudiobook.data.PairTokenMessage
import com.rodrigos01.aiaudiobook.data.WearMessagePaths
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles the phone's response to an auth-pairing request and signs the watch in with the
 * resulting Firebase custom token, publishing the outcome via [WearAuthResultBus] for the UI to
 * react to. Lives in this standalone object rather than the service class so it can be invoked
 * both by the manifest-declared [WearAuthResultListenerService] below (best-effort delivery,
 * subject to Android's background-execution restrictions when the app isn't running) and a live
 * [com.google.android.gms.wearable.MessageClient] listener registered in
 * [com.rodrigos01.aiaudiobook.wear.WearApplication] while the app process is alive - per Google's
 * own migration guidance to combine both rather than rely on the manifest alone.
 */
object WearAuthResultHandler {
    private val authRepository = AuthRepository()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun handle(messageEvent: MessageEvent) {
        if (messageEvent.path != WearMessagePaths.AUTH_TOKEN_RESULT) return

        val message = runCatching { PairTokenMessage.decode(messageEvent.data) }
            .getOrElse { PairTokenMessage(error = "Malformed response from phone") }

        val customToken = message.customToken
        if (customToken == null) {
            WearAuthResultBus.emit(message)
            return
        }

        scope.launch {
            authRepository.signInWithCustomToken(customToken).fold(
                onSuccess = { WearAuthResultBus.emit(PairTokenMessage(customToken = customToken)) },
                onFailure = { error ->
                    WearAuthResultBus.emit(PairTokenMessage(error = error.localizedMessage ?: "Sign in failed"))
                }
            )
        }
    }
}

class WearAuthResultListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        WearAuthResultHandler.handle(messageEvent)
    }
}
