package com.myplaywin.app.ludo.data.model

import androidx.compose.ui.graphics.Color

enum class LudoColor(
    val colorName: String,
    val displayColor: Color,
    val darkColor: Color,
    val lightColor: Color,
    val startIndex: Int,
    val homeEntryIndex: Int,
    val safeStarIndex: Int
) {
    RED(
        colorName = "Red",
        displayColor = Color(0xFFE53935),
        darkColor = Color(0xFFB71C1C),
        lightColor = Color(0xFFFFEBEE),
        startIndex = 0,
        homeEntryIndex = 50,
        safeStarIndex = 8
    ),
    GREEN(
        colorName = "Green",
        displayColor = Color(0xFF43A047),
        darkColor = Color(0xFF1B5E20),
        lightColor = Color(0xE8E8F5E9),
        startIndex = 13,
        homeEntryIndex = 11,
        safeStarIndex = 21
    ),
    YELLOW(
        colorName = "Yellow",
        displayColor = Color(0xFFFBC02D),
        darkColor = Color(0xFFF57F17),
        lightColor = Color(0xFFFFFDE7),
        startIndex = 26,
        homeEntryIndex = 24,
        safeStarIndex = 34
    ),
    BLUE(
        colorName = "Blue",
        displayColor = Color(0xFF1E88E5),
        darkColor = Color(0xFF0D47A1),
        lightColor = Color(0xFFE3F2FD),
        startIndex = 39,
        homeEntryIndex = 37,
        safeStarIndex = 47
    )
}

enum class LudoGameMode(val title: String, val playerCapacity: Int) {
    TWO_PLAYER("2 Players", 2),
    THREE_PLAYER("3 Players", 3),
    FOUR_PLAYER("4 Players", 4),
    PRIVATE_ROOM("Private Room", 4),
    VS_BOT("Practice vs Bot", 4)
}

enum class LudoRoomStatus {
    WAITING,
    STARTING,
    PLAYING,
    FINISHED,
    CANCELLED
}

data class LudoPlayer(
    val uid: String = "",
    val name: String = "Player",
    val avatarUrl: String = "",
    val colorIndex: Int = 0, // 0 = RED, 1 = GREEN, 2 = YELLOW, 3 = BLUE
    val isBot: Boolean = false,
    val isReady: Boolean = true,
    val isOnline: Boolean = true,
    val tokens: List<Int> = listOf(0, 0, 0, 0), // Step count 0..57 for tokens 0..3
    val rank: Int = 0 // 0 = playing, 1 = 1st place, 2 = 2nd place, etc.
) {
    val ludoColor: LudoColor
        get() = LudoColor.entries.getOrElse(colorIndex % 4) { LudoColor.RED }

    val hasFinishedAllTokens: Boolean
        get() = tokens.all { it >= 57 }

    val finishedTokensCount: Boolean
        get() = tokens.all { it >= 57 }
}

data class LudoBoardCell(
    val row: Int,
    val col: Int,
    val isStar: Boolean = false,
    val isStartCell: Boolean = false,
    val startColor: LudoColor? = null
)

data class LudoGameState(
    val roomId: String = "",
    val roomCode: String = "",
    val hostUid: String = "",
    val hostName: String = "Host",
    val gameMode: String = LudoGameMode.FOUR_PLAYER.name,
    val maxPlayers: Int = 4,
    val status: String = LudoRoomStatus.WAITING.name,
    val players: List<LudoPlayer> = emptyList(),
    val currentTurnIndex: Int = 0,
    val currentTurnUid: String = "",
    val diceRoll: Int = 0, // 0 = waiting to roll, 1..6 = rolled value
    val hasRolled: Boolean = false,
    val movableTokenIndices: List<Int> = emptyList(),
    val consecutiveSixes: Int = 0,
    val winnerUid: String = "",
    val winnerName: String = "",
    val rankings: List<String> = emptyList(),
    val lastActionText: String = "Welcome to Ludo!",
    val turnNumber: Long = 0L,
    val turnTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val currentTurnPlayer: LudoPlayer?
        get() = players.getOrNull(currentTurnIndex)
}
