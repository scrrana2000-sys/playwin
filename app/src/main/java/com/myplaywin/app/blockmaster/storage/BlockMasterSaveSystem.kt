package com.myplaywin.app.blockmaster.storage

import android.content.Context
import android.content.SharedPreferences
import com.myplaywin.app.blockmaster.constants.BlockMasterConstants
import com.myplaywin.app.blockmaster.liveops.MysteryChestRarity
import com.myplaywin.app.blockmaster.powerups.PowerUpType
import com.myplaywin.app.blockmaster.progression.PlayerProgressionManager
import com.myplaywin.app.blockmaster.world.WorldProgressionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BlockMasterSaveData(
    val highScore: Int = 0,
    val coins: Int = 0,
    val playerLevel: Int = 1,
    val playerName: String = "Player 1",
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val musicVolume: Float = 0.8f,
    val sfxVolume: Float = 1.0f,
    val totalGamesPlayed: Int = 0,
    val totalLinesCleared: Int = 0,
    val timePlayedSeconds: Long = 0L,
    val unlockedWorldsCount: Int = 1,
    // Phase 4 Extensions
    val playerXp: Long = 0L,
    val currentInfiniteLevel: Int = 1,
    val highestLevelReached: Int = 1,
    val highestComboAllTime: Int = 0,
    // Phase 5 Power-Up Inventory
    val powerUpClearRow: Int = 3,
    val powerUpClearCol: Int = 3,
    val powerUpDestroyBlock: Int = 3,
    val powerUpFreezeTime: Int = 2,
    val powerUpScoreBooster: Int = 2,
    val powerUpCoinBooster: Int = 2,
    // Phase 5 Special Block & Power-Up Statistics
    val totalBombsExploded: Int = 0,
    val totalIceShattered: Int = 0,
    val totalSteelDestroyed: Int = 0,
    val totalLightningTriggers: Int = 0,
    val totalHighestChainReaction: Int = 0,
    val totalPowerUpsUsed: Int = 0,
    // Phase 6 World System
    val selectedWorldId: Int = 0,
    val claimedWorldRewards: Set<String> = emptySet(),
    // Phase 7 LiveOps System Persistence
    val claimedAchievements: Set<String> = emptySet(),
    val loginCalendarClaimedDays: Set<Int> = emptySet(),
    val lastLoginClaimTimestamp: Long = 0L,
    val unopenedBronzeChests: Int = 1,
    val unopenedSilverChests: Int = 0,
    val unopenedGoldChests: Int = 0,
    val unopenedDiamondChests: Int = 0,
    val unopenedLegendaryChests: Int = 0,
    val claimedDailyMissionIds: Set<String> = emptySet(),
    val claimedWeeklyMissionIds: Set<String> = emptySet(),
    val totalGamesWon: Int = 0,
    val totalGamesLost: Int = 0,
    val totalPerfectClears: Int = 0,
    val notificationsEnabled: Boolean = true,
    // Phase 9 Extensions
    val seasonXp: Long = 0L,
    val currentSeasonId: Int = 1,
    val claimedSeasonFreeRewards: Set<String> = emptySet(),
    val claimedSeasonPremiumRewards: Set<String> = emptySet(),
    val unlockedCosmeticIds: Set<String> = setOf("skin_classic", "theme_classic", "frame_default", "bg_default", "particle_sparks", "title_rookie"),
    val equippedBlockSkin: String = "skin_classic",
    val equippedGridTheme: String = "theme_classic",
    val equippedBoardFrame: String = "frame_default",
    val equippedBackground: String = "bg_default",
    val equippedTitle: String = "Puzzle Rookie",
    val lastLuckySpinTimestamp: Long = 0L,
    val lifetimeScore: Long = 0L,
    val lifetimeCoinsEarned: Long = 0L,
    val lastActiveTimestamp: Long = 0L,
    // Phase 11 Settings Extensions
    val hapticEnabled: Boolean = true,
    val graphicsQuality: String = "High",
    val fpsDisplayEnabled: Boolean = false,
    val selectedLanguage: String = "English"
)

