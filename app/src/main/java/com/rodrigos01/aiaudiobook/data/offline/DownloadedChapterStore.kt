package com.rodrigos01.aiaudiobook.data.offline

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.offlineChaptersDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "offline_chapters"
)

@Serializable
data class DownloadedChapter(
    val chapterId: String,
    val filePath: String,
    val sizeBytes: Long,
    val downloadedAt: Long
)

/**
 * Persists which chapters have a completed offline download and where the file lives.
 * In-flight download progress is transient state owned by [OfflineDownloadRepository] via
 * WorkManager, not stored here.
 */
class DownloadedChapterStore(context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val dataStore = context.applicationContext.offlineChaptersDataStore

    private fun keyFor(chapterId: String) = stringPreferencesKey("chapter_$chapterId")

    fun observe(chapterId: String): Flow<DownloadedChapter?> =
        dataStore.data.map { prefs -> prefs[keyFor(chapterId)]?.let(::decode) }

    fun observeAll(): Flow<Map<String, DownloadedChapter>> =
        dataStore.data.map { prefs ->
            prefs.asMap().entries
                .filter { it.key.name.startsWith("chapter_") }
                .mapNotNull { (_, value) -> (value as? String)?.let(::decode) }
                .associateBy { it.chapterId }
        }

    suspend fun get(chapterId: String): DownloadedChapter? =
        dataStore.data.first()[keyFor(chapterId)]?.let(::decode)

    suspend fun put(entry: DownloadedChapter) {
        dataStore.edit { prefs -> prefs[keyFor(entry.chapterId)] = json.encodeToString(entry) }
    }

    suspend fun remove(chapterId: String) {
        dataStore.edit { prefs -> prefs.remove(keyFor(chapterId)) }
    }

    private fun decode(raw: String): DownloadedChapter? =
        runCatching { json.decodeFromString<DownloadedChapter>(raw) }.getOrNull()
}
