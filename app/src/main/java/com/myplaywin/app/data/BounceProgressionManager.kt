package com.myplaywin.app.data

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.myplaywin.app.ui.viewmodel.PlayWinViewModel

data class BounceAchievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean = false,
    val rewardCoins: Int = 100
)

data class BounceGameStats(
    val totalPlayTimeSeconds: Long = 0L,
    val levelsCompletedCount: Int = 0,
    val totalCoinsCollected: Int = 0,
    val totalStarsCollected: Int = 0,
    val deathCount: Int = 0,
    val highestCombo: Int = 0,
    val bestTimeSeconds: Float = 9999f
)

data class BounceDailyMission(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val rewardCoins: Int,
    val isClaimed: Boolean = false
)

object BounceProgressionManager {

    val INITIAL_ACHIEVEMENTS = listOf(
        BounceAchievement("ach_first_level", "First Bounce", "Complete Level 1", "🎯", rewardCoins = 100),
        BounceAchievement("ach_coin_100", "Coin Collector", "Collect 100 total Coins", "🪙", rewardCoins = 150),
        BounceAchievement("ach_coin_1000", "Treasury Tycoon", "Collect 1,000 total Coins", "💎", rewardCoins = 500),
        BounceAchievement("ach_world_1", "World 1 Champion", "Complete World 1 (Levels 1-5)", "🏆", rewardCoins = 300),
        BounceAchievement("ach_game_complete", "Bounce Legend", "Conquer all 20 Levels", "👑", rewardCoins = 1000),
        BounceAchievement("ach_speed_runner", "Speed Demon", "Finish any level in under 30 seconds", "⚡", rewardCoins = 250),
        BounceAchievement("ach_star_hunter", "Star Collector", "Earn 30 total Stars", "⭐", rewardCoins = 300),
        BounceAchievement("ach_no_damage", "Flawless Bouncer", "Finish a level without taking damage", "🛡️", rewardCoins = 200)
    )

    fun getAchievements(prefs: SharedPreferences): List<BounceAchievement> {
        val unlockedSet = prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
        return INITIAL_ACHIEVEMENTS.map { ach ->
            ach.copy(isUnlocked = unlockedSet.contains(ach.id))
        }
    }

