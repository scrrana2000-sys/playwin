package com.playwin.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseTask(
    val id: String = "",
    val title: String = "",
    val progressText: String = "",
    val rewardText: String = "",
    val rewardCoins: Int = 0,
    val type: String = "" // "daily", "streak", "video", "spin", "scratch", "trivia", "math"
)

@IgnoreExtraProperties
data class FirebaseCoupon(
    val couponId: String = "",
    val couponName: String = "",
    val coinCost: Int = 0,
    val requiredCoins: Int = 0,
    val couponImage: String = "",
    val enabled: Boolean = true,
    val status: String = "In Stock",
    val remainingStock: Int = 50,
    val brand: String = "",
    val featured: Boolean = false,
    val category: String = "Shopping",
    val code: String = "",
    val description: String = "",
    val expiryDate: String = ""
) {
    // Backward-compatible computed properties for any existing code references
    val id: String get() = couponId
    val title: String get() = couponName
    val cost: Int get() = if (requiredCoins > 0) requiredCoins else coinCost
    val image: String get() = couponImage
    val isEnabled: Boolean get() = enabled
    val availability: String get() = status
    val stock: Int get() = remainingStock
}

@IgnoreExtraProperties
data class FirebaseRedemption(
    val id: String = "",
    val userId: String = "",
    val couponId: String = "",
    val couponName: String = "",
    val coinsSpent: Int = 0,
    val status: String = "Pending", // "Pending", "Approved", "Rejected", "Used"
    val timestamp: Long = System.currentTimeMillis(),
    val couponCode: String = "",
    val expiryDate: String = ""
)

@IgnoreExtraProperties
data class FirebaseSpinReward(
    val id: String = "",
    val name: String = "",
    val title: String = "", // Admin App compatibility
    val type: String = "", // "Coins", "Coupon", "Retry", "Better Luck Next Time"
    val rewardType: String = "", // Admin App compatibility
    val value: String = "", // amount of coins or coupon code/id, empty for retry / better luck
    val reward: String = "", // Admin App compatibility
    val displayOrder: Int = 0,
    val order: Int = 0, // Admin App compatibility
    val probabilityWeight: Int = 1,
    val probability: Int = 0, // Admin App compatibility
    val active: Boolean = true,
    val enabled: Boolean = true, // Admin App compatibility
    val icon: String = "",
    val color: String = "",
    val description: String = ""
)

@IgnoreExtraProperties
data class FirebaseSpinWheelConfig(
    val enabled: Boolean = true,
    val dailySpins: Int = 2,
    val title: String = "Lucky Spin & Win",
    val dailySpinLimit: Int = 10,
    val dailyFreeSpins: Int = 2,
    val rewardedAdAfterFreeSpins: Boolean = true,
    val requireRewardedAdBeforeEveryExtraSpin: Boolean = true,
    val maxRewardedAdSpinsPerDay: Int = 10,
    val segments: List<FirebaseSpinReward> = emptyList(),
    val lastUpdated: Long = 0L
)

@IgnoreExtraProperties
data class FirebaseCouponRedemption(
    val requestId: String = "",
    val userUid: String = "",
    val displayName: String = "",
    val email: String = "",
    val mobileNumber: String = "",
    val couponName: String = "",
    val requiredCoins: Int = 0,
    val giftCardOrRechargeNumber: String = "",
    val additionalNotes: String = "",
    val status: String = "Pending", // "Pending", "Approved", "Rejected", "Completed"
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class FirebaseUser(
    // Realtime Database schema fields as specifically requested
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val coins: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val totalReferrals: Int = 0,
    val rewardHistoryCount: Int = 0,
    val joinedAt: Long = System.currentTimeMillis(),
    
    // New fields requested
    val referrals: Int = 0,
    val records: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),

    // Backwards compatibility legacy fields
    val userId: String = "",
    val dailyStreak: Int = 0,
    val lastCheckInTime: Long = 0L,
    val lastCheckInDate: String = "",
    val totalCheckInRewards: Int = 0,
    val lastActiveTime: Long = System.currentTimeMillis(),

    // Video Ads fields
    val dailyAdsWatched: Int = 0,
    val lastAdResetTime: Long = 0L,

    // Referral fields
    val referredBy: String = "",
    val hasUsedReferralCode: Boolean = false,

    // Spin Wheel fields
    val remainingSpins: Int = 1,
    val totalSpinRewards: Int = 0,
    val lastSpinDate: String = "",
    val freeSpinUsed: Boolean = false,
    val rewardAdSpinUsed: Boolean = false,
    val dailySpinCount: Int = 0,
    val rewardedSpinCount: Int = 0,

    // Scratch Card fields
    val remainingScratchCards: Int = 1,
    val lastScratchResetTime: Long = 0L,
    val totalScratchRewards: Int = 0,
    val freeScratchUsed: Boolean = false,
    val rewardAdScratchUsed: Boolean = false,
    val lastScratchDate: String = "",
    val lastRewardAdTime: Long = 0L,
    val scratchesToday: Int = 0,
    
    // Block status
    val isBlocked: Boolean = false,

    // Quiz Timer tracking fields
    val totalTimeOuts: Int = 0,
    val lifelinesLostByTimeout: Int = 0,
    val averageAnswerTime: Double = 0.0,
    val totalAnswersCount: Int = 0,
    val totalAnswersTimeSum: Long = 0L,
    val pendingRewards: Int = 0,
    val referralsCoinsEarned: Int = 0,
    val deviceId: String = "",
    val referralCode: String = "",
    val stats: Map<String, Int> = emptyMap(),
    val dailyCheckIn: FirebaseUserDailyCheckIn? = null
)

