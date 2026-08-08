package com.myplaywin.app.shadowhero.engine

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.myplaywin.app.shadowhero.audio.ShadowHeroAudioEngine
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// Player Anim States
enum class PlayerAnimState {
    IDLE,
    RUN,
    JUMP,
    FALL,
    DOUBLE_JUMP,
    WALL_SLIDE,
    WALL_JUMP,
    DASH,
    LAND,
    DEATH
}

// Particle for dash, wall slide dust, landing impact
data class HeroParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var color: Color,
    var alpha: Float,
    var maxLife: Float,
    var currentLife: Float = 0f
)

// Ghost trail frame for dash effect
data class DashGhostFrame(
    val x: Float,
    val y: Float,
    val facingRight: Boolean,
    val isDashing: Boolean,
    var alpha: Float = 0.8f
)

// Platform Structure in the Test Environment
data class LevelPlatform(
    val id: String,
    val bounds: Rect,
    val isWall: Boolean = false,
    val isOneWay: Boolean = false,
    val color: Color = Color(0xFF3B0764),
    val behaviorType: PlatformBehaviorType = PlatformBehaviorType.NORMAL,
    var isTriggered: Boolean = false,
    var triggerTimer: Float = 0f,
    var currentOffsetY: Float = 0f,
    var isBroken: Boolean = false
) {
    fun onPlayerLand() {
        if (!isTriggered && (behaviorType == PlatformBehaviorType.FALLING || behaviorType == PlatformBehaviorType.DISAPPEARING || behaviorType == PlatformBehaviorType.BREAKABLE)) {
            isTriggered = true
            triggerTimer = 0f
        }
    }

    fun update(dt: Float) {
        if (isTriggered) {
            triggerTimer += dt
            if (behaviorType == PlatformBehaviorType.FALLING && triggerTimer > 0.4f) {
                currentOffsetY += 600f * dt
            }
            if (behaviorType == PlatformBehaviorType.BREAKABLE && triggerTimer > 0.3f) {
                isBroken = true
            }
        }
    }

    fun reset() {
        isTriggered = false
        triggerTimer = 0f
        currentOffsetY = 0f
        isBroken = false
    }
}

// Authoritative Player State Model
class ShadowHeroPlayer {
    // Dimensions
    var width: Float = 38f
    var height: Float = 54f

    // Position & Velocity
    var x: Float = 150f
    var y: Float = 480f
    var vx: Float = 0f
    var vy: Float = 0f

    // Orientation
    var facingRight: Boolean = true

    // Ground & Wall States
    var isGrounded: Boolean = false
    var isOnLeftWall: Boolean = false
    var isOnRightWall: Boolean = false
    var jumpCount: Int = 0

    // Dash States
    var isDashing: Boolean = false
    var dashTimer: Float = 0f
    var dashCooldownTimer: Float = 0f
    var dashDirection: Float = 1f

    // Landing & Animation States
    var animState: PlayerAnimState = PlayerAnimState.IDLE
    var landingSquashTimer: Float = 0f
    var runCycleTimer: Float = 0f
    var wallSlideTimer: Float = 0f
    var invincibleTimer: Float = 0f
    var isDead: Boolean = false

    // Bounding Box
    val bounds: Rect
        get() = Rect(x, y, x + width, y + height)

    fun resetToSpawn(spawnX: Float = 150f, spawnY: Float = 450f) {
        x = spawnX
        y = spawnY
        vx = 0f
        vy = 0f
        facingRight = true
        isGrounded = true
        isOnLeftWall = false
        isOnRightWall = false
        jumpCount = 0
        isDashing = false
        dashTimer = 0f
        dashCooldownTimer = 0f
        animState = PlayerAnimState.IDLE
        landingSquashTimer = 0f
        isDead = false
    }
}

// Test Environment Layout Generator (Phase 9)
object ShadowHeroTestMap {
    const val LEVEL_WIDTH = 3200f
    const val LEVEL_HEIGHT = 900f
    val SPAWN_X = 150f
    val SPAWN_Y = 450f

