package com.rodrigos01.aiaudiobook.wear

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "WearAuthRelay"

/**
 * Phone side of the Wear OS auth-pairing relay: the watch has no login screen of its own, so it
 * asks the phone (over the Wearable Data Layer) to mint a Firebase custom token for whichever
 * account is signed in here, which the watch then uses to sign itself in.
 *
 * Handling lives in this standalone object rather than the service class so it can be invoked two
 * ways, per Google's own guidance to combine both: the manifest-declared [WearAuthRelayListenerService]
 * below (best-effort delivery, subject to Android's background-execution restrictions when the app
 * isn't running) and a live [com.google.android.gms.wearable.MessageClient] listener registered in
 * [com.rodrigos01.aiaudiobook.AIAudioBookApplication] while the app process is alive (reliable,
 * covers the common case where the phone app is open or merely backgrounded).
 */
object WearAuthRelayHandler {
    private val authRepository = AuthRepository()
    private val apiRepository = ApiRepository()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    data class State(
        val status: Status,
        val message: String?,
    )

    enum class Status {
        IDLE,
        CONNECTING,
        CONNECTED,
        ERROR,
    }

    private val _connectionState = MutableStateFlow(State(Status.IDLE, null))
    val connectionState = _connectionState.asStateFlow()

    fun handle(context: Context, messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived: ${messageEvent.path}")
        if (messageEvent.path != WearMessagePaths.AUTH_TOKEN_REQUEST) return
        val sourceNodeId = messageEvent.sourceNodeId

        scope.launch {
            _connectionState.value = State(Status.CONNECTING, "Connecting to watch...")
            Log.d(TAG, "calling pairDevice()...")
            val response = if (authRepository.currentUser == null) {
                _connectionState.value = State(Status.ERROR, "Not signed in on phone")
                PairTokenMessage(error = "Not signed in on phone")
            } else {
                _connectionState.value = connectionState.value.copy(message = "Registering watch...")
                apiRepository.pairDevice().fold(
                    onSuccess = { PairTokenMessage(customToken = it.customToken) },
                    onFailure = { error ->
                        Log.w(TAG, "pairDevice() failed", error)
                        _connectionState.value = State(Status.ERROR, error.localizedMessage)
                        PairTokenMessage(error = error.localizedMessage ?: "Failed to pair device")
                    }
                )
            }
            _connectionState.value = connectionState.value.copy( message = "Saving pairing result...")
            Log.d(TAG, "pairDevice() resolved, sending result back to watch (hasToken=${response.customToken != null})")

            runCatching {
                Tasks.await(
                    Wearable.getMessageClient(context.applicationContext).sendMessage(
                        sourceNodeId,
                        WearMessagePaths.AUTH_TOKEN_RESULT,
                        PairTokenMessage.encode(response)
                    )
                )
            }.onSuccess {
                Log.d(TAG, "sent pairing result back to watch")
                _connectionState.value = State(Status.CONNECTED, "Paired with watch")
            }.onFailure {
                _connectionState.value = State(Status.ERROR, "Failed to send pairing result back to watch")
                Log.w(TAG, "Failed to send pairing result back to watch", it)
            }
        }
    }
}

class WearAuthRelayListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        WearAuthRelayHandler.handle(this, messageEvent)
    }
}
