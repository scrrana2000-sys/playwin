package com.myplaywin.app.blockmaster.world

import androidx.compose.ui.graphics.Color

data class WorldGameplayModifier(
    val description: String,
    val speedMultiplier: Float = 1.0f,
    val comboMultiplier: Float = 1.0f,
    val lightningBonusChance: Float = 0.0f,
    val slideGraceMs: Long = 0L,
    val scoreMultiplierBonus: Float = 1.0f
)

data class BlockWorld(
    val id: Int,
    val name: String,
    val description: String,
    val minLevel: Int,
    val iconEmoji: String,
    val bgGradientTop: Color,
    val bgGradientBottom: Color,
    val accentColor: Color,
    val secondaryColor: Color,
    val particleColors: List<Color>,
    val boardBorderColor: Color,
    val gridGlowColor: Color,
    val ambientMusicTheme: String,
    val gameplayModifier: WorldGameplayModifier,
    val rewardCoins: Int,
    val rewardXp: Long,
    val rewardBadge: String
)

object WorldProgressionManager {

    val WORLDS: List<BlockWorld> = listOf(
        BlockWorld(
            id = 1,
            name = "Classic City",
            description = "Blue premium neon cityscape with soft glowing grid.",
            minLevel = 1,
            iconEmoji = "🏙️",
            bgGradientTop = Color(0xFF161226),
            bgGradientBottom = Color(0xFF0F0C1B),
            accentColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFFA855F7),
            particleColors = listOf(Color(0xFF00E5FF), Color(0xFFA855F7)),
            boardBorderColor = Color(0xFF00E5FF),
            gridGlowColor = Color(0x3300E5FF),
            ambientMusicTheme = "classic_synth",
            gameplayModifier = WorldGameplayModifier(
                description = "Standard balanced gravity and scoring."
            ),
            rewardCoins = 100,
            rewardXp = 200L,
            rewardBadge = "🏙️ City Legend"
        ),
        BlockWorld(
            id = 2,
            name = "Green Forest",
            description = "Lush woodland with animated falling leaves and nature breeze.",
            minLevel = 50,
            iconEmoji = "🌲",
            bgGradientTop = Color(0xFF0B2219),
            bgGradientBottom = Color(0xFF06140F),
            accentColor = Color(0xFF00E676),
            secondaryColor = Color(0xFF76FF03),
            particleColors = listOf(Color(0xFF00E676), Color(0xFFB2FF59), Color(0xFFFFD700)),
            boardBorderColor = Color(0xFF00E676),
            gridGlowColor = Color(0x3300E676),
            ambientMusicTheme = "forest_breeze",
            gameplayModifier = WorldGameplayModifier(
                description = "Calm atmosphere slows gravity down by 15% for precise placement.",
                speedMultiplier = 0.85f
            ),
            rewardCoins = 250,
            rewardXp = 500L,
            rewardBadge = "🌲 Forest Guardian"
        ),
        BlockWorld(
            id = 3,
            name = "Frozen Ice",
            description = "Sub-zero crystalline void with falling snow and ice glow.",
            minLevel = 100,
            iconEmoji = "❄️",
            bgGradientTop = Color(0xFF0C1E30),
            bgGradientBottom = Color(0xFF07111C),
            accentColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFF80D8FF),
            particleColors = listOf(Color(0xFF00E5FF), Color(0xFFE0F7FA), Color(0xFF80D8FF)),
            boardBorderColor = Color(0xFF00E5FF),
            gridGlowColor = Color(0x3300E5FF),
            ambientMusicTheme = "frost_echo",
            gameplayModifier = WorldGameplayModifier(
                description = "Crystalline ice grants 300ms slide grace period before locking blocks.",
                slideGraceMs = 300L
            ),
            rewardCoins = 500,
            rewardXp = 1000L,
            rewardBadge = "❄️ Frost Master"
        ),
        BlockWorld(
            id = 4,
            name = "Volcano Core",
            description = "Blazing magma chamber with flowing lava, ash and heat distortion.",
            minLevel = 200,
            iconEmoji = "🌋",
            bgGradientTop = Color(0xFF2A0C0E),
            bgGradientBottom = Color(0xFF190607),
            accentColor = Color(0xFFFF3D00),
            secondaryColor = Color(0xFFFF9100),
            particleColors = listOf(Color(0xFFFF3D00), Color(0xFFFF9100), Color(0xFFFFD600)),
            boardBorderColor = Color(0xFFFF3D00),
            gridGlowColor = Color(0x33FF3D00),
            ambientMusicTheme = "volcano_pulse",
            gameplayModifier = WorldGameplayModifier(
                description = "Volcanic heat increases drop speed by 15% but awards 20% bonus points!",
                speedMultiplier = 1.15f,
                scoreMultiplierBonus = 1.20f
            ),
            rewardCoins = 1000,
            rewardXp = 2000L,
            rewardBadge = "🌋 Magma Overlord"
        ),
        BlockWorld(
            id = 5,
            name = "Electric Lab",
            description = "High voltage circuit field surging with lightning flashes.",
            minLevel = 400,
            iconEmoji = "⚡",
            bgGradientTop = Color(0xFF242207),
            bgGradientBottom = Color(0xFF141303),
            accentColor = Color(0xFFFFEA00),
            secondaryColor = Color(0xFF00E5FF),
            particleColors = listOf(Color(0xFFFFEA00), Color(0xFF00E5FF), Color(0xFF76FF03)),
            boardBorderColor = Color(0xFFFFEA00),
            gridGlowColor = Color(0x33FFEA00),
            ambientMusicTheme = "voltage_surge",
            gameplayModifier = WorldGameplayModifier(
                description = "Surging voltage grants +15% higher spawn rate for Lightning Blocks!",
                lightningBonusChance = 0.15f
            ),
            rewardCoins = 2000,
            rewardXp = 4000L,
            rewardBadge = "⚡ Voltage Pioneer"
        ),
        BlockWorld(
            id = 6,
            name = "Cyber City",
            description = "Neon digital grid with futuristic synthwave pulses.",
            minLevel = 700,
            iconEmoji = "🌆",
            bgGradientTop = Color(0xFF280B26),
            bgGradientBottom = Color(0xFF160515),
            accentColor = Color(0xFFFF007F),
            secondaryColor = Color(0xFF00F0FF),
            particleColors = listOf(Color(0xFFFF007F), Color(0xFF00F0FF), Color(0xFFD500F9)),
            boardBorderColor = Color(0xFFFF007F),
            gridGlowColor = Color(0x33FF007F),
            ambientMusicTheme = "cyber_beat",
            gameplayModifier = WorldGameplayModifier(
                description = "Digital cyber matrix grants +50% extra score and coin combo multiplier!",
                comboMultiplier = 1.50f
            ),
            rewardCoins = 5000,
            rewardXp = 10000L,
            rewardBadge = "🌆 Cyber Architect"
        ),
        BlockWorld(
            id = 7,
            name = "Deep Space",
            description = "Cosmic nebulae and orbiting stellar light particles.",
            minLevel = 1000,
            iconEmoji = "🌌",
            bgGradientTop = Color(0xFF0F0B26),
            bgGradientBottom = Color(0xFF080516),
            accentColor = Color(0xFF7C4DFF),
            secondaryColor = Color(0xFF64FFDA),
            particleColors = listOf(Color(0xFF7C4DFF), Color(0xFF64FFDA), Color(0xFFE040FB)),
            boardBorderColor = Color(0xFF7C4DFF),
            gridGlowColor = Color(0x337C4DFF),
            ambientMusicTheme = "starlight_drift",
            gameplayModifier = WorldGameplayModifier(
                description = "Zero-gravity cosmos awards +25% bonus score and coins.",
                scoreMultiplierBonus = 1.25f
            ),
            rewardCoins = 10000,
            rewardXp = 20000L,
            rewardBadge = "🌌 Cosmic Voyager"
        ),
        BlockWorld(
            id = 8,
            name = "Crystal Prism",
            description = "Radiant geometric gem void casting prismatic beams.",
            minLevel = 1500,
            iconEmoji = "💎",
            bgGradientTop = Color(0xFF240E29),
            bgGradientBottom = Color(0xFF140717),
            accentColor = Color(0xFFE040FB),
            secondaryColor = Color(0xFFFF4081),
            particleColors = listOf(Color(0xFFE040FB), Color(0xFFFF4081), Color(0xFF00E5FF)),
            boardBorderColor = Color(0xFFE040FB),
            gridGlowColor = Color(0x33E040FB),
            ambientMusicTheme = "prism_chime",
            gameplayModifier = WorldGameplayModifier(
                description = "Prismatic crystalline reflections give +2x combo rewards!",
                comboMultiplier = 2.0f
            ),
            rewardCoins = 25000,
            rewardXp = 50000L,
            rewardBadge = "💎 Prism Sovereign"
        ),
        BlockWorld(
            id = 9,
            name = "Shadow Realm",
            description = "Mysterious dark void framed by crimson specters.",
            minLevel = 2000,
            iconEmoji = "👁️",
            bgGradientTop = Color(0xFF1A050A),
            bgGradientBottom = Color(0xFF0A0204),
            accentColor = Color(0xFFFF1744),
            secondaryColor = Color(0xFFB00020),
            particleColors = listOf(Color(0xFFFF1744), Color(0xFFD50000), Color(0xFF900C3F)),
            boardBorderColor = Color(0xFFFF1744),
            gridGlowColor = Color(0x33FF1744),
            ambientMusicTheme = "shadow_whisper",
            gameplayModifier = WorldGameplayModifier(
                description = "Shadow void surges line clear scores by 30%!",
                scoreMultiplierBonus = 1.30f
            ),
            rewardCoins = 50000,
            rewardXp = 100000L,
            rewardBadge = "👁️ Void Emperor"
        ),
        BlockWorld(
            id = 10,
            name = "Infinity Cosmos",
            description = "Eternal transcendent realm of infinite cosmic block mastery.",
            minLevel = 3000,
            iconEmoji = "♾️",
            bgGradientTop = Color(0xFF261D04),
            bgGradientBottom = Color(0xFF140F02),
            accentColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFFFFFFFF),
            particleColors = listOf(Color(0xFFFFD700), Color(0xFFFFFFFF), Color(0xFF00E5FF)),
            boardBorderColor = Color(0xFFFFD700),
            gridGlowColor = Color(0x44FFD700),
            ambientMusicTheme = "infinity_harmony",
            gameplayModifier = WorldGameplayModifier(
                description = "Ultimate cosmic harmony grants 2x score and coin multiplier!",
                scoreMultiplierBonus = 2.0f,
                comboMultiplier = 2.0f
            ),
            rewardCoins = 100000,
            rewardXp = 250000L,
            rewardBadge = "♾️ Infinity Deity"
        )
    )

    fun getWorldForLevel(level: Int): BlockWorld {
        val lvl = maxOf(1, level)
        return WORLDS.lastOrNull { lvl >= it.minLevel } ?: WORLDS.first()
    }

    fun isWorldUnlocked(worldId: Int, playerLevel: Int): Boolean {
        val world = WORLDS.find { it.id == worldId } ?: return false
        return playerLevel >= world.minLevel
    }

    fun getUnlockedWorlds(playerLevel: Int): List<BlockWorld> {
        return WORLDS.filter { playerLevel >= it.minLevel }
    }
}
