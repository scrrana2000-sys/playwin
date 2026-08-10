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
    HURT,
    DEATH,
    RESPAWN,
    ATTACK
}

enum class ShadowHeroGameState {
    PLAYING,
    PAUSED,
    PLAYER_DIED,
    AUTO_RESPAWNING,
    DEATH_DIALOG,
    REWARDED_AD_LOADING,
    REWARDED_AD_PLAYING,
    RESPAWNING_FROM_REWARD,
    RESTARTING,
    QUITTING
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

// Purple combat slash effect for Shadow Strike
data class SlashEffect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val facingRight: Boolean,
    val isComboFinisher: Boolean,
    var lifeTime: Float = 0.15f,
    val maxLife: Float = 0.15f
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
    var respawnTimer: Float = 0.6f
    var deathTimer: Float = 0f

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
        animState = PlayerAnimState.RESPAWN
        landingSquashTimer = 0f
        isDead = false
        respawnTimer = 0.6f
        deathTimer = 0f
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
    val levelSeed: Long,
    val stageId: Int,
    val cpX: Float,
    val cpY: Float,
    val cameraX: Float,
    val cameraY: Float,
    val energy: Float,
    val collectedCrystals: Set<String>,
    val collectedPowerUps: Set<String>,
    val requiredCrystals: Int,
    val levelProgressX: Float
)

// Single Authoritative Game Engine & Physics Controller (Phase 15 Rule)
class ShadowHeroEngine {
    val player = ShadowHeroPlayer()

    // Intelligent Infinite World System Architecture (Phase 11A)
    val difficultyDirector = ShadowHeroDifficultyDirector()
    var chunkManager: ShadowHeroChunkManager = ShadowHeroChunkManager(1, difficultyDirector = difficultyDirector)
    val eventDirector = ShadowHeroEventDirector()
    var isDebugOverlayVisible: Boolean = false

    // Current Stage State & Procedural Generator Integration
    var appContext: android.content.Context? = null
    var levelCompletionManager: LevelCompletionManager = LevelCompletionManager(
        stageNumber = 1,
        requiredCrystals = 10
    )
    var currentStage: Int = 1
    var currentLevel: GeneratedLevel = ShadowHeroLevelGenerator.generateStage(1)
    var activeCheckpoint: LevelCheckpoint? = null
    var savedCheckpointState: CheckpointState? = null
    val collectedCrystalIds = mutableSetOf<String>()
    val collectedPowerUpIds = mutableSetOf<String>()
    var isStageComplete: Boolean = false
    var stageTimeSeconds: Float = 0f
    var stageCountdownTimer: Float = 3.5f

    // Phase 5 & 15 Energy & Power-Up Systems
    var energy: Float = 100f
    var lowEnergyFeedbackTimer: Float = 0f
    var shieldActive: Boolean = false
    var shieldTimer: Float = 0f
    var shadowTimeActive: Boolean = false
    var shadowTimeTimer: Float = 0f
    var magnetActive: Boolean = false
    var magnetTimer: Float = 0f
    var dashBoostActive: Boolean = false
    var dashBoostTimer: Float = 0f
    var doubleCrystalActive: Boolean = false
    var doubleCrystalTimer: Float = 0f

    // Death & Rewarded Continue State
    var isGameOverDialogOpen: Boolean = false
    var hasUsedContinue: Boolean = false
    var lives: Int = 3
    val maxLives: Int = 3
    var rewardContinueUsed: Boolean = false
    var isProcessingDeath: Boolean = false
    var gameState: ShadowHeroGameState = ShadowHeroGameState.PLAYING
    var adErrorMessage: String? = null

    // Progression Metrics
    var totalEnergyUsedInRun: Float = 0f
    var totalPowerUpsCollectedInRun: Int = 0

    var footstepTimer: Float = 0f

    fun updateLevelFromChunkManager() {
        val diffParams = difficultyDirector.getDifficultyParameters(currentStage)
        for (enemy in chunkManager.activeEnemies) {
            enemy.initStats(currentStage)
        }
        currentLevel = GeneratedLevel(
            seed = chunkManager.stageSeed,
            stageNumber = currentStage,
            difficultyName = "Stage $currentStage (${String.format("%.1f", diffParams.effectiveDifficulty)})",
            theme = chunkManager.theme,
            levelWidth = (chunkManager.currentChunkIndex + 4) * 1500f,
            levelHeight = 1200f,
            spawnX = 100f,
            spawnY = 590f,
            platforms = chunkManager.activePlatforms.toList(),
            checkpoints = chunkManager.activeCheckpoints.toList(),
            crystals = chunkManager.activeCrystals.toList(),
            powerUps = chunkManager.activePowerUps.toList(),
            exitPortal = chunkManager.stageExitPortal ?: LevelExitPortal(x = (chunkManager.targetStageChunkLength * 1500f), y = 590f),
            spikes = chunkManager.activeSpikes.toList(),
            movingSpikes = chunkManager.activeMovingSpikes.toList(),
            blades = chunkManager.activeBlades.toList(),
            lasers = chunkManager.activeLasers.toList(),
            hazards = chunkManager.activeHazards.toList(),
            enemies = chunkManager.activeEnemies.toList(),
            patternSequence = listOf("ChunkStreaming"),
            generationTimeMs = chunkManager.lastGenerationTimeMs,
            validationAttempts = 1
        )
    }

    fun addFloatingText(text: String, x: Float, y: Float, color: Color) {
        floatingTexts.add(FloatingText(text = text, x = x, y = y, color = color))
    }

    fun loadStage(stageNumber: Int, customSeed: Long? = null) {
        currentStage = stageNumber.coerceAtLeast(1)
        val reqCrystals = minOf(10 + (currentStage - 1) * 2, 25)
        levelCompletionManager = LevelCompletionManager(
            stageNumber = currentStage,
            requiredCrystals = reqCrystals
        )

        val targetSeed = customSeed ?: ShadowHeroLevelGenerator.stageToSeed(currentStage)
        try {
            chunkManager = ShadowHeroChunkManager(
                stageNumber = currentStage,
                stageSeed = targetSeed,
                difficultyDirector = difficultyDirector
            )
            chunkManager.updatePlayerPosition(0f)
            updateLevelFromChunkManager()
        } catch (e: Exception) {
            // Failsafe level generation with safe fallback seed
            val safeSeed = 100010007L + currentStage * 314159L
            chunkManager = ShadowHeroChunkManager(
                stageNumber = currentStage,
                stageSeed = safeSeed,
                difficultyDirector = difficultyDirector
            )
            chunkManager.updatePlayerPosition(0f)
            updateLevelFromChunkManager()
        }

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
        dashBoostActive = false
        dashBoostTimer = 0f
        doubleCrystalActive = false
        doubleCrystalTimer = 0f
        health = 3
        lives = 3
        rewardContinueUsed = false
        isProcessingDeath = false
        gameState = ShadowHeroGameState.PLAYING
        adErrorMessage = null
        comboStep = 0
        comboTimer = 0f
        attackCooldownTimer = 0f
        attackAnimTimer = 0f
        activeSlashes.clear()
        player.resetToSpawn(100f, 590f)
        pendingCameraSnap = true
        ShadowHeroAudioEngine.startBackgroundMusic(currentLevel.theme)
    }

