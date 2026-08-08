package com.myplaywin.app.shadowhero.engine

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import java.util.Random
import kotlin.math.abs
import kotlin.math.min

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
    STEPPING_STONES,
    WALL_SHAFT,
    DASH_CORRIDOR,
    BRANCHING_ROUTE,
    VERTICAL_CLIMB,
    ALTERNATING_STAGGER
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
                PatternType.STEPPING_STONES -> {
                    val count = 2 + random.nextInt(3)
                    for (p in 0 until count) {
                        val pWidth = minPlatWidth + random.nextFloat() * (maxPlatWidth - minPlatWidth)
                        val pY = groundY - (random.nextFloat() * 120f - 40f)
                        val pRect = Rect(currentX, pY, currentX + pWidth, pY + 30f)

                        // Special Platform Behavior based on stage difficulty
                        val pBehavior = when {
                            stageNumber >= 5 && p == 1 && random.nextBoolean() -> PlatformBehaviorType.FALLING
                            stageNumber >= 12 && p == count - 1 && random.nextBoolean() -> PlatformBehaviorType.DISAPPEARING
                            stageNumber >= 18 && p == 0 && random.nextBoolean() -> PlatformBehaviorType.BREAKABLE
                            else -> PlatformBehaviorType.NORMAL
                        }

                        platforms.add(
                            LevelPlatform(
                                id = "step_${i}_$p",
                                bounds = pRect,
                                color = Color(0xFF4C1D95),
                                behaviorType = pBehavior
                            )
                        )

                        // Static Spike on platform edge (leaving safe middle)
                        if (stageNumber >= 3 && pWidth >= 160f && random.nextFloat() < 0.45f) {
                            spikes.add(
                                LevelSpike(
                                    id = "spike_step_${i}_$p",
                                    x = currentX + pWidth - 36f,
                                    y = pY - 24f,
                                    width = 30f,
                                    height = 24f
                                )
                            )
                        }

                        // Enemy Shadow Walker on wide platform
                        if (stageNumber >= 4 && pWidth >= 200f && random.nextFloat() < 0.4f) {
                            enemies.add(
                                LevelEnemy(
                                    id = "walker_${i}_$p",
                                    type = EnemyType.SHADOW_WALKER,
                                    initialX = currentX + 30f,
                                    initialY = pY - 38f,
                                    patrolMinX = currentX + 10f,
                                    patrolMaxX = currentX + pWidth - 40f
                                )
                            )
                        }

                        if (random.nextBoolean()) {
                            crystals.add(
                                LevelEnergyCrystal("cryst_${i}_$p", currentX + pWidth / 2f, pY - 35f)
                            )
                        } else if (random.nextFloat() < 0.35f) {
                            val types = PowerUpType.values()
                            val pType = types[random.nextInt(types.size)]
                            powerUps.add(
                                LevelPowerUp("powerup_${i}_$p", currentX + pWidth / 2f, pY - 35f, pType)
                            )
                        }

                        currentX += pWidth + (80f + random.nextFloat() * 80f)
                    }
                }

                PatternType.WALL_SHAFT -> {
                    val shaftWidth = 200f + random.nextFloat() * 60f
                    val shaftHeight = 350f
                    val shaftTopY = groundY - shaftHeight

                    platforms.add(
                        LevelPlatform("shaft_left_$i", Rect(currentX, shaftTopY, currentX + 40f, groundY + 100f), isWall = true, color = Color(0xFF5B21B6))
                    )
                    platforms.add(
                        LevelPlatform("shaft_right_$i", Rect(currentX + shaftWidth, shaftTopY, currentX + shaftWidth + 40f, groundY + 100f), isWall = true, color = Color(0xFF5B21B6))
                    )

                    val topLedge = Rect(currentX + shaftWidth + 40f, shaftTopY + 20f, currentX + shaftWidth + 240f, shaftTopY + 50f)
                    platforms.add(LevelPlatform("shaft_top_$i", topLedge, color = Color(0xFF0284C7)))

                    // Horizontal Laser Beam inside Wall Shaft with 0.8s Warning cycle
                    if (stageNumber >= 8) {
                        lasers.add(
                            LevelLaserBeam(
                                id = "laser_shaft_$i",
                                startX = currentX + 40f,
                                startY = groundY - 180f,
                                endX = currentX + shaftWidth,
                                endY = groundY - 180f,
                                warningDuration = 0.8f,
                                activeDuration = 1.2f,
                                inactiveDuration = 1.8f,
                                isVertical = false
                            )
                        )
                    }

                    // Rotating Blade in shaft
                    if (stageNumber >= 15 && random.nextBoolean()) {
                        blades.add(
                            LevelRotatingBlade(
                                id = "blade_shaft_$i",
                                centerX = currentX + shaftWidth / 2f + 20f,
                                centerY = groundY - 240f,
                                radius = 26f,
                                rotationSpeed = 160f
                            )
                        )
                    }

                    crystals.add(LevelEnergyCrystal("shaft_cryst1_$i", currentX + shaftWidth / 2f + 20f, groundY - 120f))
                    crystals.add(LevelEnergyCrystal("shaft_cryst2_$i", currentX + shaftWidth / 2f + 20f, shaftTopY + 60f))

                    currentX += shaftWidth + 240f
                }

                PatternType.DASH_CORRIDOR -> {
                    val dashGap = 280f + random.nextFloat() * 80f
                    val platWidth = 180f
                    val platY = groundY - 30f

                    platforms.add(
                        LevelPlatform("dash_land_$i", Rect(currentX + dashGap, platY, currentX + dashGap + platWidth, platY + 35f), color = Color(0xFF6D28D9))
                    )

                    // Moving Spike floating in dash gap
                    if (stageNumber >= 6) {
                        movingSpikes.add(
                            LevelMovingSpike(
                                id = "mspike_dash_$i",
                                startX = currentX + 80f,
                                startY = platY + 40f,
                                endX = currentX + dashGap - 60f,
                                endY = platY + 40f,
                                speed = 90f + min(80f, stageNumber * 2f)
                            )
                        )
                    }

                    // Turret Enemy on landing platform shooting back
                    if (stageNumber >= 12 && random.nextFloat() < 0.5f) {
                        enemies.add(
                            LevelEnemy(
                                id = "turret_dash_$i",
                                type = EnemyType.TURRET,
                                initialX = currentX + dashGap + platWidth - 45f,
                                initialY = platY - 38f
                            )
                        )
                    }

                    for (c in 1..3) {
                        crystals.add(
                            LevelEnergyCrystal("dash_cryst_${i}_$c", currentX + (dashGap / 4f) * c, platY - 30f)
                        )
                    }

                    currentX += dashGap + platWidth
                }

                PatternType.BRANCHING_ROUTE -> {
                    val routeLength = 400f

                    platforms.add(
                        LevelPlatform("branch_low_$i", Rect(currentX, groundY, currentX + routeLength, groundY + 40f), color = Color(0xFF1E1038))
                    )
                    crystals.add(LevelEnergyCrystal("branch_low_cryst_$i", currentX + routeLength / 2f, groundY - 30f))

                    val highY = groundY - 200f
                    platforms.add(
                        LevelPlatform("branch_high1_$i", Rect(currentX + 50f, highY, currentX + 180f, highY + 25f), color = Color(0xFF8B5CF6))
                    )
                    platforms.add(
                        LevelPlatform("branch_high2_$i", Rect(currentX + 240f, highY, currentX + 370f, highY + 25f), color = Color(0xFF8B5CF6))
                    )

                    // Chaser Enemy on high bonus route in stage >= 20
                    if (stageNumber >= 20 && random.nextBoolean()) {
                        enemies.add(
                            LevelEnemy(
                                id = "chaser_branch_$i",
                                type = EnemyType.CHASER,
                                initialX = currentX + 300f,
                                initialY = highY - 45f
                            )
                        )
                    }

                    // Flying Orb hovering between low & high route
                    if (stageNumber >= 7) {
                        enemies.add(
                            LevelEnemy(
                                id = "orb_branch_$i",
                                type = EnemyType.FLYING_ORB,
                                initialX = currentX + 100f,
                                initialY = groundY - 110f,
                                patrolMinX = currentX + 80f,
                                patrolMaxX = currentX + 320f
                            )
                        )
                    }

                    crystals.add(LevelEnergyCrystal("bonus_cryst1_$i", currentX + 115f, highY - 35f, isBonusRoute = true))
                    crystals.add(LevelEnergyCrystal("bonus_cryst2_$i", currentX + 305f, highY - 35f, isBonusRoute = true))

                    currentX += routeLength
                }

                PatternType.VERTICAL_CLIMB -> {
                    var climbY = groundY
                    val steps = 3
                    for (s in 0 until steps) {
                        climbY -= 110f
                        val stepW = 150f

                        val stepBehavior = if (stageNumber >= 10 && s == 1) PlatformBehaviorType.DISAPPEARING else PlatformBehaviorType.NORMAL

                        platforms.add(
                            LevelPlatform("vert_${i}_$s", Rect(currentX, climbY, currentX + stepW, climbY + 30f), color = Color(0xFF7C3AED), behaviorType = stepBehavior)
                        )

                        // Static Spikes under step 2
                        if (s == 1 && stageNumber >= 5) {
                            spikes.add(
                                LevelSpike("spike_vert_${i}_$s", currentX + 10f, climbY - 24f, width = 28f, height = 24f)
                            )
                        }

                        crystals.add(LevelEnergyCrystal("vert_cryst_${i}_$s", currentX + stepW / 2f, climbY - 35f))
                        currentX += stepW + 60f
                    }
                }

                PatternType.ALTERNATING_STAGGER -> {
                    var altY = groundY - 40f
                    for (a in 0..2) {
                        val platW = 160f
                        altY = if (a % 2 == 0) groundY - 120f else groundY - 30f
                        platforms.add(
                            LevelPlatform("alt_${i}_$a", Rect(currentX, altY, currentX + platW, altY + 30f), color = Color(0xFF5B21B6))
                        )

                        // Rotating blade between staggered steps
                        if (a == 1 && stageNumber >= 14) {
                            blades.add(
                                LevelRotatingBlade(
                                    id = "blade_alt_${i}_$a",
                                    centerX = currentX - 30f,
                                    centerY = groundY - 75f,
                                    radius = 28f,
                                    rotationSpeed = 200f
                                )
                            )
                        }

                        currentX += platW + 80f
                    }
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
        val levelWidth = 2800f
        val levelHeight = 1000f
        val groundY = 600f

        val platforms = listOf(
            LevelPlatform("left_wall", Rect(-40f, 0f, 0f, levelHeight), isWall = true),
            LevelPlatform("ground_1", Rect(0f, groundY, 1200f, levelHeight)),
            LevelPlatform("ground_2", Rect(1350f, groundY, levelWidth, levelHeight)),
            LevelPlatform("right_wall", Rect(levelWidth, 0f, levelWidth + 40f, levelHeight), isWall = true)
        )

        val checkpoints = listOf(
            LevelCheckpoint("cp_fallback", 1100f, groundY - 60f)
        )

        val crystals = listOf(
            LevelEnergyCrystal("cryst_fb1", 500f, groundY - 30f),
            LevelEnergyCrystal("cryst_fb2", 800f, groundY - 30f),
            LevelEnergyCrystal("cryst_fb3", 1600f, groundY - 30f)
        )

        val exitPortal = LevelExitPortal(2200f, groundY - 50f)

        return GeneratedLevel(
            seed = seed,
            stageNumber = stageNumber,
            difficultyName = "Beginner (Fallback)",
            levelWidth = levelWidth,
            levelHeight = levelHeight,
            spawnX = 150f,
            spawnY = groundY - 60f,
            platforms = platforms,
            checkpoints = checkpoints,
            crystals = crystals,
            exitPortal = exitPortal,
            patternSequence = listOf("FALLBACK_SAFE"),
            generationTimeMs = 0L,
            validationAttempts = 1
        )
    }
}
