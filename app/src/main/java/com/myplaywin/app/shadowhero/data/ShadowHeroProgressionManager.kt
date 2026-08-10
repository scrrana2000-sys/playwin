package com.myplaywin.app.shadowhero.data

import android.content.Context
import android.content.SharedPreferences
import com.myplaywin.app.data.repository.WalletService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ShadowHeroStats(
    val bestStage: Int = 1,
    val highScore: Int = 0,
    val totalEnemiesDefeated: Int = 0,
    val totalMatchesPlayed: Int = 0,
    val playerLevel: Int = 1,
    val currentExp: Int = 0,
    val expToNextLevel: Int = 500,
    val totalCrystals: Int = 0,
    val bestCompletionTime: Float = 0f,
    val totalDeaths: Int = 0,
    val totalDistance: Float = 0f,
    val powerUpsCollected: Int = 0,
    val totalDashes: Int = 0,
    val totalJumps: Int = 0,
    val totalWallJumps: Int = 0,
    val checkpointsActivated: Int = 0,
    val stagesCompleted: Int = 0,
    val totalPlayTimeSeconds: Long = 0L,
    val continueAdsUsed: Int = 0
)

data class ShadowHeroSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val musicVolume: Float = 0.8f,
    val sfxVolume: Float = 0.8f,
    val showFps: Boolean = false,
    val controlSensitivity: Float = 1.0f
)

data class ShadowHeroAchievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false,
    val rewardCoins: Int = 50
)

data class ShadowHeroMission(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val currentProgress: Int,
    val maxProgress: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean
)

data class ShadowHeroDailyChallenge(
    val dateString: String,
    val seed: Long,
    val title: String,
    val description: String,
    val goalStage: Int,
    val rewardCoins: Int = 50,
    val isCompleted: Boolean,
    val isClaimed: Boolean
)

object ShadowHeroProgressionManager {
    private const val PREFS_NAME = "shadow_hero_prefs"
    private const val KEY_BEST_STAGE = "key_best_stage"
    private const val KEY_HIGH_SCORE = "key_high_score"
    private const val KEY_TOTAL_ENEMIES = "key_total_enemies"
    private const val KEY_MATCHES_PLAYED = "key_matches_played"
    private const val KEY_PLAYER_LEVEL = "key_player_level"
    private const val KEY_CURRENT_EXP = "key_current_exp"

    private const val KEY_TOTAL_CRYSTALS = "key_total_crystals"
    private const val KEY_BEST_TIME = "key_best_time"
    private const val KEY_TOTAL_DEATHS = "key_total_deaths"
    private const val KEY_TOTAL_DISTANCE = "key_total_distance"
    private const val KEY_POWERUPS_COLLECTED = "key_powerups_collected"
    private const val KEY_TOTAL_DASHES = "key_total_dashes"
    private const val KEY_TOTAL_JUMPS = "key_total_jumps"
    private const val KEY_TOTAL_WALL_JUMPS = "key_total_wall_jumps"
    private const val KEY_CHECKPOINTS = "key_checkpoints"
    private const val KEY_STAGES_COMPLETED = "key_stages_completed"
    private const val KEY_PLAY_TIME_SEC = "key_play_time_sec"
    private const val KEY_CONTINUE_ADS = "key_continue_ads"

    private const val KEY_SOUND = "key_sound"
    private const val KEY_MUSIC = "key_music"
    private const val KEY_VIBE = "key_vibe"
    private const val KEY_MUSIC_VOL = "key_music_vol"
    private const val KEY_SFX_VOL = "key_sfx_vol"

