package com.myplaywin.app.shadowhero.engine

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import java.util.Random

/**
 * Intelligent Biome System (Phase 11B)
 * Manages continuous biome progression, non-linear biome selection,
 * transition chunks between biomes, and biome section preferences.
 */
data class ChunkBiomeInfo(
    val primaryTheme: LevelTheme,
    val nextTheme: LevelTheme? = null,
    val transitionProgress: Float = 0f, // 0.0f = full primaryTheme, 1.0f = full nextTheme
    val isTransitionChunk: Boolean = false
) {
    /**
     * Interpolated primary background color for smooth continuous transitions.
     */
    val effectivePrimaryBgColor: Color
        get() = if (nextTheme != null && isTransitionChunk) {
            lerp(primaryTheme.primaryBgColor, nextTheme.primaryBgColor, transitionProgress)
        } else {
            primaryTheme.primaryBgColor
        }

    /**
     * Interpolated secondary background color.
     */
    val effectiveSecondaryBgColor: Color
        get() = if (nextTheme != null && isTransitionChunk) {
            lerp(primaryTheme.secondaryBgColor, nextTheme.secondaryBgColor, transitionProgress)
        } else {
            primaryTheme.secondaryBgColor
        }

    /**
     * Interpolated platform color.
     */
    val effectivePlatformColor: Color
        get() = if (nextTheme != null && isTransitionChunk) {
            lerp(primaryTheme.platformColor, nextTheme.platformColor, transitionProgress)
        } else {
            primaryTheme.platformColor
        }

    /**
     * Interpolated platform border color.
     */
    val effectivePlatformBorderColor: Color
        get() = if (nextTheme != null && isTransitionChunk) {
            lerp(primaryTheme.platformBorderColor, nextTheme.platformBorderColor, transitionProgress)
        } else {
            primaryTheme.platformBorderColor
        }

    /**
     * Interpolated accent glow color.
     */
    val effectiveAccentGlowColor: Color
        get() = if (nextTheme != null && isTransitionChunk) {
            lerp(primaryTheme.accentGlowColor, nextTheme.accentGlowColor, transitionProgress)
        } else {
            primaryTheme.accentGlowColor
        }
}

object ShadowHeroBiomeManager {

    /**
     * Determines non-linear, non-predictable biome sequence for a stage.
     * Avoids simple `stage % 7` cycle. Uses stage number, seed, recent history, and difficulty.
     */
    fun getBiomeSequenceForStage(stageNumber: Int, stageSeed: Long): List<LevelTheme> {
        val random = Random(stageSeed + stageNumber * 987654321L)
        val allThemes = LevelTheme.values()
        val sequence = mutableListOf<LevelTheme>()

        // Base theme derived from stage seed & pseudo-random shuffle
        val shuffledIndices = (0 until allThemes.size).toMutableList()
        // Deterministic shuffle
        for (i in shuffledIndices.indices.reversed()) {
            val j = random.nextInt(i + 1)
            val tmp = shuffledIndices[i]
            shuffledIndices[i] = shuffledIndices[j]
            shuffledIndices[j] = tmp
        }

        val primaryIndex = (stageNumber - 1 + random.nextInt(3)) % allThemes.size
        var primaryTheme = allThemes[shuffledIndices[primaryIndex]]

        // High difficulty stages prefer SHADOW_CASTLE or VOID_DIMENSION
        if (stageNumber >= 10 && random.nextFloat() < 0.4f) {
            primaryTheme = if (random.nextBoolean()) LevelTheme.VOID_DIMENSION else LevelTheme.SHADOW_CASTLE
        }

        sequence.add(primaryTheme)

        // Choose second biome for long stage progression
        val remainingThemes = allThemes.filter { it != primaryTheme }
        val secondaryTheme = remainingThemes[random.nextInt(remainingThemes.size)]
        sequence.add(secondaryTheme)

        return sequence
    }

    /**
     * Computes the ChunkBiomeInfo for a specific chunk index in a stage.
     * Every ~5 chunks can initiate a transition to a new biome.
     */
    fun getChunkBiomeInfo(
        stageNumber: Int,
        chunkIndex: Int,
        stageSeed: Long
    ): ChunkBiomeInfo {
        val sequence = getBiomeSequenceForStage(stageNumber, stageSeed)
        val primaryBiome = sequence[0]
        val secondaryBiome = sequence[1]

        val transitionChunkIndex = 4 // Chunk #4 is the transition chunk from Primary to Secondary Biome
        return when {
            chunkIndex < transitionChunkIndex -> {
                ChunkBiomeInfo(primaryTheme = primaryBiome)
            }
            chunkIndex == transitionChunkIndex -> {
                // Transition Chunk: Smooth blending from primaryBiome to secondaryBiome
                ChunkBiomeInfo(
                    primaryTheme = primaryBiome,
                    nextTheme = secondaryBiome,
                    transitionProgress = 0.5f,
                    isTransitionChunk = true
                )
            }
            else -> {
                ChunkBiomeInfo(primaryTheme = secondaryBiome)
            }
        }
    }

    /**
     * Returns section type preferences based on the active biome.
     */
    fun filterPreferredSectionTypes(
        theme: LevelTheme,
        availableTypes: List<SectionType>
    ): List<SectionType> {
        return when (theme) {
            LevelTheme.NEON_CAVES -> availableTypes.filter {
                it == SectionType.BASIC_JUMP || it == SectionType.CRYSTAL_SECTION || it == SectionType.RECOVERY_SECTION || it == SectionType.STEPPING_PLATFORMS
            }
            LevelTheme.CYBER_FACTORY -> availableTypes.filter {
                it == SectionType.TRAP_SECTION || it == SectionType.MOVING_PLATFORM || it == SectionType.DASH_CORRIDOR || it == SectionType.ENEMY_SECTION
            }
            LevelTheme.FROZEN_TEMPLE -> availableTypes.filter {
                it == SectionType.STEPPING_PLATFORMS || it == SectionType.VERTICAL_CLIMB || it == SectionType.RECOVERY_SECTION || it == SectionType.WIDE_GAP
            }
            LevelTheme.LAVA_CORE -> availableTypes.filter {
                it == SectionType.TRAP_SECTION || it == SectionType.VERTICAL_CLIMB || it == SectionType.RISK_REWARD || it == SectionType.WALL_JUMP
            }
            LevelTheme.SKY_RUINS -> availableTypes.filter {
                it == SectionType.DASH_CORRIDOR || it == SectionType.VERTICAL_CLIMB || it == SectionType.CRYSTAL_SECTION || it == SectionType.WIDE_GAP
            }
            LevelTheme.SHADOW_CASTLE -> availableTypes.filter {
                it == SectionType.TRAP_SECTION || it == SectionType.RISK_REWARD || it == SectionType.WALL_JUMP || it == SectionType.ENEMY_SECTION
            }
            LevelTheme.VOID_DIMENSION -> availableTypes.filter {
                it == SectionType.TRAP_SECTION || it == SectionType.RISK_REWARD || it == SectionType.VERTICAL_CLIMB || it == SectionType.DASH_CORRIDOR
            }
        }.ifEmpty { availableTypes }
    }
}
