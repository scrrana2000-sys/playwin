package com.myplaywin.app.blockmaster.liveops

import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData
import java.util.Calendar

enum class LiveMissionCategory {
    DAILY,
    WEEKLY
}

enum class LiveMissionType {
    CLEAR_LINES,
    SCORE_POINTS,
    PERFORM_COMBOS,
    PLAY_MATCHES,
    USE_BOMBS,
    DESTROY_ICE,
    REACH_LEVEL,
    EARN_COINS,
    PERFECT_CLEARS,
    USE_POWERUPS
}

data class LiveMission(
    val id: String,
    val category: LiveMissionCategory,
    val type: LiveMissionType,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val targetAmount: Int,
    var currentAmount: Int = 0,
    var isCompleted: Boolean = false,
    var isClaimed: Boolean = false,
    val rewardCoins: Int,
    val rewardXp: Long
) {
    val progressFraction: Float
        get() = if (targetAmount <= 0) 1f else (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)
}

object DailyWeeklyMissionEngine {

    // Generate 4 dynamic daily missions for a given day timestamp
    fun generateDailyMissions(daySeed: Long, playerLevel: Int): List<LiveMission> {
        val random = kotlin.random.Random(daySeed)
        val levelFactor = (1.0 + (playerLevel - 1) * 0.05).coerceAtMost(3.0)

        val missionPool = listOf(
            LiveMission(
                id = "daily_lines_${daySeed}",
                category = LiveMissionCategory.DAILY,
                type = LiveMissionType.CLEAR_LINES,
                title = "Line Sweeper",
                description = "Clear ${(15 * levelFactor).toInt()} lines in total",
                iconEmoji = "🧱",
                targetAmount = (15 * levelFactor).toInt(),
                rewardCoins = 150,
                rewardXp = 300L
            ),
            LiveMission(
                id = "daily_score_${daySeed}",
                category = LiveMissionCategory.DAILY,
                type = LiveMissionType.SCORE_POINTS,
                title = "Score Chaser",
                description = "Score ${(5000 * levelFactor).toInt()} points",
                iconEmoji = "🎯",
                targetAmount = (5000 * levelFactor).toInt(),
                rewardCoins = 200,
                rewardXp = 400L
            ),
            LiveMission(
                id = "daily_combos_${daySeed}",
                category = LiveMissionCategory.DAILY,
                type = LiveMissionType.PERFORM_COMBOS,
                title = "Combo Striker",
                description = "Execute 3 Combos of x2 or higher",
                iconEmoji = "🔥",
                targetAmount = 3,
                rewardCoins = 180,
                rewardXp = 350L
            ),
            LiveMission(
                id = "daily_matches_${daySeed}",
                category = LiveMissionCategory.DAILY,
                type = LiveMissionType.PLAY_MATCHES,
                title = "Daily Challenger",
                description = "Play 5 Block Master matches",
                iconEmoji = "🎮",
                targetAmount = 5,
                rewardCoins = 120,
                rewardXp = 250L
            ),
            LiveMission(
                id = "daily_bombs_${daySeed}",
                category = LiveMissionCategory.DAILY,
                type = LiveMissionType.USE_BOMBS,
                title = "Demolition Expert",
                description = "Trigger 3 Bomb Block explosions",
                iconEmoji = "💣",
                targetAmount = 3,
                rewardCoins = 250,
                rewardXp = 500L
            ),
            LiveMission(
                id = "daily_ice_${daySeed}",
                category = LiveMissionCategory.DAILY,
                type = LiveMissionType.DESTROY_ICE,
                title = "Ice Breaker",
                description = "Shatter 10 Ice Blocks",
                iconEmoji = "🧊",
                targetAmount = 10,
                rewardCoins = 220,
                rewardXp = 450L
            ),
            LiveMission(
                id = "daily_coins_${daySeed}",
                category = LiveMissionCategory.DAILY,
                type = LiveMissionType.EARN_COINS,
                title = "Gold Rush",
                description = "Collect 100 bonus coins in matches",
                iconEmoji = "🪙",
                targetAmount = 100,
                rewardCoins = 150,
                rewardXp = 300L
            )
        )

        return missionPool.shuffled(random).take(4)
    }

    // Generate 3 harder weekly missions for a given week timestamp
    fun generateWeeklyMissions(weekSeed: Long, playerLevel: Int): List<LiveMission> {
        val random = kotlin.random.Random(weekSeed)
        val levelFactor = (1.0 + (playerLevel - 1) * 0.08).coerceAtMost(4.0)

        val missionPool = listOf(
            LiveMission(
                id = "weekly_lines_${weekSeed}",
                category = LiveMissionCategory.WEEKLY,
                type = LiveMissionType.CLEAR_LINES,
                title = "Grand Line Master",
                description = "Clear ${(300 * levelFactor).toInt()} lines this week",
                iconEmoji = "⚡",
                targetAmount = (300 * levelFactor).toInt(),
                rewardCoins = 1000,
                rewardXp = 2000L
            ),
            LiveMission(
                id = "weekly_score_${weekSeed}",
                category = LiveMissionCategory.WEEKLY,
                type = LiveMissionType.SCORE_POINTS,
                title = "Legendary Score",
                description = "Accumulate ${(100000 * levelFactor).toInt()} total points",
                iconEmoji = "👑",
                targetAmount = (100000 * levelFactor).toInt(),
                rewardCoins = 1500,
                rewardXp = 3000L
            ),
            LiveMission(
                id = "weekly_matches_${weekSeed}",
                category = LiveMissionCategory.WEEKLY,
                type = LiveMissionType.PLAY_MATCHES,
                title = "Marathon Runner",
                description = "Complete 50 matches",
                iconEmoji = "🏆",
                targetAmount = 50,
                rewardCoins = 1200,
                rewardXp = 2500L
            ),
            LiveMission(
                id = "weekly_coins_${weekSeed}",
                category = LiveMissionCategory.WEEKLY,
                type = LiveMissionType.EARN_COINS,
                title = "Treasure Collector",
                description = "Collect 2,000 coins",
                iconEmoji = "💰",
                targetAmount = 2000,
                rewardCoins = 800,
                rewardXp = 1800L
            ),
            LiveMission(
                id = "weekly_powerups_${weekSeed}",
                category = LiveMissionCategory.WEEKLY,
                type = LiveMissionType.USE_POWERUPS,
                title = "Tactical Overdrive",
                description = "Use 20 Power-Ups during gameplay",
                iconEmoji = "🧪",
                targetAmount = 20,
                rewardCoins = 1500,
                rewardXp = 3500L
            )
        )

        return missionPool.shuffled(random).take(3)
    }

    // Get current day epoch key (YYYYMMDD)
    fun getCurrentDayKey(): Long {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val day = calendar.get(Calendar.DAY_OF_YEAR)
        return (year * 1000 + day).toLong()
    }

    // Get current week epoch key (YYYYWW)
    fun getCurrentWeekKey(): Long {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        return (year * 100 + week).toLong()
    }
}
