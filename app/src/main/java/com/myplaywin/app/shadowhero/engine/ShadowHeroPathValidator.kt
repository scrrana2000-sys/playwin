package com.myplaywin.app.shadowhero.engine

import androidx.compose.ui.geometry.Rect
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Level Path Validator for Shadow Hero.
 * Verifies that a generated level is physically 100% playable and reachable
 * using the player's movement capabilities (run jump, double jump, wall jump, dash).
 */
data class PathValidationResult(
    val isValid: Boolean,
    val failureReason: String? = null,
    val totalNodesChecked: Int = 0
)

object ShadowHeroPathValidator {

    // Maximum physical reach limits based on Shadow Hero physics constants
    private const val MAX_HORIZONTAL_JUMP = 320f     // Standard run jump
    private const val MAX_DOUBLE_JUMP_DIST = 460f    // Double jump span
    private const val MAX_DASH_JUMP_DIST = 580f      // Jump + Dash span
    private const val MAX_JUMP_HEIGHT = 210f         // Double jump height
    private const val MAX_FALL_DROP = 600f           // Safe drop before requiring platform
    private const val WALL_SHAFT_MIN_WIDTH = 120f
    private const val WALL_SHAFT_MAX_WIDTH = 320f

    /**
     * Validates if a level is solvable from spawn to exit portal.
     */
    fun validateLevel(level: GeneratedLevel): PathValidationResult {
        val platforms = level.platforms
        val startX = level.spawnX
        val spawnY = level.spawnY
        val exitBounds = level.exitPortal.bounds

        // 1. Basic Geometry Checks
        if (platforms.isEmpty()) {
            return PathValidationResult(false, "No platforms present in level")
        }

        // Check spawn platform existence
        val spawnPlatform = platforms.find { plat ->
            startX in plat.bounds.left..plat.bounds.right &&
                    spawnY >= plat.bounds.top - 100f && spawnY <= plat.bounds.top + 20f
        }
        if (spawnPlatform == null) {
            return PathValidationResult(false, "Spawn position does not land on a platform")
        }

        // Check exit platform existence
        val exitPlatform = platforms.find { plat ->
            exitBounds.center.x in (plat.bounds.left - 40f)..(plat.bounds.right + 40f) &&
                    abs(exitBounds.bottom - plat.bounds.top) < 30f
        }
        if (exitPlatform == null) {
            return PathValidationResult(false, "Exit portal has no solid floor platform underneath")
        }

        // 2. Build Graph Reachability
        // Nodes represent platforms and key waypoints
        val reachablePlatforms = mutableSetOf<String>()
        val queue = ArrayDeque<LevelPlatform>()

        reachablePlatforms.add(spawnPlatform.id)
        queue.add(spawnPlatform)

        var exitReachable = false

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            // Check if current platform can directly reach exit
            if (canReachExit(current.bounds, exitBounds)) {
                exitReachable = true
            }

            // Test connectivity to all other platforms
            for (nextPlat in platforms) {
                if (nextPlat.id in reachablePlatforms) continue

                if (canTraverse(current, nextPlat)) {
                    reachablePlatforms.add(nextPlat.id)
                    queue.add(nextPlat)
                }
            }
        }

        if (!exitReachable && reachablePlatforms.none { id -> id == exitPlatform.id }) {
            return PathValidationResult(
                false,
                "Exit portal is unreachable from spawn platform",
                reachablePlatforms.size
            )
        }

        // 3. Verify Checkpoints are reachable
        for (cp in level.checkpoints) {
            val cpReachable = platforms.any { plat ->
                plat.id in reachablePlatforms &&
                        cp.x in (plat.bounds.left - 50f)..(plat.bounds.right + 50f) &&
                        abs((cp.y + cp.height) - plat.bounds.top) < 40f
            }
            if (!cpReachable) {
                return PathValidationResult(
                    false,
                    "Checkpoint ${cp.id} is placed on an unreachable platform",
                    reachablePlatforms.size
                )
            }
        }

        // 4. Verify Obstacle & Hazard Fairness
        val obstacleValidation = validateObstacles(level)
        if (!obstacleValidation.isValid) {
            return obstacleValidation
        }

