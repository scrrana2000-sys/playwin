package com.myplaywin.app.shadowhero.engine

import androidx.compose.ui.geometry.Rect

enum class SpikeOrientation {
    UP, DOWN, LEFT, RIGHT
}

data class LevelSpike(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float = 30f,
    val height: Float = 24f,
    val orientation: SpikeOrientation = SpikeOrientation.UP
) {
    val bounds: Rect
        get() = Rect(x, y, x + width, y + height)
}

data class LevelMovingSpike(
    val id: String,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val width: Float = 30f,
    val height: Float = 24f,
    val speed: Float = 100f,
    var currentX: Float = startX,
    var currentY: Float = startY,
    var moveProgress: Float = 0f,
    var movingForward: Boolean = true
) {
    val bounds: Rect
        get() = Rect(currentX, currentY, currentX + width, currentY + height)

    fun reset() {
        currentX = startX
        currentY = startY
        moveProgress = 0f
        movingForward = true
    }

    fun update(dt: Float) {
        val totalDistX = endX - startX
        val totalDistY = endY - startY
        val totalDist = kotlin.math.sqrt(totalDistX * totalDistX + totalDistY * totalDistY)
        if (totalDist <= 0f) return

        val step = (speed * dt) / totalDist
        if (movingForward) {
            moveProgress += step
            if (moveProgress >= 1f) {
                moveProgress = 1f
                movingForward = false
            }
        } else {
            moveProgress -= step
            if (moveProgress <= 0f) {
                moveProgress = 0f
                movingForward = true
            }
        }
        currentX = startX + totalDistX * moveProgress
        currentY = startY + totalDistY * moveProgress
    }
}

data class LevelRotatingBlade(
    val id: String,
    val centerX: Float,
    val centerY: Float,
    val radius: Float = 28f,
    val rotationSpeed: Float = 180f,
    var currentAngle: Float = 0f,
    val orbitRadius: Float = 0f,
    val orbitSpeed: Float = 0f,
    var currentOrbitAngle: Float = 0f
) {
    val currentCenterX: Float
        get() = if (orbitRadius > 0f) centerX + kotlin.math.cos(Math.toRadians(currentOrbitAngle.toDouble())).toFloat() * orbitRadius else centerX

    val currentCenterY: Float
        get() = if (orbitRadius > 0f) centerY + kotlin.math.sin(Math.toRadians(currentOrbitAngle.toDouble())).toFloat() * orbitRadius else centerY

    val bounds: Rect
        get() {
            val cx = currentCenterX
            val cy = currentCenterY
            return Rect(cx - radius * 0.75f, cy - radius * 0.75f, cx + radius * 0.75f, cy + radius * 0.75f)
        }

    fun reset() {
        currentAngle = 0f
        currentOrbitAngle = 0f
    }

    fun update(dt: Float) {
        currentAngle = (currentAngle + rotationSpeed * dt) % 360f
        if (orbitRadius > 0f) {
            currentOrbitAngle = (currentOrbitAngle + orbitSpeed * dt) % 360f
        }
    }
}

enum class LaserState {
    INACTIVE,
    WARNING,
    ACTIVE
}

data class LevelLaserBeam(
    val id: String,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val warningDuration: Float = 0.8f,
    val activeDuration: Float = 1.5f,
    val inactiveDuration: Float = 1.5f,
    var cycleTimer: Float = 0f,
    var state: LaserState = LaserState.INACTIVE,
    val isVertical: Boolean = false
) {
    val beamBounds: Rect
        get() {
            return if (isVertical) {
                Rect(startX - 8f, startY, startX + 8f, endY)
            } else {
                Rect(startX, startY - 8f, endX, startY + 8f)
            }
        }

    fun reset() {
        cycleTimer = 0f
        state = LaserState.INACTIVE
    }

    fun update(dt: Float) {
        cycleTimer += dt
        val totalCycle = inactiveDuration + warningDuration + activeDuration
        val currentInCycle = cycleTimer % totalCycle

        state = when {
            currentInCycle < inactiveDuration -> LaserState.INACTIVE
            currentInCycle < (inactiveDuration + warningDuration) -> LaserState.WARNING
            else -> LaserState.ACTIVE
        }
    }
}

enum class EnvHazardType {
    LAVA,
    ELECTRIC_FLOOR,
    POISON_MIST
}

data class LevelEnvHazard(
    val id: String,
    val bounds: Rect,
    val type: EnvHazardType
)

enum class PlatformBehaviorType {
    NORMAL,
    MOVING,
    FALLING,
    DISAPPEARING,
    BREAKABLE
}
