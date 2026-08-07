package com.myplaywin.app.blockmaster.retention

import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData
import java.util.Calendar

data class ReturningPlayerBonus(
    val coins: Int,
    val xp: Long,
    val freeChests: Int,
    val title: String,
    val message: String
)

object RetentionAndStatsManager {

    /**
     * Checks if a returning player is eligible for a Welcome Back bonus (if inactive > 48 hours).
     */
    fun checkReturningPlayerBonus(saveData: BlockMasterSaveData): ReturningPlayerBonus? {
        val lastActive = saveData.lastActiveTimestamp
        if (lastActive <= 0L) return null

        val now = System.currentTimeMillis()
        val diffHours = (now - lastActive) / (1000 * 3600)

        return when {
            diffHours >= 168 -> { // 7 days or more inactive
                ReturningPlayerBonus(
                    coins = 1000,
                    xp = 500L,
                    freeChests = 2,
                    title = "WELCOME BACK HERO! 👑",
                    message = "We missed you! Here is a 1,000 Coin + 2 Chest starter gift to get back into the blocks!"
                )
            }
            diffHours >= 48 -> { // 2 days inactive
                ReturningPlayerBonus(
                    coins = 300,
                    xp = 200L,
                    freeChests = 1,
                    title = "WELCOME BACK! 🎁",
                    message = "Great to see you again! Claim your +300 Coins + 1 Chest return reward."
                )
            }
            else -> null
        }
    }

    /**
     * Calculates average score per match safely.
     */
    fun getAverageScore(lifetimeScore: Long, totalGames: Int): Int {
        if (totalGames <= 0) return 0
        return (lifetimeScore / totalGames).toInt()
    }

    /**
     * Calculates average survival time in seconds.
     */
    fun getAverageSurvivalTimeSec(timePlayedSec: Long, totalGames: Int): Long {
        if (totalGames <= 0) return 0L
        return timePlayedSec / totalGames
    }

    fun getFormattedSurvivalTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "${mins}m ${secs}s"
    }
}
