package com.playwin.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdManager {
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private var networkMonitor: NetworkMonitor? = null

    // Centralized single entry point references
    val Banner = BannerManager
    val Native = NativeManager
    val Rewarded = RewardedManager
    val Logger = AdLogger
    val Analytics = AdAnalytics
    val RewardAnalytics = com.playwin.ads.RewardAnalytics
    val Configuration = AdConstants

    @Synchronized
    fun initialize(context: Context) {
        val appContext = context.applicationContext
        if (_isInitialized.value) {
            AdLogger.i("MobileAds SDK is already initialized or initialization is in progress.")
            return
        }

        AdLogger.i("Starting MobileAds SDK initialization...")
        try {
            // Initialize analytics
            AdAnalytics.initialize(appContext)
            com.playwin.ads.RewardAnalytics.initialize(appContext)

            // Setup NetworkMonitor
            val monitor = NetworkMonitor(appContext)
            networkMonitor = monitor
            monitor.startMonitoring {
                AdLogger.i("Network restored. Triggering auto-recovery reload for ads...")
                Banner.loadBanner(appContext)
                Native.preload(appContext)
                Rewarded.preload(appContext)
            }

            MobileAds.initialize(appContext) { status: InitializationStatus ->
                val adapterStatusMap = status.adapterStatusMap
                for ((adapterClass, adapterStatus) in adapterStatusMap) {
                    AdLogger.d("Adapter: $adapterClass, Description: ${adapterStatus.description}, State: ${adapterStatus.initializationState}")
                }
                _isInitialized.value = true
                AdLogger.i("MobileAds SDK initialized successfully. Launching background preloads...")

                // Background preload all formats
                Banner.loadBanner(appContext)
                Native.preload(appContext)
                Rewarded.preload(appContext)
            }
        } catch (e: Exception) {
            AdLogger.e("Critical error initializing MobileAds SDK", e)
        }
    }

    fun isNetworkAvailable(): Boolean {
        return networkMonitor?.isNetworkAvailable() ?: false
    }

    fun destroy() {
        AdLogger.i("Destroying AdManager and components...")
        networkMonitor?.stopMonitoring()
        Banner.destroy()
        Native.destroy()
    }
}
