package com.myplaywin.app.blockmaster.liveops

import androidx.compose.ui.graphics.Color
import com.myplaywin.app.blockmaster.powerups.PowerUpType
import kotlin.random.Random

enum class MysteryChestRarity(
    val title: String,
    val iconEmoji: String,
    val borderHex: Color,
    val bgGradientTop: Color,
    val bgGradientBottom: Color
) {
    BRONZE(
        "Bronze Chest",
        "📦",
        Color(0xFFCD7F32),
        Color(0xFF2E1C11),
        Color(0xFF170E08)
    ),
    SILVER(
        "Silver Chest",
        "🥈",
        Color(0xFFC0C0C0),
        Color(0xFF262A33),
        Color(0xFF12151B)
    ),
    GOLD(
        "Gold Chest",
        "🥇",
        Color(0xFFFFD700),
        Color(0xFF3B2E0B),
        Color(0xFF1C1504)
    ),
    DIAMOND(
        "Diamond Chest",
        "💎",
        Color(0xFF00E5FF),
        Color(0xFF0C2B38),
        Color(0xFF06151D)
    ),
    LEGENDARY(
        "Legendary Chest",
        "👑",
        Color(0xFFE040FB),
        Color(0xFF380C3D),
        Color(0xFF1A051D)
    )
}

data class ChestRewardOutcome(
    val rarity: MysteryChestRarity,
    val coins: Int,
    val xp: Long,
    val powerUps: Map<PowerUpType, Int>,
    val specialBadge: String? = null
)

object MysteryChestEngine {

    fun openChest(rarity: MysteryChestRarity): ChestRewardOutcome {
        val random = Random(System.currentTimeMillis())

        return when (rarity) {
            MysteryChestRarity.BRONZE -> {
                val coins = random.nextInt(150, 400)
                val xp = random.nextLong(200L, 500L)
                val powerUpType = PowerUpType.values().random(random)
                ChestRewardOutcome(
                    rarity = rarity,
                    coins = coins,
                    xp = xp,
                    powerUps = mapOf(powerUpType to 1)
                )
            }
            MysteryChestRarity.SILVER -> {
                val coins = random.nextInt(400, 1000)
                val xp = random.nextLong(600L, 1200L)
                val pu1 = PowerUpType.CLEAR_ROW
                val pu2 = PowerUpType.DESTROY_BLOCK
                ChestRewardOutcome(
                    rarity = rarity,
                    coins = coins,
                    xp = xp,
                    powerUps = mapOf(pu1 to 2, pu2 to 1)
                )
            }
            MysteryChestRarity.GOLD -> {
                val coins = random.nextInt(1200, 2500)
                val xp = random.nextLong(1500L, 3500L)
                val puMap = mapOf(
                    PowerUpType.CLEAR_ROW to 2,
                    PowerUpType.FREEZE_TIME to 2,
                    PowerUpType.SCORE_BOOSTER to 2
                )
                ChestRewardOutcome(
                    rarity = rarity,
                    coins = coins,
                    xp = xp,
                    powerUps = puMap
                )
            }
            MysteryChestRarity.DIAMOND -> {
                val coins = random.nextInt(3000, 7000)
                val xp = random.nextLong(5000L, 10000L)
                val puMap = mapOf(
                    PowerUpType.CLEAR_ROW to 3,
                    PowerUpType.CLEAR_COLUMN to 3,
                    PowerUpType.DESTROY_BLOCK to 3,
                    PowerUpType.FREEZE_TIME to 3
                )
                ChestRewardOutcome(
                    rarity = rarity,
                    coins = coins,
                    xp = xp,
                    powerUps = puMap,
                    specialBadge = "💎 Diamond Stacker"
                )
            }
            MysteryChestRarity.LEGENDARY -> {
                val coins = random.nextInt(10000, 25000)
                val xp = random.nextLong(20000L, 50000L)
                val puMap = mapOf(
                    PowerUpType.CLEAR_ROW to 5,
                    PowerUpType.CLEAR_COLUMN to 5,
                    PowerUpType.DESTROY_BLOCK to 5,
                    PowerUpType.FREEZE_TIME to 5,
                    PowerUpType.SCORE_BOOSTER to 5,
                    PowerUpType.COIN_BOOSTER to 5
                )
                ChestRewardOutcome(
                    rarity = rarity,
                    coins = coins,
                    xp = xp,
                    powerUps = puMap,
                    specialBadge = "👑 Infinite Crown Champion"
                )
            }
        }
    }
}
