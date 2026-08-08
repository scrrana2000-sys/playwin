package com.myplaywin.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Phase 10: Live Events, Social Features & Long-Term Retention Models
 */

enum class MissionPeriod {
    DAILY, WEEKLY
}

enum class TournamentType(val displayName: String, val badgeColorHex: String) {
    HOURLY("Hourly Blitz", "#0284C7"),
    DAILY("Daily Cup", "#16A34A"),
    WEEKEND("Weekend Clash", "#9333EA"),
    GLOBAL("Global Championship", "#D97706")
}

enum class CosmeticCategory(val title: String) {
    AVATAR_FRAME("Avatar Frames"),
    BOARD_SKIN("Bingo Board Skins"),
    BALL_DESIGN("Called Ball Designs"),
    VICTORY_ANIMATION("Victory Animations"),
    PROFILE_BADGE("Profile Badges")
}

@IgnoreExtraProperties
data class DailyMission(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconName: String = "EmojiEvents", // Icon identifier
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val coinReward: Int = 100,
    val xpReward: Int = 200,
    val isClaimed: Boolean = false,
    val period: MissionPeriod = MissionPeriod.DAILY
)

@IgnoreExtraProperties
data class SeasonalEvent(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val themeKey: String = "DIWALI_FESTIVAL", // "DIWALI", "SUMMER", "WINTER", "HALLOWEEN"
    val bannerGradientColorsHex: List<String> = listOf("#4C1D95", "#831843", "#F59E0B"),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis() + (7 * 24 * 3600 * 1000L),
    val isLive: Boolean = true,
    val specialBonusCoins: Int = 500,
    val exclusiveRewardTitle: String = "Golden Diwali Avatar Frame"
)

@IgnoreExtraProperties
data class TournamentParticipant(
    val rank: Int = 1,
    val playerUid: String = "",
    val displayName: String = "Player",
    val avatarUrl: String = "",
    val score: Int = 0,
    val matchesWon: Int = 0
)

@IgnoreExtraProperties
data class TournamentInfo(
    val id: String = "",
    val title: String = "",
    val type: TournamentType = TournamentType.DAILY,
    val entryFeeCoins: Int = 100,
    val prizePoolCoins: Int = 5000,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis() + (24 * 3600 * 1000L),
    val maxParticipants: Int = 100,
    val registeredCount: Int = 42,
    val isRegistered: Boolean = false,
    val userRank: Int = 5,
    val userScore: Int = 1250,
    val leaderboard: List<TournamentParticipant> = emptyList()
)

@IgnoreExtraProperties
data class PrivateRoomPlayer(
    val uid: String = "",
    val displayName: String = "Player",
    val avatarUrl: String = "",
    val isHost: Boolean = false,
    val isReady: Boolean = true
)

@IgnoreExtraProperties
data class PrivateRoomDetails(
    val roomCode: String = "",
    val hostUid: String = "",
    val hostName: String = "",
    val maxPlayers: Int = 4,
    val currentPlayersCount: Int = 1,
    val isMatchStarted: Boolean = false,
    val players: List<PrivateRoomPlayer> = emptyList()
)

@IgnoreExtraProperties
data class FriendProfile(
    val uid: String = "",
    val displayName: String = "PlayWin Gamer",
    val avatarUrl: String = "",
    val avatarFrame: String = "DEFAULT_GOLD",
    val level: Int = 5,
    val isOnline: Boolean = true,
    val statusText: String = "Ready for Bingo!",
    val totalWins: Int = 24,
    val isFavorite: Boolean = false
)

@IgnoreExtraProperties
data class CosmeticItem(
    val id: String = "",
    val name: String = "",
    val category: CosmeticCategory = CosmeticCategory.AVATAR_FRAME,
    val priceCoins: Int = 500,
    val isUnlocked: Boolean = false,
    val isEquipped: Boolean = false,
    val gradientColorsHex: List<String> = listOf("#FFD700", "#FFA500")
)

@IgnoreExtraProperties
data class PlayerProfileExpanded(
    val uid: String = "",
    val displayName: String = "PlayWin Champion",
    val avatarUrl: String = "",
    val countryFlagEmoji: String = "🇮🇳",
    val equippedAvatarFrame: String = "GOLDEN_GLOW",
    val equippedBoardSkin: String = "NEON_CYBER",
    val equippedBallDesign: String = "CRYSTAL_SPHERE",
    val equippedBadge: String = "BINGO_MASTER",
    val seasonRankName: String = "Diamond League II",
    val tournamentRankName: String = "#12 Regional",
    val lifetimeWins: Int = 142,
    val totalMatches: Int = 210,
    val winRatePercent: Float = 67.6f
)
