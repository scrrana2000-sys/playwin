package com.playwin.ads

interface RewardCallback {
    fun onRewardEarned(rewardType: RewardType, amount: Int, token: String)
    fun onAdFailedToLoad(errorCode: Int, errorMessage: String)
    fun onAdFailedToShow(errorMessage: String)
    fun onAdClosed(userEarnedReward: Boolean)
}