    fun generatePlatforms(): List<LevelPlatform> {
        val platforms = mutableListOf<LevelPlatform>()

        // Outer Left Wall Boundary
        platforms.add(
            LevelPlatform("left_boundary", Rect(0f, 0f, 40f, LEVEL_HEIGHT), isWall = true, color = Color(0xFF28114B))
        )

        // Outer Right Wall Boundary
        platforms.add(
            LevelPlatform("right_boundary", Rect(LEVEL_WIDTH - 40f, 0f, LEVEL_WIDTH, LEVEL_HEIGHT), isWall = true, color = Color(0xFF28114B))
        )

        // Flat Main Ground Section 1 (Starting Hub)
        platforms.add(
            LevelPlatform("ground_1", Rect(0f, 580f, 850f, LEVEL_HEIGHT), color = Color(0xFF1E1038))
        )

        // Ground Pit Gap 1 (Between 850 and 1050)

        // Ground Section 2 (Middle Arena)
        platforms.add(
            LevelPlatform("ground_2", Rect(1050f, 580f, 1800f, LEVEL_HEIGHT), color = Color(0xFF1E1038))
        )

        // Ground Pit Gap 2 (Between 1800 and 2050)

        // Ground Section 3 (Ending Shaft Area)
        platforms.add(
            LevelPlatform("ground_3", Rect(2050f, 580f, LEVEL_WIDTH, LEVEL_HEIGHT), color = Color(0xFF1E1038))
        )

        // --- PLATFORMS FOR JUMPING & PARKOUR ---
        // Low Platforms
        platforms.add(LevelPlatform("plat_low_1", Rect(280f, 460f, 480f, 485f), color = Color(0xFF4C1D95)))
        platforms.add(LevelPlatform("plat_low_2", Rect(540f, 360f, 740f, 385f), color = Color(0xFF4C1D95)))

        // Mid Air Stepping Stones Over Gap 1
        platforms.add(LevelPlatform("plat_gap1_1", Rect(860f, 480f, 960f, 505f), color = Color(0xFF6D28D9)))
        platforms.add(LevelPlatform("plat_gap1_2", Rect(980f, 380f, 1080f, 405f), color = Color(0xFF6D28D9)))

        // High Platform
        platforms.add(LevelPlatform("plat_high_1", Rect(1150f, 260f, 1450f, 285f), color = Color(0xFF7C3AED)))
        platforms.add(LevelPlatform("plat_high_2", Rect(1520f, 180f, 1750f, 205f), color = Color(0xFF8B5CF6)))

        // --- VERTICAL WALL JUMP SHAFT SECTION ---
        // Left Wall of Shaft
        platforms.add(
            LevelPlatform("wall_shaft_left", Rect(2200f, 100f, 2240f, 580f), isWall = true, color = Color(0xFF5B21B6))
        )
        // Right Wall of Shaft
        platforms.add(
            LevelPlatform("wall_shaft_right", Rect(2440f, 0f, 2480f, 580f), isWall = true, color = Color(0xFF5B21B6))
        )

        // Upper Shaft Reward Platform
        platforms.add(
            LevelPlatform("plat_shaft_top", Rect(2240f, 120f, 2440f, 145f), color = Color(0xFF0284C7))
        )

        // End Platform
        platforms.add(
            LevelPlatform("plat_end", Rect(2600f, 420f, 3000f, 445f), color = Color(0xFF0369A1))
        )

        return platforms
    }
}

// Floating Text for damage/pickup feedback
data class FloatingText(
    var text: String,
    var x: Float,
    var y: Float,
    var color: Color,
    var life: Float = 1.0f,
    var maxLife: Float = 1.0f,
    var vy: Float = -40f
)

// Checkpoint state snapshot for procedural level continuation
data class CheckpointState(
    val cpX: Float,
    val cpY: Float,
    val energy: Float,
    val collectedCrystals: Set<String>,
    val collectedPowerUps: Set<String>
)

// Single Authoritative Game Engine & Physics Controller (Phase 15 Rule)
class ShadowHeroEngine {
    val player = ShadowHeroPlayer()

    // Current Stage State & Procedural Generator Integration
    var currentStage: Int = 1
    var currentLevel: GeneratedLevel = ShadowHeroLevelGenerator.generateStage(1)
    var activeCheckpoint: LevelCheckpoint? = null
    var savedCheckpointState: CheckpointState? = null
    val collectedCrystalIds = mutableSetOf<String>()
    val collectedPowerUpIds = mutableSetOf<String>()
    var isStageComplete: Boolean = false
    var stageTimeSeconds: Float = 0f
    var stageCountdownTimer: Float = 3.5f

    // Phase 5 Energy & Ability Systems
    var energy: Float = 100f
    var lowEnergyFeedbackTimer: Float = 0f
    var shieldActive: Boolean = false
    var shieldTimer: Float = 0f
    var shadowTimeActive: Boolean = false
    var shadowTimeTimer: Float = 0f
    var magnetActive: Boolean = false
    var magnetTimer: Float = 0f

    // Death & Rewarded Continue State
    var isGameOverDialogOpen: Boolean = false
    var hasUsedContinue: Boolean = false

    // Progression Metrics
    var totalEnergyUsedInRun: Float = 0f
    var totalPowerUpsCollectedInRun: Int = 0

    var footstepTimer: Float = 0f

    init {
        player.resetToSpawn(currentLevel.spawnX, currentLevel.spawnY)
        ShadowHeroAudioEngine.startBackgroundMusic(currentLevel.theme)
    }

    fun addFloatingText(text: String, x: Float, y: Float, color: Color) {
        floatingTexts.add(FloatingText(text = text, x = x, y = y, color = color))
    }

    fun loadStage(stageNumber: Int, customSeed: Long? = null) {
        currentStage = stageNumber
        currentLevel = ShadowHeroLevelGenerator.generateStage(stageNumber, customSeed)
        activeCheckpoint = null
        savedCheckpointState = null
        collectedCrystalIds.clear()
        collectedPowerUpIds.clear()
        isStageComplete = false
        isGameOverDialogOpen = false
        stageTimeSeconds = 0f
        stageCountdownTimer = 3.5f
        energy = MAX_ENERGY
        shieldActive = false
        shieldTimer = 0f
        shadowTimeActive = false
        shadowTimeTimer = 0f
        magnetActive = false
        magnetTimer = 0f
        player.resetToSpawn(currentLevel.spawnX, currentLevel.spawnY)
        ShadowHeroAudioEngine.startBackgroundMusic(currentLevel.theme)
    }

