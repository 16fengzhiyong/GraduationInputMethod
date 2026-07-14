package com.nuc.omeletteinputmethod

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OmeletteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize things here
    }
}
