package com.example.app

import android.app.Application
import com.dsquares.library.DSquareSDK

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DSquareSDK.init(this, apiKey = BuildConfig.API_KEY)

    }
}