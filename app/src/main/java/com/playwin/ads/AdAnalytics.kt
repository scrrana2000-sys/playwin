package com.playwin.ads

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AdAnalytics {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
            AdLogger.i("Firebase Analytics for Ads initialized successfully.")
        } catch (e: Exception) {
            AdLogger.e("Failed to initialize Firebase Analytics for Ads", e)
        }
    }

    private fun logEvent(eventName: String, params: Bundle? = null) {
        try {
            firebaseAnalytics?.logEvent(eventName, params)
            AdLogger.i("[Analytics Logged] Event: $eventName, params: ${params ?: "None"}")
        } catch (e: Exception) {
            AdLogger.e("Failed to log event: $eventName", e)
        }
    }

    fun logBannerLoaded(adUnitId: String) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
        }
        logEvent("banner_loaded", bundle)
    }

    fun logBannerClicked(adUnitId: String) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
        }
        logEvent("banner_clicked", bundle)
    }

    fun logBannerFailed(adUnitId: String, errorCode: Int, errorMessage: String) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
            putInt("error_code", errorCode)
            putString("error_message", errorMessage)
        }
        logEvent("banner_failed", bundle)
    }

    fun logNativeLoaded(adUnitId: String) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
        }
        logEvent("native_loaded", bundle)
    }

    fun logNativeClicked(adUnitId: String) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
        }
        logEvent("native_clicked", bundle)
    }

    fun logNativeFailed(adUnitId: String, errorCode: Int, errorMessage: String) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
            putInt("error_code", errorCode)
            putString("error_message", errorMessage)
        }
        logEvent("native_failed", bundle)
    }

    fun logNativeImpression(adUnitId: String) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
        }
        logEvent("native_impression", bundle)
    }
}
