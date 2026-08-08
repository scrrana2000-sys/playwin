package com.playwin.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

object InterstitialManager {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var retryCount = 0
    private const val MAX_RETRY_COUNT = 6
    private val scope = CoroutineScope(Dispatchers.Main)

    private var loadTime = 0L
    private const val CACHE_DURATION_MS = 4 * 60 * 60 * 1000L // 4 hours standard AdMob cache limit

    @Synchronized
    fun preload(context: Context) {
        val appContext = context.applicationContext
        if (interstitialAd != null && !isExpired()) {
            AdLogger.d("Interstitial ad already loaded and is valid in memory.")
            return
        }
        if (isLoading) {
            AdLogger.d("Interstitial ad is currently loading in background.")
            return
        }

        val networkMonitor = NetworkMonitor(appContext)
        if (!networkMonitor.isNetworkAvailable()) {
            AdLogger.w("Cannot preload interstitial ad: Network unavailable.")
            scheduleRetry(appContext)
            return
        }

        isLoading = true
        AdLogger.i("Preloading Interstitial Ad for background cache...")

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            appContext,
            AdConstants.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    AdLogger.i("Interstitial Ad preloaded successfully into memory.")
                    interstitialAd?.let { oldAd ->
                        oldAd.fullScreenContentCallback = null
                    }
                    interstitialAd = ad
                    loadTime = System.currentTimeMillis()
                    isLoading = false
                    retryCount = 0

                    // Revenue tracking setup
                    ad.onPaidEventListener = com.google.android.gms.ads.OnPaidEventListener { value ->
                        AdLogger.i("Interstitial Ad Revenue captured: ${value.valueMicros} ${value.currencyCode}")
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    AdLogger.e("Interstitial Ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isLoading = false
                    scheduleRetry(appContext)
                }
            }
        )
    }

    private fun isExpired(): Boolean {
        return System.currentTimeMillis() - loadTime > CACHE_DURATION_MS
    }

    private fun scheduleRetry(context: Context) {
        if (retryCount >= MAX_RETRY_COUNT) {
            AdLogger.w("Max retry count reached for preloading interstitial ad.")
            return
        }
        retryCount++
        val delaySec = Math.pow(2.0, retryCount.toDouble()).toLong().coerceAtMost(60L)
        val delayMillis = delaySec * 1000L
        AdLogger.d("Scheduling interstitial ad preload retry #$retryCount with exponential backoff in ${delaySec}s")
        scope.launch {
            delay(delayMillis)
            preload(context)
        }
    }

    fun isAdReady(context: Context): Boolean {
        val ad = interstitialAd
        if (ad != null) {
            if (isExpired()) {
                AdLogger.w("Interstitial ad expired. Clearing memory and reloading...")
                interstitialAd = null
                preload(context)
                return false
            }
            return true
        }
        if (!isLoading) {
            preload(context)
        }
        return false
    }

    fun showAd(activity: Activity, onAdClosed: () -> Unit) {
        if (isExpired()) {
            AdLogger.w("Interstitial ad expired before show. Rejecting show and triggering background reload.")
            interstitialAd = null
            preload(activity)
            onAdClosed()
            return
        }

        val currentAd = interstitialAd
        if (currentAd == null) {
            AdLogger.w("Show ad requested but interstitial ad was null. Initiating background preload.")
            preload(activity)
            onAdClosed()
            return
        }

        interstitialAd = null
        val activityRef = WeakReference(activity)

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                AdLogger.i("Interstitial ad showed full screen content.")
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                AdLogger.e("Interstitial ad failed to show: ${error.message}")
                preload(activityRef.get() ?: activity)
                onAdClosed()
            }

            override fun onAdDismissedFullScreenContent() {
                AdLogger.i("Interstitial ad dismissed full screen content.")
                preload(activityRef.get() ?: activity)
                onAdClosed()
            }
        }

        val act = activityRef.get()
        if (act == null || act.isFinishing || act.isDestroyed) {
            AdLogger.e("Activity context is finished or destroyed. Aborting interstitial show.")
            preload(activity)
            onAdClosed()
            return
        }

        currentAd.show(act)
    }
}
