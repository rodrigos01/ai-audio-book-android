package com.rodrigos01.aiaudiobook.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material3.MaterialTheme
import com.rodrigos01.aiaudiobook.wear.theme.AIAudioBookWearTheme
import com.rodrigos01.aiaudiobook.wear.ui.WearApp

class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIAudioBookWearTheme {
                WearApp()
            }
        }
    }
}
