package com.myplaywin.app.shadowhero.engine

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.myplaywin.app.shadowhero.audio.ShadowHeroAudioEngine
import kotlin.math.*

enum class EnemyType {
    // GROUND ENEMIES
    SHADOW_WARRIOR,
    SPIKE_BEAST,
    SHADOW_BRUTE,
    LAVA_GOLEM,
    NIGHT_ARCHER,
    SHIELD_GUARD,

    // FLYING ENEMIES
    DARK_BAT,
    VOID_FLYER,
    SKULL_HAWK,
    FLOATING_MAGE,
    SHADOW_DRONE,
    VOID_JELLY,

    // SPECIAL / BOSS ENEMIES
    SHADOW_KNIGHT,
    CORRUPTED_BEAST,
    VOID_OVERLORD,
    LAVA_TITAN,
    SHADOW_WORM,

    // LEGACY ALIASES FOR BACKWARD COMPATIBILITY
    SHADOW_WALKER,
    FLYING_ORB,
    TURRET,
    CHASER
}

enum class EnemyState {
    IDLE,
    PATROL,
    DETECT,
    CHASE,
    ATTACK,
    HURT,
    STUN,
    RETREAT,
    BURROWED,
    DEATH
}

enum class ProjectileType {
    ENERGY_ORB,
    LAVA_BALL,
    ARROW,
    MAGIC_BOLT,
    LASER_BEAM,
    VOID_BURST,
    FIRE_ERUPTION
}

data class EnemyProjectile(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float = 8f,
    var lifeTime: Float = 3.5f,
    var isUsed: Boolean = true,
    val type: ProjectileType = ProjectileType.ENERGY_ORB,
    val color: Color = Color(0xFFA855F7)
) {
    val bounds: Rect
        get() = Rect(x - radius, y - radius, x + radius, y + radius)

    fun update(dt: Float): Boolean {
        x += vx * dt
        y += vy * dt
        lifeTime -= dt
        return lifeTime > 0f && isUsed
    }
}

fun getEnemyDimensions(type: EnemyType): Pair<Float, Float> {
    return when (type) {
        EnemyType.SHADOW_WARRIOR, EnemyType.SHADOW_WALKER -> Pair(38f, 44f)
        EnemyType.SPIKE_BEAST, EnemyType.CHASER -> Pair(48f, 36f)
        EnemyType.SHADOW_BRUTE -> Pair(52f, 56f)
        EnemyType.LAVA_GOLEM -> Pair(54f, 58f)
        EnemyType.NIGHT_ARCHER -> Pair(36f, 44f)
        EnemyType.SHIELD_GUARD -> Pair(42f, 48f)

        EnemyType.DARK_BAT -> Pair(40f, 32f)
        EnemyType.VOID_FLYER, EnemyType.FLYING_ORB -> Pair(40f, 40f)
        EnemyType.SKULL_HAWK -> Pair(44f, 38f)
        EnemyType.FLOATING_MAGE -> Pair(42f, 48f)
        EnemyType.SHADOW_DRONE, EnemyType.TURRET -> Pair(38f, 36f)
        EnemyType.VOID_JELLY -> Pair(36f, 42f)

        EnemyType.SHADOW_KNIGHT -> Pair(50f, 58f)
        EnemyType.CORRUPTED_BEAST -> Pair(58f, 48f)
        EnemyType.VOID_OVERLORD -> Pair(56f, 64f)
        EnemyType.LAVA_TITAN -> Pair(68f, 72f)
        EnemyType.SHADOW_WORM -> Pair(46f, 60f)
    }
}

