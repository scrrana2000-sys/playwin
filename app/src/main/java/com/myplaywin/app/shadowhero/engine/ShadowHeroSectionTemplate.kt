package com.myplaywin.app.shadowhero.engine

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import java.util.Random

enum class SectionType {
    BASIC_JUMP,
    WIDE_GAP,
    STEPPING_PLATFORMS,
    VERTICAL_CLIMB,
    WALL_JUMP,
    DASH_CORRIDOR,
    MOVING_PLATFORM,
    TRAP_SECTION,
    ENEMY_SECTION,
    CRYSTAL_SECTION,
    RECOVERY_SECTION,
    RISK_REWARD,
    SECRET_SECTION,
    CHECKPOINT_SECTION
}

/**
 * Parameter configuration for a section template.
 */
data class SectionTemplateParams(
    val type: SectionType,
    val baseWidth: Float = 600f,
    val baseHeight: Float = 400f,
    val platformSpacing: Float = 180f,
    val gapSize: Float = 160f,
    val enemyCount: Int = 1,
    val hazardCount: Int = 1,
    val crystalCount: Int = 4,
    val difficulty: Float = 1f,
    val optionalRouteProbability: Float = 0.3f
)

/**
 * Output of generating a single section within a chunk.
 */
data class GeneratedSection(
    val type: SectionType,
    val startX: Float,
    val endX: Float,
    val width: Float,
    val exitX: Float,
    val exitY: Float,
    val platforms: List<LevelPlatform>,
    val checkpoints: List<LevelCheckpoint>,
    val crystals: List<LevelEnergyCrystal>,
    val powerUps: List<LevelPowerUp>,
    val spikes: List<LevelSpike>,
    val movingSpikes: List<LevelMovingSpike>,
    val blades: List<LevelRotatingBlade>,
    val lasers: List<LevelLaserBeam>,
    val hazards: List<LevelEnvHazard>,
    val enemies: List<LevelEnemy>,
    val hasSecretRoom: Boolean = false,
    val hasRiskRoute: Boolean = false
)

object ShadowHeroSectionGenerator {

    fun selectBiomePowerUp(theme: LevelTheme, random: Random): PowerUpType {
        val pool = when (theme) {
            LevelTheme.NEON_CAVES -> listOf(PowerUpType.SHIELD, PowerUpType.MAGNET, PowerUpType.SHIELD)
            LevelTheme.CYBER_FACTORY -> listOf(PowerUpType.DASH_BOOST, PowerUpType.SLOW_TIME, PowerUpType.DASH_BOOST)
            LevelTheme.FROZEN_TEMPLE -> listOf(PowerUpType.SHIELD, PowerUpType.SLOW_TIME)
            LevelTheme.LAVA_CORE -> listOf(PowerUpType.DASH_BOOST, PowerUpType.SHIELD)
            LevelTheme.SKY_RUINS -> listOf(PowerUpType.MAGNET, PowerUpType.DOUBLE_CRYSTAL)
            LevelTheme.SHADOW_CASTLE -> listOf(PowerUpType.SHIELD, PowerUpType.DASH_BOOST, PowerUpType.SLOW_TIME, PowerUpType.DOUBLE_CRYSTAL)
            else -> listOf(PowerUpType.SHIELD, PowerUpType.DASH_BOOST, PowerUpType.MAGNET, PowerUpType.SLOW_TIME, PowerUpType.DOUBLE_CRYSTAL)
        }
        return pool[random.nextInt(pool.size)]
    }

