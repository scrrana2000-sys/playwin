package com.playwin.ads

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object NativeManager {
    private var preloadedAd: NativeAd? = null
    private var loadTime: Long = 0
    private const val CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutes cache expiry
    private var isLoading = false
    private var retryCount = 0
    private const val MAX_RETRY_COUNT = 5
    private val scope = CoroutineScope(Dispatchers.Main)

    fun preload(context: Context, callbacks: AdCallbacks.Native? = null) {
        val appContext = context.applicationContext
        if (preloadedAd != null && !isExpired()) {
            AdLogger.d("Native Ad already preloaded and valid.")
            return
        }
        if (isLoading) {
            AdLogger.d("Native Ad is currently loading.")
            return
        }

        val networkMonitor = NetworkMonitor(appContext)
        if (!networkMonitor.isNetworkAvailable()) {
            AdLogger.w("Cannot preload Native Ad: Network unavailable.")
            scheduleRetry(appContext, callbacks)
            return
        }

        isLoading = true
        AdLogger.i("Preloading next Native Ad...")

        try {
            val adLoader = AdLoader.Builder(appContext, AdConstants.NATIVE_AD_UNIT_ID)
                .forNativeAd { nativeAd ->
                    AdLogger.i("Native Ad preloaded successfully.")
                    preloadedAd?.destroy() // Destroy old preloaded ad
                    preloadedAd = nativeAd
                    loadTime = System.currentTimeMillis()
                    isLoading = false
                    retryCount = 0
                    callbacks?.onAdLoaded()
                    AdAnalytics.logNativeLoaded(AdConstants.NATIVE_AD_UNIT_ID)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        AdLogger.e("Native Ad preload failed: ${error.message} (Code: ${error.code})")
                        isLoading = false
                        callbacks?.onAdFailedToLoad(error.code, error.message)
                        AdAnalytics.logNativeFailed(AdConstants.NATIVE_AD_UNIT_ID, error.code, error.message)
                        scheduleRetry(appContext, callbacks)
                    }

                    override fun onAdClicked() {
                        AdLogger.i("Native Ad clicked.")
                        callbacks?.onAdClicked()
                        AdAnalytics.logNativeClicked(AdConstants.NATIVE_AD_UNIT_ID)
                    }

                    override fun onAdImpression() {
                        AdLogger.i("Native Ad impression registered.")
                        callbacks?.onAdImpression()
                        AdAnalytics.logNativeImpression(AdConstants.NATIVE_AD_UNIT_ID)
                    }
                })
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            AdLogger.e("Error preloading native ad", e)
            isLoading = false
        }
    }

    private fun isExpired(): Boolean {
        return System.currentTimeMillis() - loadTime > CACHE_DURATION_MS
    }

    private fun scheduleRetry(context: Context, callbacks: AdCallbacks.Native?) {
        if (retryCount >= MAX_RETRY_COUNT) {
            AdLogger.w("Max retry count reached for preloading native ad.")
            return
        }
        retryCount++
        val delaySec = Math.pow(2.0, retryCount.toDouble()).toLong().coerceAtMost(60L)
        val delayMillis = delaySec * 1000L
        AdLogger.d("Scheduling native ad preload retry #$retryCount with exponential backoff in ${delaySec}s")
        scope.launch {
            delay(delayMillis)
            preload(context, callbacks)
        }
    }

    fun getAd(context: Context): NativeAd? {
        val ad = preloadedAd
        if (ad != null && !isExpired()) {
            preloadedAd = null // Consume the ad
            preload(context)   // Preload the next one immediately
            return ad
        } else {
            if (isExpired()) {
                preloadedAd?.destroy()
                preloadedAd = null
            }
            preload(context) // Ensure loading is triggered
            return null
        }
    }

    fun loadNativeAd(
        context: Context,
        callbacks: AdCallbacks.Native? = null,
        onAdLoaded: (NativeAd) -> Unit
    ) {
        val appContext = context.applicationContext
        val networkMonitor = NetworkMonitor(appContext)
        if (!networkMonitor.isNetworkAvailable()) {
            callbacks?.onAdFailedToLoad(-1, "Network unavailable")
            return
        }

        val adLoader = AdLoader.Builder(appContext, AdConstants.NATIVE_AD_UNIT_ID)
            .forNativeAd { nativeAd ->
                onAdLoaded(nativeAd)
                callbacks?.onAdLoaded()
                AdAnalytics.logNativeLoaded(AdConstants.NATIVE_AD_UNIT_ID)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    callbacks?.onAdFailedToLoad(error.code, error.message)
                    AdAnalytics.logNativeFailed(AdConstants.NATIVE_AD_UNIT_ID, error.code, error.message)
                }
                override fun onAdClicked() {
                    callbacks?.onAdClicked()
                    AdAnalytics.logNativeClicked(AdConstants.NATIVE_AD_UNIT_ID)
                }
                override fun onAdImpression() {
                    callbacks?.onAdImpression()
                    AdAnalytics.logNativeImpression(AdConstants.NATIVE_AD_UNIT_ID)
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun destroy() {
        preloadedAd?.destroy()
        preloadedAd = null
        AdLogger.d("NativeManager cached ad destroyed.")
    }

    @Composable
    fun NativeAd(
        modifier: Modifier = Modifier,
        callbacks: AdCallbacks.Native? = null
    ) {
        val context = LocalContext.current
        var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }

        DisposableEffect(Unit) {
            val ad = getAd(context)
            if (ad != null) {
                nativeAdState = ad
            } else {
                loadNativeAd(context, callbacks) { loadedAd ->
                    nativeAdState = loadedAd
                }
            }
            onDispose {
                nativeAdState?.destroy()
            }
        }

        val ad = nativeAdState
        if (ad != null) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    factory = { ctx ->
                        val density = ctx.resources.displayMetrics.density
                        val nativeAdView = NativeAdView(ctx)
                        
                        val mainLayout = LinearLayout(ctx).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(
                                (16 * density).toInt(),
                                (16 * density).toInt(),
                                (16 * density).toInt(),
                                (16 * density).toInt()
                            )
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        // Ad badge + advertiser row
                        val headerRow = LinearLayout(ctx).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = android.view.Gravity.CENTER_VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        val adBadge = TextView(ctx).apply {
                            text = "Ad"
                            textSize = 10f
                            setTextColor(android.graphics.Color.WHITE)
                            setBackgroundColor(android.graphics.Color.parseColor("#E0A900"))
                            setPadding(
                                (6 * density).toInt(),
                                (2 * density).toInt(),
                                (6 * density).toInt(),
                                (2 * density).toInt()
                            )
                            gravity = android.view.Gravity.CENTER
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                rightMargin = (8 * density).toInt()
                            }
                        }

                        val advertiserText = TextView(ctx).apply {
                            textSize = 11f
                            setTextColor(android.graphics.Color.GRAY)
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        headerRow.addView(adBadge)
                        headerRow.addView(advertiserText)
                        mainLayout.addView(headerRow)

                        // Content row (Icon + Headline/Body)
                        val contentRow = LinearLayout(ctx).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = android.view.Gravity.CENTER_VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topMargin = (8 * density).toInt()
                            }
                        }

                        val adIconView = ImageView(ctx).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                (40 * density).toInt(),
                                (40 * density).toInt()
                            ).apply {
                                rightMargin = (12 * density).toInt()
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }

                        val textLayout = LinearLayout(ctx).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        }

                        val headlineView = TextView(ctx).apply {
                            textSize = 14f
                            setTextColor(android.graphics.Color.WHITE)
                            setTypeface(null, android.graphics.Typeface.BOLD)
                        }

                        val bodyView = TextView(ctx).apply {
                            textSize = 12f
                            setTextColor(android.graphics.Color.LTGRAY)
                        }

                        textLayout.addView(headlineView)
                        textLayout.addView(bodyView)

                        contentRow.addView(adIconView)
                        contentRow.addView(textLayout)
                        mainLayout.addView(contentRow)

                        // Media Content View
                        val mediaView = com.google.android.gms.ads.nativead.MediaView(ctx).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (180 * density).toInt()
                            ).apply {
                                topMargin = (12 * density).toInt()
                            }
                        }
                        mainLayout.addView(mediaView)

                        // Bottom info/button row
                        val bottomRow = LinearLayout(ctx).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = android.view.Gravity.CENTER_VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topMargin = (12 * density).toInt()
                            }
                        }

                        val priceView = TextView(ctx).apply {
                            textSize = 11f
                            setTextColor(android.graphics.Color.WHITE)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                rightMargin = (8 * density).toInt()
                            }
                        }

                        val storeView = TextView(ctx).apply {
                            textSize = 11f
                            setTextColor(android.graphics.Color.WHITE)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                rightMargin = (8 * density).toInt()
                            }
                        }

                        val ratingView = TextView(ctx).apply {
                            textSize = 11f
                            setTextColor(android.graphics.Color.YELLOW)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                rightMargin = (8 * density).toInt()
                            }
                        }

                        val callToActionView = Button(ctx).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            setBackgroundColor(android.graphics.Color.parseColor("#7C4DFF"))
                            setTextColor(android.graphics.Color.WHITE)
                            textSize = 11f
                            setAllCaps(false)
                            setPadding(
                                (12 * density).toInt(),
                                (6 * density).toInt(),
                                (12 * density).toInt(),
                                (6 * density).toInt()
                            )
                        }

                        bottomRow.addView(priceView)
                        bottomRow.addView(storeView)
                        bottomRow.addView(ratingView)
                        bottomRow.addView(callToActionView)
                        mainLayout.addView(bottomRow)

                        nativeAdView.addView(mainLayout)

                        // Bind NativeAdView fields
                        nativeAdView.headlineView = headlineView
                        nativeAdView.bodyView = bodyView
                        nativeAdView.iconView = adIconView
                        nativeAdView.callToActionView = callToActionView
                        nativeAdView.mediaView = mediaView
                        nativeAdView.advertiserView = advertiserText
                        nativeAdView.priceView = priceView
                        nativeAdView.starRatingView = ratingView
                        nativeAdView.storeView = storeView

                        // Populate fields (Headline is required)
                        headlineView.text = ad.headline ?: ""
                        headlineView.visibility = if (ad.headline.isNullOrEmpty()) View.GONE else View.VISIBLE
                        
                        if (ad.body != null) {
                            bodyView.text = ad.body
                            bodyView.visibility = View.VISIBLE
                        } else {
                            bodyView.visibility = View.GONE
                        }

                        if (ad.icon != null) {
                            adIconView.setImageDrawable(ad.icon?.drawable)
                            adIconView.visibility = View.VISIBLE
                        } else {
                            adIconView.visibility = View.GONE
                        }

                        if (ad.advertiser != null) {
                            advertiserText.text = ad.advertiser
                            advertiserText.visibility = View.VISIBLE
                        } else {
                            advertiserText.visibility = View.GONE
                        }

                        if (ad.price != null) {
                            priceView.text = ad.price
                            priceView.visibility = View.VISIBLE
                        } else {
                            priceView.visibility = View.GONE
                        }

                        if (ad.store != null) {
                            storeView.text = ad.store
                            storeView.visibility = View.VISIBLE
                        } else {
                            storeView.visibility = View.GONE
                        }

                        if (ad.starRating != null) {
                            ratingView.text = "★ ${ad.starRating}"
                            ratingView.visibility = View.VISIBLE
                        } else {
                            ratingView.visibility = View.GONE
                        }

                        if (ad.callToAction != null) {
                            callToActionView.text = ad.callToAction
                            callToActionView.visibility = View.VISIBLE
                        } else {
                            callToActionView.visibility = View.GONE
                        }

                        if (ad.mediaContent != null) {
                            mediaView.setMediaContent(ad.mediaContent)
                            mediaView.visibility = View.VISIBLE
                        } else {
                            mediaView.visibility = View.GONE
                        }

                        nativeAdView.setNativeAd(ad)
                        nativeAdView
                    },
                    update = {
                        // Handled dynamically
                    }
                )
            }
        }
    }
}