    fun restartCurrentStage() {
        activeCheckpoint = null
        savedCheckpointState = null
        collectedCrystalIds.clear()
        collectedPowerUpIds.clear()
        isStageComplete = false
        isGameOverDialogOpen = false
        stageTimeSeconds = 0f
        stageCountdownTimer = 3.5f
        energy = MAX_ENERGY
        shieldActive = false
        shieldTimer = 0f
        shadowTimeActive = false
        shadowTimeTimer = 0f
        magnetActive = false
        magnetTimer = 0f
        for (cp in currentLevel.checkpoints) cp.isActivated = false
        for (cr in currentLevel.crystals) cr.isCollected = false
        for (pu in currentLevel.powerUps) pu.isCollected = false
        player.resetToSpawn(currentLevel.spawnX, currentLevel.spawnY)
        ShadowHeroAudioEngine.startBackgroundMusic(currentLevel.theme)
    }

    fun resetToActiveCheckpointOrStart() {
        val cpState = savedCheckpointState
        val cp = activeCheckpoint
        if (cpState != null) {
            player.resetToSpawn(cpState.cpX, cpState.cpY)
            energy = cpState.energy
        } else if (cp != null) {
            player.resetToSpawn(cp.x + cp.width / 2f - player.width / 2f, cp.y - 10f)
            energy = MAX_ENERGY
        } else {
            player.resetToSpawn(currentLevel.spawnX, currentLevel.spawnY)
            energy = MAX_ENERGY
        }
        // Reset hazard states & enemies on respawn
        for (mSpike in currentLevel.movingSpikes) mSpike.reset()
        for (blade in currentLevel.blades) blade.reset()
        for (laser in currentLevel.lasers) laser.reset()
        for (enemy in currentLevel.enemies) enemy.reset()
        for (plat in currentLevel.platforms) plat.reset()
        enemyProjectiles.clear()
        ShadowHeroAudioEngine.startBackgroundMusic(currentLevel.theme)
    }

    // Camera Variables
    var cameraX: Float = 0f
    var cameraY: Float = 0f
    var cameraShake: Float = 0f

    // Visual Effects Arrays & Active Projectiles
    val particles = mutableListOf<HeroParticle>()
    val dashGhosts = mutableListOf<DashGhostFrame>()
    val enemyProjectiles = mutableListOf<EnemyProjectile>()
    val floatingTexts = mutableListOf<FloatingText>()

    // Physics & Energy Constants
    companion object {
        const val GRAVITY = 1900f             // px/s^2
        const val MAX_FALL_SPEED = 1000f      // px/s
        const val WALL_SLIDE_SPEED = 180f     // px/s max fall speed when sliding on a wall
        const val MOVE_ACCEL = 3800f          // px/s^2
        const val MAX_RUN_SPEED = 360f        // px/s
        const val GROUND_FRICTION = 2400f     // px/s^2
        const val AIR_CONTROL = 2600f         // px/s^2

        const val JUMP_VELOCITY = -650f       // Normal jump impulse
        const val DOUBLE_JUMP_VELOCITY = -600f// Double jump impulse
        const val WALL_JUMP_VX = 450f         // Horizontal push away from wall
        const val WALL_JUMP_VY = -620f        // Upward impulse on wall jump

        const val DASH_SPEED = 1100f          // Horizontal burst velocity
        const val DASH_DURATION = 0.16f       // seconds
        const val DASH_COOLDOWN = 0.75f       // seconds

        const val MAX_ENERGY = 100f
        const val ENERGY_REGEN_RATE = 15f     // energy per sec
        const val DASH_ENERGY_COST = 25f
    }

    // Input States (Updated continuously by touch controls)
    var inputLeft: Boolean = false
    var inputRight: Boolean = false

    fun triggerJump(): Boolean {
        if (player.isDead || player.isDashing || isStageComplete) return false

        // 1. Wall Jump Check
        if (!player.isGrounded && (player.isOnLeftWall || player.isOnRightWall)) {
            val wallDir = if (player.isOnLeftWall) 1f else -1f
            player.vx = wallDir * WALL_JUMP_VX
            player.vy = WALL_JUMP_VY
            player.facingRight = (wallDir > 0)
            player.jumpCount = 1
            player.animState = PlayerAnimState.WALL_JUMP
            spawnWallParticles(if (player.isOnLeftWall) player.x else player.x + player.width, player.y + player.height / 2f)
            ShadowHeroAudioEngine.playWallJump()
            return true
        }

        // 2. Normal Ground Jump
        if (player.isGrounded) {
            player.vy = JUMP_VELOCITY
            player.isGrounded = false
            player.jumpCount = 1
            player.animState = PlayerAnimState.JUMP
            spawnJumpDust(player.x + player.width / 2f, player.y + player.height)
            ShadowHeroAudioEngine.playJump()
            return true
        }

        // 3. Double Jump (In Air)
        if (!player.isGrounded && player.jumpCount < 2) {
            player.vy = DOUBLE_JUMP_VELOCITY
            player.jumpCount = 2
            player.animState = PlayerAnimState.DOUBLE_JUMP
            spawnDoubleJumpAura(player.x + player.width / 2f, player.y + player.height / 2f)
            ShadowHeroAudioEngine.playDoubleJump()
            return true
        }

        return false
    }

