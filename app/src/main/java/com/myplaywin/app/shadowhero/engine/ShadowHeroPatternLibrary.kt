package com.myplaywin.app.shadowhero.engine

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import java.util.ArrayDeque
import java.util.Random
import kotlin.math.abs

enum class PlayerAbility {
    JUMP,
    DOUBLE_JUMP,
    WALL_JUMP,
    DASH
}

enum class PatternCategory {
    BASIC_RUN,
    SHORT_JUMP,
    LONG_JUMP,
    DOUBLE_JUMP,
    VERTICAL_CLIMB,
    WALL_JUMP,
    DASH_GAP,
    MOVING_PLATFORM,
    FALL_SECTION,
    RECOVERY_SECTION,
    COMBAT_ARENA,
    ENEMY_AMBUSH,
    HAZARD_SECTION,
    LASER_SECTION,
    SPIKE_SECTION,
    REWARD_SECTION,
    SECRET_ROUTE,
    MULTI_PATH,
    CHECKPOINT_SECTION,
    MIXED_CHALLENGE
}

data class PatternConnector(
    val heightOffset: Float = 0f,
    val requiredAbility: PlayerAbility? = null,
    val width: Float = 160f
)

data class PatternPlatformDef(
    val relX: Float,
    val relY: Float,
    val width: Float,
    val height: Float = 400f,
    val isWall: Boolean = false,
    val behaviorType: PlatformBehaviorType = PlatformBehaviorType.NORMAL
)

data class PatternHazardDef(
    val relX: Float,
    val relY: Float,
    val hazardType: String, // "SPIKE", "MOVING_SPIKE", "BLADE", "LASER"
    val width: Float = 60f,
    val height: Float = 24f,
    val isMoving: Boolean = false,
    val endRelX: Float = 0f,
    val endRelY: Float = 0f,
    val speed: Float = 120f
)

data class PatternEnemyDef(
    val relX: Float,
    val relY: Float,
    val enemyType: EnemyType,
    val patrolMinRelX: Float = 0f,
    val patrolMaxRelX: Float = 0f
)

data class PatternCollectibleDef(
    val relX: Float,
    val relY: Float,
    val isBonusRoute: Boolean = false
)

data class PatternPowerUpDef(
    val relX: Float,
    val relY: Float,
    val powerUpType: PowerUpType? = null
)

data class VariationProfile(
    val platformWidthMult: Float = 1.0f,
    val gapSizeMult: Float = 1.0f,
    val verticalOffsetDelta: Float = 0f,
    val isMirrored: Boolean = false,
    val enemyCountDelta: Int = 0,
    val crystalCountDelta: Int = 0,
    val hazardCountDelta: Int = 0,
    val decorationSeed: Long = 0L
)

data class PatternDefinition(
    val id: Int,
    val name: String,
    val category: PatternCategory,
    val difficultyScore: Int, // 1 (very easy) to 7 (extreme)
    val entryConnector: PatternConnector,
    val exitConnector: PatternConnector,
    val platformDefs: List<PatternPlatformDef>,
    val hazardDefs: List<PatternHazardDef>,
    val enemyDefs: List<PatternEnemyDef>,
    val collectibleDefs: List<PatternCollectibleDef>,
    val powerUpDefs: List<PatternPowerUpDef>,
    val requiredAbilities: Set<PlayerAbility>,
    val compatibleBiomes: Set<LevelTheme>,
    val baseWidth: Float,
    val baseHeight: Float,
    val variationProfile: VariationProfile = VariationProfile(),
    val isMirrorable: Boolean = true
)

/**
 * Procedural Pattern Registry
 * Capable of serving 100,000+ lightweight pattern configurations on demand.
 */
object ShadowHeroPatternRegistry {

    private const val MAX_PATTERN_CONFIGURATIONS = 120_000
    private val archetypeList = mutableListOf<PatternDefinition>()

    init {
        initializeBaseArchetypes()
    }

