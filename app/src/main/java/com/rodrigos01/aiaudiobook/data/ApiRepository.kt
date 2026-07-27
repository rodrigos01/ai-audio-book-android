package com.rodrigos01.aiaudiobook.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.*

@Serializable
data class ClaimResponse(
    val success: Boolean,
    val claimed_count: Int
)

@Serializable
data class Voice(
    val id: String,
    val name: String,
    val gender: String,
    val lang: String,
    val sampleUrl: String
)

@Serializable
data class CreateTitleRequest(
    val name: String,
    val ai_casting_enabled: Boolean
)

@Serializable
data class TitleResponse(
    val id: String,
    val name: String,
    val owner_id: String,
    val ai_casting_enabled: Boolean,
    val casting_map: Map<String, String> = emptyMap(),
    val narrator_voice: String? = null,
    val created_at: String? = null
)

@Serializable
data class UpdateTitleRequest(
    val name: String? = null,
    val casting_map: Map<String, String>? = null,
    val narrator_voice: String? = null
)

@Serializable
data class GenericSuccessResponse(
    val success: Boolean
)

@Serializable
data class CreateChapterRequest(
    val name: String,
    val content: String,
    val voice_id: String
)

@Serializable
data class ChapterResponse(
    val id: String,
    val title_id: String,
    val order_index: Int,
    val name: String? = null,
    val content: String,
    val voice_id: String,
    val is_ssml: Boolean,
    val created_at: String? = null
)

@Serializable
data class UpdateChapterRequest(
    val name: String? = null,
    val content: String? = null,
    val is_ssml: Boolean? = null
)

@Serializable
data class CastChapterResponse(
    val success: Boolean,
    val casting_map: Map<String, String>,
    val ssml: String,
    val sections_count: Int
)

@Serializable
data class FetchGoogleDocRequest(
    val documentId: String,
    val googleAccessToken: String
)

@Serializable
data class FetchGoogleDocResponse(
    val title: String,
    val content: String
)

interface AIAudioBookApiService {
    @POST("api/auth/claim")
    suspend fun claimTitles(): ClaimResponse

    @GET("api/voices")
    suspend fun getVoices(): List<Voice>

    @POST("api/titles")
    suspend fun createTitle(@Body request: CreateTitleRequest): TitleResponse

    @PATCH("api/titles/{id}")
    suspend fun updateTitle(@Path("id") id: String, @Body request: UpdateTitleRequest): GenericSuccessResponse

    @DELETE("api/titles/{id}")
    suspend fun deleteTitle(@Path("id") id: String): GenericSuccessResponse

    @POST("api/titles/{id}/chapters")
    suspend fun createChapter(@Path("id") titleId: String, @Body request: CreateChapterRequest): ChapterResponse

    @PATCH("api/chapters/{id}")
    suspend fun updateChapter(@Path("id") chapterId: String, @Body request: UpdateChapterRequest): GenericSuccessResponse

    @DELETE("api/chapters/{id}")
    suspend fun deleteChapter(@Path("id") chapterId: String): GenericSuccessResponse

    @POST("api/chapters/{chapterId}/cast")
    suspend fun castChapter(@Path("chapterId") chapterId: String): CastChapterResponse

    @Streaming
    @GET("api/chapters/{chapterId}/stream")
    suspend fun streamChapter(
        @Path("chapterId") chapterId: String,
        @Query("offset") offset: Int? = null,
        @Query("token") token: String? = null
    ): ResponseBody

    @POST("api/google-docs/fetch")
    suspend fun fetchGoogleDoc(@Body request: FetchGoogleDocRequest): FetchGoogleDocResponse
}

class ApiRepository(
    private val baseUrl: String = "https://ai-audio-book-api-883622140264.us-central1.run.app",
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val user = firebaseAuth.currentUser
        
        if (user == null) {
            return@Interceptor chain.proceed(originalRequest)
        }

        try {
            val tokenResult = Tasks.await(user.getIdToken(false))
            val token = tokenResult.token
            
            if (!token.isNullOrEmpty()) {
                val authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(authenticatedRequest)
            } else {
                chain.proceed(originalRequest)
            }
        } catch (e: Exception) {
            chain.proceed(originalRequest)
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    private val apiService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(AIAudioBookApiService::class.java)

    suspend fun claimTitles(): Result<ClaimResponse> = runCatching {
        apiService.claimTitles()
    }

    suspend fun getVoices(): Result<List<Voice>> = runCatching {
        apiService.getVoices()
    }

    suspend fun createTitle(name: String, aiCastingEnabled: Boolean): Result<TitleResponse> = runCatching {
        apiService.createTitle(CreateTitleRequest(name, aiCastingEnabled))
    }

    suspend fun updateTitle(
        id: String,
        name: String? = null,
        castingMap: Map<String, String>? = null,
        narratorVoice: String? = null
    ): Result<GenericSuccessResponse> = runCatching {
        apiService.updateTitle(id, UpdateTitleRequest(name, castingMap, narratorVoice))
    }

    suspend fun deleteTitle(id: String): Result<GenericSuccessResponse> = runCatching {
        apiService.deleteTitle(id)
    }

    suspend fun createChapter(
        titleId: String,
        name: String,
        content: String,
        voiceId: String
    ): Result<ChapterResponse> = runCatching {
        apiService.createChapter(titleId, CreateChapterRequest(name, content, voiceId))
    }

    suspend fun updateChapter(
        chapterId: String,
        name: String? = null,
        content: String? = null,
        isSsml: Boolean? = null
    ): Result<GenericSuccessResponse> = runCatching {
        apiService.updateChapter(chapterId, UpdateChapterRequest(name, content, isSsml))
    }

    suspend fun deleteChapter(chapterId: String): Result<GenericSuccessResponse> = runCatching {
        apiService.deleteChapter(chapterId)
    }

    suspend fun castChapter(chapterId: String): Result<CastChapterResponse> = runCatching {
        apiService.castChapter(chapterId)
    }

    fun streamChapter(
        chapterId: String,
        offset: Int? = null,
        token: String? = null
    ): Flow<ByteArray> = flow {
        val responseBody = apiService.streamChapter(chapterId, offset, token)
        responseBody.byteStream().use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (bytesRead > 0) {
                    emit(buffer.copyOf(bytesRead))
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun fetchGoogleDoc(
        documentId: String,
        googleAccessToken: String
    ): Result<FetchGoogleDocResponse> = runCatching {
        apiService.fetchGoogleDoc(FetchGoogleDocRequest(documentId, googleAccessToken))
    }
}
