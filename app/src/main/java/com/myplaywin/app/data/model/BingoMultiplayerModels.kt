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
    val level: Int = 12,
    val winRate: Float = 0.68f,
    val markedCount: Int = 0,
    val completedLinesCount: Int = 0,
    val pingMs: Int = 45,
    val isOnline: Boolean = true,
    val isReady: Boolean = false,
    val isHost: Boolean = false,
    val lastMoveTimestamp: Long = System.currentTimeMillis(),
    val card: List<Int> = emptyList(),
    val marked: List<Boolean> = emptyList(),
    val playAgainRequested: Boolean = false,
    
    // Compatibility fields
    val connected: Boolean = true
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
data class BingoGame(
    val status: String = "waiting", // "waiting", "playing", "finished"
    val currentTurn: String = "", // playerId of player whose turn it is
    val calledNumbers: List<Int> = emptyList(),
    val lastCalledNumber: Int? = null,
    val gameStartedAt: Long = 0L,
    val winnerId: String? = null
)

@IgnoreExtraProperties
data class BingoOnlineRoom(
    val roomId: String = "",
    val status: String = "waiting", // "waiting", "playing", "completed"
    val maxPlayers: Int = 2,
    val createdAt: Long = System.currentTimeMillis(),
    val players: Map<String, BingoOnlinePlayer> = emptyMap(),
    val game: BingoGame = BingoGame(),
    val host: String = "",
    val guest: String = "",
    val currentTurn: String = "",
    val calledNumbers: List<Int> = emptyList(),
    val winner: String = "",
    val gameState: String = "",
    val playerBoards: Map<String, List<Int>> = emptyMap()
) {
    // Custom getters for complete backwards compatibility with the UI screen
    val matchType: String
        get() = "ONE_VS_ONE"

    val matchStatus: String
        get() = when (status) {
            "waiting" -> "SEARCHING"
            "playing" -> "PLAYING"
            "completed" -> "COMPLETED"
            else -> status.uppercase()
        }

    val player1: BingoOnlinePlayer
        get() = players[host] ?: players.values.firstOrNull { it.isHost } ?: players.values.firstOrNull() ?: BingoOnlinePlayer()

    val player2: BingoOnlinePlayer?
        get() = players[guest] ?: players.values.firstOrNull { !it.isHost && it.uid != player1.uid }

    val calledNumbersHistory: List<Int>
        get() = calledNumbers

    val activeCalledNumber: Int?
        get() = calledNumbers.lastOrNull()

    val activeLetter: String
        get() = activeCalledNumber?.let { columnLetterForNum(it) } ?: ""

    val winnerUid: String?
        get() = if (winner.isNotEmpty()) winner else game.winnerId

    private fun columnLetterForNum(num: Int): String {
        return when (num) {
            in 1..15 -> "B"
            in 16..30 -> "I"
            in 31..45 -> "N"
            in 46..60 -> "G"
            else -> "O"
        }
    }
}

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
    val displayName: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val status: String = "waiting", // "waiting", "matched"
    val online: Boolean = true,
    val skill: Double = 1.0,
    val version: String = "1.0",
    val roomId: String = ""
)
