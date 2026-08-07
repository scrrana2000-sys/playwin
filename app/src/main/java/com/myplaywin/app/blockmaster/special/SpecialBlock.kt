package com.myplaywin.app.blockmaster.special

import androidx.compose.ui.graphics.Color

enum class SpecialBlockType {
    NONE,
    BOMB,
    ICE,
    STEEL,
    RAINBOW,
    LIGHTNING,
    COIN,
    TIME,
    MYSTERY
}

data class SpecialBlock(
    val type: SpecialBlockType,
    val name: String,
    val description: String,
    val minLevelUnlock: Int,
    val iconEmoji: String,
    val fillColor: Color,
    val borderColor: Color,
    val glowColor: Color
)

object SpecialBlockRegistry {

    val BOMB = SpecialBlock(
        type = SpecialBlockType.BOMB,
        name = "Bomb Block",
        description = "Explodes surrounding 3x3 cells and triggers chain reactions",
        minLevelUnlock = 21,
        iconEmoji = "💣",
        fillColor = Color(0xFFFF1744),
        borderColor = Color(0xFFFF9100),
        glowColor = Color(0x66FF1744)
    )

    val ICE = SpecialBlock(
        type = SpecialBlockType.ICE,
        name = "Ice Block",
        description = "Requires 2 line clear hits to shatter (cracks on 1st hit)",
        minLevelUnlock = 40,
        iconEmoji = "🧊",
        fillColor = Color(0xFF80D8FF),
        borderColor = Color(0xFFE0F7FA),
        glowColor = Color(0x6680D8FF)
    )

    val STEEL = SpecialBlock(
        type = SpecialBlockType.STEEL,
        name = "Steel Block",
        description = "Indestructible by standard line clears. Requires Bomb explosion!",
        minLevelUnlock = 70,
        iconEmoji = "⚙️",
        fillColor = Color(0xFF607D8B),
        borderColor = Color(0xFFCFD8DC),
        glowColor = Color(0x66607D8B)
    )

    val LIGHTNING = SpecialBlock(
        type = SpecialBlockType.LIGHTNING,
        name = "Lightning Block",
        description = "Clears an entire row or column with electric energy",
        minLevelUnlock = 100,
        iconEmoji = "⚡",
        fillColor = Color(0xFFFFEA00),
        borderColor = Color(0xFFFFFFFF),
        glowColor = Color(0x66FFEA00)
    )

    val RAINBOW = SpecialBlock(
        type = SpecialBlockType.RAINBOW,
        name = "Rainbow Block",
        description = "Acts as a wild block and grants bonus points",
        minLevelUnlock = 150,
        iconEmoji = "🌈",
        fillColor = Color(0xFFFF007F),
        borderColor = Color(0xFF00E5FF),
        glowColor = Color(0x66FF007F)
    )

    val COIN = SpecialBlock(
        type = SpecialBlockType.COIN,
        name = "Coin Block",
        description = "Awards extra PlayWin coins when cleared",
        minLevelUnlock = 1,
        iconEmoji = "🪙",
        fillColor = Color(0xFFFFD700),
        borderColor = Color(0xFFFFF59D),
        glowColor = Color(0x66FFD700)
    )

    val TIME = SpecialBlock(
        type = SpecialBlockType.TIME,
        name = "Time Block",
        description = "Temporarily slows down gravity and falling block speed",
        minLevelUnlock = 1,
        iconEmoji = "⏳",
        fillColor = Color(0xFF29B6F6),
        borderColor = Color(0xFFB3E5FC),
        glowColor = Color(0x6629B6F6)
    )

    val MYSTERY = SpecialBlock(
        type = SpecialBlockType.MYSTERY,
        name = "Mystery Block",
        description = "Randomly transforms into Bomb, Coin, Lightning, Rainbow, or Time",
        minLevelUnlock = 200,
        iconEmoji = "❓",
        fillColor = Color(0xFFD500F9),
        borderColor = Color(0xFFEA80FC),
        glowColor = Color(0x66D500F9)
    )

    fun getSpecialBlock(type: SpecialBlockType): SpecialBlock? = when (type) {
        SpecialBlockType.BOMB -> BOMB
        SpecialBlockType.ICE -> ICE
        SpecialBlockType.STEEL -> STEEL
        SpecialBlockType.LIGHTNING -> LIGHTNING
        SpecialBlockType.RAINBOW -> RAINBOW
        SpecialBlockType.COIN -> COIN
        SpecialBlockType.TIME -> TIME
        SpecialBlockType.MYSTERY -> MYSTERY
        SpecialBlockType.NONE -> null
    }
}
