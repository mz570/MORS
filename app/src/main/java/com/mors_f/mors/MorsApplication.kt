package com.mors_f.mors

import android.app.Application
import com.cloudinary.android.MediaManager

class MorsApplication : Application() {
    override fun onCreate() {
        super.onCreate()


        val config = mapOf(
            "cloud_name" to "dpdg31jra",
            "api_key" to "391111436651981",
            "api_secret" to "JN_NnEV-X1DfQu4Y97RgKY46qrY"
        )
        MediaManager.init(this, config)
    }
}
