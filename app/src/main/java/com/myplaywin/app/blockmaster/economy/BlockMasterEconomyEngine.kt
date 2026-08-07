package com.myplaywin.app.blockmaster.economy

import kotlin.math.roundToInt

object BlockMasterEconomyEngine {

    const val COINS_SINGLE_LINE = 5
    const val COINS_DOUBLE_LINE = 10
    const val COINS_TRIPLE_LINE = 15
    const val COINS_TETRIS_LINE = 25
    const val COINS_PERFECT_CLEAR = 50

    const val DAILY_EARNED_COINS_LIMIT = 10000 // Anti-farming safety ceiling per 24 hours

    /**
     * Calculates coins earned for a line clear with active boosters & combo multipliers.
     */
    fun calculateLineClearCoins(
        linesCleared: Int,
        comboCount: Int = 1,
        isBackToBack: Boolean = false,
        isPerfectClear: Boolean = false,
        isCoinBoosterActive: Boolean = false,
        eventMultiplier: Float = 1.0f
    ): Int {
        if (linesCleared <= 0) return 0

        val baseCoins = when (linesCleared) {
            1 -> COINS_SINGLE_LINE
            2 -> COINS_DOUBLE_LINE
            3 -> COINS_TRIPLE_LINE
            4 -> COINS_TETRIS_LINE
            else -> COINS_TETRIS_LINE + (linesCleared - 4) * 10
        }

        var total = baseCoins.toFloat()

        // Combo multiplier: +15% extra per combo step > 1
        if (comboCount > 1) {
            total *= (1.0f + (comboCount - 1) * 0.15f)
        }

        // Back-to-Back Tetris bonus (1.5x multiplier)
        if (isBackToBack && linesCleared >= 4) {
            total *= 1.5f
        }

        // Perfect Clear bonus
        if (isPerfectClear) {
            total += COINS_PERFECT_CLEAR
        }

        // Event multiplier
        total *= eventMultiplier.coerceAtLeast(1.0f)

        // Coin Booster Power-up (2x multiplier)
        if (isCoinBoosterActive) {
            total *= 2.0f
        }

        return total.roundToInt().coerceAtLeast(1)
    }

    /**
     * Calculates XP gained for actions to feed level progression.
     */
    fun calculateLineClearXp(
        linesCleared: Int,
        scoreEarned: Int,
        isScoreBoosterActive: Boolean = false
    ): Long {
        var base = (scoreEarned * 0.2f) + (linesCleared * 20)
        if (isScoreBoosterActive) {
            base *= 2.0f
        }
        return base.toLong().coerceAtLeast(10L)
    }

    /**
     * Returns progressive Daily Login Coins: Day 1 (50) to Day 7 (500).
     */
    fun getDailyLoginCoins(dayNumber: Int): Int {
        return when (dayNumber.coerceIn(1, 7)) {
            1 -> 50
            2 -> 75
            3 -> 100
            4 -> 150
            5 -> 250
            6 -> 350
            7 -> 500
            else -> 50
        }
    }
}
