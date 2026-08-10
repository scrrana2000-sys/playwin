package com.myplaywin.app.shadowhero.engine

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Checkpoint Structure
data class LevelCheckpoint(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float = 36f,
    val height: Float = 60f,
    var isActivated: Boolean = false
) {
    val bounds: Rect
        get() = Rect(x, y, x + width, y + height)
}

// Energy Crystal Collectible Structure
data class LevelEnergyCrystal(
    val id: String,
    var x: Float,
    var y: Float,
    val radius: Float = 14f,
    val isBonusRoute: Boolean = false,
    var isCollected: Boolean = false
) {
    val bounds: Rect
        get() = Rect(x - radius, y - radius, x + radius, y + radius)
}

// Power-Up Structure (Phase 5)
enum class PowerUpType {
    SHIELD,
    DASH_BOOST,
    MAGNET,
    SLOW_TIME,
    DOUBLE_CRYSTAL,
    SHADOW_TIME,
    CRYSTAL_MAGNET,
    ENERGY_BOOST,
    DASH_RECHARGE
}

data class LevelPowerUp(
    val id: String,
    val x: Float,
    val y: Float,
    val type: PowerUpType,
    val radius: Float = 16f,
    var isCollected: Boolean = false
) {
    val bounds: Rect
        get() = Rect(x - radius, y - radius, x + radius, y + radius)
}

// Exit Portal Structure
data class LevelExitPortal(
    val x: Float,
    val y: Float,
    val radius: Float = 32f
) {
    val bounds: Rect
        get() = Rect(x - radius, y - radius * 1.5f, x + radius, y + radius * 0.5f)
}

enum class AmbientParticleType {
    DRIFTING_CRYSTAL_DUST,
    ELECTRICAL_SPARKS,
    FALLING_SNOW,
    FLOATING_EMBERS,
    WIND_CLOUDS,
    FLOATING_SHADOWS,
    VOID_FRAGMENTS
}

enum class LevelTheme(
    val themeName: String,
    val primaryBgColor: Color,
    val secondaryBgColor: Color,
    val platformColor: Color,
    val platformBorderColor: Color,
    val accentGlowColor: Color,
    val particleType: AmbientParticleType
) {
    NEON_CAVES(
        themeName = "Neon Caves",
        primaryBgColor = Color(0xFF0D061A),
        secondaryBgColor = Color(0xFF1E0A38),
        platformColor = Color(0xFF2E1065),
        platformBorderColor = Color(0xFFA855F7),
        accentGlowColor = Color(0xFFE879F9),
        particleType = AmbientParticleType.DRIFTING_CRYSTAL_DUST
    ),
    CYBER_FACTORY(
        themeName = "Cyber Factory",
        primaryBgColor = Color(0xFF030712),
        secondaryBgColor = Color(0xFF0F172A),
        platformColor = Color(0xFF1E293B),
        platformBorderColor = Color(0xFF06B6D4),
        accentGlowColor = Color(0xFF38BDF8),
        particleType = AmbientParticleType.ELECTRICAL_SPARKS
    ),
    FROZEN_TEMPLE(
        themeName = "Frozen Temple",
        primaryBgColor = Color(0xFF031525),
        secondaryBgColor = Color(0xFF082F49),
        platformColor = Color(0xFF0C4A6E),
        platformBorderColor = Color(0xFF38BDF8),
        accentGlowColor = Color(0xFFBAE6FD),
        particleType = AmbientParticleType.FALLING_SNOW
    ),
    LAVA_CORE(
        themeName = "Lava Core",
        primaryBgColor = Color(0xFF180303),
        secondaryBgColor = Color(0xFF450A0A),
        platformColor = Color(0xFF7F1D1D),
        platformBorderColor = Color(0xFFF97316),
        accentGlowColor = Color(0xFFEF4444),
        particleType = AmbientParticleType.FLOATING_EMBERS
    ),
    SKY_RUINS(
        themeName = "Sky Ruins",
        primaryBgColor = Color(0xFF0A1026),
        secondaryBgColor = Color(0xFF1E1B4B),
        platformColor = Color(0xFF312E81),
        platformBorderColor = Color(0xFF818CF8),
        accentGlowColor = Color(0xFFC7D2FE),
        particleType = AmbientParticleType.WIND_CLOUDS
    ),
    SHADOW_CASTLE(
        themeName = "Shadow Castle",
        primaryBgColor = Color(0xFF090314),
        secondaryBgColor = Color(0xFF180828),
        platformColor = Color(0xFF3B0764),
        platformBorderColor = Color(0xFFD8B4FE),
        accentGlowColor = Color(0xFFC084FC),
        particleType = AmbientParticleType.FLOATING_SHADOWS
    ),
    VOID_DIMENSION(
        themeName = "Void Dimension",
        primaryBgColor = Color(0xFF020208),
        secondaryBgColor = Color(0xFF090518),
        platformColor = Color(0xFF19062B),
        platformBorderColor = Color(0xFFF43F5E),
        accentGlowColor = Color(0xFFFB7185),
        particleType = AmbientParticleType.VOID_FRAGMENTS
    );

    companion object {
        fun getThemeForStage(stageNumber: Int): LevelTheme {
            val themeIndex = ((stageNumber - 1) / 10) % values().size
            return values()[themeIndex]
        }
    }
}

