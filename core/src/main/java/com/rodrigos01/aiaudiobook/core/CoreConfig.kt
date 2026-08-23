package com.rodrigos01.aiaudiobook.core

/**
 * Flavor-specific values that [ApiRepository][com.rodrigos01.aiaudiobook.data.ApiRepository] and
 * other `:core` classes need but can't get from a `BuildConfig`, since `:core` has no product
 * flavors of its own. Each app module (`:app`, `:wear`) sets this once, from its own
 * `BuildConfig.SERVER_URL`, before constructing any repository/ViewModel/Worker.
 */
object CoreConfig {
    lateinit var serverUrl: String
}
