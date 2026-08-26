package com.rodrigos01.aiaudiobook.core

import android.content.Context

/**
 * Backend base URL used by [ApiRepository][com.rodrigos01.aiaudiobook.data.ApiRepository] and
 * other `:core` classes, driven by the user's [Environment] selection (see [EnvironmentStore]).
 * Each app module (`:app`, `:wear`) calls [init] once from its `Application.onCreate`, before
 * constructing any repository/ViewModel/Worker.
 */
object CoreConfig {
    lateinit var serverUrl: String
        private set

    fun init(context: Context) {
        serverUrl = EnvironmentStore.get(context).baseUrl
    }

    fun updateEnvironment(context: Context, environment: Environment) {
        EnvironmentStore.set(context, environment)
        serverUrl = environment.baseUrl
    }
}
