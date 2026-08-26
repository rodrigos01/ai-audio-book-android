package com.rodrigos01.aiaudiobook.core

import android.content.Context

private const val PREFS_NAME = "environment_prefs"
private const val KEY_ENVIRONMENT = "selected_environment"

/**
 * Persists the user's selected [Environment] in a SharedPreferences file. Defaults to
 * [Environment.PROD] when nothing has been saved yet.
 */
object EnvironmentStore {
    fun get(context: Context): Environment {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_ENVIRONMENT, null)
        return Environment.entries.firstOrNull { it.name == name } ?: Environment.PROD
    }

    fun set(context: Context, environment: Environment) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ENVIRONMENT, environment.name)
            .apply()
    }
}
