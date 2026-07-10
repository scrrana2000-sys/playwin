package com.playwin.ads

sealed class RewardEvents {
    data class AdLoaded(val adUnitId: String) : RewardEvents()
    data class AdFailedToLoad(val adUnitId: String, val errorCode: Int, val errorMessage: String) : RewardEvents()
    data class AdOpened(val adUnitId: String, val rewardType: RewardType) : RewardEvents()
    data class AdClosed(val adUnitId: String, val rewardType: RewardType, val earned: Boolean) : RewardEvents()
    data class RewardEarned(val rewardType: RewardType, val amount: Int, val token: String) : RewardEvents()
    data class RewardFailed(val rewardType: RewardType, val reason: String) : RewardEvents()
}
