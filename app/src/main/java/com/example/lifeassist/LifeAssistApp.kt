package com.example.lifeassist

import android.app.Application
import android.util.Log

class LifeAssistApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Perform global initializations here
        Log.d("LifeAssistApp", "Application started")
    }
}
