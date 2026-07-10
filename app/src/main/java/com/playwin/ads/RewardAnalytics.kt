package com.playwin.ads

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object RewardAnalytics {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
            AdLogger.i("Firebase Analytics for Rewards initialized successfully.")
        } catch (e: Exception) {
            AdLogger.e("Failed to initialize Firebase Analytics for Rewards: ${e.message}")
        }
    }

    private fun logEvent(eventName: String, params: Bundle? = null) {
        try {
            firebaseAnalytics?.logEvent(eventName, params)
            AdLogger.i("[Reward Analytics Logged] Event: $eventName, params: ${params ?: "None"}")
        } catch (e: Exception) {
            AdLogger.e("Failed to log Reward event $eventName: ${e.message}")
        }
    }

    fun logRewardLoaded(adUnitId: String) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
        }
        logEvent("reward_loaded", bundle)
    }

    fun logRewardFailed(adUnitId: String, errorCode: Int, errorMessage: String) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
            putInt("error_code", errorCode)
            putString("error_message", errorMessage)
        }
        logEvent("reward_failed", bundle)
    }

    fun logRewardOpened(adUnitId: String, rewardType: String) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
            putString("reward_type", rewardType)
        }
        logEvent("reward_opened", bundle)
    }

    fun logRewardClosed(adUnitId: String, rewardType: String, earned: Boolean) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
            putString("reward_type", rewardType)
            putBoolean("earned", earned)
        }
        logEvent("reward_closed", bundle)
    }

    fun logRewardEarned(rewardType: String, value: Int) {
        val bundle = Bundle().apply {
            putString("reward_type", rewardType)
            putInt("reward_value", value)
        }
        logEvent("reward_earned", bundle)
    }

    fun logRevenue(adUnitId: String, valueMicros: Long, currencyCode: String, precision: Int) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
            putLong("value_micros", valueMicros)
            putString("currency", currencyCode)
            putInt("precision", precision)
            putDouble("value_usd", valueMicros.toDouble() / 1000000.0)
        }
        logEvent("ad_revenue", bundle)
    }
}
