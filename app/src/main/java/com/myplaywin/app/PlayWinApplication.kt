package com.myplaywin.app

import android.app.Application
import com.playwin.ads.AdManager

class PlayWinApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize the unified AdManager at launch (manages preloads, analytics, logging, and recovery)
        AdManager.initialize(this)
    }
}
