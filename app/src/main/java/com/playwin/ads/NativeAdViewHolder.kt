package com.playwin.ads

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

class NativeAdViewHolder(private val nativeAdView: NativeAdView) {
    
    fun bind(nativeAd: NativeAd) {
        val headlineView = nativeAdView.headlineView as? TextView
        if (headlineView != null) {
            headlineView.text = nativeAd.headline
            headlineView.visibility = View.VISIBLE
        }

        val bodyView = nativeAdView.bodyView as? TextView
        if (bodyView != null) {
            if (nativeAd.body != null) {
                bodyView.text = nativeAd.body
                bodyView.visibility = View.VISIBLE
            } else {
                bodyView.visibility = View.GONE
            }
        }

        val iconView = nativeAdView.iconView as? ImageView
        if (iconView != null) {
            if (nativeAd.icon != null) {
                iconView.setImageDrawable(nativeAd.icon?.drawable)
                iconView.visibility = View.VISIBLE
            } else {
                iconView.visibility = View.GONE
            }
        }

        val callToActionView = nativeAdView.callToActionView as? Button
        if (callToActionView != null) {
            if (nativeAd.callToAction != null) {
                callToActionView.text = nativeAd.callToAction
                callToActionView.visibility = View.VISIBLE
            } else {
                callToActionView.visibility = View.GONE
            }
        }

        val advertiserView = nativeAdView.advertiserView as? TextView
        if (advertiserView != null) {
            if (nativeAd.advertiser != null) {
                advertiserView.text = nativeAd.advertiser
                advertiserView.visibility = View.VISIBLE
            } else {
                advertiserView.visibility = View.GONE
            }
        }

        val storeView = nativeAdView.storeView as? TextView
        if (storeView != null) {
            if (nativeAd.store != null) {
                storeView.text = nativeAd.store
                storeView.visibility = View.VISIBLE
            } else {
                storeView.visibility = View.GONE
            }
        }

        val priceView = nativeAdView.priceView as? TextView
        if (priceView != null) {
            if (nativeAd.price != null) {
                priceView.text = nativeAd.price
                priceView.visibility = View.VISIBLE
            } else {
                priceView.visibility = View.GONE
            }
        }

        val starRatingView = nativeAdView.starRatingView as? TextView
        if (starRatingView != null) {
            if (nativeAd.starRating != null) {
                starRatingView.text = "★ ${nativeAd.starRating}"
                starRatingView.visibility = View.VISIBLE
            } else {
                starRatingView.visibility = View.GONE
            }
        }

        nativeAdView.setNativeAd(nativeAd)
    }
}
