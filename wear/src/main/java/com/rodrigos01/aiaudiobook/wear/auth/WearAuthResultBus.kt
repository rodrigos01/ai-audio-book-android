package com.rodrigos01.aiaudiobook.wear.auth

import com.rodrigos01.aiaudiobook.data.PairTokenMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Relays a [PairTokenMessage] from [WearAuthResultListenerService] (instantiated by the system
 * whenever a Data Layer message arrives, so it can't hold a long-lived reference to a ViewModel)
 * to whoever is listening for the outcome of an in-flight pairing request, e.g. [WearAuthViewModel].
 */
object WearAuthResultBus {
    private val _results = MutableSharedFlow<PairTokenMessage>(extraBufferCapacity = 1)
    val results: SharedFlow<PairTokenMessage> = _results.asSharedFlow()

    fun emit(message: PairTokenMessage) {
        _results.tryEmit(message)
    }
}