    fun unlockAchievement(context: Context, prefs: SharedPreferences, viewModel: PlayWinViewModel, achId: String) {
        val unlockedSet = prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
        if (!unlockedSet.contains(achId)) {
            val updatedSet = unlockedSet + achId
            prefs.edit().putStringSet("unlocked_achievements", updatedSet).apply()

            val ach = INITIAL_ACHIEVEMENTS.find { it.id == achId }
            if (ach != null) {
                viewModel.addCoins(ach.rewardCoins, "Achievement: ${ach.title}")
                Toast.makeText(context, "🏆 Achievement Unlocked: ${ach.title} (+${ach.rewardCoins} 🪙)", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getStats(prefs: SharedPreferences): BounceGameStats {
        return BounceGameStats(
            totalPlayTimeSeconds = prefs.getLong("stats_play_time", 0L),
            levelsCompletedCount = prefs.getInt("stats_levels_completed", 0),
            totalCoinsCollected = prefs.getInt("stats_coins_collected", 0),
            totalStarsCollected = prefs.getInt("stats_stars_collected", 0),
            deathCount = prefs.getInt("stats_death_count", 0),
            highestCombo = prefs.getInt("stats_highest_combo", 0),
            bestTimeSeconds = prefs.getFloat("stats_best_time", 9999f)
        )
    }

    fun recordLevelCompleted(
        context: Context,
        prefs: SharedPreferences,
        viewModel: PlayWinViewModel,
        levelNum: Int,
        stars: Int,
        coins: Int,
        timeSeconds: Float,
        tookDamage: Boolean
    ) {
        val currentStats = getStats(prefs)
        val newLevelsCount = currentStats.levelsCompletedCount + 1
        val newCoins = currentStats.totalCoinsCollected + coins
        val newStars = currentStats.totalStarsCollected + stars
        val newBestTime = if (timeSeconds < currentStats.bestTimeSeconds) timeSeconds else currentStats.bestTimeSeconds

        prefs.edit()
            .putInt("stats_levels_completed", newLevelsCount)
            .putInt("stats_coins_collected", newCoins)
            .putInt("stats_stars_collected", newStars)
            .putFloat("stats_best_time", newBestTime)
            .apply()

        // Achievement checks
        if (levelNum >= 1) unlockAchievement(context, prefs, viewModel, "ach_first_level")
        if (newCoins >= 100) unlockAchievement(context, prefs, viewModel, "ach_coin_100")
        if (newCoins >= 1000) unlockAchievement(context, prefs, viewModel, "ach_coin_1000")
        if (levelNum >= 5) unlockAchievement(context, prefs, viewModel, "ach_world_1")
        if (levelNum >= 20) unlockAchievement(context, prefs, viewModel, "ach_game_complete")
        if (timeSeconds < 30f) unlockAchievement(context, prefs, viewModel, "ach_speed_runner")
        if (newStars >= 30) unlockAchievement(context, prefs, viewModel, "ach_star_hunter")
        if (!tookDamage) unlockAchievement(context, prefs, viewModel, "ach_no_damage")

        // Daily missions updates
        updateDailyMissionProgress(prefs, "mission_coins", coins)
        updateDailyMissionProgress(prefs, "mission_levels", 1)
        updateDailyMissionProgress(prefs, "mission_stars", stars)
        if (!tookDamage) updateDailyMissionProgress(prefs, "mission_nodamage", 1)
    }

    fun recordDeath(prefs: SharedPreferences) {
        val currentDeaths = prefs.getInt("stats_death_count", 0)
        prefs.edit().putInt("stats_death_count", currentDeaths + 1).apply()
    }

    fun addPlayTime(prefs: SharedPreferences, addSeconds: Long) {
        val current = prefs.getLong("stats_play_time", 0L)
        prefs.edit().putLong("stats_play_time", current + addSeconds).apply()
    }

    fun getDailyMissions(prefs: SharedPreferences): List<BounceDailyMission> {
        return listOf(
            BounceDailyMission(
                id = "mission_coins",
                title = "Coin Rush",
                description = "Collect 50 Coins in Bounce Quest",
                currentProgress = prefs.getInt("daily_progress_mission_coins", 0),
                maxProgress = 50,
                rewardCoins = 150,
                isClaimed = prefs.getBoolean("daily_claimed_mission_coins", false)
            ),
            BounceDailyMission(
                id = "mission_levels",
                title = "Level Master",
                description = "Complete 3 Levels",
                currentProgress = prefs.getInt("daily_progress_mission_levels", 0),
                maxProgress = 3,
                rewardCoins = 200,
                isClaimed = prefs.getBoolean("daily_claimed_mission_levels", false)
            ),
            BounceDailyMission(
                id = "mission_stars",
                title = "Star Collector",
                description = "Collect 5 Stars across levels",
                currentProgress = prefs.getInt("daily_progress_mission_stars", 0),
                maxProgress = 5,
                rewardCoins = 250,
                isClaimed = prefs.getBoolean("daily_claimed_mission_stars", false)
            ),
            BounceDailyMission(
                id = "mission_nodamage",
                title = "Untouchable",
                description = "Finish 1 Level without taking damage",
                currentProgress = prefs.getInt("daily_progress_mission_nodamage", 0),
                maxProgress = 1,
                rewardCoins = 300,
                isClaimed = prefs.getBoolean("daily_claimed_mission_nodamage", false)
            )
        )
    }

    private fun updateDailyMissionProgress(prefs: SharedPreferences, missionId: String, amount: Int) {
        val key = "daily_progress_$missionId"
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + amount).apply()
    }

    fun claimDailyMission(context: Context, prefs: SharedPreferences, viewModel: PlayWinViewModel, missionId: String) {
        val missions = getDailyMissions(prefs)
        val m = missions.find { it.id == missionId }
        if (m != null && m.currentProgress >= m.maxProgress && !m.isClaimed) {
            prefs.edit().putBoolean("daily_claimed_$missionId", true).apply()
            viewModel.addCoins(m.rewardCoins, "Daily Mission: ${m.title}")
            Toast.makeText(context, "🎯 Claimed Mission Reward +${m.rewardCoins} 🪙!", Toast.LENGTH_SHORT).show()
        }
    }
}
