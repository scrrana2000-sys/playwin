package com.myplaywin.app.blockmaster.store

import com.myplaywin.app.blockmaster.cosmetics.CosmeticItem
import com.myplaywin.app.blockmaster.cosmetics.CosmeticRegistry
import java.util.Calendar
import java.util.Random

data class DailyStoreOffer(
    val id: String,
    val title: String,
    val description: String,
    val itemType: String, // "COSMETIC", "POWERUP_PACK", "CHEST"
    val cosmeticId: String? = null,
    val powerUpType: String? = null,
    val count: Int = 1,
    val originalPriceCoins: Int,
    val discountPercent: Int,
    val iconEmoji: String,
    val colorHex: Long
) {
    val finalPriceCoins: Int
        get() = (originalPriceCoins * (1.0f - discountPercent / 100.0f)).toInt().coerceAtLeast(10)
}

object DailyStoreEngine {

    /**
     * Generates a deterministic daily set of 4 offers based on year & day-of-year.
     */
    fun getDailyOffers(): List<DailyStoreOffer> {
        val cal = Calendar.getInstance()
        val daySeed = (cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)).toLong()
        val random = Random(daySeed)

        val offers = ArrayList<DailyStoreOffer>()

        // Offer 1: Discounted Cosmetic Item
        val lockCosmetics = CosmeticRegistry.allCosmetics.filter { !it.isUnlockedByDefault }
        if (lockCosmetics.isNotEmpty()) {
            val selectedCosmetic = lockCosmetics[random.nextInt(lockCosmetics.size)]
            val discount = listOf(15, 25, 30, 50)[random.nextInt(4)]
            offers.add(
                DailyStoreOffer(
                    id = "store_cosmetic_${selectedCosmetic.id}",
                    title = selectedCosmetic.name,
                    description = "Daily Deal: ${selectedCosmetic.description}",
                    itemType = "COSMETIC",
                    cosmeticId = selectedCosmetic.id,
                    originalPriceCoins = selectedCosmetic.costCoins,
                    discountPercent = discount,
                    iconEmoji = selectedCosmetic.iconEmoji,
                    colorHex = selectedCosmetic.rarityColorHex
                )
            )
        }

        // Offer 2: Power-Up Mega Pack (+5 Clear Row & Col)
        offers.add(
            DailyStoreOffer(
                id = "store_pu_clear_pack",
                title = "Row & Col Clearer Pack",
                description = "Get +5 Row Clearers & +5 Column Clearers!",
                itemType = "POWERUP_PACK",
                powerUpType = "CLEAR_ROW_COL",
                count = 5,
                originalPriceCoins = 600,
                discountPercent = 20,
                iconEmoji = "💥",
                colorHex = 0xFF00E5FF
            )
        )

        // Offer 3: Explosive Bomb Bundle (+5 Destroy Block)
        offers.add(
            DailyStoreOffer(
                id = "store_pu_bomb_pack",
                title = "Explosive Bomb Bundle",
                description = "Get +5 Bomb Destroy Block power-ups!",
                itemType = "POWERUP_PACK",
                powerUpType = "DESTROY_BLOCK",
                count = 5,
                originalPriceCoins = 750,
                discountPercent = 25,
                iconEmoji = "💣",
                colorHex = 0xFFFF5722
            )
        )

        // Offer 4: Diamond Mystery Chest Deal
        offers.add(
            DailyStoreOffer(
                id = "store_chest_diamond",
                title = "Diamond Mystery Chest",
                description = "Contains guaranteed high coins, XP, & rare boosters!",
                itemType = "CHEST",
                count = 1,
                originalPriceCoins = 1000,
                discountPercent = 40,
                iconEmoji = "💎",
                colorHex = 0xFFE040FB
            )
        )

        return offers
    }

    /**
     * Calculates time remaining until 24-hour daily store reset in formatted string.
     */
    fun getTimeUntilResetFormatted(): String {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfDay = cal.timeInMillis
        val diffMs = maxOf(0L, endOfDay - now)

        val hours = (diffMs / (1000 * 3600)) % 24
        val mins = (diffMs / (1000 * 60)) % 60
        val secs = (diffMs / 1000) % 60
        return String.format("%02dh %02dm %02ds", hours, mins, secs)
    }
}
