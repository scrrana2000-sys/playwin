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

    // Scratch Card fields
    val remainingScratchCards: Int = 1,
    val lastScratchResetTime: Long = 0L,
    val totalScratchRewards: Int = 0,
    val freeScratchUsed: Boolean = false,
    val rewardAdScratchUsed: Boolean = false,
    val lastScratchDate: String = "",
    val lastRewardAdTime: Long = 0L,
    
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
    val referralCode: String = ""
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
data class Quiz(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIdx: Int = 0
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