@IgnoreExtraProperties
data class FirebaseTransaction(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val title: String = "",
    val coins: Int = 0,
    val status: String = "Completed",
    val timestamp: Long = System.currentTimeMillis(),
    val amount: Int = 0,
    val source: String = "",
    val coinsBefore: Int = 0,
    val coinsAfter: Int = 0
)

@IgnoreExtraProperties
data class FirebaseReferral(
    val id: String = "",
    val referredUserId: String = "",
    val referralCode: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class FirebaseWithdrawRequest(
    val id: String = "",
    val uid: String = "",
    val userName: String = "",
    val email: String = "",
    val upiId: String = "",
    val amount: Int = 0, // ₹ amount: 10, 20, 50, 100
    val coinsSpent: Int = 0, // coins amount: 1000, 2000, 5000, 10000
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val timestamp: Long = System.currentTimeMillis(),
    val remarks: String = "",
    val transactionId: String = "" // matching transaction ID
)

@IgnoreExtraProperties
data class FirebaseQuizProgress(
    val completedQuizIds: List<String> = emptyList(),
    val completedQuestionIds: List<String> = emptyList(),
    val lastQuizDate: String = "",
    val dailyQuizCompleted: Boolean = false,
    val categoryHistory: Map<String, Int> = emptyMap()
)

@IgnoreExtraProperties
data class FirebaseCompletedQuiz(
    val completed: Boolean = false,
    val completedAt: Long = 0L,
    val completedDate: String = "",
    val score: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val coinsEarned: Int = 0
)

@IgnoreExtraProperties
data class FirebaseWeeklyQuizProgress(
    val completed: Boolean = false,
    val completedAt: Long = 0L,
    val quizId: String = "",
    val coinsEarned: Int = 0,
    val score: Int = 0,
    val date: String = ""
)

@IgnoreExtraProperties
data class Quiz(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIdx: Int = 0,
    val explanation: String = ""
)

@IgnoreExtraProperties
data class FirebaseReferralRecord(
    val friendUid: String = "",
    val friendName: String = "",
    val friendEmail: String = "",
    val joinDate: Long = System.currentTimeMillis(),
    val status: String = "Pending", // "Pending" or "Rewarded"
    val coinsEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class FirebaseQuiz(
    val id: String = "",
    val title: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val category: String = "", // for compatibility
    val description: String = "",
    val difficulty: String = "Medium",
    val rewardPerQuestion: Int = 0,
    val rewardCoins: Int = 0, // for compatibility
    val passBonus: Int = 0,
    val completionBonus: Int = 0, // for compatibility
    val passingPercentage: Int = 0,
    val timerSeconds: Int = 30,
    val icon: String = "",
    val published: Boolean = false,
    val active: Boolean = false,
    val status: String = "",
    val allowReview: Boolean = true,
    val dayOfWeek: String = "",
    val questions: List<Quiz> = emptyList()
)

@IgnoreExtraProperties
data class FirebaseScratchCardSettings(
    val enabled: Boolean = true,
    val dailyLimit: Int = 5,
    val cooldownMinutes: Int = 15,
    val rewardAdRequired: Boolean = false,
    val minimumLevel: Int = 1
)

@IgnoreExtraProperties
data class FirebaseScratchCardReward(
    val id: String = "",
    val name: String = "",
    val type: String = "Coins", // "Coins", "Coupon", "Retry Scratch", "Better Luck Next Time"
    val value: String = "0",
    val probabilityWeight: Int = 10,
    val status: String = "Active",
    val displayOrder: Int = 0,
    val active: Boolean = true,
    val icon: String = "🎁",
    val color: String = "#7C4DFF",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class FirebaseScratchHistory(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val rewardId: String = "",
    val rewardName: String = "",
    val rewardType: String = "",
    val rewardValue: String = "",
    val walletBefore: Int = 0,
    val walletAfter: Int = 0,
    val status: String = "Completed",
    val deviceTime: String = "",
    val serverTime: Long = 0L,
    val transactionId: String = ""
)

@IgnoreExtraProperties
data class FirebaseDailyCheckInSettings(
    val enabled: Boolean = true,
    val rewards: List<Int>? = null,
    val maxRewardLimit: Int = 500
)

@IgnoreExtraProperties
data class FirebaseUserDailyCheckIn(
    val lastClaimTimestamp: Long = 0L,
    val nextEligibleTimestamp: Long = 0L,
    val currentDay: Int = 0,
    val streak: Int = 0,
    val totalClaims: Int = 0
)

@IgnoreExtraProperties
data class FirebaseUserDailyQuiz(
    val lastCompletedDay: String = "",
    val lastCompletedDate: String = "",
    val lastQuizId: String = "",
    val completed: Boolean = false,
    val rewardClaimed: Boolean = false
)