    fun restartCurrentStage() {
        val reqCrystals = minOf(10 + (currentStage - 1) * 2, 25)
        levelCompletionManager = LevelCompletionManager(
            stageNumber = currentStage,
            requiredCrystals = reqCrystals
        )
        chunkManager = ShadowHeroChunkManager(
            stageNumber = currentStage,
            stageSeed = currentLevel.seed,
            difficultyDirector = difficultyDirector
        )
        chunkManager.updatePlayerPosition(0f)
        updateLevelFromChunkManager()

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
        dashBoostActive = false
        dashBoostTimer = 0f
        doubleCrystalActive = false
        doubleCrystalTimer = 0f
        health = 3
        lives = 3
        rewardContinueUsed = false
        isProcessingDeath = false
        gameState = ShadowHeroGameState.PLAYING
        adErrorMessage = null
        comboStep = 0
        comboTimer = 0f
        attackCooldownTimer = 0f
        attackAnimTimer = 0f
        activeSlashes.clear()
        player.resetToSpawn(currentLevel.spawnX, currentLevel.spawnY)
        pendingCameraSnap = true
        ShadowHeroAudioEngine.startBackgroundMusic(currentLevel.theme)
    }

    fun validateCheckpointPosition(px: Float, py: Float): Pair<Float, Float> {
        var targetX = px
        var targetY = py
        
        // 1. Ensure within valid level bounds
        if (targetX < 50f) targetX = 100f
        val maxLevelWidth = currentLevel.levelWidth
        if (targetX > maxLevelWidth - 100f) targetX = maxLevelWidth - 100f
        
        val boundary = calculateDynamicDeathBoundary()
        if (targetY > boundary - 50f) {
            targetY = boundary - 120f
        }
        if (targetY < 50f) {
            targetY = 200f
        }
        
        // 2. Check if overlapping with spikes/obstacles/enemies.
        val pRect = androidx.compose.ui.geometry.Rect(targetX, targetY, targetX + player.width, targetY + player.height)
        var hasConflict = false
        
        // check spikes & hazards
        for (spike in currentLevel.spikes) {
            if (pRect.overlaps(spike.bounds)) { hasConflict = true; break }
        }
        for (mSpike in currentLevel.movingSpikes) {
            if (pRect.overlaps(mSpike.bounds)) { hasConflict = true; break }
        }
        for (blade in currentLevel.blades) {
            if (pRect.overlaps(blade.bounds)) { hasConflict = true; break }
        }
        for (laser in currentLevel.lasers) {
            if (pRect.overlaps(laser.beamBounds)) { hasConflict = true; break }
        }
        for (hazard in currentLevel.hazards) {
            if (pRect.overlaps(hazard.bounds)) { hasConflict = true; break }
        }
        
        if (hasConflict) {
            // Find nearest non-wall safe platform and spawn slightly above it
            val nearestPlatform = currentLevel.platforms.filter { !it.isWall && !it.isOneWay }.minByOrNull { 
                val dx = (it.bounds.left + it.bounds.width / 2) - px
                val dy = it.bounds.top - py
                dx * dx + dy * dy
            }
            if (nearestPlatform != null) {
                targetX = nearestPlatform.bounds.left + nearestPlatform.bounds.width / 2f - player.width / 2f
                targetY = nearestPlatform.bounds.top - player.height - 10f
            } else {
                targetX = currentLevel.spawnX
                targetY = currentLevel.spawnY
            }
        }
        
        return Pair(targetX, targetY)
    }

    fun resetToActiveCheckpointOrStart() {
        val cpState = savedCheckpointState
        val cp = activeCheckpoint
        if (cpState != null) {
            val safePos = validateCheckpointPosition(cpState.cpX, cpState.cpY)
            player.resetToSpawn(safePos.first, safePos.second)
            energy = cpState.energy
        } else if (cp != null) {
            val safePos = validateCheckpointPosition(cp.x + cp.width / 2f - player.width / 2f, cp.y - 10f)
            player.resetToSpawn(safePos.first, safePos.second)
            energy = MAX_ENERGY
        } else {
            player.resetToSpawn(currentLevel.spawnX, currentLevel.spawnY)
            energy = MAX_ENERGY
        }
        health = maxHealth
        comboStep = 0
        comboTimer = 0f
        attackCooldownTimer = 0f
        attackAnimTimer = 0f
        activeSlashes.clear()
        // Reset hazard states & enemies on respawn
        for (mSpike in currentLevel.movingSpikes) mSpike.reset()
        for (blade in currentLevel.blades) blade.reset()
        for (laser in currentLevel.lasers) laser.reset()
        for (enemy in currentLevel.enemies) enemy.reset()
        for (plat in currentLevel.platforms) plat.reset()
        enemyProjectiles.clear()
        shieldActive = false
        shieldTimer = 0f
        shadowTimeActive = false
        shadowTimeTimer = 0f
        magnetActive = false
        magnetTimer = 0f
        dashBoostActive = false
        dashBoostTimer = 0f
        doubleCrystalActive = false
        doubleCrystalTimer = 0f
        pendingCameraSnap = true
        isProcessingDeath = false
        ShadowHeroAudioEngine.startBackgroundMusic(currentLevel.theme)
    }

    // --- ENEMY SYSTEM & COMBAT STATE (Phase 13) ---
    var health: Int = 3
    var maxHealth: Int = 3

    var comboStep: Int = 0 // 0: Idle, 1: Attack 1, 2: Attack 2
    var comboTimer: Float = 0f
    var attackCooldownTimer: Float = 0f
    var attackAnimTimer: Float = 0f
    val activeSlashes = mutableListOf<SlashEffect>()

    // Camera Controller & Viewport Variables (Phase 15 Camera Fix)
    val cameraController = ShadowHeroCameraController(player)
    var pendingCameraSnap: Boolean = true