    fun triggerDash(): Boolean {
        if (player.isDead || player.isDashing || player.dashCooldownTimer > 0f || isStageComplete) return false

        // Energy Check for Dash (Phase 5)
        if (energy < DASH_ENERGY_COST) {
            lowEnergyFeedbackTimer = 0.8f
            addFloatingText("LOW ENERGY!", player.x + player.width / 2f, player.y - 15f, Color(0xFFEF4444))
            return false
        }

        energy -= DASH_ENERGY_COST
        totalEnergyUsedInRun += DASH_ENERGY_COST

        val dir = when {
            inputRight -> 1f
            inputLeft -> -1f
            else -> if (player.facingRight) 1f else -1f
        }

        player.isDashing = true
        player.dashTimer = DASH_DURATION
        player.dashCooldownTimer = DASH_COOLDOWN
        player.dashDirection = dir
        player.facingRight = (dir > 0f)
        player.vx = dir * DASH_SPEED
        player.vy = 0f
        player.animState = PlayerAnimState.DASH

        cameraShake = 12f
        ShadowHeroAudioEngine.playDash()
        return true
    }

    // Main 60 FPS Engine Tick Logic
    fun update(dt: Float, viewportWidth: Float, viewportHeight: Float) {
        val safeDt = dt.coerceIn(0.001f, 0.05f)

        if (player.isDead || isStageComplete) {
            updateParticlesAndGhosts(safeDt)
            return
        }

        if (stageCountdownTimer > 0f) {
            stageCountdownTimer -= safeDt
            updateParticlesAndGhosts(safeDt)
            updateCamera(viewportWidth, viewportHeight, safeDt)
            return
        }

        stageTimeSeconds += safeDt

        // Time Dilation when Shadow Time power-up is active (Phase 5)
        val timeDilation = if (shadowTimeActive) 0.45f else 1.0f

        // Energy Regeneration Logic
        if (!player.isDashing && energy < MAX_ENERGY) {
            energy = min(MAX_ENERGY, energy + ENERGY_REGEN_RATE * safeDt)
        }

        // Timers for Power-Ups
        if (lowEnergyFeedbackTimer > 0f) lowEnergyFeedbackTimer = max(0f, lowEnergyFeedbackTimer - safeDt)

        if (shieldActive && shieldTimer > 0f) {
            shieldTimer -= safeDt
            if (shieldTimer <= 0f) shieldActive = false
        }

        if (shadowTimeActive && shadowTimeTimer > 0f) {
            shadowTimeTimer -= safeDt
            if (shadowTimeTimer <= 0f) shadowTimeActive = false
        }

        if (magnetActive && magnetTimer > 0f) {
            magnetTimer -= safeDt
            if (magnetTimer <= 0f) magnetActive = false
        }

        if (player.invincibleTimer > 0f) {
            player.invincibleTimer = max(0f, player.invincibleTimer - safeDt)
        }

        // Magnet Crystal Attraction Logic (Phase 5)
        if (magnetActive) {
            val px = player.x + player.width / 2f
            val py = player.y + player.height / 2f
            for (cr in currentLevel.crystals) {
                if (!cr.isCollected) {
                    val dx = px - cr.x
                    val dy = py - cr.y
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    if (dist < 420f && dist > 2f) {
                        cr.x += (dx / dist) * 360f * safeDt
                        cr.y += (dy / dist) * 360f * safeDt
                    }
                }
            }
        }

        // 1. Update Dash Timers & Ghost Trails
        if (player.dashCooldownTimer > 0f) {
            player.dashCooldownTimer = max(0f, player.dashCooldownTimer - safeDt)
        }

        if (player.isDashing) {
            player.dashTimer -= safeDt
            player.vx = player.dashDirection * DASH_SPEED
            player.vy = 0f

            // Add ghost trail frame
            dashGhosts.add(
                DashGhostFrame(
                    x = player.x,
                    y = player.y,
                    facingRight = player.facingRight,
                    isDashing = true
                )
            )

            // Spawn purple dash sparks
            spawnDashSpark(player.x + player.width / 2f, player.y + player.height / 2f)

            if (player.dashTimer <= 0f) {
                player.isDashing = false
                player.vx = player.dashDirection * MAX_RUN_SPEED * 0.8f
            }
        } else {
            // 2. Normal Horizontal Movement Physics
            val moveDir = when {
                inputLeft && !inputRight -> -1f
                inputRight && !inputLeft -> 1f
                else -> 0f
            }

            if (moveDir != 0f) {
                player.facingRight = (moveDir > 0f)
                val accel = if (player.isGrounded) MOVE_ACCEL else AIR_CONTROL
                player.vx += moveDir * accel * safeDt
                player.vx = player.vx.coerceIn(-MAX_RUN_SPEED, MAX_RUN_SPEED)
            } else {
                // Ground Friction / Air Drag
                if (player.isGrounded) {
                    if (player.vx > 0) {
                        player.vx = max(0f, player.vx - GROUND_FRICTION * safeDt)
                    } else if (player.vx < 0) {
                        player.vx = min(0f, player.vx + GROUND_FRICTION * safeDt)
                    }
                } else {
                    player.vx *= 0.96f
                }
            }

            // 3. Gravity & Wall Slide
            val isAgainstWall = (player.isOnLeftWall && inputLeft) || (player.isOnRightWall && inputRight)
            val isWallSliding = !player.isGrounded && isAgainstWall && player.vy > 0f

            if (isWallSliding) {
                player.vy += GRAVITY * 0.3f * safeDt
                player.vy = min(WALL_SLIDE_SPEED, player.vy)
                player.animState = PlayerAnimState.WALL_SLIDE
                player.jumpCount = 0 // Wall slide refreshes double jump

                // Wall dust
                if (Math.random() < 0.3) {
                    val wallX = if (player.isOnLeftWall) player.x else player.x + player.width
                    spawnWallParticles(wallX, player.y + player.height * 0.6f)
                }
            } else {
                player.vy += GRAVITY * safeDt
                player.vy = min(MAX_FALL_SPEED, player.vy)
            }
        }

        // 4. Update Landing Squash Timer
        if (player.landingSquashTimer > 0f) {
            player.landingSquashTimer = max(0f, player.landingSquashTimer - safeDt)
        }

        // 4b. Update Dynamic Level Entities & Obstacles (Slowing down when Shadow Time active)
        val entityDt = safeDt * timeDilation
        for (mSpike in currentLevel.movingSpikes) mSpike.update(entityDt)
        for (blade in currentLevel.blades) blade.update(entityDt)
        for (laser in currentLevel.lasers) laser.update(entityDt)
        for (plat in currentLevel.platforms) plat.update(safeDt)
        for (enemy in currentLevel.enemies) enemy.update(entityDt, player.x, player.y, enemyProjectiles)

        val projIter = enemyProjectiles.iterator()
        while (projIter.hasNext()) {
            val proj = projIter.next()
            if (!proj.update(entityDt)) {
                projIter.remove()
            }
        }

        // 5. Integrate & Perform Collision Resolution (with spatial culling)
        moveAndResolveCollisions(safeDt, viewportWidth)

        // 5a. HAZARD & ENEMY DAMAGE / DEATH CHECKS
        val hitStaticSpike = currentLevel.spikes.any { player.bounds.overlaps(it.bounds) }
        val hitMovingSpike = currentLevel.movingSpikes.any { player.bounds.overlaps(it.bounds) }
        val hitBlade = currentLevel.blades.any { player.bounds.overlaps(it.bounds) }
        val hitLaser = currentLevel.lasers.any { it.state == LaserState.ACTIVE && player.bounds.overlaps(it.beamBounds) }
        val hitHazard = currentLevel.hazards.any { player.bounds.overlaps(it.bounds) }
        val hitEnemy = currentLevel.enemies.any { player.bounds.overlaps(it.bounds) }
        val hitProjectile = enemyProjectiles.any { player.bounds.overlaps(it.bounds) }

        if (hitStaticSpike || hitMovingSpike || hitBlade || hitLaser || hitHazard || hitEnemy || hitProjectile) {
            if (player.invincibleTimer <= 0f) {
                if (shieldActive) {
                    shieldActive = false
                    shieldTimer = 0f
                    player.invincibleTimer = 1.2f
                    spawnShieldBreakBurst(player.x + player.width / 2f, player.y + player.height / 2f)
                    addFloatingText("SHIELD BROKEN!", player.x + player.width / 2f, player.y - 20f, Color(0xFFC084FC))
                    cameraShake = 12f
                    ShadowHeroAudioEngine.playShieldBreak()
                } else {
                    triggerPlayerDeath()
                    return
                }
            }
        }

        // 5b. Checkpoint Interactions (Phase 5)
        for (cp in currentLevel.checkpoints) {
            if (!cp.isActivated && player.bounds.overlaps(cp.bounds)) {
                cp.isActivated = true
                activeCheckpoint = cp
                savedCheckpointState = CheckpointState(
                    cpX = cp.x + cp.width / 2f - player.width / 2f,
                    cpY = cp.y - 10f,
                    energy = energy,
                    collectedCrystals = collectedCrystalIds.toSet(),
                    collectedPowerUps = collectedPowerUpIds.toSet()
                )
                spawnCheckpointAura(cp.x + cp.width / 2f, cp.y + cp.height / 2f)
                addFloatingText("CHECKPOINT ACTIVATED", cp.x + cp.width / 2f, cp.y - 25f, Color(0xFF38BDF8))
                cameraShake = 8f
                ShadowHeroAudioEngine.playCheckpointActivate()
            }
        }

        // 5c. Crystal Collectibles Interactions
        for (crystal in currentLevel.crystals) {
            if (!crystal.isCollected && player.bounds.overlaps(crystal.bounds)) {
                crystal.isCollected = true
                collectedCrystalIds.add(crystal.id)
                spawnCrystalSparkles(crystal.x, crystal.y)
                addFloatingText("+1 💎", crystal.x, crystal.y - 15f, Color(0xFFFACC15))
                ShadowHeroAudioEngine.playCrystalCollect()
            }
        }

        // 5d. Power-Up Pickups Interactions (Phase 5)
        for (pu in currentLevel.powerUps) {
            if (!pu.isCollected && player.bounds.overlaps(pu.bounds)) {
                pu.isCollected = true
                collectedPowerUpIds.add(pu.id)
                totalPowerUpsCollectedInRun++
                spawnPowerUpBurst(pu.x, pu.y, pu.type)
                ShadowHeroAudioEngine.playPowerUpCollect()
                when (pu.type) {
                    PowerUpType.SHIELD -> {
                        shieldActive = true
                        shieldTimer = 12f
                        addFloatingText("SHIELD ACTIVE!", pu.x, pu.y - 20f, Color(0xFFC084FC))
                    }
                    PowerUpType.SHADOW_TIME -> {
                        shadowTimeActive = true
                        shadowTimeTimer = 7f
                        addFloatingText("SHADOW TIME!", pu.x, pu.y - 20f, Color(0xFF38BDF8))
                    }
                    PowerUpType.CRYSTAL_MAGNET -> {
                        magnetActive = true
                        magnetTimer = 10f
                        addFloatingText("CRYSTAL MAGNET!", pu.x, pu.y - 20f, Color(0xFFFACC15))
                    }
                    PowerUpType.ENERGY_BOOST -> {
                        energy = min(MAX_ENERGY, energy + 50f)
                        addFloatingText("+50 ENERGY!", pu.x, pu.y - 20f, Color(0xFF4ADE80))
                    }
                    PowerUpType.DASH_RECHARGE -> {
                        player.dashCooldownTimer = 0f
                        energy = MAX_ENERGY
                        addFloatingText("DASH RECHARGED!", pu.x, pu.y - 20f, Color(0xFFE879F9))
                    }
                }
            }
        }

        // 5e. Exit Portal Interaction
        if (!isStageComplete && player.bounds.overlaps(currentLevel.exitPortal.bounds)) {
            isStageComplete = true
            spawnPortalCelebration(currentLevel.exitPortal.x, currentLevel.exitPortal.y)
            cameraShake = 16f
            ShadowHeroAudioEngine.playStageComplete()
        }

        // 6. Check Pit Fall / Death Condition
        if (player.y > currentLevel.levelHeight + 100f) {
            triggerPlayerDeath()
            return
        }

        // Footsteps SFX
        if (player.isGrounded && abs(player.vx) > 30f && !player.isDashing) {
            footstepTimer -= safeDt
            if (footstepTimer <= 0f) {
                ShadowHeroAudioEngine.playFootstep()
                footstepTimer = 0.28f
            }
        }

        // 7. Update Animation State
        updateAnimationState(safeDt)

        // 8. Update Camera Follow & Shake
        updateCamera(viewportWidth, viewportHeight, safeDt)

        // 9. Update Particles, Ghosts & Floating Texts
        updateParticlesAndGhosts(safeDt)
    }

