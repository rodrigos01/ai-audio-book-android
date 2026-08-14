package com.rodrigos01.aiaudiobook.common.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object FileUtils {

    /**
     * Reads text content from an InputStream using UTF-8 encoding.
     */
    fun readTextFromStream(inputStream: InputStream): String {
        return BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }

    /**
     * Reads text content from a content URI using UTF-8 encoding.
     */
    fun readTextFromUri(context: Context, uri: Uri): Result<String> {
        return runCatching {
            val resolver = context.contentResolver
            val inputStream = resolver.openInputStream(uri)
                ?: throw IllegalStateException("Could not open input stream from file.")

            inputStream.use { stream ->
                readTextFromStream(stream)
            }
        }
    }

    /**
     * Strips file extension and trims whitespace from a filename.
     */
    fun cleanFileName(fileName: String?): String? {
        if (fileName.isNullOrBlank()) return null
        return fileName.substringBeforeLast('.').trim().ifBlank { null }
    }

    /**
     * Extracts the display name from a content URI and strips the file extension (.txt, etc.).
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var displayName: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            displayName = cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore and fallback
            }
        }

        if (displayName == null) {
            displayName = uri.lastPathSegment
        }

        return cleanFileName(displayName)
    }

    /**
     * Creates an Intent to open the Google Drive native file picker.
     */
    fun createGoogleDrivePickerIntent(context: Context? = null, accountEmail: String? = null): Intent {
        val driveGetContent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "text/plain",
                    "text/*",
                    "application/vnd.google-apps.document",
                    "*/*"
                )
            )
            if (!accountEmail.isNullOrBlank()) {
                putExtra("account_name", accountEmail)
                putExtra("authAccount", accountEmail)
                putExtra("account", accountEmail)
            }
            `package` = "com.google.android.apps.docs"
        }

        if (context != null) {
            val pm = context.packageManager
            if (driveGetContent.resolveActivity(pm) != null) {
                return driveGetContent
            }

            // Check ACTION_PICK for Drive
            val drivePick = Intent(Intent.ACTION_PICK).apply {
                type = "*/*"
                `package` = "com.google.android.apps.docs"
            }
            if (drivePick.resolveActivity(pm) != null) {
                return drivePick
            }

            // Fallback to DocumentsUI with initial URI pointing to Google Drive
            val driveDocIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf(
                        "text/plain",
                        "text/*",
                        "application/vnd.google-apps.document",
                        "*/*"
                    )
                )
                try {
                    putExtra(
                        android.provider.DocumentsContract.EXTRA_INITIAL_URI,
                        Uri.parse("content://com.google.android.apps.docs.storage/document/root")
                    )
                } catch (_: Exception) {}
            }
            if (driveDocIntent.resolveActivity(pm) != null) {
                return driveDocIntent
            }
        }

        return driveGetContent
    }

    /**
     * Creates a fallback Intent for general document picker for text files.
     */
    fun createGeneralTextFilePickerIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "text/plain",
                    "text/*",
                    "application/vnd.google-apps.document",
                    "*/*"
                )
            )
        }
    }
}