    val cameraX: Float get() = cameraController.cameraX
    val cameraY: Float get() = cameraController.cameraY
    var cameraShake: Float
        get() = cameraController.cameraShake
        set(value) { cameraController.cameraShake = value }

    var viewportWidth: Float = 1280f
    var viewportHeight: Float = 720f

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

    // Input States & Game Feel Timers (Phase 12)
    var inputLeft: Boolean = false
    var inputRight: Boolean = false
    var coyoteTimer: Float = 0f
    var jumpBufferTimer: Float = 0f
    var deathSequenceTimer: Float = 0f
    var hitStopTimer: Float = 0f

    init {
        loadStage(1)
    }

    fun triggerJump(): Boolean {
        if (player.isDead || player.isDashing || isStageComplete || player.respawnTimer > 0f) return false

        // 1. Wall Jump Check
        if (!player.isGrounded && (player.isOnLeftWall || player.isOnRightWall)) {
            val wallDir = if (player.isOnLeftWall) 1f else -1f
            player.vx = wallDir * WALL_JUMP_VX
            player.vy = WALL_JUMP_VY
            player.facingRight = (wallDir > 0)
            player.jumpCount = 1
            player.animState = PlayerAnimState.WALL_JUMP
            coyoteTimer = 0f
            jumpBufferTimer = 0f
            spawnWallParticles(if (player.isOnLeftWall) player.x else player.x + player.width, player.y + player.height / 2f)
            ShadowHeroAudioEngine.playWallJump()
            return true
        }

        // 2. Normal Ground Jump (Direct Grounded OR Coyote Time)
        if (player.isGrounded || coyoteTimer > 0f) {
            player.vy = JUMP_VELOCITY
            player.isGrounded = false
            player.jumpCount = 1
            player.animState = PlayerAnimState.JUMP
            coyoteTimer = 0f
            jumpBufferTimer = 0f
            spawnJumpDust(player.x + player.width / 2f, player.y + player.height)
            ShadowHeroAudioEngine.playJump()
            return true
        }

        // 3. Double Jump (In Air)
        if (!player.isGrounded && player.jumpCount < 2) {
            player.vy = DOUBLE_JUMP_VELOCITY
            player.jumpCount = 2
            player.animState = PlayerAnimState.DOUBLE_JUMP
            jumpBufferTimer = 0f
            spawnDoubleJumpAura(player.x + player.width / 2f, player.y + player.height / 2f)
            ShadowHeroAudioEngine.playDoubleJump()
            return true
        }

        // Buffer Jump Input if pressed slightly before landing or touching wall
        jumpBufferTimer = 0.14f
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

        val dashCooldown = if (dashBoostActive) DASH_COOLDOWN * 0.4f else DASH_COOLDOWN
        val dashSpeed = if (dashBoostActive) DASH_SPEED * 1.25f else DASH_SPEED

        player.isDashing = true
        player.dashTimer = DASH_DURATION
        player.dashCooldownTimer = dashCooldown
        player.dashDirection = dir
        player.facingRight = (dir > 0f)
        player.vx = dir * dashSpeed
        player.vy = 0f
        player.animState = PlayerAnimState.DASH

        cameraShake = 12f
        ShadowHeroAudioEngine.playDash()
        return true
    }