    fun performRewardedContinue() {
        hasUsedContinue = true
        isGameOverDialogOpen = false

        val cpState = savedCheckpointState
        val cp = activeCheckpoint
        if (cpState != null) {
            player.resetToSpawn(cpState.cpX, cpState.cpY)
            energy = cpState.energy
        } else if (cp != null) {
            player.resetToSpawn(cp.x + cp.width / 2f - player.width / 2f, cp.y - 10f)
            energy = MAX_ENERGY
        } else {
            player.resetToSpawn(currentLevel.spawnX, currentLevel.spawnY)
            energy = MAX_ENERGY
        }

        // Grant 3-second temporary shield
        shieldActive = true
        shieldTimer = 3f
        player.invincibleTimer = 3f

        for (mSpike in currentLevel.movingSpikes) mSpike.reset()
        for (blade in currentLevel.blades) blade.reset()
        for (laser in currentLevel.lasers) laser.reset()
        for (enemy in currentLevel.enemies) enemy.reset()
        for (plat in currentLevel.platforms) plat.reset()
        enemyProjectiles.clear()

        player.isDead = false
        addFloatingText("SECOND CHANCE!", player.x + player.width / 2f, player.y - 20f, Color(0xFFFFD700))
        ShadowHeroAudioEngine.playSecondChance()
    }