    fun generateSectionFromPattern(
        pattern: PatternDefinition,
        startX: Float,
        startY: Float,
        theme: LevelTheme,
        random: Random,
        chunkIndex: Int,
        sectionIndex: Int
    ): GeneratedSection {
        val platforms = mutableListOf<LevelPlatform>()
        val checkpoints = mutableListOf<LevelCheckpoint>()
        val crystals = mutableListOf<LevelEnergyCrystal>()
        val powerUps = mutableListOf<LevelPowerUp>()
        val spikes = mutableListOf<LevelSpike>()
        val movingSpikes = mutableListOf<LevelMovingSpike>()
        val blades = mutableListOf<LevelRotatingBlade>()
        val lasers = mutableListOf<LevelLaserBeam>()
        val hazards = mutableListOf<LevelEnvHazard>()
        val enemies = mutableListOf<LevelEnemy>()

        val prefix = "c${chunkIndex}_s${sectionIndex}_p${pattern.id}"
        val platColor = theme.platformColor

        // Instantiation of pattern platforms
        for ((idx, p) in pattern.platformDefs.withIndex()) {
            platforms.add(
                LevelPlatform(
                    id = "${prefix}_plat_$idx",
                    bounds = Rect(startX + p.relX, startY + p.relY, startX + p.relX + p.width, startY + p.relY + p.height),
                    isWall = p.isWall,
                    behaviorType = p.behaviorType,
                    color = platColor
                )
            )
        }

        // Instantiation of hazards
        for ((idx, h) in pattern.hazardDefs.withIndex()) {
            when (h.hazardType) {
                "SPIKE" -> spikes.add(LevelSpike("${prefix}_sp_$idx", startX + h.relX, startY + h.relY, h.width, h.height))
                "MOVING_SPIKE" -> movingSpikes.add(LevelMovingSpike("${prefix}_msp_$idx", startX + h.relX, startY + h.relY, startX + h.endRelX, startY + h.endRelY, h.speed))
                "BLADE" -> blades.add(LevelRotatingBlade("${prefix}_blade_$idx", startX + h.relX, startY + h.relY, 32f, 220f))
                "LASER" -> lasers.add(LevelLaserBeam("${prefix}_laser_$idx", startX + h.relX, startY + h.relY, startX + h.endRelX, startY + h.endRelY, isVertical = true))
            }
        }

        // Instantiation of collectibles
        for ((idx, c) in pattern.collectibleDefs.withIndex()) {
            crystals.add(LevelEnergyCrystal("${prefix}_cr_$idx", startX + c.relX, startY + c.relY, isBonusRoute = c.isBonusRoute))
        }

        // Instantiation of powerups
        for ((idx, pu) in pattern.powerUpDefs.withIndex()) {
            val type = pu.powerUpType ?: selectBiomePowerUp(theme, random)
            powerUps.add(LevelPowerUp("${prefix}_pu_$idx", startX + pu.relX, startY + pu.relY, type))
        }

        // Instantiation of enemies
        for ((idx, e) in pattern.enemyDefs.withIndex()) {
            enemies.add(
                LevelEnemy(
                    id = "${prefix}_en_$idx",
                    type = e.enemyType,
                    initialX = startX + e.relX,
                    initialY = startY + e.relY,
                    patrolMinX = startX + e.patrolMinRelX,
                    patrolMaxX = startX + e.patrolMaxRelX
                )
            )
        }

        // Checkpoint if category is CHECKPOINT_SECTION
        if (pattern.category == PatternCategory.CHECKPOINT_SECTION) {
            checkpoints.add(LevelCheckpoint("${prefix}_cp", startX + pattern.baseWidth / 2f - 18f, startY - 60f))
        }

        val mappedType = when (pattern.category) {
            PatternCategory.BASIC_RUN, PatternCategory.SHORT_JUMP -> SectionType.BASIC_JUMP
            PatternCategory.LONG_JUMP, PatternCategory.DASH_GAP -> SectionType.WIDE_GAP
            PatternCategory.VERTICAL_CLIMB -> SectionType.VERTICAL_CLIMB
            PatternCategory.WALL_JUMP -> SectionType.WALL_JUMP
            PatternCategory.MOVING_PLATFORM -> SectionType.MOVING_PLATFORM
            PatternCategory.COMBAT_ARENA, PatternCategory.ENEMY_AMBUSH -> SectionType.ENEMY_SECTION
            PatternCategory.RECOVERY_SECTION -> SectionType.RECOVERY_SECTION
            PatternCategory.REWARD_SECTION -> SectionType.CRYSTAL_SECTION
            PatternCategory.SECRET_ROUTE -> SectionType.SECRET_SECTION
            PatternCategory.CHECKPOINT_SECTION -> SectionType.CHECKPOINT_SECTION
            else -> SectionType.TRAP_SECTION
        }

        return GeneratedSection(
            type = mappedType,
            startX = startX,
            endX = startX + pattern.baseWidth,
            width = pattern.baseWidth,
            exitX = startX + pattern.baseWidth,
            exitY = startY + pattern.exitConnector.heightOffset,
            platforms = platforms,
            checkpoints = checkpoints,
            crystals = crystals,
            powerUps = powerUps,
            spikes = spikes,
            movingSpikes = movingSpikes,
            blades = blades,
            lasers = lasers,
            hazards = hazards,
            enemies = enemies,
            hasSecretRoom = pattern.category == PatternCategory.SECRET_ROUTE,
            hasRiskRoute = pattern.category == PatternCategory.MULTI_PATH
        )
    }

