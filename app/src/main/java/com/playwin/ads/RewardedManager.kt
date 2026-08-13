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

enum class AdState {
    IDLE,
    LOADING,
    READY,
    SHOWING,
    FAILED
}

object RewardedManager {
    private var adState: AdState = AdState.IDLE
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var retryCount = 0
    private const val MAX_RETRY_COUNT = 3
    private val scope = CoroutineScope(Dispatchers.Main)

    private var loadTime = 0L
    private const val CACHE_DURATION_MS = 4 * 60 * 60 * 1000L // 4 hours standard AdMob cache limit

    private data class PendingShowRequest(
        val activityRef: WeakReference<Activity>,
        val rewardType: RewardType,
        val callbacks: RewardCallback,
        val source: String
    )

    private val pendingShowRequests = mutableListOf<PendingShowRequest>()
    private val pendingLoadCallbacks = mutableListOf<(Boolean, String?) -> Unit>()

    fun getAdState(): AdState = adState
    fun isLoading(): Boolean = isLoading || adState == AdState.LOADING

    @Synchronized
    fun preload(context: Context, onComplete: ((Boolean, String?) -> Unit)? = null) {
        val appContext = context.applicationContext
        
        if (rewardedAd != null && !isExpired()) {
            adState = AdState.READY
            AdLogger.d("Rewarded: Ad already cached and valid in memory.")
            onComplete?.invoke(true, null)
            return
        }

        if (isLoading || adState == AdState.LOADING) {
            AdLogger.d("Rewarded: Ad load already in progress. Request queued.")
            onComplete?.let { pendingLoadCallbacks.add(it) }
            return
        }

        if (adState == AdState.SHOWING) {
            AdLogger.d("Rewarded: Currently showing an ad. Preload postponed.")
            onComplete?.invoke(false, "Ad is currently showing")
            return
        }

        val networkMonitor = NetworkMonitor(appContext)
        if (!networkMonitor.isNetworkAvailable()) {
            adState = AdState.FAILED
            AdLogger.w("Rewarded: LOAD_FAILED Network Unavailable")
            onComplete?.invoke(false, "Network Unavailable")
            scheduleRetry(appContext)
            return
        }

        isLoading = true
        adState = AdState.LOADING
        onComplete?.let { pendingLoadCallbacks.add(it) }
        AdLogger.i("Rewarded: LOAD_START (adUnit = ${AdConstants.REWARDED_AD_UNIT_ID})")

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            appContext,
            AdConstants.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    AdLogger.i("Rewarded: LOAD_SUCCESS")
                    rewardedAd?.let { oldAd ->
                        oldAd.fullScreenContentCallback = null
                    }
                    rewardedAd = ad
                    loadTime = System.currentTimeMillis()
                    isLoading = false
                    adState = AdState.READY
                    retryCount = 0
                    RewardAnalytics.logRewardLoaded(AdConstants.REWARDED_AD_UNIT_ID)

                    // Flush pending load callbacks
                    val loadCallbacks = pendingLoadCallbacks.toList()
                    pendingLoadCallbacks.clear()
                    loadCallbacks.forEach { it.invoke(true, null) }

                    // Process pending show request if any
                    if (pendingShowRequests.isNotEmpty()) {
                        val request = pendingShowRequests.removeAt(0)
                        val act = request.activityRef.get()
                        if (act != null && !act.isFinishing && !act.isDestroyed) {
                            showAdInternal(act, request.rewardType, request.callbacks, request.source)
                        } else {
                            request.callbacks.onAdFailedToShow("Activity context is no longer valid.")
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    AdLogger.e("Rewarded: LOAD_FAILED Code=${loadAdError.code}, Message=${loadAdError.message}, Domain=${loadAdError.domain}")
                    rewardedAd = null
                    isLoading = false
                    adState = AdState.FAILED
                    RewardAnalytics.logRewardFailed(
                        adUnitId = AdConstants.REWARDED_AD_UNIT_ID,
                        errorCode = loadAdError.code,
                        errorMessage = loadAdError.message
                    )

                    // Flush pending load callbacks
                    val loadCallbacks = pendingLoadCallbacks.toList()
                    pendingLoadCallbacks.clear()
                    loadCallbacks.forEach { it.invoke(false, loadAdError.message) }

                    // Notify pending show requests of failure
                    val showRequests = pendingShowRequests.toList()
                    pendingShowRequests.clear()
                    showRequests.forEach { req ->
                        req.callbacks.onAdFailedToLoad(loadAdError.code, loadAdError.message)
                    }

                    // Controlled retry with backoff
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
            AdLogger.w("Rewarded: Max retry count ($MAX_RETRY_COUNT) reached. Stopping auto-retry.")
            return
        }
        retryCount++
        val delays = arrayOf(4L, 12L, 30L)
        val delaySec = delays.getOrElse(retryCount - 1) { 30L }
        val delayMillis = delaySec * 1000L
        AdLogger.d("Rewarded: Scheduling retry #$retryCount in ${delaySec}s")
        scope.launch {
            delay(delayMillis)
            preload(context)
        }
    }

    fun isAdReady(context: Context): Boolean {
        if (rewardedAd != null) {
            if (isExpired()) {
                AdLogger.w("Rewarded: Ad expired. Clearing memory and reloading...")
                rewardedAd = null
                adState = AdState.IDLE
                preload(context)
                return false
            }
            adState = AdState.READY
            return true
        }
        if (!isLoading && adState != AdState.LOADING) {
            preload(context)
        }
        return false
    }

    fun showAd(
        activity: Activity,
        rewardType: RewardType,
        callbacks: RewardCallback,
        source: String? = null
    ) {
        val resolvedSource = source ?: when (rewardType) {
            RewardType.SPIN -> "SPIN"
            RewardType.DAILY_TASK -> "WATCH_EARN"
            RewardType.QUIZ_LIFELINE -> "QUIZ_LIFELINE"
            RewardType.BINGO_SECOND_CHANCE -> "BINGO_SECOND_CHANCE"
            RewardType.BINGO_DOUBLE_REWARD -> "BINGO_DOUBLE_REWARD"
            RewardType.BINGO_DAILY_LOGIN_DOUBLE -> "BINGO_DAILY_LOGIN_DOUBLE"
            RewardType.SHADOW_HERO_DOUBLE_REWARD -> "SHADOW_HERO_DOUBLE_REWARD"
            RewardType.BLOCK_MASTER_CONTINUE -> "SHADOW_HERO_REVIVE"
            else -> "OTHER"
        }

        if (activity.isFinishing || activity.isDestroyed) {
            AdLogger.e("Rewarded: SHOW_FAILED Activity invalid")
            callbacks.onAdFailedToShow("Activity is no longer valid.")
            return
        }

        if (isAdReady(activity)) {
            showAdInternal(activity, rewardType, callbacks, resolvedSource)
        } else {
            if (isLoading || adState == AdState.LOADING) {
                AdLogger.i("Rewarded: Ad is currently loading. Show request queued.")
                pendingShowRequests.add(PendingShowRequest(WeakReference(activity), rewardType, callbacks, resolvedSource))
            } else {
                AdLogger.i("Rewarded: Ad not preloaded. Initiating single load operation and queuing show.")
                pendingShowRequests.add(PendingShowRequest(WeakReference(activity), rewardType, callbacks, resolvedSource))
                preload(activity)
            }
        }
    }

    private fun showAdInternal(
        activity: Activity,
        rewardType: RewardType,
        callbacks: RewardCallback,
        resolvedSource: String
    ) {
        val currentAd = rewardedAd
        if (currentAd == null) {
            AdLogger.w("Rewarded: SHOW_FAILED Ad is null")
            callbacks.onAdFailedToShow("Ad not ready.")
            preload(activity)
            return
        }

        if (!RewardQueue.canProcessRequest()) {
            AdLogger.w("Rewarded: Action throttled by RewardQueue")
            callbacks.onAdFailedToShow("Action throttled. Please try again.")
            return
        }

        adState = AdState.SHOWING
        rewardedAd = null // Consume instance

        val activityRef = WeakReference(activity)
        var rewardGranted = false
        val token = RewardQueue.generateUniqueToken()
        val adEventId = java.util.UUID.randomUUID().toString()

        currentAd.onPaidEventListener = com.google.android.gms.ads.OnPaidEventListener { value ->
            AdLogger.i("Rewarded: Revenue captured: ${value.valueMicros} ${value.currencyCode} (eventId=$adEventId)")
            RewardAnalytics.logRevenue(
                adUnitId = AdConstants.REWARDED_AD_UNIT_ID,
                valueMicros = value.valueMicros,
                currencyCode = value.currencyCode,
                precision = value.precisionType
            )
            TelemetryManager.logOrUpdateAdTelemetry(
                eventId = adEventId,
                adFormat = "REWARDED",
                adUnitId = AdConstants.REWARDED_AD_UNIT_ID,
                source = resolvedSource,
                rewardType = rewardType.name,
                valueMicros = value.valueMicros,
                currencyCode = value.currencyCode,
                precision = value.precisionType,
                revenueStatus = if (value.valueMicros > 0) "CONFIRMED" else "ZERO_VALUE"
            )
        }

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                AdLogger.i("Rewarded: SHOW_START for $rewardType (eventId=$adEventId)")
                RewardAnalytics.logRewardOpened(AdConstants.REWARDED_AD_UNIT_ID, rewardType.name)
                TelemetryManager.logOrUpdateAdTelemetry(
                    eventId = adEventId,
                    adFormat = "REWARDED",
                    adUnitId = AdConstants.REWARDED_AD_UNIT_ID,
                    source = resolvedSource,
                    rewardType = rewardType.name,
                    revenueStatus = "PENDING"
                )
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                adState = AdState.FAILED
                AdLogger.e("Rewarded: SHOW_FAILED Code=${error.code}, Message=${error.message}")
                callbacks.onAdFailedToShow(error.message)
                RewardAnalytics.logRewardFailed(AdConstants.REWARDED_AD_UNIT_ID, error.code, error.message)
                
                TelemetryManager.logOrUpdateAdTelemetry(
                    eventId = adEventId,
                    adFormat = "REWARDED",
                    adUnitId = AdConstants.REWARDED_AD_UNIT_ID,
                    source = resolvedSource,
                    rewardType = rewardType.name,
                    revenueStatus = "UNAVAILABLE"
                )

                preload(activityRef.get() ?: activity)
            }

            override fun onAdDismissedFullScreenContent() {
                adState = AdState.IDLE
                AdLogger.i("Rewarded: DISMISSED for $rewardType")

                TelemetryManager.finalizeAdEventIfPending(adEventId)

                callbacks.onAdClosed(rewardGranted)
                preload(activityRef.get() ?: activity)
            }
        }

        val act = activityRef.get()
        if (act == null || act.isFinishing || act.isDestroyed) {
            adState = AdState.FAILED
            AdLogger.e("Rewarded: SHOW_FAILED Activity invalid before show call")
            callbacks.onAdFailedToShow("Activity is no longer valid.")
            preload(activity)
            return
        }

        currentAd.show(act) { rewardItem ->
            if (!rewardGranted) {
                rewardGranted = true
                AdLogger.i("Rewarded: REWARD_GRANTED for type $rewardType: ${rewardItem.amount} ${rewardItem.type}")
                RewardAnalytics.logRewardEarned(rewardType.name, rewardItem.amount)

                TelemetryManager.logOrUpdateAdTelemetry(
                    eventId = adEventId,
                    rewardGranted = true,
                    rewardValue = rewardItem.amount
                )

                callbacks.onRewardEarned(rewardType, rewardItem.amount, token)
            }
        }
    }
}
