package com.myplaywin.app.blockmaster.liveops

import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val isUnlocked: Boolean,
    val isClaimed: Boolean,
    val rewardCoins: Int,
    val rewardXp: Long
) {
    val progressFraction: Float
        get() = if (targetAmount <= 0L) 1f else (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)
}

object AchievementEngine {

    val DEFINITIONS = listOf(
        AchievementDefinition(
            id = "ach_first_match",
            title = "First Steps",
            description = "Play your first Block Master match",
            iconEmoji = "🎮",
            targetAmount = 1L,
            rewardCoins = 100,
            rewardXp = 200L,
            statExtractor = { it.totalGamesPlayed.toLong() }
        ),
        AchievementDefinition(
            id = "ach_100_matches",
            title = "Centurion Stacker",
            description = "Play 100 matches",
            iconEmoji = "🕹️",
            targetAmount = 100L,
            rewardCoins = 1000,
            rewardXp = 2500L,
            statExtractor = { it.totalGamesPlayed.toLong() }
        ),
        AchievementDefinition(
            id = "ach_1000_matches",
            title = "Master Stacker",
            description = "Play 1,000 matches",
            iconEmoji = "🏆",
            targetAmount = 1000L,
            rewardCoins = 10000,
            rewardXp = 25000L,
            statExtractor = { it.totalGamesPlayed.toLong() }
        ),
        AchievementDefinition(
            id = "ach_10_lines",
            title = "Line Clearer",
            description = "Clear 10 total lines",
            iconEmoji = "🧱",
            targetAmount = 10L,
            rewardCoins = 150,
            rewardXp = 300L,
            statExtractor = { it.totalLinesCleared.toLong() }
        ),
        AchievementDefinition(
            id = "ach_10000_lines",
            title = "Line Legend",
            description = "Clear 10,000 total lines",
            iconEmoji = "⚡",
            targetAmount = 10000L,
            rewardCoins = 15000,
            rewardXp = 30000L,
            statExtractor = { it.totalLinesCleared.toLong() }
        ),
        AchievementDefinition(
            id = "ach_first_combo",
            title = "First Surge",
            description = "Achieve a combo multiplier of x2 or higher",
            iconEmoji = "🔥",
            targetAmount = 2L,
            rewardCoins = 200,
            rewardXp = 400L,
            statExtractor = { it.highestComboAllTime.toLong() }
        ),
        AchievementDefinition(
            id = "ach_100_combos",
            title = "Combo Master",
            description = "Reach an all-time combo of x10",
            iconEmoji = "💥",
            targetAmount = 10L,
            rewardCoins = 2500,
            rewardXp = 5000L,
            statExtractor = { it.highestComboAllTime.toLong() }
        ),
        AchievementDefinition(
            id = "ach_level_100",
            title = "Century Club",
            description = "Reach Level 100 in Infinite Mode",
            iconEmoji = "👑",
            targetAmount = 100L,
            rewardCoins = 5000,
            rewardXp = 10000L,
            statExtractor = { it.playerLevel.toLong() }
        ),
        AchievementDefinition(
            id = "ach_level_1000",
            title = "Infinite Deity",
            description = "Reach Level 1,000 in Infinite Mode",
            iconEmoji = "♾️",
            targetAmount = 1000L,
            rewardCoins = 50000,
            rewardXp = 100000L,
            statExtractor = { it.playerLevel.toLong() }
        ),
        AchievementDefinition(
            id = "ach_all_worlds",
            title = "Cosmic Explorer",
            description = "Unlock all 10 World Biomes",
            iconEmoji = "🌌",
            targetAmount = 10L,
            rewardCoins = 20000,
            rewardXp = 50000L,
            statExtractor = { it.unlockedWorldsCount.toLong() }
        ),
        AchievementDefinition(
            id = "ach_500_bombs",
            title = "Demolition Expert",
            description = "Explode 500 Bomb Blocks",
            iconEmoji = "💣",
            targetAmount = 500L,
            rewardCoins = 5000,
            rewardXp = 12000L,
            statExtractor = { it.totalBombsExploded.toLong() }
        ),
        AchievementDefinition(
            id = "ach_1m_coins",
            title = "Coin Tycoon",
            description = "Accumulate 1,000,000 lifetime coins",
            iconEmoji = "🪙",
            targetAmount = 1000000L,
            rewardCoins = 50000,
            rewardXp = 100000L,
            statExtractor = { it.coins.toLong() }
        ),
        AchievementDefinition(
            id = "ach_100_powerups",
            title = "Tactical Master",
            description = "Use 100 total Power-Ups",
            iconEmoji = "🧪",
            targetAmount = 100L,
            rewardCoins = 3000,
            rewardXp = 7000L,
            statExtractor = { it.totalPowerUpsUsed.toLong() }
        )
    )

    fun evaluateAchievements(
        saveData: BlockMasterSaveData,
        claimedSet: Set<String>
    ): List<AchievementItem> {
        return DEFINITIONS.map { def ->
            val currentVal = def.statExtractor(saveData)
            val isUnlocked = currentVal >= def.targetAmount
            val isClaimed = claimedSet.contains(def.id)
            AchievementItem(
                id = def.id,
                title = def.title,
                description = def.description,
                iconEmoji = def.iconEmoji,
                targetAmount = def.targetAmount,
                currentAmount = currentVal.coerceAtMost(def.targetAmount),
                isUnlocked = isUnlocked,
                isClaimed = isClaimed,
                rewardCoins = def.rewardCoins,
                rewardXp = def.rewardXp
            )
        }
    }
}

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val targetAmount: Long,
    val rewardCoins: Int,
    val rewardXp: Long,
    val statExtractor: (BlockMasterSaveData) -> Long
)
