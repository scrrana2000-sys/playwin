package com.myplaywin.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myplaywin.app.audio.BounceAudioEngine
import com.myplaywin.app.data.BounceProgressionManager
import com.myplaywin.app.data.BounceAchievement
import com.myplaywin.app.data.BounceGameStats
import com.myplaywin.app.data.BounceDailyMission
import com.playwin.ads.BannerManager
import com.playwin.ads.RewardedManager
import com.playwin.ads.RewardType
import com.playwin.ads.RewardCallback
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.ui.viewmodel.PlayWinViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import kotlin.math.*
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

// --- DATA STRUCTURES ---
enum class SpikeDirection { UP, DOWN, LEFT, RIGHT }

enum class EnemyType { WALKING, FLYING, ROTATING_HAZARD }

data class BounceEnemy(
    val id: Int,
    val type: EnemyType,
    val x: Float,
    val y: Float,
    val width: Float = 28f,
    val height: Float = 28f,
    val moveRangeX: Float = 100f,
    val moveRangeY: Float = 0f,
    val moveSpeed: Float = 60f,
    val initialX: Float = x,
    val initialY: Float = y
)

data class BounceKey(
    val id: Int,
    val x: Float,
    val y: Float,
    val colorHex: Long = 0xFFFFD700,
    var isCollected: Boolean = false
)

data class BounceDoor(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float = 24f,
    val height: Float = 80f,
    val keyIdNeeded: Int,
    val keyColorHex: Long = 0xFFFFD700,
    var isUnlocked: Boolean = false,
    var unlockAnimProgress: Float = 0f
)

data class BounceWaterZone(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val waterColor: Color = Color(0x6600B0FF)
)

enum class InteractiveType { BREAKABLE, PUSHABLE_BOX, BOUNCE_PAD, SECRET_PASSAGEWAY }

data class BounceInteractiveBlock(
    val id: Int,
    val type: InteractiveType,
    val x: Float,
    val y: Float,
    val width: Float = 40f,
    val height: Float = 40f,
    var durability: Int = 1,
    var isDestroyed: Boolean = false,
    var currentX: Float = x,
    var currentY: Float = y,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var opacity: Float = 1f
)

data class BounceObstacle(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isSpike: Boolean = false,
    val spikeDirection: SpikeDirection = SpikeDirection.UP,
    val isMoving: Boolean = false,
    val moveRangeX: Float = 0f,
    val moveRangeY: Float = 0f,
    val moveSpeed: Float = 0.05f,
    val initialX: Float = x,
    val initialY: Float = y,
    val isFallingPlatform: Boolean = false,
    val fallDelay: Float = 0.35f,
    val respawnDelay: Float = 3.0f,
    val isSpring: Boolean = false,
    val springForce: Float = -650f,
    val isExitPlatform: Boolean = false
) {
    val topCenter: androidx.compose.ui.geometry.Offset get() = androidx.compose.ui.geometry.Offset(x + width / 2f, y)
}

data class BounceCheckpoint(
    val id: Int,
    val x: Float,
    val y: Float,
    var isActivated: Boolean = false
)

data class BounceCollectible(
    val x: Float,
    val y: Float,
    val isStar: Boolean = false,
    val isBonus: Boolean = false,
    var isCollected: Boolean = false
)

data class BounceLevel(
    val number: Int,
    val name: String,
    val description: String,
    val width: Float, // Total scrollable level width
    val height: Float, // Total scrollable level height
    val startX: Float,
    val startY: Float,
    val portalX: Float,
    val portalY: Float,
    val platforms: List<BounceObstacle>,
    val collectibles: List<BounceCollectible>,
    val checkpoints: List<BounceCheckpoint> = emptyList(),
    val enemies: List<BounceEnemy> = emptyList(),
    val keys: List<BounceKey> = emptyList(),
    val doors: List<BounceDoor> = emptyList(),
    val waterZones: List<BounceWaterZone> = emptyList(),
    val interactiveBlocks: List<BounceInteractiveBlock> = emptyList(),
    val baseRewardCoins: Int
)

data class BounceHistoryEntry(
    val date: String,
    val levelName: String,
    val stars: Int,
    val coins: Int,
    val score: Int
)

// --- BALL SKINS CATALOG ---
data class BallSkin(
    val id: String,
    val name: String,
    val priceCoins: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val darkColor: Color,
    val trailColor: Color,
    val description: String
)

val BALL_SKINS = listOf(
    BallSkin("skin_neon_violet", "Neon Violet", 0, Color(0xFFA855F7), Color(0xFF7C3AED), Color(0xFF3B0764), Color(0xFFA855F7), "Classic neon violet orb with purple energy."),
    BallSkin("skin_cyber_cyan", "Cyber Cyan", 150, Color(0xFF00E5FF), Color(0xFF0091EA), Color(0xFF00264D), Color(0xFF00E5FF), "High-tech cyan glow with circuit spark trail."),
    BallSkin("skin_solar_flare", "Solar Amber", 300, Color(0xFFFFD700), Color(0xFFFF6D00), Color(0xFF802000), Color(0xFFFFD700), "Blazing fiery gold sphere powered by solar energy."),
    BallSkin("skin_emerald_dragon", "Emerald Dragon", 450, Color(0xFF00E676), Color(0xFF00A152), Color(0xFF003B19), Color(0xFF00E676), "Mythical green orb with shimmering emerald aura."),
    BallSkin("skin_cosmic_magenta", "Cosmic Void", 600, Color(0xFFFF00D6), Color(0xFFC2185B), Color(0xFF4A0033), Color(0xFFFF00D6), "Deep cosmic magenta sphere born from interstellar dust."),
    BallSkin("skin_rainbow_sparkle", "Prism Rainbow", 1000, Color(0xFFFF1744), Color(0xFF00E5FF), Color(0xFFFFEA00), Color(0xFFFF00D6), "Legendary rainbow orb that cycles prismatic energy.")
)

fun getLevelAccentColor(levelNumber: Int): Color {
    return when ((levelNumber - 1) % 10 + 1) {
        1 -> Color(0xFF22C55E) // Forest Emerald
        2 -> Color(0xFF38BDF8) // Ice Glacier Blue
        3 -> Color(0xFFFF3D00) // Lava Magma Red
        4 -> Color(0xFFF59E0B) // Factory Amber Gold
        5 -> Color(0xFFA855F7) // Castle Royal Purple
        6 -> Color(0xFF06B6D4) // Underwater Aquamarine
        7 -> Color(0xFF00E5FF) // Sky Electric Cyan
        8 -> Color(0xFF10B981) // Jungle Overgrown Green
        9 -> Color(0xFFFFD700) // Desert Sunken Gold
        10 -> Color(0xFFE081FF) // Crystal Cave Amethyst
        else -> Color(0xFF7C3AED)
    }
}

// --- SAFE COERCE IN RANGE EXTENSIONS ---
fun Float.safeCoerceIn(minimumValue: Float, maximumValue: Float): Float {
    if (minimumValue > maximumValue) {
        android.util.Log.w("BounceClassic", "safeCoerceIn invalid range: min=$minimumValue, max=$maximumValue, value=$this. Falling back to minimumValue.")
        return minimumValue
    }
    return this.coerceIn(minimumValue, maximumValue)
}

fun Int.safeCoerceIn(minimumValue: Int, maximumValue: Int): Int {
    if (minimumValue > maximumValue) {
        android.util.Log.w("BounceClassic", "safeCoerceIn invalid range: min=$minimumValue, max=$maximumValue, value=$this. Falling back to minimumValue.")
        return minimumValue
    }
    return this.coerceIn(minimumValue, maximumValue)
}

// --- STRICT LEVEL DESIGN & VALIDATION ENGINE ---
// Validates and auto-balances every level according to strict gameplay rules:
// 1. Normal Jump Rule: Max vertical rise <= 110f (80% of player max normal jump height ~140f)
// 2. Spring Rule: Spring launch <= 210f with guaranteed matching landing platform
// 3. Platform Generator: Validates & repositions platforms, auto-inserts bridge platforms for wide gaps
// 4. Vertical Progression: Smooth stair-like progression without large vertical voids
// 5. Camera Compatibility: Clamps platform Y to [80f, level.height - 100f] so player stays in playable view
// 6. Reachability Validation: Guarantees checkpoints, keys, doors, exit portal & collectibles are reachable
fun sanitizeAndValidateLevel(level: BounceLevel): BounceLevel {
    val maxNormalJumpRise = 110f // 80% of player max normal jump height (~140f)
    val maxSpringJumpRise = 210f // Max safe vertical reach for spring pads (~270f launch)
    val maxHorizontalGap = 220f  // Max safe horizontal gap between platform edges
    val minPlatformY = 80f
    val maxPlatformY = (level.height - 100f).coerceAtLeast(minPlatformY + 50f)

    val originalPlatforms = level.platforms
    val walkable = mutableListOf<BounceObstacle>()
    val hazards = mutableListOf<BounceObstacle>()

    for (p in originalPlatforms) {
        if (p.isSpike || p.spikeDirection != null) {
            hazards.add(p)
        } else {
            walkable.add(p)
        }
    }

    walkable.sortBy { it.x }

    val sanitizedPlatforms = mutableListOf<BounceObstacle>()

    if (walkable.isEmpty() || walkable.first().x > level.startX + 200f) {
        val startPlat = BounceObstacle(
            x = (level.startX - 50f).coerceAtLeast(0f),
            y = (level.startY + 40f).safeCoerceIn(minPlatformY, maxPlatformY),
            width = 350f,
            height = 120f
        )
        sanitizedPlatforms.add(startPlat)
    } else {
        val first = walkable.removeAt(0)
        val clampedY = first.y.safeCoerceIn(minPlatformY, maxPlatformY)
        sanitizedPlatforms.add(first.copy(y = clampedY))
    }

    for (plat in walkable) {
        val prev = sanitizedPlatforms.last()

        val gapX = plat.x - (prev.x + prev.width)
        val riseY = prev.y - plat.y

        val maxAllowedRise = if (prev.isSpring) maxSpringJumpRise else maxNormalJumpRise

        var newX = plat.x
        var newY = plat.y

        // Vertical adjustment according to Normal Jump and Spring rules
        if (riseY > maxAllowedRise) {
            newY = prev.y - maxAllowedRise
        } else if (riseY < -280f) {
            newY = prev.y + 220f
        }
        newY = newY.safeCoerceIn(minPlatformY, maxPlatformY)

        // Horizontal bridge insertion for wide gaps
        if (gapX > maxHorizontalGap) {
            val bridgeX = prev.x + prev.width + 40f
            val bridgeWidth = (gapX - 80f).safeCoerceIn(120f, 200f)
            val bridgeY = ((prev.y + newY) / 2f).safeCoerceIn(minPlatformY, maxPlatformY)

            sanitizedPlatforms.add(
                BounceObstacle(
                    x = bridgeX,
                    y = bridgeY,
                    width = bridgeWidth,
                    height = 35f,
                    isMoving = false
                )
            )
            newX = bridgeX + bridgeWidth + 50f
        }

        // Spring landing pad rule
        if (prev.isSpring) {
            val springLandingX = prev.x + 80f
            val springLandingY = (prev.y - 180f).safeCoerceIn(minPlatformY, maxPlatformY)
            if (newY < prev.y - maxSpringJumpRise || newX > prev.x + 350f) {
                newX = springLandingX
                newY = springLandingY
            }
        }

        sanitizedPlatforms.add(plat.copy(x = newX, y = newY))
    }

    for (h in hazards) {
        val clampedHY = h.y.safeCoerceIn(minPlatformY, (level.height - 40f).coerceAtLeast(minPlatformY))
        sanitizedPlatforms.add(h.copy(y = clampedHY))
    }

    val finalWalkable = sanitizedPlatforms.filter { !it.isSpike && it.spikeDirection == null }.sortedBy { it.x }

    fun findClosestPlatform(targetX: Float): BounceObstacle {
        return finalWalkable.minByOrNull { Math.abs((it.x + it.width / 2f) - targetX) }
            ?: BounceObstacle(targetX - 100f, maxPlatformY, 200f, 40f)
    }

    val sanitizedDoors = level.doors.map { door ->
        val plat = findClosestPlatform(door.x)
        val clampedDoorY = (plat.y - door.height).safeCoerceIn(minPlatformY, maxPlatformY)
        door.copy(y = clampedDoorY)
    }

    fun isCollidingAt(cx: Float, cy: Float, r: Float): Boolean {
        if (cx < r + 15f || cx > level.width - r - 15f) return true
        if (cy < minPlatformY + 30f || cy > level.height - r - 25f) return true

        for (p in sanitizedPlatforms) {
            val minPX = if (p.isMoving) minOf(p.x, p.x + p.moveRangeX) else p.x
            val maxPX = if (p.isMoving) maxOf(p.x + p.width, p.x + p.width + p.moveRangeX) else p.x + p.width
            val minPY = if (p.isMoving) minOf(p.y, p.y + p.moveRangeY) else p.y
            val maxPY = if (p.isMoving) maxOf(p.y + p.height, p.y + p.height + p.moveRangeY) else p.y + p.height

            val closestX = cx.safeCoerceIn(minPX, maxPX)
            val closestY = cy.safeCoerceIn(minPY, maxPY)

            val distSq = (cx - closestX) * (cx - closestX) + (cy - closestY) * (cy - closestY)
            val overlapRadius = if (p.isSpike || p.spikeDirection != null) r + 25f else r
            if (distSq < overlapRadius * overlapRadius) {
                return true
            }

            if (cx in minPX..maxPX && cy > maxPY && cy - maxPY < 65f) {
                return true
            }
        }

        for (door in sanitizedDoors) {
            val closestX = cx.safeCoerceIn(door.x, door.x + door.width)
            val closestY = cy.safeCoerceIn(door.y, door.y + door.height)
            val distSq = (cx - closestX) * (cx - closestX) + (cy - closestY) * (cy - closestY)
            if (distSq < (r + 10f) * (r + 10f)) return true
        }

        for (block in level.interactiveBlocks) {
            val closestX = cx.safeCoerceIn(block.x, block.x + block.width)
            val closestY = cy.safeCoerceIn(block.y, block.y + block.height)
            val distSq = (cx - closestX) * (cx - closestX) + (cy - closestY) * (cy - closestY)
            if (distSq < (r + 12f) * (r + 12f)) return true
        }

        for (enemy in level.enemies) {
            val minEX = if (enemy.moveRangeX != 0f) minOf(enemy.x, enemy.x + enemy.moveRangeX) else enemy.x
            val maxEX = if (enemy.moveRangeX != 0f) maxOf(enemy.x, enemy.x + enemy.moveRangeX) else enemy.x
            val minEY = if (enemy.moveRangeY != 0f) minOf(enemy.y, enemy.y + enemy.moveRangeY) else enemy.y
            val maxEY = if (enemy.moveRangeY != 0f) maxOf(enemy.y, enemy.y + enemy.moveRangeY) else enemy.y

            val closestX = cx.safeCoerceIn(minEX, maxEX)
            val closestY = cy.safeCoerceIn(minEY, maxEY)
            val distSq = (cx - closestX) * (cx - closestX) + (cy - closestY) * (cy - closestY)
            if (distSq < 55f * 55f) return true
        }

        return false
    }

    fun findValidPlacement(
        initX: Float,
        initY: Float,
        walkablePlatforms: List<BounceObstacle>,
        allPlatforms: List<BounceObstacle>,
        doors: List<BounceDoor>,
        blocks: List<BounceInteractiveBlock>,
        enemies: List<BounceEnemy>,
        minPlatformY: Float,
        maxPlatformY: Float,
        levelWidth: Float,
        levelHeight: Float,
        isCheckpoint: Boolean = false
    ): Pair<Float, Float> {
        val sortedPlatforms = walkablePlatforms.sortedBy { Math.abs((it.x + it.width / 2f) - initX) }

        for (p in sortedPlatforms) {
            val xPositions = mutableListOf<Float>()
            val midX = p.x + p.width / 2f
            
            xPositions.add(initX.safeCoerceIn(p.x + 25f, p.x + p.width - 25f))
            xPositions.add(midX)
            if (p.width > 120f) {
                xPositions.add(midX - 35f)
                xPositions.add(midX + 35f)
            }

            val yPositions = mutableListOf<Float>()
            if (isCheckpoint) {
                yPositions.add(p.y - 50f)
            } else if (p.isSpring) {
                yPositions.add(p.y - 150f)
                yPositions.add(p.y - 120f)
                yPositions.add(p.y - 180f)
            } else {
                yPositions.add(p.y - 55f)
                yPositions.add(p.y - 45f)
                yPositions.add(p.y - 95f)
            }

            for (ty in yPositions) {
                for (tx in xPositions) {
                    if (!isCollidingAt(tx, ty, r = 14f)) {
                        return Pair(tx, ty)
                    }
                }
            }
        }

        if (sortedPlatforms.isNotEmpty()) {
            val p = sortedPlatforms.first()
            val tx = p.x + p.width / 2f
            val ty = if (isCheckpoint) p.y - 50f else (if (p.isSpring) p.y - 150f else p.y - 55f)
            return Pair(tx, ty)
        }

        return Pair(initX, initY)
    }

    val sanitizedCheckpoints = level.checkpoints.map { cp ->
        val (safeX, safeY) = findValidPlacement(
            initX = cp.x,
            initY = cp.y,
            walkablePlatforms = finalWalkable,
            allPlatforms = sanitizedPlatforms,
            doors = sanitizedDoors,
            blocks = level.interactiveBlocks,
            enemies = level.enemies,
            minPlatformY = minPlatformY,
            maxPlatformY = maxPlatformY,
            levelWidth = level.width,
            levelHeight = level.height,
            isCheckpoint = true
        )
        cp.copy(x = safeX, y = safeY)
    }

    val sanitizedKeys = level.keys.map { key ->
        val (safeX, safeY) = findValidPlacement(
            initX = key.x,
            initY = key.y,
            walkablePlatforms = finalWalkable,
            allPlatforms = sanitizedPlatforms,
            doors = sanitizedDoors,
            blocks = level.interactiveBlocks,
            enemies = level.enemies,
            minPlatformY = minPlatformY,
            maxPlatformY = maxPlatformY,
            levelWidth = level.width,
            levelHeight = level.height,
            isCheckpoint = false
        )
        key.copy(x = safeX, y = safeY)
    }

    // Dedicated Exit Flag (Finish Portal) Placement logic
    // 1. Identify/mark the final solid exit platform.
    var exitPlatform = sanitizedPlatforms.firstOrNull { it.isExitPlatform }
    if (exitPlatform == null) {
        val solidPlats = sanitizedPlatforms.filter { plat ->
            !plat.isSpike && plat.spikeDirection == null && !plat.isSpring && !plat.isMoving && !plat.isFallingPlatform
        }
        val candidate = solidPlats.maxByOrNull { it.x + it.width }
            ?: sanitizedPlatforms.filter { !it.isSpike && it.spikeDirection == null }.maxByOrNull { it.x + it.width }
            ?: sanitizedPlatforms.lastOrNull()
            ?: BounceObstacle(level.width - 300f, 500f, 200f, 40f)

        val idx = sanitizedPlatforms.indexOf(candidate)
        val marked = candidate.copy(isExitPlatform = true)
        if (idx != -1) {
            sanitizedPlatforms[idx] = marked
        } else {
            sanitizedPlatforms.add(marked)
        }
        exitPlatform = marked
    }

    // 2. Position the exit portal exactly on top of the platform using exitPlatform.topCenter
    val safePortalX = exitPlatform.topCenter.x
    val safePortalY = exitPlatform.topCenter.y

    val sanitizedCollectibles = level.collectibles.map { col ->
        val (safeX, safeY) = findValidPlacement(
            initX = col.x,
            initY = col.y,
            walkablePlatforms = finalWalkable,
            allPlatforms = sanitizedPlatforms,
            doors = sanitizedDoors,
            blocks = level.interactiveBlocks,
            enemies = level.enemies,
            minPlatformY = minPlatformY,
            maxPlatformY = maxPlatformY,
            levelWidth = level.width,
            levelHeight = level.height,
            isCheckpoint = false
        )
        col.copy(x = safeX, y = safeY)
    }

    return level.copy(
        platforms = sanitizedPlatforms,
        checkpoints = sanitizedCheckpoints,
        keys = sanitizedKeys,
        doors = sanitizedDoors,
        portalX = safePortalX,
        portalY = safePortalY,
        collectibles = sanitizedCollectibles
    )
}

// --- SMART PROCEDURAL LEVEL GENERATOR & AUTOMATED AI PLAY-TEST ENGINE ---

enum class ProceduralTheme(
    val themeName: String,
    val accentColor: Color,
    val bgIndex: Int
) {
    FOREST("Forest", Color(0xFF22C55E), 1),
    ICE("Ice", Color(0xFF38BDF8), 2),
    LAVA("Lava", Color(0xFFFF3D00), 3),
    FACTORY("Factory", Color(0xFFF59E0B), 4),
    CASTLE("Castle", Color(0xFFA855F7), 5),
    UNDERWATER("Underwater", Color(0xFF06B6D4), 6),
    SKY("Sky", Color(0xFF00E5FF), 7),
    JUNGLE("Jungle", Color(0xFF10B981), 8),
    DESERT("Desert", Color(0xFFFFD700), 9),
    CRYSTAL_CAVE("Crystal Cave", Color(0xFFE081FF), 10)
}

data class LevelChunk(
    val width: Float,
    val height: Float,
    val platforms: List<BounceObstacle>,
    val collectibles: List<BounceCollectible> = emptyList(),
    val checkpoints: List<BounceCheckpoint> = emptyList(),
    val enemies: List<BounceEnemy> = emptyList(),
    val keys: List<BounceKey> = emptyList(),
    val doors: List<BounceDoor> = emptyList(),
    val waterZones: List<BounceWaterZone> = emptyList(),
    val interactiveBlocks: List<BounceInteractiveBlock> = emptyList(),
    val endY: Float,
    val portalX: Float? = null,
    val portalY: Float? = null
)

object SmartProceduralLevelGenerator {

    enum class LevelLength { SMALL, MEDIUM, LARGE }

    private fun getLevelLength(levelNum: Int): LevelLength {
        return when {
            levelNum <= 5 -> LevelLength.SMALL
            levelNum <= 12 -> LevelLength.MEDIUM
            else -> LevelLength.LARGE
        }
    }

    fun generateLevel(levelNum: Int, context: Context? = null): BounceLevel {
        if (context != null) {
            val prefs = context.getSharedPreferences("bounce_game_prefs", Context.MODE_PRIVATE)
            val savedTheme = prefs.getString("infinite_level_theme_$levelNum", null)
            val savedDiff = prefs.getFloat("infinite_level_difficulty_$levelNum", -1f)
            val savedChunks = prefs.getString("infinite_level_chunks_$levelNum", null)
            if (savedTheme != null && savedDiff >= 0f && savedChunks != null) {
                try {
                    val theme = ProceduralTheme.valueOf(savedTheme)
                    val chunks = savedChunks.split(",").map {
                        val parts = it.split(":")
                        Pair(ChunkType.valueOf(parts[0]), parts[1].toInt())
                    }
                    return buildLevelFromConfig(levelNum, theme, savedDiff, chunks, context)
                } catch (e: Exception) {
                    // Fallback to fresh generation
                }
            }
        }

        var attempts = 0
        while (attempts < 100) {
            val candidate = buildSmartCandidateLevel(levelNum, seed = (levelNum * 1337 + attempts).toLong(), context)
            if (verifyAndPlaytestLevel(candidate.first)) {
                if (context != null) {
                    val prefs = context.getSharedPreferences("bounce_game_prefs", Context.MODE_PRIVATE)
                    val chunkStr = candidate.second.joinToString(",") { "${it.first.name}:${it.second}" }
                    prefs.edit()
                        .putString("infinite_level_theme_$levelNum", candidate.third.name)
                        .putFloat("infinite_level_difficulty_$levelNum", candidate.fourth)
                        .putString("infinite_level_chunks_$levelNum", chunkStr)
                        .apply()
                }
                return candidate.first
            }
            attempts++
        }

        val fallbackCandidate = buildSmartCandidateLevel(levelNum, seed = levelNum.toLong(), context)
        return fallbackCandidate.first
    }

    private fun buildSmartCandidateLevel(
        levelNum: Int,
        seed: Long,
        context: Context?
    ): Tuple4<BounceLevel, List<Pair<ChunkType, Int>>, ProceduralTheme, Float> {
        val random = java.util.Random(seed)

        // 1. Adaptive Difficulty Settings & progression
        val adaptiveSettings = if (context != null) {
            com.myplaywin.app.data.AdaptiveDifficultyManager.getAdaptiveSettings(context, levelNum)
        } else {
            com.myplaywin.app.data.AdaptiveDifficultySettings(
                difficultyOffset = 0f,
                increaseSafePlatforms = false,
                reduceEnemyDensity = false,
                addBonusPaths = true,
                addRiskRewardShortcuts = true,
                hiddenCavesChance = 0.25f,
                secretStarRoutesChance = 0.25f,
                verticalExplorationChance = 0.3f,
                bonusCoinRoomChance = 0.2f
            )
        }

        val baseDifficulty = when {
            levelNum <= 5 -> 0.05f + (levelNum - 1) * 0.03f
            levelNum <= 15 -> 0.2f + (levelNum - 6) * 0.04f
            else -> (0.6f + (levelNum - 16) * 0.015f).coerceAtMost(1.0f)
        }
        val difficulty = (baseDifficulty + adaptiveSettings.difficultyOffset).coerceIn(0.01f, 1.0f)

        // 2. Theme Selection (Avoiding repetition of theme in last 4 levels for rich rotation)
        val themes = ProceduralTheme.values()
        val recentlyUsedThemes = mutableSetOf<ProceduralTheme>()
        if (context != null) {
            val prefs = context.getSharedPreferences("bounce_game_prefs", Context.MODE_PRIVATE)
            for (i in maxOf(1, levelNum - 4) until levelNum) {
                val savedThemeStr = prefs.getString("infinite_level_theme_$i", null)
                try {
                    savedThemeStr?.let { recentlyUsedThemes.add(ProceduralTheme.valueOf(it)) }
                } catch (e: Exception) {}
            }
        }
        val allowedThemes = themes.filter { it !in recentlyUsedThemes }
        val theme = if (allowedThemes.isNotEmpty()) {
            allowedThemes[random.nextInt(allowedThemes.size)]
        } else {
            // Fallback: avoid only immediate back-to-back theme
            val prevTheme = if (levelNum > 1 && context != null) {
                val prefs = context.getSharedPreferences("bounce_game_prefs", Context.MODE_PRIVATE)
                val savedThemeStr = prefs.getString("infinite_level_theme_${levelNum - 1}", null)
                try { savedThemeStr?.let { ProceduralTheme.valueOf(it) } } catch (e: Exception) { null }
            } else null
            if (prevTheme != null) {
                val rem = themes.filter { it != prevTheme }
                rem[random.nextInt(rem.size)]
            } else {
                themes[random.nextInt(themes.size)]
            }
        }

        // 3. Track recently used chunk variations in the last 25 levels
        val recentlyUsedChunks = mutableSetOf<Pair<ChunkType, Int>>()
        if (context != null) {
            val prefs = context.getSharedPreferences("bounce_game_prefs", Context.MODE_PRIVATE)
            val startCheck = maxOf(1, levelNum - 25)
            for (i in startCheck until levelNum) {
                val savedChunks = prefs.getString("infinite_level_chunks_$i", null)
                if (savedChunks != null) {
                    try {
                        savedChunks.split(",").forEach {
                            val parts = it.split(":")
                            recentlyUsedChunks.add(Pair(ChunkType.valueOf(parts[0]), parts[1].toInt()))
                        }
                    } catch (e: Exception) {}
                }
            }
        }

        // 4. Construct exact chunk sequence as mandated by user request
        val chunkTypes = listOf(
            ChunkType.START,
            ChunkType.EASY,
            ChunkType.MEDIUM,
            ChunkType.CHECKPOINT,
            ChunkType.SECRET,
            // Hard chunk selection (one of 5 types, avoiding repeating L-1 type)
            listOf(ChunkType.VERTICAL, ChunkType.MOVING_PLATFORM, ChunkType.SPRING, ChunkType.ENEMY, ChunkType.PUZZLE).let { hardTypes ->
                val prevHardType = if (levelNum > 1 && context != null) {
                    val prefs = context.getSharedPreferences("bounce_game_prefs", Context.MODE_PRIVATE)
                    val savedChunks = prefs.getString("infinite_level_chunks_${levelNum - 1}", null)
                    savedChunks?.split(",")?.map { it.split(":") }
                        ?.find { it[0] in listOf("VERTICAL", "MOVING_PLATFORM", "SPRING", "ENEMY", "PUZZLE") }
                        ?.let { try { ChunkType.valueOf(it[0]) } catch (e: Exception) { null } }
                } else null
                val allowedHardTypes = if (prevHardType != null) hardTypes.filter { it != prevHardType } else hardTypes
                allowedHardTypes[random.nextInt(allowedHardTypes.size)]
            },
            ChunkType.CHECKPOINT,
            ChunkType.FINAL_CHALLENGE,
            ChunkType.EXIT
        )

        // 5. Select unique chunk variations based on difficulty range and recent usage
        val chunkConfigList = mutableListOf<Pair<ChunkType, Int>>()
        for (type in chunkTypes) {
            val maxVars = ChunkLibrary.getNumVariations(type)
            val allowedRange = when {
                levelNum <= 5 -> {
                    val limit = maxOf(3, (maxVars * 0.3f).toInt())
                    0 until limit
                }
                levelNum <= 15 -> {
                    val low = (maxVars * 0.25f).toInt()
                    val high = maxOf(low + 3, (maxVars * 0.75f).toInt())
                    low until high
                }
                else -> {
                    val low = (maxVars * 0.5f).toInt()
                    low until maxVars
                }
            }

            var chosenVar = -1
            val shuffledCandidates = allowedRange.shuffled(random)
            for (cand in shuffledCandidates) {
                if (Pair(type, cand) !in recentlyUsedChunks) {
                    chosenVar = cand
                    break
                }
            }

            if (chosenVar == -1) {
                val allShuffled = (0 until maxVars).shuffled(random)
                for (cand in allShuffled) {
                    if (Pair(type, cand) !in recentlyUsedChunks) {
                        chosenVar = cand
                        break
                    }
                }
            }

            if (chosenVar == -1) {
                chosenVar = random.nextInt(maxVars)
            }

            chunkConfigList.add(Pair(type, chosenVar))
        }

        // 6. Similarity checker: avoid repeating the same configurations within the last 25 generated levels
        if (context != null) {
            val prefs = context.getSharedPreferences("bounce_game_prefs", Context.MODE_PRIVATE)
            val startCheck = maxOf(1, levelNum - 25)
            var similarityAttempts = 0
            var tooSimilar = true
            while (tooSimilar && similarityAttempts < 15) {
                tooSimilar = false
                for (i in startCheck until levelNum) {
                    val savedChunks = prefs.getString("infinite_level_chunks_$i", null) ?: continue
                    try {
                        val savedList = savedChunks.split(",").map {
                            val parts = it.split(":")
                            Pair(ChunkType.valueOf(parts[0]), parts[1].toInt())
                        }
                        var matchCount = 0
                        for (p in chunkConfigList) {
                            if (p in savedList) matchCount++
                        }
                        val similarity = matchCount.toFloat() / chunkConfigList.size
                        if (similarity > 0.5f) { // More than 50% identical variations
                            tooSimilar = true
                            break
                        }
                    } catch (e: Exception) {}
                }
                if (tooSimilar) {
                    // Randomly modify a couple of variations to keep it distinct
                    val randomIdx = random.nextInt(chunkConfigList.size)
                    val (type, _) = chunkConfigList[randomIdx]
                    chunkConfigList[randomIdx] = Pair(type, random.nextInt(ChunkLibrary.getNumVariations(type)))
                    similarityAttempts++
                }
            }
        }

        val levelObj = buildLevelFromConfig(levelNum, theme, difficulty, chunkConfigList, context)
        return Tuple4(levelObj, chunkConfigList, theme, difficulty)
    }