    private fun initializeBaseArchetypes() {
        var archetypeId = 1

        // 1. BASIC RUN ARCHETYPES (15 archetypes)
        for (i in 0 until 15) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Basic Run $i",
                    category = PatternCategory.BASIC_RUN,
                    difficultyScore = 1,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(0f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 320f + i * 15f),
                        PatternPlatformDef(380f + i * 20f, 0f, 300f + i * 10f)
                    ),
                    hazardDefs = emptyList(),
                    enemyDefs = if (i % 3 == 0) listOf(PatternEnemyDef(450f, -38f, EnemyType.SHADOW_WALKER, 400f, 600f)) else emptyList(),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(280f, -50f),
                        PatternCollectibleDef(320f, -70f),
                        PatternCollectibleDef(360f, -50f)
                    ),
                    powerUpDefs = emptyList(),
                    requiredAbilities = setOf(PlayerAbility.JUMP),
                    compatibleBiomes = emptySet(),
                    baseWidth = 700f + i * 30f,
                    baseHeight = 400f
                )
            )
        }

        // 2. SHORT JUMP ARCHETYPES (15 archetypes)
        for (i in 0 until 15) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Short Jump $i",
                    category = PatternCategory.SHORT_JUMP,
                    difficultyScore = 2,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(if (i % 2 == 0) -40f else 30f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 220f),
                        PatternPlatformDef(340f + i * 10f, if (i % 2 == 0) -40f else 30f, 250f)
                    ),
                    hazardDefs = if (i % 2 == 1) listOf(PatternHazardDef(230f, 120f, "SPIKE", width = 100f)) else emptyList(),
                    enemyDefs = listOf(PatternEnemyDef(400f, -38f, EnemyType.FLYING_ORB, 350f, 550f)),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(250f, -60f),
                        PatternCollectibleDef(290f, -80f),
                        PatternCollectibleDef(330f, -60f)
                    ),
                    powerUpDefs = emptyList(),
                    requiredAbilities = setOf(PlayerAbility.JUMP),
                    compatibleBiomes = emptySet(),
                    baseWidth = 600f + i * 15f,
                    baseHeight = 400f
                )
            )
        }

        // 3. LONG JUMP ARCHETYPES (12 archetypes)
        for (i in 0 until 12) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Long Jump $i",
                    category = PatternCategory.LONG_JUMP,
                    difficultyScore = 3,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(-20f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 200f),
                        PatternPlatformDef(420f + i * 12f, -20f, 220f)
                    ),
                    hazardDefs = listOf(PatternHazardDef(210f, 150f, "SPIKE", width = 200f)),
                    enemyDefs = emptyList(),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(260f, -60f),
                        PatternCollectibleDef(310f, -90f),
                        PatternCollectibleDef(360f, -60f)
                    ),
                    powerUpDefs = if (i % 4 == 0) listOf(PatternPowerUpDef(310f, -120f)) else emptyList(),
                    requiredAbilities = setOf(PlayerAbility.JUMP, PlayerAbility.DOUBLE_JUMP),
                    compatibleBiomes = emptySet(),
                    baseWidth = 650f + i * 15f,
                    baseHeight = 400f
                )
            )
        }

        // 4. DOUBLE JUMP ARCHETYPES (12 archetypes)
        for (i in 0 until 12) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Double Jump $i",
                    category = PatternCategory.DOUBLE_JUMP,
                    difficultyScore = 3,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(-80f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 180f),
                        PatternPlatformDef(240f, -50f, 120f),
                        PatternPlatformDef(460f + i * 10f, -80f, 200f)
                    ),
                    hazardDefs = emptyList(),
                    enemyDefs = listOf(PatternEnemyDef(500f, -118f, EnemyType.FLYING_ORB, 460f, 620f)),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(240f, -90f),
                        PatternCollectibleDef(350f, -120f),
                        PatternCollectibleDef(460f, -120f)
                    ),
                    powerUpDefs = emptyList(),
                    requiredAbilities = setOf(PlayerAbility.JUMP, PlayerAbility.DOUBLE_JUMP),
                    compatibleBiomes = emptySet(),
                    baseWidth = 680f + i * 10f,
                    baseHeight = 450f
                )
            )
        }

        // 5. VERTICAL CLIMB ARCHETYPES (10 archetypes)
        for (i in 0 until 10) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Vertical Climb $i",
                    category = PatternCategory.VERTICAL_CLIMB,
                    difficultyScore = 4,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(-260f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 160f),
                        PatternPlatformDef(180f, -90f, 130f),
                        PatternPlatformDef(330f, -180f, 130f),
                        PatternPlatformDef(480f, -260f, 220f)
                    ),
                    hazardDefs = emptyList(),
                    enemyDefs = listOf(PatternEnemyDef(350f, -218f, EnemyType.TURRET, 330f, 440f)),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(240f, -130f),
                        PatternCollectibleDef(390f, -220f)
                    ),
                    powerUpDefs = emptyList(),
                    requiredAbilities = setOf(PlayerAbility.JUMP, PlayerAbility.DOUBLE_JUMP),
                    compatibleBiomes = setOf(LevelTheme.NEON_CAVES, LevelTheme.FROZEN_TEMPLE, LevelTheme.SHADOW_CASTLE),
                    baseWidth = 720f,
                    baseHeight = 600f
                )
            )
        }

        // 6. WALL JUMP ARCHETYPES (10 archetypes)
        for (i in 0 until 10) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Wall Jump Shaft $i",
                    category = PatternCategory.WALL_JUMP,
                    difficultyScore = 4,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(-300f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 150f),
                        PatternPlatformDef(180f, -300f, 30f, 400f, isWall = true),
                        PatternPlatformDef(360f, -300f, 30f, 400f, isWall = true),
                        PatternPlatformDef(390f, -300f, 220f)
                    ),
                    hazardDefs = listOf(PatternHazardDef(210f, 80f, "SPIKE", width = 140f)),
                    enemyDefs = emptyList(),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(270f, -100f),
                        PatternCollectibleDef(270f, -200f)
                    ),
                    powerUpDefs = emptyList(),
                    requiredAbilities = setOf(PlayerAbility.JUMP, PlayerAbility.WALL_JUMP),
                    compatibleBiomes = setOf(LevelTheme.FROZEN_TEMPLE, LevelTheme.LAVA_CORE, LevelTheme.SKY_RUINS, LevelTheme.SHADOW_CASTLE),
                    baseWidth = 630f,
                    baseHeight = 650f
                )
            )
        }

        // 7. DASH GAP ARCHETYPES (10 archetypes)
        for (i in 0 until 10) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Dash Gap $i",
                    category = PatternCategory.DASH_GAP,
                    difficultyScore = 4,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(0f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 200f),
                        PatternPlatformDef(540f + i * 15f, 0f, 220f)
                    ),
                    hazardDefs = listOf(PatternHazardDef(210f, 150f, "SPIKE", width = 320f)),
                    enemyDefs = emptyList(),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(280f, -40f),
                        PatternCollectibleDef(360f, -40f),
                        PatternCollectibleDef(440f, -40f)
                    ),
                    powerUpDefs = if (i % 3 == 0) listOf(PatternPowerUpDef(360f, -80f, PowerUpType.DASH_BOOST)) else emptyList(),
                    requiredAbilities = setOf(PlayerAbility.JUMP, PlayerAbility.DASH),
                    compatibleBiomes = setOf(LevelTheme.CYBER_FACTORY, LevelTheme.LAVA_CORE, LevelTheme.SKY_RUINS),
                    baseWidth = 780f + i * 15f,
                    baseHeight = 400f
                )
            )
        }

        // 8. MOVING PLATFORM ARCHETYPES (10 archetypes)
        for (i in 0 until 10) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Moving Platform $i",
                    category = PatternCategory.MOVING_PLATFORM,
                    difficultyScore = 3,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(0f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 180f),
                        PatternPlatformDef(220f, 0f, 130f, 36f, behaviorType = PlatformBehaviorType.MOVING),
                        PatternPlatformDef(550f, 0f, 200f)
                    ),
                    hazardDefs = emptyList(),
                    enemyDefs = listOf(PatternEnemyDef(600f, -38f, EnemyType.FLYING_ORB, 550f, 720f)),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(280f, -60f),
                        PatternCollectibleDef(400f, -60f)
                    ),
                    powerUpDefs = emptyList(),
                    requiredAbilities = setOf(PlayerAbility.JUMP),
                    compatibleBiomes = setOf(LevelTheme.CYBER_FACTORY, LevelTheme.LAVA_CORE, LevelTheme.NEON_CAVES),
                    baseWidth = 760f,
                    baseHeight = 400f
                )
            )
        }

        // 9. RECOVERY SECTION ARCHETYPES (8 archetypes)
        for (i in 0 until 8) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Recovery $i",
                    category = PatternCategory.RECOVERY_SECTION,
                    difficultyScore = 1,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(0f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 450f + i * 20f)
                    ),
                    hazardDefs = emptyList(),
                    enemyDefs = emptyList(),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(120f, -30f),
                        PatternCollectibleDef(200f, -30f),
                        PatternCollectibleDef(280f, -30f)
                    ),
                    powerUpDefs = listOf(PatternPowerUpDef(220f, -50f)),
                    requiredAbilities = setOf(PlayerAbility.JUMP),
                    compatibleBiomes = emptySet(),
                    baseWidth = 480f + i * 20f,
                    baseHeight = 400f
                )
            )
        }

        // 10. COMBAT ARENA ARCHETYPES (10 archetypes)
        for (i in 0 until 10) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Combat Arena $i",
                    category = PatternCategory.COMBAT_ARENA,
                    difficultyScore = 4,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(0f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 580f)
                    ),
                    hazardDefs = emptyList(),
                    enemyDefs = listOf(
                        PatternEnemyDef(180f, -38f, EnemyType.SHADOW_WALKER, 100f, 260f),
                        PatternEnemyDef(400f, -38f, if (i % 2 == 0) EnemyType.CHASER else EnemyType.TURRET, 320f, 520f)
                    ),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(150f, -40f),
                        PatternCollectibleDef(420f, -40f)
                    ),
                    powerUpDefs = listOf(PatternPowerUpDef(290f, -60f, PowerUpType.SHIELD)),
                    requiredAbilities = setOf(PlayerAbility.JUMP),
                    compatibleBiomes = setOf(LevelTheme.CYBER_FACTORY, LevelTheme.LAVA_CORE, LevelTheme.SHADOW_CASTLE),
                    baseWidth = 600f,
                    baseHeight = 400f
                )
            )
        }

        // 11. HAZARD / LASER / SPIKE ARCHETYPES (15 archetypes)
        for (i in 0 until 15) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Hazard Corridor $i",
                    category = if (i % 2 == 0) PatternCategory.HAZARD_SECTION else PatternCategory.LASER_SECTION,
                    difficultyScore = 5,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(0f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 540f)
                    ),
                    hazardDefs = if (i % 2 == 0) listOf(
                        PatternHazardDef(180f, -24f, "MOVING_SPIKE", isMoving = true, endRelX = 360f, endRelY = -24f, speed = 140f)
                    ) else listOf(
                        PatternHazardDef(270f, -120f, "LASER", endRelX = 270f, endRelY = 0f)
                    ),
                    enemyDefs = emptyList(),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(120f, -30f),
                        PatternCollectibleDef(220f, -30f),
                        PatternCollectibleDef(320f, -30f),
                        PatternCollectibleDef(420f, -30f)
                    ),
                    powerUpDefs = emptyList(),
                    requiredAbilities = setOf(PlayerAbility.JUMP, PlayerAbility.DASH),
                    compatibleBiomes = setOf(LevelTheme.CYBER_FACTORY, LevelTheme.LAVA_CORE, LevelTheme.VOID_DIMENSION),
                    baseWidth = 560f,
                    baseHeight = 400f
                )
            )
        }

        // 12. REWARD & SECRET ROUTE ARCHETYPES (10 archetypes)
        for (i in 0 until 10) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Secret Reward $i",
                    category = if (i % 2 == 0) PatternCategory.REWARD_SECTION else PatternCategory.SECRET_ROUTE,
                    difficultyScore = 2,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(0f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 480f),
                        PatternPlatformDef(150f, -180f, 200f, 28f)
                    ),
                    hazardDefs = emptyList(),
                    enemyDefs = emptyList(),
                    collectibleDefs = listOf(
                        PatternCollectibleDef(170f, -220f, isBonusRoute = true),
                        PatternCollectibleDef(210f, -220f, isBonusRoute = true),
                        PatternCollectibleDef(250f, -220f, isBonusRoute = true),
                        PatternCollectibleDef(290f, -220f, isBonusRoute = true)
                    ),
                    powerUpDefs = listOf(PatternPowerUpDef(230f, -260f, PowerUpType.DOUBLE_CRYSTAL)),
                    requiredAbilities = setOf(PlayerAbility.JUMP, PlayerAbility.DOUBLE_JUMP),
                    compatibleBiomes = emptySet(),
                    baseWidth = 500f,
                    baseHeight = 500f
                )
            )
        }

        // 13. CHECKPOINT ARCHETYPES (5 archetypes)
        for (i in 0 until 5) {
            archetypeList.add(
                PatternDefinition(
                    id = archetypeId++,
                    name = "Checkpoint $i",
                    category = PatternCategory.CHECKPOINT_SECTION,
                    difficultyScore = 1,
                    entryConnector = PatternConnector(0f),
                    exitConnector = PatternConnector(0f),
                    platformDefs = listOf(
                        PatternPlatformDef(0f, 0f, 380f)
                    ),
                    hazardDefs = emptyList(),
                    enemyDefs = emptyList(),
                    collectibleDefs = emptyList(),
                    powerUpDefs = emptyList(),
                    requiredAbilities = setOf(PlayerAbility.JUMP),
                    compatibleBiomes = emptySet(),
                    baseWidth = 400f,
                    baseHeight = 400f
                )
            )
        }
    }

    /**
     * Resolves pattern details for any configuration ID up to 100,000+.
     */
    fun getPattern(patternId: Int): PatternDefinition {
        val safeId = ((abs(patternId) - 1) % MAX_PATTERN_CONFIGURATIONS) + 1
        val archetypeIndex = (safeId - 1) % archetypeList.size
        val archetype = archetypeList[archetypeIndex]

        val varKey = safeId / archetypeList.size
        val widthMult = 0.85f + (varKey % 5) * 0.08f
        val gapMult = 0.85f + ((varKey / 5) % 5) * 0.08f
        val vertDelta = (((varKey / 25) % 5) - 2) * 20f
        val isMirrored = (varKey / 125) % 2 == 1 && archetype.isMirrorable
        val enemyDelta = ((varKey / 250) % 3) - 1
        val crystalDelta = ((varKey / 750) % 3)

        val profile = VariationProfile(
            platformWidthMult = widthMult,
            gapSizeMult = gapMult,
            verticalOffsetDelta = vertDelta,
            isMirrored = isMirrored,
            enemyCountDelta = enemyDelta,
            crystalCountDelta = crystalDelta,
            decorationSeed = safeId.toLong() * 10007L
        )

        // Apply parameter variations dynamically
        val modPlatforms = archetype.platformDefs.map { p ->
            val w = (p.width * profile.platformWidthMult).coerceAtLeast(80f)
            val rx = if (profile.isMirrored) (archetype.baseWidth - p.relX - w) else p.relX
            p.copy(relX = rx, relY = p.relY + profile.verticalOffsetDelta, width = w)
        }

        val modCollectibles = archetype.collectibleDefs.map { c ->
            val rx = if (profile.isMirrored) (archetype.baseWidth - c.relX) else c.relX
            c.copy(relX = rx, relY = c.relY + profile.verticalOffsetDelta)
        }

        val modEnemies = archetype.enemyDefs.map { e ->
            val rx = if (profile.isMirrored) (archetype.baseWidth - e.relX) else e.relX
            e.copy(relX = rx, relY = e.relY + profile.verticalOffsetDelta)
        }

        return archetype.copy(
            id = safeId,
            platformDefs = modPlatforms,
            collectibleDefs = modCollectibles,
            enemyDefs = modEnemies,
            variationProfile = profile
        )
    }

    fun getTotalPatternConfigurations(): Int = MAX_PATTERN_CONFIGURATIONS
    fun getArchetypeCount(): Int = archetypeList.size
}

