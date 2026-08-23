package com.rodrigos01.aiaudiobook.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Wearable Data Layer message paths for the phone<->companion-device auth-pairing relay: the
 * companion (e.g. the Wear OS app) sends an empty request on [AUTH_TOKEN_REQUEST], and the phone
 * replies on [AUTH_TOKEN_RESULT] with a [PairTokenMessage] payload.
 */
object WearMessagePaths {
    const val AUTH_TOKEN_REQUEST = "/aiaudiobook/auth-token-request"
    const val AUTH_TOKEN_RESULT = "/aiaudiobook/auth-token-result"
}

@Serializable
data class PairTokenMessage(
    val customToken: String? = null,
    val error: String? = null
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun encode(message: PairTokenMessage): ByteArray =
            json.encodeToString(serializer(), message).encodeToByteArray()

        fun decode(bytes: ByteArray): PairTokenMessage =
            json.decodeFromString(serializer(), bytes.decodeToString())
    }
}
