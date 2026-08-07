package com.myplaywin.app.blockmaster.powerups

import androidx.compose.ui.graphics.Color

enum class PowerUpType {
    CLEAR_ROW,
    CLEAR_COLUMN,
    DESTROY_BLOCK,
    FREEZE_TIME,
    SCORE_BOOSTER,
    COIN_BOOSTER
}

data class PowerUp(
    val type: PowerUpType,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val color: Color,
    val durationSec: Int = 0 // 0 means instant effect
)

object PowerUpRegistry {

    val CLEAR_ROW = PowerUp(
        type = PowerUpType.CLEAR_ROW,
        name = "Line Blast",
        description = "Instantly vaporizes the bottom occupied row",
        iconEmoji = "🧹",
        color = Color(0xFFA855F7)
    )

    val CLEAR_COLUMN = PowerUp(
        type = PowerUpType.CLEAR_COLUMN,
        name = "Column Beam",
        description = "Vaporizes the center grid column",
        iconEmoji = "⚡",
        color = Color(0xFF00E5FF)
    )

    val DESTROY_BLOCK = PowerUp(
        type = PowerUpType.DESTROY_BLOCK,
        name = "Block Buster",
        description = "Destroys the highest placed block on the board",
        iconEmoji = "💥",
        color = Color(0xFFFF1744)
    )

    val FREEZE_TIME = PowerUp(
        type = PowerUpType.FREEZE_TIME,
        name = "Time Freeze",
        description = "Slows block falling speed by 50% for 15s",
        iconEmoji = "❄️",
        color = Color(0xFF80D8FF),
        durationSec = 15
    )

    val SCORE_BOOSTER = PowerUp(
        type = PowerUpType.SCORE_BOOSTER,
        name = "2x Score",
        description = "Doubles all points earned for 20s",
        iconEmoji = "🚀",
        color = Color(0xFFFFD700),
        durationSec = 20
    )

    val COIN_BOOSTER = PowerUp(
        type = PowerUpType.COIN_BOOSTER,
        name = "2x Coins",
        description = "Doubles all PlayWin coins earned for 20s",
        iconEmoji = "🪙",
        color = Color(0xFF00E676),
        durationSec = 20
    )

    val ALL_POWER_UPS = listOf(
        CLEAR_ROW,
        CLEAR_COLUMN,
        DESTROY_BLOCK,
        FREEZE_TIME,
        SCORE_BOOSTER,
        COIN_BOOSTER
    )

    fun getPowerUp(type: PowerUpType): PowerUp = when (type) {
        PowerUpType.CLEAR_ROW -> CLEAR_ROW
        PowerUpType.CLEAR_COLUMN -> CLEAR_COLUMN
        PowerUpType.DESTROY_BLOCK -> DESTROY_BLOCK
        PowerUpType.FREEZE_TIME -> FREEZE_TIME
        PowerUpType.SCORE_BOOSTER -> SCORE_BOOSTER
        PowerUpType.COIN_BOOSTER -> COIN_BOOSTER
    }
}
