package com.rodrigos01.aiaudiobook.common.util

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

object GoogleAuthHelper {
    private const val TAG = "GoogleAuthHelper"

    val DOCS_SCOPES = listOf(
        Scope("https://www.googleapis.com/auth/documents.readonly"),
        Scope("https://www.googleapis.com/auth/drive.readonly")
    )

    private val httpClient by lazy { OkHttpClient() }

    /**
     * Initiates OAuth 2.0 authorization for Google Docs scopes.
     * Passes the user's account if available to avoid asking the user to pick an account again.
     */
    suspend fun requestDocsAuthorization(context: Context, accountEmail: String? = null): AuthorizationResult {
        val builder = AuthorizationRequest.builder()
            .setRequestedScopes(DOCS_SCOPES)

        if (!accountEmail.isNullOrBlank()) {
            builder.setAccount(Account(accountEmail, "com.google"))
        }

        val request = builder.build()

        return Identity.getAuthorizationClient(context)
            .authorize(request)
            .await()
    }

    /**
     * Retrieves the AuthorizationResult from an Activity result intent after user consent.
     */
    fun getAuthorizationResultFromIntent(context: Context, data: Intent?): AuthorizationResult? {
        if (data == null) return null
        return try {
            Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(data)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting authorization result from intent", e)
            null
        }
    }

    /**
     * Extracts a Google Doc ID from a content URI, file path, or docs.google.com URL.
     * Rejects encoded SAF IDs (e.g., enc=encoded=...).
     */
    fun extractGoogleDocId(uri: Uri): String? {
        val uriString = uri.toString()

        // 1. Check docs.google.com web URL format
        val docUrlRegex = Regex("/document/d/([a-zA-Z0-9_-]+)")
        docUrlRegex.find(uriString)?.let {
            return it.groupValues[1]
        }

        // 2. Check doc= or doc%3D query/path parameter
        val docParamRegex = Regex("(?:doc=|doc%3D)([a-zA-Z0-9_-]+)")
        docParamRegex.find(uriString)?.let { match ->
            val id = match.groupValues[1]
            if (!id.startsWith("enc") && id.length >= 25) {
                return id
            }
        }

        // 3. Check DocumentsContract document ID
        try {
            val docId = DocumentsContract.getDocumentId(uri)
            if (!docId.isNullOrBlank() && !docId.startsWith("enc=") && !docId.contains("encoded=")) {
                val docMatch = docParamRegex.find(docId)
                if (docMatch != null && !docMatch.groupValues[1].startsWith("enc")) {
                    return docMatch.groupValues[1]
                }
                val cleanId = docId.substringAfterLast(':').substringAfterLast('/')
                if (cleanId.length >= 25 && !cleanId.startsWith("enc")) return cleanId
            }
        } catch (_: Exception) {}

        return null
    }

    /**
     * Resolves the real Google Drive File ID using the Google Drive REST API and user's OAuth access token.
     */
    suspend fun resolveDriveFileId(fileName: String, accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val cleanName = fileName.replace("'", "\\'")
            val query = "name = '$cleanName' and trashed = false"
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://www.googleapis.com/drive/v3/files?q=$encodedQuery&fields=files(id,name,mimeType)&orderBy=modifiedTime%20desc"

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to resolve file ID: ${response.code} ${response.message}")
                    return@withContext null
                }
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val files = json.optJSONArray("files")
                if (files != null && files.length() > 0) {
                    val file = files.getJSONObject(0)
                    val id = file.optString("id")
                    Log.d(TAG, "Resolved file '$fileName' to Drive ID: $id")
                    return@withContext id.ifBlank { null }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving Drive file ID for name '$fileName'", e)
        }
        return@withContext null
    }
}
