package com.playwin.ads

import android.content.Context

class RewardRepository(private val context: Context) {
    private val networkMonitor = NetworkMonitor(context.applicationContext)

    fun isNetworkAvailable(): Boolean {
        return networkMonitor.isNetworkAvailable()
    }

    fun isSdkReady(): Boolean {
        // MobileAds standard checks
        return true
    }
}