/**
 * Reachability Validator
 * Verifies that a generated section is physically traversable using player's current abilities.
 */
object ShadowHeroReachabilityValidator {

    fun validatePatternReachability(
        pattern: PatternDefinition,
        unlockedAbilities: Set<PlayerAbility>
    ): Boolean {
        val maxHorizontalSpan = when {
            PlayerAbility.DASH in unlockedAbilities -> 540f
            PlayerAbility.DOUBLE_JUMP in unlockedAbilities -> 420f
            else -> 280f
        }

        val maxJumpHeight = if (PlayerAbility.DOUBLE_JUMP in unlockedAbilities) 210f else 130f

        val sortedPlats = pattern.platformDefs.sortedBy { it.relX }
        for (i in 0 until sortedPlats.size - 1) {
            val p1 = sortedPlats[i]
            val p2 = sortedPlats[i + 1]
            val gapX = p2.relX - (p1.relX + p1.width)
            val dy = p2.relY - p1.relY

            if (gapX > maxHorizontalSpan) return false
            if (-dy > maxJumpHeight && !p1.isWall && !p2.isWall) return false
        }
        return true
    }
}

/**
 * Smart Pattern Selector with long-range anti-repeat memory.
 */
class ShadowHeroSmartPatternSelector {

    private val recentPatternIds = ArrayDeque<Int>()
    private val recentCategories = ArrayDeque<PatternCategory>()
    private val recentCategoryPairs = ArrayDeque<Pair<PatternCategory, PatternCategory>>()