    private fun validateChunk(
        chunk: LevelChunk,
        type: ChunkType,
        levelNum: Int,
        variation: Int
    ): Pair<Boolean, String> {
        val reasons = mutableListOf<String>()

        if (chunk.width <= 0f) {
            reasons.add("Chunk width must be positive, got ${chunk.width}")
        }
        if (chunk.height <= 0f) {
            reasons.add("Chunk height must be positive, got ${chunk.height}")
        }

        for ((pIdx, p) in chunk.platforms.withIndex()) {
            if (p.width <= 0f) {
                reasons.add("Platform #$pIdx width must be positive, got ${p.width}")
            }
            if (p.height <= 0f) {
                reasons.add("Platform #$pIdx height must be positive, got ${p.height}")
            }
            if (p.isMoving) {
                if (p.moveRangeX < 0f) {
                    reasons.add("Platform #$pIdx moveRangeX is negative: ${p.moveRangeX}")
                }
                if (p.moveRangeY < 0f) {
                    reasons.add("Platform #$pIdx moveRangeY is negative: ${p.moveRangeY}")
                }
            }
        }

        for ((eIdx, e) in chunk.enemies.withIndex()) {
            if (e.moveRangeX < 0f) {
                reasons.add("Enemy #$eIdx moveRangeX is negative: ${e.moveRangeX}")
            }
            if (e.moveRangeY < 0f) {
                reasons.add("Enemy #$eIdx moveRangeY is negative: ${e.moveRangeY}")
            }
        }

        if (type == ChunkType.EXIT) {
            val px = chunk.portalX
            val py = chunk.portalY
            if (px == null || py == null) {
                reasons.add("Exit chunk must define portalX and portalY coordinates")
            } else {
                val plat = chunk.platforms.firstOrNull { it.isExitPlatform }
                    ?: chunk.platforms.firstOrNull { !it.isSpike && it.spikeDirection == null && !it.isSpring && !it.isMoving && !it.isFallingPlatform }
                    ?: chunk.platforms.firstOrNull { !it.isSpike && it.spikeDirection == null }
                
                if (plat == null) {
                    reasons.add("Exit chunk must contain a walkable platform")
                } else {
                    // Rule 1: The Exit Portal must always be attached to the TOP SURFACE of the FINAL WALKABLE PLATFORM.
                    // Rule 2: Never attach the Exit to walls, platform sides, platform bottoms, decorative blocks, or terrain.
                    // Validation:
                    // - Exit is below platform top:
                    if (py > plat.y - 5f) {
                        reasons.add("Exit portal must be placed strictly on top of the platform (py=$py, plat.y=${plat.y})")
                    }

                    // - Must never spawn beyond the platform edge
                    if (px < plat.x || px > plat.x + plat.width) {
                        reasons.add("Exit portal must be horizontally within the platform boundaries (px=$px, plat=[${plat.x}, ${plat.x + plat.width}])")
                    }

                    // - Must never spawn outside the camera bounds (height is 600f)
                    if (py < 40f || py > 560f || px < 0f) {
                        reasons.add("Exit portal must be within safe camera bounds (px=$px, py=$py)")
                    }

                    // - Exit intersects any collider or overlaps a wall:
                    for (p in chunk.platforms) {
                        if (p != plat) {
                            val r = 24f
                            val closestX = px.coerceIn(p.x, p.x + p.width)
                            val closestY = py.coerceIn(p.y, p.y + p.height)
                            val distSq = (px - closestX) * (px - closestX) + (py - closestY) * (py - closestY)
                            if (distSq < r * r) {
                                reasons.add("Exit portal overlaps or intersects platform collider of block at (x=${p.x}, y=${p.y})")
                            }
                        }
                    }

                    // - Standing space validation: at least one player-width of standing space on either side (player width = 28f)
                    val standingSpaceLeft = px - plat.x
                    val standingSpaceRight = (plat.x + plat.width) - px
                    val minStandingSpace = 28f
                    if (standingSpaceLeft < minStandingSpace && standingSpaceRight < minStandingSpace) {
                        reasons.add("Exit portal lacks sufficient standing space on either side (left=$standingSpaceLeft, right=$standingSpaceRight)")
                    }

                    // - Overhead clearance / direct reachability validation: no ceiling blocking access
                    for (p in chunk.platforms) {
                        if (p != plat) {
                            if (px + 15f > p.x && px - 15f < p.x + p.width) {
                                if (p.y + p.height > py - 60f && p.y < py) {
                                    reasons.add("Exit path overhead is blocked by platform (y=${p.y}, height=${p.height})")
                                }
                            }
                        }
                    }

                    // - No spikes or hazards block the player's horizontal path on the platform
                    for (p in chunk.platforms) {
                        if (p.isSpike || p.spikeDirection != null) {
                            if (p.x + p.width > plat.x && p.x < px) {
                                reasons.add("Exit path is blocked by hazard spikes")
                            }
                        }
                    }
                }
            }
        }

        if (reasons.isNotEmpty()) {
            val invalidValues = "Platforms: ${chunk.platforms.size}, Enemies: ${chunk.enemies.size}"
            val reasonStr = reasons.joinToString("; ")
            android.util.Log.e(
                "BounceClassic",
                """
                [REJECTED CHUNK]
                Level Number: $levelNum
                Chunk Type: $type
                Variation: $variation
                Generated Bounds: width=${chunk.width}, height=${chunk.height}
                Invalid Values: $invalidValues
                Reason for Rejection: $reasonStr
                """.trimIndent()
            )
            return Pair(false, reasonStr)
        }

        return Pair(true, "")
    }

    private fun sanitizeChunkDirectly(chunk: LevelChunk): LevelChunk {
        val sanitizedPlatforms = chunk.platforms.map { p ->
            p.copy(
                width = p.width.coerceAtLeast(20f),
                height = p.height.coerceAtLeast(20f),
                moveRangeX = p.moveRangeX.coerceAtLeast(0f),
                moveRangeY = p.moveRangeY.coerceAtLeast(0f)
            )
        }
        val sanitizedEnemies = chunk.enemies.map { enemy ->
            enemy.copy(
                moveRangeX = enemy.moveRangeX.coerceAtLeast(0f),
                moveRangeY = enemy.moveRangeY.coerceAtLeast(0f)
            )
        }
        return chunk.copy(
            width = chunk.width.coerceAtLeast(100f),
            height = chunk.height.coerceAtLeast(100f),
            platforms = sanitizedPlatforms,
            enemies = sanitizedEnemies
        )
    }

    private fun buildLevelFromConfig(
        levelNum: Int,
        theme: ProceduralTheme,
        difficulty: Float,
        chunks: List<Pair<ChunkType, Int>>,
        context: Context?
    ): BounceLevel {
        val levelHeight = 600f

        val platforms = mutableListOf<BounceObstacle>()
        val collectibles = mutableListOf<BounceCollectible>()
        val checkpoints = mutableListOf<BounceCheckpoint>()
        val enemies = mutableListOf<BounceEnemy>()
        val keys = mutableListOf<BounceKey>()
        val doors = mutableListOf<BounceDoor>()
        val waterZones = mutableListOf<BounceWaterZone>()
        val interactiveBlocks = mutableListOf<BounceInteractiveBlock>()

        var currentX = 0f
        var currentY = 440f

        var checkpointCounter = 1
        var levelPortalX: Float? = null
        var levelPortalY: Float? = null

        for ((index, chunkPair) in chunks.withIndex()) {
            val type = chunkPair.first
            var variation = chunkPair.second

            var chunk: LevelChunk? = null
            var attempts = 0
            val maxVars = ChunkLibrary.getNumVariations(type)

            while (attempts < 50) {
                val tempChunk = generateChunk(
                    type = type,
                    variation = variation,
                    startX = currentX,
                    startY = currentY,
                    difficulty = difficulty,
                    levelNum = levelNum,
                    checkpointId = if (type == ChunkType.CHECKPOINT) checkpointCounter else 0
                )

                val (isValid, _) = validateChunk(tempChunk, type, levelNum, variation)
                if (isValid) {
                    chunk = tempChunk
                    break
                } else {
                    variation = (variation + 1) % maxVars
                    attempts++
                }
            }

            if (chunk == null) {
                android.util.Log.w("BounceClassic", "Fallback sanitizing chunk for Level $levelNum, Type $type, Var $variation")
                val fallbackChunk = generateChunk(
                    type = type,
                    variation = 0,
                    startX = currentX,
                    startY = currentY,
                    difficulty = difficulty,
                    levelNum = levelNum,
                    checkpointId = if (type == ChunkType.CHECKPOINT) checkpointCounter else 0
                )
                chunk = sanitizeChunkDirectly(fallbackChunk)
            }

            if (type == ChunkType.CHECKPOINT) {
                checkpointCounter++
            }

            appendChunk(chunk, platforms, collectibles, checkpoints, enemies, keys, doors, waterZones, interactiveBlocks)
            if (chunk.portalX != null && chunk.portalY != null) {
                levelPortalX = chunk.portalX
                levelPortalY = chunk.portalY
            }
            currentX += chunk.width
            currentY = chunk.endY
        }

        // Apply Adaptive Difficulty and Variety injection
        val adaptiveSettings = if (context != null) {
            com.myplaywin.app.data.AdaptiveDifficultyManager.getAdaptiveSettings(context, levelNum)
        } else {
            com.myplaywin.app.data.AdaptiveDifficultySettings(
                difficultyOffset = 0f,
                increaseSafePlatforms = false,
                reduceEnemyDensity = false,
                addBonusPaths = true,
                addRiskRewardShortcuts = true,
                hiddenCavesChance = 0.25f,
                secretStarRoutesChance = 0.25f,
                verticalExplorationChance = 0.3f,
                bonusCoinRoomChance = 0.2f
            )
        }

        injectLevelVarietyFeatures(
            platforms = platforms,
            collectibles = collectibles,
            enemies = enemies,
            interactiveBlocks = interactiveBlocks,
            adaptiveSettings = adaptiveSettings,
            levelNum = levelNum,
            totalWidth = currentX,
            random = java.util.Random(levelNum.toLong() * 1337 + 999)
        )

        val titleThemeName = theme.themeName
        val titleChunk = when ((levelNum - 1) % 10) {
            0 -> "Trail"
            1 -> "Glacier"
            2 -> "Caldera"
            3 -> "Workshop"
            4 -> "Spires"
            5 -> "Trench"
            6 -> "Stratus"
            7 -> "Overgrowth"
            8 -> "Pyramid"
            9 -> "Amethyst"
            else -> "Expedition"
        }

        val levelName = "Level $levelNum: $titleThemeName $titleChunk"
        val levelDesc = "Explore the treacherous terrains of $titleThemeName. Complete all challenges, find the secret chamber, and exit safely."

        // Find or assign the Exit Platform explicitly marked with isExitPlatform = true
        var exitPlatform = platforms.firstOrNull { it.isExitPlatform }
        if (exitPlatform == null) {
            val rightmostWalkable = platforms.filter { !it.isSpike && it.spikeDirection == null && !it.isSpring && !it.isMoving && !it.isFallingPlatform }.maxByOrNull { it.x + it.width }
                ?: platforms.filter { !it.isSpike && it.spikeDirection == null }.maxByOrNull { it.x + it.width }
                ?: platforms.lastOrNull()
            
            if (rightmostWalkable != null) {
                val idx = platforms.indexOf(rightmostWalkable)
                val marked = rightmostWalkable.copy(isExitPlatform = true)
                if (idx != -1) {
                    platforms[idx] = marked
                } else {
                    platforms.add(marked)
                }
                exitPlatform = marked
            }
        }
        if (exitPlatform == null) {
            val fallback = BounceObstacle(currentX - 400f, currentY, 400f, 100f, isExitPlatform = true)
            platforms.add(fallback)
            exitPlatform = fallback
        }

        val calculatedPortalX = exitPlatform.topCenter.x
        val calculatedPortalY = exitPlatform.topCenter.y

        val levelObj = BounceLevel(
            number = levelNum,
            name = levelName,
            description = levelDesc,
            width = currentX,
            height = levelHeight,
            startX = 100f,
            startY = 390f,
            portalX = calculatedPortalX,
            portalY = calculatedPortalY,
            platforms = platforms,
            collectibles = collectibles,
            checkpoints = checkpoints,
            enemies = enemies,
            keys = keys,
            doors = doors,
            waterZones = waterZones,
            interactiveBlocks = interactiveBlocks,
            baseRewardCoins = 50 + levelNum * 25
        )

        return sanitizeAndValidateLevel(levelObj)
    }

    private fun injectLevelVarietyFeatures(
        platforms: MutableList<BounceObstacle>,
        collectibles: MutableList<BounceCollectible>,
        enemies: MutableList<BounceEnemy>,
        interactiveBlocks: MutableList<BounceInteractiveBlock>,
        adaptiveSettings: com.myplaywin.app.data.AdaptiveDifficultySettings,
        levelNum: Int,
        totalWidth: Float,
        random: java.util.Random
    ) {
        val startZone = 450f
        val endZone = totalWidth - 550f
        if (endZone <= startZone) return

        // 1. INCREASE SAFE PLATFORMS (EMPATHY HELPERS)
        if (adaptiveSettings.increaseSafePlatforms) {
            var checkX = startZone
            while (checkX < endZone) {
                val hasPlatformNearby = platforms.any { p -> checkX >= p.x - 30f && checkX <= p.x + p.width + 30f }
                if (!hasPlatformNearby) {
                    platforms.add(
                        BounceObstacle(
                            x = checkX,
                            y = 440f,
                            width = 160f,
                            height = 25f
                        )
                    )
                    collectibles.add(
                        BounceCollectible(
                            x = checkX + 80f,
                            y = 390f,
                            isStar = false,
                            isBonus = true
                        )
                    )
                    checkX += 200f
                } else {
                    checkX += 100f
                }
            }

            // Convert falling platforms to solid ones
            val stablePlatforms = platforms.map { p ->
                if (p.isFallingPlatform) p.copy(isFallingPlatform = false) else p
            }
            platforms.clear()
            platforms.addAll(stablePlatforms)
        }

        // 2. REDUCE ENEMY DENSITY
        if (adaptiveSettings.reduceEnemyDensity) {
            val reducedEnemies = enemies.filterIndexed { i, _ -> i % 3 == 0 }
            enemies.clear()
            enemies.addAll(reducedEnemies)
        }

        // 3. HIDDEN CAVES (Aesthetic & Secrets)
        if (random.nextFloat() < adaptiveSettings.hiddenCavesChance) {
            val caveX = startZone + random.nextFloat() * (endZone - startZone - 350f)
            val caveY = 475f
            platforms.add(BounceObstacle(x = caveX, y = 350f, width = 300f, height = 30f))
            platforms.add(BounceObstacle(x = caveX + 20f, y = caveY, width = 260f, height = 30f))
            interactiveBlocks.add(
                BounceInteractiveBlock(
                    id = 30000 + levelNum,
                    type = InteractiveType.BREAKABLE,
                    x = caveX - 10f,
                    y = caveY - 40f,
                    width = 40f,
                    height = 40f
                )
            )
            for (c in 0 until 4) {
                collectibles.add(
                    BounceCollectible(
                        x = caveX + 60f + c * 50f,
                        y = caveY - 35f,
                        isStar = false,
                        isBonus = true
                    )
                )
            }
            if (random.nextBoolean()) {
                collectibles.add(
                    BounceCollectible(
                        x = caveX + 150f,
                        y = caveY - 70f,
                        isStar = true,
                        isBonus = true
                    )
                )
            }
        }

        // 4. BONUS COIN ROOMS
        if (random.nextFloat() < adaptiveSettings.bonusCoinRoomChance) {
            val roomX = startZone + random.nextFloat() * (endZone - startZone - 200f)
            val roomY = 180f
            platforms.add(BounceObstacle(x = roomX, y = roomY, width = 160f, height = 20f))
            platforms.add(BounceObstacle(x = roomX, y = roomY, width = 20f, height = 100f))
            platforms.add(BounceObstacle(x = roomX + 140f, y = roomY, width = 20f, height = 100f))
            interactiveBlocks.add(
                BounceInteractiveBlock(
                    id = 31000 + levelNum,
                    type = InteractiveType.BREAKABLE,
                    x = roomX + 20f,
                    y = roomY + 100f,
                    width = 40f,
                    height = 20f
                )
            )
            interactiveBlocks.add(
                BounceInteractiveBlock(
                    id = 31100 + levelNum,
                    type = InteractiveType.BREAKABLE,
                    x = roomX + 60f,
                    y = roomY + 100f,
                    width = 40f,
                    height = 20f
                )
            )
            interactiveBlocks.add(
                BounceInteractiveBlock(
                    id = 31200 + levelNum,
                    type = InteractiveType.BREAKABLE,
                    x = roomX + 100f,
                    y = roomY + 100f,
                    width = 40f,
                    height = 20f
                )
            )
            collectibles.add(BounceCollectible(x = roomX + 45f, y = roomY + 40f, isStar = false, isBonus = true))
            collectibles.add(BounceCollectible(x = roomX + 80f, y = roomY + 40f, isStar = false, isBonus = true))
            collectibles.add(BounceCollectible(x = roomX + 115f, y = roomY + 40f, isStar = false, isBonus = true))
            collectibles.add(BounceCollectible(x = roomX + 45f, y = roomY + 70f, isStar = false, isBonus = true))
            collectibles.add(BounceCollectible(x = roomX + 80f, y = roomY + 70f, isStar = false, isBonus = true))
            collectibles.add(BounceCollectible(x = roomX + 115f, y = roomY + 70f, isStar = false, isBonus = true))
        }

        // 5. SECRET STAR ROUTES (High altitude vertical challenge)
        if (random.nextFloat() < adaptiveSettings.secretStarRoutesChance) {
            val routeX = startZone + random.nextFloat() * (endZone - startZone - 400f)
            platforms.add(
                BounceObstacle(
                    x = routeX,
                    y = 380f,
                    width = 40f,
                    height = 20f,
                    isSpring = true,
                    springForce = -720f
                )
            )
            platforms.add(BounceObstacle(x = routeX + 120f, y = 240f, width = 60f, height = 20f))
            platforms.add(BounceObstacle(x = routeX + 240f, y = 140f, width = 60f, height = 20f))
            collectibles.add(
                BounceCollectible(
                    x = routeX + 270f,
                    y = 90f,
                    isStar = true,
                    isBonus = true
                )
            )
        }

        // 6. RISK VS REWARD SHORTCUTS
        if (adaptiveSettings.addRiskRewardShortcuts && random.nextFloat() < 0.4f) {
            val shortX = startZone + random.nextFloat() * (endZone - startZone - 300f)
            platforms.add(BounceObstacle(x = shortX, y = 390f, width = 200f, height = 20f))
            platforms.add(
                BounceObstacle(
                    x = shortX + 85f,
                    y = 370f,
                    width = 30f,
                    height = 20f,
                    isSpike = true,
                    spikeDirection = SpikeDirection.UP
                )
            )
            collectibles.add(BounceCollectible(x = shortX + 40f, y = 340f, isStar = false, isBonus = true))
            collectibles.add(BounceCollectible(x = shortX + 100f, y = 300f, isStar = true, isBonus = true))
            collectibles.add(BounceCollectible(x = shortX + 160f, y = 340f, isStar = false, isBonus = true))
        }

        // 7. DYNAMIC ENEMY SPEED SCALING, STAR GUARDIANS & DIFFICULTY ENHANCEMENTS
        val speedMult = 1f + (levelNum - 1) * 0.05f
        val updatedEnemies = enemies.map { enemy ->
            val scaledSpeed = (enemy.moveSpeed * speedMult).coerceAtMost(220f)
            enemy.copy(moveSpeed = scaledSpeed)
        }.toMutableList()
        enemies.clear()
        enemies.addAll(updatedEnemies)

        // Place enemies near stars to increase risk / reward challenge
        val stars = collectibles.filter { it.isStar }
        var enemyIdCounter = levelNum * 1000 + 800
        for (star in stars) {
            val hasNearbyEnemy = enemies.any { kotlin.math.abs(it.x - star.x) < 140f && kotlin.math.abs(it.y - star.y) < 140f }
            if (!hasNearbyEnemy) {
                if (random.nextBoolean()) {
                    enemies.add(
                        BounceEnemy(
                            id = enemyIdCounter++,
                            type = EnemyType.FLYING,
                            x = star.x,
                            y = (star.y - 40f).coerceAtLeast(60f),
                            moveRangeX = 60f,
                            moveRangeY = 35f,
                            moveSpeed = (65f * speedMult).coerceAtMost(200f)
                        )
                    )
                } else {
                    enemies.add(
                        BounceEnemy(
                            id = enemyIdCounter++,
                            type = EnemyType.ROTATING_HAZARD,
                            x = star.x,
                            y = star.y + 20f,
                            moveSpeed = (160f * speedMult).coerceAtMost(250f)
                        )
                    )
                }
            }
        }

        // Add moving spikes in later levels (Level 3+)
        if (levelNum >= 3 && random.nextFloat() < 0.6f) {
            val spikeX = startZone + random.nextFloat() * (endZone - startZone - 200f)
            platforms.add(
                BounceObstacle(
                    x = spikeX,
                    y = 450f,
                    width = 35f,
                    height = 25f,
                    isSpike = true,
                    isMoving = true,
                    moveRangeX = 80f,
                    moveSpeed = 0.06f * speedMult
                )
            )
        }
    }

    private fun appendChunk(
        chunk: LevelChunk,
        platforms: MutableList<BounceObstacle>,
        collectibles: MutableList<BounceCollectible>,
        checkpoints: MutableList<BounceCheckpoint>,
        enemies: MutableList<BounceEnemy>,
        keys: MutableList<BounceKey>,
        doors: MutableList<BounceDoor>,
        waterZones: MutableList<BounceWaterZone>,
        interactiveBlocks: MutableList<BounceInteractiveBlock>
    ) {
        platforms.addAll(chunk.platforms)
        collectibles.addAll(chunk.collectibles)
        checkpoints.addAll(chunk.checkpoints)
        enemies.addAll(chunk.enemies)
        keys.addAll(chunk.keys)
        doors.addAll(chunk.doors)
        waterZones.addAll(chunk.waterZones)
        interactiveBlocks.addAll(chunk.interactiveBlocks)
    }

    enum class ChunkType {
        START,
        EASY,
        MEDIUM,
        VERTICAL,
        MOVING_PLATFORM,
        SPRING,
        ENEMY,
        SECRET,
        PUZZLE,
        CHECKPOINT,
        FINAL_CHALLENGE,
        EXIT
    }

    private fun generateChunk(
        type: ChunkType,
        variation: Int,
        startX: Float,
        startY: Float,
        difficulty: Float,
        levelNum: Int,
        checkpointId: Int
    ): LevelChunk {
        return when (type) {
            ChunkType.START -> ChunkLibrary.generateStartChunk(startX, startY, variation)
            ChunkType.EASY -> ChunkLibrary.generateEasyChunk(startX, startY, variation)
            ChunkType.MEDIUM -> ChunkLibrary.generateMediumChunk(startX, startY, variation)
            ChunkType.VERTICAL -> ChunkLibrary.generateVerticalChunk(startX, startY, variation)
            ChunkType.MOVING_PLATFORM -> ChunkLibrary.generateMovingPlatformChunk(startX, startY, variation)
            ChunkType.SPRING -> ChunkLibrary.generateSpringChunk(startX, startY, variation)
            ChunkType.ENEMY -> ChunkLibrary.generateEnemyChunk(startX, startY, variation, levelNum, difficulty)
            ChunkType.SECRET -> ChunkLibrary.generateSecretChunk(startX, startY, variation)
            ChunkType.PUZZLE -> ChunkLibrary.generatePuzzleChunk(startX, startY, variation, levelNum)
            ChunkType.CHECKPOINT -> ChunkLibrary.generateCheckpointChunk(startX, startY, variation, checkpointId)
            ChunkType.FINAL_CHALLENGE -> ChunkLibrary.generateFinalChallengeChunk(startX, startY, variation, levelNum, difficulty)
            ChunkType.EXIT -> ChunkLibrary.generateExitChunk(startX, startY, variation)
        }
    }

    private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}


