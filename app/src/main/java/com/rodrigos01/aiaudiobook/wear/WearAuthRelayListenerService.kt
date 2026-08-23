package com.rodrigos01.aiaudiobook.wear

import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.rodrigos01.aiaudiobook.data.ApiRepository
import com.rodrigos01.aiaudiobook.data.AuthRepository
import com.rodrigos01.aiaudiobook.data.PairTokenMessage
import com.rodrigos01.aiaudiobook.data.WearMessagePaths
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Phone side of the Wear OS auth-pairing relay: the watch has no login screen of its own, so it
 * asks the phone (over the Wearable Data Layer) to mint a Firebase custom token for whichever
 * account is signed in here, which the watch then uses to sign itself in.
 */
class WearAuthRelayListenerService : WearableListenerService() {
    private val authRepository = AuthRepository()
    private val apiRepository = ApiRepository()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != WearMessagePaths.AUTH_TOKEN_REQUEST) return
        val sourceNodeId = messageEvent.sourceNodeId

        scope.launch {
            val response = if (authRepository.currentUser == null) {
                PairTokenMessage(error = "Not signed in on phone")
            } else {
                apiRepository.pairDevice().fold(
                    onSuccess = { PairTokenMessage(customToken = it.customToken) },
                    onFailure = { error ->
                        PairTokenMessage(error = error.localizedMessage ?: "Failed to pair device")
                    }
                )
            }

            runCatching {
                Tasks.await(
                    Wearable.getMessageClient(this@WearAuthRelayListenerService).sendMessage(
                        sourceNodeId,
                        WearMessagePaths.AUTH_TOKEN_RESULT,
                        PairTokenMessage.encode(response)
                    )
                )
            }
        }
    }
}
