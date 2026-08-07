package com.myplaywin.app.blockmaster.progression

data class PlayerRank(
    val title: String,
    val badgeEmoji: String,
    val rankIndex: Int,
    val rankColorHex: Long = 0xFF00E5FF
)

object PlayerProgressionManager {

    fun getRankForLevel(level: Int): PlayerRank {
        return when {
            level >= 5000 -> PlayerRank("Infinite Legend", "👑", 12, 0xFFFFD700)
            level >= 2500 -> PlayerRank("Block King", "👑", 11, 0xFFFF007F)
            level >= 1000 -> PlayerRank("Infinity God", "♾️", 10, 0xFFFFD700)
            level >= 500 -> PlayerRank("Realm Sovereign", "🔮", 9, 0xFFE040FB)
            level >= 250 -> PlayerRank("Cosmic Architect", "🌌", 8, 0xFF7C4DFF)
            level >= 100 -> PlayerRank("Block Legend", "⚡", 7, 0xFFFF007F)
            level >= 50 -> PlayerRank("Grandmaster", "👑", 6, 0xFFFFD700)
            level >= 35 -> PlayerRank("Combo Champion", "🏆", 5, 0xFF00E676)
            level >= 20 -> PlayerRank("Tetris Expert", "💎", 4, 0xFF00E5FF)
            level >= 10 -> PlayerRank("Line Master", "🥇", 3, 0xFFA855F7)
            level >= 5 -> PlayerRank("Apprentice Builder", "🥈", 2, 0xFF80D8FF)
            else -> PlayerRank("Novice Stacker", "🥉", 1, 0xFFB0BEC5)
        }
    }

    fun getXpRequiredForLevel(level: Int): Long {
        val lvl = maxOf(1, level)
        return (100 * Math.pow(lvl.toDouble(), 1.35)).toLong()
    }

    fun getXpProgressForLevel(xp: Long, currentLevel: Int): Pair<Long, Long> {
        val currentLevelReq = if (currentLevel <= 1) 0L else getXpRequiredForLevel(currentLevel - 1)
        val nextLevelReq = getXpRequiredForLevel(currentLevel)

        val xpInCurrentLevel = (xp - currentLevelReq).coerceAtLeast(0L)
        val xpNeededForNext = (nextLevelReq - currentLevelReq).coerceAtLeast(1L)

        return Pair(xpInCurrentLevel, xpNeededForNext)
    }

    fun calculateLevelFromXp(xp: Long): Int {
        var level = 1
        while (xp >= getXpRequiredForLevel(level)) {
            level++
        }
        return level
    }
}