// --- AUTOMATED AI PLAY-TEST SIMULATION SOLVER ---
fun verifyAndPlaytestLevel(level: BounceLevel): Boolean {
    // 1. Basic level structure integrity validation
    if (level.platforms.isEmpty()) return false
    for (p in level.platforms) {
        if (p.y < 50f || p.y > level.height - 40f) return false
    }

    // 2. Classify walkable (non-spike) platforms
    val walkable = level.platforms.filter { !it.isSpike }.sortedBy { it.x }
    if (walkable.isEmpty()) return false

    // Locate player start and portal platforms
    val startPlatIndex = walkable.indexOfFirst { level.startX >= it.x - 30f && level.startX <= it.x + it.width + 30f }
    if (startPlatIndex == -1) return false

    val portalPlatIndex = walkable.indexOfFirst { level.portalX >= it.x - 60f && level.portalX <= it.x + it.width + 60f }
    if (portalPlatIndex == -1) return false

    // 3. Platform Safety & Overlap Checks (No collectibles, keys, doors, checkpoints, portal or enemies inside solid platforms)
    fun overlapsSolid(x: Float, y: Float, radius: Float = 12f): Boolean {
        for (p in level.platforms) {
            if (p.isSpike) continue
            // Platform box
            val bufferY = 4f
            if (x + radius > p.x && x - radius < p.x + p.width &&
                y + radius > p.y + bufferY && y - radius < p.y + p.height) {
                return true
            }
        }
        return false
    }

    // Check collectibles & keys placement
    for (col in level.collectibles) {
        if (overlapsSolid(col.x, col.y, 10f)) return false
        if (col.y > level.height - 30f) return false // below terrain / abyss
    }
    for (key in level.keys) {
        if (overlapsSolid(key.x, key.y, 10f)) return false
        if (key.y > level.height - 30f) return false // below terrain / abyss
    }
    for (cp in level.checkpoints) {
        if (overlapsSolid(cp.x, cp.y, 12f)) return false
        if (cp.y > level.height - 30f) return false // below terrain / abyss
    }
    // Exit Portal placement and validation check
    val exitPlat = level.platforms.firstOrNull { it.isExitPlatform }
    if (exitPlat == null) return false // Must have an exit platform!

    // Verify it is aligned to topCenter of that platform
    if (Math.abs(level.portalX - exitPlat.topCenter.x) > 1f || Math.abs(level.portalY - exitPlat.topCenter.y) > 1f) {
        return false // Exit Portal must use topCenter!
    }

    // Exit is below platform top:
    if (level.portalY > exitPlat.y - 5f) return false

    // Exit intersects any collider or overlaps a wall:
    for (p in level.platforms) {
        if (p != exitPlat) {
            val r = 24f // Full safety collision boundary circle
            val closestX = level.portalX.coerceIn(p.x, p.x + p.width)
            val closestY = level.portalY.coerceIn(p.y, p.y + p.height)
            val distSq = (level.portalX - closestX) * (level.portalX - closestX) + (level.portalY - closestY) * (level.portalY - closestY)
            if (distSq < r * r) return false // Intersects or overlaps!
        }
    }

    // Exit is not directly reachable by the player (e.g. blocked by overhead platform or lacks standing space)
    val standingSpaceLeft = level.portalX - exitPlat.x
    val standingSpaceRight = (exitPlat.x + exitPlat.width) - level.portalX
    val minStandingSpace = 28f
    if (standingSpaceLeft < minStandingSpace && standingSpaceRight < minStandingSpace) {
        return false // Not enough standing space!
    }

    for (p in level.platforms) {
        if (p != exitPlat) {
            // Overhead blockage check
            if (level.portalX + 15f > p.x && level.portalX - 15f < p.x + p.width) {
                if (p.y + p.height > level.portalY - 60f && p.y < level.portalY) {
                    return false // Overhead path blocked!
                }
            }
        }
    }

    // Enemies inside terrain check
    for (enemy in level.enemies) {
        if (overlapsSolid(enemy.x, enemy.y, 12f)) return false
    }

    // Ensure exit is after all objectives geometrically
    for (star in level.collectibles.filter { it.isStar }) {
        if (star.x >= level.portalX - 40f) return false
    }
    for (key in level.keys) {
        if (key.x >= level.portalX - 40f) return false
    }
    for (cp in level.checkpoints) {
        if (cp.x >= level.portalX - 40f) return false
    }

    // 5. Checkpoint Safety Validation (safe respawn coordinates)
    for (cp in level.checkpoints) {
        // Respawn position cannot be too close to spikes
        for (p in level.platforms.filter { it.isSpike }) {
            val distSq = (cp.x - (p.x + p.width / 2f)) * (cp.x - (p.x + p.width / 2f)) +
                         (cp.y - (p.y + p.height / 2f)) * (cp.y - (p.y + p.height / 2f))
            if (distSq < 50f * 50f) return false
        }
        // Respawn position cannot be too close to enemies initial position
        for (enemy in level.enemies) {
            val distSq = (cp.x - enemy.x) * (cp.x - enemy.x) + (cp.y - enemy.y) * (cp.y - enemy.y)
            if (distSq < 80f * 80f) return false
        }
        // Must have a platform beneath
        val hasUnderlyingPlat = walkable.any { p ->
            cp.x >= p.x - 20f && cp.x <= p.x + p.width + 20f && p.y >= cp.y && p.y - cp.y <= 130f
        }
        if (!hasUnderlyingPlat) return false
    }

    // 6. Camera / Screen Boundary Visibility Validation
    // All gameplay-essential assets must remain visible on screen
    for (p in level.platforms) {
        if (p.x < -100f || p.x > level.width + 100f) return false
    }

    // 7. Level Flow Progression Verification
    // Level must flow from Left (START) to Right (EXIT)
    if (level.startX > 800f) return false
    if (level.portalX < level.width - 600f) return false
    if (level.checkpoints.size >= 2) {
        // Checkpoints should be ordered in progressive flow
        val sortedCP = level.checkpoints.sortedBy { it.x }
        for (i in 0 until sortedCP.size - 1) {
            if (sortedCP[i].x >= sortedCP[i+1].x) return false
        }
    }

    // 8. Separating Stars and Coins for high performance playtest bitmasking
    val stars = level.collectibles.filter { it.isStar }
    val coins = level.collectibles.filter { !it.isStar }

    val numStars = stars.size
    val numKeys = level.keys.size
    val numCheckpoints = level.checkpoints.size

    val keyIndices = level.keys.mapIndexed { index, key -> key.id to index }.toMap()
    val cpIndices = level.checkpoints.mapIndexed { index, cp -> cp.id to index }.toMap()

    val platformStars = IntArray(walkable.size) { 0 }
    val platformKeys = IntArray(walkable.size) { 0 }
    val platformCheckpoints = IntArray(walkable.size) { 0 }

    for (i in stars.indices) {
        val star = stars[i]
        val platIndex = walkable.indexOfFirst { star.x >= it.x - 80f && star.x <= it.x + it.width + 80f }
        if (platIndex != -1) {
            platformStars[platIndex] = platformStars[platIndex] or (1 shl i)
        }
    }

    for (i in level.keys.indices) {
        val key = level.keys[i]
        val platIndex = walkable.indexOfFirst { key.x >= it.x - 80f && key.x <= it.x + it.width + 80f }
        if (platIndex != -1) {
            val bit = keyIndices[key.id] ?: 0
            platformKeys[platIndex] = platformKeys[platIndex] or (1 shl bit)
        }
    }

    for (i in level.checkpoints.indices) {
        val cp = level.checkpoints[i]
        val platIndex = walkable.indexOfFirst { cp.x >= it.x - 80f && cp.x <= it.x + it.width + 80f }
        if (platIndex != -1) {
            val bit = cpIndices[cp.id] ?: 0
            platformCheckpoints[platIndex] = platformCheckpoints[platIndex] or (1 shl bit)
        }
    }

    // BFS Transition Helper matching exact player physical capability
    fun canTransition(fromIndex: Int, toIndex: Int, currentKeys: Int): Boolean {
        val p1 = walkable[fromIndex]
        val p2 = walkable[toIndex]

        // Check door blockages
        for (door in level.doors) {
            val keyBit = keyIndices[door.keyIdNeeded]
            val isUnlocked = keyBit != null && ((currentKeys and (1 shl keyBit)) != 0)
            if (!isUnlocked) {
                val minX = minOf(p1.x + p1.width/2f, p2.x + p2.width/2f)
                val maxX = maxOf(p1.x + p1.width/2f, p2.x + p2.width/2f)
                if (door.x in minX..maxX) {
                    return false
                }
            }
        }

        val gapX = if (p2.x > p1.x + p1.width) {
            p2.x - (p1.x + p1.width)
        } else if (p1.x > p2.x + p2.width) {
            p1.x - (p2.x + p2.width)
        } else {
            0f
        }
        val riseY = p1.y - p2.y

        // 1. Direct Walk/Roll or short jump
        if (Math.abs(riseY) <= 30f && gapX <= 220f) return true

        // 2. Normal Jump Up
        if (riseY > 0f && riseY <= 125f && gapX <= 220f) return true

        // 3. Spring Jump Up (Spring launches player higher)
        if (p1.isSpring && riseY > 0f && riseY <= 240f && gapX <= 350f) return true

        // 4. Dropping Down / Falling
        if (riseY < 0f) {
            if (gapX <= 0f) return true
            if (gapX <= 180f + (-riseY) * 0.5f) return true
        }

        return false
    }

    // BFS state
    data class State(
        val platform: Int,
        val stars: Int,
        val keys: Int,
        val checkpoints: Int
    )

    val queue = java.util.ArrayDeque<State>()
    val visited = mutableSetOf<State>()

    val startStars = platformStars[startPlatIndex]
    val startKeys = platformKeys[startPlatIndex]
    val startCPs = platformCheckpoints[startPlatIndex]
    val initialState = State(startPlatIndex, startStars, startKeys, startCPs)

    queue.add(initialState)
    visited.add(initialState)

    var iterations = 0
    val maxIterations = 20000

    while (queue.isNotEmpty()) {
        iterations++
        if (iterations > maxIterations) {
            return false // Too complex / rejected
        }

        val curr = queue.poll() ?: break

        for (nextPlat in walkable.indices) {
            if (nextPlat == curr.platform) continue
            if (canTransition(curr.platform, nextPlat, curr.keys)) {
                val nextStars = curr.stars or platformStars[nextPlat]
                val nextKeys = curr.keys or platformKeys[nextPlat]
                val nextCPs = curr.checkpoints or platformCheckpoints[nextPlat]
                val nextState = State(nextPlat, nextStars, nextKeys, nextCPs)

                if (visited.add(nextState)) {
                    queue.add(nextState)
                }
            }
        }
    }

    // --- PLAY-TEST CHECKS ---

    // 1. All stars and checkpoints reachable to exit portal
    val allStarsMask = (1 shl numStars) - 1
    val allCheckpointsMask = (1 shl numCheckpoints) - 1
    val canComplete100Percent = visited.any {
        it.platform == portalPlatIndex &&
        it.stars == allStarsMask &&
        it.checkpoints == allCheckpointsMask
    }
    if (!canComplete100Percent) return false

    // 2. Trapped / Softlock Verification (every reached state must be able to reach exit portal platform)
    val canReachPortal = mutableSetOf<State>()
    val reverseEdges = mutableMapOf<State, MutableList<State>>()

    for (s in visited) {
        if (s.platform == portalPlatIndex) {
            canReachPortal.add(s)
        }
    }

    for (curr in visited) {
        for (nextPlat in walkable.indices) {
            if (nextPlat == curr.platform) continue
            if (canTransition(curr.platform, nextPlat, curr.keys)) {
                val nextStars = curr.stars or platformStars[nextPlat]
                val nextKeys = curr.keys or platformKeys[nextPlat]
                val nextCPs = curr.checkpoints or platformCheckpoints[nextPlat]
                val nextState = State(nextPlat, nextStars, nextKeys, nextCPs)
                if (nextState in visited) {
                    reverseEdges.getOrPut(nextState) { mutableListOf() }.add(curr)
                }
            }
        }
    }

    val revQueue = java.util.ArrayDeque<State>()
    revQueue.addAll(canReachPortal)
    while (revQueue.isNotEmpty()) {
        val curr = revQueue.poll() ?: break
        val prevs = reverseEdges[curr] ?: emptyList()
        for (prev in prevs) {
            if (canReachPortal.add(prev)) {
                revQueue.add(prev)
            }
        }
    }

    if (canReachPortal.size != visited.size) {
        return false // Left trapped
    }

    // 3. Collectible Star/Key lockout after checkpoint check
    fun hasPathToPlatform(fromPlat: Int, toPlat: Int, currentKeys: Int): Boolean {
        val platQueue = java.util.ArrayDeque<Int>()
        val platVisited = mutableSetOf<Int>()
        platQueue.add(fromPlat)
        platVisited.add(fromPlat)
        while (platQueue.isNotEmpty()) {
            val cp = platQueue.poll() ?: break
            if (cp == toPlat) return true
            for (np in walkable.indices) {
                if (np == cp) continue
                if (canTransition(cp, np, currentKeys)) {
                    if (platVisited.add(np)) {
                        platQueue.add(np)
                    }
                }
            }
        }
        return false
    }

    for (curr in visited) {
        for (k in level.checkpoints.indices) {
            val cpActivated = (curr.checkpoints and (1 shl k)) != 0
            if (cpActivated) {
                for (s_idx in stars.indices) {
                    val starCollected = (curr.stars and (1 shl s_idx)) != 0
                    if (!starCollected) {
                        val starPlat = walkable.indexOfFirst { stars[s_idx].x >= it.x - 80f && stars[s_idx].x <= it.x + it.width + 80f }
                        if (starPlat != -1) {
                            if (!hasPathToPlatform(curr.platform, starPlat, curr.keys)) {
                                return false // Softlocked from star
                            }
                        }
                    }
                }
                for (key_idx in level.keys.indices) {
                    val keyBit = keyIndices[level.keys[key_idx].id] ?: 0
                    val keyCollected = (curr.keys and (1 shl keyBit)) != 0
                    if (!keyCollected) {
                        val keyPlat = walkable.indexOfFirst { level.keys[key_idx].x >= it.x - 80f && level.keys[key_idx].x <= it.x + it.width + 80f }
                        if (keyPlat != -1) {
                            if (!hasPathToPlatform(curr.platform, keyPlat, curr.keys)) {
                                return false // Softlocked from key
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. Verify Every Coin is fully reachable
    val visitedPlatforms = visited.map { it.platform }.toSet()
    for (coin in coins) {
        val coinPlatIndex = walkable.indexOfFirst { coin.x >= it.x - 80f && coin.x <= it.x + it.width + 80f }
        if (coinPlatIndex == -1 || coinPlatIndex !in visitedPlatforms) {
            return false // Coin is unreachable
        }
    }

    // 5. Difficulty Progression Jump Distance Checks (Ensure no sudden spikes)
    val levelNum = level.number
    val maxAllowedGap = when {
        levelNum <= 5 -> 160f
        levelNum <= 15 -> 190f
        else -> 220f
    }
    // Verify that the actual jumps executed along visited paths do not exceed the maxAllowedGap
    for (curr in visited) {
        for (nextPlat in walkable.indices) {
            if (canTransition(curr.platform, nextPlat, curr.keys)) {
                val p1 = walkable[curr.platform]
                val p2 = walkable[nextPlat]
                val gapX = if (p2.x > p1.x + p1.width) {
                    p2.x - (p1.x + p1.width)
                } else if (p1.x > p2.x + p2.width) {
                    p1.x - (p2.x + p2.width)
                } else 0f
                if (gapX > maxAllowedGap && !p1.isSpring) {
                    return false // Sudden difficulty spike / too far jump
                }
            }
        }
    }

    // Check enemy counts by level phase (supporting 30-50% higher enemy density)
    val enemyCount = level.enemies.size
    if (levelNum <= 5 && enemyCount > 10) return false
    if (levelNum in 6..15 && enemyCount > 20) return false
    if (levelNum > 15 && enemyCount > 30) return false

    return true
}

fun generateAll20Levels(): List<BounceLevel> {
    val list = mutableListOf<BounceLevel>()
    for (i in 1..20) {
        list.add(SmartProceduralLevelGenerator.generateLevel(i))
    }
    return list
}

private fun legacyOldLevels(): List<BounceLevel> {
    val list = mutableListOf<BounceLevel>()

    // ==========================================
    // LEVEL 1: Tutorial Grassland
    // Theme: Lush green meadows, spring mushrooms, breakable brick wall & secret room
    // Size: 3800f x 600f (3-5 min exploration)
    // ==========================================
    list.add(
        BounceLevel(
            number = 1,
            name = "Tutorial Grassland",
            description = "Welcome to Bounce! Master movement, spring mushrooms, breakable walls & discover secret rooms.",
            width = 3800f, height = 600f, startX = 100f, startY = 420f, portalX = 3600f, portalY = 360f,
            platforms = listOf(
                // Section 1: Grass Meadow Start
                BounceObstacle(0f, 480f, 600f, 120f),
                BounceObstacle(700f, 480f, 100f, 30f, isSpring = true, springForce = -670f), // Spring mushroom
                BounceObstacle(820f, 320f, 300f, 40f),
                // Section 2: First Checkpoint Meadow
                BounceObstacle(1150f, 480f, 400f, 120f),
                // Section 3: Elevated Bridge & Secret Chamber Ledge
                BounceObstacle(1600f, 220f, 350f, 35f), // Secret Room floor
                BounceObstacle(1600f, 480f, 300f, 120f), // Main lower track
                // Section 4: Gap & Moving Platform
                BounceObstacle(2000f, 380f, 160f, 35f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.05f),
                BounceObstacle(2300f, 480f, 400f, 120f),
                // Section 5: Pushable Box Spike Pit Bridge
                BounceObstacle(2800f, 480f, 250f, 120f),
                BounceObstacle(3050f, 560f, 250f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP),
                BounceObstacle(3300f, 480f, 500f, 120f) // Final Portal Hill
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1250f, y = 430f),
                BounceCheckpoint(id = 2, x = 2450f, y = 430f)
            ),
            enemies = listOf(
                BounceEnemy(id = 101, type = EnemyType.WALKING, x = 400f, y = 452f, moveRangeX = 100f, moveSpeed = 45f),
                BounceEnemy(id = 102, type = EnemyType.WALKING, x = 1350f, y = 452f, moveRangeX = 100f, moveSpeed = 50f),
                BounceEnemy(id = 103, type = EnemyType.ROTATING_HAZARD, x = 2100f, y = 280f, moveSpeed = 100f)
            ),
            keys = listOf(
                BounceKey(id = 1, x = 950f, y = 260f, colorHex = 0xFFFFD700)
            ),
            doors = listOf(
                BounceDoor(id = 1, x = 2600f, y = 400f, width = 24f, height = 80f, keyIdNeeded = 1, keyColorHex = 0xFFFFD700)
            ),
            waterZones = listOf(
                BounceWaterZone(x = 600f, y = 500f, width = 200f, height = 100f)
            ),
            interactiveBlocks = listOf(
                BounceInteractiveBlock(id = 1, type = InteractiveType.BREAKABLE, x = 1550f, y = 360f, durability = 1),
                BounceInteractiveBlock(id = 2, type = InteractiveType.SECRET_PASSAGEWAY, x = 1600f, y = 380f, width = 40f, height = 100f),
                BounceInteractiveBlock(id = 3, type = InteractiveType.PUSHABLE_BOX, x = 2880f, y = 440f)
            ),
            collectibles = listOf(
                BounceCollectible(200f, 420f),
                BounceCollectible(350f, 420f),
                BounceCollectible(950f, 220f, isStar = true),
                BounceCollectible(1300f, 420f),
                // Secret Room Collectibles
                BounceCollectible(1700f, 160f, isStar = true),
                BounceCollectible(1800f, 160f, isStar = true),
                BounceCollectible(1900f, 160f),
                BounceCollectible(2400f, 420f),
                BounceCollectible(3100f, 320f, isStar = true),
                BounceCollectible(3500f, 420f)
            ),
            baseRewardCoins = 50
        )
    )

    // ==========================================
    // LEVEL 2: Sky Ascent
    // Theme: Azure sky, floating clouds, vertical climbing, wind platforms & flying gargoyles
    // Size: 3600f x 1200f (High vertical climbing!)
    // ==========================================
    list.add(
        BounceLevel(
            number = 2,
            name = "Sky Ascent",
            description = "Ascend high into the clouds! Navigate floating wind platforms, falling clouds & flying gargoyles.",
            width = 3600f, height = 1200f, startX = 100f, startY = 1020f, portalX = 3400f, portalY = 180f,
            platforms = listOf(
                // Base launchpad
                BounceObstacle(0f, 1080f, 400f, 120f),
                BounceObstacle(420f, 950f, 100f, 30f, isSpring = true, springForce = -680f),
                // Vertical Staircase 1
                BounceObstacle(550f, 820f, 160f, 35f, isMoving = true, moveRangeY = 80f, moveSpeed = 0.05f),
                BounceObstacle(800f, 700f, 180f, 35f),
                BounceObstacle(1100f, 850f, 300f, 40f), // CP1 Cloud
                // Crumbling Cloud Section
                BounceObstacle(1500f, 750f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(1700f, 650f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(1900f, 550f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(2100f, 500f, 300f, 40f), // Sky Gate Cloud
                // CP2 Platform
                BounceObstacle(2500f, 450f, 300f, 40f),
                // High Altitude Final Stretch
                BounceObstacle(2900f, 320f, 180f, 35f, isMoving = true, moveRangeX = 150f, moveSpeed = 0.07f),
                BounceObstacle(3200f, 240f, 400f, 120f) // Peak Portal
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1200f, y = 800f),
                BounceCheckpoint(id = 2, x = 2600f, y = 400f)
            ),
            enemies = listOf(
                BounceEnemy(id = 201, type = EnemyType.FLYING, x = 750f, y = 600f, moveRangeX = 100f, moveRangeY = 40f, moveSpeed = 55f),
                BounceEnemy(id = 202, type = EnemyType.FLYING, x = 1800f, y = 450f, moveRangeX = 0f, moveRangeY = 80f, moveSpeed = 65f),
                BounceEnemy(id = 203, type = EnemyType.ROTATING_HAZARD, x = 2950f, y = 220f, moveSpeed = 140f)
            ),
            keys = listOf(
                BounceKey(id = 2, x = 1800f, y = 320f, colorHex = 0xFF00E5FF)
            ),
            doors = listOf(
                BounceDoor(id = 2, x = 2200f, y = 420f, width = 24f, height = 80f, keyIdNeeded = 2, keyColorHex = 0xFF00E5FF)
            ),
            waterZones = listOf(
                BounceWaterZone(x = 1400f, y = 1100f, width = 800f, height = 100f)
            ),
            interactiveBlocks = listOf(
                BounceInteractiveBlock(id = 20, type = InteractiveType.BOUNCE_PAD, x = 2650f, y = 390f),
                BounceInteractiveBlock(id = 21, type = InteractiveType.SECRET_PASSAGEWAY, x = 2700f, y = 200f, width = 40f, height = 120f)
            ),
            collectibles = listOf(
                BounceCollectible(200f, 1020f),
                BounceCollectible(850f, 640f, isStar = true),
                BounceCollectible(1200f, 790f),
                BounceCollectible(1800f, 260f, isStar = true),
                BounceCollectible(2600f, 390f),
                BounceCollectible(2800f, 140f, isStar = true),
                BounceCollectible(3300f, 180f)
            ),
            baseRewardCoins = 60
        )
    )

    // ==========================================
    // LEVEL 3: Ancient Fortress
    // Theme: Crimson stone walls, spike pits, saw blades & narrow stone corridors
    // Size: 4600f x 700f
    // ==========================================
    list.add(
        BounceLevel(
            number = 3,
            name = "Ancient Fortress",
            description = "Infiltrate the stone fortress! Avoid sharp floor & ceiling spikes, rotating sawblades & red iron gates.",
            width = 4600f, height = 700f, startX = 100f, startY = 520f, portalX = 4400f, portalY = 420f,
            platforms = listOf(
                BounceObstacle(0f, 580f, 500f, 120f),
                // Spike pit 1 with moving slab
                BounceObstacle(550f, 660f, 250f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP),
                BounceObstacle(600f, 520f, 150f, 35f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.06f),
                // Fortress Corridor 1
                BounceObstacle(850f, 580f, 550f, 120f),
                BounceObstacle(850f, 320f, 550f, 30f, isSpike = true, spikeDirection = SpikeDirection.DOWN), // Ceiling spikes
                // Courtyard CP1
                BounceObstacle(1500f, 580f, 400f, 120f),
                // Ledge to Red Key & Secret Passage
                BounceObstacle(1800f, 380f, 120f, 30f, isSpring = true, springForce = -670f),
                BounceObstacle(2000f, 260f, 350f, 35f), // Secret passage floor
                BounceObstacle(2000f, 580f, 500f, 120f), // Main floor
                // Spike Floor Chasm & CP2
                BounceObstacle(2600f, 660f, 300f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP),
                BounceObstacle(2700f, 500f, 100f, 30f, isFallingPlatform = true),
                BounceObstacle(3000f, 580f, 400f, 120f), // CP2
                // Moving Lift Finale
                BounceObstacle(3500f, 520f, 160f, 35f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.06f),
                BounceObstacle(3800f, 660f, 300f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP),
                BounceObstacle(4200f, 580f, 400f, 120f) // Exit Room
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1600f, y = 530f),
                BounceCheckpoint(id = 2, x = 3100f, y = 530f)
            ),
            enemies = listOf(
                BounceEnemy(id = 301, type = EnemyType.ROTATING_HAZARD, x = 1100f, y = 480f, moveSpeed = 150f),
                BounceEnemy(id = 302, type = EnemyType.ROTATING_HAZARD, x = 1300f, y = 480f, moveSpeed = 160f),
                BounceEnemy(id = 303, type = EnemyType.WALKING, x = 2200f, y = 552f, moveRangeX = 120f, moveSpeed = 60f),
                BounceEnemy(id = 304, type = EnemyType.FLYING, x = 3600f, y = 350f, moveRangeX = 100f, moveRangeY = 50f, moveSpeed = 70f)
            ),
            keys = listOf(
                BounceKey(id = 3, x = 1900f, y = 200f, colorHex = 0xFFFF1744)
            ),
            doors = listOf(
                BounceDoor(id = 3, x = 2400f, y = 500f, width = 24f, height = 80f, keyIdNeeded = 3, keyColorHex = 0xFFFF1744)
            ),
            interactiveBlocks = listOf(
                BounceInteractiveBlock(id = 30, type = InteractiveType.SECRET_PASSAGEWAY, x = 2100f, y = 460f, width = 40f, height = 120f),
                BounceInteractiveBlock(id = 31, type = InteractiveType.BREAKABLE, x = 2500f, y = 500f, durability = 2)
            ),
            collectibles = listOf(
                BounceCollectible(200f, 520f),
                BounceCollectible(1100f, 420f, isStar = true),
                BounceCollectible(1900f, 150f, isStar = true),
                BounceCollectible(2200f, 200f),
                BounceCollectible(3100f, 520f),
                BounceCollectible(3700f, 250f, isStar = true),
                BounceCollectible(4300f, 520f)
            ),
            baseRewardCoins = 75
        )
    )

    // ==========================================
    // LEVEL 4: Mechanical Factory
    // Theme: Industrial factory, conveyor belts, pushable boxes, gears & steam crushers
    // Size: 4800f x 700f
    // ==========================================
    list.add(
        BounceLevel(
            number = 4,
            name = "Mechanical Factory",
            description = "Navigate industrial conveyor belts, push heavy iron crates, dodge steam crushers & unlock factory vaults.",
            width = 4800f, height = 700f, startX = 100f, startY = 520f, portalX = 4600f, portalY = 420f,
            platforms = listOf(
                BounceObstacle(0f, 580f, 500f, 120f),
                // Conveyor 1
                BounceObstacle(550f, 520f, 400f, 35f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.08f),
                BounceObstacle(1000f, 580f, 500f, 120f), // Pushable box zone
                // CP1 Station
                BounceObstacle(1600f, 580f, 400f, 120f),
                // Gear Section & Yellow Key
                BounceObstacle(2100f, 400f, 180f, 35f),
                BounceObstacle(2300f, 580f, 500f, 120f), // Yellow Vault Door
                // Secret Warehouse
                BounceObstacle(2700f, 280f, 350f, 35f),
                BounceObstacle(2900f, 580f, 400f, 120f), // CP2
                // Conveyor Gauntlet
                BounceObstacle(3400f, 500f, 200f, 35f, isMoving = true, moveRangeX = 100f, moveSpeed = 0.09f),
                BounceObstacle(3700f, 440f, 200f, 35f, isMoving = true, moveRangeY = 80f, moveSpeed = 0.07f),
                BounceObstacle(4000f, 660f, 300f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP),
                BounceObstacle(4400f, 580f, 400f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1700f, y = 530f),
                BounceCheckpoint(id = 2, x = 3000f, y = 530f)
            ),
            enemies = listOf(
                BounceEnemy(id = 401, type = EnemyType.WALKING, x = 1200f, y = 552f, moveRangeX = 100f, moveSpeed = 60f),
                BounceEnemy(id = 402, type = EnemyType.ROTATING_HAZARD, x = 2150f, y = 350f, moveSpeed = 160f),
                BounceEnemy(id = 403, type = EnemyType.FLYING, x = 3500f, y = 320f, moveRangeX = 120f, moveRangeY = 40f, moveSpeed = 70f)
            ),
            keys = listOf(
                BounceKey(id = 4, x = 2150f, y = 320f, colorHex = 0xFFF59E0B)
            ),
            doors = listOf(
                BounceDoor(id = 4, x = 2600f, y = 500f, width = 24f, height = 80f, keyIdNeeded = 4, keyColorHex = 0xFFF59E0B)
            ),
            interactiveBlocks = listOf(
                BounceInteractiveBlock(id = 40, type = InteractiveType.PUSHABLE_BOX, x = 1100f, y = 540f),
                BounceInteractiveBlock(id = 41, type = InteractiveType.SECRET_PASSAGEWAY, x = 2700f, y = 460f, width = 40f, height = 120f),
                BounceInteractiveBlock(id = 42, type = InteractiveType.BREAKABLE, x = 2900f, y = 500f, durability = 1)
            ),
            collectibles = listOf(
                BounceCollectible(200f, 520f),
                BounceCollectible(750f, 460f, isStar = true),
                BounceCollectible(2150f, 260f, isStar = true),
                // Secret Warehouse Collectibles
                BounceCollectible(2800f, 220f, isStar = true),
                BounceCollectible(2900f, 220f),
                BounceCollectible(3000f, 520f),
                BounceCollectible(3800f, 380f, isStar = true),
                BounceCollectible(4500f, 520f)
            ),
            baseRewardCoins = 85
        )
    )

    // ==========================================
    // LEVEL 5: Underwater World
    // Theme: Deep ocean abyss, aquatic swimming, buoyant air bubbles & submerged ruins
    // Size: 5200f x 850f
    // ==========================================
    list.add(
        BounceLevel(
            number = 5,
            name = "Underwater World",
            description = "Dive into deep ocean chasms! Master buoyant swimming, collect air bubbles & discover submerged ruins.",
            width = 5200f, height = 850f, startX = 100f, startY = 520f, portalX = 5000f, portalY = 460f,
            platforms = listOf(
                BounceObstacle(0f, 580f, 500f, 120f),
                // Submerged ruins inside water
                BounceObstacle(700f, 650f, 250f, 40f),
                BounceObstacle(1100f, 550f, 250f, 40f),
                BounceObstacle(1500f, 580f, 400f, 120f), // CP1 Submerged Shrine
                // Floating Crate Section
                BounceObstacle(2000f, 680f, 300f, 40f),
                BounceObstacle(2400f, 580f, 500f, 120f), // Coral Gate
                // Secret Grotto
                BounceObstacle(2800f, 320f, 350f, 35f),
                BounceObstacle(3000f, 580f, 400f, 120f), // CP2
                // Abyss Trench
                BounceObstacle(3600f, 620f, 180f, 35f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.05f),
                BounceObstacle(4000f, 550f, 180f, 35f, isSpring = true, springForce = -680f),
                BounceObstacle(4400f, 580f, 800f, 120f) // Exit Temple
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1600f, y = 530f),
                BounceCheckpoint(id = 2, x = 3100f, y = 530f)
            ),
            enemies = listOf(
                BounceEnemy(id = 501, type = EnemyType.FLYING, x = 900f, y = 450f, moveRangeX = 100f, moveRangeY = 60f, moveSpeed = 50f),
                BounceEnemy(id = 502, type = EnemyType.ROTATING_HAZARD, x = 2150f, y = 550f, moveSpeed = 120f),
                BounceEnemy(id = 503, type = EnemyType.FLYING, x = 3800f, y = 400f, moveRangeX = 120f, moveRangeY = 80f, moveSpeed = 65f)
            ),
            keys = listOf(
                BounceKey(id = 5, x = 2100f, y = 450f, colorHex = 0xFF06B6D4)
            ),
            doors = listOf(
                BounceDoor(id = 5, x = 2600f, y = 500f, width = 24f, height = 80f, keyIdNeeded = 5, keyColorHex = 0xFF06B6D4)
            ),
            waterZones = listOf(
                BounceWaterZone(x = 500f, y = 350f, width = 1000f, height = 500f),
                BounceWaterZone(x = 3400f, y = 350f, width = 1000f, height = 500f)
            ),
            interactiveBlocks = listOf(
                BounceInteractiveBlock(id = 50, type = InteractiveType.PUSHABLE_BOX, x = 1900f, y = 640f),
                BounceInteractiveBlock(id = 51, type = InteractiveType.SECRET_PASSAGEWAY, x = 2800f, y = 460f, width = 40f, height = 120f)
            ),
            collectibles = listOf(
                BounceCollectible(200f, 520f),
                BounceCollectible(800f, 580f, isStar = true),
                BounceCollectible(1200f, 480f),
                BounceCollectible(2100f, 380f, isStar = true),
                // Secret Grotto
                BounceCollectible(2900f, 260f, isStar = true),
                BounceCollectible(3100f, 520f),
                BounceCollectible(3800f, 320f, isStar = true),
                BounceCollectible(4800f, 520f)
            ),
            baseRewardCoins = 100
        )
    )

    // ==========================================
    // LEVEL 6: Lava Caverns
    // Theme: Volcanic magma, obsidian rock, crumbling lava platforms & fire traps
    // Size: 5400f x 800f
    // ==========================================
    list.add(
        BounceLevel(
            number = 6,
            name = "Lava Caverns",
            description = "Brave the fiery volcano! Leap over boiling lava pools, crumbling obsidian & fiery traps.",
            width = 5400f, height = 800f, startX = 100f, startY = 520f, portalX = 5200f, portalY = 420f,
            platforms = listOf(
                BounceObstacle(0f, 580f, 500f, 120f),
                // Lava Lake 1 stepping stones
                BounceObstacle(600f, 520f, 140f, 35f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.07f),
                BounceObstacle(850f, 460f, 140f, 35f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.08f),
                BounceObstacle(1100f, 400f, 140f, 35f),
                BounceObstacle(1400f, 580f, 400f, 120f), // CP1
                // Crumbling Obsidian Run
                BounceObstacle(1900f, 500f, 110f, 30f, isFallingPlatform = true),
                BounceObstacle(2100f, 420f, 110f, 30f, isFallingPlatform = true),
                BounceObstacle(2300f, 350f, 110f, 30f, isFallingPlatform = true),
                BounceObstacle(2500f, 580f, 500f, 120f), // Fire Door
                // Secret Lava Chamber
                BounceObstacle(3000f, 280f, 350f, 35f),
                BounceObstacle(3200f, 580f, 400f, 120f), // CP2
                // Magma Bounce Launch
                BounceObstacle(3800f, 580f, 100f, 30f, isSpring = true, springForce = -690f),
                BounceObstacle(4100f, 320f, 200f, 35f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.09f),
                BounceObstacle(4400f, 660f, 400f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP),
                BounceObstacle(4900f, 580f, 500f, 120f) // Exit
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1500f, y = 530f),
                BounceCheckpoint(id = 2, x = 3300f, y = 530f)
            ),
            enemies = listOf(
                BounceEnemy(id = 601, type = EnemyType.ROTATING_HAZARD, x = 1150f, y = 350f, moveSpeed = 180f),
                BounceEnemy(id = 602, type = EnemyType.WALKING, x = 2700f, y = 552f, moveRangeX = 120f, moveSpeed = 65f),
                BounceEnemy(id = 603, type = EnemyType.FLYING, x = 4200f, y = 250f, moveRangeX = 100f, moveRangeY = 60f, moveSpeed = 80f)
            ),
            keys = listOf(
                BounceKey(id = 6, x = 2300f, y = 270f, colorHex = 0xFFFF3D00)
            ),
            doors = listOf(
                BounceDoor(id = 6, x = 2700f, y = 500f, width = 24f, height = 80f, keyIdNeeded = 6, keyColorHex = 0xFFFF3D00)
            ),
            interactiveBlocks = listOf(
                BounceInteractiveBlock(id = 60, type = InteractiveType.BREAKABLE, x = 2950f, y = 500f, durability = 2),
                BounceInteractiveBlock(id = 61, type = InteractiveType.SECRET_PASSAGEWAY, x = 3000f, y = 460f, width = 40f, height = 120f)
            ),
            collectibles = listOf(
                BounceCollectible(200f, 520f),
                BounceCollectible(1100f, 340f, isStar = true),
                BounceCollectible(2300f, 210f, isStar = true),
                // Secret Chamber
                BounceCollectible(3100f, 220f, isStar = true),
                BounceCollectible(3300f, 520f),
                BounceCollectible(4100f, 260f, isStar = true),
                BounceCollectible(5000f, 520f)
            ),
            baseRewardCoins = 120
        )
    )

    // ==========================================
    // LEVEL 7: Frozen Mountains
    // Theme: Icy mountain peaks, slippery ice platforms, falling icicles & snow spires
    // Size: 5600f x 900f
    // ==========================================
    list.add(
        BounceLevel(
            number = 7,
            name = "Frozen Mountains",
            description = "Conquer icy mountain peaks! Mind the slippery ice platforms, falling stalactites & howling winds.",
            width = 5600f, height = 900f, startX = 100f, startY = 620f, portalX = 5400f, portalY = 480f,
            platforms = listOf(
                BounceObstacle(0f, 680f, 500f, 120f),
                // Slippery Ice Platform Run
                BounceObstacle(600f, 600f, 300f, 35f),
                BounceObstacle(1000f, 520f, 300f, 35f),
                BounceObstacle(1400f, 440f, 100f, 30f, isSpring = true, springForce = -680f),
                BounceObstacle(1600f, 680f, 400f, 120f), // CP1
                // Falling Icicle Gap
                BounceObstacle(2100f, 580f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(2350f, 500f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(2600f, 420f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(2900f, 680f, 500f, 120f), // Frost Door
                // Secret Ice Cavern
                BounceObstacle(3300f, 360f, 350f, 35f),
                BounceObstacle(3500f, 680f, 400f, 120f), // CP2
                // Summit Slide Finale
                BounceObstacle(4100f, 580f, 250f, 35f, isMoving = true, moveRangeX = 140f, moveSpeed = 0.08f),
                BounceObstacle(4500f, 480f, 250f, 35f, isMoving = true, moveRangeX = 140f, moveSpeed = 0.09f),
                BounceObstacle(4900f, 760f, 300f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP),
                BounceObstacle(5200f, 680f, 400f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1700f, y = 630f),
                BounceCheckpoint(id = 2, x = 3600f, y = 630f)
            ),
            enemies = listOf(
                BounceEnemy(id = 701, type = EnemyType.WALKING, x = 750f, y = 572f, moveRangeX = 120f, moveSpeed = 70f),
                BounceEnemy(id = 702, type = EnemyType.FLYING, x = 2400f, y = 350f, moveRangeX = 120f, moveRangeY = 60f, moveSpeed = 75f),
                BounceEnemy(id = 703, type = EnemyType.ROTATING_HAZARD, x = 4300f, y = 500f, moveSpeed = 170f)
            ),
            keys = listOf(
                BounceKey(id = 7, x = 2600f, y = 340f, colorHex = 0xFF38BDF8)
            ),
            doors = listOf(
                BounceDoor(id = 7, x = 3000f, y = 600f, width = 24f, height = 80f, keyIdNeeded = 7, keyColorHex = 0xFF38BDF8)
            ),
            interactiveBlocks = listOf(
                BounceInteractiveBlock(id = 70, type = InteractiveType.SECRET_PASSAGEWAY, x = 3300f, y = 560f, width = 40f, height = 120f)
            ),
            collectibles = listOf(
                BounceCollectible(200f, 620f),
                BounceCollectible(1000f, 460f, isStar = true),
                BounceCollectible(2600f, 280f, isStar = true),
                // Secret Cavern
                BounceCollectible(3400f, 300f, isStar = true),
                BounceCollectible(3600f, 620f),
                BounceCollectible(4500f, 420f, isStar = true),
                BounceCollectible(5300f, 620f)
            ),
            baseRewardCoins = 140
        )
    )

    // ==========================================
    // LEVEL 8: Jungle Ruins
    // Theme: Mossy stone ruins, vine spring trees, falling rock platforms & secret hidden cavern
    // Size: 5800f x 900f
    // ==========================================
    list.add(
        BounceLevel(
            number = 8,
            name = "Jungle Ruins",
            description = "Explore overgrown jungle ruins! Swing across vine spring trees, dodge falling boulders & find secret cave paths.",
            width = 5800f, height = 900f, startX = 100f, startY = 620f, portalX = 5600f, portalY = 480f,
            platforms = listOf(
                BounceObstacle(0f, 680f, 500f, 120f),
                // Tree stump spring bounce
                BounceObstacle(600f, 600f, 100f, 30f, isSpring = true, springForce = -680f),
                BounceObstacle(750f, 450f, 300f, 35f),
                BounceObstacle(1150f, 380f, 300f, 35f),
                BounceObstacle(1600f, 680f, 400f, 120f), // CP1 Overgrown Shrine
                // Falling Rock Ledges
                BounceObstacle(2100f, 580f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(2350f, 500f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(2600f, 420f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(2900f, 680f, 500f, 120f), // Vine Door
                // Secret Cavern
                BounceObstacle(3300f, 340f, 400f, 35f),
                BounceObstacle(3600f, 680f, 400f, 120f), // CP2
                // Canopy Traversal
                BounceObstacle(4200f, 550f, 200f, 35f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.08f),
                BounceObstacle(4500f, 420f, 200f, 35f, isMoving = true, moveRangeY = 80f, moveSpeed = 0.07f),
                BounceObstacle(4800f, 760f, 350f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP),
                BounceObstacle(5300f, 680f, 500f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1700f, y = 630f),
                BounceCheckpoint(id = 2, x = 3700f, y = 630f)
            ),
            enemies = listOf(
                BounceEnemy(id = 801, type = EnemyType.WALKING, x = 900f, y = 422f, moveRangeX = 100f, moveSpeed = 60f),
                BounceEnemy(id = 802, type = EnemyType.FLYING, x = 2400f, y = 320f, moveRangeX = 120f, moveRangeY = 50f, moveSpeed = 70f),
                BounceEnemy(id = 803, type = EnemyType.ROTATING_HAZARD, x = 4300f, y = 480f, moveSpeed = 160f)
            ),
            keys = listOf(
                BounceKey(id = 8, x = 2600f, y = 340f, colorHex = 0xFF10B981)
            ),
            doors = listOf(
                BounceDoor(id = 8, x = 3000f, y = 600f, width = 24f, height = 80f, keyIdNeeded = 8, keyColorHex = 0xFF10B981)
            ),
            interactiveBlocks = listOf(
                BounceInteractiveBlock(id = 80, type = InteractiveType.SECRET_PASSAGEWAY, x = 3300f, y = 560f, width = 40f, height = 120f),
                BounceInteractiveBlock(id = 81, type = InteractiveType.PUSHABLE_BOX, x = 3400f, y = 300f)
            ),
            collectibles = listOf(
                BounceCollectible(200f, 620f),
                BounceCollectible(900f, 390f, isStar = true),
                BounceCollectible(2600f, 280f, isStar = true),
                // Secret Cavern Collectibles
                BounceCollectible(3400f, 280f, isStar = true),
                BounceCollectible(3500f, 280f),
                BounceCollectible(3700f, 620f),
                BounceCollectible(4500f, 360f, isStar = true),
                BounceCollectible(5400f, 620f)
            ),
            baseRewardCoins = 160
        )
    )

    // ==========================================
    // LEVEL 9: Crystal Cave
    // Theme: Amethyst crystals, high bounce pads, crystal locks & teleporters
    // Size: 6000f x 950f
    // ==========================================
    list.add(
        BounceLevel(
            number = 9,
            name = "Crystal Cave",
            description = "Discover glowing crystal caverns! Use high-velocity crystal bounce pads, solve crystal gate locks & uncover magic portals.",
            width = 6000f, height = 950f, startX = 100f, startY = 650f, portalX = 5800f, portalY = 480f,
            platforms = listOf(
                BounceObstacle(0f, 720f, 500f, 120f),
                // High Crystal Bounce Pad chain
                BounceObstacle(600f, 650f, 100f, 30f, isSpring = true, springForce = -690f),
                BounceObstacle(800f, 480f, 250f, 35f),
                BounceObstacle(1200f, 380f, 250f, 35f, isMoving = true, moveRangeX = 100f, moveSpeed = 0.08f),
                BounceObstacle(1600f, 720f, 400f, 120f), // CP1 Crystal Altar
                // Breakable Crystal Wall
                BounceObstacle(2100f, 620f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(2350f, 520f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(2600f, 420f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(2900f, 720f, 500f, 120f), // Crystal Door
                // Secret High Chamber
                BounceObstacle(3300f, 320f, 400f, 35f),
                BounceObstacle(3700f, 720f, 400f, 120f), // CP2
                // High Precision Bouncing Finale
                BounceObstacle(4300f, 600f, 200f, 35f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.08f),
                BounceObstacle(4700f, 460f, 200f, 35f, isMoving = true, moveRangeX = 150f, moveSpeed = 0.09f),
                BounceObstacle(5000f, 800f, 400f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP),
                BounceObstacle(5500f, 720f, 500f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1700f, y = 670f),
                BounceCheckpoint(id = 2, x = 3800f, y = 670f)
            ),
            enemies = listOf(
                BounceEnemy(id = 901, type = EnemyType.ROTATING_HAZARD, x = 900f, y = 420f, moveSpeed = 170f),
                BounceEnemy(id = 902, type = EnemyType.FLYING, x = 2400f, y = 320f, moveRangeX = 120f, moveRangeY = 60f, moveSpeed = 80f),
                BounceEnemy(id = 903, type = EnemyType.WALKING, x = 3100f, y = 692f, moveRangeX = 100f, moveSpeed = 75f)
            ),
            keys = listOf(
                BounceKey(id = 9, x = 2600f, y = 340f, colorHex = 0xFFE081FF)
            ),
            doors = listOf(
                BounceDoor(id = 9, x = 3000f, y = 640f, width = 24f, height = 80f, keyIdNeeded = 9, keyColorHex = 0xFFE081FF)
            ),
            interactiveBlocks = listOf(
                BounceInteractiveBlock(id = 90, type = InteractiveType.BREAKABLE, x = 2000f, y = 640f, durability = 2),
                BounceInteractiveBlock(id = 91, type = InteractiveType.SECRET_PASSAGEWAY, x = 3300f, y = 600f, width = 40f, height = 120f)
            ),
            collectibles = listOf(
                BounceCollectible(200f, 660f),
                BounceCollectible(900f, 420f, isStar = true),
                BounceCollectible(2600f, 280f, isStar = true),
                // Secret Chamber
                BounceCollectible(3400f, 260f, isStar = true),
                BounceCollectible(3800f, 660f),
                BounceCollectible(4700f, 400f, isStar = true),
                BounceCollectible(5600f, 660f)
            ),
            baseRewardCoins = 180
        )
    )

    // ==========================================
    // LEVEL 10: Shadow Castle
    // Theme: Master Trial! Gothic castle, dual key gates, rotating sawblades, flying gargoyles & master portal
    // Size: 6500f x 1100f
    // ==========================================
    list.add(
        BounceLevel(
            number = 10,
            name = "Shadow Castle",
            description = "The Master Trial! Navigate dark castle corridors, rotating sawblade gauntlets, flying gargoyles & open the master portal.",
            width = 6500f, height = 1100f, startX = 100f, startY = 850f, portalX = 6300f, portalY = 450f,
            platforms = listOf(
                // Drawbridge Start
                BounceObstacle(0f, 920f, 600f, 120f),
                BounceObstacle(900f, 850f, 300f, 40f),
                BounceObstacle(1300f, 750f, 300f, 40f),
                BounceObstacle(1800f, 920f, 400f, 120f), // CP1 Courtyard
                // Key 1 Spring Ledge & Red Door
                BounceObstacle(2300f, 850f, 100f, 30f, isSpring = true, springForce = -700f),
                BounceObstacle(2400f, 500f, 300f, 35f), // Gold Key ledge
                BounceObstacle(2800f, 920f, 500f, 120f), // Red Door
                // Secret Dungeon
                BounceObstacle(3200f, 380f, 400f, 35f),
                BounceObstacle(3500f, 920f, 400f, 120f), // CP2
                // Sawblade Gauntlet
                BounceObstacle(4000f, 820f, 150f, 35f, isMoving = true, moveRangeX = 100f, moveSpeed = 0.09f),
                BounceObstacle(4300f, 700f, 150f, 35f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.08f),
                BounceObstacle(4600f, 580f, 300f, 35f), // Cyan Key ledge
                BounceObstacle(5000f, 920f, 500f, 120f), // Master Gate
                // Boss Portal Tower Ascent
                BounceObstacle(5600f, 780f, 180f, 35f, isSpring = true, springForce = -700f),
                BounceObstacle(5800f, 580f, 180f, 35f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.09f),
                BounceObstacle(6100f, 500f, 400f, 120f) // Master Exit
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1900f, y = 870f),
                BounceCheckpoint(id = 2, x = 3600f, y = 870f)
            ),
            enemies = listOf(
                BounceEnemy(id = 1001, type = EnemyType.FLYING, x = 1000f, y = 650f, moveRangeX = 100f, moveRangeY = 50f, moveSpeed = 70f),
                BounceEnemy(id = 1002, type = EnemyType.ROTATING_HAZARD, x = 2500f, y = 420f, moveSpeed = 180f),
                BounceEnemy(id = 1003, type = EnemyType.WALKING, x = 3000f, y = 892f, moveRangeX = 120f, moveSpeed = 80f),
                BounceEnemy(id = 1004, type = EnemyType.FLYING, x = 4400f, y = 500f, moveRangeX = 140f, moveRangeY = 80f, moveSpeed = 85f),
                BounceEnemy(id = 1005, type = EnemyType.ROTATING_HAZARD, x = 5850f, y = 500f, moveSpeed = 200f)
            ),
            keys = listOf(
                BounceKey(id = 101, x = 2500f, y = 420f, colorHex = 0xFFFFD700),
                BounceKey(id = 102, x = 4700f, y = 500f, colorHex = 0xFF00E5FF)
            ),
            doors = listOf(
                BounceDoor(id = 101, x = 3000f, y = 840f, width = 24f, height = 80f, keyIdNeeded = 101, keyColorHex = 0xFFFFD700),
                BounceDoor(id = 102, x = 5200f, y = 840f, width = 24f, height = 80f, keyIdNeeded = 102, keyColorHex = 0xFF00E5FF)
            ),
            waterZones = listOf(
                BounceWaterZone(x = 600f, y = 880f, width = 300f, height = 150f)
            ),
            interactiveBlocks = listOf(
                BounceInteractiveBlock(id = 100, type = InteractiveType.BREAKABLE, x = 1200f, y = 700f, durability = 2),
                BounceInteractiveBlock(id = 101, type = InteractiveType.SECRET_PASSAGEWAY, x = 3200f, y = 800f, width = 40f, height = 120f)
            ),
            collectibles = listOf(
                BounceCollectible(200f, 860f),
                BounceCollectible(2500f, 360f, isStar = true),
                // Secret Dungeon
                BounceCollectible(3300f, 320f, isStar = true),
                BounceCollectible(3400f, 320f, isStar = true),
                BounceCollectible(3600f, 860f),
                BounceCollectible(4700f, 440f, isStar = true),
                BounceCollectible(6200f, 440f)
            ),
            baseRewardCoins = 250
        )
    )

    // ==========================================
    // LEVELS 11 THROUGH 20: Expanded Handcrafted Campaign
    // Handcrafted manually designed levels for perfect playability
    // ==========================================
    // LEVEL 11: Electric Skyway
    list.add(
        BounceLevel(
            number = 11,
            name = "Electric Skyway",
            description = "Moving platforms over electric spike pits require precise jump timing.",
            width = 5000f, height = 800f, startX = 100f, startY = 520f, portalX = 4800f, portalY = 480f,
            platforms = listOf(
                BounceObstacle(0f, 600f, 400f, 100f),
                BounceObstacle(600f, 500f, 150f, 35f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.06f),
                BounceObstacle(1000f, 450f, 300f, 40f),
                BounceObstacle(1450f, 350f, 200f, 35f),
                BounceObstacle(1800f, 480f, 100f, 35f, isSpring = true, springForce = -680f),
                BounceObstacle(2000f, 250f, 300f, 35f),
                BounceObstacle(2500f, 550f, 400f, 40f),
                BounceObstacle(3100f, 450f, 160f, 35f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.05f),
                BounceObstacle(3400f, 300f, 250f, 35f),
                BounceObstacle(3850f, 500f, 400f, 40f),
                BounceObstacle(4400f, 550f, 600f, 100f),
                BounceObstacle(400f, 750f, 4000f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1150f, y = 400f),
                BounceCheckpoint(id = 2, x = 3950f, y = 450f)
            ),
            enemies = listOf(
                BounceEnemy(id = 1101, type = EnemyType.WALKING, x = 1100f, y = 410f, moveRangeX = 120f),
                BounceEnemy(id = 1102, type = EnemyType.FLYING, x = 2100f, y = 200f, moveRangeX = 100f)
            ),
            keys = listOf(
                BounceKey(id = 11, x = 3500f, y = 230f, colorHex = 0xFF00E5FF)
            ),
            doors = listOf(
                BounceDoor(id = 11, x = 4100f, y = 420f, width = 24f, height = 80f, keyIdNeeded = 11, keyColorHex = 0xFF00E5FF)
            ),
            collectibles = listOf(
                BounceCollectible(1550f, 280f, isStar = true),
                BounceCollectible(2150f, 180f, isStar = true),
                BounceCollectible(3500f, 230f, isStar = true)
            ),
            baseRewardCoins = 265
        )
    )

    // LEVEL 12: Molten Core
    list.add(
        BounceLevel(
            number = 12,
            name = "Molten Core",
            description = "Brace yourself against relentless sawblades, crumbling obsidian & rising lava.",
            width = 5200f, height = 800f, startX = 100f, startY = 540f, portalX = 4900f, portalY = 500f,
            platforms = listOf(
                BounceObstacle(0f, 620f, 500f, 100f),
                BounceObstacle(700f, 520f, 200f, 35f),
                BounceObstacle(1050f, 420f, 150f, 35f, isFallingPlatform = true),
                BounceObstacle(1350f, 500f, 150f, 35f, isSpring = true, springForce = -690f),
                BounceObstacle(1600f, 250f, 300f, 40f),
                BounceObstacle(2100f, 580f, 500f, 120f),
                BounceObstacle(2800f, 480f, 160f, 35f, isMoving = true, moveRangeX = 140f, moveSpeed = 0.07f),
                BounceObstacle(3200f, 500f, 350f, 40f),
                BounceObstacle(3750f, 400f, 180f, 35f),
                BounceObstacle(4100f, 300f, 250f, 35f),
                BounceObstacle(4600f, 580f, 600f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1750f, y = 200f),
                BounceCheckpoint(id = 2, x = 3300f, y = 450f)
            ),
            enemies = listOf(
                BounceEnemy(id = 1201, type = EnemyType.WALKING, x = 1700f, y = 210f, moveRangeX = 100f),
                BounceEnemy(id = 1202, type = EnemyType.FLYING, x = 2900f, y = 300f, moveRangeX = 80f)
            ),
            keys = listOf(
                BounceKey(id = 12, x = 4200f, y = 230f, colorHex = 0xFFFF1744)
            ),
            doors = listOf(
                BounceDoor(id = 12, x = 4800f, y = 500f, keyIdNeeded = 12, keyColorHex = 0xFFFF1744)
            ),
            waterZones = listOf(
                BounceWaterZone(x = 600f, y = 640f, width = 1500f, height = 160f, waterColor = Color(0x99FF3D00))
            ),
            collectibles = listOf(
                BounceCollectible(1120f, 350f, isStar = true),
                BounceCollectible(1700f, 180f, isStar = true),
                BounceCollectible(4200f, 230f, isStar = true)
            ),
            baseRewardCoins = 280
        )
    )

    // LEVEL 13: Aquatic Cavern
    list.add(
        BounceLevel(
            number = 13,
            name = "Aquatic Cavern",
            description = "Navigate deep underwater tunnels with buoyant pushable box bridges.",
            width = 5300f, height = 800f, startX = 100f, startY = 500f, portalX = 4950f, portalY = 500f,
            platforms = listOf(
                BounceObstacle(0f, 580f, 450f, 120f),
                BounceObstacle(600f, 450f, 180f, 35f),
                BounceObstacle(900f, 580f, 300f, 40f),
                BounceObstacle(1350f, 580f, 100f, 35f, isSpring = true, springForce = -650f),
                BounceObstacle(1550f, 300f, 300f, 40f),
                BounceObstacle(2000f, 580f, 400f, 40f),
                BounceObstacle(2600f, 450f, 350f, 40f),
                BounceObstacle(3150f, 400f, 150f, 35f, isMoving = true, moveRangeX = 100f, moveSpeed = 0.05f),
                BounceObstacle(3500f, 250f, 250f, 35f),
                BounceObstacle(4000f, 550f, 350f, 40f),
                BounceObstacle(4650f, 580f, 650f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1700f, y = 250f),
                BounceCheckpoint(id = 2, x = 2750f, y = 400f)
            ),
            enemies = listOf(
                BounceEnemy(id = 1301, type = EnemyType.FLYING, x = 1000f, y = 400f, moveRangeX = 80f, moveRangeY = 100f)
            ),
            keys = listOf(
                BounceKey(id = 13, x = 3600f, y = 180f, colorHex = 0xFF00B0FF)
            ),
            doors = listOf(
                BounceDoor(id = 13, x = 4150f, y = 470f, keyIdNeeded = 13, keyColorHex = 0xFF00B0FF)
            ),
            waterZones = listOf(
                BounceWaterZone(x = 500f, y = 450f, width = 3500f, height = 350f, waterColor = Color(0x6600B0FF))
            ),
            collectibles = listOf(
                BounceCollectible(1050f, 510f, isStar = true),
                BounceCollectible(2200f, 510f, isStar = true),
                BounceCollectible(3600f, 180f, isStar = true)
            ),
            baseRewardCoins = 295
        )
    )

    // LEVEL 14: Dual Gate Trial
    list.add(
        BounceLevel(
            number = 14,
            name = "Dual Gate Trial",
            description = "Find two different colored keys to unlock both ancient color gates.",
            width = 5500f, height = 800f, startX = 100f, startY = 520f, portalX = 5200f, portalY = 520f,
            platforms = listOf(
                BounceObstacle(0f, 600f, 500f, 100f),
                BounceObstacle(600f, 450f, 300f, 35f),
                BounceObstacle(1100f, 300f, 200f, 35f),
                BounceObstacle(1500f, 520f, 400f, 40f),
                BounceObstacle(2100f, 450f, 400f, 40f),
                BounceObstacle(2700f, 300f, 200f, 35f),
                BounceObstacle(3100f, 480f, 150f, 35f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.06f),
                BounceObstacle(3500f, 550f, 400f, 40f),
                BounceObstacle(4100f, 550f, 120f, 35f, isSpring = true, springForce = -680f),
                BounceObstacle(4400f, 320f, 300f, 35f),
                BounceObstacle(4900f, 600f, 600f, 100f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1700f, y = 470f),
                BounceCheckpoint(id = 2, x = 3700f, y = 500f)
            ),
            keys = listOf(
                BounceKey(id = 14, x = 1200f, y = 230f, colorHex = 0xFFFFD700),
                BounceKey(id = 114, x = 2800f, y = 230f, colorHex = 0xFF00E5FF)
            ),
            doors = listOf(
                BounceDoor(id = 14, x = 1700f, y = 440f, keyIdNeeded = 14, keyColorHex = 0xFFFFD700),
                BounceDoor(id = 114, x = 3700f, y = 470f, keyIdNeeded = 114, keyColorHex = 0xFF00E5FF)
            ),
            collectibles = listOf(
                BounceCollectible(1200f, 230f, isStar = true),
                BounceCollectible(2800f, 230f, isStar = true),
                BounceCollectible(4550f, 250f, isStar = true)
            ),
            baseRewardCoins = 310
        )
    )

    // LEVEL 15: Heavy Iron Bridge
    list.add(
        BounceLevel(
            number = 15,
            name = "Heavy Iron Bridge",
            description = "Push heavy iron boxes into water pits to form safe stepping bridges.",
            width = 5600f, height = 850f, startX = 100f, startY = 540f, portalX = 5200f, portalY = 540f,
            platforms = listOf(
                BounceObstacle(0f, 620f, 450f, 120f),
                BounceObstacle(600f, 520f, 250f, 40f),
                BounceObstacle(950f, 420f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(1200f, 550f, 120f, 35f, isSpring = true, springForce = -680f),
                BounceObstacle(1450f, 280f, 350f, 40f),
                BounceObstacle(2000f, 500f, 500f, 40f),
                BounceObstacle(2700f, 420f, 200f, 35f, isMoving = true, moveRangeX = 140f, moveSpeed = 0.07f),
                BounceObstacle(3100f, 300f, 300f, 40f),
                BounceObstacle(3600f, 520f, 300f, 40f),
                BounceObstacle(4100f, 580f, 450f, 120f),
                BounceObstacle(4800f, 620f, 800f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1600f, y = 230f),
                BounceCheckpoint(id = 2, x = 2250f, y = 450f)
            ),
            keys = listOf(
                BounceKey(id = 15, x = 3250f, y = 230f, colorHex = 0xFF64748B)
            ),
            doors = listOf(
                BounceDoor(id = 15, x = 4300f, y = 500f, keyIdNeeded = 15, keyColorHex = 0xFF64748B)
            ),
            collectibles = listOf(
                BounceCollectible(1625f, 210f, isStar = true),
                BounceCollectible(2250f, 430f, isStar = true),
                BounceCollectible(3250f, 230f, isStar = true)
            ),
            baseRewardCoins = 325
        )
    )

    // LEVEL 16: Gargoyle Citadel
    list.add(
        BounceLevel(
            number = 16,
            name = "Gargoyle Citadel",
            description = "High density flying gargoyles and rotating sawblades guard the citadel!",
            width = 5800f, height = 900f, startX = 100f, startY = 520f, portalX = 5300f, portalY = 520f,
            platforms = listOf(
                BounceObstacle(0f, 600f, 500f, 100f),
                BounceObstacle(650f, 480f, 180f, 35f),
                BounceObstacle(950f, 380f, 150f, 35f, isMoving = true, moveRangeY = 120f, moveSpeed = 0.07f),
                BounceObstacle(1250f, 520f, 400f, 40f),
                BounceObstacle(1800f, 320f, 300f, 35f),
                BounceObstacle(2300f, 550f, 500f, 40f),
                BounceObstacle(3000f, 450f, 200f, 35f, isFallingPlatform = true),
                BounceObstacle(3350f, 250f, 250f, 35f),
                BounceObstacle(3800f, 480f, 180f, 35f, isMoving = true, moveRangeX = 140f, moveSpeed = 0.08f),
                BounceObstacle(4200f, 580f, 400f, 40f),
                BounceObstacle(4850f, 600f, 950f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1450f, y = 470f),
                BounceCheckpoint(id = 2, x = 2550f, y = 500f)
            ),
            keys = listOf(
                BounceKey(id = 16, x = 3475f, y = 180f, colorHex = 0xFFDC2626)
            ),
            doors = listOf(
                BounceDoor(id = 16, x = 4400f, y = 500f, keyIdNeeded = 16, keyColorHex = 0xFFDC2626)
            ),
            enemies = listOf(
                BounceEnemy(id = 1601, type = EnemyType.FLYING, x = 2000f, y = 250f, moveRangeX = 100f, moveRangeY = 40f, moveSpeed = 75f),
                BounceEnemy(id = 1602, type = EnemyType.FLYING, x = 3100f, y = 350f, moveRangeX = 120f, moveRangeY = 80f, moveSpeed = 85f)
            ),
            collectibles = listOf(
                BounceCollectible(1950f, 250f, isStar = true),
                BounceCollectible(3475f, 180f, isStar = true),
                BounceCollectible(5000f, 530f, isStar = true)
            ),
            baseRewardCoins = 340
        )
    )

    // LEVEL 17: Triple Checkpoint Descent
    list.add(
        BounceLevel(
            number = 17,
            name = "Triple Checkpoint Descent",
            description = "Massive endurance run with 3 checkpoints and high rewards!",
            width = 6000f, height = 1000f, startX = 100f, startY = 670f, portalX = 5500f, portalY = 670f,
            platforms = listOf(
                BounceObstacle(0f, 750f, 450f, 100f),
                BounceObstacle(600f, 600f, 300f, 40f),
                BounceObstacle(1050f, 480f, 180f, 35f),
                BounceObstacle(1350f, 650f, 120f, 35f, isSpring = true, springForce = -710f),
                BounceObstacle(1600f, 250f, 350f, 40f),
                BounceObstacle(2150f, 480f, 250f, 35f),
                BounceObstacle(2600f, 700f, 450f, 40f),
                BounceObstacle(3200f, 400f, 300f, 40f),
                BounceObstacle(3650f, 300f, 150f, 35f, isMoving = true, moveRangeY = 150f, moveSpeed = 0.08f),
                BounceObstacle(3950f, 200f, 300f, 40f),
                BounceObstacle(4450f, 600f, 400f, 40f),
                BounceObstacle(5100f, 750f, 900f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 800f, y = 550f),
                BounceCheckpoint(id = 2, x = 2800f, y = 650f),
                BounceCheckpoint(id = 3, x = 4100f, y = 150f)
            ),
            keys = listOf(
                BounceKey(id = 17, x = 4100f, y = 130f, colorHex = 0xFF38BDF8)
            ),
            doors = listOf(
                BounceDoor(id = 17, x = 4650f, y = 520f, keyIdNeeded = 17, keyColorHex = 0xFF38BDF8)
            ),
            collectibles = listOf(
                BounceCollectible(1775f, 180f, isStar = true),
                BounceCollectible(3350f, 330f, isStar = true),
                BounceCollectible(4100f, 130f, isStar = true)
            ),
            baseRewardCoins = 355
        )
    )

    // LEVEL 18: Sawblade Speedway
    list.add(
        BounceLevel(
            number = 18,
            name = "Sawblade Speedway",
            description = "High speed hazard gauntlet with narrow safety margins.",
            width = 6100f, height = 850f, startX = 100f, startY = 520f, portalX = 5600f, portalY = 520f,
            platforms = listOf(
                BounceObstacle(0f, 600f, 450f, 100f),
                BounceObstacle(600f, 500f, 150f, 35f, isMoving = true, moveRangeX = 140f, moveSpeed = 0.08f),
                BounceObstacle(900f, 420f, 250f, 35f),
                BounceObstacle(1300f, 520f, 120f, 30f, isFallingPlatform = true),
                BounceObstacle(1600f, 580f, 400f, 40f),
                BounceObstacle(2150f, 580f, 100f, 35f, isSpring = true, springForce = -720f),
                BounceObstacle(2400f, 220f, 350f, 40f),
                BounceObstacle(2900f, 450f, 200f, 35f),
                BounceObstacle(3300f, 580f, 400f, 40f),
                BounceObstacle(3850f, 400f, 180f, 35f),
                BounceObstacle(4200f, 280f, 300f, 40f),
                BounceObstacle(4650f, 550f, 400f, 40f),
                BounceObstacle(5250f, 600f, 850f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1800f, y = 530f),
                BounceCheckpoint(id = 2, x = 3500f, y = 530f)
            ),
            keys = listOf(
                BounceKey(id = 18, x = 4350f, y = 210f, colorHex = 0xFFF97316)
            ),
            doors = listOf(
                BounceDoor(id = 18, x = 4850f, y = 470f, keyIdNeeded = 18, keyColorHex = 0xFFF97316)
            ),
            enemies = listOf(
                BounceEnemy(id = 1801, type = EnemyType.ROTATING_HAZARD, x = 1100f, y = 320f, moveSpeed = 220f),
                BounceEnemy(id = 1802, type = EnemyType.ROTATING_HAZARD, x = 2550f, y = 150f, moveSpeed = 240f)
            ),
            collectibles = listOf(
                BounceCollectible(1025f, 350f, isStar = true),
                BounceCollectible(2575f, 150f, isStar = true),
                BounceCollectible(4350f, 210f, isStar = true)
            ),
            baseRewardCoins = 370
        )
    )

    // LEVEL 19: The Gauntlet
    list.add(
        BounceLevel(
            number = 19,
            name = "The Gauntlet",
            description = "All mechanics combined! Spikes, gargoyles, keys, water & crumbling floors.",
            width = 6300f, height = 900f, startX = 100f, startY = 570f, portalX = 5800f, portalY = 570f,
            platforms = listOf(
                BounceObstacle(0f, 650f, 450f, 120f),
                BounceObstacle(600f, 500f, 180f, 35f),
                BounceObstacle(950f, 550f, 250f, 40f),
                BounceObstacle(1350f, 520f, 150f, 35f),
                BounceObstacle(1650f, 580f, 400f, 40f),
                BounceObstacle(2200f, 450f, 160f, 35f, isMoving = true, moveRangeX = 140f, moveSpeed = 0.08f),
                BounceObstacle(2550f, 220f, 300f, 40f),
                BounceObstacle(3000f, 520f, 120f, 35f, isSpring = true, springForce = -690f),
                BounceObstacle(3300f, 550f, 450f, 40f),
                BounceObstacle(3900f, 450f, 150f, 35f, isFallingPlatform = true),
                BounceObstacle(4250f, 550f, 350f, 40f),
                BounceObstacle(4750f, 420f, 150f, 35f, isMoving = true, moveRangeY = 120f, moveSpeed = 0.09f),
                BounceObstacle(5100f, 600f, 350f, 40f),
                BounceObstacle(5650f, 650f, 650f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1850f, y = 530f),
                BounceCheckpoint(id = 2, x = 3500f, y = 500f)
            ),
            keys = listOf(
                BounceKey(id = 19, x = 2700f, y = 150f, colorHex = 0xFFEC4899)
            ),
            doors = listOf(
                BounceDoor(id = 19, x = 5250f, y = 520f, keyIdNeeded = 19, keyColorHex = 0xFFEC4899)
            ),
            waterZones = listOf(
                BounceWaterZone(x = 500f, y = 600f, width = 1200f, height = 200f, waterColor = Color(0x99FF3D00))
            ),
            collectibles = listOf(
                BounceCollectible(1075f, 480f, isStar = true),
                BounceCollectible(2700f, 150f, isStar = true),
                BounceCollectible(5250f, 450f, isStar = true)
            ),
            baseRewardCoins = 385
        )
    )

    // LEVEL 20: Bounce Supreme
    list.add(
        BounceLevel(
            number = 20,
            name = "Bounce Supreme",
            description = "The ultimate master trial! Complete Bounce Quest for grand glory!",
            width = 6500f, height = 900f, startX = 100f, startY = 570f, portalX = 6000f, portalY = 570f,
            platforms = listOf(
                BounceObstacle(0f, 650f, 500f, 120f),
                BounceObstacle(650f, 520f, 200f, 35f),
                BounceObstacle(1000f, 380f, 150f, 35f, isMoving = true, moveRangeY = 140f, moveSpeed = 0.09f),
                BounceObstacle(1300f, 580f, 400f, 40f),
                BounceObstacle(1850f, 580f, 120f, 35f, isSpring = true, springForce = -720f),
                BounceObstacle(2100f, 220f, 350f, 40f),
                BounceObstacle(2600f, 420f, 180f, 35f),
                BounceObstacle(2950f, 550f, 400f, 40f),
                BounceObstacle(3500f, 520f, 300f, 40f),
                BounceObstacle(3950f, 580f, 100f, 35f, isSpring = true, springForce = -670f),
                BounceObstacle(4200f, 250f, 300f, 40f),
                BounceObstacle(4650f, 580f, 450f, 40f),
                BounceObstacle(5250f, 450f, 180f, 35f),
                BounceObstacle(5600f, 650f, 900f, 120f)
            ),
            checkpoints = listOf(
                BounceCheckpoint(id = 1, x = 1500f, y = 530f),
                BounceCheckpoint(id = 2, x = 3150f, y = 500f)
            ),
            keys = listOf(
                BounceKey(id = 20, x = 4350f, y = 180f, colorHex = 0xFFFFD700)
            ),
            doors = listOf(
                BounceDoor(id = 20, x = 4850f, y = 500f, keyIdNeeded = 20, keyColorHex = 0xFFFFD700)
            ),
            collectibles = listOf(
                BounceCollectible(2275f, 150f, isStar = true),
                BounceCollectible(4350f, 180f, isStar = true),
                BounceCollectible(5800f, 580f, isStar = true)
            ),
            baseRewardCoins = 500
        )
    )


    return list.map { sanitizeAndValidateLevel(it) }
}

@Composable
fun BounceClassicScreen(
    viewModel: PlayWinViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val walletState by viewModel.walletState.collectAsStateWithLifecycle()
    val prefs = remember { context.getSharedPreferences("bounce_game_prefs", Context.MODE_PRIVATE) }

    // Level unlock & stars/scores persistence
    var unlockedLevel by remember { mutableIntStateOf(prefs.getInt("unlocked_level", 1)) }
    var levelStarsMap by remember {
        val loaded = mutableMapOf<Int, Int>()
        for (i in 1..unlockedLevel) {
            val s = prefs.getInt("level_stars_$i", 0)
            if (s > 0) loaded[i] = s
        }
        mutableStateOf(loaded.toMap())
    }
    var levelScoresMap by remember {
        val loaded = mutableMapOf<Int, Int>()
        for (i in 1..unlockedLevel) {
            val sc = prefs.getInt("level_score_$i", 0)
            if (sc > 0) loaded[i] = sc
        }
        mutableStateOf(loaded.toMap())
    }
    var historyList by remember {
        mutableStateOf(parseBounceHistory(prefs.getString("bounce_history", "") ?: ""))
    }

    // Ball Skins & Customization persistence
    var ownedSkins by remember {
        val setStr = prefs.getStringSet("owned_skins", setOf("skin_neon_violet")) ?: setOf("skin_neon_violet")
        mutableStateOf(setStr)
    }
    var selectedSkinId by remember {
        mutableStateOf(prefs.getString("selected_skin", "skin_neon_violet") ?: "skin_neon_violet")
    }

    // Daily Rewards persistence
    var dailyLastClaim by remember { mutableLongStateOf(prefs.getLong("daily_last_claim", 0L)) }
    var dailyStreak by remember { mutableIntStateOf(prefs.getInt("daily_streak", 1)) }

    // Settings & Control Preferences
    var sfxEnabled by remember { mutableStateOf(prefs.getBoolean("sfx_enabled", true)) }
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }
    var controlScale by remember { mutableFloatStateOf(prefs.getFloat("control_scale", 1.0f)) }

    // Dialog & UI overlay visibilities
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDailyRewardDialog by remember { mutableStateOf(false) }
    var showSkinsStoreDialog by remember { mutableStateOf(false) }
    var showLevelPreviewDialog by remember { mutableStateOf(false) }
    var previewLevelNum by remember { mutableIntStateOf(1) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showDailyMissionsDialog by remember { mutableStateOf(false) }
    var showPlayGamesDialog by remember { mutableStateOf(false) }
    var isCloudSynced by remember { mutableStateOf(false) }

    // Navigation & active play states
    var isPlaying by remember { mutableStateOf(false) }
    var selectedLevelNum by remember { mutableIntStateOf(1) }

    // Infinite Campaign levels are generated on-the-fly

    val totalStarsEarned = remember(levelStarsMap) { levelStarsMap.values.sum() }
    val isDailyClaimAvailable = remember(dailyLastClaim) {
        val now = System.currentTimeMillis()
        now - dailyLastClaim >= 24 * 3600 * 1000L
    }

    LaunchedEffect(Unit) {
        RewardedManager.preload(context)
    }

    // Orientation & Immersive Fullscreen management: Portrait for menus & World Map, Landscape + Immersive Fullscreen ONLY during active gameplay
    LaunchedEffect(isPlaying) {
        val activity = context as? Activity
        val window = activity?.window
        if (isPlaying) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            RewardedManager.preload(context)
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    window.attributes.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    window.attributes.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Intercept hardware Back press during gameplay
    BackHandler(enabled = isPlaying) {
        isPlaying = false
    }

    if (!isPlaying) {
        // --- WORLD MAP & MAIN MENU SCREEN (PORTRAIT IMMERSIVE) ---
        Scaffold(
            containerColor = Color(0xFF090615),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF131024), Color(0xFF0D0A1B))
                            )
                        )
                        .statusBarsPadding()
                        .displayCutoutPadding()
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    // Top Bar Header: Left Circular Back Button & Centered Title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Left: Circular Back Button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(40.dp)
                                .background(Color(0xFF1E1B2C), CircleShape)
                                .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Center: Perfectly Centered BOUNCE QUEST Title & Subtitle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(
                                text = "BOUNCE QUEST",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "CLASSIC ADVENTURE",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stat Counters Row: Stars, Coins, and Lives with Equal Spacing & No Clipping
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stars Counter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1B172B), RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$totalStarsEarned/60",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Coins Counter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1B172B), RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪙", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${walletState.coins}",
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Lives Counter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1B172B), RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0xFFFF00D6).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("❤️", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "3/3 Lives",
                                    color = Color(0xFFFF00D6),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Action Navigation Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Missions
                        Surface(
                            onClick = { showDailyMissionsDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B172B),
                            border = BorderStroke(1.dp, Color(0xFF00FF88).copy(alpha = 0.5f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text("🎯", fontSize = 12.sp)
                                Text("Missions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Badges
                        Surface(
                            onClick = { showAchievementsDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B172B),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text("🏆", fontSize = 12.sp)
                                Text("Badges", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Stats
                        Surface(
                            onClick = { showStatsDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B172B),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text("📊", fontSize = 12.sp)
                                Text("Stats", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Play Games
                        Surface(
                            onClick = { showPlayGamesDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B172B),
                            border = BorderStroke(1.dp, Color(0xFF34A853).copy(alpha = 0.5f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text("🎮", fontSize = 12.sp)
                                Text("Play Games", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Skins Store
                        Surface(
                            onClick = { showSkinsStoreDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B172B),
                            border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text("🎨", fontSize = 12.sp)
                                Text("Skins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Settings
                        Surface(
                            onClick = { showSettingsDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B172B),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp, start = 18.dp, end = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Campaign Banner Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF7C3AED).copy(alpha = 0.45f),
                                        Color(0xFF15102B),
                                        Color(0xFF00E5FF).copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🗺️ ENDLESS ADVENTURE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Complete procedural challenges, find secret chambers, and survive the trials to unlock infinite stages!",
                                    color = Color(0xFFD1D5DB),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.25f), CircleShape)
                                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "INFINITE",
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Main Stats Dashboard Panel
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131024))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Subtitle/Adventure Indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .background(Color(0xFF1E1838), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF00E5FF), CircleShape)
                                )
                                Text(
                                    text = "READY FOR DISCOVERY",
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Current Level Big Display
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Level $unlockedLevel",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Current Stage",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.08f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // Quick Stats Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Best Score Stat
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🏆 Best Score", color = Color(0xFFFF00D6), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val bestScore = levelScoresMap[unlockedLevel] ?: 0
                                    Text(
                                        text = if (bestScore > 0) "$bestScore" else "0",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(30.dp)
                                        .background(Color.White.copy(alpha = 0.08f))
                                )

                                // Total Stars Stat
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⭐ Total Stars", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$totalStarsEarned",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(30.dp)
                                        .background(Color.White.copy(alpha = 0.08f))
                                )

                                // Coins Stat
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🪙 Coins", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${walletState.coins}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Start Adventure Action Button (Glow + Play Action)
                item {
                    Button(
                        onClick = {
                            selectedLevelNum = unlockedLevel
                            isPlaying = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "START ADVENTURE",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // Recent Campaigns History Title
                item {
                    Text(
                        "📜 ADVENTURE JOURNAL",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }

                if (historyList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF13111C), RoundedCornerShape(12.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No adventures recorded yet.\nStart the adventure to log your legacy!",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(historyList) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF13111C), RoundedCornerShape(12.dp))
                                .border(0.5.dp, Color(0xFF7C3AED).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(entry.levelName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(entry.date, color = Color.Gray, fontSize = 10.sp)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(3) { i ->
                                        Text(if (i < entry.stars) "⭐" else "☆", fontSize = 10.sp)
                                    }
                                }
                                Text("+${entry.coins} 🪙", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- ACTIVE GAMEPLAY SCREEN (LANDSCAPE) ---
        val activeLevel = remember(selectedLevelNum) {
            SmartProceduralLevelGenerator.generateLevel(selectedLevelNum, context)
        }
        ActiveBounceQuestGame(
            level = activeLevel,
            viewModel = viewModel,
            selectedSkinId = selectedSkinId,
            onBackToMenu = { isPlaying = false },
            onLevelCompleted = { starsCollected, coinsCollected, finalScore, deaths, missedJumps, time, checkpointRespawns, discoveredSecret ->
                // Record session performance to adaptive difficulty engine
                com.myplaywin.app.data.AdaptiveDifficultyManager.recordSessionPerformance(
                    context = context,
                    levelNum = selectedLevelNum,
                    deaths = deaths,
                    missedJumps = missedJumps,
                    starsCollected = starsCollected,
                    totalLevelStars = activeLevel.collectibles.count { it.isStar },
                    timeSeconds = time,
                    checkpointRespawns = checkpointRespawns
                )

                // Mark Level as complete & update unlocked level
                val nextLevel = selectedLevelNum + 1
                if (nextLevel > unlockedLevel) {
                    unlockedLevel = nextLevel
                    prefs.edit().putInt("unlocked_level", nextLevel).apply()
                }

                // Update Stars & High Score
                val prevStars = levelStarsMap[selectedLevelNum] ?: 0
                if (starsCollected > prevStars) {
                    levelStarsMap = levelStarsMap + (selectedLevelNum to starsCollected)
                    prefs.edit().putInt("level_stars_$selectedLevelNum", starsCollected).apply()
                }
                val prevScore = levelScoresMap[selectedLevelNum] ?: 0
                if (finalScore > prevScore) {
                    levelScoresMap = levelScoresMap + (selectedLevelNum to finalScore)
                    prefs.edit().putInt("level_score_$selectedLevelNum", finalScore).apply()
                }

                // Dynamic Reward System: Rebalanced rewards for accomplishments
                var baseReward = 10 + (starsCollected * 1) + coinsCollected
                var bonusCoins = 0
                val bonusesEarned = mutableListOf<String>()

                val totalStars = activeLevel.collectibles.count { it.isStar }
                if (totalStars > 0 && starsCollected == totalStars) {
                    bonusCoins += 15
                    bonusesEarned.add("⭐ 100% Stars Perfect (+15)")
                }
                if (deaths == 0) {
                    bonusCoins += 20
                    bonusesEarned.add("🛡️ No-Death Flawless Run (+20)")
                }
                val isFastRun = time < (activeLevel.width * 0.025f).coerceAtMost(60f)
                if (isFastRun) {
                    bonusCoins += 15
                    bonusesEarned.add("⚡ Speed Demon Finish (+15)")
                }
                if (discoveredSecret) {
                    bonusCoins += 10
                    bonusesEarned.add("🔍 Secret Cave Discovered (+10)")
                }

                val totalReward = baseReward + bonusCoins
                if (bonusCoins > 0) {
                    viewModel.addCoins(bonusCoins, "Bounce Performance Bonuses Lvl $selectedLevelNum")
                    val bonusText = bonusesEarned.joinToString("\n")
                    android.widget.Toast.makeText(
                        context,
                        "🎉 PERFORMANCE BONUSES (+${bonusCoins} 🪙):\n$bonusText",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }

                // Append to history log
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                val currentDateStr = sdf.format(Date())
                val entry = BounceHistoryEntry(
                    date = currentDateStr,
                    levelName = activeLevel.name,
                    stars = starsCollected,
                    coins = totalReward,
                    score = finalScore
                )
                val updated = listOf(entry) + historyList.take(19)
                historyList = updated
                prefs.edit().putString("bounce_history", serializeBounceHistory(updated)).apply()

                selectedLevelNum = nextLevel
            }
        )
    }

    // --- DAILY LOGIN REWARDS DIALOG ---
    if (showDailyRewardDialog) {
        val dailyRewards = listOf(50, 50, 50, 50, 50, 50, 50)

        AlertDialog(
            onDismissRequest = { showDailyRewardDialog = false },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("📅 DAILY REWARDS STREAK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    IconButton(onClick = { showDailyRewardDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Log in every day to claim bonus PlayWin coins & unlock the legendary Prism Rainbow Ball Skin!", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // 7 Day Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        (0..6).chunked(4).forEach { chunk ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                chunk.forEach { dayIdx ->
                                    val dayNum = dayIdx + 1
                                    val rewardCoins = dailyRewards[dayIdx]
                                    val isClaimed = dayNum < dailyStreak
                                    val isToday = dayNum == dailyStreak

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isToday) Color(0xFFFF00D6).copy(alpha = 0.2f)
                                                else if (isClaimed) Color(0xFF1E1B2C)
                                                else Color(0xFF0D0A1B),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isToday) Color(0xFFFF00D6)
                                                else if (isClaimed) Color(0xFF7C3AED).copy(alpha = 0.4f)
                                                else Color.Gray.copy(alpha = 0.2f),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Day $dayNum", color = Color.Gray, fontSize = 9.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(if (dayNum == 7) "🌈" else "🪙", fontSize = 16.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                if (dayNum == 7) "SKIN" else "+$rewardCoins",
                                                color = if (isToday) Color(0xFFFF00D6) else Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reward = dailyRewards[(dailyStreak - 1).coerceIn(0, 6)]
                        viewModel.addCoins(reward, "Daily Login Day $dailyStreak")

                        if (dailyStreak == 7) {
                            ownedSkins = ownedSkins + "skin_rainbow_sparkle"
                            prefs.edit().putStringSet("owned_skins", ownedSkins).apply()
                        }

                        val now = System.currentTimeMillis()
                        dailyLastClaim = now
                        prefs.edit().putLong("daily_last_claim", now).apply()

                        val nextStreak = if (dailyStreak >= 7) 1 else dailyStreak + 1
                        dailyStreak = nextStreak
                        prefs.edit().putInt("daily_streak", nextStreak).apply()

                        android.widget.Toast.makeText(context, "Claimed $reward Coins!", android.widget.Toast.LENGTH_SHORT).show()
                        showDailyRewardDialog = false
                    },
                    enabled = isDailyClaimAvailable,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF00D6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isDailyClaimAvailable) "CLAIM TODAY'S REWARD 🪙" else "CLAIMED TODAY ✓",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        )
    }

    // --- BALL SKINS STORE DIALOG ---
    if (showSkinsStoreDialog) {
        AlertDialog(
            onDismissRequest = { showSkinsStoreDialog = false },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🎨 BALL SKINS STORE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    IconButton(onClick = { showSkinsStoreDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Customize your ball's radiant aura & particle trails!", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(BALL_SKINS) { skin ->
                            val isOwned = ownedSkins.contains(skin.id)
                            val isSelected = selectedSkinId == skin.id

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) Color(0xFF7C3AED).copy(alpha = 0.25f)
                                        else Color(0xFF1E1B2C),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFA855F7)
                                        else Color.Gray.copy(alpha = 0.2f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Ball Color Orb Preview
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(skin.primaryColor, skin.secondaryColor, skin.darkColor)
                                                ),
                                                CircleShape
                                            )
                                            .border(1.5.dp, skin.trailColor, CircleShape)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(skin.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(skin.description, color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    if (isSelected) {
                                        Text("EQUIPPED ✓", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 10.sp)
                                    } else if (isOwned) {
                                        Button(
                                            onClick = {
                                                selectedSkinId = skin.id
                                                prefs.edit().putString("selected_skin", skin.id).apply()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Text("EQUIP", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                if (walletState.coins >= skin.priceCoins) {
                                                    viewModel.addCoins(-skin.priceCoins, "Unlocked Skin ${skin.name}")
                                                    val updatedSkins = ownedSkins + skin.id
                                                    ownedSkins = updatedSkins
                                                    prefs.edit().putStringSet("owned_skins", updatedSkins).apply()

                                                    selectedSkinId = skin.id
                                                    prefs.edit().putString("selected_skin", skin.id).apply()

                                                    android.widget.Toast.makeText(context, "Unlocked ${skin.name}!", android.widget.Toast.LENGTH_SHORT).show()
                                                } else {
                                                    android.widget.Toast.makeText(context, "Need ${skin.priceCoins} coins!", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("${skin.priceCoins} 🪙", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSkinsStoreDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- SETTINGS OVERLAY DIALOG ---
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("⚙️ CONTROLS & SETTINGS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Customize virtual joystick size, vibration & audio options.", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Control Size:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.8f to "Small", 1.0f to "Medium", 1.25f to "Large").forEach { (sc, label) ->
                            val isSelected = kotlin.math.abs(controlScale - sc) < 0.05f
                            Button(
                                onClick = {
                                    controlScale = sc
                                    prefs.edit().putFloat("control_scale", sc).apply()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF7C3AED) else Color(0xFF1E1B2C)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text(label, color = Color.White, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vibration Feedback:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { checked ->
                                vibrationEnabled = checked
                                prefs.edit().putBoolean("vibration_enabled", checked).apply()
                                if (checked) triggerHaptic(context, true)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF7C3AED),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF1E1B2C)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Music & SFX Audio:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Switch(
                            checked = sfxEnabled,
                            onCheckedChange = { checked ->
                                sfxEnabled = checked
                                prefs.edit().putBoolean("sfx_enabled", checked).apply()
                                BounceAudioEngine.isMuted = !checked
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF7C3AED),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF1E1B2C)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- ACHIEVEMENTS DIALOG ---
    if (showAchievementsDialog) {
        val achievements = remember { BounceProgressionManager.getAchievements(prefs) }

        AlertDialog(
            onDismissRequest = { showAchievementsDialog = false },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🏆 BOUNCE ACHIEVEMENTS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    IconButton(onClick = { showAchievementsDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Complete milestones to unlock achievements & claim bonus coins!", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(achievements) { ach ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (ach.isUnlocked) Color(0xFF7C3AED).copy(alpha = 0.2f) else Color(0xFF1E1B2C),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (ach.isUnlocked) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.2f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Text(ach.icon, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(ach.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(ach.description, color = Color.Gray, fontSize = 10.sp)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (ach.isUnlocked) Color(0xFFFFD700).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            if (ach.isUnlocked) "UNLOCKED ✓ (+${ach.rewardCoins} 🪙)" else "LOCKED (+${ach.rewardCoins} 🪙)",
                                            color = if (ach.isUnlocked) Color(0xFFFFD700) else Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAchievementsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- GAME STATISTICS DIALOG ---
    if (showStatsDialog) {
        val stats = remember { BounceProgressionManager.getStats(prefs) }

        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("📊 BOUNCE CAREER STATS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    IconButton(onClick = { showStatsDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val formattedPlayTime = "${stats.totalPlayTimeSeconds / 60}m ${stats.totalPlayTimeSeconds % 60}s"
                    val formattedBestTime = if (stats.bestTimeSeconds >= 9900f) "--" else "${stats.bestTimeSeconds.toInt()}s"

                    val statItems = listOf(
                        "⏱️ Total Play Time" to formattedPlayTime,
                        "🏁 Levels Completed" to "${stats.levelsCompletedCount}",
                        "🪙 Coins Collected" to "${stats.totalCoinsCollected}",
                        "⭐ Stars Earned" to "${stats.totalStarsCollected}",
                        "💥 Total Deaths" to "${stats.deathCount}",
                        "⚡ Best Level Completion Time" to formattedBestTime
                    )

                    statItems.forEach { (label, valStr) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1B2C), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, color = Color.Gray, fontSize = 12.sp)
                                Text(valStr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showStatsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- DAILY MISSIONS DIALOG ---
    if (showDailyMissionsDialog) {
        val missions = remember { BounceProgressionManager.getDailyMissions(prefs) }

        AlertDialog(
            onDismissRequest = { showDailyMissionsDialog = false },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🎯 DAILY MISSIONS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    IconButton(onClick = { showDailyMissionsDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Complete daily missions for direct coin rewards!", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        missions.forEach { m ->
                            val isReady = m.currentProgress >= m.maxProgress && !m.isClaimed

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1B2C), RoundedCornerShape(10.dp))
                                    .border(
                                        1.dp,
                                        if (isReady) Color(0xFF00FF88) else Color.Gray.copy(alpha = 0.2f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(m.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(m.description, color = Color.Gray, fontSize = 10.sp)
                                        }

                                        if (m.isClaimed) {
                                            Text("CLAIMED ✓", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        } else {
                                            Button(
                                                onClick = {
                                                    BounceProgressionManager.claimDailyMission(context, prefs, viewModel, m.id)
                                                    showDailyMissionsDialog = false
                                                },
                                                enabled = isReady,
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    if (isReady) "CLAIM +${m.rewardCoins} 🪙" else "${m.currentProgress}/${m.maxProgress}",
                                                    color = if (isReady) Color.Black else Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { (m.currentProgress.toFloat() / m.maxProgress).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = Color(0xFF00FF88),
                                        trackColor = Color(0xFF0D0A1B)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDailyMissionsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- GOOGLE PLAY GAMES SERVICES DIALOG ---
    if (showPlayGamesDialog) {
        AlertDialog(
            onDismissRequest = { showPlayGamesDialog = false },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🎮 GOOGLE PLAY GAMES", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    IconButton(onClick = { showPlayGamesDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF34A853).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF34A853).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🟢", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Play Games Connected", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Cloud Saves & Global Leaderboards Active", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isCloudSynced = true
                            android.widget.Toast.makeText(context, "☁️ Cloud Save Synced with Play Games!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isCloudSynced) "CLOUD SAVED ✓" else "☁️ SYNC CLOUD PROGRESS", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            android.widget.Toast.makeText(context, "🏆 Syncing Play Games Leaderboards...", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B2C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🥇 View Global Leaderboard", color = Color.White)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPlayGamesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// --- ACTIVE GAMEPLAY LAYOUT & ENGINE ---
data class BounceParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    var size: Float,
    val color: Color,
    var isSparkle: Boolean = false
)

enum class PlatformState { STABLE, SHAKING, FALLING, RESPAWNING }

class FallingState(
    var state: PlatformState = PlatformState.STABLE,
    var timer: Float = 0f,
    var offsetY: Float = 0f,
    var shakeX: Float = 0f,
    var alpha: Float = 1f
)

data class BounceCoinParticle(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val delayMs: Int,
    val durationMs: Int
)

@Composable
fun ActiveBounceQuestGame(
    level: BounceLevel,
    viewModel: PlayWinViewModel,
    selectedSkinId: String = "skin_neon_violet",
    onBackToMenu: () -> Unit,
    onLevelCompleted: (stars: Int, coins: Int, score: Int, deaths: Int, missedJumps: Int, time: Float, checkpointRespawns: Int, discoveredSecret: Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeLevel = remember(level) { sanitizeAndValidateLevel(level) }

    // Control preferences & settings state
    val prefs = remember { context.getSharedPreferences("bounce_game_prefs", Context.MODE_PRIVATE) }
    var activeSkinId by remember(selectedSkinId) {
        mutableStateOf(prefs.getString("selected_skin", selectedSkinId) ?: selectedSkinId)
    }
    val currentSkin = remember(activeSkinId) {
        BALL_SKINS.find { it.id == activeSkinId } ?: BALL_SKINS.first()
    }
    var controlScale by remember { mutableFloatStateOf(prefs.getFloat("control_scale", 1.0f)) }
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSkinsInGameDialog by remember { mutableStateOf(false) }
    val isDebugExitOverlayEnabled = false
    var joystickXAmount by remember { mutableFloatStateOf(0f) }

    // Dynamic Floating Joystick State
    var isJoystickActive by remember { mutableStateOf(false) }
    var joystickCenterPos by remember { mutableStateOf<Offset?>(null) }
    var joystickKnobPos by remember { mutableStateOf<Offset?>(null) }
    var lastVibratedDirection by remember { mutableIntStateOf(0) }

    // --- REWARD & VICTORY DIALOG STATES ---
    var showVictoryDialog by remember(activeLevel) { mutableStateOf(true) }
    var rewardState by remember(activeLevel) {
        mutableStateOf(
            prefs.getString("bounce_reward_state_level_${activeLevel.number}", "Pending") ?: "Pending"
        )
    }
    var isProcessingReward by remember { mutableStateOf(false) }

    // Coin animation states
    var showCoinAnimation by remember { mutableStateOf(false) }
    var coinParticles by remember { mutableStateOf<List<BounceCoinParticle>>(emptyList()) }
    val coinAnimProgress = remember { androidx.compose.animation.core.Animatable(0f) }

    fun triggerCoinFlyAnimation(onFinished: () -> Unit) {
        val particles = (1..15).map { i ->
            BounceCoinParticle(
                id = i,
                startX = 100f + (Math.random() * 200f).toFloat(),
                startY = 350f + (Math.random() * 100f).toFloat(),
                endX = 150f + (Math.random() * 100f).toFloat(),
                endY = 50f + (Math.random() * 40f).toFloat(),
                delayMs = (i * 45),
                durationMs = 600 + (Math.random() * 250).toInt()
            )
        }
        coinParticles = particles
        showCoinAnimation = true
        
        scope.launch {
            coinAnimProgress.snapTo(0f)
            coinAnimProgress.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 1400,
                    easing = androidx.compose.animation.core.LinearEasing
                )
            )
            showCoinAnimation = false
            onFinished()
        }
    }

    LaunchedEffect(activeLevel) {
        prefs.edit().putString("bounce_reward_state_level_${activeLevel.number}", "Pending").commit()
    }

    // Physics & Game Engine Constants
    val ballRadius = 14f
    val maxSpeed = 240f
    val acceleration = 1800f
    val friction = 2000f
    val gravity = 850f
    val jumpForce = -490f
    val coyoteTimeMax = 0.12f
    val jumpBufferMax = 0.12f

    // Ball physics variables
    var ballX by remember(activeLevel) { mutableFloatStateOf(activeLevel.startX) }
    var ballY by remember(activeLevel) { mutableFloatStateOf(activeLevel.startY) }
    var ballVx by remember { mutableFloatStateOf(0f) }
    var ballVy by remember { mutableFloatStateOf(0f) }

    // Visual animation variables
    var squashX by remember { mutableFloatStateOf(1f) }
    var squashY by remember { mutableFloatStateOf(1f) }
    var rollAngle by remember { mutableFloatStateOf(0f) }
    var cameraX by remember(activeLevel) { mutableFloatStateOf((activeLevel.startX - 800f / 2f).safeCoerceIn(0f, (activeLevel.width - 800f).coerceAtLeast(0f))) }
    var cameraY by remember(activeLevel) { mutableFloatStateOf((activeLevel.startY - 500f * 0.42f).safeCoerceIn(0f, (activeLevel.height - 500f).coerceAtLeast(0f))) }
    var viewportWidthWorld by remember { mutableFloatStateOf(800f) }
    var viewportHeightWorld by remember { mutableFloatStateOf(500f) }

    // Screen Shake & Damage Invincibility
    var cameraShakeTimer by remember { mutableFloatStateOf(0f) }
    var cameraShakeIntensity by remember { mutableFloatStateOf(0f) }
    var shakeDx by remember { mutableFloatStateOf(0f) }
    var shakeDy by remember { mutableFloatStateOf(0f) }
    var invincibilityTimer by remember { mutableFloatStateOf(0f) }

    // Particles list
    val particles = remember { mutableStateListOf<BounceParticle>() }

    // Gameplay & Health states
    var livesRemaining by remember { mutableIntStateOf(3) }
    var isExitAnimating by remember { mutableStateOf(false) }
    var exitAnimationTimer by remember { mutableFloatStateOf(0f) }
    var isLoadingNextLevel by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }
    var transitionAlpha by remember { mutableFloatStateOf(0f) }
    var hasUsedAdRevive by remember { mutableStateOf(false) }
    var elapsedTimeSeconds by remember { mutableFloatStateOf(0f) }
    var lastCheckpointX by remember(activeLevel) { mutableFloatStateOf(activeLevel.startX) }
    var lastCheckpointY by remember(activeLevel) { mutableFloatStateOf(activeLevel.startY) }

    var isGrounded by remember { mutableStateOf(false) }
    var wasGrounded by remember { mutableStateOf(false) }
    var coyoteTimer by remember { mutableFloatStateOf(0f) }
    var jumpBufferTimer by remember { mutableFloatStateOf(0f) }

    var isPaused by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var gameCompleted by remember { mutableStateOf(false) }

    // Performance telemetry states
    var levelAttemptDeaths by remember { mutableIntStateOf(0) }
    var missedJumps by remember { mutableIntStateOf(0) }
    var checkpointRespawns by remember { mutableIntStateOf(0) }

    // Water states
    var isSubmergedInWater by remember { mutableStateOf(false) }
    var lastWaterState by remember { mutableStateOf(false) }
    var waterBubbleTimer by remember { mutableFloatStateOf(0f) }

    // Dynamic Phase 3 Game World Entities
    val totalLevelStars = remember(activeLevel) { activeLevel.collectibles.count { it.isStar } }
    val dynamicCollectibles = remember(activeLevel) {
        activeLevel.collectibles.map { it.copy() }.toMutableStateList()
    }
    val dynamicObstacles = remember(activeLevel) {
        activeLevel.platforms.map { it.copy() }.toMutableStateList()
    }
    val dynamicCheckpoints = remember(activeLevel) {
        activeLevel.checkpoints.map { it.copy() }.toMutableStateList()
    }
    val dynamicEnemies = remember(activeLevel) {
        activeLevel.enemies.map { it.copy() }.toMutableStateList()
    }
    val dynamicKeys = remember(activeLevel) {
        activeLevel.keys.map { it.copy() }.toMutableStateList()
    }
    val dynamicDoors = remember(activeLevel) {
        activeLevel.doors.map { it.copy() }.toMutableStateList()
    }
    val dynamicInteractiveBlocks = remember(activeLevel) {
        activeLevel.interactiveBlocks.map { it.copy() }.toMutableStateList()
    }

    // Dynamic states for falling platforms and spring compression
    val fallingPlatformStates = remember(activeLevel) {
        List(activeLevel.platforms.size) { FallingState() }
    }
    val springCompressions = remember(activeLevel) {
        mutableStateListOf(*Array(activeLevel.platforms.size) { 0f })
    }

    var starsCollected by remember { mutableIntStateOf(0) }
    var coinsCollected by remember { mutableIntStateOf(0) }
    var currentScore by remember { mutableIntStateOf(0) }

    // On-screen controls state
    var isHoldingLeft by remember { mutableStateOf(false) }
    var isHoldingRight by remember { mutableStateOf(false) }
    var jumpRequested by remember { mutableStateOf(false) }

    // Portal rotation state
    val portalRotation by animateFloatAsState(
        targetValue = if (isPaused) 0f else 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portal_spin"
    )

    // Formatted level time
    val formattedTime = remember(elapsedTimeSeconds) {
        val mins = (elapsedTimeSeconds.toInt() / 60)
        val secs = (elapsedTimeSeconds.toInt() % 60)
        String.format("%02d:%02d", mins, secs)
    }

    // --- PARTICLE GENERATOR HELPERS ---
    fun spawnVictoryConfetti(x: Float, y: Float) {
        val colors = listOf(
            Color(0xFFFF00D6), Color(0xFF00E5FF), Color(0xFFFFD700),
            Color(0xFF22C55E), Color(0xFFFF3D00), Color(0xFFA855F7)
        )
        val random = java.util.Random()
        for (i in 0 until 50) {
            val angle = random.nextFloat() * 2f * Math.PI
            val speed = 60f + random.nextFloat() * 180f
            particles.add(
                BounceParticle(
                    x = x,
                    y = y,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed - 60f).toFloat(), // Upwards bias
                    alpha = 1.0f,
                    size = 3.5f + random.nextFloat() * 4.5f,
                    color = colors[random.nextInt(colors.size)],
                    isSparkle = random.nextBoolean()
                )
            )
        }
    }

    fun spawnLandingDust(x: Float, y: Float) {
        for (i in 0 until 8) {
            val angle = Math.PI + (Math.random() - 0.5) * Math.PI * 0.85
            val speed = 25f + Math.random().toFloat() * 45f
            particles.add(
                BounceParticle(
                    x = x + (Math.random().toFloat() - 0.5f) * 14f,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed - 15f,
                    alpha = 0.85f,
                    size = 2.5f + Math.random().toFloat() * 3.5f,
                    color = currentSkin.trailColor.copy(alpha = 0.75f)
                )
            )
        }
    }

    fun spawnJumpDust(x: Float, y: Float) {
        for (i in 0 until 6) {
            val angle = Math.PI + (Math.random() - 0.5) * Math.PI * 0.7
            val speed = 20f + Math.random().toFloat() * 30f
            particles.add(
                BounceParticle(
                    x = x + (Math.random().toFloat() - 0.5f) * 10f,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    alpha = 0.75f,
                    size = 2f + Math.random().toFloat() * 3f,
                    color = currentSkin.primaryColor.copy(alpha = 0.75f)
                )
            )
        }
    }

    fun spawnSkinMotionTrail(x: Float, y: Float, vx: Float) {
        if (Math.random() < 0.35) {
            particles.add(
                BounceParticle(
                    x = x + (Math.random().toFloat() - 0.5f) * 6f,
                    y = y + (Math.random().toFloat() - 0.5f) * 6f,
                    vx = -vx * 0.12f + (Math.random().toFloat() - 0.5f) * 15f,
                    vy = (Math.random().toFloat() - 0.5f) * 15f - 8f,
                    alpha = 0.65f,
                    size = 2f + Math.random().toFloat() * 2.5f,
                    color = currentSkin.trailColor,
                    isSparkle = (currentSkin.id == "skin_rainbow_sparkle" || currentSkin.id == "skin_cyber_cyan") && Math.random() < 0.4
                )
            )
        }
    }

    fun spawnStarSparkles(x: Float, y: Float) {
        for (i in 0 until 12) {
            val angle = Math.random() * Math.PI * 2.0
            val speed = 40f + Math.random().toFloat() * 70f
            particles.add(
                BounceParticle(
                    x = x,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    alpha = 1f,
                    size = 3.5f + Math.random().toFloat() * 4.5f,
                    color = if (i % 2 == 0) Color(0xFFFFD700) else Color(0xFFFF9100),
                    isSparkle = true
                )
            )
        }
    }

    fun spawnCoinSparkles(x: Float, y: Float) {
        for (i in 0 until 8) {
            val angle = Math.random() * Math.PI * 2.0
            val speed = 30f + Math.random().toFloat() * 50f
            particles.add(
                BounceParticle(
                    x = x,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    alpha = 1f,
                    size = 2.5f + Math.random().toFloat() * 3f,
                    color = Color(0xFF00E5FF),
                    isSparkle = true
                )
            )
        }
    }

    fun spawnCheckpointBeam(x: Float, y: Float) {
        for (i in 0 until 18) {
            val angle = -Math.PI / 2.0 + (Math.random() - 0.5) * Math.PI * 0.75
            val speed = 50f + Math.random().toFloat() * 90f
            particles.add(
                BounceParticle(
                    x = x,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    alpha = 1f,
                    size = 3f + Math.random().toFloat() * 4f,
                    color = Color(0xFF00FF88),
                    isSparkle = true
                )
            )
        }
    }

    fun spawnSpringBurst(x: Float, y: Float) {
        for (i in 0 until 10) {
            val angle = -Math.PI / 2.0 + (Math.random() - 0.5) * Math.PI * 0.8
            val speed = 40f + Math.random().toFloat() * 60f
            particles.add(
                BounceParticle(
                    x = x,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    alpha = 0.9f,
                    size = 3f + Math.random().toFloat() * 4f,
                    color = Color(0xFF76FF03)
                )
            )
        }
    }

    fun spawnSpikeHitBurst(x: Float, y: Float) {
        for (i in 0 until 16) {
            val angle = Math.random() * Math.PI * 2.0
            val speed = 60f + Math.random().toFloat() * 100f
            particles.add(
                BounceParticle(
                    x = x,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    alpha = 1f,
                    size = 3.5f + Math.random().toFloat() * 4f,
                    color = Color(0xFFFF3D00),
                    isSparkle = true
                )
            )
        }
    }

    fun spawnEnemyHitBurst(x: Float, y: Float) {
        for (i in 0 until 18) {
            val angle = Math.random() * Math.PI * 2.0
            val speed = 80f + Math.random().toFloat() * 120f
            particles.add(
                BounceParticle(
                    x = x,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    alpha = 1f,
                    size = 4f + Math.random().toFloat() * 5f,
                    color = Color(0xFFFF1744),
                    isSparkle = true
                )
            )
        }
    }

    fun spawnWaterSplash(x: Float, y: Float) {
        for (i in 0 until 14) {
            val angle = -Math.PI / 2.0 + (Math.random() - 0.5) * Math.PI * 0.7
            val speed = 60f + Math.random().toFloat() * 90f
            particles.add(
                BounceParticle(
                    x = x,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    alpha = 0.9f,
                    size = 3f + Math.random().toFloat() * 4f,
                    color = Color(0xFF00E5FF)
                )
            )
        }
    }

    fun spawnWaterBubbles(x: Float, y: Float) {
        for (i in 0 until 3) {
            particles.add(
                BounceParticle(
                    x = x + (Math.random().toFloat() - 0.5f) * 12f,
                    y = y,
                    vx = (Math.random().toFloat() - 0.5f) * 15f,
                    vy = -20f - Math.random().toFloat() * 25f,
                    alpha = 0.8f,
                    size = 2.5f + Math.random().toFloat() * 3f,
                    color = Color(0xB2E0F7FA)
                )
            )
        }
    }

    fun spawnKeyCollectParticles(x: Float, y: Float, color: Color) {
        for (i in 0 until 16) {
            val angle = Math.random() * Math.PI * 2.0
            val speed = 50f + Math.random().toFloat() * 80f
            particles.add(
                BounceParticle(
                    x = x,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    alpha = 1f,
                    size = 4f + Math.random().toFloat() * 4f,
                    color = color,
                    isSparkle = true
                )
            )
        }
    }

    fun spawnDoorUnlockParticles(x: Float, y: Float, color: Color) {
        for (i in 0 until 24) {
            val angle = Math.random() * Math.PI * 2.0
            val speed = 70f + Math.random().toFloat() * 110f
            particles.add(
                BounceParticle(
                    x = x,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    alpha = 1f,
                    size = 4f + Math.random().toFloat() * 5f,
                    color = color,
                    isSparkle = true
                )
            )
        }
    }

    fun spawnBlockBreakDebris(x: Float, y: Float) {
        for (i in 0 until 12) {
            val angle = Math.random() * Math.PI * 2.0
            val speed = 60f + Math.random().toFloat() * 100f
            particles.add(
                BounceParticle(
                    x = x,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed - 40f,
                    alpha = 1f,
                    size = 3.5f + Math.random().toFloat() * 4f,
                    color = Color(0xFFFFB74D)
                )
            )
        }
    }

    fun handlePlayerDeath(isSpikeHit: Boolean = false) {
        if (invincibilityTimer > 0f) return
        triggerHaptic(context, vibrationEnabled)
        BounceAudioEngine.playEnemyHit()
        BounceProgressionManager.recordDeath(prefs)
        cameraShakeTimer = 0f
        cameraShakeIntensity = 0f
        invincibilityTimer = 1.2f

        // Telemetry tracking
        levelAttemptDeaths++
        if (isSpikeHit) {
            missedJumps++
        }

        if (isSpikeHit) {
            spawnSpikeHitBurst(ballX, ballY)
        } else {
            spawnEnemyHitBurst(ballX, ballY)
        }

        livesRemaining--
        if (livesRemaining > 0) {
            ballX = lastCheckpointX
            ballY = lastCheckpointY - 10f
            ballVx = 0f
            ballVy = 0f
            squashX = 1.2f
            squashY = 0.8f
            cameraX = (lastCheckpointX - viewportWidthWorld / 2f).safeCoerceIn(0f, (activeLevel.width - viewportWidthWorld).coerceAtLeast(0f))
            cameraY = (lastCheckpointY - viewportHeightWorld * 0.42f).safeCoerceIn(0f, (activeLevel.height - viewportHeightWorld).coerceAtLeast(0f))
            spawnCheckpointBeam(lastCheckpointX, lastCheckpointY)
        } else {
            BounceAudioEngine.playGameOver()
            isGameOver = true
        }
    }

    fun respawnAtCheckpoint() {
        checkpointRespawns++
        livesRemaining = 3
        ballX = lastCheckpointX
        ballY = lastCheckpointY - 10f
        ballVx = 0f
        ballVy = 0f
        squashX = 1f
        squashY = 1f
        invincibilityTimer = 0f
        cameraShakeTimer = 0f
        cameraX = (lastCheckpointX - viewportWidthWorld / 2f).safeCoerceIn(0f, (activeLevel.width - viewportWidthWorld).coerceAtLeast(0f))
        cameraY = (lastCheckpointY - viewportHeightWorld * 0.42f).safeCoerceIn(0f, (activeLevel.height - viewportHeightWorld).coerceAtLeast(0f))
        isGameOver = false
        isGrounded = false
        particles.clear()
    }

    fun restartActiveGame() {
        ballX = activeLevel.startX
        ballY = activeLevel.startY
        ballVx = 0f
        ballVy = 0f
        livesRemaining = 3
        hasUsedAdRevive = false
        elapsedTimeSeconds = 0f
        lastCheckpointX = activeLevel.startX
        lastCheckpointY = activeLevel.startY
        squashX = 1f
        squashY = 1f
        rollAngle = 0f
        cameraX = (activeLevel.startX - viewportWidthWorld / 2f).safeCoerceIn(0f, (activeLevel.width - viewportWidthWorld).coerceAtLeast(0f))
        cameraY = (activeLevel.startY - viewportHeightWorld * 0.42f).safeCoerceIn(0f, (activeLevel.height - viewportHeightWorld).coerceAtLeast(0f))
        invincibilityTimer = 0f
        cameraShakeTimer = 0f
        isGrounded = false
        wasGrounded = false
        coyoteTimer = 0f
        jumpBufferTimer = 0f
        isGameOver = false
        gameCompleted = false
        starsCollected = 0
        coinsCollected = 0
        currentScore = 0
        isHoldingLeft = false
        isHoldingRight = false
        joystickXAmount = 0f
        jumpRequested = false
        particles.clear()

        for (i in dynamicCollectibles.indices) {
            dynamicCollectibles[i] = dynamicCollectibles[i].copy(isCollected = false)
        }
        for (i in dynamicCheckpoints.indices) {
            dynamicCheckpoints[i] = dynamicCheckpoints[i].copy(isActivated = false)
        }
        for (i in dynamicEnemies.indices) {
            dynamicEnemies[i] = dynamicEnemies[i].copy()
        }
        for (i in dynamicKeys.indices) {
            dynamicKeys[i] = dynamicKeys[i].copy(isCollected = false)
        }
        for (i in dynamicDoors.indices) {
            dynamicDoors[i] = dynamicDoors[i].copy(isUnlocked = false, unlockAnimProgress = 0f)
        }
        for (i in dynamicInteractiveBlocks.indices) {
            dynamicInteractiveBlocks[i] = dynamicInteractiveBlocks[i].copy(
                isDestroyed = false,
                durability = 1,
                currentX = dynamicInteractiveBlocks[i].x,
                currentY = dynamicInteractiveBlocks[i].y,
                vx = 0f,
                vy = 0f,
                opacity = 1f
            )
        }
        for (i in fallingPlatformStates.indices) {
            fallingPlatformStates[i].state = PlatformState.STABLE
            fallingPlatformStates[i].timer = 0f
            fallingPlatformStates[i].offsetY = 0f
            fallingPlatformStates[i].shakeX = 0f
            fallingPlatformStates[i].alpha = 1f
        }
        for (i in springCompressions.indices) {
            springCompressions[i] = 0f
        }
    }

    LaunchedEffect(activeLevel) {
        ballX = activeLevel.startX
        ballY = activeLevel.startY
        ballVx = 0f
        ballVy = 0f
        livesRemaining = 3
        isExitAnimating = false
        exitAnimationTimer = 0f
        isLoadingNextLevel = false
        transitionAlpha = 0f
        hasUsedAdRevive = false
        elapsedTimeSeconds = 0f
        lastCheckpointX = activeLevel.startX
        lastCheckpointY = activeLevel.startY
        squashX = 1f
        squashY = 1f
        rollAngle = 0f
        cameraX = (activeLevel.startX - viewportWidthWorld / 2f).safeCoerceIn(0f, (activeLevel.width - viewportWidthWorld).coerceAtLeast(0f))
        cameraY = (activeLevel.startY - viewportHeightWorld * 0.42f).safeCoerceIn(0f, (activeLevel.height - viewportHeightWorld).coerceAtLeast(0f))
        invincibilityTimer = 0f
        cameraShakeTimer = 0f
        isGrounded = false
        wasGrounded = false
        coyoteTimer = 0f
        jumpBufferTimer = 0f
        isGameOver = false
        gameCompleted = false
        starsCollected = 0
        coinsCollected = 0
        currentScore = 0
        isHoldingLeft = false
        isHoldingRight = false
        joystickXAmount = 0f
        jumpRequested = false
        particles.clear()

        for (i in fallingPlatformStates.indices) {
            fallingPlatformStates[i].state = PlatformState.STABLE
            fallingPlatformStates[i].timer = 0f
            fallingPlatformStates[i].offsetY = 0f
            fallingPlatformStates[i].shakeX = 0f
            fallingPlatformStates[i].alpha = 1f
        }
        for (i in springCompressions.indices) {
            springCompressions[i] = 0f
        }
    }

    // High Performance 60 FPS Engine Loop with withFrameNanos
    LaunchedEffect(isPaused, isGameOver, gameCompleted) {
        var lastFrameNanos = System.nanoTime()
        var animationTime = 0f

        while (!isPaused && !isGameOver && !gameCompleted) {
            withFrameNanos { frameNanos ->
                val dtNanos = frameNanos - lastFrameNanos
                lastFrameNanos = frameNanos

                var dt = (dtNanos / 1_000_000_000f).coerceIn(0.005f, 0.033f)
                animationTime += dt
                elapsedTimeSeconds += dt

                if (isExitAnimating) {
                    exitAnimationTimer -= dt
                    ballX += (activeLevel.portalX - ballX) * 0.15f
                    ballY += (activeLevel.portalY - ballY) * 0.15f
                    squashX = (exitAnimationTimer / 1.3f).coerceIn(0f, 1f)
                    squashY = (exitAnimationTimer / 1.3f).coerceIn(0f, 1f)
                    if (exitAnimationTimer <= 0f) {
                        isExitAnimating = false
                        gameCompleted = true
                        BounceProgressionManager.recordLevelCompleted(
                            context = context,
                            prefs = prefs,
                            viewModel = viewModel,
                            levelNum = level.number,
                            stars = starsCollected,
                            coins = coinsCollected,
                            timeSeconds = elapsedTimeSeconds,
                            tookDamage = livesRemaining < 3
                        )
                    }

                    val particleIterator = particles.iterator()
                    while (particleIterator.hasNext()) {
                        val p = particleIterator.next()
                        p.x += p.vx * dt
                        p.y += p.vy * dt
                        p.alpha -= 1.8f * dt
                        p.size = (p.size - 1.2f * dt).coerceAtLeast(0.5f)
                        if (p.alpha <= 0f) particleIterator.remove()
                    }
                    return@withFrameNanos
                }

                // Camera Shake Decay
                if (cameraShakeTimer > 0f) {
                    cameraShakeTimer -= dt
                    val prog = (cameraShakeTimer / 0.35f).coerceIn(0f, 1f)
                    shakeDx = ((Math.random() - 0.5) * 2 * cameraShakeIntensity * prog).toFloat()
                    shakeDy = ((Math.random() - 0.5) * 2 * cameraShakeIntensity * prog).toFloat()
                } else {
                    shakeDx = 0f
                    shakeDy = 0f
                }

                // Invincibility i-frame Timer
                if (invincibilityTimer > 0f) {
                    invincibilityTimer -= dt
                }

                // Particles update
                val particleIterator = particles.iterator()
                while (particleIterator.hasNext()) {
                    val p = particleIterator.next()
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.alpha -= 1.8f * dt
                    p.size = (p.size - 1.2f * dt).coerceAtLeast(0.5f)
                    if (p.alpha <= 0f) particleIterator.remove()
                }

                // Spring compression relaxation
                for (i in springCompressions.indices) {
                    springCompressions[i] = (springCompressions[i] - 6f * dt).coerceAtLeast(0f)
                }

                // Falling platforms state update
                for (i in dynamicObstacles.indices) {
                    val obs = dynamicObstacles[i]
                    if (obs.isFallingPlatform) {
                        val fs = fallingPlatformStates[i]
                        when (fs.state) {
                            PlatformState.SHAKING -> {
                                fs.timer -= dt
                                fs.shakeX = (sin(fs.timer * 60f) * 4f)
                                if (fs.timer <= 0f) {
                                    fs.state = PlatformState.FALLING
                                    fs.timer = obs.respawnDelay
                                    fs.shakeX = 0f
                                }
                            }
                            PlatformState.FALLING -> {
                                fs.offsetY += 400f * dt
                                fs.alpha = (fs.alpha - 1.2f * dt).coerceAtLeast(0f)
                                fs.timer -= dt
                                if (fs.timer <= 0f) {
                                    fs.state = PlatformState.RESPAWNING
                                    fs.offsetY = 0f
                                    fs.timer = 0.5f
                                }
                            }
                            PlatformState.RESPAWNING -> {
                                fs.alpha = (fs.alpha + 2f * dt).coerceAtMost(1f)
                                if (fs.alpha >= 1f) {
                                    fs.state = PlatformState.STABLE
                                    fs.alpha = 1f
                                }
                            }
                            PlatformState.STABLE -> {}
                        }
                    }
                }

                // Door unlock sliding progress
                for (i in dynamicDoors.indices) {
                    val door = dynamicDoors[i]
                    if (door.isUnlocked && door.unlockAnimProgress < 1f) {
                        door.unlockAnimProgress = (door.unlockAnimProgress + 2.5f * dt).coerceAtMost(1f)
                    }
                }

                // Precision Sub-stepping (4 steps per frame to guarantee zero tunneling)
                val subSteps = 4
                val subDt = dt / subSteps

                for (step in 0 until subSteps) {
                    wasGrounded = isGrounded

                    // 1. Moving obstacles displacement calculation
                    for (i in dynamicObstacles.indices) {
                        val obs = dynamicObstacles[i]
                        if (obs.isMoving) {
                            val theta = (animationTime * obs.moveSpeed * 10f)
                            val newX = obs.initialX + cos(theta) * obs.moveRangeX
                            val newY = obs.initialY + sin(theta) * obs.moveRangeY
                            val dx = newX - obs.x
                            val dy = newY - obs.y

                            dynamicObstacles[i] = obs.copy(x = newX, y = newY)

                            // Carry ball if grounded on moving platform
                            if (isGrounded && checkCollision(ballX, ballY + 2f, ballRadius, obs)) {
                                ballX += dx
                                ballY += dy
                            }
                        }
                    }

                    // 2. Enemies Patrol & Collision Physics
                    for (enemy in dynamicEnemies) {
                        val ex: Float
                        val ey: Float
                        when (enemy.type) {
                            EnemyType.WALKING -> {
                                val cycle = sin(animationTime * (enemy.moveSpeed / 20f))
                                ex = enemy.initialX + cycle * enemy.moveRangeX
                                ey = enemy.initialY
                            }
                            EnemyType.FLYING -> {
                                val cycleX = cos(animationTime * (enemy.moveSpeed / 25f))
                                val cycleY = sin(animationTime * (enemy.moveSpeed / 15f))
                                ex = enemy.initialX + cycleX * enemy.moveRangeX
                                ey = enemy.initialY + cycleY * enemy.moveRangeY
                            }
                            EnemyType.ROTATING_HAZARD -> {
                                ex = enemy.x
                                ey = enemy.y
                            }
                        }

                        val enemyObs = BounceObstacle(ex - enemy.width / 2f, ey - enemy.height / 2f, enemy.width, enemy.height)
                        if (checkCollision(ballX, ballY, ballRadius, enemyObs)) {
                            handlePlayerDeath(isSpikeHit = false)
                            break
                        }
                    }

                    // 3. Water Zones Detection & Buoyancy Physics
                    var insideWater = false
                    for (water in activeLevel.waterZones) {
                        if (ballX >= water.x && ballX <= water.x + water.width &&
                            ballY >= water.y && ballY <= water.y + water.height) {
                            insideWater = true
                            break
                        }
                    }
                    isSubmergedInWater = insideWater

                    if (isSubmergedInWater) {
                        if (!lastWaterState) {
                            spawnWaterSplash(ballX, ballY)
                            BounceAudioEngine.playWaterSplash()
                            triggerHaptic(context, vibrationEnabled)
                        }
                        // Water Drag
                        ballVx *= (1f - 3f * subDt).coerceAtLeast(0.85f)
                        ballVy *= (1f - 4f * subDt).coerceAtLeast(0.8f)
                        // Upward Buoyancy
                        ballVy -= 480f * subDt

                        waterBubbleTimer += subDt
                        if (waterBubbleTimer >= 0.2f) {
                            spawnWaterBubbles(ballX, ballY + 5f)
                            waterBubbleTimer = 0f
                        }
                    } else if (lastWaterState) {
                        spawnWaterSplash(ballX, ballY)
                        BounceAudioEngine.playWaterSplash()
                    }
                    lastWaterState = isSubmergedInWater

                    // 4. Keys & Locked Doors Logic
                    for (i in dynamicKeys.indices) {
                        val key = dynamicKeys[i]
                        if (!key.isCollected) {
                            val distSq = (ballX - key.x) * (ballX - key.x) + (ballY - key.y) * (ballY - key.y)
                            if (distSq < (ballRadius + 16f) * (ballRadius + 16f)) {
                                key.isCollected = true
                                spawnKeyCollectParticles(key.x, key.y, Color(key.colorHex))
                                BounceAudioEngine.playDoorUnlock()
                                triggerHaptic(context, vibrationEnabled)

                                // Unlock matching door
                                for (d in dynamicDoors) {
                                    if (d.keyIdNeeded == key.id && !d.isUnlocked) {
                                        d.isUnlocked = true
                                        spawnDoorUnlockParticles(d.x + d.width / 2f, d.y + d.height / 2f, Color(d.keyColorHex))
                                        BounceAudioEngine.playDoorUnlock()
                                    }
                                }
                            }
                        }
                    }

                    // 5. Interactive Objects Updates
                    for (i in dynamicInteractiveBlocks.indices) {
                        val block = dynamicInteractiveBlocks[i]
                        if (block.isDestroyed) continue

                        when (block.type) {
                            InteractiveType.SECRET_PASSAGEWAY -> {
                                val isInside = ballX >= block.x && ballX <= block.x + block.width &&
                                        ballY >= block.y && ballY <= block.y + block.height
                                block.opacity = if (isInside) 0.25f else 1f
                            }
                            InteractiveType.PUSHABLE_BOX -> {
                                block.vy += 800f * subDt
                                block.currentY += block.vy * subDt
                                block.currentX += block.vx * subDt
                                block.vx *= (1f - 4f * subDt).coerceAtLeast(0.8f)

                                // Platform collision for pushable box
                                for (obs in dynamicObstacles) {
                                    if (!obs.isSpike) {
                                        if (block.currentX + block.width > obs.x && block.currentX < obs.x + obs.width &&
                                            block.currentY + block.height > obs.y && block.currentY < obs.y + obs.height) {
                                            if (block.vy > 0f) {
                                                block.currentY = obs.y - block.height
                                                block.vy = 0f
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }

                    // 6. Horizontal movement direction & acceleration/friction
                    var moveDir = joystickXAmount.coerceIn(-1f, 1f)
                    if (isHoldingLeft) moveDir -= 1f
                    if (isHoldingRight) moveDir += 1f
                    moveDir = moveDir.coerceIn(-1f, 1f)

                    if (moveDir != 0f) {
                        val accel = if (isGrounded) acceleration else acceleration * 0.75f
                        ballVx += moveDir * accel * subDt
                        ballVx = ballVx.coerceIn(-maxSpeed, maxSpeed)
                    } else if (isGrounded) {
                        if (ballVx > 0f) {
                            ballVx = (ballVx - friction * subDt).coerceAtLeast(0f)
                        } else if (ballVx < 0f) {
                            ballVx = (ballVx + friction * subDt).coerceAtMost(0f)
                        }
                    }

                    // 7. Jump buffering & Coyote time handling
                    if (isGrounded) {
                        coyoteTimer = coyoteTimeMax
                    } else {
                        coyoteTimer -= subDt
                    }

                    if (jumpRequested) {
                        jumpBufferTimer = jumpBufferMax
                        jumpRequested = false
                    } else {
                        jumpBufferTimer -= subDt
                    }

                    if (jumpBufferTimer > 0f && (coyoteTimer > 0f || isSubmergedInWater)) {
                        val actualJumpForce = if (isSubmergedInWater) jumpForce * 0.75f else jumpForce
                        ballVy = actualJumpForce
                        isGrounded = false
                        coyoteTimer = 0f
                        jumpBufferTimer = 0f

                        squashY = 1.25f
                        squashX = 0.78f
                        if (!isSubmergedInWater) spawnJumpDust(ballX, ballY + ballRadius)
                        BounceAudioEngine.playJump()
                    }

                    // Apply gravity
                    val effectiveGravity = if (isSubmergedInWater) gravity * 0.4f else gravity
                    ballVy += effectiveGravity * subDt

                    // 8. Resolve X Axis Collision
                    ballX += ballVx * subDt
                    ballX = ballX.coerceIn(ballRadius, activeLevel.width - ballRadius)
                    if (kotlin.math.abs(ballVx) > 20f) {
                        spawnSkinMotionTrail(ballX, ballY, ballVx)
                    }

                    // X Collision with Locked Doors & Interactive Blocks & Platforms
                    for (door in dynamicDoors) {
                        if (!door.isUnlocked || door.unlockAnimProgress < 0.8f) {
                            val doorObs = BounceObstacle(door.x, door.y, door.width, door.height)
                            if (checkCollision(ballX, ballY, ballRadius, doorObs)) {
                                if (ballVx > 0f) {
                                    ballX = door.x - ballRadius
                                    ballVx = 0f
                                } else if (ballVx < 0f) {
                                    ballX = door.x + door.width + ballRadius
                                    ballVx = 0f
                                }
                            }
                        }
                    }

                    for (block in dynamicInteractiveBlocks) {
                        if (block.isDestroyed || block.type == InteractiveType.SECRET_PASSAGEWAY) continue
                        val blockObs = BounceObstacle(block.currentX, block.currentY, block.width, block.height)
                        if (checkCollision(ballX, ballY, ballRadius, blockObs)) {
                            if (block.type == InteractiveType.PUSHABLE_BOX) {
                                block.vx += ballVx * 0.4f
                            }
                            if (ballVx > 0f) {
                                ballX = block.currentX - ballRadius
                                ballVx = 0f
                            } else if (ballVx < 0f) {
                                ballX = block.currentX + block.width + ballRadius
                                ballVx = 0f
                            }
                        }
                    }

                    for (i in dynamicObstacles.indices) {
                        val obs = dynamicObstacles[i]
                        val fs = fallingPlatformStates[i]
                        if (fs.state == PlatformState.FALLING || fs.state == PlatformState.RESPAWNING) continue

                        val actualX = obs.x + (if (obs.isFallingPlatform) fs.shakeX else 0f)
                        val actualY = obs.y + (if (obs.isFallingPlatform) fs.offsetY else 0f)
                        val effectiveObs = obs.copy(x = actualX, y = actualY)

                        if (!obs.isSpike && checkCollision(ballX, ballY, ballRadius, effectiveObs)) {
                            if (ballVx > 0f) {
                                ballX = actualX - ballRadius
                                ballVx = 0f
                            } else if (ballVx < 0f) {
                                ballX = actualX + obs.width + ballRadius
                                ballVx = 0f
                            }
                        }
                    }

                    // 9. Resolve Y Axis Collision & Hazards
                    ballY += ballVy * subDt
                    isGrounded = false

                    // Pit fall check
                    if (ballY - ballRadius > activeLevel.height) {
                        handlePlayerDeath(isSpikeHit = false)
                        missedJumps++
                        break
                    }

                    // Y Collision with Interactive Blocks
                    for (block in dynamicInteractiveBlocks) {
                        if (block.isDestroyed || block.type == InteractiveType.SECRET_PASSAGEWAY) continue
                        val blockObs = BounceObstacle(block.currentX, block.currentY, block.width, block.height)
                        if (checkCollision(ballX, ballY, ballRadius, blockObs)) {
                            if (block.type == InteractiveType.BREAKABLE) {
                                if (kotlin.math.abs(ballVy) > 120f || kotlin.math.abs(ballVx) > 120f) {
                                    block.isDestroyed = true
                                    spawnBlockBreakDebris(block.currentX + block.width / 2f, block.currentY + block.height / 2f)
                                    triggerHaptic(context, vibrationEnabled)
                                }
                            } else if (block.type == InteractiveType.BOUNCE_PAD) {
                                if (ballVy > 0f) {
                                    ballY = block.currentY - ballRadius
                                    ballVy = -680f
                                    squashY = 0.6f
                                    squashX = 1.35f
                                    spawnSpringBurst(ballX, block.currentY)
                                }
                            } else {
                                if (ballVy > 0f) {
                                    ballY = block.currentY - ballRadius
                                    ballVy = 0f
                                    isGrounded = true
                                } else if (ballVy < 0f) {
                                    ballY = block.currentY + block.height + ballRadius
                                    ballVy = 0f
                                }
                            }
                        }
                    }

                    for (i in dynamicObstacles.indices) {
                        val obs = dynamicObstacles[i]
                        val fs = fallingPlatformStates[i]
                        if (fs.state == PlatformState.FALLING || fs.state == PlatformState.RESPAWNING) continue

                        val actualX = obs.x + (if (obs.isFallingPlatform) fs.shakeX else 0f)
                        val actualY = obs.y + (if (obs.isFallingPlatform) fs.offsetY else 0f)
                        val effectiveObs = obs.copy(x = actualX, y = actualY)

                        if (checkCollision(ballX, ballY, ballRadius, effectiveObs)) {
                            if (obs.isSpike) {
                                handlePlayerDeath(isSpikeHit = true)
                                break
                            } else if (obs.isSpring) {
                                if (ballVy > 0f) {
                                    ballY = actualY - ballRadius
                                    ballVy = obs.springForce
                                    squashY = 0.6f
                                    squashX = 1.35f
                                    springCompressions[i] = 1f
                                    spawnSpringBurst(ballX, actualY)
                                    BounceAudioEngine.playJump()
                                }
                            } else {
                                if (ballVy > 0f && (ballY - ballVy * subDt) <= actualY + 4f) {
                                    ballY = actualY - ballRadius
                                    ballVy = 0f
                                    isGrounded = true

                                    // Trigger falling platform shake
                                    if (obs.isFallingPlatform && fs.state == PlatformState.STABLE) {
                                        fs.state = PlatformState.SHAKING
                                        fs.timer = obs.fallDelay
                                    }

                                    if (!wasGrounded) {
                                        squashX = 1.32f
                                        squashY = 0.7f
                                        spawnLandingDust(ballX, actualY)
                                        BounceAudioEngine.playLanding()
                                    }
                                } else if (ballVy < 0f) {
                                    ballY = actualY + obs.height + ballRadius
                                    ballVy = 0f
                                }
                            }
                        }
                    }

                    // 10. Checkpoint collision checking
                    for (cp in dynamicCheckpoints) {
                        if (!cp.isActivated) {
                            val distSq = (ballX - cp.x) * (ballX - cp.x) + (ballY - cp.y) * (ballY - cp.y)
                            if (distSq < (ballRadius + 20f) * (ballRadius + 20f)) {
                                cp.isActivated = true
                                lastCheckpointX = cp.x
                                lastCheckpointY = cp.y
                                spawnCheckpointBeam(cp.x, cp.y)
                                BounceAudioEngine.playCheckpoint()
                                triggerHaptic(context, vibrationEnabled)
                            }
                        }
                    }

                    // 11. Collectibles checking
                    for (i in dynamicCollectibles.indices) {
                        val item = dynamicCollectibles[i]
                        if (!item.isCollected) {
                            val distSq = (ballX - item.x) * (ballX - item.x) + (ballY - item.y) * (ballY - item.y)
                            if (distSq < (ballRadius + 14f) * (ballRadius + 14f)) {
                                dynamicCollectibles[i] = item.copy(isCollected = true)
                                if (item.isStar) {
                                    starsCollected++
                                    currentScore += 50
                                    spawnStarSparkles(item.x, item.y)
                                    BounceAudioEngine.playStar()
                                } else {
                                    coinsCollected++
                                    currentScore += 10
                                    spawnCoinSparkles(item.x, item.y)
                                    BounceAudioEngine.playCoin()
                                }
                            }
                        }
                    }

                    // 12. Portal completion
                    val pDistSq = (ballX - activeLevel.portalX) * (ballX - activeLevel.portalX) + (ballY - activeLevel.portalY) * (ballY - activeLevel.portalY)
                    if (pDistSq < (ballRadius + 24f) * (ballRadius + 24f)) {
                        if (!gameCompleted && !isExitAnimating) {
                            isExitAnimating = true
                            exitAnimationTimer = 1.3f
                            BounceAudioEngine.playVictory()
                            spawnVictoryConfetti(activeLevel.portalX, activeLevel.portalY)
                        }
                    }
                }

                // Restoration of squash/stretch factors
                squashX += (1f - squashX) * (1f - exp(-15f * dt))
                squashY += (1f - squashY) * (1f - exp(-15f * dt))

                // Roll rotation
                rollAngle += (ballVx * dt / ballRadius) * (180f / PI.toFloat())

                // Smooth Camera Follow: keeps player centered ~42% from top
                val lookAhead = ballVx * 0.25f
                val targetCamX = (ballX - viewportWidthWorld / 2f + lookAhead).safeCoerceIn(0f, (activeLevel.width - viewportWidthWorld).coerceAtLeast(0f))
                cameraX += (targetCamX - cameraX) * (1f - exp(-11f * dt))

                val targetCamY = (ballY - viewportHeightWorld * 0.42f).safeCoerceIn(0f, (activeLevel.height - viewportHeightWorld).coerceAtLeast(0f))
                cameraY += (targetCamY - cameraY) * (1f - exp(-11f * dt))
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF131024), Color(0xFF090615))
                )
            )
    ) {
        val screenWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val screenHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

        // Standard fixed viewport height in world coordinates (500 units)
        // Maintains exact platform scaling, consistent jump perception, and zero dynamic zoom changes
        val targetWorldHeight = 500f
        val worldScale = if (screenHeightPx > 0f) screenHeightPx / targetWorldHeight else 1.5f

        val viewWidthWorld = if (worldScale > 0f) screenWidthPx / worldScale else 800f
        val viewHeightWorld = targetWorldHeight

        viewportWidthWorld = viewWidthWorld
        viewportHeightWorld = viewHeightWorld

        // Drawing Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            withTransform({
                scale(worldScale, worldScale, pivot = Offset.Zero)
                translate(-cameraX, -cameraY)
            }) {
                val leftBound = cameraX - 120f
                val rightBound = cameraX + viewportWidthWorld + 120f
                val topBound = cameraY - 120f
                val bottomBound = cameraY + viewportHeightWorld + 120f

                // Theme-Based Visual Background Atmosphere
                val themeAccent = getLevelAccentColor(level.number)
                var ambientX = 200f
                while (ambientX < activeLevel.width) {
                    drawCircle(
                        color = themeAccent.copy(alpha = 0.05f),
                        radius = 350f,
                        center = Offset(ambientX, activeLevel.height * 0.4f)
                    )
                    ambientX += 800f
                }

                when ((level.number - 1) % 10 + 1) {
                    1 -> { // Forest: Sun glow & rolling green hill silhouettes
                        drawCircle(color = Color(0xFFFFD700).copy(alpha = 0.08f), radius = 180f, center = Offset(500f, 100f))
                        var hx = 0f
                        while (hx < activeLevel.width) {
                            val hillPath = Path().apply {
                                moveTo(hx, activeLevel.height)
                                quadraticTo(hx + 300f, activeLevel.height - 180f, hx + 600f, activeLevel.height)
                                close()
                            }
                            drawPath(hillPath, color = Color(0xFF15803D).copy(alpha = 0.12f))
                            hx += 550f
                        }
                    }
                    2 -> { // Ice: Glacial peaks & falling snowflakes
                        var mx = 0f
                        while (mx < activeLevel.width) {
                            val mPath = Path().apply {
                                moveTo(mx, activeLevel.height)
                                lineTo(mx + 350f, activeLevel.height - 280f)
                                lineTo(mx + 700f, activeLevel.height)
                                close()
                            }
                            drawPath(mPath, color = Color(0xFF38BDF8).copy(alpha = 0.08f))
                            mx += 600f
                        }
                        var sx = 80f
                        while (sx < activeLevel.width) {
                            val sy = (elapsedTimeSeconds * 60f + sx * 7f) % activeLevel.height
                            drawCircle(color = Color.White.copy(alpha = 0.35f), radius = 3.5f, center = Offset(sx + sin(sy / 20f) * 10f, sy))
                            sx += 180f
                        }
                    }
                    3 -> { // Lava: Magma glow at bottom & rising embers
                        drawRect(color = Color(0xFFFF3D00).copy(alpha = 0.15f), topLeft = Offset(0f, activeLevel.height - 120f), size = Size(activeLevel.width, 120f))
                        var ex = 100f
                        while (ex < activeLevel.width) {
                            val ey = activeLevel.height - ((elapsedTimeSeconds * 90f + ex * 5f) % (activeLevel.height - 100f))
                            drawCircle(color = Color(0xFFFFD700).copy(alpha = 0.3f), radius = 4f, center = Offset(ex + sin(ey / 30f) * 12f, ey))
                            ex += 200f
                        }
                    }
                    4 -> { // Factory: Industrial gear silhouettes & cogwheels
                        var gx = 300f
                        while (gx < activeLevel.width) {
                            val rot = elapsedTimeSeconds * 30f
                            withTransform({ rotate(rot, Offset(gx, 200f)) }) {
                                drawCircle(color = Color(0xFFF59E0B).copy(alpha = 0.08f), radius = 110f, center = Offset(gx, 200f), style = Stroke(width = 16f))
                            }
                            gx += 800f
                        }
                    }
                    5 -> { // Castle: Gothic spires & stone pillars
                        var sx = 200f
                        while (sx < activeLevel.width) {
                            drawRect(color = Color(0xFFA855F7).copy(alpha = 0.08f), topLeft = Offset(sx, 120f), size = Size(80f, activeLevel.height - 120f))
                            val roofPath = Path().apply {
                                moveTo(sx, 120f)
                                lineTo(sx + 40f, 30f)
                                lineTo(sx + 80f, 120f)
                                close()
                            }
                            drawPath(roofPath, color = Color(0xFFA855F7).copy(alpha = 0.14f))
                            sx += 750f
                        }
                    }
                    6 -> { // Underwater: Translucent bubbles rising
                        var bx = 150f
                        while (bx < activeLevel.width) {
                            val by = (activeLevel.height - (elapsedTimeSeconds * 70f + bx * 3f) % activeLevel.height)
                            drawCircle(color = Color(0xFF80DEEA).copy(alpha = 0.18f), radius = 12f, center = Offset(bx, by), style = Stroke(width = 2.5f))
                            bx += 250f
                        }
                    }
                    7 -> { // Sky: Floating cloud silhouettes
                        var cx = 100f
                        while (cx < activeLevel.width) {
                            val cy = 180f + sin(cx / 200f) * 80f
                            drawCircle(color = Color.White.copy(alpha = 0.08f), radius = 90f, center = Offset(cx, cy))
                            drawCircle(color = Color.White.copy(alpha = 0.08f), radius = 120f, center = Offset(cx + 60f, cy - 20f))
                            drawCircle(color = Color.White.copy(alpha = 0.08f), radius = 80f, center = Offset(cx + 120f, cy))
                            cx += 700f
                        }
                    }
                    8 -> { // Jungle: Hanging vines & canopy glow
                        var vx = 120f
                        while (vx < activeLevel.width) {
                            drawLine(color = Color(0xFF10B981).copy(alpha = 0.22f), start = Offset(vx, 0f), end = Offset(vx, 200f + sin(vx) * 60f), strokeWidth = 5f)
                            vx += 250f
                        }
                    }
                    9 -> { // Desert: Sunken pyramids & golden sandstorm
                        var px = 150f
                        while (px < activeLevel.width) {
                            val pyrPath = Path().apply {
                                moveTo(px, activeLevel.height)
                                lineTo(px + 200f, activeLevel.height - 220f)
                                lineTo(px + 400f, activeLevel.height)
                                close()
                            }
                            drawPath(pyrPath, color = Color(0xFFFFD700).copy(alpha = 0.07f))
                            px += 650f
                        }
                    }
                    10 -> { // Crystal Cave: Amethyst crystal sparkles
                        var cx = 150f
                        while (cx < activeLevel.width) {
                            val cy = 180f + (cx * 3f) % 300f
                            drawStar(cx, cy, 4, 16f, 7f, Color(0xFFE081FF).copy(alpha = 0.28f))
                            cx += 350f
                        }
                    }
                }

                // 1. Draw Water Zones
                for (water in activeLevel.waterZones) {
                    if (water.x + water.width < leftBound || water.x > rightBound) continue
                    drawRect(
                        color = water.waterColor,
                        topLeft = Offset(water.x, water.y),
                        size = Size(water.width, water.height)
                    )
                    // Water surface wave highlight line
                    val wavePath = Path().apply {
                        moveTo(water.x, water.y)
                        var wx = water.x
                        val waveStep = 20f
                        while (wx <= water.x + water.width) {
                            val wy = water.y + sin((wx + elapsedTimeSeconds * 100f) / 15f) * 3f
                            lineTo(wx, wy)
                            wx += waveStep
                        }
                    }
                    drawPath(wavePath, color = Color(0xFF80DEEA), style = Stroke(width = 2.5f))
                }

                // 2. Draw Checkpoint Flags
                for (cp in dynamicCheckpoints) {
                    if (cp.x + 40f < leftBound || cp.x - 40f > rightBound) continue
                    val flagX = cp.x
                    val flagY = cp.y
                    // Pole
                    drawLine(
                        color = Color(0xFF8E8A9F),
                        start = Offset(flagX, flagY),
                        end = Offset(flagX, flagY - 45f),
                        strokeWidth = 3f
                    )
                    // Pole top ball
                    drawCircle(
                        color = if (cp.isActivated) Color(0xFF00FF88) else Color(0xFFA855F7),
                        radius = 4f,
                        center = Offset(flagX, flagY - 47f)
                    )
                    // Flag banner
                    val bannerPath = Path().apply {
                        moveTo(flagX, flagY - 45f)
                        lineTo(flagX + 28f, flagY - 32f)
                        lineTo(flagX, flagY - 20f)
                        close()
                    }
                    if (cp.isActivated) {
                        drawPath(bannerPath, color = Color(0xFF00FF88))
                        drawStar(flagX + 10f, flagY - 32f, 5, 4f, 2f, Color.White)
                    } else {
                        drawPath(bannerPath, color = Color(0xFF7C3AED).copy(alpha = 0.6f))
                    }
                    // Base ring
                    drawCircle(
                        color = if (cp.isActivated) Color(0xFF00FF88).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f),
                        radius = 10f,
                        center = Offset(flagX, flagY)
                    )
                }

                // 3. Draw Doors & Keys
                for (key in dynamicKeys) {
                    if (key.x + 30f < leftBound || key.x - 30f > rightBound) continue
                    if (!key.isCollected) {
                        val rot = elapsedTimeSeconds * 90f
                        withTransform({
                            rotate(rot, pivot = Offset(key.x, key.y))
                        }) {
                            drawCircle(
                                color = Color(key.colorHex),
                                radius = 10f,
                                center = Offset(key.x, key.y),
                                style = Stroke(width = 3.5f)
                            )
                            drawLine(
                                color = Color(key.colorHex),
                                start = Offset(key.x + 8f, key.y),
                                end = Offset(key.x + 18f, key.y),
                                strokeWidth = 3.5f
                            )
                            drawLine(
                                color = Color(key.colorHex),
                                start = Offset(key.x + 14f, key.y),
                                end = Offset(key.x + 14f, key.y + 6f),
                                strokeWidth = 3f
                            )
                        }
                    }
                }

                for (door in dynamicDoors) {
                    if (door.x + door.width < leftBound || door.x > rightBound) continue
                    val offsetY = door.unlockAnimProgress * door.height
                    if (door.unlockAnimProgress < 0.95f) {
                        drawRoundRect(
                            color = Color(0xFF1F1B2E),
                            topLeft = Offset(door.x, door.y - offsetY),
                            size = Size(door.width, door.height),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color(door.keyColorHex).copy(alpha = 1f - door.unlockAnimProgress),
                            topLeft = Offset(door.x + 2f, door.y - offsetY + 2f),
                            size = Size(door.width - 4f, door.height - 4f),
                            cornerRadius = CornerRadius(4f, 4f),
                            style = Stroke(width = 2f)
                        )
                        // Keyhole plate
                        drawCircle(
                            color = Color(door.keyColorHex),
                            radius = 6f,
                            center = Offset(door.x + door.width / 2f, door.y - offsetY + door.height / 2f)
                        )
                    }
                }

                // 4. Draw Interactive Objects
                for (block in dynamicInteractiveBlocks) {
                    if (block.isDestroyed) continue
                    if (block.currentX + block.width < leftBound || block.currentX > rightBound) continue
                    when (block.type) {
                        InteractiveType.SECRET_PASSAGEWAY -> {
                            drawRect(
                                color = Color(0xFF1B1635).copy(alpha = block.opacity),
                                topLeft = Offset(block.currentX, block.currentY),
                                size = Size(block.width, block.height)
                            )
                        }
                        InteractiveType.BREAKABLE -> {
                            drawRoundRect(
                                color = Color(0xFFB45309),
                                topLeft = Offset(block.currentX, block.currentY),
                                size = Size(block.width, block.height),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            // Brick line details
                            drawLine(Color(0xFF78350F), Offset(block.currentX, block.currentY + 20f), Offset(block.currentX + block.width, block.currentY + 20f), strokeWidth = 2f)
                            drawLine(Color(0xFF78350F), Offset(block.currentX + 20f, block.currentY), Offset(block.currentX + 20f, block.currentY + 20f), strokeWidth = 2f)
                            drawLine(Color(0xFF78350F), Offset(block.currentX + 10f, block.currentY + 20f), Offset(block.currentX + 10f, block.currentY + 40f), strokeWidth = 2f)
                        }
                        InteractiveType.PUSHABLE_BOX -> {
                            drawRoundRect(
                                color = Color(0xFF9A3412),
                                topLeft = Offset(block.currentX, block.currentY),
                                size = Size(block.width, block.height),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            drawLine(Color(0xFFEA580C), Offset(block.currentX + 4f, block.currentY + 4f), Offset(block.currentX + block.width - 4f, block.currentY + block.height - 4f), strokeWidth = 2.5f)
                            drawLine(Color(0xFFEA580C), Offset(block.currentX + block.width - 4f, block.currentY + 4f), Offset(block.currentX + 4f, block.currentY + block.height - 4f), strokeWidth = 2.5f)
                        }
                        InteractiveType.BOUNCE_PAD -> {
                            drawRoundRect(
                                color = Color(0xFF1E293B),
                                topLeft = Offset(block.currentX, block.currentY + 20f),
                                size = Size(block.width, 20f),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                            drawRoundRect(
                                color = Color(0xFF10B981),
                                topLeft = Offset(block.currentX, block.currentY),
                                size = Size(block.width, 10f),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }
                }

                // 5. Draw Platforms & Obstacles
                for (i in dynamicObstacles.indices) {
                    val obs = dynamicObstacles[i]
                    val fs = fallingPlatformStates[i]
                    if (fs.state == PlatformState.FALLING && fs.alpha <= 0.05f) continue

                    val drawX = obs.x + (if (obs.isFallingPlatform) fs.shakeX else 0f)
                    val drawY = obs.y + (if (obs.isFallingPlatform) fs.offsetY else 0f)

                    if (drawX + obs.width < leftBound || drawX > rightBound) continue

                    if (obs.isSpike) {
                        // Hazard Spikes
                        val spikeColor = Color(0xFFFF3D00)
                        val coreColor = Color(0xFFFF9100)
                        val path = Path()
                        val pathCore = Path()

                        when (obs.spikeDirection) {
                            SpikeDirection.UP -> {
                                val spikeCount = (obs.width / 20f).toInt().coerceAtLeast(1)
                                val spikeW = obs.width / spikeCount
                                for (s in 0 until spikeCount) {
                                    val sx = drawX + s * spikeW
                                    path.moveTo(sx, drawY + obs.height)
                                    path.lineTo(sx + spikeW / 2f, drawY)
                                    path.lineTo(sx + spikeW, drawY + obs.height)

                                    pathCore.moveTo(sx + spikeW * 0.25f, drawY + obs.height)
                                    pathCore.lineTo(sx + spikeW / 2f, drawY + obs.height * 0.35f)
                                    pathCore.lineTo(sx + spikeW * 0.75f, drawY + obs.height)
                                }
                            }
                            SpikeDirection.DOWN -> {
                                val spikeCount = (obs.width / 20f).toInt().coerceAtLeast(1)
                                val spikeW = obs.width / spikeCount
                                for (s in 0 until spikeCount) {
                                    val sx = drawX + s * spikeW
                                    path.moveTo(sx, drawY)
                                    path.lineTo(sx + spikeW / 2f, drawY + obs.height)
                                    path.lineTo(sx + spikeW, drawY)

                                    pathCore.moveTo(sx + spikeW * 0.25f, drawY)
                                    pathCore.lineTo(sx + spikeW / 2f, drawY + obs.height * 0.65f)
                                    pathCore.lineTo(sx + spikeW * 0.75f, drawY)
                                }
                            }
                            SpikeDirection.LEFT -> {
                                path.moveTo(drawX + obs.width, drawY)
                                path.lineTo(drawX, drawY + obs.height / 2f)
                                path.lineTo(drawX + obs.width, drawY + obs.height)
                                pathCore.moveTo(drawX + obs.width, drawY + obs.height * 0.2f)
                                pathCore.lineTo(drawX + obs.width * 0.3f, drawY + obs.height / 2f)
                                pathCore.lineTo(drawX + obs.width, drawY + obs.height * 0.8f)
                            }
                            SpikeDirection.RIGHT -> {
                                path.moveTo(drawX, drawY)
                                path.lineTo(drawX + obs.width, drawY + obs.height / 2f)
                                path.lineTo(drawX, drawY + obs.height)
                                pathCore.moveTo(drawX, drawY + obs.height * 0.2f)
                                pathCore.lineTo(drawX + obs.width * 0.7f, drawY + obs.height / 2f)
                                pathCore.lineTo(drawX, drawY + obs.height * 0.8f)
                            }
                        }
                        drawPath(path, color = spikeColor.copy(alpha = fs.alpha))
                        drawPath(pathCore, color = coreColor.copy(alpha = fs.alpha))
                    } else if (obs.isSpring) {
                        // Spring Block
                        val comp = springCompressions[i]
                        val plateY = drawY + comp * 12f

                        // Base plate
                        drawRoundRect(
                            color = Color(0xFF2A2342),
                            topLeft = Offset(drawX, drawY + 18f),
                            size = Size(obs.width, obs.height - 18f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        // Coiled spring lines
                        val coilColor = Color(0xFFFFD700)
                        val midY = (plateY + drawY + 18f) / 2f
                        drawLine(coilColor, Offset(drawX + 12f, plateY + 6f), Offset(drawX + obs.width - 12f, midY), strokeWidth = 3f)
                        drawLine(coilColor, Offset(drawX + obs.width - 12f, midY), Offset(drawX + 12f, drawY + 18f), strokeWidth = 3f)

                        // Top launcher pad
                        drawRoundRect(
                            color = Color(0xFF76FF03),
                            topLeft = Offset(drawX, plateY),
                            size = Size(obs.width, 8f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    } else if (obs.isFallingPlatform) {
                        // Falling / Crumbling Platform
                        drawRoundRect(
                            color = Color(0xFF2C2440).copy(alpha = fs.alpha),
                            topLeft = Offset(drawX, drawY),
                            size = Size(obs.width, obs.height),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        drawRect(
                            color = Color(0xFFFF9100).copy(alpha = fs.alpha * 0.8f),
                            topLeft = Offset(drawX, drawY),
                            size = Size(obs.width, 4f)
                        )
                        // Stress crack line
                        drawLine(
                            color = Color(0xFFFF3D00).copy(alpha = fs.alpha),
                            start = Offset(drawX + obs.width * 0.3f, drawY + 4f),
                            end = Offset(drawX + obs.width * 0.7f, drawY + obs.height - 4f),
                            strokeWidth = 2f
                        )
                    } else {
                        // Standard / Moving platform
                        drawRoundRect(
                            color = Color(0xFF1B1635),
                            topLeft = Offset(drawX, drawY),
                            size = Size(obs.width, obs.height),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawRect(
                            color = themeAccent.copy(alpha = 0.8f),
                            topLeft = Offset(drawX, drawY),
                            size = Size(obs.width, 5f)
                        )
                    }
                }

                // 6. Draw Enemies & Rotating Hazards
                for (enemy in dynamicEnemies) {
                    val exApprox = if (enemy.type == EnemyType.ROTATING_HAZARD) enemy.x else enemy.initialX
                    val rangeX = if (enemy.type == EnemyType.ROTATING_HAZARD) 20f else enemy.moveRangeX + 30f
                    if (exApprox + rangeX < leftBound || exApprox - rangeX > rightBound) continue

                    val ex: Float
                    val ey: Float
                    when (enemy.type) {
                        EnemyType.WALKING -> {
                            val cycle = sin(elapsedTimeSeconds * (enemy.moveSpeed / 20f))
                            ex = enemy.initialX + cycle * enemy.moveRangeX
                            ey = enemy.initialY

                            // Crawler body
                            drawCircle(color = Color(0xFFDC2626), radius = 12f, center = Offset(ex, ey))
                            drawCircle(color = Color(0xFF991B1B), radius = 8f, center = Offset(ex, ey))
                            // Red eye
                            drawCircle(color = Color.White, radius = 4f, center = Offset(ex + cycle * 3f, ey - 2f))
                            drawCircle(color = Color.Red, radius = 2f, center = Offset(ex + cycle * 4f, ey - 2f))
                        }
                        EnemyType.FLYING -> {
                            val cycleX = cos(elapsedTimeSeconds * (enemy.moveSpeed / 25f))
                            val cycleY = sin(elapsedTimeSeconds * (enemy.moveSpeed / 15f))
                            ex = enemy.initialX + cycleX * enemy.moveRangeX
                            ey = enemy.initialY + cycleY * enemy.moveRangeY

                            // Flying gargoyle wing animation
                            val wingAngle = sin(elapsedTimeSeconds * 12f) * 20f
                            withTransform({
                                rotate(wingAngle, pivot = Offset(ex - 10f, ey))
                            }) {
                                drawCircle(color = Color(0xFF7C3AED), radius = 8f, center = Offset(ex - 14f, ey - 4f))
                            }
                            withTransform({
                                rotate(-wingAngle, pivot = Offset(ex + 10f, ey))
                            }) {
                                drawCircle(color = Color(0xFF7C3AED), radius = 8f, center = Offset(ex + 14f, ey - 4f))
                            }
                            drawCircle(color = Color(0xFF4C1D95), radius = 11f, center = Offset(ex, ey))
                            drawCircle(color = Color(0xFF00E5FF), radius = 4f, center = Offset(ex, ey))
                        }
                        EnemyType.ROTATING_HAZARD -> {
                            ex = enemy.x
                            ey = enemy.y
                            val rotAngle = elapsedTimeSeconds * enemy.moveSpeed
                            withTransform({
                                rotate(rotAngle, pivot = Offset(ex, ey))
                            }) {
                                drawCircle(color = Color(0xFF475569), radius = 14f, center = Offset(ex, ey))
                                // 8 Saw teeth
                                for (t in 0 until 8) {
                                    val ta = t * (Math.PI / 4.0)
                                    val tx1 = ex + cos(ta).toFloat() * 14f
                                    val ty1 = ey + sin(ta).toFloat() * 14f
                                    val tx2 = ex + cos(ta + 0.2).toFloat() * 22f
                                    val ty2 = ey + sin(ta + 0.2).toFloat() * 22f
                                    drawLine(Color(0xFFEF4444), Offset(tx1, ty1), Offset(tx2, ty2), strokeWidth = 3f)
                                }
                                drawCircle(color = Color(0xFFF87171), radius = 6f, center = Offset(ex, ey))
                            }
                        }
                    }
                }

                // 7. Draw Collectibles
                for (item in dynamicCollectibles) {
                    if (item.x + 20f < leftBound || item.x - 20f > rightBound) continue
                    if (!item.isCollected) {
                        if (item.isStar) {
                            drawStar(
                                cx = item.x,
                                cy = item.y,
                                spikes = 5,
                                outerRadius = 12f,
                                innerRadius = 5f,
                                color = Color(0xFFFFD700)
                            )
                        } else {
                            drawCircle(
                                color = Color(0xFF00E5FF),
                                radius = 10f,
                                center = Offset(item.x, item.y)
                            )
                            drawCircle(
                                color = Color(0xFF090615),
                                radius = 6f,
                                center = Offset(item.x, item.y)
                            )
                            drawCircle(
                                color = Color(0xFF00E5FF),
                                radius = 3f,
                                center = Offset(item.x, item.y)
                            )
                        }
                    }
                }

                // 8. Draw Finish Portal & Flag

                // A. Soft Neon Glow around the Flag Destination area
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(themeAccent.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(activeLevel.portalX, activeLevel.portalY - 30f),
                        radius = 80f
                    ),
                    radius = 80f,
                    center = Offset(activeLevel.portalX, activeLevel.portalY - 30f)
                )

                // B. Keep the existing rotating portal base below the flag
                drawCircle(
                    color = Color(0xFFFF00D6).copy(alpha = 0.15f),
                    radius = 32f,
                    center = Offset(activeLevel.portalX, activeLevel.portalY)
                )
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color(0xFF00E5FF), Color(0xFFFF00D6), Color(0xFF00E5FF))
                    ),
                    radius = 24f,
                    center = Offset(activeLevel.portalX, activeLevel.portalY),
                    style = Stroke(width = 4f)
                )
                withTransform({
                    rotate(portalRotation, pivot = Offset(activeLevel.portalX, activeLevel.portalY))
                }) {
                    drawCircle(
                        color = Color(0xFFFF00D6),
                        radius = 10f,
                        center = Offset(activeLevel.portalX, activeLevel.portalY)
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(activeLevel.portalX - 22f, activeLevel.portalY),
                        end = Offset(activeLevel.portalX + 22f, activeLevel.portalY),
                        strokeWidth = 3f
                    )
                }

                // C. Draw Vertical Flagpole above the portal center
                val poleHeight = 65f
                val poleStartX = activeLevel.portalX
                val poleStartY = activeLevel.portalY
                val poleEndY = activeLevel.portalY - poleHeight

                // Draw flagpole drop shadow
                drawLine(
                    color = Color.Black.copy(alpha = 0.25f),
                    start = Offset(poleStartX + 3f, poleStartY),
                    end = Offset(poleStartX + 3f, poleEndY),
                    strokeWidth = 4f
                )

                // Draw flagpole metallic body
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF888888), Color(0xFFDDDDDD), Color(0xFF555555))
                    ),
                    start = Offset(poleStartX, poleStartY),
                    end = Offset(poleStartX, poleEndY),
                    strokeWidth = 3.5f
                )

                // Draw Gold Top Orb of the flagpole
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = 4.5f,
                    center = Offset(poleStartX, poleEndY)
                )

                // D. Draw Beautiful Waving Flag
                val flagTime = System.currentTimeMillis() / 1000f
                val waveOffset = (kotlin.math.sin(flagTime * 8f) * 4.5f)
                val waveOffset2 = (kotlin.math.cos(flagTime * 8f) * 3f)

                val flagPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(poleStartX, poleEndY + 5f)
                    // Top edge waving to the right
                    lineTo(poleStartX + 18f, poleEndY + 5f + waveOffset)
                    lineTo(poleStartX + 36f, poleEndY + 8f + waveOffset2)
                    lineTo(poleStartX + 48f, poleEndY + 12f + waveOffset)
                    // Right vertical edge
                    lineTo(poleStartX + 44f, poleEndY + 32f + waveOffset2)
                    // Bottom edge waving back to the pole
                    lineTo(poleStartX + 32f, poleEndY + 28f + waveOffset)
                    lineTo(poleStartX + 16f, poleEndY + 26f + waveOffset2)
                    lineTo(poleStartX, poleEndY + 24f)
                    close()
                }

                // Bright orange/red/yellow theme-matching waving gradient brush
                val flagBrush = Brush.horizontalGradient(
                    colors = listOf(themeAccent, Color(0xFFFF3D00), Color(0xFFFFD700)),
                    startX = poleStartX,
                    endX = poleStartX + 48f
                )

                // Draw flag path with glowing style
                drawPath(
                    path = flagPath,
                    brush = flagBrush
                )

                // Add nice inner details/borders to flag path for extra depth/premium style
                drawPath(
                    path = flagPath,
                    color = Color.White.copy(alpha = 0.28f),
                    style = Stroke(width = 1.5f)
                )

                // E. Ambient floating tiny sparkles around the flagpole/destination
                for (i in 0 until 5) {
                    val particleTime = flagTime + i * 1.6f
                    val progress = (particleTime % 2.5f) / 2.5f // 0.0 to 1.0
                    val pX = poleStartX + kotlin.math.sin(i * 157.32f) * 35f
                    val pY = poleStartY - 10f - progress * 55f
                    val pAlpha = (1f - progress) * 0.85f
                    val pSize = 2.2f * (1f - progress * 0.4f)
                    val pColor = if (i % 2 == 0) Color(0xFFFFEA00) else themeAccent
                    drawCircle(
                        color = pColor.copy(alpha = pAlpha),
                        radius = pSize,
                        center = Offset(pX, pY)
                    )
                }

                // 9. Draw Particles
                for (p in particles) {
                    if (p.isSparkle) {
                        drawStar(p.x, p.y, 4, p.size, p.size * 0.4f, p.color.copy(alpha = p.alpha))
                    } else {
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha),
                            radius = p.size,
                            center = Offset(p.x, p.y)
                        )
                    }
                }

                // 10. Draw Ball with Squash & Stretch + Roll Rotation + Invincibility Flashing
                val ballAlpha = if (invincibilityTimer > 0f && (invincibilityTimer * 10f).toInt() % 2 == 0) 0.35f else 1f
                withTransform({
                    scale(squashX, squashY, pivot = Offset(ballX, ballY))
                    rotate(rollAngle, pivot = Offset(ballX, ballY))
                }) {
                    // Outer Skin Glow/Aura
                    drawCircle(
                        color = currentSkin.trailColor.copy(alpha = 0.35f * ballAlpha),
                        radius = ballRadius + 3.5f,
                        center = Offset(ballX, ballY)
                    )

                    // Equipped Ball Skin Radial Gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                currentSkin.primaryColor.copy(alpha = ballAlpha),
                                currentSkin.secondaryColor.copy(alpha = ballAlpha),
                                currentSkin.darkColor.copy(alpha = ballAlpha)
                            ),
                            center = Offset(ballX - 3f, ballY - 3f),
                            radius = ballRadius
                        ),
                        radius = ballRadius,
                        center = Offset(ballX, ballY)
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f * ballAlpha),
                        radius = 3f,
                        center = Offset(ballX - 5f, ballY - 5f)
                    )

                    val headingOffset = if (ballVx > 0) 3f else if (ballVx < 0) -3f else 0f
                    drawCircle(
                        color = Color.White.copy(alpha = ballAlpha),
                        radius = 2.8f,
                        center = Offset(ballX - 4f + headingOffset, ballY - 2f)
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = ballAlpha),
                        radius = 1.2f,
                        center = Offset(ballX - 4.2f + headingOffset * 1.2f, ballY - 2f)
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = ballAlpha),
                        radius = 2.8f,
                        center = Offset(ballX + 2f + headingOffset, ballY - 2f)
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = ballAlpha),
                        radius = 1.2f,
                        center = Offset(ballX + 1.8f + headingOffset * 1.2f, ballY - 2f)
                    )
                }

                // --- VISUAL DEBUG OVERLAY FOR EXIT SYSTEM ---
                if (isDebugExitOverlayEnabled) {
                    val exitPlat = dynamicObstacles.firstOrNull { it.isExitPlatform }
                    if (exitPlat != null) {
                        // 1. Highlight Exit Platform (dashed neon cyan frame)
                        drawRoundRect(
                            color = Color(0xFF00FFCC),
                            topLeft = Offset(exitPlat.x, exitPlat.y),
                            size = Size(exitPlat.width, exitPlat.height),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = 4f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f))
                        )
                        drawRect(
                            color = Color(0xFF00FFCC).copy(alpha = 0.15f),
                            topLeft = Offset(exitPlat.x, exitPlat.y),
                            size = Size(exitPlat.width, exitPlat.height)
                        )
                        
                        // 2. Draw Exit Position (Neon Magenta Target Crosshair at exitPlatform.topCenter)
                        val topCenterPos = exitPlat.topCenter
                        drawCircle(
                            color = Color(0xFFFF00D6),
                            radius = 6f,
                            center = topCenterPos
                        )
                        drawLine(
                            color = Color(0xFFFF00D6),
                            start = Offset(topCenterPos.x - 20f, topCenterPos.y),
                            end = Offset(topCenterPos.x + 20f, topCenterPos.y),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0xFFFF00D6),
                            start = Offset(topCenterPos.x, topCenterPos.y - 20f),
                            end = Offset(topCenterPos.x, topCenterPos.y + 20f),
                            strokeWidth = 3f
                        )

                        // 3. Draw Exit Collider Boundary Circle (Neon Magenta, matching portal collision radius)
                        val colliderRadius = 24f
                        drawCircle(
                            color = Color(0xFFFF00D6).copy(alpha = 0.22f),
                            radius = colliderRadius,
                            center = Offset(activeLevel.portalX, activeLevel.portalY)
                        )
                        drawCircle(
                            color = Color(0xFFFF00D6),
                            radius = colliderRadius,
                            center = Offset(activeLevel.portalX, activeLevel.portalY),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
            }
        }

        // Floating Top HUD Bar Overlay with Integrated Banner and Safe Areas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Side: Back Button + Compact Level Title Card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Color(0xFF0D0A1B).copy(alpha = 0.85f), RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = onBackToMenu,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Exit to Menu", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = "Level ${level.number}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Center: Real Adaptive AdMob Banner
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(max = 50.dp),
                contentAlignment = Alignment.Center
            ) {
                BannerManager.BannerAd()
            }

            // Right Side: HUD Counters & Pause Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Hearts / Lives counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF0D0A1B).copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFFF3D00).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("❤️", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("$livesRemaining", color = Color(0xFFFF3D00), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Live Stars
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF0D0A1B).copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("⭐", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("$starsCollected/$totalLevelStars", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Coins
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF0D0A1B).copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("🪙", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("$coinsCollected", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF0D0A1B).copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("⏱️", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(formattedTime, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                IconButton(
                    onClick = { isPaused = true },
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(0xFF0D0A1B).copy(alpha = 0.85f), CircleShape)
                        .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // --- DYNAMIC FLOATING JOYSTICK (LEFT HALF TOUCH AREA) & JUMP BUTTON (RIGHT SIDE) ---
        val density = LocalDensity.current

        // Dynamic Floating Joystick touch zone covering left 50% of screen
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterStart)
                .pointerInput(controlScale) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (change.pressed) {
                                val touchPos = change.position
                                if (!isJoystickActive) {
                                    isJoystickActive = true
                                    joystickCenterPos = touchPos
                                    joystickKnobPos = touchPos
                                    triggerHaptic(context, vibrationEnabled)
                                } else {
                                    joystickKnobPos = touchPos
                                }

                                val center = joystickCenterPos ?: touchPos
                                val rawOffset = touchPos - center
                                val distance = rawOffset.getDistance()

                                val baseRadiusPx = with(density) { 42.dp.toPx() * controlScale }
                                val knobRadiusPx = with(density) { 18.dp.toPx() * controlScale }
                                val maxDist = (baseRadiusPx - knobRadiusPx).coerceAtLeast(1f)
                                val clampedDist = distance.coerceAtMost(maxDist)
                                val angle = atan2(rawOffset.y, rawOffset.x)

                                val clampedOffset = if (distance > 0f) {
                                    Offset(cos(angle) * clampedDist, sin(angle) * clampedDist)
                                } else Offset.Zero

                                val rawNormX = clampedOffset.x / maxDist
                                val deadZone = 0.08f
                                val normX = when {
                                    rawNormX > deadZone -> ((rawNormX - deadZone) / (1f - deadZone)).coerceIn(0f, 1f)
                                    rawNormX < -deadZone -> ((rawNormX + deadZone) / (1f - deadZone)).coerceIn(-1f, 0f)
                                    else -> 0f
                                }

                                joystickXAmount = normX

                                val currentDir = if (normX < -0.3f) -1 else if (normX > 0.3f) 1 else 0
                                if (currentDir != lastVibratedDirection && currentDir != 0) {
                                    lastVibratedDirection = currentDir
                                    triggerHaptic(context, vibrationEnabled)
                                } else if (currentDir == 0) {
                                    lastVibratedDirection = 0
                                }

                                change.consume()
                            } else {
                                isJoystickActive = false
                                joystickXAmount = 0f
                                lastVibratedDirection = 0
                            }
                        }
                    }
                }
        )

        // Floating Joystick Canvas Visual with smooth fade-in and fade-out animation (~38% opacity)
        val joystickAlpha by animateFloatAsState(
            targetValue = if (isJoystickActive) 0.38f else 0f,
            animationSpec = tween(durationMillis = 100),
            label = "joystick_fade"
        )

        if (joystickAlpha > 0.01f && joystickCenterPos != null) {
            val center = joystickCenterPos!!
            val knobPos = joystickKnobPos ?: center
            val rawOffset = knobPos - center
            val baseRadiusPx = with(density) { 42.dp.toPx() * controlScale }
            val knobRadiusPx = with(density) { 18.dp.toPx() * controlScale }
            val maxDist = (baseRadiusPx - knobRadiusPx).coerceAtLeast(1f)
            val clampedDist = rawOffset.getDistance().coerceAtMost(maxDist)
            val angle = atan2(rawOffset.y, rawOffset.x)
            val clampedOffset = if (rawOffset.getDistance() > 0f) {
                Offset(cos(angle) * clampedDist, sin(angle) * clampedDist)
            } else Offset.Zero
            val finalKnobPos = center + clampedOffset

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = joystickAlpha }
            ) {
                // Base Outer Circle
                drawCircle(
                    color = Color(0xFF0D0A1B),
                    radius = baseRadiusPx,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = baseRadiusPx,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Directional guide line
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(center.x - baseRadiusPx + 8.dp.toPx(), center.y),
                    end = Offset(center.x + baseRadiusPx - 8.dp.toPx(), center.y),
                    strokeWidth = 2.dp.toPx()
                )

                // Joystick Knob
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                    radius = knobRadiusPx * 1.25f,
                    center = finalKnobPos
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFF00E5FF), Color(0xFF131024)),
                        center = finalKnobPos - Offset(2.dp.toPx(), 2.dp.toPx()),
                        radius = knobRadiusPx
                    ),
                    radius = knobRadiusPx,
                    center = finalKnobPos
                )
                drawCircle(
                    color = Color.White,
                    radius = knobRadiusPx,
                    center = finalKnobPos,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Jump Button (Bottom Right)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            VirtualJumpButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                scale = controlScale,
                onJumpPressed = {
                    jumpRequested = true
                },
                onVibrate = {
                    triggerHaptic(context, vibrationEnabled)
                }
            )
        }
    }

    // --- PAUSE GAME OVERLAY DIALOG ---
    if (isPaused && !isGameOver && !gameCompleted) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("🎮 GAME PAUSED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text("Your campaign is currently held. Resume when you're ready!", color = Color.Gray)
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Resume Play", color = Color.White)
                    }

                    Button(
                        onClick = { showSkinsInGameDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B2C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ball Skins 🎨", color = Color.White)
                    }

                    Button(
                        onClick = { showSettingsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B2C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Controls & Settings ⚙️", color = Color.White)
                    }
                    Button(
                        onClick = { restartActiveGame(); isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B2C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restart Level", color = Color.White)
                    }
                    Button(
                        onClick = onBackToMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Exit to Map", color = Color.Red)
                    }
                }
            }
        )
    }

    // --- GAME OVER OVERLAY DIALOG ---
    // --- GAME OVER OVERLAY DIALOG ---
    if (isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("💥 GAME OVER!", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Out of lives! Watch out for floor/wall spikes, traps, and crumbling platforms.", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STARS", color = Color.Gray, fontSize = 11.sp)
                            Text("$starsCollected / $totalLevelStars", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("COINS", color = Color.Gray, fontSize = 11.sp)
                            Text("$coinsCollected", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TIME", color = Color.Gray, fontSize = 11.sp)
                            Text(formattedTime, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!hasUsedAdRevive) {
                        Button(
                            onClick = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    if (RewardedManager.isAdReady(activity)) {
                                        RewardedManager.showAd(
                                            activity = activity,
                                            rewardType = RewardType.BONUS_COINS,
                                            callbacks = object : RewardCallback {
                                                override fun onRewardEarned(rewardType: RewardType, amount: Int, token: String) {
                                                    livesRemaining = 3
                                                    hasUsedAdRevive = true
                                                    isGameOver = false
                                                    invincibilityTimer = 3.0f
                                                    ballX = lastCheckpointX
                                                    ballY = lastCheckpointY - 10f
                                                    ballVx = 0f
                                                    ballVy = 0f
                                                    android.widget.Toast.makeText(context, "❤️ Revived with 3 Lives from Checkpoint!", android.widget.Toast.LENGTH_SHORT).show()
                                                }

                                                override fun onAdFailedToLoad(errorCode: Int, errorMessage: String) {
                                                    android.widget.Toast.makeText(context, "Advertisement not available. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
                                                    RewardedManager.preload(activity)
                                                }

                                                override fun onAdFailedToShow(errorMessage: String) {
                                                    android.widget.Toast.makeText(context, "Advertisement not available. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
                                                    RewardedManager.preload(activity)
                                                }

                                                override fun onAdClosed(userEarnedReward: Boolean) {
                                                    if (!userEarnedReward) {
                                                        android.widget.Toast.makeText(context, "Ad skipped. No revive granted.", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                    RewardedManager.preload(activity)
                                                }
                                            }
                                        )
                                    } else {
                                        android.widget.Toast.makeText(context, "Advertisement not available. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
                                        RewardedManager.preload(activity)
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "Advertisement not available. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF00D6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Watch Ad → Continue (+3 Lives) 📺❤️", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { restartActiveGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restart Level 🔄", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onBackToMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B2C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Exit to Map", color = Color.White)
                    }
                }
            }
        )
    }

    // --- VICTORY TRANSITION FLOW CONTROLLER ---
    fun triggerNextLevelTransition(adWatched: Boolean) {
        val coinsReward = 10 + (starsCollected * 1) + coinsCollected
        scope.launch {
            val messages = listOf(
                "Loading Next Adventure...",
                "Generating New World...",
                "Preparing Next Challenge..."
            )
            loadingMessage = messages.random()
            isLoadingNextLevel = true

            // Smooth fade-in
            val fadeDuration = 300L
            val steps = 15
            for (step in 1..steps) {
                transitionAlpha = step / steps.toFloat()
                delay(fadeDuration / steps)
            }

            // Save progress and update level
            onLevelCompleted(
                starsCollected,
                coinsCollected,
                currentScore,
                levelAttemptDeaths,
                missedJumps,
                elapsedTimeSeconds,
                checkpointRespawns,
                dynamicCollectibles.any { it.isCollected && it.isBonus }
            )

            // Hold transition screen for premium game feel
            delay(1000L)

            // Smooth fade-out
            for (step in steps downTo 0) {
                transitionAlpha = step / steps.toFloat()
                delay(fadeDuration / steps)
            }

            isLoadingNextLevel = false
        }
    }

    // --- VICTORY GAME OVERLAY DIALOG ---
    if (gameCompleted && showVictoryDialog && !isLoadingNextLevel) {
        val coinsReward = 10 + (starsCollected * 1) + coinsCollected

        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 8.dp,
            title = {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                    IconButton(
                        onClick = { showVictoryDialog = false },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Level",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "🎉 LEVEL COMPLETE!",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            },
            text = {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val dialogWidth = maxWidth
                    val dialogHeight = maxHeight

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Ready for your next challenge?",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )

                        // 1. Comparison Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1B2C).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Left: Normal Reward
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("🪙", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+$coinsReward",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Coins Earned",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(Color.White.copy(alpha = 0.15f))
                            )

                            // Right: 2X Reward
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("💰", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+${coinsReward * 2}",
                                        color = Color(0xFFFF00D6),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFF00D6), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "2X",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "With Ad (2X Coins)",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // 2. Interactive Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isPending = rewardState == "Pending"

                            // Left Button: Watch Ad
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFF00D6),
                                                Color(0xFF8000FF)
                                            )
                                        )
                                    )
                                    .graphicsLayer { alpha = if (isPending) 1f else 0.4f }
                                    .clickable(enabled = isPending && !isProcessingReward) {
                                        if (isPending && !isProcessingReward) {
                                            isProcessingReward = true
                                            val activity = context as? Activity
                                            if (activity != null) {
                                                if (RewardedManager.isAdReady(activity)) {
                                                    RewardedManager.showAd(
                                                        activity = activity,
                                                        rewardType = RewardType.BONUS_COINS,
                                                        callbacks = object : RewardCallback {
                                                            override fun onRewardEarned(rewardType: RewardType, amount: Int, token: String) {
                                                                val txId = "bounce_lvl_${level.number}_ad_${System.currentTimeMillis()}"
                                                                rewardState = "2X Claimed"
                                                                prefs.edit().putString("bounce_reward_state_level_${level.number}", "2X Claimed").commit()

                                                                 viewModel.addCoins(coinsReward + 15, "Bounce Quest Lvl ${level.number} (+15 Ad Bonus)", txId) { success ->
                                                                    triggerCoinFlyAnimation {
                                                                        android.widget.Toast.makeText(context, "Claimed Bonus (+15 ad coins)!", android.widget.Toast.LENGTH_SHORT).show()
                                                                        triggerNextLevelTransition(adWatched = true)
                                                                        isProcessingReward = false
                                                                    }
                                                                }
                                                            }

                                                            override fun onAdFailedToLoad(errorCode: Int, errorMessage: String) {
                                                                android.widget.Toast.makeText(context, "Ad failed. Please try again or claim normal reward.", android.widget.Toast.LENGTH_SHORT).show()
                                                                RewardedManager.preload(activity)
                                                                isProcessingReward = false
                                                            }

                                                            override fun onAdFailedToShow(errorMessage: String) {
                                                                android.widget.Toast.makeText(context, "Ad failed. Please try again or claim normal reward.", android.widget.Toast.LENGTH_SHORT).show()
                                                                RewardedManager.preload(activity)
                                                                isProcessingReward = false
                                                            }

                                                            override fun onAdClosed(userEarnedReward: Boolean) {
                                                                RewardedManager.preload(activity)
                                                                if (!userEarnedReward) {
                                                                    isProcessingReward = false
                                                                }
                                                            }
                                                        }
                                                    )
                                                } else {
                                                    android.widget.Toast.makeText(context, "Ad not ready. Preloading... please try again or claim normal reward.", android.widget.Toast.LENGTH_SHORT).show()
                                                    RewardedManager.preload(activity)
                                                    isProcessingReward = false
                                                }
                                            } else {
                                                isProcessingReward = false
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🎥", fontSize = 16.sp)
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("Watch Ad", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("+15 Ad Bonus", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
                                    }
                                }
                            }

                            // Right Button: Claim Reward
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFACC15),
                                                Color(0xFFEAB308)
                                            )
                                        )
                                    )
                                    .graphicsLayer { alpha = if (isPending) 1f else 0.4f }
                                    .clickable(enabled = isPending && !isProcessingReward) {
                                        if (isPending && !isProcessingReward) {
                                            isProcessingReward = true
                                            val txId = "bounce_lvl_${level.number}_claim_${System.currentTimeMillis()}"
                                            rewardState = "Claimed"
                                            prefs.edit().putString("bounce_reward_state_level_${level.number}", "Claimed").commit()

                                            viewModel.addCoins(coinsReward, "Bounce Quest Lvl ${level.number}", txId) { success ->
                                                triggerCoinFlyAnimation {
                                                    android.widget.Toast.makeText(context, "Claimed $coinsReward coins successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                                    triggerNextLevelTransition(adWatched = false)
                                                    isProcessingReward = false
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🪙", fontSize = 16.sp)
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("Claim Reward", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("+${coinsReward}", color = Color.Black.copy(alpha = 0.8f), fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // 3. Continue Button
                        Button(
                            onClick = {
                                if (!isProcessingReward) {
                                    isProcessingReward = true
                                    if (rewardState == "Pending") {
                                        val txId = "bounce_lvl_${level.number}_claim_${System.currentTimeMillis()}"
                                        rewardState = "Claimed"
                                        prefs.edit().putString("bounce_reward_state_level_${level.number}", "Claimed").commit()
                                        viewModel.addCoins(coinsReward, "Bounce Quest Lvl ${level.number}", txId) { success ->
                                            triggerCoinFlyAnimation {
                                                triggerNextLevelTransition(adWatched = false)
                                                isProcessingReward = false
                                            }
                                        }
                                    } else {
                                        triggerNextLevelTransition(adWatched = false)
                                        isProcessingReward = false
                                    }
                                }
                            },
                            enabled = !isProcessingReward,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E1B2C),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // 4. Exit Button (Red link at the bottom)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isProcessingReward) {
                                    if (!isProcessingReward) {
                                        isProcessingReward = true
                                        if (rewardState == "Pending") {
                                            val txId = "bounce_lvl_${level.number}_claim_${System.currentTimeMillis()}"
                                            rewardState = "Claimed"
                                            prefs.edit().putString("bounce_reward_state_level_${level.number}", "Claimed").commit()
                                            viewModel.addCoins(coinsReward, "Bounce Quest Lvl ${level.number}", txId) { success ->
                                                triggerCoinFlyAnimation {
                                                    onBackToMenu()
                                                    isProcessingReward = false
                                                }
                                            }
                                        } else {
                                            onBackToMenu()
                                            isProcessingReward = false
                                        }
                                    }
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Exit",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Exit",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // 5. Coin Particles Flying Animation Overlay
                    if (showCoinAnimation) {
                        coinParticles.forEach { particle ->
                            val globalProgress = coinAnimProgress.value
                            val totalAnimTime = 1400f
                            val particleStartFraction = particle.delayMs / totalAnimTime
                            val particleEndFraction = (particle.delayMs + particle.durationMs) / totalAnimTime
                            val pProgress = ((globalProgress - particleStartFraction) / (particleEndFraction - particleStartFraction)).coerceIn(0f, 1f)

                            if (pProgress > 0f && pProgress < 1f) {
                                val dialogWidthPx = dialogWidth.value
                                val dialogHeightPx = dialogHeight.value

                                // Map particle positions dynamically relative to dialog width & height
                                val startX = dialogWidthPx * (particle.startX / 300f)
                                val startY = dialogHeightPx * (particle.startY / 500f)
                                val endX = dialogWidthPx * (particle.endX / 300f)
                                val endY = dialogHeightPx * (particle.endY / 500f)

                                // Curved trajectory
                                val currentX = startX + (endX - startX) * pProgress + (Math.sin(pProgress * Math.PI) * 25f).toFloat()
                                val currentY = startY + (endY - startY) * pProgress
                                val scale = 1.2f - (pProgress * 0.6f)
                                val alpha = if (pProgress > 0.8f) (1f - pProgress) / 0.2f else 1f

                                Text(
                                    text = "🪙",
                                    fontSize = 18.sp,
                                    modifier = Modifier
                                        .graphicsLayer {
                                            translationX = currentX
                                            translationY = currentY
                                            scaleX = scale
                                            scaleY = scale
                                            this.alpha = alpha
                                            rotationZ = pProgress * 360f
                                        }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // --- REOPEN VICTORY DIALOG BUTTON ---
    if (gameCompleted && !showVictoryDialog && !isLoadingNextLevel) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Button(
                onClick = { showVictoryDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Show Level Summary",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Level Summary 🏆", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- PREMIUM LOADING TRANSITION OVERLAY ---
    if (isLoadingNextLevel) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0E17).copy(alpha = transitionAlpha))
                .clickable(enabled = false) {}, // absorb touch events
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF00E5FF),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = loadingMessage,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // --- SETTINGS OVERLAY DIALOG ---
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("⚙️ CONTROLS & SETTINGS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Customize virtual joystick size & vibration feedback for peak platformer control.", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Control Size:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.8f to "Small", 1.0f to "Medium", 1.25f to "Large").forEach { (sc, label) ->
                            val isSelected = kotlin.math.abs(controlScale - sc) < 0.05f
                            Button(
                                onClick = {
                                    controlScale = sc
                                    prefs.edit().putFloat("control_scale", sc).apply()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF7C3AED) else Color(0xFF1E1B2C)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text(label, color = Color.White, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vibration Feedback:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { checked ->
                                vibrationEnabled = checked
                                prefs.edit().putBoolean("vibration_enabled", checked).apply()
                                if (checked) triggerHaptic(context, true)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF7C3AED),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF1E1B2C)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showSkinsInGameDialog) {
        var ownedSkins by remember {
            val setStr = prefs.getStringSet("owned_skins", setOf("skin_neon_violet")) ?: setOf("skin_neon_violet")
            mutableStateOf(setStr)
        }
        val walletState by viewModel.walletState.collectAsStateWithLifecycle()

        AlertDialog(
            onDismissRequest = { showSkinsInGameDialog = false },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🎨 BALL SKINS STORE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    IconButton(onClick = { showSkinsInGameDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Equip a skin to apply instantly to your ball!", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(BALL_SKINS) { skin ->
                            val isOwned = ownedSkins.contains(skin.id)
                            val isSelected = activeSkinId == skin.id

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) Color(0xFF7C3AED).copy(alpha = 0.25f)
                                        else Color(0xFF1E1B2C),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFA855F7)
                                        else Color.Gray.copy(alpha = 0.2f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(skin.primaryColor, skin.secondaryColor, skin.darkColor)
                                                ),
                                                CircleShape
                                            )
                                            .border(1.5.dp, skin.trailColor, CircleShape)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(skin.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(skin.description, color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    if (isSelected) {
                                        Text("EQUIPPED ✓", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 10.sp)
                                    } else if (isOwned) {
                                        Button(
                                            onClick = {
                                                activeSkinId = skin.id
                                                prefs.edit().putString("selected_skin", skin.id).apply()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Text("EQUIP", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                if (walletState.coins >= skin.priceCoins) {
                                                    viewModel.addCoins(-skin.priceCoins, "Unlocked Skin ${skin.name}")
                                                    val updatedSkins = ownedSkins + skin.id
                                                    ownedSkins = updatedSkins
                                                    prefs.edit().putStringSet("owned_skins", updatedSkins).apply()

                                                    activeSkinId = skin.id
                                                    prefs.edit().putString("selected_skin", skin.id).apply()

                                                    android.widget.Toast.makeText(context, "Unlocked ${skin.name}!", android.widget.Toast.LENGTH_SHORT).show()
                                                } else {
                                                    android.widget.Toast.makeText(context, "Need ${skin.priceCoins} coins!", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("${skin.priceCoins} 🪙", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSkinsInGameDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// --- HAPTIC & VIRTUAL CONTROLS ---
fun triggerHaptic(context: Context, enabled: Boolean) {
    if (!enabled) return
    try {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    } catch (_: Exception) {}
}

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    scale: Float = 1.0f,
    onValueChange: (xAmount: Float) -> Unit,
    onVibrate: () -> Unit
) {
    val density = LocalDensity.current
    val baseRadiusPx = with(density) { (55.dp * scale).toPx() }
    val knobRadiusPx = with(density) { (24.dp * scale).toPx() }

    var isDragging by remember { mutableStateOf(false) }
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var lastVibratedDirection by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .size((55.dp * 2 * scale))
            .pointerInput(scale) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (change.pressed) {
                            isDragging = true
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val rawOffset = change.position - center
                            val distance = rawOffset.getDistance()
                            val maxDist = (baseRadiusPx - knobRadiusPx).coerceAtLeast(1f)
                            val clampedDistance = distance.coerceAtMost(maxDist)
                            val angle = atan2(rawOffset.y, rawOffset.x)

                            val clampedOffset = if (distance > 0f) {
                                Offset(
                                    cos(angle) * clampedDistance,
                                    sin(angle) * clampedDistance
                                )
                            } else Offset.Zero

                            knobOffset = clampedOffset

                            val normalizedX = (clampedOffset.x / maxDist).coerceIn(-1f, 1f)
                            onValueChange(normalizedX)

                            val currentDir = if (normalizedX < -0.3f) -1 else if (normalizedX > 0.3f) 1 else 0
                            if (currentDir != lastVibratedDirection && currentDir != 0) {
                                lastVibratedDirection = currentDir
                                onVibrate()
                            } else if (currentDir == 0) {
                                lastVibratedDirection = 0
                            }

                            change.consume()
                        } else {
                            isDragging = false
                            knobOffset = Offset.Zero
                            lastVibratedDirection = 0
                            onValueChange(0f)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = baseRadiusPx

            // Outer ring background
            drawCircle(
                color = Color(0xFF0D0A1B).copy(alpha = if (isDragging) 0.70f else 0.45f),
                radius = outerRadius,
                center = center
            )
            // Outer ring border
            drawCircle(
                color = if (isDragging) Color(0xFF00E5FF).copy(alpha = 0.85f) else Color(0xFF7C3AED).copy(alpha = 0.5f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = if (isDragging) 3.dp.toPx() else 2.dp.toPx())
            )

            // Directional guide line
            val axisAlpha = if (isDragging) 0.6f else 0.3f
            drawLine(
                color = Color.White.copy(alpha = axisAlpha),
                start = Offset(center.x - outerRadius + 10.dp.toPx(), center.y),
                end = Offset(center.x + outerRadius - 10.dp.toPx(), center.y),
                strokeWidth = 2.dp.toPx()
            )

            // Joystick Knob
            val knobCenter = center + knobOffset
            val knobColor = if (isDragging) Color(0xFF00E5FF) else Color(0xFF7C3AED)

            drawCircle(
                color = knobColor.copy(alpha = 0.35f),
                radius = knobRadiusPx * 1.25f,
                center = knobCenter
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, knobColor, Color(0xFF131024)),
                    center = knobCenter - Offset(2.dp.toPx(), 2.dp.toPx()),
                    radius = knobRadiusPx
                ),
                radius = knobRadiusPx,
                center = knobCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = knobRadiusPx,
                center = knobCenter,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun VirtualJumpButton(
    modifier: Modifier = Modifier,
    scale: Float = 1.0f,
    onJumpPressed: () -> Unit,
    onVibrate: () -> Unit
) {
    val buttonSize = 68.dp * scale
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(buttonSize)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (change.pressed) {
                            if (!isPressed) {
                                isPressed = true
                                onJumpPressed()
                                onVibrate()
                            }
                            change.consume()
                        } else {
                            isPressed = false
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val currentScale = if (isPressed) 0.92f else 1.0f
        val bgColor = if (isPressed) Color(0xFF00E5FF) else Color(0xFF7C3AED)
        val alpha = if (isPressed) 0.85f else 0.55f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(currentScale)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            bgColor.copy(alpha = alpha),
                            Color(0xFF3B0764).copy(alpha = alpha)
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = if (isPressed) 3.dp else 2.dp,
                    color = if (isPressed) Color.White else Color(0xFF00E5FF).copy(alpha = 0.7f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Jump",
                    tint = Color.White,
                    modifier = Modifier.size((26 * scale).dp)
                )
                Text(
                    text = "JUMP",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (9 * scale).sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// --- COLLISION SOLVERS ---
private fun checkCollision(bx: Float, by: Float, br: Float, obs: BounceObstacle): Boolean {
    // Closest point on platform to circle center
    val closestX = bx.safeCoerceIn(obs.x, obs.x + obs.width)
    val closestY = by.safeCoerceIn(obs.y, obs.y + obs.height)

    // Distance squared
    val distSq = (bx - closestX) * (bx - closestX) + (by - closestY) * (by - closestY)
    return distSq < (br * br)
}

// Canvas extensions to draw vector star safely
private fun DrawScope.drawStar(
    cx: Float,
    cy: Float,
    spikes: Int,
    outerRadius: Float,
    innerRadius: Float,
    color: Color
) {
    val path = Path()
    var rot = Math.PI / 2 * 3
    val step = Math.PI / spikes

    path.moveTo(cx, cy - outerRadius)
    for (i in 0 until spikes) {
        val x1 = cx + cos(rot).toFloat() * outerRadius
        val y1 = cy + sin(rot).toFloat() * outerRadius
        path.lineTo(x1, y1)
        rot += step

        val x2 = cx + cos(rot).toFloat() * innerRadius
        val y2 = cy + sin(rot).toFloat() * innerRadius
        path.lineTo(x2, y2)
        rot += step
    }
    path.close()
    drawPath(path, color = color)
}

// --- HISTORY SERIALIZER/DESERIALIZER ---
private fun parseBounceHistory(data: String): List<BounceHistoryEntry> {
    if (data.isEmpty()) return emptyList()
    val list = mutableListOf<BounceHistoryEntry>()
    try {
        val records = data.split(";")
        for (r in records) {
            if (r.isEmpty()) continue
            val parts = r.split("|")
            if (parts.size == 5) {
                list.add(
                    BounceHistoryEntry(
                        date = parts[0],
                        levelName = parts[1],
                        stars = parts[2].toIntOrNull() ?: 0,
                        coins = parts[3].toIntOrNull() ?: 0,
                        score = parts[4].toIntOrNull() ?: 0
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

private fun serializeBounceHistory(history: List<BounceHistoryEntry>): String {
    val sb = StringBuilder()
    for (i in history.indices) {
        val e = history[i]
        sb.append(e.date).append("|").append(e.levelName).append("|").append(e.stars).append("|").append(e.coins).append("|").append(e.score)
        if (i < history.size - 1) {
            sb.append(";")
        }
    }
    return sb.toString()
}
