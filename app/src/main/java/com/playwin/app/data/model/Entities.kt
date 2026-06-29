package com.playwin.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_wallet")
data class UserWallet(
    @PrimaryKey val id: Int = 1, // Single user row
    val coins: Int = 100,      // Initial free 100 coins
    val dailyStreak: Int = 0,
    val lastCheckInTime: Long = 0L,
    val userId: String = "",      // Unique user ID for Firebase sync
    val dailyAdsWatched: Int = 0,
    val lastAdResetTime: Long = 0L,
    val referredBy: String = "",
    val hasUsedReferralCode: Boolean = false,
    val totalReferrals: Int = 0,
    val remainingSpins: Int = 1,
    val totalSpinRewards: Int = 0,
    val lastSpinDate: String = "",
    val freeSpinUsed: Boolean = false,
    val rewardAdSpinUsed: Boolean = false,
    val remainingScratchCards: Int = 1,
    val lastScratchResetTime: Long = 0L,
    val totalScratchRewards: Int = 0,
    val freeScratchUsed: Boolean = false,
    val rewardAdScratchUsed: Boolean = false,
    val lastScratchDate: String = "",
    val lastCheckInDate: String = "",
    val totalCheckInRewards: Int = 0,
    val lastRewardAdTime: Long = 0L,
    val pendingRewards: Int = 0,
    val referralsCoinsEarned: Int = 0
)

@Entity(tableName = "reward_transaction")
data class RewardTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Int,
    val source: String, // "Spin & Win", "Lucky Scratch", "Trivia Quiz", "Daily Reward", "Math Solver"
    val timestamp: Long = System.currentTimeMillis()
)
