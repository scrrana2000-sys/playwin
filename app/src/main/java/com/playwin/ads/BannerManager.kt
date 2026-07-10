package com.playwin.ads

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object BannerManager {
    private var preloadedAdView: AdView? = null
    private var isLoading = false
    private var retryCount = 0
    private const val MAX_RETRY_COUNT = 5
    private val scope = CoroutineScope(Dispatchers.Main)

    private fun getAdSize(context: Context): AdSize {
        val displayMetrics = context.resources.displayMetrics
        val widthPixels = displayMetrics.widthPixels
        val density = displayMetrics.density
        val widthInDp = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthInDp)
    }

    fun loadBanner(context: Context, callbacks: AdCallbacks.Banner? = null) {
        val appContext = context.applicationContext
        if (preloadedAdView != null || isLoading) {
            AdLogger.d("Banner ad already preloaded or currently loading.")
            return
        }

        val networkMonitor = NetworkMonitor(appContext)
        if (!networkMonitor.isNetworkAvailable()) {
            AdLogger.w("Banner ad failed to load: Network unavailable.")
            scheduleRetry(appContext, callbacks)
            return
        }

        isLoading = true
        AdLogger.i("Preloading Banner Ad...")

        try {
            val adSize = getAdSize(appContext)
            val adView = AdView(appContext).apply {
                adUnitId = AdConstants.BANNER_AD_UNIT_ID
                setAdSize(adSize)
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        AdLogger.i("Banner ad loaded successfully.")
                        this@BannerManager.isLoading = false
                        retryCount = 0
                        callbacks?.onAdLoaded()
                        AdAnalytics.logBannerLoaded(AdConstants.BANNER_AD_UNIT_ID)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        AdLogger.e("Banner ad failed to load: ${error.message} (Code: ${error.code})")
                        this@BannerManager.isLoading = false
                        preloadedAdView = null
                        callbacks?.onAdFailedToLoad(error.code, error.message)
                        AdAnalytics.logBannerFailed(AdConstants.BANNER_AD_UNIT_ID, error.code, error.message)
                        scheduleRetry(appContext, callbacks)
                    }

                    override fun onAdClicked() {
                        AdLogger.i("Banner ad clicked.")
                        callbacks?.onAdClicked()
                        AdAnalytics.logBannerClicked(AdConstants.BANNER_AD_UNIT_ID)
                    }

                    override fun onAdImpression() {
                        AdLogger.d("Banner ad impression registered.")
                        callbacks?.onAdImpression()
                    }
                }
            }

            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            preloadedAdView = adView
        } catch (e: Exception) {
            AdLogger.e("Error preloading banner ad", e)
            isLoading = false
        }
    }

    private fun scheduleRetry(context: Context, callbacks: AdCallbacks.Banner?) {
        if (retryCount >= MAX_RETRY_COUNT) {
            AdLogger.w("Max retry count reached for loading banner ad.")
            return
        }
        retryCount++
        val delayMillis = (retryCount * 5000L).coerceAtMost(30000L)
        AdLogger.d("Scheduling banner ad retry #$retryCount in ${delayMillis / 1000}s")
        scope.launch {
            delay(delayMillis)
            loadBanner(context, callbacks)
        }
    }

    fun destroy() {
        preloadedAdView?.destroy()
        preloadedAdView = null
        isLoading = false
        AdLogger.d("Banner ad cache destroyed.")
    }

    @Composable
    fun BannerAd(
        modifier: Modifier = Modifier,
        callbacks: AdCallbacks.Banner? = null
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val adView = remember(context) {
            val preloaded = preloadedAdView
            if (preloaded != null) {
                preloadedAdView = null // Consume preloaded ad
                loadBanner(context) // Preload the next banner in background
                preloaded
            } else {
                AdView(context).apply {
                    adUnitId = AdConstants.BANNER_AD_UNIT_ID
                    setAdSize(getAdSize(context))
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            AdLogger.i("Fallback local banner ad loaded.")
                            callbacks?.onAdLoaded()
                            AdAnalytics.logBannerLoaded(AdConstants.BANNER_AD_UNIT_ID)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            AdLogger.e("Fallback local banner ad failed: ${error.message}")
                            callbacks?.onAdFailedToLoad(error.code, error.message)
                            AdAnalytics.logBannerFailed(AdConstants.BANNER_AD_UNIT_ID, error.code, error.message)
                        }

                        override fun onAdClicked() {
                            callbacks?.onAdClicked()
                            AdAnalytics.logBannerClicked(AdConstants.BANNER_AD_UNIT_ID)
                        }

                        override fun onAdImpression() {
                            callbacks?.onAdImpression()
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        }

        // Manage clean lifecycles for the displayed AdView
        DisposableEffect(adView, lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> adView.pause()
                    Lifecycle.Event.ON_RESUME -> adView.resume()
                    Lifecycle.Event.ON_DESTROY -> adView.destroy()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                adView.destroy()
            }
        }

        val heightDp = getAdSize(context).height
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(heightDp.dp),
            factory = {
                (adView.parent as? android.view.ViewGroup)?.removeView(adView)
                adView
            }
        )
    }
}