    private fun moveAndResolveCollisions(dt: Float, viewportWidth: Float) {
        val wasGrounded = player.isGrounded

        // Spatial culling range for 60 FPS performance
        val visibleMinX = player.x - viewportWidth - 400f
        val visibleMaxX = player.x + viewportWidth + 400f

        val activePlatforms = currentLevel.platforms.filter { plat ->
            plat.bounds.right >= visibleMinX && plat.bounds.left <= visibleMaxX
        }

        // Move X first
        player.x += player.vx * dt
        player.isOnLeftWall = false
        player.isOnRightWall = false

        var pBounds = player.bounds
        for (plat in activePlatforms) {
            if (pBounds.overlaps(plat.bounds)) {
                if (player.vx > 0f) {
                    player.x = plat.bounds.left - player.width
                    player.isOnRightWall = plat.isWall
                } else if (player.vx < 0f) {
                    player.x = plat.bounds.right
                    player.isOnLeftWall = plat.isWall
                }
                player.vx = 0f
                pBounds = player.bounds
            }
        }

        // Move Y next
        player.y += player.vy * dt
        player.isGrounded = false
        pBounds = player.bounds

        for (plat in activePlatforms) {
            if (pBounds.overlaps(plat.bounds)) {
                if (player.vy > 0f) {
                    // Falling down onto platform
                    player.y = plat.bounds.top - player.height
                    player.vy = 0f
                    player.isGrounded = true
                    player.jumpCount = 0

                    if (!wasGrounded) {
                        plat.onPlayerLand()
                        player.landingSquashTimer = 0.12f
                        spawnLandingDust(player.x + player.width / 2f, player.y + player.height)
                        cameraShake = 4f
                    }
                } else if (player.vy < 0f) {
                    // Hitting head on ceiling
                    player.y = plat.bounds.bottom
                    player.vy = 0f
                }
                pBounds = player.bounds
            }
        }
    }

