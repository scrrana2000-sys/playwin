package com.myplaywin.app.shadowhero.engine

import androidx.compose.ui.geometry.Rect
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class EnemyType {
    SHADOW_WALKER,
    FLYING_ORB,
    TURRET,
    CHASER
}

data class EnemyProjectile(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float = 8f,
    var lifeTime: Float = 3.5f
) {
    val bounds: Rect
        get() = Rect(x - radius, y - radius, x + radius, y + radius)

    fun update(dt: Float): Boolean {
        x += vx * dt
        y += vy * dt
        lifeTime -= dt
        return lifeTime > 0f
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
    val width: Float = 34f,
    val height: Float = 38f,
    val patrolMinX: Float = initialX - 120f,
    val patrolMaxX: Float = initialX + 120f,
    var facingRight: Boolean = true,
    var fireTimer: Float = 0f,
    val fireCooldown: Float = 2.0f
) {
    val bounds: Rect
        get() = Rect(x, y, x + width, y + height)

    fun reset() {
        x = initialX
        y = initialY
        vx = if (type == EnemyType.SHADOW_WALKER) 80f else 0f
        vy = 0f
        facingRight = true
        fireTimer = 0f
    }

    fun update(dt: Float, playerX: Float, playerY: Float, newProjectiles: MutableList<EnemyProjectile>) {
        when (type) {
            EnemyType.SHADOW_WALKER -> {
                if (vx == 0f) vx = 80f
                x += vx * dt
                if (x >= patrolMaxX) {
                    x = patrolMaxX
                    vx = -abs(vx)
                    facingRight = false
                } else if (x <= patrolMinX) {
                    x = patrolMinX
                    vx = abs(vx)
                    facingRight = true
                }
            }

            EnemyType.FLYING_ORB -> {
                if (vx == 0f) vx = 100f
                x += vx * dt
                y = initialY + sin((x * 0.02f).toDouble()).toFloat() * 18f
                if (x >= patrolMaxX) {
                    x = patrolMaxX
                    vx = -abs(vx)
                } else if (x <= patrolMinX) {
                    x = patrolMinX
                    vx = abs(vx)
                }
            }

            EnemyType.TURRET -> {
                val dx = playerX - (x + width / 2f)
                val dy = playerY - (y + height / 2f)
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                fireTimer += dt
                if (dist <= 380f && fireTimer >= fireCooldown) {
                    fireTimer = 0f
                    val angle = atan2(dy.toDouble(), dx.toDouble())
                    val speed = 220f
                    newProjectiles.add(
                        EnemyProjectile(
                            x = x + width / 2f,
                            y = y + height / 2f,
                            vx = cos(angle).toFloat() * speed,
                            vy = sin(angle).toFloat() * speed
                        )
                    )
                }
            }

            EnemyType.CHASER -> {
                val dx = playerX - x
                val dy = playerY - y
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                if (dist in 10f..320f) {
                    val speed = 90f
                    x += (dx / dist) * speed * dt
                    y += (dy / dist) * speed * dt
                } else {
                    val origDx = initialX - x
                    val origDy = initialY - y
                    val origDist = kotlin.math.sqrt(origDx * origDx + origDy * origDy)
                    if (origDist > 5f) {
                        x += (origDx / origDist) * 40f * dt
                        y += (origDy / origDist) * 40f * dt
                    }
                }
            }
        }
    }
}