data class LevelEnemy(
    val id: String,
    val type: EnemyType,
    val initialX: Float,
    val initialY: Float,
    var x: Float = initialX,
    var y: Float = initialY,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var width: Float = getEnemyDimensions(type).first,
    var height: Float = getEnemyDimensions(type).second,
    var patrolMinX: Float = initialX - 120f,
    var patrolMaxX: Float = initialX + 120f,
    var facingRight: Boolean = true,
    
    // Core combat variables
    var state: EnemyState = EnemyState.PATROL,
    var health: Float = 20f,
    var maxHealth: Float = 20f,
    var isStatsInitialized: Boolean = false,
    var hurtTimer: Float = 0f,
    var stunTimer: Float = 0f,
    var warningTimer: Float = 0f,
    var warningDuration: Float = 0.5f,
    var attackCooldownTimer: Float = 0f,
    var chaseTimer: Float = 0f,
    var hasDetectedPlayer: Boolean = false,
    
    // Unique AI & Animation Trackers
    var isAttacking: Boolean = false,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var animTimer: Float = 0f,
    var specialTimer: Float = 0f,
    var chargeVx: Float = 0f,
    var burrowDepth: Float = 0f, // 0f = surface, 1f = fully buried underground
    var isShieldUp: Boolean = false,
    var laserWarningTimer: Float = 0f,
    var laserBeamActiveTimer: Float = 0f,
    var groundSlamRadius: Float = 0f,
    var teleportTimer: Float = 0f
) {
    val effectiveType: EnemyType
        get() = when (type) {
            EnemyType.SHADOW_WALKER -> EnemyType.SHADOW_WARRIOR
            EnemyType.FLYING_ORB -> EnemyType.VOID_FLYER
            EnemyType.TURRET -> EnemyType.SHADOW_DRONE
            EnemyType.CHASER -> EnemyType.SPIKE_BEAST
            else -> type
        }

    val bounds: Rect
        get() {
            if (effectiveType == EnemyType.SHADOW_WORM && burrowDepth > 0.6f) {
                // When deeply buried, reduce or remove hitbox
                return Rect(0f, 0f, 0f, 0f)
            }
            return Rect(x, y, x + width, y + height)
        }

    fun reset() {
        x = initialX
        y = initialY
        vx = 80f
        vy = 0f
        facingRight = true
        state = EnemyState.PATROL
        health = maxHealth
        hurtTimer = 0f
        stunTimer = 0f
        warningTimer = 0f
        attackCooldownTimer = 0f
        chaseTimer = 0f
        hasDetectedPlayer = false
        isAttacking = false
        animTimer = 0f
        specialTimer = 0f
        chargeVx = 0f
        burrowDepth = 0f
        isShieldUp = false
        laserWarningTimer = 0f
        laserBeamActiveTimer = 0f
        groundSlamRadius = 0f
        teleportTimer = 0f
    }

    fun initStats(stage: Int) {
        if (isStatsInitialized) return
        isStatsInitialized = true
        
        val dims = getEnemyDimensions(effectiveType)
        width = dims.first
        height = dims.second

        val mult = 1f + (stage - 1) * 0.15f
        when (effectiveType) {
            EnemyType.SHADOW_WARRIOR -> {
                maxHealth = 25f * mult
                vx = 80f * (1f + (stage - 1) * 0.05f)
                warningDuration = 0.35f
            }
            EnemyType.SPIKE_BEAST -> {
                maxHealth = 35f * mult
                vx = 100f
                warningDuration = 0.3f
            }
            EnemyType.SHADOW_BRUTE -> {
                maxHealth = 70f * mult
                vx = 50f
                warningDuration = 0.5f
            }
            EnemyType.LAVA_GOLEM -> {
                maxHealth = 90f * mult
                vx = 45f
                warningDuration = 0.6f
            }
            EnemyType.NIGHT_ARCHER -> {
                maxHealth = 30f * mult
                vx = 75f
                warningDuration = 0.45f
            }
            EnemyType.SHIELD_GUARD -> {
                maxHealth = 50f * mult
                vx = 55f
                isShieldUp = true
                warningDuration = 0.4f
            }
            EnemyType.DARK_BAT -> {
                maxHealth = 20f * mult
                vx = 110f
                warningDuration = 0.25f
            }
            EnemyType.VOID_FLYER -> {
                maxHealth = 30f * mult
                vx = 90f
                warningDuration = 0.45f
            }
            EnemyType.SKULL_HAWK -> {
                maxHealth = 35f * mult
                vx = 120f
                warningDuration = 0.3f
            }
            EnemyType.FLOATING_MAGE -> {
                maxHealth = 40f * mult
                vx = 70f
                warningDuration = 0.5f
            }
            EnemyType.SHADOW_DRONE -> {
                maxHealth = 35f * mult
                vx = 0f
                warningDuration = 0.6f
            }
            EnemyType.VOID_JELLY -> {
                maxHealth = 25f * mult
                vx = 60f
                warningDuration = 0.2f
            }
            EnemyType.SHADOW_KNIGHT -> {
                maxHealth = 120f * mult
                vx = 110f
                warningDuration = 0.35f
            }
            EnemyType.CORRUPTED_BEAST -> {
                maxHealth = 150f * mult
                vx = 95f
                warningDuration = 0.4f
            }
            EnemyType.VOID_OVERLORD -> {
                maxHealth = 200f * mult
                vx = 65f
                warningDuration = 0.5f
            }
            EnemyType.LAVA_TITAN -> {
                maxHealth = 250f * mult
                vx = 40f
                warningDuration = 0.6f
            }
            EnemyType.SHADOW_WORM -> {
                maxHealth = 160f * mult
                vx = 0f
                warningDuration = 0.4f
            }
            else -> {
                maxHealth = 25f * mult
                vx = 80f
                warningDuration = 0.4f
            }
        }
        health = maxHealth
    }

    /**
     * Takes damage. Returns true if enemy died.
     * Handles shield blocking for SHIELD_GUARD and burrow invulnerability for SHADOW_WORM.
     */
    fun takeDamage(
        amount: Float, 
        playerX: Float? = null,
        onBlock: (() -> Unit)? = null
    ): Boolean {
        if (state == EnemyState.DEATH) return false

        // 1. Shadow Worm buried invulnerability
        if (effectiveType == EnemyType.SHADOW_WORM && burrowDepth > 0.4f) {
            onBlock?.invoke()
            return false
        }

        // 2. Shield Guard frontal block check
        if (effectiveType == EnemyType.SHIELD_GUARD && isShieldUp && playerX != null) {
            val attackFromFront = if (facingRight) (playerX > x + width / 2f) else (playerX < x + width / 2f)
            if (attackFromFront) {
                onBlock?.invoke()
                ShadowHeroAudioEngine.playShieldBreak()
                return false // Blocked completely!
            }
        }

        // 3. Lava Golem / Lava Titan armor resistance
        val damageApplied = when (effectiveType) {
            EnemyType.LAVA_GOLEM -> amount * 0.75f
            EnemyType.LAVA_TITAN -> amount * 0.70f
            else -> amount
        }

        health -= damageApplied
        hurtTimer = 0.22f
        stunTimer = 0.20f
        state = EnemyState.HURT
        ShadowHeroAudioEngine.playEnemyHurt()

        if (health <= 0f) {
            health = 0f
            state = EnemyState.DEATH
            ShadowHeroAudioEngine.playEnemyDeath()
            return true // Died
        }
        return false
    }

    fun update(
        dt: Float, 
        playerX: Float, 
        playerY: Float, 
        playerIsDead: Boolean,
        newProjectiles: MutableList<EnemyProjectile>,
        onDealDamage: (damage: Float) -> Unit
    ) {
        if (state == EnemyState.DEATH) return

        animTimer += dt

        // Timers
        if (hurtTimer > 0f) hurtTimer -= dt
        if (laserWarningTimer > 0f) laserWarningTimer -= dt
        if (laserBeamActiveTimer > 0f) laserBeamActiveTimer -= dt
        if (groundSlamRadius > 0f) groundSlamRadius += dt * 200f

        if (stunTimer > 0f) {
            stunTimer -= dt
            if (stunTimer <= 0f && state == EnemyState.HURT) {
                state = EnemyState.PATROL
            }
            return
        }

        if (attackCooldownTimer > 0f) {
            attackCooldownTimer -= dt
        }

        val cx = x + width / 2f
        val cy = y + height / 2f
        val dx = playerX - cx
        val dy = playerY - cy
        val dist = sqrt(dx * dx + dy * dy)

        // Turn towards target when chasing or detecting
        if (state == EnemyState.CHASE || state == EnemyState.DETECT || state == EnemyState.ATTACK) {
            facingRight = dx > 0f
        }

        when (effectiveType) {
            // ==========================================
            // GROUND ENEMIES
            // ==========================================

            EnemyType.SHADOW_WARRIOR -> {
                val detRange = 220f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && abs(dx) < detRange && abs(dy) < 90f) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            if (!hasDetectedPlayer) {
                                hasDetectedPlayer = true
                                ShadowHeroAudioEngine.playEnemyDetection()
                            }
                        } else {
                            if (vx == 0f) vx = 80f
                            x += vx * dt
                            facingRight = vx > 0f
                            if (x >= patrolMaxX) { x = patrolMaxX; vx = -abs(vx) }
                            else if (x <= patrolMinX) { x = patrolMinX; vx = abs(vx) }
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) state = EnemyState.CHASE
                    }
                    EnemyState.CHASE -> {
                        if (playerIsDead || abs(dx) > detRange * 1.5f || abs(dy) > 120f) {
                            state = EnemyState.PATROL
                            hasDetectedPlayer = false
                        } else if (dist < 48f) {
                            state = EnemyState.ATTACK
                            warningTimer = 0.25f
                            ShadowHeroAudioEngine.playEnemyAttack()
                        } else {
                            val speed = 125f
                            val dirX = if (dx > 0) 1f else -1f
                            facingRight = dirX > 0f
                            x = (x + dirX * speed * dt).coerceIn(patrolMinX, patrolMaxX)
                        }
                    }
                    EnemyState.ATTACK -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            if (dist < 55f && !playerIsDead) onDealDamage(1f)
                            attackCooldownTimer = 1.3f
                            state = EnemyState.PATROL
                        }
                    }
                    else -> {}
                }
            }

            EnemyType.SPIKE_BEAST -> {
                val detRange = 280f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && abs(dx) < detRange && abs(dy) < 70f) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            if (!hasDetectedPlayer) {
                                hasDetectedPlayer = true
                                ShadowHeroAudioEngine.playChaserWarning()
                            }
                        } else {
                            if (vx == 0f) vx = 100f
                            x += vx * dt
                            facingRight = vx > 0f
                            if (x >= patrolMaxX) { x = patrolMaxX; vx = -abs(vx) }
                            else if (x <= patrolMinX) { x = patrolMinX; vx = abs(vx) }
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            state = EnemyState.CHASE
                            chaseTimer = 0.7f // High speed charge duration
                            chargeVx = if (facingRight) 300f else -300f
                            ShadowHeroAudioEngine.playEnemyAttack()
                        }
                    }
                    EnemyState.CHASE -> {
                        chaseTimer -= dt
                        x = (x + chargeVx * dt).coerceIn(patrolMinX, patrolMaxX)
                        if (dist < 50f && !playerIsDead) {
                            onDealDamage(1f)
                        }
                        if (chaseTimer <= 0f) {
                            state = EnemyState.STUN
                            stunTimer = 0.9f // Recovery after charge
                            attackCooldownTimer = 2.0f
                        }
                    }
                    else -> {}
                }
            }

            EnemyType.SHADOW_BRUTE -> {
                val detRange = 240f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && abs(dx) < detRange && abs(dy) < 80f) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            if (!hasDetectedPlayer) {
                                hasDetectedPlayer = true
                                ShadowHeroAudioEngine.playEnemyDetection()
                            }
                        } else {
                            if (vx == 0f) vx = 50f
                            x += vx * dt
                            facingRight = vx > 0f
                            if (x >= patrolMaxX) { x = patrolMaxX; vx = -abs(vx) }
                            else if (x <= patrolMinX) { x = patrolMinX; vx = abs(vx) }
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) state = EnemyState.CHASE
                    }
                    EnemyState.CHASE -> {
                        if (playerIsDead || abs(dx) > detRange * 1.4f) {
                            state = EnemyState.PATROL
                            hasDetectedPlayer = false
                        } else if (dist < 60f) {
                            state = EnemyState.ATTACK
                            warningTimer = 0.45f
                            ShadowHeroAudioEngine.playEnemyAttack()
                        } else {
                            val speed = 75f
                            val dirX = if (dx > 0) 1f else -1f
                            x = (x + dirX * speed * dt).coerceIn(patrolMinX, patrolMaxX)
                        }
                    }
                    EnemyState.ATTACK -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            groundSlamRadius = 1f
                            if (dist < 75f && !playerIsDead) onDealDamage(1f)
                            attackCooldownTimer = 2.2f
                            state = EnemyState.PATROL
                        }
                    }
                    else -> {}
                }
            }

            EnemyType.LAVA_GOLEM -> {
                val detRange = 320f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && dist < detRange) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            if (!hasDetectedPlayer) {
                                hasDetectedPlayer = true
                                ShadowHeroAudioEngine.playEnemyDetection()
                            }
                        } else {
                            if (vx == 0f) vx = 45f
                            x += vx * dt
                            facingRight = vx > 0f
                            if (x >= patrolMaxX) { x = patrolMaxX; vx = -abs(vx) }
                            else if (x <= patrolMinX) { x = patrolMinX; vx = abs(vx) }
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) state = EnemyState.CHASE
                    }
                    EnemyState.CHASE -> {
                        if (attackCooldownTimer <= 0f && !playerIsDead) {
                            state = EnemyState.ATTACK
                            warningTimer = 0.35f
                        } else {
                            val dirX = if (dx > 0) 1f else -1f
                            x = (x + dirX * 50f * dt).coerceIn(patrolMinX, patrolMaxX)
                        }
                    }
                    EnemyState.ATTACK -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            val angle = atan2(dy.toDouble(), dx.toDouble())
                            val speed = 220f
                            newProjectiles.add(
                                EnemyProjectile(
                                    x = cx, y = cy,
                                    vx = cos(angle).toFloat() * speed,
                                    vy = sin(angle).toFloat() * speed,
                                    radius = 11f,
                                    type = ProjectileType.LAVA_BALL,
                                    color = Color(0xFFF97316)
                                )
                            )
                            ShadowHeroAudioEngine.playLaserShoot()
                            attackCooldownTimer = 2.5f
                            state = EnemyState.CHASE
                        }
                    }
                    else -> {}
                }
            }

            EnemyType.NIGHT_ARCHER -> {
                val detRange = 340f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && dist < detRange) {
                            state = EnemyState.CHASE
                            if (!hasDetectedPlayer) {
                                hasDetectedPlayer = true
                                ShadowHeroAudioEngine.playEnemyDetection()
                            }
                        } else {
                            if (vx == 0f) vx = 75f
                            x += vx * dt
                            facingRight = vx > 0f
                            if (x >= patrolMaxX) { x = patrolMaxX; vx = -abs(vx) }
                            else if (x <= patrolMinX) { x = patrolMinX; vx = abs(vx) }
                        }
                    }
                    EnemyState.CHASE, EnemyState.RETREAT -> {
                        if (dist < 110f) {
                            // Retreat step backwards when player gets too close!
                            state = EnemyState.RETREAT
                            val retreatDir = if (dx > 0) -1f else 1f
                            x = (x + retreatDir * 120f * dt).coerceIn(patrolMinX, patrolMaxX)
                        } else {
                            state = EnemyState.CHASE
                        }

                        if (attackCooldownTimer <= 0f && dist in 110f..350f && !playerIsDead) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            targetX = playerX
                            targetY = playerY
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            state = EnemyState.ATTACK
                            warningTimer = 0.1f
                        }
                    }
                    EnemyState.ATTACK -> {
                        val angle = atan2((targetY - cy).toDouble(), (targetX - cx).toDouble())
                        val speed = 320f
                        newProjectiles.add(
                            EnemyProjectile(
                                x = cx, y = cy,
                                vx = cos(angle).toFloat() * speed,
                                vy = sin(angle).toFloat() * speed,
                                radius = 7f,
                                type = ProjectileType.ARROW,
                                color = Color(0xFFC084FC)
                            )
                        )
                        ShadowHeroAudioEngine.playLaserShoot()
                        attackCooldownTimer = 2.0f
                        state = EnemyState.CHASE
                    }
                    else -> {}
                }
            }

            EnemyType.SHIELD_GUARD -> {
                val detRange = 220f
                isShieldUp = true
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && abs(dx) < detRange && abs(dy) < 80f) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            if (!hasDetectedPlayer) {
                                hasDetectedPlayer = true
                                ShadowHeroAudioEngine.playEnemyDetection()
                            }
                        } else {
                            if (vx == 0f) vx = 55f
                            x += vx * dt
                            facingRight = vx > 0f
                            if (x >= patrolMaxX) { x = patrolMaxX; vx = -abs(vx) }
                            else if (x <= patrolMinX) { x = patrolMinX; vx = abs(vx) }
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) state = EnemyState.CHASE
                    }
                    EnemyState.CHASE -> {
                        val dirX = if (dx > 0) 1f else -1f
                        facingRight = dirX > 0f
                        x = (x + dirX * 65f * dt).coerceIn(patrolMinX, patrolMaxX)
                        if (dist < 45f && attackCooldownTimer <= 0f) {
                            state = EnemyState.ATTACK
                            warningTimer = 0.3f
                        }
                    }
                    EnemyState.ATTACK -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            if (dist < 52f && !playerIsDead) onDealDamage(1f)
                            attackCooldownTimer = 1.8f
                            state = EnemyState.CHASE
                        }
                    }
                    else -> {}
                }
            }

            // ==========================================
            // FLYING ENEMIES
            // ==========================================

            EnemyType.DARK_BAT -> {
                val detRange = 260f
                y = initialY + sin((animTimer * 6f).toDouble()).toFloat() * 12f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && dist < detRange) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            if (!hasDetectedPlayer) {
                                hasDetectedPlayer = true
                                ShadowHeroAudioEngine.playEnemyDetection()
                            }
                        } else {
                            if (vx == 0f) vx = 110f
                            x += vx * dt
                            facingRight = vx > 0f
                            if (x >= patrolMaxX) { x = patrolMaxX; vx = -abs(vx) }
                            else if (x <= patrolMinX) { x = patrolMinX; vx = abs(vx) }
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            state = EnemyState.ATTACK
                            targetX = playerX
                            targetY = playerY
                            warningTimer = 0.8f // Dive duration
                        }
                    }
                    EnemyState.ATTACK -> {
                        warningTimer -= dt
                        val angle = atan2((targetY - cy).toDouble(), (targetX - cx).toDouble())
                        val speed = 260f
                        x += cos(angle).toFloat() * speed * dt
                        y += sin(angle).toFloat() * speed * dt
                        if (dist < 40f && !playerIsDead) onDealDamage(1f)
                        if (warningTimer <= 0f) {
                            state = EnemyState.PATROL
                            attackCooldownTimer = 2.0f
                        }
                    }
                    else -> {}
                }
            }

            EnemyType.VOID_FLYER -> {
                val detRange = 300f
                y = initialY + sin((animTimer * 4f).toDouble()).toFloat() * 15f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && dist < detRange) {
                            state = EnemyState.CHASE
                            chaseTimer = 4.0f
                            if (!hasDetectedPlayer) {
                                hasDetectedPlayer = true
                                ShadowHeroAudioEngine.playEnemyDetection()
                            }
                        } else {
                            if (vx == 0f) vx = 90f
                            x += vx * dt
                            facingRight = vx > 0f
                            if (x >= patrolMaxX) { x = patrolMaxX; vx = -abs(vx) }
                            else if (x <= patrolMinX) { x = patrolMinX; vx = abs(vx) }
                        }
                    }
                    EnemyState.CHASE -> {
                        val angle = atan2(dy.toDouble(), dx.toDouble())
                        x += cos(angle).toFloat() * 65f * dt
                        y += sin(angle).toFloat() * 45f * dt
                        if (attackCooldownTimer <= 0f && dist < 260f && !playerIsDead) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            targetX = playerX
                            targetY = playerY
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            state = EnemyState.ATTACK
                            warningTimer = 0.1f
                        }
                    }
                    EnemyState.ATTACK -> {
                        val angle = atan2((targetY - cy).toDouble(), (targetX - cx).toDouble())
                        val speed = 250f
                        newProjectiles.add(
                            EnemyProjectile(
                                x = cx, y = cy,
                                vx = cos(angle).toFloat() * speed,
                                vy = sin(angle).toFloat() * speed,
                                radius = 9f,
                                type = ProjectileType.ENERGY_ORB,
                                color = Color(0xFFA855F7)
                            )
                        )
                        ShadowHeroAudioEngine.playLaserShoot()
                        attackCooldownTimer = 2.2f
                        state = EnemyState.CHASE
                    }
                    else -> {}
                }
            }

            EnemyType.SKULL_HAWK -> {
                val detRange = 320f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && dist < detRange) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            if (!hasDetectedPlayer) {
                                hasDetectedPlayer = true
                                ShadowHeroAudioEngine.playEnemyDetection()
                            }
                        } else {
                            if (vx == 0f) vx = 120f
                            x += vx * dt
                            y = initialY + cos((animTimer * 5f).toDouble()).toFloat() * 14f
                            facingRight = vx > 0f
                            if (x >= patrolMaxX) { x = patrolMaxX; vx = -abs(vx) }
                            else if (x <= patrolMinX) { x = patrolMinX; vx = abs(vx) }
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            state = EnemyState.ATTACK
                            targetX = playerX
                            targetY = playerY
                            warningTimer = 0.7f
                        }
                    }
                    EnemyState.ATTACK -> {
                        warningTimer -= dt
                        val angle = atan2((targetY - cy).toDouble(), (targetX - cx).toDouble())
                        val speed = 290f
                        x += cos(angle).toFloat() * speed * dt
                        y += sin(angle).toFloat() * speed * dt
                        if (dist < 42f && !playerIsDead) onDealDamage(1f)
                        if (warningTimer <= 0f) {
                            state = EnemyState.PATROL
                            attackCooldownTimer = 2.4f
                        }
                    }
                    else -> {}
                }
            }

            EnemyType.FLOATING_MAGE -> {
                val detRange = 340f
                y = initialY + sin((animTimer * 3.5f).toDouble()).toFloat() * 12f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && dist < detRange) {
                            state = EnemyState.CHASE
                            if (!hasDetectedPlayer) {
                                hasDetectedPlayer = true
                                ShadowHeroAudioEngine.playEnemyDetection()
                            }
                        }
                    }
                    EnemyState.CHASE -> {
                        // Short teleport shadow step if player approaches too close!
                        if (dist < 90f) {
                            val teleOffset = if (facingRight) -160f else 160f
                            x = (x + teleOffset).coerceIn(patrolMinX, patrolMaxX)
                            teleportTimer = 0.4f
                        }

                        if (attackCooldownTimer <= 0f && dist < 320f && !playerIsDead) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            targetX = playerX
                            targetY = playerY
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            state = EnemyState.ATTACK
                            warningTimer = 0.1f
                        }
                    }
                    EnemyState.ATTACK -> {
                        val angle = atan2((targetY - cy).toDouble(), (targetX - cx).toDouble())
                        val speed = 270f
                        newProjectiles.add(
                            EnemyProjectile(
                                x = cx, y = cy,
                                vx = cos(angle).toFloat() * speed,
                                vy = sin(angle).toFloat() * speed,
                                radius = 9f,
                                type = ProjectileType.MAGIC_BOLT,
                                color = Color(0xFF22D3EE)
                            )
                        )
                        ShadowHeroAudioEngine.playLaserShoot()
                        attackCooldownTimer = 2.2f
                        state = EnemyState.CHASE
                    }
                    else -> {}
                }
            }

            EnemyType.SHADOW_DRONE -> {
                val detRange = 380f
                y = initialY + sin((animTimer * 6f).toDouble()).toFloat() * 8f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && dist < detRange) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            laserWarningTimer = warningDuration
                            if (!hasDetectedPlayer) {
                                hasDetectedPlayer = true
                                ShadowHeroAudioEngine.playEnemyDetection()
                            }
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        laserWarningTimer = warningTimer
                        if (warningTimer <= 0f) {
                            state = EnemyState.ATTACK
                            warningTimer = 0.15f
                        }
                    }
                    EnemyState.ATTACK -> {
                        val angle = atan2(dy.toDouble(), dx.toDouble())
                        val speed = 380f
                        newProjectiles.add(
                            EnemyProjectile(
                                x = cx, y = cy,
                                vx = cos(angle).toFloat() * speed,
                                vy = sin(angle).toFloat() * speed,
                                radius = 6f,
                                type = ProjectileType.LASER_BEAM,
                                color = Color(0xFFEF4444)
                            )
                        )
                        ShadowHeroAudioEngine.playLaserShoot()
                        attackCooldownTimer = 2.5f
                        state = EnemyState.IDLE
                    }
                    else -> {
                        if (attackCooldownTimer <= 0f) state = EnemyState.PATROL
                    }
                }
            }

            EnemyType.VOID_JELLY -> {
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        y = initialY + sin((animTimer * 4f).toDouble()).toFloat() * 10f
                        if (!playerIsDead && abs(dx) < 60f && dy > 0f && dy < 280f) {
                            state = EnemyState.ATTACK // Drop down to crush player
                            warningTimer = 0.8f
                        }
                    }
                    EnemyState.ATTACK -> {
                        y += 240f * dt // Fast drop downward
                        if (dist < 42f && !playerIsDead) onDealDamage(1f)
                        if (y >= initialY + 200f || warningTimer <= 0f) {
                            state = EnemyState.RETREAT
                        }
                    }
                    EnemyState.RETREAT -> {
                        y -= 70f * dt // Slowly float back up
                        if (y <= initialY) {
                            y = initialY
                            state = EnemyState.PATROL
                        }
                    }
                    else -> {}
                }
            }

            // ==========================================
            // SPECIAL / BOSS ENEMIES
            // ==========================================

            EnemyType.SHADOW_KNIGHT -> {
                val detRange = 320f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && abs(dx) < detRange) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                        } else {
                            if (vx == 0f) vx = 110f
                            x += vx * dt
                            facingRight = vx > 0f
                            if (x >= patrolMaxX) { x = patrolMaxX; vx = -abs(vx) }
                            else if (x <= patrolMinX) { x = patrolMinX; vx = abs(vx) }
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) state = EnemyState.CHASE
                    }
                    EnemyState.CHASE -> {
                        if (attackCooldownTimer <= 0f && dist < 180f && !playerIsDead) {
                            state = EnemyState.ATTACK // Dash-slash combo
                            warningTimer = 0.35f
                            chargeVx = if (facingRight) 320f else -320f
                        } else {
                            val dirX = if (dx > 0) 1f else -1f
                            x = (x + dirX * 110f * dt).coerceIn(patrolMinX, patrolMaxX)
                        }
                    }
                    EnemyState.ATTACK -> {
                        warningTimer -= dt
                        x = (x + chargeVx * dt).coerceIn(patrolMinX, patrolMaxX)
                        if (dist < 60f && !playerIsDead) onDealDamage(1f)
                        if (warningTimer <= 0f) {
                            attackCooldownTimer = 2.0f
                            state = EnemyState.CHASE
                        }
                    }
                    else -> {}
                }
            }

            EnemyType.CORRUPTED_BEAST -> {
                val detRange = 300f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && dist < detRange) state = EnemyState.CHASE
                        else {
                            if (vx == 0f) vx = 95f
                            x += vx * dt
                            facingRight = vx > 0f
                            if (x >= patrolMaxX) { x = patrolMaxX; vx = -abs(vx) }
                            else if (x <= patrolMinX) { x = patrolMinX; vx = abs(vx) }
                        }
                    }
                    EnemyState.CHASE -> {
                        if (attackCooldownTimer <= 0f && dist < 90f && !playerIsDead) {
                            state = EnemyState.ATTACK // Area Slam
                            warningTimer = 0.45f
                        } else {
                            val dirX = if (dx > 0) 1f else -1f
                            x = (x + dirX * 95f * dt).coerceIn(patrolMinX, patrolMaxX)
                        }
                    }
                    EnemyState.ATTACK -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            groundSlamRadius = 1f
                            if (dist < 110f && !playerIsDead) onDealDamage(1f)
                            attackCooldownTimer = 2.5f
                            state = EnemyState.CHASE
                        }
                    }
                    else -> {}
                }
            }

            EnemyType.VOID_OVERLORD -> {
                val detRange = 360f
                y = initialY + sin((animTimer * 2.5f).toDouble()).toFloat() * 16f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && dist < detRange) state = EnemyState.CHASE
                    }
                    EnemyState.CHASE -> {
                        if (attackCooldownTimer <= 0f && !playerIsDead) {
                            state = EnemyState.ATTACK
                            warningTimer = 0.4f
                            targetX = playerX
                            targetY = playerY
                        }
                    }
                    EnemyState.ATTACK -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            // 3-way dark energy orb spread
                            val baseAngle = atan2((targetY - cy).toDouble(), (targetX - cx).toDouble())
                            for (offset in listOf(-0.25, 0.0, 0.25)) {
                                val angle = baseAngle + offset
                                val speed = 260f
                                newProjectiles.add(
                                    EnemyProjectile(
                                        x = cx, y = cy,
                                        vx = cos(angle).toFloat() * speed,
                                        vy = sin(angle).toFloat() * speed,
                                        radius = 10f,
                                        type = ProjectileType.VOID_BURST,
                                        color = Color(0xFFA855F7)
                                    )
                                )
                            }
                            ShadowHeroAudioEngine.playLaserShoot()
                            attackCooldownTimer = 2.8f
                            state = EnemyState.CHASE
                        }
                    }
                    else -> {}
                }
            }

            EnemyType.LAVA_TITAN -> {
                val detRange = 320f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        if (!playerIsDead && dist < detRange) state = EnemyState.CHASE
                    }
                    EnemyState.CHASE -> {
                        if (attackCooldownTimer <= 0f && !playerIsDead) {
                            state = EnemyState.ATTACK
                            warningTimer = 0.5f
                        } else {
                            val dirX = if (dx > 0) 1f else -1f
                            x = (x + dirX * 40f * dt).coerceIn(patrolMinX, patrolMaxX)
                        }
                    }
                    EnemyState.ATTACK -> {
                        warningTimer -= dt
                        if (warningTimer <= 0f) {
                            groundSlamRadius = 1f
                            // Launch 2 lava boulders
                            for (dir in listOf(-1f, 1f)) {
                                newProjectiles.add(
                                    EnemyProjectile(
                                        x = cx, y = cy,
                                        vx = dir * 180f, vy = -180f,
                                        radius = 12f,
                                        type = ProjectileType.LAVA_BALL,
                                        color = Color(0xFFF97316)
                                    )
                                )
                            }
                            if (dist < 120f && !playerIsDead) onDealDamage(1f)
                            attackCooldownTimer = 3.2f
                            state = EnemyState.CHASE
                        }
                    }
                    else -> {}
                }
            }

            EnemyType.SHADOW_WORM -> {
                val detRange = 300f
                when (state) {
                    EnemyState.IDLE, EnemyState.PATROL -> {
                        burrowDepth = 1f // Buried underground
                        if (!playerIsDead && abs(dx) < detRange) {
                            state = EnemyState.DETECT
                            warningTimer = warningDuration
                            targetX = playerX
                        }
                    }
                    EnemyState.DETECT -> {
                        warningTimer -= dt
                        x = (targetX - width / 2f).coerceIn(patrolMinX, patrolMaxX)
                        if (warningTimer <= 0f) {
                            state = EnemyState.ATTACK
                            warningTimer = 1.2f // Emerge timer
                        }
                    }
                    EnemyState.ATTACK -> {
                        warningTimer -= dt
                        burrowDepth = (warningTimer / 1.2f).coerceIn(0f, 1f) // Emerges upwards
                        if (dist < 60f && burrowDepth < 0.3f && !playerIsDead) {
                            onDealDamage(1f)
                        }
                        if (warningTimer <= 0f) {
                            state = EnemyState.BURROWED
                            warningTimer = 1.0f
                        }
                    }
                    EnemyState.BURROWED -> {
                        warningTimer -= dt
                        burrowDepth = (1f - warningTimer / 1.0f).coerceIn(0f, 1f) // Burrows back down
                        if (warningTimer <= 0f) {
                            attackCooldownTimer = 3.0f
                            state = EnemyState.PATROL
                        }
                    }
                    else -> {}
                }
            }

            else -> {}
        }
    }
}