    private fun updateAnimationState(dt: Float) {
        if (player.isDashing) return

        if (player.isGrounded) {
            if (abs(player.vx) > 20f) {
                player.animState = PlayerAnimState.RUN
                player.runCycleTimer += dt * 12f
            } else {
                player.animState = if (player.landingSquashTimer > 0f) PlayerAnimState.LAND else PlayerAnimState.IDLE
                player.runCycleTimer = 0f
            }
        } else {
            if (player.animState != PlayerAnimState.WALL_SLIDE && player.animState != PlayerAnimState.WALL_JUMP) {
                player.animState = if (player.vy < 0f) {
                    if (player.jumpCount >= 2) PlayerAnimState.DOUBLE_JUMP else PlayerAnimState.JUMP
                } else {
                    PlayerAnimState.FALL
                }
            }
        }
    }

    private fun updateCamera(vw: Float, vh: Float, dt: Float) {
        if (vw <= 0f || vh <= 0f) return

        // Smooth camera lerp focusing ahead of movement direction
        val leadOffset = if (player.facingRight) 70f else -70f
        val targetCamX = (player.x + player.width / 2f + leadOffset - vw / 2f)
            .coerceIn(0f, max(0f, currentLevel.levelWidth - vw))

        val targetCamY = (player.y + player.height / 2f - vh / 2f)
            .coerceIn(-200f, max(0f, currentLevel.levelHeight - vh))

        cameraX += (targetCamX - cameraX) * 0.12f
        cameraY += (targetCamY - cameraY) * 0.12f

        // Camera Shake Decay
        if (cameraShake > 0f) {
            cameraShake = max(0f, cameraShake - dt * 25f)
        }
    }

    private fun updateParticlesAndGhosts(dt: Float) {
        // Update particles
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.currentLife += dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.alpha = (1f - (p.currentLife / p.maxLife)).coerceIn(0f, 1f)
            if (p.currentLife >= p.maxLife) {
                iterator.remove()
            }
        }

        // Update dash ghost frames
        val ghostIter = dashGhosts.iterator()
        while (ghostIter.hasNext()) {
            val g = ghostIter.next()
            g.alpha -= dt * 4f
            if (g.alpha <= 0f) {
                ghostIter.remove()
            }
        }

