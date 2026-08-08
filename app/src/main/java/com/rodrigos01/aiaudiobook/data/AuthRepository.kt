package com.rodrigos01.aiaudiobook.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.MessageDigest
import kotlin.coroutines.resume

class AuthRepository(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    private fun logAppSignature(context: Context) {
        try {
            val packageName = context.packageName
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            
            if (signatures.isNullOrEmpty()) {
                Log.w(TAG, "No signatures found for package: $packageName")
                return
            }

            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA-1")
                val digest = md.digest(signature.toByteArray())
                val sha1 = digest.joinToString(":") { String.format("%02X", it) }
                Log.d(TAG, "Current App SHA-1: $sha1")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error logging signature", e)
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<AuthResult> {
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.success(task.result!!))
                    } else {
                        continuation.resume(Result.failure(task.exception ?: Exception("Login failed.")))
                    }
                }
        }
    }

    suspend fun registerWithEmail(email: String, password: String): Result<AuthResult> {
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.success(task.result!!))
                    } else {
                        continuation.resume(Result.failure(task.exception ?: Exception("Registration failed.")))
                    }
                }
        }
    }

    suspend fun loginWithGoogleCredential(idToken: String): Result<AuthResult> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.success(task.result!!))
                    } else {
                        continuation.resume(Result.failure(task.exception ?: Exception("Google sign in failed.")))
                    }
                }
        }
    }

    suspend fun signInWithGoogle(context: Context, webClientId: String): Result<AuthResult> {
        Log.d(TAG, "signInWithGoogle called with webClientId: $webClientId")
        logAppSignature(context)
        
        val credentialManager = androidx.credentials.CredentialManager.create(context)
        
        val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = androidx.credentials.GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential
            
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                loginWithGoogleCredential(googleIdTokenCredential.idToken)
            } else {
                Result.failure(Exception("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during getCredential", e)
            val friendlyMessage = when (e) {
                is androidx.credentials.exceptions.NoCredentialException -> "No Google accounts found on this device."
                is androidx.credentials.exceptions.GetCredentialException -> {
                    if (e.message?.contains("28444") == true) {
                        "Developer config error (28444). Check Logcat for 'Current App SHA-1' and verify it matches your Google Cloud Console."
                    } else {
                        "Sign-in failed: ${e.message}"
                    }
                }
                else -> e.localizedMessage ?: "Google Sign-In failed."
            }
            Result.failure(Exception(friendlyMessage))
        }
    }

    suspend fun signOut(context: Context) {
        firebaseAuth.signOut()
        try {
            val credentialManager = androidx.credentials.CredentialManager.create(context)
            credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(TAG, "Error during signOut", e)
        }
    }
}
