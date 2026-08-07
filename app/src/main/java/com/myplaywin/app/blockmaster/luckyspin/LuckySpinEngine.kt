package com.myplaywin.app.blockmaster.luckyspin

import java.util.Random

data class SpinRewardSlice(
    val index: Int,
    val title: String,
    val subtitle: String,
    val rewardType: String, // "COINS", "XP", "POWERUPS", "CHEST", "TITLE", "JACKPOT"
    val rewardValue: Int,
    val iconEmoji: String,
    val colorHex: Long,
    val weight: Int // Weighted probability
)

object LuckySpinEngine {

    val wheelSlices = listOf(
        SpinRewardSlice(0, "+100 Coins", "PlayWin Credit", "COINS", 100, "🪙", 0xFFFFD700, 35),
        SpinRewardSlice(1, "+500 Coins", "Big Win!", "COINS", 500, "💰", 0xFF00E5FF, 20),
        SpinRewardSlice(2, "+1000 Coins", "Mega Win!", "COINS", 1000, "💎", 0xFFA855F7, 10),
        SpinRewardSlice(3, "+250 XP", "Level Boost", "XP", 250, "⭐", 0xFF00E676, 15),
        SpinRewardSlice(4, "Booster Pack", "+2 Bomb & Row", "POWERUPS", 2, "⚡", 0xFFFF5722, 12),
        SpinRewardSlice(5, "Diamond Chest", "Rare Loot", "CHEST", 1, "🧰", 0xFFE040FB, 5),
        SpinRewardSlice(6, "Lucky Title", "Spin Master", "TITLE", 1, "🎰", 0xFFFF007F, 2),
        SpinRewardSlice(7, "JACKPOT!", "+5000 Coins", "JACKPOT", 5000, "🏆", 0xFFFFD700, 1)
    )

    const val SPIN_COST_COINS = 200

    /**
     * Checks if the 24-hour daily free spin is available.
     */
    fun canFreeSpin(lastSpinTimestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val elapsedHours = (now - lastSpinTimestamp) / (1000 * 3600)
        return elapsedHours >= 24
    }

    /**
     * Calculates weighted random slice outcome index (0..7).
     */
    fun getRandomSpinResult(): SpinRewardSlice {
        val totalWeight = wheelSlices.sumOf { it.weight }
        val randomVal = Random().nextInt(totalWeight)

        var accumulated = 0
        for (slice in wheelSlices) {
            accumulated += slice.weight
            if (randomVal < accumulated) {
                return slice
            }
        }
        return wheelSlices.first()
    }
}
