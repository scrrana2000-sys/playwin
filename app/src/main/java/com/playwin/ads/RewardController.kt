package com.playwin.ads

import com.myplaywin.app.ui.viewmodel.PlayWinViewModel

object RewardController {
    
    interface RewardGrantCallback {
        fun onSuccess(message: String)
        fun onFailure(error: String)
    }

    fun grantReward(
        viewModel: PlayWinViewModel,
        rewardType: RewardType,
        token: String,
        callback: RewardGrantCallback
    ) {
        // Anti-cheat verification
        if (!RewardQueue.isTokenActive(token)) {
            callback.onFailure("Invalid reward token. Security check failed.")
            return
        }

        // Consume token to prevent double-claiming (replay protection)
        RewardQueue.consumeToken(token)

        when (rewardType) {
            RewardType.QUIZ_LIFELINE -> {
                // Lifelines are managed within active Quiz screens but verified through here
                callback.onSuccess("Successfully unlocked +2 Lifelines!")
            }
            RewardType.SPIN -> {
                viewModel.grantAdSpinRewardLocally()
                callback.onSuccess("Successfully unlocked +1 Spin Wheel!")
            }
            RewardType.DAILY_TASK -> {
                viewModel.claimDailyReward { success, errorMsg ->
                    if (success) {
                        callback.onSuccess("Daily Check-In task reward claimed successfully!")
                    } else {
                        callback.onFailure(errorMsg ?: "Failed to claim daily task reward.")
                    }
                }
            }
            RewardType.BONUS_COINS -> {
                viewModel.claimVideoAdReward { success, error ->
                    if (success) {
                        callback.onSuccess("Bonus Coins successfully added to your wallet!")
                    } else {
                        callback.onFailure(error ?: "Failed to claim bonus coins.")
                    }
                }
            }
            RewardType.PROFILE_REWARD -> {
                viewModel.claimUpgradedRewardedAdReward { success, error, isBonus ->
                    if (success) {
                        val msg = if (isBonus) {
                            "Upgraded Reward Coins Added! Daily Bonus Unlocked!"
                        } else {
                            "Upgraded Reward Coins successfully added to your wallet!"
                        }
                        callback.onSuccess(msg)
                    } else {
                        callback.onFailure(error ?: "Failed to claim upgraded reward.")
                    }
                }
            }
            RewardType.BLOCK_MASTER_CONTINUE,
            RewardType.BLOCK_MASTER_DOUBLE_REWARD,
            RewardType.BLOCK_MASTER_EXTRA_COINS,
            RewardType.BLOCK_MASTER_POWERUP,
            RewardType.BINGO_DOUBLE_REWARD,
            RewardType.BINGO_SECOND_CHANCE,
            RewardType.BINGO_DAILY_LOGIN_DOUBLE -> {
                callback.onSuccess("Block Master/Bingo ad reward granted successfully!")
            }
        }
    }
}
