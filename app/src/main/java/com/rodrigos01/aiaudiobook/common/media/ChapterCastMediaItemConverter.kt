package com.rodrigos01.aiaudiobook.common.media

import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import com.google.android.gms.cast.HlsSegmentFormat
import com.google.android.gms.cast.MediaQueueItem

/**
 * Fills in what a Cast receiver needs that media3's default converter leaves out for our chapters:
 * the backend serves audio-only HLS made of raw AAC segments, which a receiver can't open unless
 * the segment format is declared, and a known length lets it show the duration straight away
 * instead of 0 until the playlist is parsed.
 */
class ChapterCastMediaItemConverter(
    private val delegate: MediaItemConverter = DefaultMediaItemConverter()
) : MediaItemConverter {

    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        val queueItem = delegate.toMediaQueueItem(mediaItem)
        queueItem.media?.writer?.apply {
            if (mediaItem.localConfiguration?.mimeType == MimeTypes.APPLICATION_M3U8) {
                setHlsSegmentFormat(HlsSegmentFormat.AAC)
            }
            val durationMs = mediaItem.mediaMetadata.durationMs
            if (durationMs != null && durationMs > 0) setStreamDuration(durationMs)
        }
        return queueItem
    }

    override fun toMediaItem(queueItem: MediaQueueItem): MediaItem = delegate.toMediaItem(queueItem)
}