    // Main 60 FPS Engine Tick Logic
    fun update(dt: Float, rawVw: Float, rawVh: Float, context: android.content.Context? = null) {
        if (context != null) {
            appContext = context
        }
        val targetWorldHeight = 440f
        val scale = if (rawVh > 0f) (rawVh / targetWorldHeight).coerceAtLeast(1.0f) else 1f
        val worldVw = rawVw / scale
        val worldVh = rawVh / scale

        this.viewportWidth = worldVw
        this.viewportHeight = worldVh
        val safeDt = dt.coerceIn(0.001f, 0.05f)

        if (player.isDead) {
            if (deathSequenceTimer > 0f) {
                deathSequenceTimer -= safeDt
                player.deathTimer = deathSequenceTimer
                if (Math.random() < 0.6) {
                    spawnDeathEscapingParticles(player.x + player.width / 2f, player.y + player.height / 2f)
                }
                if (deathSequenceTimer <= 0f) {
                    processDeathSequenceComplete()
                }
            }
            updateParticlesAndGhosts(safeDt)
            updateCamera(viewportWidth, viewportHeight, safeDt)
            return
        }

        if (isStageComplete) {
            updateParticlesAndGhosts(safeDt)
            updateCamera(viewportWidth, viewportHeight, safeDt)
            return
        }

        // Hit-stop impact freeze (Phase 14)
        if (hitStopTimer > 0f) {
            hitStopTimer = max(0f, hitStopTimer - safeDt)
            updateParticlesAndGhosts(safeDt * 0.2f)
            updateCamera(viewportWidth, viewportHeight, safeDt)
            return
        }

        if (player.respawnTimer > 0f) {
            player.respawnTimer = max(0f, player.respawnTimer - safeDt)
            if (player.respawnTimer > 0f) {
                player.animState = PlayerAnimState.RESPAWN
                spawnRespawnGatheringParticles(player.x + player.width / 2f, player.y + player.height / 2f)
            }
        }

        // Coyote Time & Jump Buffer Timers (Phase 12)
        if (coyoteTimer > 0f) coyoteTimer = max(0f, coyoteTimer - safeDt)
        if (jumpBufferTimer > 0f) {
            jumpBufferTimer = max(0f, jumpBufferTimer - safeDt)
            if (player.isGrounded || coyoteTimer > 0f || player.isOnLeftWall || player.isOnRightWall) {
                triggerJump()
            }
        }

        if (stageCountdownTimer > 0f) {
            stageCountdownTimer -= safeDt
            updateParticlesAndGhosts(safeDt)
            updateCamera(viewportWidth, viewportHeight, safeDt)
            return
        }

        stageTimeSeconds += safeDt

        // Update Dynamic Event Director (Phase 11B)
        eventDirector.update(safeDt, player.x, player.y, currentStage, difficultyDirector, currentLevel.platforms)

        // Stream intelligent world chunks around player position
        chunkManager.updatePlayerPosition(player.x, activeCheckpoint?.x)
        updateLevelFromChunkManager()

        // Time Dilation when Shadow Time power-up is active (Phase 5)
        val timeDilation = if (shadowTimeActive) 0.45f else 1.0f

        // Energy Regeneration Logic (Boosted during ENERGY_SURGE event)
        val activeRegenRate = if (eventDirector.isEnergySurgeActive) ENERGY_REGEN_RATE * 3.5f else ENERGY_REGEN_RATE
        if (!player.isDashing && energy < MAX_ENERGY) {
            energy = min(MAX_ENERGY, energy + activeRegenRate * safeDt)
        }

        // Falling Crystal Pickup Collision (Crystal Rain Event)
        val playerBounds = player.bounds
        for (fc in eventDirector.fallingCrystals) {
            if (!fc.isCollected && playerBounds.overlaps(fc.bounds)) {
                fc.isCollected = true
                energy = min(MAX_ENERGY, energy + 15f)
                addFloatingText("+CRYSTAL", fc.x, fc.y, Color(0xFF38BDF8))
                ShadowHeroAudioEngine.playCrystalCollect()
            }
        }

        // Meteor Hazard Impact Damage Check (Meteor Event)
        for (meteor in eventDirector.activeMeteors) {
            if (meteor.isImpacted && meteor.impactDurationTimer > 0.4f) {
                if (playerBounds.overlaps(meteor.impactBounds) && player.invincibleTimer <= 0f) {
                    if (shieldActive) {
                        shieldActive = false
                        player.invincibleTimer = 1.0f
                        addFloatingText("SHIELD BROKEN", player.x, player.y - 20f, Color(0xFFEF4444))
                    } else {
                        triggerPlayerDeath()
                    }
                }
            }
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

        if (dashBoostActive && dashBoostTimer > 0f) {
            dashBoostTimer -= safeDt
            if (dashBoostTimer <= 0f) dashBoostActive = false
        }

        if (doubleCrystalActive && doubleCrystalTimer > 0f) {
            doubleCrystalTimer -= safeDt
            if (doubleCrystalTimer <= 0f) doubleCrystalActive = false
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
            // 2. Normal Horizontal Movement Physics (Speed Surge Event support)
            val effectiveMaxRunSpeed = if (eventDirector.isSpeedSurgeActive) MAX_RUN_SPEED * 1.28f else MAX_RUN_SPEED
            val moveDir = when {
                inputLeft && !inputRight -> -1f
                inputRight && !inputLeft -> 1f
                else -> 0f
            }

            if (moveDir != 0f) {
                player.facingRight = (moveDir > 0f)
                val accel = if (player.isGrounded) MOVE_ACCEL else AIR_CONTROL
                player.vx += moveDir * accel * safeDt
                player.vx = player.vx.coerceIn(-effectiveMaxRunSpeed, effectiveMaxRunSpeed)
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

            // 3. Gravity & Wall Slide (Low Gravity Event support)
            val effectiveGravity = if (eventDirector.isLowGravityActive) GRAVITY * 0.55f else GRAVITY
            val isAgainstWall = (player.isOnLeftWall && inputLeft) || (player.isOnRightWall && inputRight)
            val isWallSliding = !player.isGrounded && isAgainstWall && player.vy > 0f

            if (isWallSliding) {
                player.vy += effectiveGravity * 0.3f * safeDt
                player.vy = min(WALL_SLIDE_SPEED, player.vy)
                player.animState = PlayerAnimState.WALL_SLIDE
                player.jumpCount = 0 // Wall slide refreshes double jump

                // Wall dust
                if (Math.random() < 0.3) {
                    val wallX = if (player.isOnLeftWall) player.x else player.x + player.width
                    spawnWallParticles(wallX, player.y + player.height * 0.6f)
                }
            } else {
                player.vy += effectiveGravity * safeDt
                player.vy = min(MAX_FALL_SPEED, player.vy)
            }
        }

        // 4. Update Landing Squash Timer
        if (player.landingSquashTimer > 0f) {
            player.landingSquashTimer = max(0f, player.landingSquashTimer - safeDt)
        }

        // 4c. Update Attack & Combos (Phase 13)
        if (attackCooldownTimer > 0f) {
            attackCooldownTimer = max(0f, attackCooldownTimer - safeDt)
        }
        if (comboTimer > 0f) {
            comboTimer = max(0f, comboTimer - safeDt)
            if (comboTimer <= 0f) {
                comboStep = 0
            }
        }
        if (attackAnimTimer > 0f) {
            attackAnimTimer = max(0f, attackAnimTimer - safeDt)
        }

        val slashIter = activeSlashes.iterator()
        while (slashIter.hasNext()) {
            val slash = slashIter.next()
            slash.lifeTime -= safeDt
            if (slash.lifeTime <= 0f) {
                slashIter.remove()
            }
        }

        // 4b. Update Dynamic Level Entities & Obstacles (Slowing down when Shadow Time active)
        val entityDt = safeDt * timeDilation
        for (mSpike in currentLevel.movingSpikes) mSpike.update(entityDt)
        for (blade in currentLevel.blades) blade.update(entityDt)
        for (laser in currentLevel.lasers) laser.update(entityDt)
        for (plat in currentLevel.platforms) plat.update(safeDt)
        for (enemy in currentLevel.enemies) {
            enemy.update(
                dt = entityDt,
                playerX = player.x,
                playerY = player.y,
                playerIsDead = player.isDead,
                newProjectiles = enemyProjectiles,
                onDealDamage = { damageAmount ->
                    damagePlayer(damageAmount.toInt())
                }
            )
        }

        val projIter = enemyProjectiles.iterator()
        while (projIter.hasNext()) {
            val proj = projIter.next()
            if (!proj.update(entityDt)) {
                projIter.remove()
            } else if (player.bounds.overlaps(proj.bounds) && player.invincibleTimer <= 0f) {
                projIter.remove()
                damagePlayer(1)
            } else {
                val hitPlatform = currentLevel.platforms.any { !it.isWall && proj.bounds.overlaps(it.bounds) }
                if (hitPlatform) {
                    projIter.remove()
                }
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
        val hitEnemy = currentLevel.enemies.any { enemy -> enemy.state != EnemyState.DEATH && player.bounds.overlaps(enemy.bounds) }
        val hitProjectile = enemyProjectiles.any { player.bounds.overlaps(it.bounds) }

        if (hitStaticSpike || hitMovingSpike || hitBlade || hitLaser || hitHazard || hitEnemy || hitProjectile) {
            if (player.invincibleTimer <= 0f) {
                damagePlayer(1)
            }
        }

        // 5b. Checkpoint Interactions (Phase 5)
        for (cp in currentLevel.checkpoints) {
            if (!cp.isActivated && player.bounds.overlaps(cp.bounds)) {
                cp.isActivated = true
                activeCheckpoint = cp
                savedCheckpointState = CheckpointState(
                    levelSeed = currentLevel.seed,
                    stageId = currentStage,
                    cpX = cp.x + cp.width / 2f - player.width / 2f,
                    cpY = cp.y - 10f,
                    cameraX = cameraX,
                    cameraY = cameraY,
                    energy = energy,
                    collectedCrystals = collectedCrystalIds.toSet(),
                    collectedPowerUps = collectedPowerUpIds.toSet(),
                    requiredCrystals = levelCompletionManager.requiredCrystals,
                    levelProgressX = player.x
                )
                health = maxHealth // FULLY REGENERATE HEALTH ON CHECKPOINT
                spawnCheckpointAura(cp.x + cp.width / 2f, cp.y + cp.height / 2f)
                addFloatingText("CHECKPOINT & HEALED! ❤️", cp.x + cp.width / 2f, cp.y - 25f, Color(0xFF4ADE80))
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
                if (doubleCrystalActive) {
                    collectedCrystalIds.add("${crystal.id}_2x")
                    addFloatingText("+2 💎 2X!", crystal.x, crystal.y - 15f, Color(0xFF4ADE80))
                } else {
                    addFloatingText("+1 💎", crystal.x, crystal.y - 15f, Color(0xFFFACC15))
                }
                ShadowHeroAudioEngine.playCrystalCollect()
            }
        }

        // 5d. Power-Up Pickups Interactions (Phase 15)
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
                        shieldTimer = 15f
                        addFloatingText("SHIELD ACTIVE! 🛡️", pu.x, pu.y - 20f, Color(0xFF22D3EE))
                    }
                    PowerUpType.DASH_BOOST, PowerUpType.DASH_RECHARGE -> {
                        dashBoostActive = true
                        dashBoostTimer = 10f
                        player.dashCooldownTimer = 0f
                        addFloatingText("DASH BOOST! ⚡", pu.x, pu.y - 20f, Color(0xFFE879F9))
                    }
                    PowerUpType.MAGNET, PowerUpType.CRYSTAL_MAGNET -> {
                        magnetActive = true
                        magnetTimer = 12f
                        addFloatingText("MAGNET ACTIVE! 🧲", pu.x, pu.y - 20f, Color(0xFFFACC15))
                    }
                    PowerUpType.SLOW_TIME, PowerUpType.SHADOW_TIME -> {
                        shadowTimeActive = true
                        shadowTimeTimer = 8f
                        addFloatingText("SLOW TIME! ⏱️", pu.x, pu.y - 20f, Color(0xFF38BDF8))
                    }
                    PowerUpType.DOUBLE_CRYSTAL, PowerUpType.ENERGY_BOOST -> {
                        doubleCrystalActive = true
                        doubleCrystalTimer = 10f
                        energy = min(MAX_ENERGY, energy + 50f)
                        addFloatingText("2X CRYSTALS! 💎2X", pu.x, pu.y - 20f, Color(0xFF4ADE80))
                    }
                }
            }
        }

        // 5e. Exit Portal Interaction (Authoritative Single Level Completion System)
        if (!isStageComplete && player.bounds.overlaps(currentLevel.exitPortal.bounds)) {
            val ctx = appContext
            if (ctx != null) {
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_hero"
                val completeSuccess = levelCompletionManager.evaluateAndCheckExit(
                    context = ctx,
                    collectedCrystalsCount = collectedCrystalIds.size,
                    stageTimeSeconds = stageTimeSeconds,
                    userId = userId,
                    seed = currentLevel.seed,
                    onCompletionTriggered = {
                        isStageComplete = true
                        spawnPortalCelebration(currentLevel.exitPortal.x, currentLevel.exitPortal.y)
                        cameraShake = 16f
                        ShadowHeroAudioEngine.playStageComplete()
                    }
                )
                if (!completeSuccess && levelCompletionManager.state == LevelCompletionState.ACTIVE) {
                    val needed = levelCompletionManager.requiredCrystals - collectedCrystalIds.size
                    if (needed > 0) {
                        addFloatingText("NEED $needed MORE 💎 CRYSTALS!", currentLevel.exitPortal.x, currentLevel.exitPortal.y - 40f, Color(0xFFEF4444))
                    }
                }
            } else {
                if (collectedCrystalIds.size >= levelCompletionManager.requiredCrystals) {
                    isStageComplete = true
                    spawnPortalCelebration(currentLevel.exitPortal.x, currentLevel.exitPortal.y)
                    cameraShake = 16f
                    ShadowHeroAudioEngine.playStageComplete()
                } else {
                    val needed = levelCompletionManager.requiredCrystals - collectedCrystalIds.size
                    addFloatingText("NEED $needed MORE 💎 CRYSTALS!", currentLevel.exitPortal.x, currentLevel.exitPortal.y - 40f, Color(0xFFEF4444))
                }
            }
        }

        // 6. Check Dynamic Pit Fall / Death Condition (Req 1, 2, 3, 6)
        val deathBoundary = calculateDynamicDeathBoundary()
        if (player.y > deathBoundary) {
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
        health = maxHealth
        comboStep = 0
        comboTimer = 0f
        attackCooldownTimer = 0f
        attackAnimTimer = 0f
        activeSlashes.clear()

        for (mSpike in currentLevel.movingSpikes) mSpike.reset()
        for (blade in currentLevel.blades) blade.reset()
        for (laser in currentLevel.lasers) laser.reset()
        for (enemy in currentLevel.enemies) enemy.reset()
        for (plat in currentLevel.platforms) plat.reset()
        enemyProjectiles.clear()

        player.isDead = false
        pendingCameraSnap = true
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

        if (wasGrounded && !player.isGrounded && player.vy >= 0f) {
            coyoteTimer = 0.12f
        }
    }

    private fun updateAnimationState(dt: Float) {
        if (player.isDead) {
            player.animState = PlayerAnimState.DEATH
            return
        }
        if (player.respawnTimer > 0f) {
            player.animState = PlayerAnimState.RESPAWN
            return
        }
        if (player.invincibleTimer > 0.8f && !shieldActive) {
            player.animState = PlayerAnimState.HURT
            return
        }
        if (player.isDashing) {
            player.animState = PlayerAnimState.DASH
            return
        }

        if (player.isGrounded) {
            if (player.landingSquashTimer > 0f) {
                player.animState = PlayerAnimState.LAND
            } else if (abs(player.vx) > 20f) {
                player.animState = PlayerAnimState.RUN
                val speedRatio = (abs(player.vx) / MAX_RUN_SPEED).coerceIn(0.5f, 1.8f)
                player.runCycleTimer += dt * 14f * speedRatio
            } else {
                player.animState = PlayerAnimState.IDLE
                player.runCycleTimer = 0f
            }
        } else {
            val isAgainstWall = (player.isOnLeftWall && inputLeft) || (player.isOnRightWall && inputRight)
            if (isAgainstWall && player.vy > 0f) {
                player.animState = PlayerAnimState.WALL_SLIDE
                player.wallSlideTimer += dt
            } else if (player.animState == PlayerAnimState.WALL_JUMP && player.vy < -100f) {
                // Keep WALL_JUMP during upward boost
            } else if (player.vy < 0f) {
                player.animState = if (player.jumpCount >= 2) PlayerAnimState.DOUBLE_JUMP else PlayerAnimState.JUMP
            } else {
                player.animState = PlayerAnimState.FALL
            }
        }
    }

    private fun updateCamera(vw: Float, vh: Float, dt: Float) {
        if (vw <= 0f || vh <= 0f) return

        if (pendingCameraSnap) {
            cameraController.snapToPlayer(vw, vh)
            pendingCameraSnap = false
        } else {
            cameraController.update(
                vw = vw,
                vh = vh,
                dt = dt,
                levelWidth = currentLevel.levelWidth,
                levelHeight = currentLevel.levelHeight,
                isDead = player.isDead
            )
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
            PowerUpType.SHIELD -> Color(0xFF22D3EE)
            PowerUpType.DASH_BOOST, PowerUpType.DASH_RECHARGE -> Color(0xFFE879F9)
            PowerUpType.MAGNET, PowerUpType.CRYSTAL_MAGNET -> Color(0xFFFACC15)
            PowerUpType.SLOW_TIME, PowerUpType.SHADOW_TIME -> Color(0xFF38BDF8)
            PowerUpType.DOUBLE_CRYSTAL, PowerUpType.ENERGY_BOOST -> Color(0xFF4ADE80)
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

    private fun spawnCriticalHitBurst(cx: Float, cy: Float) {
        for (i in 0..24) {
            val angle = Math.random() * Math.PI * 2
            val speed = 140f + Math.random().toFloat() * 180f
            particles.add(
                HeroParticle(
                    x = cx,
                    y = cy,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = Math.sin(angle).toFloat() * speed,
                    radius = 3.5f + Math.random().toFloat() * 4.5f,
                    color = if (i % 2 == 0) Color(0xFFFACC15) else Color(0xFFFB923C),
                    alpha = 1f,
                    maxLife = 0.35f
                )
            )
        }
    }

    private fun spawnEnemyDeathBurst(cx: Float, cy: Float) {
        for (i in 0..28) {
            val angle = Math.random() * Math.PI * 2
            val speed = 100f + Math.random().toFloat() * 220f
            particles.add(
                HeroParticle(
                    x = cx,
                    y = cy,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = Math.sin(angle).toFloat() * speed,
                    radius = 3f + Math.random().toFloat() * 5f,
                    color = if (i % 3 == 0) Color(0xFFA855F7) else if (i % 3 == 1) Color(0xFFC084FC) else Color(0xFF22D3EE),
                    alpha = 1f,
                    maxLife = 0.45f
                )
            )
        }
    }

    fun damagePlayer(amount: Int = 1) {
        if (player.isDead || player.invincibleTimer > 0f || isStageComplete || player.respawnTimer > 0f) return

        if (shieldActive) {
            shieldActive = false
            shieldTimer = 0f
            player.invincibleTimer = 1.2f
            spawnShieldBreakBurst(player.x + player.width / 2f, player.y + player.height / 2f)
            addFloatingText("SHIELD BROKEN!", player.x + player.width / 2f, player.y - 20f, Color(0xFFC084FC))
            cameraShake = 12f
            ShadowHeroAudioEngine.playShieldBreak()
            return
        }

        health = (health - amount).coerceAtLeast(0)
        player.invincibleTimer = 1.2f
        player.animState = PlayerAnimState.HURT
        cameraShake = 10f

        // Play hurt sound & light haptic feedback
        ShadowHeroAudioEngine.playPlayerHurt()

        // Knockback: push player away from damage direction
        player.vx = if (player.facingRight) -320f else 320f
        player.vy = -280f
        player.isGrounded = false

        addFloatingText("-❤️", player.x + player.width / 2f, player.y - 20f, Color(0xFFEF4444))

        if (health <= 0) {
            triggerPlayerDeath()
        }
    }

    fun triggerAttack(): Boolean {
        if (player.isDead || isStageComplete || attackCooldownTimer > 0f || player.respawnTimer > 0f) return false

        val isAirAttack = !player.isGrounded
        player.animState = PlayerAnimState.ATTACK

        // Determine combo step
        if (comboStep == 1 && comboTimer > 0f) {
            comboStep = 2
            comboTimer = 0f
            attackCooldownTimer = 0.38f // combo finisher cooldown
            ShadowHeroAudioEngine.playShadowStrikeCombo()
        } else {
            comboStep = 1
            comboTimer = 0.65f // window to hit Attack 2
            attackCooldownTimer = 0.22f // standard slash cooldown
            ShadowHeroAudioEngine.playShadowStrike()
        }

        attackAnimTimer = 0.25f

        // Purple/pink slash dimensions & positioning
        val sw = if (comboStep == 2) 88f else 75f
        val sh = if (comboStep == 2) 70f else 60f
        val sx = if (player.facingRight) player.x + player.width - 5f else player.x - sw + 5f
        val sy = player.y - 12f

        val slash = SlashEffect(
            x = sx,
            y = sy,
            width = sw,
            height = sh,
            facingRight = player.facingRight,
            isComboFinisher = (comboStep == 2)
        )
        activeSlashes.add(slash)

        // Visual slash particles
        spawnSlashParticles(sx + sw / 2f, sy + sh / 2f, player.facingRight)

        // Air attack pushing effect
        if (isAirAttack) {
            player.vx = if (player.facingRight) 220f else -220f
            player.vy = player.vy.coerceAtMost(-120f) // upward kick or lift
            player.isGrounded = false
        }

        // Check damage to enemies in slash box
        val slashBounds = Rect(sx, sy, sx + sw, sy + sh)
        val baseDamage = if (comboStep == 2) 35f else 20f

        val enemiesHit = currentLevel.enemies.filter { enemy ->
            enemy.state != EnemyState.DEATH && slashBounds.overlaps(enemy.bounds)
        }

        if (enemiesHit.isNotEmpty()) {
            val isCrit = Math.random() < 0.15
            val finalDamage = if (isCrit) baseDamage * 1.8f else baseDamage
            hitStopTimer = if (isCrit || comboStep == 2) 0.08f else 0.05f

            for (enemy in enemiesHit) {
                var wasBlocked = false
                val died = enemy.takeDamage(finalDamage, playerX = player.x, onBlock = {
                    wasBlocked = true
                    addFloatingText("BLOCKED!", enemy.x + enemy.width / 2f, enemy.y - 20f, Color(0xFFA855F7))
                    spawnHitBurstParticles(enemy.x + enemy.width / 2f, enemy.y + enemy.height / 2f)
                })

                if (wasBlocked) continue

                // Push enemy back
                val pushDir = if (player.facingRight) 1f else -1f
                enemy.x = (enemy.x + pushDir * 38f).coerceIn(enemy.patrolMinX, enemy.patrolMaxX)

                if (isCrit) {
                    spawnCriticalHitBurst(enemy.x + enemy.width / 2f, enemy.y + enemy.height / 2f)
                    addFloatingText("CRIT ${finalDamage.toInt()}!", enemy.x + enemy.width / 2f, enemy.y - 25f, Color(0xFFFACC15))
                    ShadowHeroAudioEngine.playCriticalHit()
                    cameraShake = 12f
                } else {
                    spawnHitBurstParticles(enemy.x + enemy.width / 2f, enemy.y + enemy.height / 2f)
                    addFloatingText("-${finalDamage.toInt()}", enemy.x + enemy.width / 2f, enemy.y - 20f, Color(0xFFF87171))
                    cameraShake = if (comboStep == 2) 8f else 4f
                }

                if (died) {
                    spawnEnemyDeathBurst(enemy.x + enemy.width / 2f, enemy.y + enemy.height / 2f)
                    addFloatingText("+100 XP", enemy.x + enemy.width / 2f, enemy.y - 35f, Color(0xFFC084FC))

                    // Drop optional reward crystal
                    if (Math.random() < 0.50) {
                        val newCrystal = LevelEnergyCrystal(
                            id = "drop_${System.currentTimeMillis()}_${Math.random()}",
                            x = enemy.x + enemy.width / 2f,
                            y = enemy.y + enemy.height / 2f - 10f,
                            radius = 12f
                        )
                        val currentCrystals = currentLevel.crystals.toMutableList()
                        currentCrystals.add(newCrystal)
                        currentLevel = currentLevel.copy(crystals = currentCrystals)
                        spawnPowerUpBurst(newCrystal.x, newCrystal.y, PowerUpType.CRYSTAL_MAGNET)
                        addFloatingText("+CRYSTAL", newCrystal.x, newCrystal.y - 15f, Color(0xFF38BDF8))
                    }
                }
            }
        }

        return true
    }

    private fun spawnSlashParticles(cx: Float, cy: Float, facingRight: Boolean) {
        val dir = if (facingRight) 1f else -1f
        for (i in 0..12) {
            val angle = (if (facingRight) -Math.PI / 3f else Math.PI * 4f / 3f) + (Math.random() - 0.5) * Math.PI / 2f
            val speed = 150f + Math.random().toFloat() * 150f
            particles.add(
                HeroParticle(
                    x = cx,
                    y = cy,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = Math.sin(angle).toFloat() * speed,
                    radius = 3f + Math.random().toFloat() * 4f,
                    color = if (i % 2 == 0) Color(0xFFA855F7) else Color(0xFFEC4899),
                    alpha = 1f,
                    maxLife = 0.22f
                )
            )
        }
    }

    private fun spawnHitBurstParticles(cx: Float, cy: Float) {
        for (i in 0..15) {
            val angle = Math.random() * Math.PI * 2
            val speed = 80f + Math.random().toFloat() * 120f
            particles.add(
                HeroParticle(
                    x = cx,
                    y = cy,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = Math.sin(angle).toFloat() * speed,
                    radius = 2.5f + Math.random().toFloat() * 3.5f,
                    color = Color(0xFFF43F5E),
                    alpha = 1f,
                    maxLife = 0.25f
                )
            )
        }
    }

    fun calculateDynamicDeathBoundary(): Float {
        val nonWallPlatforms = currentLevel.platforms.filter { !it.isWall }
        if (nonWallPlatforms.isEmpty()) return currentLevel.levelHeight + 100f
        val lowestPlatformTop = nonWallPlatforms.maxOf { it.bounds.top }
        return lowestPlatformTop + 220f
    }

    fun triggerPlayerDeath() {
        if (isProcessingDeath || player.isDead) return
        isProcessingDeath = true
        gameState = ShadowHeroGameState.PLAYER_DIED
        player.isDead = true
        player.vx = 0f
        player.vy = 0f
        player.isDashing = false
        player.animState = PlayerAnimState.DEATH
        player.deathTimer = 0.8f
        deathSequenceTimer = 0.8f
        difficultyDirector.recordDeath("HAZARD_OR_FALL")
        spawnDeathExplosion(player.x + player.width / 2f, player.y + player.height / 2f)
        cameraShake = 18f
        ShadowHeroAudioEngine.playPlayerDeath()
    }

    fun processDeathSequenceComplete() {
        if (lives > 1) {
            consumeLife()
            resetToActiveCheckpointOrStart()
            gameState = ShadowHeroGameState.PLAYING
        } else {
            consumeLife()
            gameState = ShadowHeroGameState.DEATH_DIALOG
            isGameOverDialogOpen = true
        }
    }

    fun consumeLife() {
        lives = (lives - 1).coerceAtLeast(0)
    }

    private fun spawnDeathEscapingParticles(cx: Float, cy: Float) {
        for (i in 0..2) {
            val angle = Math.random() * Math.PI * 2
            val speed = 30f + Math.random().toFloat() * 70f
            particles.add(
                HeroParticle(
                    x = cx + (Math.random().toFloat() - 0.5f) * 20f,
                    y = cy + (Math.random().toFloat() - 0.5f) * 30f,
                    vx = Math.cos(angle).toFloat() * speed,
                    vy = -Math.abs(Math.sin(angle).toFloat()) * speed - 20f,
                    radius = 2.5f + Math.random().toFloat() * 3.5f,
                    color = if (i % 2 == 0) Color(0xFFA855F7) else Color(0xFFEF4444),
                    alpha = 0.9f,
                    maxLife = 0.4f
                )
            )
        }
    }

    private fun spawnRespawnGatheringParticles(cx: Float, cy: Float) {
        for (i in 0..3) {
            val angle = Math.random() * Math.PI * 2
            val dist = 60f + Math.random().toFloat() * 40f
            val startX = cx + Math.cos(angle).toFloat() * dist
            val startY = cy + Math.sin(angle).toFloat() * dist
            val speed = 180f
            particles.add(
                HeroParticle(
                    x = startX,
                    y = startY,
                    vx = (cx - startX) * 2.5f,
                    vy = (cy - startY) * 2.5f,
                    radius = 2f + Math.random().toFloat() * 3f,
                    color = Color(0xFF38BDF8),
                    alpha = 0.85f,
                    maxLife = 0.25f
                )
            )
        }
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

// Single Authoritative Camera Controller for Shadow Hero (Phase 15 Camera Fix)
class ShadowHeroCameraController(val player: ShadowHeroPlayer) {
    var cameraX: Float = 0f
    var cameraY: Float = 0f
    var cameraShake: Float = 0f

    // Highly responsive follow zones / dead zones (half-size in world units)
    private val hDeadZone = 30f
    private val vDeadZone = 40f

    // Configurable interpolation speeds for frame-rate independence (per second)
    private val normalLerpSpeed = 6.5f
    private val fastLerpSpeed = 14.0f

    // Look-ahead states
    var lastTargetX: Float = 0f
    var lastTargetY: Float = 0f
    private var currentLookAheadX = 0f
    private var currentLookAheadY = 0f

    // Cached camera bounds
    var minBoundX: Float = 0f
    var maxBoundX: Float = Float.MAX_VALUE
    var minBoundY: Float = -3000f
    var maxBoundY: Float = 1200f

    fun update(
        vw: Float,
        vh: Float,
        dt: Float,
        levelWidth: Float,
        levelHeight: Float,
        isDead: Boolean
    ) {
        if (vw <= 0f || vh <= 0f) return

        // 1. Update dynamic world bounds based on currently generated chunks
        minBoundX = 0f
        maxBoundX = kotlin.math.max(levelWidth, player.x + vw)
        minBoundY = -4000f // Deep vertical climbs are supported
        maxBoundY = kotlin.math.max(levelHeight, player.y + vh)

        // 2. If player is dead, keep camera focused on their death point and decay shake
        if (isDead) {
            decayShake(dt)
            return
        }

        // 3. Calculate target look-ahead based on player movement
        val playerCenterX = player.x + player.width / 2f
        val playerCenterY = player.y + player.height / 2f

        val targetLookAheadX = when {
            player.isDashing -> if (player.facingRight) 140f else -140f
            kotlin.math.abs(player.vx) > 120f -> if (player.facingRight) 80f else -80f
            else -> if (player.facingRight) 40f else -40f
        }

        val targetLookAheadY = when {
            player.vy > 120f -> 40f   // Look slightly down when falling
            player.vy < -120f -> -50f // Look slightly up when jumping
            else -> 0f
        }

        // Frame-rate independent smoothing of look-ahead offsets
        val lookAheadSpeed = 3.5f
        val lookAheadFactor = 1f - kotlin.math.exp(-lookAheadSpeed * dt)
        currentLookAheadX += (targetLookAheadX - currentLookAheadX) * lookAheadFactor
        currentLookAheadY += (targetLookAheadY - currentLookAheadY) * lookAheadFactor

        // 4. Determine camera target position centering player with look-ahead
        val idealTargetX = playerCenterX + currentLookAheadX - vw / 2f
        val idealTargetY = playerCenterY + currentLookAheadY - vh * 0.52f

        lastTargetX = idealTargetX
        lastTargetY = idealTargetY

        // 5. Apply follow zone (dead zone) thresholds
        val playerRelX = playerCenterX - (cameraX + vw / 2f)
        val playerRelY = playerCenterY - (cameraY + vh * 0.52f)

        var followTargetX = cameraX
        var followTargetY = cameraY

        if (kotlin.math.abs(playerRelX) > hDeadZone) {
            val offset = kotlin.math.abs(playerRelX) - hDeadZone
            followTargetX += if (playerRelX > 0f) offset else -offset
        }
        if (kotlin.math.abs(playerRelY) > vDeadZone) {
            val offset = kotlin.math.abs(playerRelY) - vDeadZone
            followTargetY += if (playerRelY > 0f) offset else -offset
        }

        // 6. Smoothly lerp camera position towards follow target
        // Increase lerp speed during fast moves (Dash, Wall jump, Double jump, High fall) to prevent outrunning
        val isFastMovement = player.isDashing || kotlin.math.abs(player.vx) > 400f || kotlin.math.abs(player.vy) > 650f
        val activeLerpSpeed = if (isFastMovement) fastLerpSpeed else normalLerpSpeed
        val lerpFactor = 1f - kotlin.math.exp(-activeLerpSpeed * dt)

        cameraX += (idealTargetX - cameraX) * lerpFactor
        cameraY += (idealTargetY - cameraY) * lerpFactor

        // 7. Clamp final camera coordinates to current dynamic bounds
        cameraX = cameraX.coerceIn(minBoundX, kotlin.math.max(minBoundX, maxBoundX - vw))
        cameraY = cameraY.coerceIn(minBoundY, kotlin.math.max(minBoundY, maxBoundY - vh))

        decayShake(dt)
    }

    private fun decayShake(dt: Float) {
        if (cameraShake > 0f) {
            cameraShake = kotlin.math.max(0f, cameraShake - dt * 25f)
        }
    }

    fun snapToPlayer(vw: Float, vh: Float) {
        if (vw <= 0f || vh <= 0f) return
        val playerCenterX = player.x + player.width / 2f
        val playerCenterY = player.y + player.height / 2f
        val leadOffset = if (player.facingRight) 90f else -90f

        currentLookAheadX = leadOffset
        currentLookAheadY = 0f

        minBoundX = 0f
        maxBoundX = kotlin.math.max(maxBoundX, player.x + vw)
        minBoundY = -4000f
        maxBoundY = kotlin.math.max(maxBoundY, player.y + vh)

        cameraX = (playerCenterX + leadOffset - vw / 2f)
            .coerceIn(minBoundX, kotlin.math.max(minBoundX, maxBoundX - vw))
        cameraY = (playerCenterY - vh * 0.52f)
            .coerceIn(minBoundY, kotlin.math.max(minBoundY, maxBoundY - vh))
    }
}
