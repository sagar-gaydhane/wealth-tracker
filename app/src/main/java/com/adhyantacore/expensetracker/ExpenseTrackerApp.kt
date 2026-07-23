package com.adhyantacore.expensetracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpenseTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Force the app to always use Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}