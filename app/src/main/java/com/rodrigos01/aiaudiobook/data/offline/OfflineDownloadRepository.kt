package com.rodrigos01.aiaudiobook.data.offline

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Downloads chapters for offline playback and reports their state, combining transient
 * in-flight progress from WorkManager with the persisted "already downloaded" record in
 * [DownloadedChapterStore].
 */
class OfflineDownloadRepository(
    context: Context,
    private val downloadedChapterStore: DownloadedChapterStore = DownloadedChapterStore(context)
) {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    fun downloadChapter(chapterId: String) {
        val request = OneTimeWorkRequestBuilder<ChapterDownloadWorker>()
            .setInputData(ChapterDownloadWorker.inputData(chapterId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(workName(chapterId), ExistingWorkPolicy.KEEP, request)
    }

    fun cancelDownload(chapterId: String) {
        workManager.cancelUniqueWork(workName(chapterId))
    }

    suspend fun deleteDownload(chapterId: String) {
        cancelDownload(chapterId)
        downloadedChapterStore.get(chapterId)?.let { entry -> File(entry.filePath).delete() }
        downloadedChapterStore.remove(chapterId)
        File(appContext.filesDir, "${ChapterDownloadWorker.OFFLINE_DIR_NAME}/$chapterId.mp3.tmp").delete()
    }

    fun observeDownloadState(chapterId: String): Flow<ChapterDownloadState> =
        combine(
            downloadedChapterStore.observe(chapterId),
            workManager.getWorkInfosForUniqueWorkFlow(workName(chapterId))
        ) { downloaded, workInfos ->
            val activeWork = workInfos.firstOrNull { !it.state.isFinished }
            when {
                activeWork != null -> activeWork.toDownloadState() ?: ChapterDownloadState.Preparing(0, 0)
                downloaded != null -> ChapterDownloadState.Downloaded(downloaded.filePath, downloaded.sizeBytes)
                workInfos.any { it.state == WorkInfo.State.FAILED } -> ChapterDownloadState.Failed(
                    workInfos.last().outputData.getString(ChapterDownloadWorker.KEY_ERROR) ?: "Download failed"
                )
                else -> ChapterDownloadState.NotDownloaded
            }
        }

    /** Local file path for a chapter, if it has a completed download, without observing changes. */
    suspend fun localFilePath(chapterId: String): String? = downloadedChapterStore.get(chapterId)?.filePath

    private fun WorkInfo.toDownloadState(): ChapterDownloadState? =
        when (progress.getString(ChapterDownloadWorker.KEY_PHASE)) {
            ChapterDownloadWorker.PHASE_PREPARING -> ChapterDownloadState.Preparing(
                generatedSections = progress.getInt(ChapterDownloadWorker.KEY_GENERATED_SECTIONS, 0),
                totalSections = progress.getInt(ChapterDownloadWorker.KEY_TOTAL_SECTIONS, 0)
            )
            ChapterDownloadWorker.PHASE_DOWNLOADING -> ChapterDownloadState.Downloading(
                bytesWritten = progress.getLong(ChapterDownloadWorker.KEY_BYTES_WRITTEN, 0)
            )
            else -> null
        }

    private fun workName(chapterId: String) = "download_chapter_$chapterId"
}