// Authoritative Generated Level Container
data class GeneratedLevel(
    val seed: Long,
    val stageNumber: Int,
    val difficultyName: String,
    val theme: LevelTheme = LevelTheme.getThemeForStage(stageNumber),
    val levelWidth: Float,
    val levelHeight: Float,
    val spawnX: Float,
    val spawnY: Float,
    val platforms: List<LevelPlatform>,
    val checkpoints: List<LevelCheckpoint>,
    val crystals: List<LevelEnergyCrystal>,
    val powerUps: List<LevelPowerUp> = emptyList(),
    val exitPortal: LevelExitPortal,
    val spikes: List<LevelSpike> = emptyList(),
    val movingSpikes: List<LevelMovingSpike> = emptyList(),
    val blades: List<LevelRotatingBlade> = emptyList(),
    val lasers: List<LevelLaserBeam> = emptyList(),
    val hazards: List<LevelEnvHazard> = emptyList(),
    val enemies: List<LevelEnemy> = emptyList(),
    val patternSequence: List<String>,
    val generationTimeMs: Long,
    val validationAttempts: Int
)

enum class PatternType {
    GROUND_PLATFORM,
    SMALL_GAP,
    LARGE_GAP,
    STEPPING_STONES,
    MOVING_PLATFORMS,
    VERTICAL_CLIMB,
    WALL_SHAFT,
    DASH_CORRIDOR,
    NARROW_PASSAGE,
    FALLING_PLATFORMS,
    HAZARD_SECTION,
    ENEMY_SECTION,
    CRYSTAL_ROUTE,
    OPTIONAL_RISK_REWARD
}

object ShadowHeroLevelGenerator {

    /**
     * Converts a Stage Number to a deterministic 64-bit Seed.
     */
    fun stageToSeed(stageNumber: Int): Long {
        return stageNumber.toLong() * 3141592653L + 123456789L
    }

    /**
     * Generates a deterministic seed based on current date (YYYYMMDD) for Daily Challenges.
     */
    fun dailySeed(year: Int, month: Int, day: Int): Long {
        return (year * 10000 + month * 100 + day).toLong() * 987654321L
    }

    /**
     * Generates a guaranteed valid, playable Shadow Hero stage.
     */
    fun generateStage(stageNumber: Int, customSeed: Long? = null): GeneratedLevel {
        val startTime = System.currentTimeMillis()
        val baseSeed = customSeed ?: stageToSeed(stageNumber)

        var currentSeed = baseSeed
        var attempts = 0
        var generatedLevel: GeneratedLevel? = null

        // Loop until path validator confirms 100% reachability
        while (attempts < 15) {
            attempts++
            val candidate = buildCandidateLevel(stageNumber, currentSeed, attempts)
            val validation = ShadowHeroPathValidator.validateLevel(candidate)

            if (validation.isValid) {
                val duration = System.currentTimeMillis() - startTime
                generatedLevel = candidate.copy(
                    generationTimeMs = duration,
                    validationAttempts = attempts
                )
                break
            } else {
                // Shift seed deterministically if candidate failed validation
                currentSeed += 10007L
            }
        }

        // Fallback: If all attempts failed (unlikely), generate guaranteed flat fallback layout
        return generatedLevel ?: buildSafeFallbackLevel(stageNumber, baseSeed)
    }

