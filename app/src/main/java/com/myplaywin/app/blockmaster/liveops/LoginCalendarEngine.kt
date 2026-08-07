package com.myplaywin.app.blockmaster.liveops

import com.myplaywin.app.blockmaster.powerups.PowerUpType

data class LoginRewardDay(
    val dayNumber: Int,
    val title: String,
    val rewardCoins: Int = 0,
    val rewardXp: Long = 0L,
    val powerUpReward: Pair<PowerUpType, Int>? = null,
    val chestReward: MysteryChestRarity? = null,
    val iconEmoji: String = "🎁",
    val isMilestone: Boolean = false
)

object LoginCalendarEngine {

    val DAYS_30: List<LoginRewardDay> = List(30) { index ->
        val day = index + 1
        when (day) {
            1 -> LoginRewardDay(1, "Day 1 Welcome", rewardCoins = 150, rewardXp = 200L, iconEmoji = "🪙")
            2 -> LoginRewardDay(2, "Day 2 Booster", rewardCoins = 200, powerUpReward = PowerUpType.CLEAR_ROW to 1, iconEmoji = "💣")
            3 -> LoginRewardDay(3, "Day 3 XP Boost", rewardCoins = 250, rewardXp = 500L, iconEmoji = "⭐")
            4 -> LoginRewardDay(4, "Day 4 Power Supply", powerUpReward = PowerUpType.DESTROY_BLOCK to 2, iconEmoji = "🔨")
            5 -> LoginRewardDay(5, "Bronze Chest", chestReward = MysteryChestRarity.BRONZE, isMilestone = true, iconEmoji = "📦")
            6 -> LoginRewardDay(6, "Day 6 Stacker", rewardCoins = 400, rewardXp = 800L, iconEmoji = "🧱")
            7 -> LoginRewardDay(7, "Silver Chest", rewardCoins = 500, chestReward = MysteryChestRarity.SILVER, isMilestone = true, iconEmoji = "🥈")
            8 -> LoginRewardDay(8, "Day 8 Freeze", powerUpReward = PowerUpType.FREEZE_TIME to 2, iconEmoji = "⏱️")
            9 -> LoginRewardDay(9, "Day 9 XP Surge", rewardCoins = 600, rewardXp = 1000L, iconEmoji = "⭐")
            10 -> LoginRewardDay(10, "Day 10 Gold Pack", rewardCoins = 800, powerUpReward = PowerUpType.SCORE_BOOSTER to 2, iconEmoji = "💰")
            11 -> LoginRewardDay(11, "Day 11 Column Clear", powerUpReward = PowerUpType.CLEAR_COLUMN to 2, iconEmoji = "⚡")
            12 -> LoginRewardDay(12, "Day 12 XP Rush", rewardCoins = 900, rewardXp = 1500L, iconEmoji = "🚀")
            13 -> LoginRewardDay(13, "Day 13 Coin Booster", powerUpReward = PowerUpType.COIN_BOOSTER to 2, iconEmoji = "🪙")
            14 -> LoginRewardDay(14, "Gold Chest", rewardCoins = 1500, chestReward = MysteryChestRarity.GOLD, isMilestone = true, iconEmoji = "🥇")
            15 -> LoginRewardDay(15, "Day 15 Midpoint", rewardCoins = 1200, rewardXp = 2500L, iconEmoji = "🌟")
            16 -> LoginRewardDay(16, "Day 16 Arsenal", powerUpReward = PowerUpType.CLEAR_ROW to 3, iconEmoji = "💣")
            17 -> LoginRewardDay(17, "Day 17 Super XP", rewardCoins = 1500, rewardXp = 3000L, iconEmoji = "⭐")
            18 -> LoginRewardDay(18, "Day 18 Diamond Spark", powerUpReward = PowerUpType.DESTROY_BLOCK to 3, iconEmoji = "💎")
            19 -> LoginRewardDay(19, "Day 19 Score Surge", powerUpReward = PowerUpType.SCORE_BOOSTER to 3, iconEmoji = "🔥")
            20 -> LoginRewardDay(20, "Diamond Chest", rewardCoins = 2500, chestReward = MysteryChestRarity.DIAMOND, isMilestone = true, iconEmoji = "💎")
            21 -> LoginRewardDay(21, "Day 21 Master XP", rewardCoins = 2000, rewardXp = 4000L, iconEmoji = "👑")
            22 -> LoginRewardDay(22, "Day 22 Freeze Surge", powerUpReward = PowerUpType.FREEZE_TIME to 3, iconEmoji = "⏱️")
            23 -> LoginRewardDay(23, "Day 23 Super Coins", rewardCoins = 3000, iconEmoji = "💰")
            24 -> LoginRewardDay(24, "Day 24 Coin Multiplier", powerUpReward = PowerUpType.COIN_BOOSTER to 3, iconEmoji = "🪙")
            25 -> LoginRewardDay(25, "Grand Silver Chest", rewardCoins = 3500, chestReward = MysteryChestRarity.SILVER, isMilestone = true, iconEmoji = "🥈")
            26 -> LoginRewardDay(26, "Day 26 Mega XP", rewardCoins = 4000, rewardXp = 8000L, iconEmoji = "⭐")
            27 -> LoginRewardDay(27, "Day 27 Complete Kit", powerUpReward = PowerUpType.CLEAR_COLUMN to 3, iconEmoji = "⚡")
            28 -> LoginRewardDay(28, "Grand Gold Chest", rewardCoins = 5000, chestReward = MysteryChestRarity.GOLD, isMilestone = true, iconEmoji = "🥇")
            29 -> LoginRewardDay(29, "Day 29 Eve of Glory", rewardCoins = 6000, rewardXp = 12000L, iconEmoji = "🌌")
            30 -> LoginRewardDay(30, "Legendary Crown Chest", rewardCoins = 10000, rewardXp = 25000L, chestReward = MysteryChestRarity.LEGENDARY, isMilestone = true, iconEmoji = "♾️")
            else -> LoginRewardDay(day, "Day $day Reward", rewardCoins = 100 * day)
        }
    }
}
