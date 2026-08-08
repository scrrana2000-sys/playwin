package com.myplaywin.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Phase 10: Live Events, Social Features & Long-Term Retention Models
 */

enum class MissionPeriod {
    DAILY, WEEKLY
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
data class PrivateRoomPlayer(
    val uid: String = "",
    val displayName: String = "Player",
    val avatarUrl: String = "",
    val isHost: Boolean = false,
    val isReady: Boolean = true
)

@IgnoreExtraProperties
data class GameSession(
    val gameId: String = "",
    val roomId: String = "",
    val players: List<String> = emptyList(),
    val bingoBoards: Map<String, List<Int>> = emptyMap(),
    val calledNumbers: List<Int> = emptyList(),
    val seed: Long = 0L,
    val currentTurn: String = "",
    val gameState: String = "playing"
)

@IgnoreExtraProperties
data class PrivateRoomDetails(
    val roomCode: String = "",
    val hostUid: String = "",
    val hostName: String = "",
    val maxPlayers: Int = 4,
    val currentPlayersCount: Int = 1,
    val isMatchStarted: Boolean = false,
    val players: List<PrivateRoomPlayer> = emptyList(),
    val status: String = "waiting",
    val seed: Long = 0L,
    val gameStartedAt: Long = 0L,
    val gameSession: GameSession? = null
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
