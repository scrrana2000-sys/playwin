package com.myplaywin.app.blockmaster.engine

class SmartDifficultyEngine {

    private var consecutiveWins = 0
    private var consecutiveLosses = 0
    private var averageClearTimeSec = 60f

    fun recordLevelSuccess(durationSec: Long) {
        consecutiveWins++
        consecutiveLosses = 0
        if (durationSec > 0) {
            averageClearTimeSec = (averageClearTimeSec * 0.7f) + (durationSec * 0.3f)
        }
    }

    fun recordLevelFailure() {
        consecutiveLosses++
        consecutiveWins = 0
    }

    fun getDifficultyMultiplier(): Float {
        var multiplier = 1.0f

        // Win streak increases difficulty smoothly
        if (consecutiveWins >= 3) {
            multiplier += (consecutiveWins - 2) * 0.04f
        }

        // Loss streak eases difficulty smoothly
        if (consecutiveLosses >= 2) {
            multiplier -= (consecutiveLosses - 1) * 0.06f
        }

        return multiplier.coerceIn(0.80f, 1.25f)
    }

    fun calculateGravityDropInterval(level: Int): Long {
        val lvl = maxOf(1, level)
        val diffMultiplier = getDifficultyMultiplier()

        // Base exponential decay formula from level 1 (800ms) down to level 100+ (~80ms)
        val baseInterval = 800.0 * Math.pow(0.965, (lvl - 1).toDouble())

        // Modulate with difficulty multiplier
        val adjustedInterval = (baseInterval / diffMultiplier).toLong()

        // Absolute boundaries to prevent impossible drops or overly slow gameplay
        return adjustedInterval.coerceIn(75L, 900L)
    }

    fun calculateScoreMultiplier(level: Int): Float {
        val lvl = maxOf(1, level)
        return 1.0f + (lvl - 1) * 0.05f
    }

    fun calculateCoinRewardMultiplier(level: Int): Float {
        val lvl = maxOf(1, level)
        return 1.0f + (lvl - 1) * 0.03f
    }
}
