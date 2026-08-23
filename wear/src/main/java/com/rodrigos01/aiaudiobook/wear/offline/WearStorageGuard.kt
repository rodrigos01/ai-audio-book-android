package com.rodrigos01.aiaudiobook.wear.offline

import android.content.Context
import android.os.StatFs

/**
 * Blocks new chapter downloads when the watch is low on space, rather than silently enqueueing a
 * download that can't finish. v1 policy is warn-and-block only - no automatic eviction of older
 * downloads, per the Wear OS plan (the user can free space manually from the downloads screen).
 */
object WearStorageGuard {
    private const val RESERVE_BYTES = 150L * 1024 * 1024 // 150 MB

    fun freeBytes(context: Context): Long =
        StatFs(context.applicationContext.filesDir.path).availableBytes

    fun hasSpaceForDownload(context: Context): Boolean = freeBytes(context) > RESERVE_BYTES
}
