package com.myplaywin.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Phase 7: Real-Time Online Multiplayer Foundation Data Models
 */

@IgnoreExtraProperties
data class BingoOnlinePlayer(
    val uid: String = "",
    val displayName: String = "Player",
    val avatarUrl: String = "",
    val level: Int = 1,
    val winRate: Float = 0.5f,
    val markedCount: Int = 0,
    val completedLinesCount: Int = 0,
    val pingMs: Int = 45,
    val isOnline: Boolean = true,
    val isReady: Boolean = false,
    val isHost: Boolean = false,
    val lastMoveTimestamp: Long = System.currentTimeMillis()
)

enum class BingoMatchStatus {
    SEARCHING,
    MATCHED,
    LOADING,
    COUNTDOWN,
    PLAYING,
    PAUSED,
    RECONNECTING,
    VICTORY,
    DEFEAT,
    DRAW,
    COMPLETED
}

enum class BingoMatchType {
    ONE_VS_ONE,
    TWO_VS_TWO,
    FOUR_PLAYER_ROOM,
    TOURNAMENT
}

@IgnoreExtraProperties
data class BingoOnlineRoom(
    val roomId: String = "",
    val matchType: String = "ONE_VS_ONE",
    val matchStatus: String = "SEARCHING",
    val player1: BingoOnlinePlayer = BingoOnlinePlayer(),
    val player2: BingoOnlinePlayer? = null,
    val calledNumbersHistory: List<Int> = emptyList(),
    val activeCalledNumber: Int? = null,
    val activeLetter: String = "",
    val gameTimerSeconds: Int = 120,
    val nextCallTimestamp: Long = 0L,
    val winnerUid: String? = null,
    val boardSeedP1: Long = 12345L,
    val boardSeedP2: Long = 54321L,
    val serverTimestamp: Long = System.currentTimeMillis(),
    val region: String = "US-AUTO",
    val appVersion: String = "1.0.0",
    val isSpectatorAllowed: Boolean = false
)

@IgnoreExtraProperties
data class BingoMovePayload(
    val moveId: String = "",
    val roomId: String = "",
    val playerUid: String = "",
    val tileRow: Int = 0,
    val tileCol: Int = 0,
    val tileNumber: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val moveType: String = "DAUB", // "DAUB", "CLAIM_BINGO"
    val clientHash: String = ""
)

data class BingoAntiCheatResult(
    val isValid: Boolean,
    val reason: String = "SUCCESS",
    val action: AntiCheatSanction = AntiCheatSanction.ACCEPT
)

enum class AntiCheatSanction {
    ACCEPT,
    REJECT_MOVE,
    WARNING,
    TERMINATE_ROOM
}

@IgnoreExtraProperties
data class MatchmakingQueueEntry(
    val uid: String = "",
    val displayName: String = "Player",
    val level: Int = 1,
    val winRate: Float = 0.5f,
    val pingMs: Int = 45,
    val region: String = "US-AUTO",
    val appVersion: String = "1.0.0",
    val matchType: String = "ONE_VS_ONE",
    val timestamp: Long = System.currentTimeMillis()
)
