package com.myplaywin.app.blockmaster.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.myplaywin.app.blockmaster.security.BlockMasterAntiCheat
import com.playwin.ads.RewardCallback
import com.playwin.ads.RewardType
import com.playwin.ads.RewardedManager

object BlockMasterAdEngine {

    private const val TAG = "BlockMasterAdEngine"

    fun preload(context: Context) {
        try {
            RewardedManager.preload(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error preloading Rewarded Ad: ${e.message}")
        }
    }

    fun isAdReady(context: Context): Boolean {
        return try {
            RewardedManager.isAdReady(context)
        } catch (e: Exception) {
            false
        }
    }

    fun showRewardedAd(
        activity: Activity,
        rewardType: RewardType,
        onSuccess: (RewardType, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        // Anti-Cheat Check
        val (allowed, reason) = BlockMasterAntiCheat.canWatchRewardedAd()
        if (!allowed) {
            val msg = reason ?: "Ad unavailable at this time."
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
            onError(msg)
            return
        }

        if (!RewardedManager.isAdReady(activity)) {
            RewardedManager.preload(activity)
            val msg = "Ad is loading. Please try again in a few seconds."
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
            onError(msg)
            return
        }

        RewardedManager.showAd(
            activity = activity,
            rewardType = rewardType,
            callbacks = object : RewardCallback {
                override fun onRewardEarned(type: RewardType, amount: Int, token: String) {
                    Log.d(TAG, "[AD REWARD SUCCESS] Earned ad reward for type: $type")
                    BlockMasterAntiCheat.recordRewardedAdWatched()
                    onSuccess(type, amount)
                }

                override fun onAdFailedToLoad(errorCode: Int, errorMessage: String) {
                    Log.w(TAG, "[AD FAILED TO LOAD] code: $errorCode, msg: $errorMessage")
                    onError(errorMessage)
                }

                override fun onAdFailedToShow(error: String) {
                    Log.w(TAG, "[AD FAILED TO SHOW] $error")
                    RewardedManager.preload(activity)
                    Toast.makeText(activity, "Ad failed to display: $error", Toast.LENGTH_SHORT).show()
                    onError(error)
                }

                override fun onAdClosed(rewardEarned: Boolean) {
                    Log.d(TAG, "Ad dismissed. Reward earned status: $rewardEarned")
                    RewardedManager.preload(activity)
                }
            }
        )
    }
}
