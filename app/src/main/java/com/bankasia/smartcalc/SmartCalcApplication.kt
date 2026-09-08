package com.bankasia.smartcalc

import android.app.Application
import com.bankasia.smartcalc.data.local.ThemePreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartCalcApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemePreferences.init(this)
    }
}