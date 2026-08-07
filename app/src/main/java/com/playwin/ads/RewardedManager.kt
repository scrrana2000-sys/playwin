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
    private const val MAX_RETRY_COUNT = 6
    private val scope = CoroutineScope(Dispatchers.Main)

    // Cache management
    private var loadTime = 0L
    private const val CACHE_DURATION_MS = 4 * 60 * 60 * 1000L // 4 hours standard AdMob cache limit

    @Synchronized
    fun preload(context: Context) {
        val appContext = context.applicationContext
        if (rewardedAd != null && !isExpired()) {
            AdLogger.d("Rewarded ad already loaded and is valid in memory.")
            return
        }
        if (isLoading) {
            AdLogger.d("Rewarded ad is currently loading in background.")
            return
        }

        val networkMonitor = NetworkMonitor(appContext)
        if (!networkMonitor.isNetworkAvailable()) {
            AdLogger.w("Cannot preload rewarded ad: Network unavailable.")
            scheduleRetry(appContext)
            return
        }

        isLoading = true
        AdLogger.i("Preloading Rewarded Ad for background cache...")

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            appContext,
            AdConstants.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    AdLogger.i("Rewarded Ad preloaded successfully into memory.")
                    // Ensure single instance in memory to prevent memory leaks
                    rewardedAd?.let { oldAd ->
                        oldAd.fullScreenContentCallback = null
                    }
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
                    // Retry with exponential backoff
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
        val delaySec = Math.pow(2.0, retryCount.toDouble()).toLong().coerceAtMost(60L)
        val delayMillis = delaySec * 1000L
        AdLogger.d("Scheduling rewarded ad preload retry #$retryCount with exponential backoff in ${delaySec}s")
        scope.launch {
            delay(delayMillis)
            preload(context)
        }
    }

    fun isAdReady(context: Context): Boolean {
        val ad = rewardedAd
        if (ad != null) {
            if (isExpired()) {
                AdLogger.w("Rewarded ad expired. Clearing memory and reloading...")
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
            AdLogger.w("Ad expired before show. Rejecting show and triggering background reload.")
            rewardedAd = null
            callbacks.onAdFailedToShow("Ad session expired. Please try again.")
            preload(activity)
            return
        }

        val currentAd = rewardedAd
        if (currentAd == null) {
            AdLogger.w("Show ad requested but rewarded ad was null. Initiating background preload.")
            callbacks.onAdFailedToShow("Ad not ready or loaded yet.")
            preload(activity)
            return
        }

        // Prevent rapid duplicate requests using RewardQueue
        if (!RewardQueue.canProcessRequest()) {
            callbacks.onAdFailedToShow("Action throttled. Please try again.")
            return
        }

        // Clear single instance memory reference before presenting ad
        rewardedAd = null

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
                callbacks.onAdFailedToShow(error.message)
                
                // Track failed ad analytics
                RewardAnalytics.logRewardFailed(AdConstants.REWARDED_AD_UNIT_ID, error.code, error.message)
                
                // Immediately preload next ad on failure
                preload(activityRef.get() ?: activity)
            }

            override fun onAdDismissedFullScreenContent() {
                AdLogger.i("Rewarded ad dismissed full screen content.")
                RewardAnalytics.logRewardClosed(AdConstants.REWARDED_AD_UNIT_ID, rewardType.name, userEarnedReward)
                callbacks.onAdClosed(userEarnedReward)
                
                // Immediately preload the next ad after previous one is dismissed
                preload(activityRef.get() ?: activity)
            }
        }

        val act = activityRef.get()
        if (act == null || act.isFinishing || act.isDestroyed) {
            AdLogger.e("Activity context is finished or destroyed. Aborting show.")
            callbacks.onAdFailedToShow("Activity is no longer valid.")
            preload(activity)
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