    private const val PREFIX_CLAIMED_TX = "tx_claimed_"
    private const val PREFIX_ACH_UNLOCKED = "ach_unlocked_"
    private const val PREFIX_ACH_CLAIMED = "ach_claimed_"
    private const val PREFIX_MISSION_CLAIMED = "mission_claimed_"
    private const val PREFIX_MISSION_PROGRESS = "mission_progress_"
    private const val KEY_MISSION_DATE = "mission_last_date"
    private const val KEY_DAILY_CHALLENGE_CLAIMED = "daily_challenge_claimed_"
    private const val KEY_DAILY_CHALLENGE_COMPLETED = "daily_challenge_completed_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getTodayUtcDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun getStats(context: Context): ShadowHeroStats {
        val prefs = getPrefs(context)
        return ShadowHeroStats(
            bestStage = prefs.getInt(KEY_BEST_STAGE, 1),
            highScore = prefs.getInt(KEY_HIGH_SCORE, 0),
            totalEnemiesDefeated = prefs.getInt(KEY_TOTAL_ENEMIES, 0),
            totalMatchesPlayed = prefs.getInt(KEY_MATCHES_PLAYED, 0),
            playerLevel = prefs.getInt(KEY_PLAYER_LEVEL, 1),
            currentExp = prefs.getInt(KEY_CURRENT_EXP, 0),
            expToNextLevel = 500,
            totalCrystals = prefs.getInt(KEY_TOTAL_CRYSTALS, 0),
            bestCompletionTime = prefs.getFloat(KEY_BEST_TIME, 0f),
            totalDeaths = prefs.getInt(KEY_TOTAL_DEATHS, 0),
            totalDistance = prefs.getFloat(KEY_TOTAL_DISTANCE, 0f),
            powerUpsCollected = prefs.getInt(KEY_POWERUPS_COLLECTED, 0),
            totalDashes = prefs.getInt(KEY_TOTAL_DASHES, 0),
            totalJumps = prefs.getInt(KEY_TOTAL_JUMPS, 0),
            totalWallJumps = prefs.getInt(KEY_TOTAL_WALL_JUMPS, 0),
            checkpointsActivated = prefs.getInt(KEY_CHECKPOINTS, 0),
            stagesCompleted = prefs.getInt(KEY_STAGES_COMPLETED, 0),
            totalPlayTimeSeconds = prefs.getLong(KEY_PLAY_TIME_SEC, 0L),
            continueAdsUsed = prefs.getInt(KEY_CONTINUE_ADS, 0)
        )
    }

    fun recordGameplayMetrics(
        context: Context,
        dashes: Int = 0,
        jumps: Int = 0,
        wallJumps: Int = 0,
        checkpoints: Int = 0,
        powerUps: Int = 0,
        crystals: Int = 0,
        deaths: Int = 0,
        distance: Float = 0f,
        playTimeSeconds: Long = 0L,
        continueAds: Int = 0
    ) {
        val prefs = getPrefs(context)
        val currentDashes = prefs.getInt(KEY_TOTAL_DASHES, 0)
        val currentJumps = prefs.getInt(KEY_TOTAL_JUMPS, 0)
        val currentWallJumps = prefs.getInt(KEY_TOTAL_WALL_JUMPS, 0)
        val currentCheckpoints = prefs.getInt(KEY_CHECKPOINTS, 0)
        val currentPowerUps = prefs.getInt(KEY_POWERUPS_COLLECTED, 0)
        val currentCrystals = prefs.getInt(KEY_TOTAL_CRYSTALS, 0)
        val currentDeaths = prefs.getInt(KEY_TOTAL_DEATHS, 0)
        val currentDistance = prefs.getFloat(KEY_TOTAL_DISTANCE, 0f)
        val currentPlayTime = prefs.getLong(KEY_PLAY_TIME_SEC, 0L)
        val currentContinueAds = prefs.getInt(KEY_CONTINUE_ADS, 0)

        prefs.edit().apply {
            putInt(KEY_TOTAL_DASHES, currentDashes + dashes)
            putInt(KEY_TOTAL_JUMPS, currentJumps + jumps)
            putInt(KEY_TOTAL_WALL_JUMPS, currentWallJumps + wallJumps)
            putInt(KEY_CHECKPOINTS, currentCheckpoints + checkpoints)
            putInt(KEY_POWERUPS_COLLECTED, currentPowerUps + powerUps)
            putInt(KEY_TOTAL_CRYSTALS, currentCrystals + crystals)
            putInt(KEY_TOTAL_DEATHS, currentDeaths + deaths)
            putFloat(KEY_TOTAL_DISTANCE, currentDistance + distance)
            putLong(KEY_PLAY_TIME_SEC, currentPlayTime + playTimeSeconds)
            putInt(KEY_CONTINUE_ADS, currentContinueAds + continueAds)
            apply()
        }

        // Update daily mission progress for actions
        if (dashes > 0) incrementMissionProgress(context, "m_dash", dashes)
        if (checkpoints > 0) incrementMissionProgress(context, "m_checkpoint", checkpoints)
        if (crystals > 0) incrementMissionProgress(context, "m_crystals", crystals)
    }

    fun updateStatsOnStageComplete(
        context: Context,
        stage: Int,
        crystalsCollected: Int,
        completionTime: Float,
        distanceTraveled: Float,
        powerUpsUsed: Int,
        deathsInStage: Int = 0
    ) {
        val prefs = getPrefs(context)
        val currentBestStage = prefs.getInt(KEY_BEST_STAGE, 1)
        val currentBestTime = prefs.getFloat(KEY_BEST_TIME, 0f)
        val currentStagesCompleted = prefs.getInt(KEY_STAGES_COMPLETED, 0)

        val nextUnlockedStage = maxOf(currentBestStage, stage + 1)

        prefs.edit().apply {
            putInt(KEY_BEST_STAGE, nextUnlockedStage)
            if (currentBestTime == 0f || completionTime < currentBestTime) putFloat(KEY_BEST_TIME, completionTime)
            putInt(KEY_STAGES_COMPLETED, currentStagesCompleted + 1)
            apply()
        }

        // Mission tracking
        incrementMissionProgress(context, "m_play_1", 1)
        incrementMissionProgress(context, "m_complete_3", 1)
        if (deathsInStage == 0) incrementMissionProgress(context, "m_no_death", 1)
        if (stage >= 5) incrementMissionProgress(context, "m_reach_stage5", 1)

        // Check daily challenge progress
        checkDailyChallengeStageCleared(context, stage, deathsInStage, crystalsCollected)
    }

    fun incrementDeathCount(context: Context) {
        val prefs = getPrefs(context)
        val deaths = prefs.getInt(KEY_TOTAL_DEATHS, 0)
        prefs.edit().putInt(KEY_TOTAL_DEATHS, deaths + 1).apply()
    }

    fun updateStatsOnGameEnd(context: Context, stageReached: Int, score: Int, enemiesDefeated: Int) {
        val prefs = getPrefs(context)
        val currentBest = prefs.getInt(KEY_BEST_STAGE, 1)
        val currentHigh = prefs.getInt(KEY_HIGH_SCORE, 0)
        val currentEnemies = prefs.getInt(KEY_TOTAL_ENEMIES, 0)
        val currentMatches = prefs.getInt(KEY_MATCHES_PLAYED, 0)

        prefs.edit().apply {
            if (stageReached > currentBest) putInt(KEY_BEST_STAGE, stageReached)
            if (score > currentHigh) putInt(KEY_HIGH_SCORE, score)
            putInt(KEY_TOTAL_ENEMIES, currentEnemies + enemiesDefeated)
            putInt(KEY_MATCHES_PLAYED, currentMatches + 1)
            apply()
        }
    }

    // --- COIN REWARD SYSTEM & ATOMIC TRANSACTION ---
    fun awardStageCompletionCoins(
        context: Context,
        userId: String,
        stage: Int,
        seed: Long,
        crystalsCollected: Int,
        totalCrystalsInStage: Int,
        completionTime: Float,
        deathsInStage: Int,
        onResult: (Boolean, Int, String?) -> Unit
    ) {
        val txKey = "sh_stage_${userId}_${stage}_${seed}"
        val prefs = getPrefs(context)
        
        if (prefs.getBoolean(PREFIX_CLAIMED_TX + txKey, false)) {
            onResult(false, 0, "Stage reward already claimed for this run.")
            return
        }

        val totalCoins = when {
            stage % 3 == 1 -> ShadowHeroRewardConfig.LEVEL_EASY_REWARD
            stage % 3 == 2 -> ShadowHeroRewardConfig.LEVEL_NORMAL_REWARD
            else -> ShadowHeroRewardConfig.LEVEL_HARD_REWARD
        }.coerceAtMost(ShadowHeroRewardConfig.MAX_LEVEL_REWARD)

        WalletService.updateWallet(
            userId = userId,
            coinsDelta = totalCoins,
            source = "SHADOW_HERO",
            type = "LEVEL_COMPLETE",
            onComplete = { success, _, _, error ->
                if (success) {
                    prefs.edit().putBoolean(PREFIX_CLAIMED_TX + txKey, true).apply()
                    
                    // Award crystals collected as separate COLLECTIBLE transaction
                    val collectibleCoins = (crystalsCollected * ShadowHeroRewardConfig.COLLECTIBLE_REWARD).coerceAtMost(5)
                    if (collectibleCoins > 0) {
                        WalletService.updateWallet(
                            userId = userId,
                            coinsDelta = collectibleCoins,
                            source = "SHADOW_HERO",
                            type = "COLLECTIBLE",
                            onComplete = { _, _, _, _ -> }
                        )
                    }
                    
                    onResult(true, totalCoins, null)
                } else {
                    onResult(false, 0, error ?: "Transaction failed")
                }
            }
        )
    }

    fun isDoubleRewardClaimed(context: Context, userId: String, stage: Int, seed: Long): Boolean {
        val txDoubleKey = "sh_stage_double_${userId}_${stage}_${seed}"
        return getPrefs(context).getBoolean(PREFIX_CLAIMED_TX + txDoubleKey, false)
    }

    fun awardDoubleStageReward(
        context: Context,
        userId: String,
        stage: Int,
        seed: Long,
        baseRewardAwarded: Int,
        onResult: (Boolean, Int, String?) -> Unit
    ) {
        val txDoubleKey = "sh_stage_double_${userId}_${stage}_${seed}"
        val prefs = getPrefs(context)
        
        if (prefs.getBoolean(PREFIX_CLAIMED_TX + txDoubleKey, false)) {
            onResult(false, 0, "Double reward already claimed for this stage.")
            return
        }

        val extraReward = baseRewardAwarded
        val totalWithDouble = baseRewardAwarded + extraReward
        
        val cappedExtra = if (totalWithDouble > ShadowHeroRewardConfig.MAX_LEVEL_REWARD_WITH_AD) {
            (ShadowHeroRewardConfig.MAX_LEVEL_REWARD_WITH_AD - baseRewardAwarded).coerceAtLeast(0)
        } else {
            extraReward
        }

        if (cappedExtra <= 0) {
            onResult(false, 0, "Maximum ad reward already reached.")
            return
        }

        WalletService.updateWallet(
            userId = userId,
            coinsDelta = cappedExtra,
            source = "SHADOW_HERO",
            type = "REWARDED_DOUBLE",
            onComplete = { success, _, _, error ->
                if (success) {
                    prefs.edit().putBoolean(PREFIX_CLAIMED_TX + txDoubleKey, true).apply()
                    onResult(true, cappedExtra, null)
                } else {
                    onResult(false, 0, error ?: "Transaction failed")
                }
            }
        )
    }

    // --- DAILY MISSIONS ---
    fun getDailyMissions(context: Context): List<ShadowHeroMission> {
        val prefs = getPrefs(context)
        val today = getTodayUtcDateString()
        val lastSavedDate = prefs.getString(KEY_MISSION_DATE, "") ?: ""

        if (lastSavedDate != today) {
            // Reset daily mission progress & claimed status for new day
            val editor = prefs.edit()
            editor.putString(KEY_MISSION_DATE, today)
            listOf("m_play_1", "m_complete_3", "m_crystals", "m_dash", "m_checkpoint", "m_no_death", "m_reach_stage5").forEach { id ->
                editor.putInt(PREFIX_MISSION_PROGRESS + id, 0)
                editor.putBoolean(PREFIX_MISSION_CLAIMED + id, false)
            }
            editor.apply()
        }

        val m1Prog = prefs.getInt(PREFIX_MISSION_PROGRESS + "m_play_1", 0)
        val m1Claim = prefs.getBoolean(PREFIX_MISSION_CLAIMED + "m_play_1", false)

        val m2Prog = prefs.getInt(PREFIX_MISSION_PROGRESS + "m_complete_3", 0)
        val m2Claim = prefs.getBoolean(PREFIX_MISSION_CLAIMED + "m_complete_3", false)

        val m3Prog = prefs.getInt(PREFIX_MISSION_PROGRESS + "m_crystals", 0)
        val m3Claim = prefs.getBoolean(PREFIX_MISSION_CLAIMED + "m_crystals", false)

        val m4Prog = prefs.getInt(PREFIX_MISSION_PROGRESS + "m_dash", 0)
        val m4Claim = prefs.getBoolean(PREFIX_MISSION_CLAIMED + "m_dash", false)

        val m5Prog = prefs.getInt(PREFIX_MISSION_PROGRESS + "m_checkpoint", 0)
        val m5Claim = prefs.getBoolean(PREFIX_MISSION_CLAIMED + "m_checkpoint", false)

        val m6Prog = prefs.getInt(PREFIX_MISSION_PROGRESS + "m_no_death", 0)
        val m6Claim = prefs.getBoolean(PREFIX_MISSION_CLAIMED + "m_no_death", false)

        val m7Prog = prefs.getInt(PREFIX_MISSION_PROGRESS + "m_reach_stage5", 0)
        val m7Claim = prefs.getBoolean(PREFIX_MISSION_CLAIMED + "m_reach_stage5", false)

        return listOf(
            ShadowHeroMission("m_play_1", "Awaken Shadow", "Play 1 Shadow Hero stage", 2, m1Prog, 1, m1Prog >= 1, m1Claim),
            ShadowHeroMission("m_complete_3", "Void Conqueror", "Complete 3 stages", 5, m2Prog, 3, m2Prog >= 3, m2Claim),
            ShadowHeroMission("m_crystals", "Crystal Collector", "Collect 20 crystals", 3, m3Prog, 20, m3Prog >= 20, m3Claim),
            ShadowHeroMission("m_dash", "Shadow Speedster", "Perform 10 dashes", 2, m4Prog, 10, m4Prog >= 10, m4Claim),
            ShadowHeroMission("m_checkpoint", "Anchor of Light", "Activate 1 checkpoint", 2, m5Prog, 1, m5Prog >= 1, m5Claim),
            ShadowHeroMission("m_no_death", "Flawless Runner", "Clear a stage without dying", 4, m6Prog, 1, m6Prog >= 1, m6Claim),
            ShadowHeroMission("m_reach_stage5", "Deep Descent", "Reach Stage 5 or higher", 5, m7Prog, 5, m7Prog >= 5, m7Claim)
        )
    }

    private fun incrementMissionProgress(context: Context, missionId: String, delta: Int) {
        val prefs = getPrefs(context)
        val key = PREFIX_MISSION_PROGRESS + missionId
        val curr = prefs.getInt(key, 0)
        prefs.edit().putInt(key, curr + delta).apply()
    }

    fun claimMissionReward(
        context: Context,
        userId: String,
        missionId: String,
        rewardCoins: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        val prefs = getPrefs(context)
        val claimKey = PREFIX_MISSION_CLAIMED + missionId
        if (prefs.getBoolean(claimKey, false)) {
            onResult(false, "Reward already claimed")
            return
        }

        val finalReward = rewardCoins.coerceAtMost(ShadowHeroRewardConfig.MISSION_MAX_REWARD)
        WalletService.updateWallet(
            userId = userId,
            coinsDelta = finalReward,
            source = "SHADOW_HERO",
            type = "MISSION",
            onComplete = { success, _, _, error ->
                if (success) {
                    prefs.edit().putBoolean(claimKey, true).apply()
                    onResult(true, null)
                } else {
                    onResult(false, error ?: "Transaction failed")
                }
            }
        )
    }

    // --- DAILY CHALLENGES ---
    fun getDailyChallenge(context: Context): ShadowHeroDailyChallenge {
        val dateStr = getTodayUtcDateString()
        val seed = dateStr.hashCode().toLong() + 777L
        val prefs = getPrefs(context)

        val isCompleted = prefs.getBoolean(KEY_DAILY_CHALLENGE_COMPLETED + dateStr, false)
        val isClaimed = prefs.getBoolean(KEY_DAILY_CHALLENGE_CLAIMED + dateStr, false)

        return ShadowHeroDailyChallenge(
            dateString = dateStr,
            seed = seed,
            title = "Daily Realm: Stage 5 Clear",
            description = "Reach and clear Stage 5 on today's deterministic realm seed.",
            goalStage = 5,
            rewardCoins = ShadowHeroRewardConfig.BOSS_MAX_REWARD,
            isCompleted = isCompleted,
            isClaimed = isClaimed
        )
    }

    fun getWeeklyChallengeSeed(): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return "$year-W$week".hashCode().toLong() + 9999L
    }

    private fun checkDailyChallengeStageCleared(context: Context, stage: Int, deathsInStage: Int, crystalsCollected: Int) {
        val challenge = getDailyChallenge(context)
        if (!challenge.isCompleted && stage >= challenge.goalStage) {
            val prefs = getPrefs(context)
            prefs.edit().putBoolean(KEY_DAILY_CHALLENGE_COMPLETED + challenge.dateString, true).apply()
        }
    }

    fun claimDailyChallengeReward(
        context: Context,
        userId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val challenge = getDailyChallenge(context)
        if (!challenge.isCompleted) {
            onResult(false, "Daily Challenge is not yet completed.")
            return
        }

        if (challenge.isClaimed) {
            onResult(false, "Daily Challenge reward already claimed.")
            return
        }

        val finalReward = challenge.rewardCoins.coerceAtMost(ShadowHeroRewardConfig.BOSS_MAX_REWARD)
        WalletService.updateWallet(
            userId = userId,
            coinsDelta = finalReward,
            source = "SHADOW_HERO",
            type = "BOSS",
            onComplete = { success, _, _, error ->
                if (success) {
                    getPrefs(context).edit().putBoolean(KEY_DAILY_CHALLENGE_CLAIMED + challenge.dateString, true).apply()
                    onResult(true, null)
                } else {
                    onResult(false, error ?: "Transaction failed")
                }
            }
        )
    }

    // --- ACHIEVEMENTS SYSTEM ---
    fun getAchievements(context: Context): List<ShadowHeroAchievement> {
        val stats = getStats(context)
        val prefs = getPrefs(context)

        val rawAchievements = listOf(
            ShadowHeroAchievement("ach_first_shadow", "FIRST SHADOW", "Complete your first stage.", "🗡️", stats.stagesCompleted >= 1, rewardCoins = 5),
            ShadowHeroAchievement("ach_crystal_hunter", "CRYSTAL HUNTER", "Collect 100 crystals.", "💎", stats.totalCrystals >= 100, rewardCoins = 10),
            ShadowHeroAchievement("ach_wall_master", "WALL MASTER", "Perform 50 wall jumps.", "🧗", stats.totalWallJumps >= 50, rewardCoins = 10),
            ShadowHeroAchievement("ach_shadow_dasher", "SHADOW DASHER", "Perform 100 dashes.", "⚡", stats.totalDashes >= 100, rewardCoins = 10),
            ShadowHeroAchievement("ach_survivor", "SURVIVOR", "Complete 10 stages without using Continue.", "🛡️", stats.stagesCompleted >= 10 && stats.continueAdsUsed == 0, rewardCoins = 10),
            ShadowHeroAchievement("ach_deep_explorer", "DEEP EXPLORER", "Reach Stage 50.", "🌌", stats.bestStage >= 50, rewardCoins = 10),
            ShadowHeroAchievement("ach_shadow_legend", "SHADOW LEGEND", "Reach Stage 100.", "👑", stats.bestStage >= 100, rewardCoins = 10),
            ShadowHeroAchievement("ach_infinite_runner", "INFINITE RUNNER", "Reach Stage 500.", "🪐", stats.bestStage >= 500, rewardCoins = 10)
        )

        return rawAchievements.map { ach ->
            val wasUnlockedLocally = prefs.getBoolean(PREFIX_ACH_UNLOCKED + ach.id, false)
            val isClaimed = prefs.getBoolean(PREFIX_ACH_CLAIMED + ach.id, false)
            val unlockedNow = ach.isUnlocked || wasUnlockedLocally

            if (unlockedNow && !wasUnlockedLocally) {
                prefs.edit().putBoolean(PREFIX_ACH_UNLOCKED + ach.id, true).apply()
            }

            ach.copy(isUnlocked = unlockedNow, isClaimed = isClaimed)
        }
    }

    fun checkNewAchievements(context: Context): List<ShadowHeroAchievement> {
        val achievements = getAchievements(context)
        val prefs = getPrefs(context)
        val newUnclaimed = mutableListOf<ShadowHeroAchievement>()

        achievements.forEach { ach ->
            if (ach.isUnlocked && !ach.isClaimed) {
                newUnclaimed.add(ach)
            }
        }
        return newUnclaimed
    }

    fun claimAchievementReward(
        context: Context,
        userId: String,
        achievementId: String,
        rewardCoins: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        val prefs = getPrefs(context)
        val claimKey = PREFIX_ACH_CLAIMED + achievementId
        if (prefs.getBoolean(claimKey, false)) {
            onResult(false, "Achievement reward already claimed")
            return
        }

        val finalReward = rewardCoins.coerceAtMost(ShadowHeroRewardConfig.ACHIEVEMENT_MAX_REWARD)
        WalletService.updateWallet(
            userId = userId,
            coinsDelta = finalReward,
            source = "SHADOW_HERO",
            type = "ACHIEVEMENT",
            onComplete = { success, _, _, error ->
                if (success) {
                    prefs.edit().putBoolean(claimKey, true).apply()
                    onResult(true, null)
                } else {
                    onResult(false, error ?: "Transaction failed")
                }
            }
        )
    }

    // --- SETTINGS ---
    fun getSettings(context: Context): ShadowHeroSettings {
        val prefs = getPrefs(context)
        return ShadowHeroSettings(
            soundEnabled = prefs.getBoolean(KEY_SOUND, true),
            musicEnabled = prefs.getBoolean(KEY_MUSIC, true),
            vibrationEnabled = prefs.getBoolean(KEY_VIBE, true),
            musicVolume = prefs.getFloat(KEY_MUSIC_VOL, 0.8f),
            sfxVolume = prefs.getFloat(KEY_SFX_VOL, 0.8f)
        )
    }

    fun saveSettings(context: Context, settings: ShadowHeroSettings) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_SOUND, settings.soundEnabled)
            putBoolean(KEY_MUSIC, settings.musicEnabled)
            putBoolean(KEY_VIBE, settings.vibrationEnabled)
            putFloat(KEY_MUSIC_VOL, settings.musicVolume)
            putFloat(KEY_SFX_VOL, settings.sfxVolume)
            apply()
        }
    }
}

