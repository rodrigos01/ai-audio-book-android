package com.rodrigos01.aiaudiobook.common.media

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.rodrigos01.aiaudiobook.R

/** Receiver app is `cast_receiver_app_id` (res/values/cast.xml): the Default Media Receiver unless a styled one is configured. */
class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions =
        CastOptions.Builder()
            .setReceiverApplicationId(context.getString(R.string.cast_receiver_app_id))
            .build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null
}
