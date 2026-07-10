package com.playwin.ads

sealed class RewardState {
    object Idle : RewardState()
    object Loading : RewardState()
    data class Success(val rewardType: RewardType, val amount: Int, val message: String) : RewardState()
    data class Error(val message: String) : RewardState()
}
