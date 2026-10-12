package com.rodrigos01.aiaudiobook.common.media

import android.content.Context
import androidx.media3.cast.DefaultCastOptionsProvider
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

/** Uses the Default Media Receiver; swap in a custom receiver app id if one is registered. */
class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions =
        CastOptions.Builder()
            .setReceiverApplicationId(DefaultCastOptionsProvider.APP_ID_DEFAULT_RECEIVER_WITH_DRM)
            .build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null
}