    private val PATTERN_HISTORY_CAPACITY = 50
    private val CATEGORY_HISTORY_CAPACITY = 10
    private val PAIR_HISTORY_CAPACITY = 20

    fun selectSmartPattern(
        biome: LevelTheme,
        targetDifficulty: Int,
        unlockedAbilities: Set<PlayerAbility>,
        previousCategory: PatternCategory?,
        random: Random
    ): PatternDefinition {

        val totalConfigs = ShadowHeroPatternRegistry.getTotalPatternConfigurations()
        val numArchetypes = ShadowHeroPatternRegistry.getArchetypeCount()

        var candidate: PatternDefinition? = null
        var attempts = 0

        while (candidate == null && attempts < 200) {
            attempts++
            val candidateId = random.nextInt(totalConfigs) + 1
            val pat = ShadowHeroPatternRegistry.getPattern(candidateId)

            // 1. Ability Requirement Check
            if (!unlockedAbilities.containsAll(pat.requiredAbilities)) continue

            // 2. Difficulty Score Filter (targetDifficulty ± 1)
            if (abs(pat.difficultyScore - targetDifficulty) > 2) continue

            // 3. Biome Filter
            if (pat.compatibleBiomes.isNotEmpty() && !pat.compatibleBiomes.contains(biome)) continue

            // 4. History Anti-Repeat Check (Req 9, 10, 11)
            if (recentPatternIds.contains(pat.id)) continue
            val lastTwoCategories = recentCategories.toList().takeLast(2)
            if (lastTwoCategories.size >= 2 && lastTwoCategories.all { c -> c == pat.category }) continue
            if (previousCategory != null && recentCategoryPairs.contains(Pair(previousCategory, pat.category))) continue

            // 5. Reachability Check (Req 22)
            if (!ShadowHeroReachabilityValidator.validatePatternReachability(pat, unlockedAbilities)) continue

            candidate = pat
        }

        // Fallback if strict restrictions filtered all candidates
        if (candidate == null) {
            val fallbackId = random.nextInt(numArchetypes) + 1
            candidate = ShadowHeroPatternRegistry.getPattern(fallbackId)
        }

        // Record history
        recordSelection(candidate.id, candidate.category, previousCategory)

        return candidate
    }

    private fun recordSelection(id: Int, category: PatternCategory, prevCategory: PatternCategory?) {
        recentPatternIds.addLast(id)
        if (recentPatternIds.size > PATTERN_HISTORY_CAPACITY) recentPatternIds.removeFirst()

        recentCategories.addLast(category)
        if (recentCategories.size > CATEGORY_HISTORY_CAPACITY) recentCategories.removeFirst()

        if (prevCategory != null) {
            recentCategoryPairs.addLast(Pair(prevCategory, category))
            if (recentCategoryPairs.size > PAIR_HISTORY_CAPACITY) recentCategoryPairs.removeFirst()
        }
    }

    fun getRecentHistorySummary(): String {
        return "Recent Pattern IDs: [${recentPatternIds.toList().takeLast(5).joinToString(",")}] | Recent Categories: [${recentCategories.toList().takeLast(4).joinToString(",")}]"
    }
}
