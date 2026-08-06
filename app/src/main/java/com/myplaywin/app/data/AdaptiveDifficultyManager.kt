package com.myplaywin.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

data class AdaptiveDifficultySettings(
    val difficultyOffset: Float, // Adjusts base difficulty (e.g., -0.15f to +0.15f)
    val increaseSafePlatforms: Boolean, // Struggling players get extra safe helper platforms
    val reduceEnemyDensity: Boolean, // Struggling players face fewer enemies
    val addBonusPaths: Boolean, // Advanced players get additional alternative paths
    val addRiskRewardShortcuts: Boolean, // Advanced players get dangerous shortcuts
    val hiddenCavesChance: Float, // Decides chance of hidden cave sections
    val secretStarRoutesChance: Float, // Decides chance of secret high-reward star routes
    val verticalExplorationChance: Float, // Decides chance of sections involving vertical climbing
    val bonusCoinRoomChance: Float // Decides chance of a dedicated bonus room filled with coins
)

object AdaptiveDifficultyManager {

    private const val TAG = "AdaptiveDifficulty"
    private const val PREFS_NAME = "bounce_game_prefs"

    // Save player metrics for a completed level
    fun recordSessionPerformance(
        context: Context,
        levelNum: Int,
        deaths: Int,
        missedJumps: Int,
        starsCollected: Int,
        totalLevelStars: Int,
        timeSeconds: Float,
        checkpointRespawns: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val starRatio = if (totalLevelStars > 0) starsCollected.toFloat() / totalLevelStars else 1.0f

        prefs.edit()
            .putInt("perf_deaths_$levelNum", deaths)
            .putInt("perf_missed_jumps_$levelNum", missedJumps)
            .putFloat("perf_star_ratio_$levelNum", starRatio)
            .putFloat("perf_time_$levelNum", timeSeconds)
            .putInt("perf_checkpoints_$levelNum", checkpointRespawns)
            .putBoolean("perf_recorded_$levelNum", true)
            .apply()

        Log.d(TAG, "Recorded performance for Level $levelNum: deaths=$deaths, missedJumps=$missedJumps, starRatio=$starRatio, time=$timeSeconds, checkpoints=$checkpointRespawns")
    }

    // Retrieve adaptive settings for level generation based on past performance
    fun getAdaptiveSettings(context: Context, levelNum: Int): AdaptiveDifficultySettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Analyze last 3 completed levels (of the procedural/infinite system)
        var analyzedCount = 0
        var totalDeaths = 0
        var totalMissedJumps = 0
        var totalStarRatio = 0f
        var totalCheckpoints = 0
        var totalTime = 0f

        // Check recent levels (going back up to 5 levels to find at least 3 with performance records)
        var checkLevel = levelNum - 1
        while (checkLevel >= 1 && analyzedCount < 3) {
            if (prefs.getBoolean("perf_recorded_$checkLevel", false)) {
                totalDeaths += prefs.getInt("perf_deaths_$checkLevel", 0)
                totalMissedJumps += prefs.getInt("perf_missed_jumps_$checkLevel", 0)
                totalStarRatio += prefs.getFloat("perf_star_ratio_$checkLevel", 1.0f)
                totalCheckpoints += prefs.getInt("perf_checkpoints_$checkLevel", 0)
                totalTime += prefs.getFloat("perf_time_$checkLevel", 45f)
                analyzedCount++
            }
            checkLevel--
        }

        // Default multipliers/values if no history yet
        if (analyzedCount == 0) {
            return AdaptiveDifficultySettings(
                difficultyOffset = 0f,
                increaseSafePlatforms = false,
                reduceEnemyDensity = false,
                addBonusPaths = levelNum > 5,
                addRiskRewardShortcuts = levelNum > 8,
                hiddenCavesChance = 0.25f,
                secretStarRoutesChance = 0.25f,
                verticalExplorationChance = 0.3f,
                bonusCoinRoomChance = 0.2f
            )
        }

