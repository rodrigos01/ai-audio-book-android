package com.rodrigos01.aiaudiobook.data

import com.google.firebase.Timestamp

data class Title(
    val id: String = "",
    val name: String = "",
    val owner_id: String = "",
    val ai_casting_enabled: Boolean = false,
    val casting_map: Map<String, String> = emptyMap(),
    val narrator_voice: String? = null,
    val created_at: Timestamp? = null
)

data class Chapter(
    val id: String = "",
    val title_id: String = "",
    val order_index: Int = 0,
    val name: String? = null,
    val content: String = "",
    val voice_id: String = "",
    val is_ssml: Boolean = false,
    val created_at: Timestamp? = null
)