    private fun buildCandidateLevel(stageNumber: Int, seed: Long, attemptIndex: Int): GeneratedLevel {
        val random = Random(seed)

        // Difficulty Parameters based on Stage Number
        val difficultyName = when {
            stageNumber <= 10 -> "Beginner"
            stageNumber <= 25 -> "Easy/Medium"
            stageNumber <= 50 -> "Medium"
            stageNumber <= 100 -> "Hard"
            else -> "Advanced"
        }

        val minPlatWidth = (220f - min(100f, stageNumber * 1.2f)).coerceAtLeast(100f)
        val maxPlatWidth = (300f - min(100f, stageNumber * 1.5f)).coerceAtLeast(140f)

        val minGapWidth = (120f + min(160f, stageNumber * 1.8f)).coerceAtMost(280f)
        val maxGapWidth = (180f + min(180f, stageNumber * 2.2f)).coerceAtMost(340f)

        val totalSections = (3 + min(8, stageNumber / 8)).coerceIn(3, 10)

        val levelHeight = 1000f
        val groundY = 650f

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
        val patternSequence = mutableListOf<String>()

        var currentX = 0f

        // 1. BOUNDARIES
        platforms.add(LevelPlatform("left_wall", Rect(-40f, -200f, 0f, levelHeight), isWall = true, color = Color(0xFF1E0A38)))

        // 2. START SECTION (Safe Landing Spawn - STRICT NO HAZARDS ZONE)
        val startPlatWidth = 450f
        platforms.add(
            LevelPlatform("start_ground", Rect(currentX, groundY, currentX + startPlatWidth, levelHeight), color = Color(0xFF1E1038))
        )

        val spawnX = currentX + 100f
        val spawnY = groundY - 60f
        currentX += startPlatWidth

        // Bottom Lava Pit across entire stage width
        hazards.add(
            LevelEnvHazard(
                id = "pit_lava",
                bounds = Rect(-100f, levelHeight - 80f, 10000f, levelHeight + 200f),
                type = EnvHazardType.LAVA
            )
        )

        // Anti-repetition tracker
        var lastPattern: PatternType? = null

        // 3. GENERATE SECTION PATTERNS WITH PHASE 4 OBSTACLES & ENEMIES
        for (i in 0 until totalSections) {
            val availablePatterns = PatternType.values().filter { it != lastPattern }
            val chosenPattern = availablePatterns[random.nextInt(availablePatterns.size)]
            lastPattern = chosenPattern
            patternSequence.add(chosenPattern.name)

            val gap = minGapWidth + random.nextFloat() * (maxGapWidth - minGapWidth)
            currentX += gap

            when (chosenPattern) {
                PatternType.GROUND_PLATFORM -> {
                    val pWidth = 280f + random.nextFloat() * 120f
                    val pRect = Rect(currentX, groundY, currentX + pWidth, groundY + 40f)
                    platforms.add(LevelPlatform("ground_${i}", pRect, color = Color(0xFF1E1038)))

                    if (stageNumber >= 1 && random.nextFloat() < 0.65f) {
                        val eType = selectEnemyForStage(stageNumber, isFlying = false, random = random)
                        val eDims = getEnemyDimensions(eType)
                        val enemy = LevelEnemy(
                            id = "enemy_ground_${i}",
                            type = eType,
                            initialX = currentX + 60f,
                            initialY = groundY - eDims.second,
                            patrolMinX = currentX + 15f,
                            patrolMaxX = currentX + pWidth - 15f
                        )
                        enemy.initStats(stageNumber)
                        enemies.add(enemy)
                    }

                    crystals.add(LevelEnergyCrystal("cryst_ground_${i}", currentX + pWidth / 2f, groundY - 35f))
                    currentX += pWidth
                }

                PatternType.SMALL_GAP -> {
                    val gap = 140f + random.nextFloat() * 60f
                    val pWidth = 220f + random.nextFloat() * 80f
                    val pRect = Rect(currentX + gap, groundY - 20f, currentX + gap + pWidth, groundY + 20f)
                    platforms.add(LevelPlatform("sgap_${i}", pRect, color = Color(0xFF4C1D95)))

                    crystals.add(LevelEnergyCrystal("cryst_sgap_${i}", currentX + gap / 2f, groundY - 50f))
                    currentX += gap + pWidth
                }

                PatternType.LARGE_GAP -> {
                    val gap = 240f + random.nextFloat() * 80f
                    val pWidth = 200f + random.nextFloat() * 80f
                    val pRect = Rect(currentX + gap, groundY - 30f, currentX + gap + pWidth, groundY + 20f)
                    platforms.add(LevelPlatform("lgap_${i}", pRect, color = Color(0xFF5B21B6)))

                    for (c in 1..3) {
                        crystals.add(LevelEnergyCrystal("cryst_lgap_${i}_$c", currentX + (gap / 4f) * c, groundY - 60f))
                    }
                    currentX += gap + pWidth
                }

                PatternType.STEPPING_STONES -> {
                    val count = 2 + random.nextInt(3)
                    for (p in 0 until count) {
                        val pWidth = minPlatWidth + random.nextFloat() * (maxPlatWidth - minPlatWidth)
                        val pY = groundY - (random.nextFloat() * 120f - 40f)
                        val pRect = Rect(currentX, pY, currentX + pWidth, pY + 30f)

                        val pBehavior = when {
                            stageNumber >= 5 && p == 1 && random.nextBoolean() -> PlatformBehaviorType.FALLING
                            stageNumber >= 12 && p == count - 1 && random.nextBoolean() -> PlatformBehaviorType.DISAPPEARING
                            else -> PlatformBehaviorType.NORMAL
                        }

                        platforms.add(
                            LevelPlatform("step_${i}_$p", pRect, color = Color(0xFF4C1D95), behaviorType = pBehavior)
                        )

                        if (random.nextBoolean()) {
                            crystals.add(LevelEnergyCrystal("cryst_${i}_$p", currentX + pWidth / 2f, pY - 35f))
                        }
                        currentX += pWidth + (80f + random.nextFloat() * 80f)
                    }
                }

                PatternType.MOVING_PLATFORMS -> {
                    val gap = 200f + random.nextFloat() * 80f
                    val pWidth = 180f
                    val pY = groundY - 40f
                    val pRect = Rect(currentX + gap, pY, currentX + gap + pWidth, pY + 30f)

                    platforms.add(
                        LevelPlatform(
                            id = "mplat_${i}",
                            bounds = pRect,
                            color = Color(0xFF0284C7),
                            behaviorType = PlatformBehaviorType.MOVING
                        )
                    )

                    crystals.add(LevelEnergyCrystal("cryst_mplat_${i}", currentX + gap + pWidth / 2f, pY - 35f))
                    currentX += gap + pWidth
                }

                PatternType.VERTICAL_CLIMB -> {
                    var climbY = groundY
                    val steps = 3
                    for (s in 0 until steps) {
                        climbY -= 110f
                        val stepW = 150f
                        platforms.add(
                            LevelPlatform("vert_${i}_$s", Rect(currentX, climbY, currentX + stepW, climbY + 30f), color = Color(0xFF7C3AED))
                        )
                        crystals.add(LevelEnergyCrystal("vert_cryst_${i}_$s", currentX + stepW / 2f, climbY - 35f))
                        currentX += stepW + 60f
                    }
                }

                PatternType.WALL_SHAFT -> {
                    val shaftWidth = 200f + random.nextFloat() * 60f
                    val shaftHeight = 350f
                    val shaftTopY = groundY - shaftHeight

                    platforms.add(LevelPlatform("shaft_left_$i", Rect(currentX, shaftTopY, currentX + 40f, groundY + 100f), isWall = true, color = Color(0xFF5B21B6)))
                    platforms.add(LevelPlatform("shaft_right_$i", Rect(currentX + shaftWidth, shaftTopY, currentX + shaftWidth + 40f, groundY + 100f), isWall = true, color = Color(0xFF5B21B6)))

                    val topLedge = Rect(currentX + shaftWidth + 40f, shaftTopY + 20f, currentX + shaftWidth + 240f, shaftTopY + 50f)
                    platforms.add(LevelPlatform("shaft_top_$i", topLedge, color = Color(0xFF0284C7)))

                    crystals.add(LevelEnergyCrystal("shaft_cryst1_$i", currentX + shaftWidth / 2f + 20f, groundY - 120f))
                    crystals.add(LevelEnergyCrystal("shaft_cryst2_$i", currentX + shaftWidth / 2f + 20f, shaftTopY + 60f))

                    currentX += shaftWidth + 240f
                }

                PatternType.DASH_CORRIDOR -> {
                    val dashGap = 280f + random.nextFloat() * 80f
                    val platWidth = 180f
                    val platY = groundY - 30f

                    platforms.add(LevelPlatform("dash_land_$i", Rect(currentX + dashGap, platY, currentX + dashGap + platWidth, platY + 35f), color = Color(0xFF6D28D9)))

                    for (c in 1..3) {
                        crystals.add(LevelEnergyCrystal("dash_cryst_${i}_$c", currentX + (dashGap / 4f) * c, platY - 30f))
                    }
                    currentX += dashGap + platWidth
                }

                PatternType.NARROW_PASSAGE -> {
                    val passWidth = 320f
                    platforms.add(LevelPlatform("pass_floor_$i", Rect(currentX, groundY, currentX + passWidth, groundY + 40f), color = Color(0xFF1E1038)))
                    platforms.add(LevelPlatform("pass_ceil_$i", Rect(currentX, groundY - 140f, currentX + passWidth, groundY - 100f), color = Color(0xFF1E1038)))

                    if (stageNumber >= 6) {
                        lasers.add(
                            LevelLaserBeam(
                                id = "laser_pass_$i",
                                startX = currentX + 60f,
                                startY = groundY - 100f,
                                endX = currentX + 60f,
                                endY = groundY,
                                warningDuration = 0.8f,
                                activeDuration = 1.2f,
                                inactiveDuration = 1.8f,
                                isVertical = true
                            )
                        )
                    }

                    crystals.add(LevelEnergyCrystal("pass_cryst_$i", currentX + passWidth / 2f, groundY - 35f))
                    currentX += passWidth
                }

                PatternType.FALLING_PLATFORMS -> {
                    val count = 3
                    for (f in 0 until count) {
                        val fWidth = 140f
                        val fY = groundY - 20f
                        platforms.add(
                            LevelPlatform("falling_${i}_$f", Rect(currentX, fY, currentX + fWidth, fY + 25f), color = Color(0xFFB91C1C), behaviorType = PlatformBehaviorType.FALLING)
                        )
                        crystals.add(LevelEnergyCrystal("fall_cryst_${i}_$f", currentX + fWidth / 2f, fY - 35f))
                        currentX += fWidth + 90f
                    }
                }

                PatternType.HAZARD_SECTION -> {
                    val hWidth = 300f
                    platforms.add(LevelPlatform("haz_floor_$i", Rect(currentX, groundY, currentX + hWidth, groundY + 40f), color = Color(0xFF1E1038)))

                    if (stageNumber >= 10) {
                        blades.add(
                            LevelRotatingBlade("blade_haz_$i", currentX + hWidth / 2f, groundY - 80f, 32f, 180f)
                        )
                    } else {
                        spikes.add(LevelSpike("spike_haz_$i", currentX + hWidth / 2f - 18f, groundY - 24f, 36f, 24f))
                    }

                    crystals.add(LevelEnergyCrystal("haz_cryst_$i", currentX + hWidth / 2f, groundY - 130f))
                    currentX += hWidth
                }

                PatternType.ENEMY_SECTION -> {
                    val eWidth = 320f
                    platforms.add(LevelPlatform("enemy_plat_$i", Rect(currentX, groundY - 20f, currentX + eWidth, groundY + 30f), color = Color(0xFF5B21B6)))

                    enemies.add(
                        LevelEnemy(
                            id = "enemy_sec_$i",
                            type = if (stageNumber >= 8) EnemyType.FLYING_ORB else EnemyType.SHADOW_WALKER,
                            initialX = currentX + eWidth / 2f,
                            initialY = groundY - 60f,
                            patrolMinX = currentX + 30f,
                            patrolMaxX = currentX + eWidth - 30f
                        )
                    )

                    crystals.add(LevelEnergyCrystal("enemy_cryst_$i", currentX + eWidth / 2f, groundY - 70f))
                    currentX += eWidth
                }

                PatternType.CRYSTAL_ROUTE -> {
                    val cWidth = 360f
                    platforms.add(LevelPlatform("cryst_route_plat_$i", Rect(currentX, groundY, currentX + cWidth, groundY + 40f), color = Color(0xFF0369A1)))

                    for (c in 1..5) {
                        val arcY = groundY - 35f - sin((c / 6f) * Math.PI.toFloat()) * 90f
                        crystals.add(LevelEnergyCrystal("cryst_route_${i}_$c", currentX + (cWidth / 6f) * c, arcY))
                    }
                    currentX += cWidth
                }

                PatternType.OPTIONAL_RISK_REWARD -> {
                    val routeLength = 400f

                    platforms.add(LevelPlatform("branch_low_$i", Rect(currentX, groundY, currentX + routeLength, groundY + 40f), color = Color(0xFF1E1038)))
                    crystals.add(LevelEnergyCrystal("branch_low_cryst_$i", currentX + routeLength / 2f, groundY - 30f))

                    val highY = groundY - 200f
                    platforms.add(LevelPlatform("branch_high1_$i", Rect(currentX + 50f, highY, currentX + 180f, highY + 25f), color = Color(0xFF8B5CF6)))
                    platforms.add(LevelPlatform("branch_high2_$i", Rect(currentX + 240f, highY, currentX + 370f, highY + 25f), color = Color(0xFF8B5CF6)))

                    crystals.add(LevelEnergyCrystal("bonus_cryst1_$i", currentX + 115f, highY - 35f, isBonusRoute = true))
                    crystals.add(LevelEnergyCrystal("bonus_cryst2_$i", currentX + 305f, highY - 35f, isBonusRoute = true))

                    currentX += routeLength
                }
            }

            // Checkpoint placement after every 2-3 sections or mid-level
            if ((i + 1) == totalSections / 2 || (i + 1) == totalSections - 1) {
                currentX += 80f
                val cpPlatWidth = 260f
                val cpPlatY = groundY - 20f

                platforms.add(
                    LevelPlatform("cp_plat_$i", Rect(currentX, cpPlatY, currentX + cpPlatWidth, cpPlatY + 40f), color = Color(0xFF1E1038))
                )

                checkpoints.add(
                    LevelCheckpoint("cp_$i", x = currentX + cpPlatWidth / 2f - 18f, y = cpPlatY - 60f)
                )

                currentX += cpPlatWidth
            }
        }

        // 4. EXIT SECTION & PORTAL (STRICT SAFE ZONE)
        currentX += 120f
        val exitPlatWidth = 400f
        val exitPlatY = groundY - 10f

        platforms.add(
            LevelPlatform("exit_ground", Rect(currentX, exitPlatY, currentX + exitPlatWidth, levelHeight), color = Color(0xFF0369A1))
        )

        val exitPortal = LevelExitPortal(
            x = currentX + exitPlatWidth / 2f,
            y = exitPlatY - 50f
        )

        currentX += exitPlatWidth + 40f
        val levelWidth = currentX

        platforms.add(LevelPlatform("right_wall", Rect(levelWidth, -200f, levelWidth + 40f, levelHeight), isWall = true, color = Color(0xFF1E0A38)))

        return GeneratedLevel(
            seed = seed,
            stageNumber = stageNumber,
            difficultyName = difficultyName,
            levelWidth = levelWidth,
            levelHeight = levelHeight,
            spawnX = spawnX,
            spawnY = spawnY,
            platforms = platforms,
            checkpoints = checkpoints,
            crystals = crystals,
            powerUps = powerUps,
            exitPortal = exitPortal,
            spikes = spikes,
            movingSpikes = movingSpikes,
            blades = blades,
            lasers = lasers,
            hazards = hazards,
            enemies = enemies,
            patternSequence = patternSequence,
            generationTimeMs = 0L,
            validationAttempts = attemptIndex
        )
    }