        val avgDeaths = totalDeaths.toFloat() / analyzedCount
        val avgMissedJumps = totalMissedJumps.toFloat() / analyzedCount
        val avgStarRatio = totalStarRatio / analyzedCount
        val avgCheckpoints = totalCheckpoints.toFloat() / analyzedCount
        val avgTime = totalTime / analyzedCount

        // Define dynamic scoring model (High score = player performs well, Low score = player struggles)
        // Base score starts at 1.0f
        var performanceScore = 1.0f

        // Penalize for heavy deaths, checkpoints usage, and missed jumps
        performanceScore -= (avgDeaths * 0.22f)
        performanceScore -= (avgCheckpoints * 0.12f)
        performanceScore -= (avgMissedJumps * 0.04f)

        // Reward for high star collection ratio
        performanceScore += (avgStarRatio - 0.7f) * 0.4f

        // Penalize if taking excessively long to finish (e.g. > 120s on average, indicating struggling/getting lost)
        if (avgTime > 120f) {
            performanceScore -= 0.15f
        } else if (avgTime < 45f) {
            // Speed runners get bonus challenge
            performanceScore += 0.1f
        }

        performanceScore = performanceScore.coerceIn(0.0f, 1.0f)

        Log.d(TAG, "Adaptive Engine Analysed $analyzedCount levels. AvgDeaths: $avgDeaths, AvgMissedJumps: $avgMissedJumps, AvgStarRatio: $avgStarRatio, PerformanceScore: $performanceScore")

        // Define adaptive settings according to player skill bracket
        return when {
            performanceScore < 0.45f -> {
                // Bracket: Struggling. Reduce difficulty and increase helpers.
                AdaptiveDifficultySettings(
                    difficultyOffset = -0.15f,
                    increaseSafePlatforms = true,
                    reduceEnemyDensity = true,
                    addBonusPaths = false,
                    addRiskRewardShortcuts = false,
                    hiddenCavesChance = 0.15f, // simpler levels
                    secretStarRoutesChance = 0.1f, // prioritize completion
                    verticalExplorationChance = 0.15f, // reduce complex vertically
                    bonusCoinRoomChance = 0.4f // provide generous coin aids to buy items/skins!
                )
            }
            performanceScore < 0.65f -> {
                // Bracket: Moderately challenged. Slightly tune down or keep baseline.
                AdaptiveDifficultySettings(
                    difficultyOffset = -0.05f,
                    increaseSafePlatforms = avgDeaths > 1.0f,
                    reduceEnemyDensity = avgDeaths > 1.2f,
                    addBonusPaths = true,
                    addRiskRewardShortcuts = false,
                    hiddenCavesChance = 0.25f,
                    secretStarRoutesChance = 0.25f,
                    verticalExplorationChance = 0.3f,
                    bonusCoinRoomChance = 0.25f
                )
            }
            performanceScore > 0.85f -> {
                // Bracket: Advanced/Flawless. Maximize challenge and content variety!
                AdaptiveDifficultySettings(
                    difficultyOffset = 0.15f,
                    increaseSafePlatforms = false,
                    reduceEnemyDensity = false,
                    addBonusPaths = true,
                    addRiskRewardShortcuts = true,
                    hiddenCavesChance = 0.4f,
                    secretStarRoutesChance = 0.5f,
                    verticalExplorationChance = 0.45f,
                    bonusCoinRoomChance = 0.3f
                )
            }
            else -> {
                // Bracket: Perfectly balanced baseline.
                AdaptiveDifficultySettings(
                    difficultyOffset = 0.05f,
                    increaseSafePlatforms = false,
                    reduceEnemyDensity = false,
                    addBonusPaths = true,
                    addRiskRewardShortcuts = true,
                    hiddenCavesChance = 0.3f,
                    secretStarRoutesChance = 0.3f,
                    verticalExplorationChance = 0.35f,
                    bonusCoinRoomChance = 0.2f
                )
            }
        }
    }
}
