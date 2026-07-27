package com.rodrigos01.aiaudiobook.data

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AuthRepository(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    suspend fun loginWithEmail(email: String, password: String): Result<AuthResult> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email and password cannot be empty."))
        }
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        if (result != null) {
                            continuation.resume(Result.success(result))
                        } else {
                            continuation.resume(Result.failure(Exception("Authentication succeeded but returned empty result.")))
                        }
                    } else {
                        continuation.resume(Result.failure(task.exception ?: Exception("Login failed.")))
                    }
                }
        }
    }

    suspend fun registerWithEmail(email: String, password: String): Result<AuthResult> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email and password cannot be empty."))
        }
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        if (result != null) {
                            continuation.resume(Result.success(result))
                        } else {
                            continuation.resume(Result.failure(Exception("Registration succeeded but returned empty result.")))
                        }
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
                        val result = task.result
                        if (result != null) {
                            continuation.resume(Result.success(result))
                        } else {
                            continuation.resume(Result.failure(Exception("Google sign in succeeded but returned empty result.")))
                        }
                    } else {
                        continuation.resume(Result.failure(task.exception ?: Exception("Google sign in failed.")))
                    }
                }
        }
    }

    suspend fun signInWithGoogle(context: android.content.Context, webClientId: String): Result<AuthResult> {
        val credentialManager = androidx.credentials.CredentialManager.create(context)
        val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        val request = androidx.credentials.GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            val credential = result.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                loginWithGoogleCredential(googleIdTokenCredential.idToken)
            } else {
                Result.failure(Exception("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: Exception) {
            val friendlyMessage = when (e) {
                is androidx.credentials.exceptions.NoCredentialException -> {
                    "No Google accounts found on this device. Please sign in to a Google account in your device settings."
                }
                else -> e.localizedMessage ?: "Google Sign-In failed."
            }
            Result.failure(Exception(friendlyMessage))
        }
    }

    suspend fun signOut(context: android.content.Context) {
        firebaseAuth.signOut()
        try {
            val credentialManager = androidx.credentials.CredentialManager.create(context)
            credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
        } catch (e: Exception) {
            // Ignore error
        }
    }
}
