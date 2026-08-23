package com.rodrigos01.aiaudiobook.wear.auth

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import com.rodrigos01.aiaudiobook.data.WearMessagePaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Watch side of the auth-pairing relay: asks the connected phone to mint a Firebase custom token
 * for its signed-in account. The actual token comes back asynchronously via
 * [WearAuthResultListenerService] -> [WearAuthResultBus], not as a return value here, since Data
 * Layer messaging is fire-and-forget.
 */
class WearAuthSyncClient(private val context: Context) {

    /** Returns failure if no phone node is currently connected/reachable. */
    suspend fun requestPairing(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val appContext = context.applicationContext
            val nodes = Tasks.await(Wearable.getNodeClient(appContext).connectedNodes)
            val phoneNode = nodes.firstOrNull() ?: error("No paired phone connected")

            Tasks.await(
                Wearable.getMessageClient(appContext).sendMessage(
                    phoneNode.id,
                    WearMessagePaths.AUTH_TOKEN_REQUEST,
                    ByteArray(0)
                )
            )
            Unit
        }
    }
}