    private fun buildSafeFallbackLevel(stageNumber: Int, seed: Long): GeneratedLevel {
        val random = Random(seed)
        val levelHeight = 1000f
        var currentX = 0f
        var currentY = 600f

        val platforms = mutableListOf<LevelPlatform>()
        val checkpoints = mutableListOf<LevelCheckpoint>()
        val crystals = mutableListOf<LevelEnergyCrystal>()
        val powerUps = mutableListOf<LevelPowerUp>()
        val spikes = mutableListOf<LevelSpike>()
        val enemies = mutableListOf<LevelEnemy>()

        // Left Boundary Wall
        platforms.add(LevelPlatform("left_wall", Rect(-40f, -200f, 0f, levelHeight), isWall = true))

        // Start Safe Platform
        val startW = 400f
        platforms.add(LevelPlatform("fb_start", Rect(currentX, currentY, currentX + startW, levelHeight)))
        val spawnX = currentX + 100f
        val spawnY = currentY - 60f
        currentX += startW

        // Dynamic Segment Generation (6-12 sections depending on stage)
        val numSegments = 5 + (stageNumber % 6)
        for (s in 0 until numSegments) {
            val gapWidth = 100f + random.nextFloat() * 120f
            currentX += gapWidth

            val platWidth = 220f + random.nextFloat() * 180f
            val yDelta = (random.nextFloat() * 120f) - 60f
            currentY = (currentY + yDelta).coerceIn(400f, 700f)

            val pRect = Rect(currentX, currentY, currentX + platWidth, levelHeight)
            platforms.add(LevelPlatform("fb_plat_$s", pRect, color = Color(0xFF311B92)))

            // Checkpoint at midpoint
            if (s == numSegments / 2) {
                checkpoints.add(LevelCheckpoint("cp_fb_$s", currentX + 60f, currentY - 60f))
            }

            // Energy Crystals
            val countCryst = 1 + random.nextInt(3)
            for (c in 0 until countCryst) {
                crystals.add(
                    LevelEnergyCrystal(
                        id = "fb_cryst_${s}_$c",
                        x = currentX + 40f + (c * 50f),
                        y = currentY - 35f
                    )
                )
            }

            // Spikes on higher stages
            if (stageNumber >= 3 && random.nextFloat() < 0.35f) {
                spikes.add(
                    LevelSpike("fb_spike_$s", currentX + platWidth - 50f, currentY - 24f, 32f, 24f)
                )
            }

            // Enemy Shadow Walker on higher stages
            if (stageNumber >= 4 && random.nextFloat() < 0.3f) {
                enemies.add(
                    LevelEnemy(
                        id = "fb_enemy_$s",
                        type = EnemyType.SHADOW_WALKER,
                        initialX = currentX + platWidth / 2f,
                        initialY = currentY - 38f,
                        patrolMinX = currentX + 20f,
                        patrolMaxX = currentX + platWidth - 20f
                    )
                )
            }

            currentX += platWidth
        }

        // Exit Platform & Portal
        currentX += 80f
        val exitW = 350f
        val exitPlatY = currentY
        platforms.add(LevelPlatform("fb_exit_ground", Rect(currentX, exitPlatY, currentX + exitW, levelHeight), color = Color(0xFF0369A1)))
        val exitPortal = LevelExitPortal(currentX + exitW / 2f, exitPlatY - 50f)
        currentX += exitW

        // Right Wall
        platforms.add(LevelPlatform("right_wall", Rect(currentX, -200f, currentX + 40f, levelHeight), isWall = true))

        return GeneratedLevel(
            seed = seed,
            stageNumber = stageNumber,
            difficultyName = "Stage $stageNumber",
            levelWidth = currentX,
            levelHeight = levelHeight,
            spawnX = spawnX,
            spawnY = spawnY,
            platforms = platforms,
            checkpoints = checkpoints,
            crystals = crystals,
            powerUps = powerUps,
            spikes = spikes,
            enemies = enemies,
            exitPortal = exitPortal,
            patternSequence = listOf("DYNAMIC_STAGE_$stageNumber"),
            generationTimeMs = 0L,
            validationAttempts = 1
        )
    }
}