        // Update floating texts
        val textIter = floatingTexts.iterator()
        while (textIter.hasNext()) {
            val ft = textIter.next()
            ft.life -= dt
            ft.y += ft.vy * dt
            if (ft.life <= 0f) textIter.remove()
        }
    }

    // Particle Spawners
    private fun spawnJumpDust(cx: Float, cy: Float) {
        for (i in 0..8) {
            particles.add(
                HeroParticle(
                    x = cx + (Math.random().toFloat() - 0.5f) * 20f,
                    y = cy,
                    vx = (Math.random().toFloat() - 0.5f) * 120f,
                    vy = -Math.random().toFloat() * 100f,
                    radius = 3f + Math.random().toFloat() * 4f,
                    color = Color(0xFFA855F7),
                    alpha = 0.8f,
                    maxLife = 0.3f
                )
            )
        }
    }

    private fun spawnDoubleJumpAura(cx: Float, cy: Float) {
        for (i in 0..12) {
            val angle = Math.random() * Math.PI * 2
            val speed = 150f + Math.random().toFloat() * 100f
            particles.add(
                HeroParticle(
                    x = cx,
                    y = cy,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = Math.sin(angle).toFloat() * speed,
                    radius = 4f + Math.random().toFloat() * 3f,
                    color = Color(0xFF22D3EE),
                    alpha = 0.9f,
                    maxLife = 0.25f
                )
            )
        }
    }

    private fun spawnWallParticles(wx: Float, wy: Float) {
        for (i in 0..3) {
            particles.add(
                HeroParticle(
                    x = wx,
                    y = wy + (Math.random().toFloat() - 0.5f) * 16f,
                    vx = (Math.random().toFloat() - 0.5f) * 60f,
                    vy = -Math.random().toFloat() * 80f,
                    radius = 2f + Math.random().toFloat() * 3f,
                    color = Color(0xFFC084FC),
                    alpha = 0.8f,
                    maxLife = 0.2f
                )
            )
        }
    }

    private fun spawnDashSpark(cx: Float, cy: Float) {
        for (i in 0..2) {
            particles.add(
                HeroParticle(
                    x = cx + (Math.random().toFloat() - 0.5f) * 10f,
                    y = cy + (Math.random().toFloat() - 0.5f) * 10f,
                    vx = -player.dashDirection * (100f + Math.random().toFloat() * 150f),
                    vy = (Math.random().toFloat() - 0.5f) * 80f,
                    radius = 3f + Math.random().toFloat() * 4f,
                    color = if (Math.random() < 0.5) Color(0xFFA855F7) else Color(0xFF38BDF8),
                    alpha = 0.9f,
                    maxLife = 0.2f
                )
            )
        }
    }

    private fun spawnLandingDust(cx: Float, cy: Float) {
        for (i in 0..10) {
            particles.add(
                HeroParticle(
                    x = cx + (Math.random().toFloat() - 0.5f) * 30f,
                    y = cy,
                    vx = (Math.random().toFloat() - 0.5f) * 200f,
                    vy = -Math.random().toFloat() * 60f,
                    radius = 3f + Math.random().toFloat() * 3f,
                    color = Color(0xFF9333EA),
                    alpha = 0.7f,
                    maxLife = 0.25f
                )
            )
        }
    }

    private fun spawnCheckpointAura(cx: Float, cy: Float) {
        for (i in 0..16) {
            val angle = Math.random() * Math.PI * 2
            val speed = 80f + Math.random().toFloat() * 120f
            particles.add(
                HeroParticle(
                    x = cx,
                    y = cy,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = Math.sin(angle).toFloat() * speed,
                    radius = 4f + Math.random().toFloat() * 4f,
                    color = Color(0xFF38BDF8),
                    alpha = 0.9f,
                    maxLife = 0.4f
                )
            )
        }
    }

    private fun spawnCrystalSparkles(cx: Float, cy: Float) {
        for (i in 0..12) {
            val angle = Math.random() * Math.PI * 2
            val speed = 100f + Math.random().toFloat() * 150f
            particles.add(
                HeroParticle(
                    x = cx,
                    y = cy,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = Math.sin(angle).toFloat() * speed,
                    radius = 3f + Math.random().toFloat() * 3f,
                    color = Color(0xFFFACC15),
                    alpha = 1f,
                    maxLife = 0.35f
                )
            )
        }
    }

    private fun spawnPowerUpBurst(cx: Float, cy: Float, type: PowerUpType) {
        val color = when (type) {
            PowerUpType.SHIELD -> Color(0xFFC084FC)
            PowerUpType.SHADOW_TIME -> Color(0xFF38BDF8)
            PowerUpType.CRYSTAL_MAGNET -> Color(0xFFFACC15)
            PowerUpType.ENERGY_BOOST -> Color(0xFF4ADE80)
            PowerUpType.DASH_RECHARGE -> Color(0xFFE879F9)
        }
        for (i in 0..16) {
            val angle = Math.random() * Math.PI * 2
            val speed = 120f + Math.random().toFloat() * 160f
            particles.add(
                HeroParticle(
                    x = cx,
                    y = cy,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = Math.sin(angle).toFloat() * speed,
                    radius = 4f + Math.random().toFloat() * 4f,
                    color = color,
                    alpha = 1f,
                    maxLife = 0.4f
                )
            )
        }
    }

    private fun spawnShieldBreakBurst(cx: Float, cy: Float) {
        for (i in 0..20) {
            val angle = Math.random() * Math.PI * 2
            val speed = 160f + Math.random().toFloat() * 200f
            particles.add(
                HeroParticle(
                    x = cx,
                    y = cy,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = Math.sin(angle).toFloat() * speed,
                    radius = 4f + Math.random().toFloat() * 5f,
                    color = Color(0xFFC084FC),
                    alpha = 1f,
                    maxLife = 0.45f
                )
            )
        }
    }

    fun triggerPlayerDeath() {
        if (player.isDead) return
        player.isDead = true
        player.animState = PlayerAnimState.DEATH
        isGameOverDialogOpen = true
        spawnDeathExplosion(player.x + player.width / 2f, player.y + player.height / 2f)
        cameraShake = 18f
        ShadowHeroAudioEngine.playPlayerDeath()
    }

    private fun spawnDeathExplosion(cx: Float, cy: Float) {
        for (i in 0..25) {
            val angle = Math.random() * Math.PI * 2
            val speed = 140f + Math.random().toFloat() * 220f
            particles.add(
                HeroParticle(
                    x = cx,
                    y = cy,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = Math.sin(angle).toFloat() * speed,
                    radius = 4f + Math.random().toFloat() * 5f,
                    color = if (i % 2 == 0) Color(0xFFEF4444) else Color(0xFFA855F7),
                    alpha = 1f,
                    maxLife = 0.45f
                )
            )
        }
    }

    private fun spawnPortalCelebration(cx: Float, cy: Float) {
        for (i in 0..30) {
            val angle = Math.random() * Math.PI * 2
            val speed = 120f + Math.random().toFloat() * 250f
            particles.add(
                HeroParticle(
                    x = cx,
                    y = cy,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = Math.sin(angle).toFloat() * speed,
                    radius = 5f + Math.random().toFloat() * 5f,
                    color = if (i % 2 == 0) Color(0xFFC084FC) else Color(0xFF38BDF8),
                    alpha = 1f,
                    maxLife = 0.6f
                )
            )
        }
    }
}
