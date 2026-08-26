package com.rodrigos01.aiaudiobook.core

/**
 * The backend environments the app can talk to. Selected via the hidden long-press selector on
 * the home screen (see `EnvironmentSelectorDialog`) and persisted by [EnvironmentStore].
 */
enum class Environment(val displayName: String, val baseUrl: String) {
    PROD("Production", "https://ai-audio-book-api-883622140264.us-central1.run.app/"),
    STAGING("Staging", "https://claude-develop-ai-audio-book-api-883622140264.us-central1.run.app/"),
    DEV("Development", "http://10.0.2.2:3005/")
}