class BlockMasterSaveSystem(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        BlockMasterConstants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _saveData = MutableStateFlow(loadSaveData())
    val saveData: StateFlow<BlockMasterSaveData> = _saveData.asStateFlow()

    fun loadSaveData(): BlockMasterSaveData {
        val xp = prefs.getLong(BlockMasterConstants.KEY_PLAYER_XP, 0L)
        val infLevel = prefs.getInt(BlockMasterConstants.KEY_CURRENT_INFINITE_LEVEL, 1)
        val highestLvl = prefs.getInt(BlockMasterConstants.KEY_HIGHEST_LEVEL, maxOf(1, infLevel))
        val lines = prefs.getInt(BlockMasterConstants.KEY_LINES_CLEARED, 0)
        val pLevel = maxOf(infLevel, PlayerProgressionManager.calculateLevelFromXp(xp))
        val unlockedCount = WorldProgressionManager.getUnlockedWorlds(pLevel).size

        val claimedWorldSet = prefs.getStringSet("claimed_world_rewards", emptySet()) ?: emptySet()
        val claimedAchSet = prefs.getStringSet("claimed_achievements", emptySet()) ?: emptySet()

        val loginClaimedInts = prefs.getStringSet("login_claimed_days", emptySet())
            ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()

        val claimedDailyMissions = prefs.getStringSet("claimed_daily_missions", emptySet()) ?: emptySet()
        val claimedWeeklyMissions = prefs.getStringSet("claimed_weekly_missions", emptySet()) ?: emptySet()

        val selectedWorld = prefs.getInt("selected_world_id", 0)

        return BlockMasterSaveData(
            highScore = prefs.getInt(BlockMasterConstants.KEY_HIGH_SCORE, 0),
            coins = prefs.getInt(BlockMasterConstants.KEY_COINS, 0),
            playerLevel = pLevel,
            playerName = prefs.getString(BlockMasterConstants.KEY_PLAYER_NAME, "Player 1") ?: "Player 1",
            soundEnabled = prefs.getBoolean(BlockMasterConstants.KEY_SOUND_ENABLED, true),
            musicEnabled = prefs.getBoolean(BlockMasterConstants.KEY_MUSIC_ENABLED, true),
            musicVolume = prefs.getFloat(BlockMasterConstants.KEY_MUSIC_VOLUME, 0.8f),
            sfxVolume = prefs.getFloat(BlockMasterConstants.KEY_SFX_VOLUME, 1.0f),
            totalGamesPlayed = prefs.getInt(BlockMasterConstants.KEY_TOTAL_GAMES, 0),
            totalLinesCleared = lines,
            timePlayedSeconds = prefs.getLong(BlockMasterConstants.KEY_TIME_PLAYED_SEC, 0L),
            unlockedWorldsCount = unlockedCount,
            playerXp = xp,
            currentInfiniteLevel = infLevel,
            highestLevelReached = highestLvl,
            highestComboAllTime = prefs.getInt(BlockMasterConstants.KEY_HIGHEST_COMBO, 0),
            // Power-Ups
            powerUpClearRow = prefs.getInt("pu_clear_row", 3),
            powerUpClearCol = prefs.getInt("pu_clear_col", 3),
            powerUpDestroyBlock = prefs.getInt("pu_destroy_block", 3),
            powerUpFreezeTime = prefs.getInt("pu_freeze_time", 2),
            powerUpScoreBooster = prefs.getInt("pu_score_booster", 2),
            powerUpCoinBooster = prefs.getInt("pu_coin_booster", 2),
            // Statistics
            totalBombsExploded = prefs.getInt("stat_bombs", 0),
            totalIceShattered = prefs.getInt("stat_ice", 0),
            totalSteelDestroyed = prefs.getInt("stat_steel", 0),
            totalLightningTriggers = prefs.getInt("stat_lightning", 0),
            totalHighestChainReaction = prefs.getInt("stat_chain_max", 0),
            totalPowerUpsUsed = prefs.getInt("stat_pu_used", 0),
            // World System
            selectedWorldId = selectedWorld,
            claimedWorldRewards = claimedWorldSet,
            // Phase 7 LiveOps Persistence
            claimedAchievements = claimedAchSet,
            loginCalendarClaimedDays = loginClaimedInts,
            lastLoginClaimTimestamp = prefs.getLong("last_login_claim_ts", 0L),
            unopenedBronzeChests = prefs.getInt("chests_bronze", 1),
            unopenedSilverChests = prefs.getInt("chests_silver", 0),
            unopenedGoldChests = prefs.getInt("chests_gold", 0),
            unopenedDiamondChests = prefs.getInt("chests_diamond", 0),
            unopenedLegendaryChests = prefs.getInt("chests_legendary", 0),
            claimedDailyMissionIds = claimedDailyMissions,
            claimedWeeklyMissionIds = claimedWeeklyMissions,
            totalGamesWon = prefs.getInt("stat_games_won", 0),
            totalGamesLost = prefs.getInt("stat_games_lost", 0),
            totalPerfectClears = prefs.getInt("stat_perfect_clears", 0),
            notificationsEnabled = prefs.getBoolean("notif_enabled", true),
            // Phase 9 Loading
            seasonXp = prefs.getLong("season_xp", 0L),
            currentSeasonId = prefs.getInt("current_season_id", 1),
            claimedSeasonFreeRewards = prefs.getStringSet("claimed_season_free_rewards", emptySet()) ?: emptySet(),
            claimedSeasonPremiumRewards = prefs.getStringSet("claimed_season_premium_rewards", emptySet()) ?: emptySet(),
            unlockedCosmeticIds = prefs.getStringSet("unlocked_cosmetics", setOf("skin_classic", "theme_classic", "frame_default", "bg_default", "particle_sparks", "title_rookie")) ?: setOf("skin_classic", "theme_classic", "frame_default", "bg_default", "particle_sparks", "title_rookie"),
            equippedBlockSkin = prefs.getString("equipped_block_skin", "skin_classic") ?: "skin_classic",
            equippedGridTheme = prefs.getString("equipped_grid_theme", "theme_classic") ?: "theme_classic",
            equippedBoardFrame = prefs.getString("equipped_board_frame", "frame_default") ?: "frame_default",
            equippedBackground = prefs.getString("equipped_background", "bg_default") ?: "bg_default",
            equippedTitle = prefs.getString("equipped_title", "Puzzle Rookie") ?: "Puzzle Rookie",
            lastLuckySpinTimestamp = prefs.getLong("last_lucky_spin_ts", 0L),
            lifetimeScore = prefs.getLong("stat_lifetime_score", 0L),
            lifetimeCoinsEarned = prefs.getLong("stat_lifetime_coins", 0L),
            lastActiveTimestamp = prefs.getLong("last_active_ts", System.currentTimeMillis()),
            hapticEnabled = prefs.getBoolean("haptic_enabled", true),
            graphicsQuality = prefs.getString("graphics_quality", "High") ?: "High",
            fpsDisplayEnabled = prefs.getBoolean("fps_display_enabled", false),
            selectedLanguage = prefs.getString("selected_language", "English") ?: "English"
        )
    }

    fun toggleHaptic() {
        val current = _saveData.value.hapticEnabled
        prefs.edit().putBoolean("haptic_enabled", !current).apply()
        _saveData.value = _saveData.value.copy(hapticEnabled = !current)
    }

    fun setGraphicsQuality(quality: String) {
        prefs.edit().putString("graphics_quality", quality).apply()
        _saveData.value = _saveData.value.copy(graphicsQuality = quality)
    }

    fun toggleFpsDisplay() {
        val current = _saveData.value.fpsDisplayEnabled
        prefs.edit().putBoolean("fps_display_enabled", !current).apply()
        _saveData.value = _saveData.value.copy(fpsDisplayEnabled = !current)
    }

    fun setSelectedLanguage(language: String) {
        prefs.edit().putString("selected_language", language).apply()
        _saveData.value = _saveData.value.copy(selectedLanguage = language)
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
        _saveData.value = loadSaveData()
    }

    fun setSelectedWorldId(worldId: Int) {
        prefs.edit().putInt("selected_world_id", worldId).apply()
        _saveData.value = _saveData.value.copy(selectedWorldId = worldId)
    }

    fun claimWorldReward(worldId: Int): Boolean {
        val current = _saveData.value
        val worldIdStr = worldId.toString()
        if (current.claimedWorldRewards.contains(worldIdStr)) return false

        val world = WorldProgressionManager.WORLDS.find { it.id == worldId } ?: return false
        if (current.playerLevel < world.minLevel) return false

        val newClaimedSet = current.claimedWorldRewards + worldIdStr
        val newCoins = current.coins + world.rewardCoins
        val newXp = current.playerXp + world.rewardXp
        val newPlayerLevel = maxOf(current.playerLevel, PlayerProgressionManager.calculateLevelFromXp(newXp))

        prefs.edit()
            .putStringSet("claimed_world_rewards", newClaimedSet)
            .putInt(BlockMasterConstants.KEY_COINS, newCoins)
            .putLong(BlockMasterConstants.KEY_PLAYER_XP, newXp)
            .putInt(BlockMasterConstants.KEY_LEVEL, newPlayerLevel)
            .apply()

        _saveData.value = current.copy(
            claimedWorldRewards = newClaimedSet,
            coins = newCoins,
            playerXp = newXp,
            playerLevel = newPlayerLevel
        )
        return true
    }

    fun claimAchievement(achievementId: String, rewardCoins: Int, rewardXp: Long): Boolean {
        val current = _saveData.value
        if (current.claimedAchievements.contains(achievementId)) return false

        val newClaimed = current.claimedAchievements + achievementId
        val newCoins = current.coins + rewardCoins
        val newXp = current.playerXp + rewardXp
        val newLevel = maxOf(current.playerLevel, PlayerProgressionManager.calculateLevelFromXp(newXp))

        prefs.edit()
            .putStringSet("claimed_achievements", newClaimed)
            .putInt(BlockMasterConstants.KEY_COINS, newCoins)
            .putLong(BlockMasterConstants.KEY_PLAYER_XP, newXp)
            .putInt(BlockMasterConstants.KEY_LEVEL, newLevel)
            .apply()

        _saveData.value = current.copy(
            claimedAchievements = newClaimed,
            coins = newCoins,
            playerXp = newXp,
            playerLevel = newLevel
        )
        return true
    }

    fun claimLoginDay(dayNumber: Int, coins: Int, xp: Long, chestRarity: MysteryChestRarity?, powerUpReward: Pair<PowerUpType, Int>?): Boolean {
        val current = _saveData.value
        if (current.loginCalendarClaimedDays.contains(dayNumber)) return false

        val newClaimedDays = current.loginCalendarClaimedDays + dayNumber
        val newCoins = current.coins + coins
        val newXp = current.playerXp + xp
        val newLevel = maxOf(current.playerLevel, PlayerProgressionManager.calculateLevelFromXp(newXp))
        val now = System.currentTimeMillis()

        val editor = prefs.edit()
            .putStringSet("login_claimed_days", newClaimedDays.map { it.toString() }.toSet())
            .putLong("last_login_claim_ts", now)
            .putInt(BlockMasterConstants.KEY_COINS, newCoins)
            .putLong(BlockMasterConstants.KEY_PLAYER_XP, newXp)
            .putInt(BlockMasterConstants.KEY_LEVEL, newLevel)

        var bCount = current.unopenedBronzeChests
        var sCount = current.unopenedSilverChests
        var gCount = current.unopenedGoldChests
        var dCount = current.unopenedDiamondChests
        var lCount = current.unopenedLegendaryChests

        chestRarity?.let {
            when (it) {
                MysteryChestRarity.BRONZE -> { bCount++; editor.putInt("chests_bronze", bCount) }
                MysteryChestRarity.SILVER -> { sCount++; editor.putInt("chests_silver", sCount) }
                MysteryChestRarity.GOLD -> { gCount++; editor.putInt("chests_gold", gCount) }
                MysteryChestRarity.DIAMOND -> { dCount++; editor.putInt("chests_diamond", dCount) }
                MysteryChestRarity.LEGENDARY -> { lCount++; editor.putInt("chests_legendary", lCount) }
            }
        }

        editor.apply()

        powerUpReward?.let { (puType, amount) ->
            addPowerUpCount(puType, amount)
        }

        _saveData.value = _saveData.value.copy(
            loginCalendarClaimedDays = newClaimedDays,
            lastLoginClaimTimestamp = now,
            coins = newCoins,
            playerXp = newXp,
            playerLevel = newLevel,
            unopenedBronzeChests = bCount,
            unopenedSilverChests = sCount,
            unopenedGoldChests = gCount,
            unopenedDiamondChests = dCount,
            unopenedLegendaryChests = lCount
        )
        return true
    }

    fun addChest(rarity: MysteryChestRarity, amount: Int = 1) {
        val current = _saveData.value
        val editor = prefs.edit()

        val updatedData = when (rarity) {
            MysteryChestRarity.BRONZE -> {
                val n = current.unopenedBronzeChests + amount
                editor.putInt("chests_bronze", n)
                current.copy(unopenedBronzeChests = n)
            }
            MysteryChestRarity.SILVER -> {
                val n = current.unopenedSilverChests + amount
                editor.putInt("chests_silver", n)
                current.copy(unopenedSilverChests = n)
            }
            MysteryChestRarity.GOLD -> {
                val n = current.unopenedGoldChests + amount
                editor.putInt("chests_gold", n)
                current.copy(unopenedGoldChests = n)
            }
            MysteryChestRarity.DIAMOND -> {
                val n = current.unopenedDiamondChests + amount
                editor.putInt("chests_diamond", n)
                current.copy(unopenedDiamondChests = n)
            }
            MysteryChestRarity.LEGENDARY -> {
                val n = current.unopenedLegendaryChests + amount
                editor.putInt("chests_legendary", n)
                current.copy(unopenedLegendaryChests = n)
            }
        }

        editor.apply()
        _saveData.value = updatedData
    }

    fun consumeChest(rarity: MysteryChestRarity): Boolean {
        val current = _saveData.value
        val count = when (rarity) {
            MysteryChestRarity.BRONZE -> current.unopenedBronzeChests
            MysteryChestRarity.SILVER -> current.unopenedSilverChests
            MysteryChestRarity.GOLD -> current.unopenedGoldChests
            MysteryChestRarity.DIAMOND -> current.unopenedDiamondChests
            MysteryChestRarity.LEGENDARY -> current.unopenedLegendaryChests
        }

        if (count <= 0) return false

        val editor = prefs.edit()
        val updatedData = when (rarity) {
            MysteryChestRarity.BRONZE -> {
                val n = count - 1
                editor.putInt("chests_bronze", n)
                current.copy(unopenedBronzeChests = n)
            }
            MysteryChestRarity.SILVER -> {
                val n = count - 1
                editor.putInt("chests_silver", n)
                current.copy(unopenedSilverChests = n)
            }
            MysteryChestRarity.GOLD -> {
                val n = count - 1
                editor.putInt("chests_gold", n)
                current.copy(unopenedGoldChests = n)
            }
            MysteryChestRarity.DIAMOND -> {
                val n = count - 1
                editor.putInt("chests_diamond", n)
                current.copy(unopenedDiamondChests = n)
            }
            MysteryChestRarity.LEGENDARY -> {
                val n = count - 1
                editor.putInt("chests_legendary", n)
                current.copy(unopenedLegendaryChests = n)
            }
        }

        editor.apply()
        _saveData.value = updatedData
        return true
    }

    fun addChestCount(rarity: MysteryChestRarity, amount: Int = 1) {
        val current = _saveData.value
        val editor = prefs.edit()
        val updatedData = when (rarity) {
            MysteryChestRarity.BRONZE -> {
                val n = current.unopenedBronzeChests + amount
                editor.putInt("chests_bronze", n)
                current.copy(unopenedBronzeChests = n)
            }
            MysteryChestRarity.SILVER -> {
                val n = current.unopenedSilverChests + amount
                editor.putInt("chests_silver", n)
                current.copy(unopenedSilverChests = n)
            }
            MysteryChestRarity.GOLD -> {
                val n = current.unopenedGoldChests + amount
                editor.putInt("chests_gold", n)
                current.copy(unopenedGoldChests = n)
            }
            MysteryChestRarity.DIAMOND -> {
                val n = current.unopenedDiamondChests + amount
                editor.putInt("chests_diamond", n)
                current.copy(unopenedDiamondChests = n)
            }
            MysteryChestRarity.LEGENDARY -> {
                val n = current.unopenedLegendaryChests + amount
                editor.putInt("chests_legendary", n)
                current.copy(unopenedLegendaryChests = n)
            }
        }
        editor.apply()
        _saveData.value = updatedData
    }

    fun claimDailyMission(missionId: String, rewardCoins: Int, rewardXp: Long): Boolean {
        val current = _saveData.value
        if (current.claimedDailyMissionIds.contains(missionId)) return false

        val newSet = current.claimedDailyMissionIds + missionId
        val newCoins = current.coins + rewardCoins
        val newXp = current.playerXp + rewardXp
        val newLevel = maxOf(current.playerLevel, PlayerProgressionManager.calculateLevelFromXp(newXp))

        prefs.edit()
            .putStringSet("claimed_daily_missions", newSet)
            .putInt(BlockMasterConstants.KEY_COINS, newCoins)
            .putLong(BlockMasterConstants.KEY_PLAYER_XP, newXp)
            .putInt(BlockMasterConstants.KEY_LEVEL, newLevel)
            .apply()

        _saveData.value = current.copy(
            claimedDailyMissionIds = newSet,
            coins = newCoins,
            playerXp = newXp,
            playerLevel = newLevel
        )
        return true
    }

    fun claimWeeklyMission(missionId: String, rewardCoins: Int, rewardXp: Long): Boolean {
        val current = _saveData.value
        if (current.claimedWeeklyMissionIds.contains(missionId)) return false

        val newSet = current.claimedWeeklyMissionIds + missionId
        val newCoins = current.coins + rewardCoins
        val newXp = current.playerXp + rewardXp
        val newLevel = maxOf(current.playerLevel, PlayerProgressionManager.calculateLevelFromXp(newXp))

        prefs.edit()
            .putStringSet("claimed_weekly_missions", newSet)
            .putInt(BlockMasterConstants.KEY_COINS, newCoins)
            .putLong(BlockMasterConstants.KEY_PLAYER_XP, newXp)
            .putInt(BlockMasterConstants.KEY_LEVEL, newLevel)
            .apply()

        _saveData.value = current.copy(
            claimedWeeklyMissionIds = newSet,
            coins = newCoins,
            playerXp = newXp,
            playerLevel = newLevel
        )
        return true
    }

    fun consumePowerUp(type: PowerUpType): Boolean {
        val current = _saveData.value
        val count = when (type) {
            PowerUpType.CLEAR_ROW -> current.powerUpClearRow
            PowerUpType.CLEAR_COLUMN -> current.powerUpClearCol
            PowerUpType.DESTROY_BLOCK -> current.powerUpDestroyBlock
            PowerUpType.FREEZE_TIME -> current.powerUpFreezeTime
            PowerUpType.SCORE_BOOSTER -> current.powerUpScoreBooster
            PowerUpType.COIN_BOOSTER -> current.powerUpCoinBooster
        }

        if (count <= 0) return false

        val newCount = count - 1
        val newPuUsed = current.totalPowerUpsUsed + 1
        val editor = prefs.edit().putInt("stat_pu_used", newPuUsed)

        val updatedData = when (type) {
            PowerUpType.CLEAR_ROW -> {
                editor.putInt("pu_clear_row", newCount)
                current.copy(powerUpClearRow = newCount, totalPowerUpsUsed = newPuUsed)
            }
            PowerUpType.CLEAR_COLUMN -> {
                editor.putInt("pu_clear_col", newCount)
                current.copy(powerUpClearCol = newCount, totalPowerUpsUsed = newPuUsed)
            }
            PowerUpType.DESTROY_BLOCK -> {
                editor.putInt("pu_destroy_block", newCount)
                current.copy(powerUpDestroyBlock = newCount, totalPowerUpsUsed = newPuUsed)
            }
            PowerUpType.FREEZE_TIME -> {
                editor.putInt("pu_freeze_time", newCount)
                current.copy(powerUpFreezeTime = newCount, totalPowerUpsUsed = newPuUsed)
            }
            PowerUpType.SCORE_BOOSTER -> {
                editor.putInt("pu_score_booster", newCount)
                current.copy(powerUpScoreBooster = newCount, totalPowerUpsUsed = newPuUsed)
            }
            PowerUpType.COIN_BOOSTER -> {
                editor.putInt("pu_coin_booster", newCount)
                current.copy(powerUpCoinBooster = newCount, totalPowerUpsUsed = newPuUsed)
            }
        }

        editor.apply()
        _saveData.value = updatedData
        return true
    }

    fun addPowerUpCount(type: PowerUpType, amount: Int) {
        val current = _saveData.value
        val editor = prefs.edit()
        val updatedData = when (type) {
            PowerUpType.CLEAR_ROW -> {
                val newCount = current.powerUpClearRow + amount
                editor.putInt("pu_clear_row", newCount)
                current.copy(powerUpClearRow = newCount)
            }
            PowerUpType.CLEAR_COLUMN -> {
                val newCount = current.powerUpClearCol + amount
                editor.putInt("pu_clear_col", newCount)
                current.copy(powerUpClearCol = newCount)
            }
            PowerUpType.DESTROY_BLOCK -> {
                val newCount = current.powerUpDestroyBlock + amount
                editor.putInt("pu_destroy_block", newCount)
                current.copy(powerUpDestroyBlock = newCount)
            }
            PowerUpType.FREEZE_TIME -> {
                val newCount = current.powerUpFreezeTime + amount
                editor.putInt("pu_freeze_time", newCount)
                current.copy(powerUpFreezeTime = newCount)
            }
            PowerUpType.SCORE_BOOSTER -> {
                val newCount = current.powerUpScoreBooster + amount
                editor.putInt("pu_score_booster", newCount)
                current.copy(powerUpScoreBooster = newCount)
            }
            PowerUpType.COIN_BOOSTER -> {
                val newCount = current.powerUpCoinBooster + amount
                editor.putInt("pu_coin_booster", newCount)
                current.copy(powerUpCoinBooster = newCount)
            }
        }
        editor.apply()
        _saveData.value = updatedData
    }

    fun recordSpecialBlockStats(
        bombs: Int = 0,
        ice: Int = 0,
        steel: Int = 0,
        lightning: Int = 0,
        chainMax: Int = 0,
        perfectClears: Int = 0
    ) {
        val current = _saveData.value
        val newBombs = current.totalBombsExploded + bombs
        val newIce = current.totalIceShattered + ice
        val newSteel = current.totalSteelDestroyed + steel
        val newLightning = current.totalLightningTriggers + lightning
        val newChainMax = maxOf(current.totalHighestChainReaction, chainMax)
        val newPerfectClears = current.totalPerfectClears + perfectClears

        prefs.edit()
            .putInt("stat_bombs", newBombs)
            .putInt("stat_ice", newIce)
            .putInt("stat_steel", newSteel)
            .putInt("stat_lightning", newLightning)
            .putInt("stat_chain_max", newChainMax)
            .putInt("stat_perfect_clears", newPerfectClears)
            .apply()

        _saveData.value = current.copy(
            totalBombsExploded = newBombs,
            totalIceShattered = newIce,
            totalSteelDestroyed = newSteel,
            totalLightningTriggers = newLightning,
            totalHighestChainReaction = newChainMax,
            totalPerfectClears = newPerfectClears
        )
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(BlockMasterConstants.KEY_SOUND_ENABLED, enabled).apply()
        _saveData.value = _saveData.value.copy(soundEnabled = enabled)
    }

    fun setMusicEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(BlockMasterConstants.KEY_MUSIC_ENABLED, enabled).apply()
        _saveData.value = _saveData.value.copy(musicEnabled = enabled)
    }

    fun spendCoins(amount: Int): Boolean {
        val current = _saveData.value
        if (current.coins < amount) return false
        val newCoins = current.coins - amount
        prefs.edit().putInt(BlockMasterConstants.KEY_COINS, newCoins).apply()
        _saveData.value = current.copy(coins = newCoins)
        return true
    }

    fun updateVolumes(musicVol: Float, sfxVol: Float) {
        prefs.edit()
            .putFloat(BlockMasterConstants.KEY_MUSIC_VOLUME, musicVol)
            .putFloat(BlockMasterConstants.KEY_SFX_VOLUME, sfxVol)
            .apply()
        _saveData.value = _saveData.value.copy(musicVolume = musicVol, sfxVolume = sfxVol)
    }

    fun updatePlayerName(name: String) {
        prefs.edit().putString(BlockMasterConstants.KEY_PLAYER_NAME, name).apply()
        _saveData.value = _saveData.value.copy(playerName = name)
    }

    fun addXpAndCoins(earnedXp: Long, earnedCoins: Int) {
        val current = _saveData.value
        val newXp = current.playerXp + earnedXp
        val newCoins = current.coins + earnedCoins
        val newLevel = maxOf(current.playerLevel, PlayerProgressionManager.calculateLevelFromXp(newXp))
        val newUnlockedCount = WorldProgressionManager.getUnlockedWorlds(newLevel).size

        prefs.edit()
            .putLong(BlockMasterConstants.KEY_PLAYER_XP, newXp)
            .putInt(BlockMasterConstants.KEY_COINS, newCoins)
            .putInt(BlockMasterConstants.KEY_LEVEL, newLevel)
            .putInt(BlockMasterConstants.KEY_UNLOCKED_WORLDS, newUnlockedCount)
            .apply()

        _saveData.value = current.copy(
            playerXp = newXp,
            coins = newCoins,
            playerLevel = newLevel,
            unlockedWorldsCount = newUnlockedCount
        )
    }

    fun updateInfiniteLevel(newLevel: Int) {
        val current = _saveData.value
        val highestLvl = maxOf(current.highestLevelReached, newLevel)
        val pLevel = maxOf(current.playerLevel, newLevel)
        val unlockedCount = WorldProgressionManager.getUnlockedWorlds(pLevel).size

        prefs.edit()
            .putInt(BlockMasterConstants.KEY_CURRENT_INFINITE_LEVEL, newLevel)
            .putInt(BlockMasterConstants.KEY_HIGHEST_LEVEL, highestLvl)
            .putInt(BlockMasterConstants.KEY_LEVEL, pLevel)
            .putInt(BlockMasterConstants.KEY_UNLOCKED_WORLDS, unlockedCount)
            .apply()

        _saveData.value = current.copy(
            currentInfiniteLevel = newLevel,
            highestLevelReached = highestLvl,
            playerLevel = pLevel,
            unlockedWorldsCount = unlockedCount
        )
    }

    fun saveGameProgress(
        score: Int,
        addedCoins: Int,
        linesCleared: Int,
        durationSec: Long,
        reachedLevel: Int = 1,
        earnedXp: Long = 0L,
        maxCombo: Int = 0,
        isWin: Boolean = false
    ) {
        val current = _saveData.value
        val newHighScore = maxOf(current.highScore, score)
        val newCoins = current.coins + addedCoins
        val newGames = current.totalGamesPlayed + 1
        val newWon = if (isWin) current.totalGamesWon + 1 else current.totalGamesWon
        val newLost = if (!isWin) current.totalGamesLost + 1 else current.totalGamesLost
        val newLines = current.totalLinesCleared + linesCleared
        val newTime = current.timePlayedSeconds + durationSec
        val newXp = current.playerXp + earnedXp
        val newInfLevel = maxOf(current.currentInfiniteLevel, reachedLevel)
        val newHighestLvl = maxOf(current.highestLevelReached, newInfLevel)
        val newCombo = maxOf(current.highestComboAllTime, maxCombo)
        val newPlayerLevel = maxOf(newInfLevel, PlayerProgressionManager.calculateLevelFromXp(newXp))
        val unlockedCount = WorldProgressionManager.getUnlockedWorlds(newPlayerLevel).size

        prefs.edit()
            .putInt(BlockMasterConstants.KEY_HIGH_SCORE, newHighScore)
            .putInt(BlockMasterConstants.KEY_COINS, newCoins)
            .putInt(BlockMasterConstants.KEY_LEVEL, newPlayerLevel)
            .putInt(BlockMasterConstants.KEY_TOTAL_GAMES, newGames)
            .putInt("stat_games_won", newWon)
            .putInt("stat_games_lost", newLost)
            .putInt(BlockMasterConstants.KEY_LINES_CLEARED, newLines)
            .putLong(BlockMasterConstants.KEY_TIME_PLAYED_SEC, newTime)
            .putLong(BlockMasterConstants.KEY_PLAYER_XP, newXp)
            .putInt(BlockMasterConstants.KEY_CURRENT_INFINITE_LEVEL, newInfLevel)
            .putInt(BlockMasterConstants.KEY_HIGHEST_LEVEL, newHighestLvl)
            .putInt(BlockMasterConstants.KEY_HIGHEST_COMBO, newCombo)
            .putInt(BlockMasterConstants.KEY_UNLOCKED_WORLDS, unlockedCount)
            .apply()

        _saveData.value = current.copy(
            highScore = newHighScore,
            coins = newCoins,
            playerLevel = newPlayerLevel,
            totalGamesPlayed = newGames,
            totalGamesWon = newWon,
            totalGamesLost = newLost,
            totalLinesCleared = newLines,
            timePlayedSeconds = newTime,
            playerXp = newXp,
            currentInfiniteLevel = newInfLevel,
            highestLevelReached = newHighestLvl,
            highestComboAllTime = newCombo,
            unlockedWorldsCount = unlockedCount,
            lifetimeScore = current.lifetimeScore + score,
            lifetimeCoinsEarned = current.lifetimeCoinsEarned + maxOf(0, addedCoins),
            seasonXp = current.seasonXp + maxOf(0, (score / 10).toLong()),
            lastActiveTimestamp = System.currentTimeMillis()
        )

        prefs.edit()
            .putLong("stat_lifetime_score", _saveData.value.lifetimeScore)
            .putLong("stat_lifetime_coins", _saveData.value.lifetimeCoinsEarned)
            .putLong("season_xp", _saveData.value.seasonXp)
            .putLong("last_active_ts", _saveData.value.lastActiveTimestamp)
            .apply()
    }

    fun addSeasonXp(xp: Long) {
        val current = _saveData.value
        val newXp = current.seasonXp + xp
        prefs.edit().putLong("season_xp", newXp).apply()
        _saveData.value = current.copy(seasonXp = newXp)
    }

    fun claimSeasonFreeReward(levelKey: String): Boolean {
        val current = _saveData.value
        if (current.claimedSeasonFreeRewards.contains(levelKey)) return false
        val newSet = current.claimedSeasonFreeRewards + levelKey
        prefs.edit().putStringSet("claimed_season_free_rewards", newSet).apply()
        _saveData.value = current.copy(claimedSeasonFreeRewards = newSet)
        return true
    }

    fun claimSeasonPremiumReward(levelKey: String): Boolean {
        val current = _saveData.value
        if (current.claimedSeasonPremiumRewards.contains(levelKey)) return false
        val newSet = current.claimedSeasonPremiumRewards + levelKey
        prefs.edit().putStringSet("claimed_season_premium_rewards", newSet).apply()
        _saveData.value = current.copy(claimedSeasonPremiumRewards = newSet)
        return true
    }

    fun unlockCosmetic(cosmeticId: String) {
        val current = _saveData.value
        val newSet = current.unlockedCosmeticIds + cosmeticId
        prefs.edit().putStringSet("unlocked_cosmetics", newSet).apply()
        _saveData.value = current.copy(unlockedCosmeticIds = newSet)
    }

    fun equipCosmetic(categoryName: String, cosmeticId: String) {
        val current = _saveData.value
        val editor = prefs.edit()
        val updated = when (categoryName) {
            "BLOCK_SKIN" -> {
                editor.putString("equipped_block_skin", cosmeticId)
                current.copy(equippedBlockSkin = cosmeticId)
            }
            "GRID_THEME" -> {
                editor.putString("equipped_grid_theme", cosmeticId)
                current.copy(equippedGridTheme = cosmeticId)
            }
            "BOARD_FRAME" -> {
                editor.putString("equipped_board_frame", cosmeticId)
                current.copy(equippedBoardFrame = cosmeticId)
            }
            "BACKGROUND" -> {
                editor.putString("equipped_background", cosmeticId)
                current.copy(equippedBackground = cosmeticId)
            }
            "TITLE" -> {
                editor.putString("equipped_title", cosmeticId)
                current.copy(equippedTitle = cosmeticId)
            }
            else -> current
        }
        editor.apply()
        _saveData.value = updated
    }

    fun recordLuckySpinTimestamp() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong("last_lucky_spin_ts", now).apply()
        _saveData.value = _saveData.value.copy(lastLuckySpinTimestamp = now)
    }
}
