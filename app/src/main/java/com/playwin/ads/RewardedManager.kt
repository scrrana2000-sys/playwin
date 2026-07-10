package com.playwin.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

object RewardedManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var retryCount = 0
    private const val MAX_RETRY_COUNT = 5
    private val scope = CoroutineScope(Dispatchers.Main)

    // Cache management
    private var loadTime = 0L
    private const val CACHE_DURATION_MS = 4 * 60 * 60 * 1000L // 4 hours standard AdMob cache limit

    fun preload(context: Context) {
        val appContext = context.applicationContext
        if (rewardedAd != null && !isExpired()) {
            AdLogger.d("Rewarded ad already loaded and is valid.")
            return
        }
        if (isLoading) {
            AdLogger.d("Rewarded ad is currently loading.")
            return
        }

        val networkMonitor = NetworkMonitor(appContext)
        if (!networkMonitor.isNetworkAvailable()) {
            AdLogger.w("Cannot preload rewarded ad: Network unavailable.")
            scheduleRetry(appContext)
            return
        }

        isLoading = true
        AdLogger.i("Preloading Rewarded Ad...")

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            appContext,
            AdConstants.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    AdLogger.i("Rewarded Ad loaded successfully.")
                    rewardedAd = ad
                    loadTime = System.currentTimeMillis()
                    isLoading = false
                    retryCount = 0
                    RewardAnalytics.logRewardLoaded(AdConstants.REWARDED_AD_UNIT_ID)

                    // Revenue tracking setup (capture Ad Revenue, Currency, Precision, Ad Unit, Value Micros)
                    ad.onPaidEventListener = com.google.android.gms.ads.OnPaidEventListener { value ->
                        AdLogger.i("Rewarded Ad Revenue captured: ${value.valueMicros} ${value.currencyCode}")
                        RewardAnalytics.logRevenue(
                            adUnitId = AdConstants.REWARDED_AD_UNIT_ID,
                            valueMicros = value.valueMicros,
                            currencyCode = value.currencyCode,
                            precision = value.precisionType
                        )
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    AdLogger.e("Rewarded Ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
                    isLoading = false
                    RewardAnalytics.logRewardFailed(
                        adUnitId = AdConstants.REWARDED_AD_UNIT_ID,
                        errorCode = loadAdError.code,
                        errorMessage = loadAdError.message
                    )
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
            AdLogger.w("Max retry count reached for preloading rewarded ad.")
            return
        }
        retryCount++
        val delayMillis = (retryCount * 5000L).coerceAtMost(30000L)
        AdLogger.d("Scheduling rewarded ad preload retry #$retryCount in ${delayMillis / 1000}s")
        scope.launch {
            delay(delayMillis)
            preload(context)
        }
    }

    fun isAdReady(context: Context): Boolean {
        val ad = rewardedAd
        if (ad != null) {
            if (isExpired()) {
                AdLogger.w("Rewarded ad expired. Destroying and reloading...")
                rewardedAd = null
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

    fun showAd(
        activity: Activity,
        rewardType: RewardType,
        callbacks: RewardCallback
    ) {
        if (isExpired()) {
            AdLogger.w("Ad expired before show. Rejecting show and triggering reload.")
            rewardedAd = null
            callbacks.onAdFailedToShow("Ad session expired. Please try again.")
            preload(activity)
            return
        }

        val currentAd = rewardedAd
        if (currentAd == null) {
            AdLogger.w("Show ad requested but rewarded ad was null. Initiating reload.")
            callbacks.onAdFailedToShow("Ad not ready or loaded yet.")
            preload(activity)
            return
        }

        // Prevent rapid duplicate requests using RewardQueue
        if (!RewardQueue.canProcessRequest()) {
            callbacks.onAdFailedToShow("Action throttled. Please try again.")
            return
        }

        val activityRef = WeakReference(activity)
        var userEarnedReward = false
        val token = RewardQueue.generateUniqueToken()

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                AdLogger.i("Rewarded ad showed full screen content for $rewardType.")
                RewardAnalytics.logRewardOpened(AdConstants.REWARDED_AD_UNIT_ID, rewardType.name)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                AdLogger.e("Rewarded ad failed to show: ${error.message}")
                rewardedAd = null
                callbacks.onAdFailedToShow(error.message)
                
                // Track failed ad analytics
                RewardAnalytics.logRewardFailed(AdConstants.REWARDED_AD_UNIT_ID, error.code, error.message)
                
                preload(activityRef.get() ?: activity)
            }

            override fun onAdDismissedFullScreenContent() {
                AdLogger.i("Rewarded ad dismissed full screen content.")
                rewardedAd = null
                RewardAnalytics.logRewardClosed(AdConstants.REWARDED_AD_UNIT_ID, rewardType.name, userEarnedReward)
                callbacks.onAdClosed(userEarnedReward)
                
                // Preload the next ad instantly to always keep one ad ready in background
                preload(activityRef.get() ?: activity)
            }
        }

        val act = activityRef.get()
        if (act == null || act.isFinishing || act.isDestroyed) {
            AdLogger.e("Activity context is finished or destroyed. Aborting show.")
            callbacks.onAdFailedToShow("Activity is no longer valid.")
            return
        }

        currentAd.show(act) { rewardItem ->
            userEarnedReward = true
            AdLogger.i("User earned reward for type $rewardType: ${rewardItem.amount} ${rewardItem.type}")
            RewardAnalytics.logRewardEarned(rewardType.name, rewardItem.amount)
            callbacks.onRewardEarned(rewardType, rewardItem.amount, token)
        }
    }
}