fun selectEnemyForStage(
    stage: Int,
    isFlying: Boolean = false,
    isElite: Boolean = false,
    random: java.util.Random = java.util.Random()
): EnemyType {
    if (isElite || (stage >= 10 && random.nextFloat() < 0.20f)) {
        val elites = listOf(
            EnemyType.SHADOW_KNIGHT,
            EnemyType.CORRUPTED_BEAST,
            EnemyType.VOID_OVERLORD,
            EnemyType.LAVA_TITAN,
            EnemyType.SHADOW_WORM
        )
        return elites[random.nextInt(elites.size)]
    }

    if (isFlying) {
        val earlyFlying = listOf(EnemyType.DARK_BAT, EnemyType.VOID_FLYER, EnemyType.VOID_JELLY)
        val midFlying = listOf(EnemyType.SKULL_HAWK, EnemyType.FLOATING_MAGE, EnemyType.SHADOW_DRONE)
        val lateFlying = listOf(EnemyType.FLOATING_MAGE, EnemyType.SHADOW_DRONE, EnemyType.VOID_FLYER, EnemyType.SKULL_HAWK)

        val pool = when {
            stage <= 5 -> earlyFlying
            stage <= 10 -> midFlying
            else -> lateFlying
        }
        return pool[random.nextInt(pool.size)]
    } else {
        val earlyGround = listOf(EnemyType.SHADOW_WARRIOR, EnemyType.SPIKE_BEAST, EnemyType.NIGHT_ARCHER)
        val midGround = listOf(EnemyType.SHADOW_BRUTE, EnemyType.SHIELD_GUARD, EnemyType.LAVA_GOLEM, EnemyType.NIGHT_ARCHER)
        val lateGround = listOf(EnemyType.SHIELD_GUARD, EnemyType.LAVA_GOLEM, EnemyType.SHADOW_BRUTE, EnemyType.SPIKE_BEAST, EnemyType.SHADOW_WARRIOR)

        val pool = when {
            stage <= 5 -> earlyGround
            stage <= 10 -> midGround
            else -> lateGround
        }
        return pool[random.nextInt(pool.size)]
    }
}

