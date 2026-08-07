package com.myplaywin.app.blockmaster.cosmetics

enum class CosmeticCategory(val displayName: String, val iconEmoji: String) {
    BLOCK_SKIN("Block Skins", "🧩"),
    GRID_THEME("Grid Themes", "🔲"),
    BOARD_FRAME("Board Frames", "🖼️"),
    BACKGROUND("Backgrounds", "🌌"),
    PARTICLE_EFFECT("Particle Effects", "✨"),
    TITLE("Titles", "🏷️"),
    BADGE("Badges", "🎖️")
}

data class CosmeticItem(
    val id: String,
    val name: String,
    val category: CosmeticCategory,
    val rarity: String, // "COMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC"
    val rarityColorHex: Long,
    val iconEmoji: String,
    val costCoins: Int,
    val isUnlockedByDefault: Boolean = false,
    val description: String
)

object CosmeticRegistry {

    val allCosmetics = listOf(
        // Block Skins
        CosmeticItem("skin_classic", "Classic Retro", CosmeticCategory.BLOCK_SKIN, "COMMON", 0xFF00E5FF, "🧩", 0, true, "Original retro block style."),
        CosmeticItem("skin_cyber", "Cyber Neon", CosmeticCategory.BLOCK_SKIN, "RARE", 0xFF00FFCC, "⚡", 500, false, "Glowing futuristic neon pulses."),
        CosmeticItem("skin_jungle", "Jungle Moss", CosmeticCategory.BLOCK_SKIN, "RARE", 0xFF4CAF50, "🌿", 750, false, "Carved ancient stone blocks."),
        CosmeticItem("skin_ice", "Glacial Frost", CosmeticCategory.BLOCK_SKIN, "EPIC", 0xFF80DEEA, "❄️", 1200, false, "Sub-zero crystalline ice blocks."),
        CosmeticItem("skin_volcano", "Magma Core", CosmeticCategory.BLOCK_SKIN, "EPIC", 0xFFFF5722, "🌋", 1500, false, "Glowing molten lava blocks."),
        CosmeticItem("skin_cosmic", "Starlight Void", CosmeticCategory.BLOCK_SKIN, "LEGENDARY", 0xFF9C27B0, "🚀", 2500, false, "Deep space galaxy blocks."),
        CosmeticItem("skin_crystal", "Prismatic Gem", CosmeticCategory.BLOCK_SKIN, "MYTHIC", 0xFFFFD700, "💎", 5000, false, "Ultraviolet diamond gemstone blocks."),

        // Grid Themes
        CosmeticItem("theme_classic", "Midnight Purple", CosmeticCategory.GRID_THEME, "COMMON", 0xFFA855F7, "🔲", 0, true, "Standard dark purple grid board."),
        CosmeticItem("theme_cyber", "Cyber Synth", CosmeticCategory.GRID_THEME, "RARE", 0xFF00E5FF, "💻", 600, false, "Neon grid lines with dark blue backdrop."),
        CosmeticItem("theme_emerald", "Emerald Sanctuary", CosmeticCategory.GRID_THEME, "EPIC", 0xFF00E676, "🟢", 1400, false, "Lush emerald glowing border grid."),
        CosmeticItem("theme_gold", "Royal Gold", CosmeticCategory.GRID_THEME, "LEGENDARY", 0xFFFFD700, "👑", 3000, false, "Golden luxury board frame."),

        // Board Frames
        CosmeticItem("frame_default", "Clean Minimal", CosmeticCategory.BOARD_FRAME, "COMMON", 0xFF888888, "🖼️", 0, true, "Sleek minimal frame."),
        CosmeticItem("frame_neon_border", "Neon Glow Frame", CosmeticCategory.BOARD_FRAME, "RARE", 0xFFE040FB, "🌟", 800, false, "Pulsing neon perimeter glow."),
        CosmeticItem("frame_dragon", "Dragon Scale Frame", CosmeticCategory.BOARD_FRAME, "LEGENDARY", 0xFFFF3D00, "🐉", 3500, false, "Draconic red mythic frame."),

        // Backgrounds
        CosmeticItem("bg_default", "Deep Night Canvas", CosmeticCategory.BACKGROUND, "COMMON", 0xFF120E24, "🌌", 0, true, "Dark ambient starry space."),
        CosmeticItem("bg_cyberpunk", "Neon Metropolis", CosmeticCategory.BACKGROUND, "EPIC", 0xFF2A004E, "🌆", 1800, false, "Futuristic skyline gradient."),
        CosmeticItem("bg_aurora", "Northern Lights", CosmeticCategory.BACKGROUND, "LEGENDARY", 0xFF004D40, "🌌", 3200, false, "Wavy atmospheric polar lights."),

        // Particle Effects
        CosmeticItem("particle_sparks", "Golden Sparks", CosmeticCategory.PARTICLE_EFFECT, "COMMON", 0xFFFFD700, "✨", 0, true, "Golden star burst on line clear."),
        CosmeticItem("particle_plasma", "Plasma Burst", CosmeticCategory.PARTICLE_EFFECT, "EPIC", 0xFF00E5FF, "💥", 1500, false, "Electric shockwave on Tetris clears."),
        CosmeticItem("particle_fireworks", "Grand Fireworks", CosmeticCategory.PARTICLE_EFFECT, "LEGENDARY", 0xFFFF007F, "🎆", 4000, false, "Multi-color festive firework explosions."),

        // Titles
        CosmeticItem("title_rookie", "Puzzle Rookie", CosmeticCategory.TITLE, "COMMON", 0xFFAAAAAA, "🏷️", 0, true, "Beginning block builder."),
        CosmeticItem("title_line_slayer", "Line Slayer ⚔️", CosmeticCategory.TITLE, "RARE", 0xFF00E5FF, "⚔️", 500, false, "Cleared 500 total lines."),
        CosmeticItem("title_combo_king", "Combo King 👑", CosmeticCategory.TITLE, "EPIC", 0xFFFFD700, "👑", 1500, false, "Achieved 10x combo streak."),
        CosmeticItem("title_block_god", "BLOCK GOD ⚡", CosmeticCategory.TITLE, "MYTHIC", 0xFFE040FB, "⚡", 10000, false, "Supreme master of Block Master.")
    )

    fun getCosmeticById(id: String): CosmeticItem? {
        return allCosmetics.firstOrNull { it.id == id }
    }

    fun getCosmeticsByCategory(category: CosmeticCategory): List<CosmeticItem> {
        return allCosmetics.filter { it.category == category }
    }

    fun calculateCompletionPercentage(unlockedIds: Set<String>): Int {
        if (allCosmetics.isEmpty()) return 100
        val unlockedCount = allCosmetics.count { it.isUnlockedByDefault || unlockedIds.contains(it.id) }
        return ((unlockedCount.toFloat() / allCosmetics.size) * 100).toInt().coerceIn(0, 100)
    }
}
