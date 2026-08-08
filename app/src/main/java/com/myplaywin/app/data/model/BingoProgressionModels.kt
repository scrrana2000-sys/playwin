package com.myplaywin.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Phase 8: Player Progression, Economy & Leaderboard Models
 */

@IgnoreExtraProperties
data class BingoPlayerProgression(
    val playerUid: String = "",
    val displayName: String = "PlayWin Player",
    val avatarUrl: String = "",
    val level: Int = 1,
    val currentXp: Int = 0,
    val totalXp: Int = 0,
    val requiredXpNextLevel: Int = 150,
    val currentCoins: Int = 500,
    val currentWinStreak: Int = 0,
    val longestWinStreak: Int = 0,
    val currentBadgeTitle: String = "Beginner",
    val profileFrameId: String = "FRAME_GOLD",
    val country: String = "US",
    val lastDailyBonusDate: String = "",
    val dailyMatchesToday: Int = 0,
    val dailyWinsToday: Int = 0
)

@IgnoreExtraProperties
data class BingoPlayerStats(
    val playerUid: String = "",
    val totalMatches: Int = 0,
    val offlineMatches: Int = 0,
    val onlineMatches: Int = 0,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val totalDraws: Int = 0,
    val winRatePercent: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val fastestVictorySec: Int = 999,
    val avgMatchTimeSec: Int = 0,
    val totalCoinsEarned: Int = 0,
    val totalXpEarned: Int = 0,
    val highestLevel: Int = 1,
    val favoriteMode: String = "Classic 1v1",
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class BingoMatchHistoryRecord(
    val id: String = "",
    val matchType: String = "OFFLINE", // "OFFLINE", "ONLINE_1V1"
    val difficulty: String = "MEDIUM", // "EASY", "MEDIUM", "HARD", "RANKED"
    val opponentName: String = "AI Bot",
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 45,
    val result: String = "VICTORY", // "VICTORY", "DEFEAT", "DRAW"
    val coinsEarned: Int = 50,
    val xpEarned: Int = 100,
    val numbersCalledCount: Int = 12
)

@IgnoreExtraProperties
data class BingoAchievement(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val emoji: String = "🏆",
    val currentProgress: Int = 0,
    val maxProgress: Int = 1,
    val isUnlocked: Boolean = false,
    val rewardCoins: Int = 100,
    val rewardXp: Int = 150,
    val rewardBadgeId: String? = null
)

@IgnoreExtraProperties
data class BingoBadge(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val emoji: String = "🏅",
    val rarity: String = "COMMON", // "COMMON", "RARE", "EPIC", "LEGENDARY"
    val isUnlocked: Boolean = false,
    val unlockedTimestamp: Long = 0L
)

enum class LeaderboardCategory {
    GLOBAL,
    COUNTRY,
    WEEKLY,
    MONTHLY,
    ALL_TIME
}

enum class LeaderboardSortBy {
    LEVEL,
    WINS,
    XP,
    WIN_RATE,
    STREAK
}

@IgnoreExtraProperties
data class BingoLeaderboardEntry(
    val rank: Int = 1,
    val playerUid: String = "",
    val displayName: String = "Player",
    val avatarUrl: String = "",
    val level: Int = 1,
    val totalWins: Int = 0,
    val totalXp: Int = 0,
    val winRatePercent: Float = 0f,
    val streak: Int = 0,
    val country: String = "US",
    val badgeTitle: String = "Pro"
)
