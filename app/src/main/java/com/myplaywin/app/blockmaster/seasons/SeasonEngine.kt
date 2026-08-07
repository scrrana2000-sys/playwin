package com.myplaywin.app.blockmaster.seasons

import com.myplaywin.app.blockmaster.cosmetics.CosmeticRegistry
import kotlin.math.max

data class SeasonData(
    val id: Int,
    val name: String,
    val subtitle: String,
    val themeHex: Long,
    val secondaryHex: Long,
    val blockSkinId: String,
    val bgGradient: List<Long>,
    val iconEmoji: String,
    val description: String
)

data class SeasonPassLevel(
    val level: Int,
    val requiredXp: Long,
    val freeRewardCoins: Int = 0,
    val freeRewardPowerUp: String? = null,
    val freeRewardTitle: String? = null,
    val premiumRewardCoins: Int = 0,
    val premiumRewardCosmeticId: String? = null,
    val premiumRewardBadge: String? = null
)

object SeasonEngine {

    val seasonsList = listOf(
        SeasonData(
            id = 1,
            name = "Season 1: Classic Genesis",
            subtitle = "The original retro block legend",
            themeHex = 0xFF00E5FF,
            secondaryHex = 0xFFA855F7,
            blockSkinId = "skin_classic",
            bgGradient = listOf(0xFF0F0C1B, 0xFF18102B),
            iconEmoji = "🕹️",
            description = "Experience the classic origins of block puzzle mastery."
        ),
        SeasonData(
            id = 2,
            name = "Season 2: Cyber Grid",
            subtitle = "Futuristic neon light show",
            themeHex = 0xFF00FFCC,
            secondaryHex = 0xFFFF007F,
            blockSkinId = "skin_cyber",
            bgGradient = listOf(0xFF0D1B2A, 0xFF1B263B),
            iconEmoji = "⚡",
            description = "Enter the high-frequency neon cyber grid."
        ),
        SeasonData(
            id = 3,
            name = "Season 3: Jungle Temple",
            subtitle = "Ancient overgrown mystery",
            themeHex = 0xFF4CAF50,
            secondaryHex = 0xFFFF9800,
            blockSkinId = "skin_jungle",
            bgGradient = listOf(0xFF0B2B11, 0xFF143D1A),
            iconEmoji = "🌿",
            description = "Unearth hidden stone treasures in the tropical jungle."
        ),
        SeasonData(
            id = 4,
            name = "Season 4: Ice Age Frost",
            subtitle = "Glacial sub-zero challenge",
            themeHex = 0xFF80DEEA,
            secondaryHex = 0xFF00B0FF,
            blockSkinId = "skin_ice",
            bgGradient = listOf(0xFF081C24, 0xFF0F303E),
            iconEmoji = "❄️",
            description = "Shatter frozen crystal blocks under sub-zero pressure."
        ),
        SeasonData(
            id = 5,
            name = "Season 5: Volcanic Inferno",
            subtitle = "Molten lava heatwave",
            themeHex = 0xFFFF5722,
            secondaryHex = 0xFFFFD700,
            blockSkinId = "skin_volcano",
            bgGradient = listOf(0xFF2B0B0B, 0xFF3D1414),
            iconEmoji = "🌋",
            description = "Harness explosive molten power before the lava rises."
        ),
        SeasonData(
            id = 6,
            name = "Season 6: Cosmic Space",
            subtitle = "Interstellar gravity shifts",
            themeHex = 0xFF9C27B0,
            secondaryHex = 0xFFE040FB,
            blockSkinId = "skin_cosmic",
            bgGradient = listOf(0xFF120824, 0xFF1F0E3D),
            iconEmoji = "🚀",
            description = "Navigate infinite galaxy corridors beyond the stars."
        ),
        SeasonData(
            id = 7,
            name = "Season 7: Neon Future",
            subtitle = "Ultra high-tech synthesis",
            themeHex = 0xFFFF00E5,
            secondaryHex = 0xFF00E5FF,
            blockSkinId = "skin_neon",
            bgGradient = listOf(0xFF1E0629, 0xFF310B42),
            iconEmoji = "🌃",
            description = "Master the synthwave beat of tomorrow's metropolis."
        ),
        SeasonData(
            id = 8,
            name = "Season 8: Crystal Kingdom",
            subtitle = "Prismatic gem luminescence",
            themeHex = 0xFFE040FB,
            secondaryHex = 0xFFFFD700,
            blockSkinId = "skin_crystal",
            bgGradient = listOf(0xFF1D0A24, 0xFF30133A),
            iconEmoji = "💎",
            description = "Unlock the ultimate glowing gemstone crown."
        )
    )

    fun getSeasonById(id: Int): SeasonData {
        return seasonsList.firstOrNull { it.id == id } ?: seasonsList.first()
    }

    /**
     * Calculates active season based on current calendar month (1..8 dynamic loop).
     */
    fun getCurrentActiveSeason(): SeasonData {
        val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) // 0..11
        val index = month % seasonsList.size
        return seasonsList[index]
    }

    /**
     * Generates 100 Season Pass Levels dynamically for a season.
     */
    fun getSeasonPassLevels(seasonId: Int): List<SeasonPassLevel> {
        val levels = ArrayList<SeasonPassLevel>()
        for (lvl in 1..100) {
            val reqXp = lvl * 300L

            val freeCoins = when {
                lvl % 10 == 0 -> 500
                lvl % 5 == 0 -> 200
                else -> 50
            }

            val freePu = when {
                lvl % 7 == 0 -> "BOMB_BOOSTER"
                lvl % 4 == 0 -> "CLEAR_ROW"
                else -> null
            }

            val freeTitle = when (lvl) {
                10 -> "Season $seasonId Novice"
                50 -> "Season $seasonId Master"
                100 -> "Season $seasonId Grand Champion 🏆"
                else -> null
            }

            val premCoins = freeCoins * 3
            val premCosmetic = when (lvl) {
                1 -> "skin_s${seasonId}_starter"
                25 -> "frame_s${seasonId}_bronze"
                50 -> "bg_s${seasonId}_neon"
                75 -> "title_s${seasonId}_hero"
                100 -> "skin_s${seasonId}_mythic"
                else -> null
            }

            val premBadge = if (lvl == 100) "Season $seasonId Conqueror 👑" else null

            levels.add(
                SeasonPassLevel(
                    level = lvl,
                    requiredXp = reqXp,
                    freeRewardCoins = freeCoins,
                    freeRewardPowerUp = freePu,
                    freeRewardTitle = freeTitle,
                    premiumRewardCoins = premCoins,
                    premiumRewardCosmeticId = premCosmetic,
                    premiumRewardBadge = premBadge
                )
            )
        }
        return levels
    }

    fun calculatePassLevel(seasonXp: Long): Int {
        val lvl = (seasonXp / 300L).toInt() + 1
        return lvl.coerceIn(1, 100)
    }
}
