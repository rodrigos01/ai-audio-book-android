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
 * Receives the phone's response to an auth-pairing request and signs the watch in with the
 * resulting Firebase custom token. Runs as a system-instantiated service (not tied to any
 * Activity/ViewModel lifecycle), so it publishes the outcome via [WearAuthResultBus] for the UI
 * to react to.
 */
class WearAuthResultListenerService : WearableListenerService() {
    private val authRepository = AuthRepository()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
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
