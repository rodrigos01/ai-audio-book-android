package com.rodrigos01.aiaudiobook.data.offline

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.workDataOf
import androidx.work.WorkerParameters
import com.rodrigos01.aiaudiobook.data.ApiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import java.io.File
import java.io.FileOutputStream

/**
 * Downloads a chapter for offline playback: polls `prepare` until every section is synthesized
 * and cached server-side, then streams the now-fully-cached audio to a local file. See the
 * backend README's "Downloading a chapter for offline playback" section for why these two calls
 * are used together instead of just `stream`.
 */
class ChapterDownloadWorker @JvmOverloads constructor(
    context: Context,
    params: WorkerParameters,
    private val apiRepository: ApiRepository = ApiRepository(),
    private val downloadedChapterStore: DownloadedChapterStore = DownloadedChapterStore(context)
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val chapterId = inputData.getString(KEY_CHAPTER_ID)
            ?: return Result.failure(workDataOf(KEY_ERROR to "Missing chapterId"))

        val prepareResult = pollUntilReady(chapterId)
        if (prepareResult != null) return prepareResult

        return downloadToFile(chapterId)
    }

    /** Returns null once ready, or a terminal [Result] if preparation failed/stalled. */
    private suspend fun pollUntilReady(chapterId: String): Result? {
        var lastGenerated = -1
        var stallRounds = 0

        while (true) {
            val response = apiRepository.prepareChapter(chapterId).getOrElse { error ->
                return if (runAttemptCount < MAX_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.failure(workDataOf(KEY_ERROR to (error.message ?: "Failed to prepare chapter")))
                }
            }

            setProgress(
                workDataOf(
                    KEY_PHASE to PHASE_PREPARING,
                    KEY_GENERATED_SECTIONS to response.generatedSections,
                    KEY_TOTAL_SECTIONS to response.totalSections
                )
            )

            if (response.ready) return null

            stallRounds = if (response.generatedSections == lastGenerated) stallRounds + 1 else 0
            lastGenerated = response.generatedSections
            if (stallRounds >= MAX_STALL_ROUNDS) {
                return Result.failure(workDataOf(KEY_ERROR to "Chapter preparation stalled"))
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    private suspend fun downloadToFile(chapterId: String): Result {
        val offlineDir = File(applicationContext.filesDir, OFFLINE_DIR_NAME).apply { mkdirs() }
        val tempFile = File(offlineDir, "$chapterId.mp3.tmp")
        val finalFile = File(offlineDir, "$chapterId.mp3")
        var bytesWritten = 0L

        try {
            FileOutputStream(tempFile).use { output ->
                apiRepository.streamChapter(chapterId, offset = 0).collect { chunk ->
                    output.write(chunk)
                    bytesWritten += chunk.size
                    setProgress(workDataOf(KEY_PHASE to PHASE_DOWNLOADING, KEY_BYTES_WRITTEN to bytesWritten))
                }
            }
        } catch (error: Exception) {
            tempFile.delete()
            return if (runAttemptCount < MAX_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure(workDataOf(KEY_ERROR to (error.message ?: "Failed to download chapter audio")))
            }
        }

        if (!tempFile.renameTo(finalFile)) {
            tempFile.delete()
            return Result.failure(workDataOf(KEY_ERROR to "Failed to save downloaded audio"))
        }

        downloadedChapterStore.put(
            DownloadedChapter(
                chapterId = chapterId,
                filePath = finalFile.absolutePath,
                sizeBytes = finalFile.length(),
                downloadedAt = System.currentTimeMillis()
            )
        )

        return Result.success(workDataOf(KEY_FILE_PATH to finalFile.absolutePath))
    }

    companion object {
        const val KEY_CHAPTER_ID = "chapterId"
        const val KEY_PHASE = "phase"
        const val KEY_GENERATED_SECTIONS = "generatedSections"
        const val KEY_TOTAL_SECTIONS = "totalSections"
        const val KEY_BYTES_WRITTEN = "bytesWritten"
        const val KEY_FILE_PATH = "filePath"
        const val KEY_ERROR = "error"

        const val PHASE_PREPARING = "preparing"
        const val PHASE_DOWNLOADING = "downloading"

        const val OFFLINE_DIR_NAME = "offline"

        private const val MAX_STALL_ROUNDS = 3
        private const val MAX_ATTEMPTS = 5
        private const val POLL_INTERVAL_MS = 1000L

        fun inputData(chapterId: String): Data = workDataOf(KEY_CHAPTER_ID to chapterId)
    }
}