        return PathValidationResult(true, null, reachablePlatforms.size)
    }

    /**
     * Validates that obstacles, traps, and enemies adhere to fairness rules.
     */
    fun validateObstacles(level: GeneratedLevel): PathValidationResult {
        val spawnX = level.spawnX
        val spawnY = level.spawnY
        val exitBounds = level.exitPortal.bounds

        // 1. Spawn Safe Zone: No hazards within 220px of spawn
        for (spike in level.spikes) {
            if (abs(spike.x - spawnX) < 220f && abs(spike.y - spawnY) < 180f) {
                return PathValidationResult(false, "Spike too close to spawn point")
            }
        }
        for (mSpike in level.movingSpikes) {
            if (abs(mSpike.startX - spawnX) < 220f && abs(mSpike.startY - spawnY) < 180f) {
                return PathValidationResult(false, "Moving spike too close to spawn point")
            }
        }
        for (blade in level.blades) {
            if (abs(blade.centerX - spawnX) < 220f && abs(blade.centerY - spawnY) < 180f) {
                return PathValidationResult(false, "Blade too close to spawn point")
            }
        }
        for (laser in level.lasers) {
            if (abs(laser.startX - spawnX) < 220f && abs(laser.startY - spawnY) < 180f) {
                return PathValidationResult(false, "Laser beam too close to spawn point")
            }
        }
        for (enemy in level.enemies) {
            if (abs(enemy.initialX - spawnX) < 220f && abs(enemy.initialY - spawnY) < 180f) {
                return PathValidationResult(false, "Enemy spawned too close to spawn point")
            }
        }

        // 2. Exit Portal Safe Zone: No hazards directly blocking exit portal
        for (spike in level.spikes) {
            if (abs(spike.x - exitBounds.center.x) < 150f && abs(spike.y - exitBounds.center.y) < 120f) {
                return PathValidationResult(false, "Spike directly blocking exit portal")
            }
        }
        for (enemy in level.enemies) {
            if (abs(enemy.initialX - exitBounds.center.x) < 150f && abs(enemy.initialY - exitBounds.center.y) < 120f) {
                return PathValidationResult(false, "Enemy camped directly at exit portal")
            }
        }

        // 3. Checkpoints Safe Zone: No hazards overlapping checkpoints
        for (cp in level.checkpoints) {
            val cpBounds = cp.bounds
            for (spike in level.spikes) {
                if (cpBounds.overlaps(spike.bounds)) {
                    return PathValidationResult(false, "Spike overlaps checkpoint ${cp.id}")
                }
            }
            for (enemy in level.enemies) {
                if (cpBounds.overlaps(enemy.bounds)) {
                    return PathValidationResult(false, "Enemy overlaps checkpoint ${cp.id}")
                }
            }
        }

        return PathValidationResult(true, null)
    }

    private fun canReachExit(fromBounds: Rect, exitBounds: Rect): Boolean {
        val dx = exitBounds.center.x - fromBounds.center.x
        val dy = fromBounds.top - exitBounds.bottom // Positive if exit is higher

        if (abs(dx) <= MAX_DASH_JUMP_DIST && dy <= MAX_JUMP_HEIGHT && dy >= -MAX_FALL_DROP) {
            return true
        }
        return false
    }

    private fun canTraverse(fromPlat: LevelPlatform, toPlat: LevelPlatform): Boolean {
        val b1 = fromPlat.bounds
        val b2 = toPlat.bounds

        // Horizontal gap distance between platform edges
        val horizontalGap = when {
            b2.left > b1.right -> b2.left - b1.right
            b1.left > b2.right -> b1.left - b2.right
            else -> 0f // Overlapping horizontally
        }

        // Vertical difference (positive if target is higher)
        val heightDiff = b1.top - b2.top

        // Wall Jump Shaft Exception: If both are vertical wall shaft walls
        if (fromPlat.isWall || toPlat.isWall) {
            val dx = abs(b2.center.x - b1.center.x)
            if (dx in WALL_SHAFT_MIN_WIDTH..WALL_SHAFT_MAX_WIDTH) {
                return true
            }
        }

        // Case 1: Target platform is at similar height or lower
        if (heightDiff >= -MAX_FALL_DROP && heightDiff <= 0f) {
            // Drop down / flat jump
            if (horizontalGap <= MAX_DASH_JUMP_DIST) {
                return true
            }
        }

        // Case 2: Target platform is higher
        if (heightDiff > 0f && heightDiff <= MAX_JUMP_HEIGHT) {
            if (horizontalGap <= MAX_DOUBLE_JUMP_DIST) {
                return true
            }
        }

        // Case 3: High platform requiring dash jump
        if (heightDiff in 0f..MAX_JUMP_HEIGHT && horizontalGap <= MAX_DASH_JUMP_DIST) {
            return true
        }

        return false
    }
}