    fun generateSection(
        type: SectionType,
        startX: Float,
        startY: Float,
        diffParams: ChunkDifficultyParams,
        theme: LevelTheme,
        random: Random,
        chunkIndex: Int,
        sectionIndex: Int
    ): GeneratedSection {
        val platforms = mutableListOf<LevelPlatform>()
        val checkpoints = mutableListOf<LevelCheckpoint>()
        val crystals = mutableListOf<LevelEnergyCrystal>()
        val powerUps = mutableListOf<LevelPowerUp>()
        val spikes = mutableListOf<LevelSpike>()
        val movingSpikes = mutableListOf<LevelMovingSpike>()
        val blades = mutableListOf<LevelRotatingBlade>()
        val lasers = mutableListOf<LevelLaserBeam>()
        val hazards = mutableListOf<LevelEnvHazard>()
        val enemies = mutableListOf<LevelEnemy>()

        var currentX = startX
        var currentY = startY

        val gapMult = diffParams.gapWidthMultiplier
        val platMult = diffParams.platformWidthMultiplier
        val hazardMult = diffParams.hazardDensityMultiplier
        val enemyMult = diffParams.enemyDensityMultiplier

        val platColor = theme.platformColor
        val prefix = "c${chunkIndex}_s${sectionIndex}"

        var endX = currentX
        var exitY = currentY
        var isSecret = false
        var isRisk = false

        when (type) {
            SectionType.BASIC_JUMP -> {
                val platWidth = (280f * platMult).coerceAtLeast(140f)
                val gap = (140f * gapMult).coerceIn(100f, 260f)

                platforms.add(LevelPlatform("${prefix}_p1", Rect(currentX, currentY, currentX + platWidth, currentY + 400f), color = platColor))
                currentX += platWidth + gap

                val platWidth2 = (260f * platMult).coerceAtLeast(140f)
                platforms.add(LevelPlatform("${prefix}_p2", Rect(currentX, currentY, currentX + platWidth2, currentY + 400f), color = platColor))

                // Enemy on second platform
                if (random.nextFloat() < 0.65f) {
                    enemies.add(
                        LevelEnemy(
                            id = "${prefix}_bj_enemy",
                            type = if (random.nextBoolean()) EnemyType.SHADOW_WALKER else EnemyType.FLYING_ORB,
                            initialX = currentX + platWidth2 / 2f,
                            initialY = currentY - 38f,
                            patrolMinX = currentX + 20f,
                            patrolMaxX = currentX + platWidth2 - 30f
                        )
                    )
                }

                // Crystals along jump arc
                val midX = currentX - gap / 2f
                val midY = currentY - 80f
                crystals.add(LevelEnergyCrystal("${prefix}_cr1", midX - 30f, midY))
                crystals.add(LevelEnergyCrystal("${prefix}_cr2", midX, midY - 20f))
                crystals.add(LevelEnergyCrystal("${prefix}_cr3", midX + 30f, midY))

                endX = currentX + platWidth2
                exitY = currentY
            }

            SectionType.WIDE_GAP -> {
                val p1Width = (220f * platMult).coerceAtLeast(120f)
                val gap = (220f * gapMult).coerceIn(180f, 320f)

                platforms.add(LevelPlatform("${prefix}_p1", Rect(currentX, currentY, currentX + p1Width, currentY + 400f), color = platColor))
                currentX += p1Width + gap

                val p2Width = (240f * platMult).coerceAtLeast(120f)
                platforms.add(LevelPlatform("${prefix}_p2", Rect(currentX, currentY, currentX + p2Width, currentY + 400f), color = platColor))

                // Spikes at gap bottom if applicable
                if (random.nextFloat() < 0.6f * hazardMult) {
                    val gapStartX = currentX - gap
                    spikes.add(LevelSpike("${prefix}_sp1", gapStartX + 20f, currentY + 160f, width = gap - 40f, height = 24f))
                }

                // Crystals along dash jump route
                val midX = currentX - gap / 2f
                crystals.add(LevelEnergyCrystal("${prefix}_cr1", midX - 40f, currentY - 50f))
                crystals.add(LevelEnergyCrystal("${prefix}_cr2", midX, currentY - 70f))
                crystals.add(LevelEnergyCrystal("${prefix}_cr3", midX + 40f, currentY - 50f))

                endX = currentX + p2Width
                exitY = currentY
            }

            SectionType.STEPPING_PLATFORMS -> {
                val count = (3 + (diffParams.effectiveDifficulty * 0.3f).toInt()).coerceIn(3, 6)
                var x = currentX
                var y = currentY

                for (i in 0 until count) {
                    val w = (120f * platMult).coerceAtLeast(80f)
                    val dy = if (i % 2 == 0) -40f else 40f
                    y = (y + dy).coerceIn(startY - 120f, startY + 60f)

                    val behavior = if (theme == LevelTheme.FROZEN_TEMPLE && i > 0 && random.nextFloat() < 0.4f) {
                        PlatformBehaviorType.FALLING
                    } else PlatformBehaviorType.NORMAL

                    platforms.add(
                        LevelPlatform(
                            id = "${prefix}_step_$i",
                            bounds = Rect(x, y, x + w, y + 36f),
                            behaviorType = behavior,
                            color = platColor
                        )
                    )

                    crystals.add(LevelEnergyCrystal("${prefix}_step_cr_$i", x + w / 2f, y - 40f))

                    x += w + (130f * gapMult).coerceIn(90f, 220f)
                }

                endX = x
                exitY = y
            }

            SectionType.VERTICAL_CLIMB -> {
                val shaftWidth = 320f
                platforms.add(LevelPlatform("${prefix}_vleft", Rect(currentX, currentY - 300f, currentX + 30f, currentY + 100f), isWall = true, color = platColor))
                platforms.add(LevelPlatform("${prefix}_vright", Rect(currentX + shaftWidth - 30f, currentY - 300f, currentX + shaftWidth, currentY + 100f), isWall = true, color = platColor))

                // Zig-zag platforms inside
                val stepY = currentY - 80f
                platforms.add(LevelPlatform("${prefix}_vp1", Rect(currentX + 30f, stepY, currentX + 160f, stepY + 30f), color = platColor))
                platforms.add(LevelPlatform("${prefix}_vp2", Rect(currentX + 140f, stepY - 90f, currentX + 270f, stepY - 60f), color = platColor))

                // Exit platform on top right
                val topX = currentX + shaftWidth
                platforms.add(LevelPlatform("${prefix}_vtop", Rect(topX, currentY - 300f, topX + 220f, currentY - 300f + 400f), color = platColor))

                endX = topX + 220f
                exitY = currentY - 300f
            }

            SectionType.WALL_JUMP -> {
                val shaftWidth = 180f
                val height = 360f

                platforms.add(LevelPlatform("${prefix}_wj_left", Rect(currentX, currentY - height, currentX + 30f, currentY + 100f), isWall = true, color = platColor))
                platforms.add(LevelPlatform("${prefix}_wj_right", Rect(currentX + shaftWidth - 30f, currentY - height, currentX + shaftWidth, currentY + 100f), isWall = true, color = platColor))

                val exitPlatX = currentX + shaftWidth
                platforms.add(LevelPlatform("${prefix}_wj_exit", Rect(exitPlatX, currentY - height, exitPlatX + 250f, currentY - height + 400f), color = platColor))

                // Spikes on wall base
                spikes.add(LevelSpike("${prefix}_wj_sp", currentX + 30f, currentY + 80f, width = shaftWidth - 60f, height = 20f))

                crystals.add(LevelEnergyCrystal("${prefix}_wj_cr1", currentX + shaftWidth / 2f, currentY - 100f))
                crystals.add(LevelEnergyCrystal("${prefix}_wj_cr2", currentX + shaftWidth / 2f, currentY - 220f))

                endX = exitPlatX + 250f
                exitY = currentY - height
            }

            SectionType.DASH_CORRIDOR -> {
                val width = 550f
                // Low ceiling corridor requiring dash / fast run
                platforms.add(LevelPlatform("${prefix}_dash_floor", Rect(currentX, currentY, currentX + width, currentY + 400f), color = platColor))
                platforms.add(LevelPlatform("${prefix}_dash_roof", Rect(currentX + 80f, currentY - 140f, currentX + width - 60f, currentY - 90f), color = platColor))

                // Laser hazard in middle if cyber factory or high difficulty
                if ((theme == LevelTheme.CYBER_FACTORY || random.nextFloat() < 0.5f * hazardMult)) {
                    lasers.add(
                        LevelLaserBeam(
                            id = "${prefix}_dash_laser",
                            startX = currentX + width / 2f,
                            startY = currentY - 90f,
                            endX = currentX + width / 2f,
                            endY = currentY,
                            isVertical = true
                        )
                    )
                }

                // Energy crystal line
                for (i in 0..4) {
                    crystals.add(LevelEnergyCrystal("${prefix}_d_cr_$i", currentX + 100f + i * 80f, currentY - 30f))
                }

                endX = currentX + width
                exitY = currentY
            }

            SectionType.MOVING_PLATFORM -> {
                val p1Width = 180f
                platforms.add(LevelPlatform("${prefix}_mp1", Rect(currentX, currentY, currentX + p1Width, currentY + 400f), color = platColor))
                currentX += p1Width

                val moveDist = (260f * gapMult).coerceIn(180f, 340f)
                platforms.add(
                    LevelPlatform(
                        id = "${prefix}_m_plat",
                        bounds = Rect(currentX + 20f, currentY, currentX + 140f, currentY + 36f),
                        behaviorType = PlatformBehaviorType.MOVING,
                        color = platColor
                    )
                )

                currentX += 20f + moveDist + 140f
                val p2Width = 200f
                platforms.add(LevelPlatform("${prefix}_mp2", Rect(currentX, currentY, currentX + p2Width, currentY + 400f), color = platColor))

                endX = currentX + p2Width
                exitY = currentY
            }

            SectionType.TRAP_SECTION -> {
                val width = 500f
                platforms.add(LevelPlatform("${prefix}_trap_base", Rect(currentX, currentY, currentX + width, currentY + 400f), color = platColor))

                // Rotating blade or moving spike trap
                if (theme == LevelTheme.LAVA_CORE || random.nextBoolean()) {
                    movingSpikes.add(
                        LevelMovingSpike(
                            id = "${prefix}_trap_msp",
                            startX = currentX + 100f,
                            startY = currentY - 24f,
                            endX = currentX + 380f,
                            endY = currentY - 24f,
                            speed = 130f * hazardMult
                        )
                    )
                } else {
                    blades.add(
                        LevelRotatingBlade(
                            id = "${prefix}_trap_blade",
                            centerX = currentX + width / 2f,
                            centerY = currentY - 70f,
                            radius = 32f,
                            rotationSpeed = 220f * hazardMult
                        )
                    )
                }

                endX = currentX + width
                exitY = currentY
            }

            SectionType.ENEMY_SECTION -> {
                val width = 550f
                platforms.add(LevelPlatform("${prefix}_en_base", Rect(currentX, currentY, currentX + width, currentY + 400f), color = platColor))

                val eType = selectEnemyForStage(stage = 5, isFlying = random.nextBoolean(), random = random)
                val eDims1 = getEnemyDimensions(eType)
                val enemy1 = LevelEnemy(
                    id = "${prefix}_enemy_1",
                    type = eType,
                    initialX = currentX + 220f,
                    initialY = currentY - eDims1.second - (if (eType.name.contains("FLY") || eType == EnemyType.DARK_BAT || eType == EnemyType.SKULL_HAWK || eType == EnemyType.FLOATING_MAGE || eType == EnemyType.SHADOW_DRONE) 80f else 0f),
                    patrolMinX = currentX + 80f,
                    patrolMaxX = currentX + 320f
                )
                enemy1.initStats(5)
                enemies.add(enemy1)

                if (random.nextFloat() < 0.7f) {
                    val eType2 = selectEnemyForStage(stage = 5, isFlying = !eType.name.contains("FLY"), random = random)
                    val eDims2 = getEnemyDimensions(eType2)
                    val enemy2 = LevelEnemy(
                        id = "${prefix}_enemy_2",
                        type = eType2,
                        initialX = currentX + 380f,
                        initialY = currentY - eDims2.second - (if (eType2.name.contains("FLY") || eType2 == EnemyType.DARK_BAT || eType2 == EnemyType.SKULL_HAWK || eType2 == EnemyType.FLOATING_MAGE || eType2 == EnemyType.SHADOW_DRONE) 80f else 0f),
                        patrolMinX = currentX + 280f,
                        patrolMaxX = currentX + 480f
                    )
                    enemy2.initStats(5)
                    enemies.add(enemy2)
                }

                endX = currentX + width
                exitY = currentY
            }

            SectionType.CRYSTAL_SECTION -> {
                val width = 450f
                platforms.add(LevelPlatform("${prefix}_cry_base", Rect(currentX, currentY, currentX + width, currentY + 400f), color = platColor))

                // Floating crystal formation (diamond shape)
                val cx = currentX + width / 2f
                val cy = currentY - 80f
                crystals.add(LevelEnergyCrystal("${prefix}_c1", cx, cy - 40f))
                crystals.add(LevelEnergyCrystal("${prefix}_c2", cx - 35f, cy))
                crystals.add(LevelEnergyCrystal("${prefix}_c3", cx + 35f, cy))
                crystals.add(LevelEnergyCrystal("${prefix}_c4", cx, cy + 40f))

                // Small chance of powerup
                if (random.nextFloat() < 0.45f) {
                    powerUps.add(
                        LevelPowerUp(
                            id = "${prefix}_pu",
                            x = cx,
                            y = cy,
                            type = selectBiomePowerUp(theme, random)
                        )
                    )
                }

                endX = currentX + width
                exitY = currentY
            }

            SectionType.RECOVERY_SECTION -> {
                // Safe, hazard-free section with healing crystal or powerup
                val width = 400f
                platforms.add(LevelPlatform("${prefix}_rec_base", Rect(currentX, currentY, currentX + width, currentY + 400f), color = Color(0xFF1E293B)))

                powerUps.add(
                    LevelPowerUp(
                        id = "${prefix}_rec_pu",
                        x = currentX + width / 2f,
                        y = currentY - 40f,
                        type = selectBiomePowerUp(theme, random)
                    )
                )

                for (i in 0..2) {
                    crystals.add(LevelEnergyCrystal("${prefix}_rec_cr_$i", currentX + 120f + i * 80f, currentY - 30f))
                }

                endX = currentX + width
                exitY = currentY
            }

            SectionType.RISK_REWARD -> {
                isRisk = true
                val width = 600f
                // Main Safe Floor below
                platforms.add(LevelPlatform("${prefix}_safe_floor", Rect(currentX, currentY + 80f, currentX + width, currentY + 480f), color = platColor))

                // Upper Risk Route with narrow platforms & high crystal yield
                val r1Width = 100f
                val r2Width = 100f
                val riskY = currentY - 80f

                platforms.add(LevelPlatform("${prefix}_risk_p1", Rect(currentX + 120f, riskY, currentX + 120f + r1Width, riskY + 30f), color = Color(0xFFF59E0B)))
                platforms.add(LevelPlatform("${prefix}_risk_p2", Rect(currentX + 320f, riskY, currentX + 320f + r2Width, riskY + 30f), color = Color(0xFFF59E0B)))

                // Risk crystals & powerup
                for (i in 0..3) {
                    crystals.add(LevelEnergyCrystal("${prefix}_risk_cr_$i", currentX + 140f + i * 80f, riskY - 35f, isBonusRoute = true))
                }
                powerUps.add(LevelPowerUp("${prefix}_risk_shield", currentX + 370f, riskY - 40f, selectBiomePowerUp(theme, random)))

                endX = currentX + width
                exitY = currentY + 80f
            }

            SectionType.SECRET_SECTION -> {
                isSecret = true
                val width = 500f
                platforms.add(LevelPlatform("${prefix}_sec_base", Rect(currentX, currentY, currentX + width, currentY + 400f), color = platColor))

                // Secret entrance: elevated breakable / narrow platform high up
                val secX = currentX + 150f
                val secY = currentY - 220f
                platforms.add(LevelPlatform("${prefix}_sec_room", Rect(secX, secY, secX + 200f, secY + 28f), color = Color(0xFF8B5CF6)))

                // Rich secret loot
                for (i in 0..4) {
                    crystals.add(LevelEnergyCrystal("${prefix}_sec_cr_$i", secX + 20f + i * 36f, secY - 35f, isBonusRoute = true))
                }
                powerUps.add(LevelPowerUp("${prefix}_sec_pu", secX + 100f, secY - 80f, selectBiomePowerUp(theme, random)))

                endX = currentX + width
                exitY = currentY
            }

            SectionType.CHECKPOINT_SECTION -> {
                val width = 380f
                platforms.add(LevelPlatform("${prefix}_cp_base", Rect(currentX, currentY, currentX + width, currentY + 400f), color = Color(0xFF0F172A)))

                checkpoints.add(
                    LevelCheckpoint(
                        id = "${prefix}_cp",
                        x = currentX + width / 2f - 18f,
                        y = currentY - 60f
                    )
                )

                endX = currentX + width
                exitY = currentY
            }
        }

        return GeneratedSection(
            type = type,
            startX = startX,
            endX = endX,
            width = endX - startX,
            exitX = endX,
            exitY = exitY,
            platforms = platforms,
            checkpoints = checkpoints,
            crystals = crystals,
            powerUps = powerUps,
            spikes = spikes,
            movingSpikes = movingSpikes,
            blades = blades,
            lasers = lasers,
            hazards = hazards,
            enemies = enemies,
            hasSecretRoom = isSecret,
            hasRiskRoute = isRisk
        )
    }
}
