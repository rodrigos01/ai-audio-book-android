package com.rodrigos01.aiaudiobook.data.offline

sealed interface ChapterDownloadState {
    data object NotDownloaded : ChapterDownloadState
    data class Preparing(val generatedSections: Int, val totalSections: Int) : ChapterDownloadState
    data class Downloading(val bytesWritten: Long) : ChapterDownloadState
    data class Downloaded(val filePath: String, val sizeBytes: Long) : ChapterDownloadState
    data class Failed(val message: String) : ChapterDownloadState
}
