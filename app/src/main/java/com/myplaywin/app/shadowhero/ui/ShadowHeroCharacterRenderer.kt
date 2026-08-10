package com.myplaywin.app.shadowhero.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.myplaywin.app.shadowhero.engine.AmbientParticleType
import com.myplaywin.app.shadowhero.engine.ChunkBiomeInfo
import com.myplaywin.app.shadowhero.engine.DashGhostFrame
import com.myplaywin.app.shadowhero.engine.ShadowHeroEventDirector
import com.myplaywin.app.shadowhero.engine.EnemyProjectile
import com.myplaywin.app.shadowhero.engine.EnemyState
import com.myplaywin.app.shadowhero.engine.EnemyType
import com.myplaywin.app.shadowhero.engine.ProjectileType
import com.myplaywin.app.shadowhero.engine.EnvHazardType
import com.myplaywin.app.shadowhero.engine.FloatingText
import com.myplaywin.app.shadowhero.engine.HeroParticle
import com.myplaywin.app.shadowhero.engine.LaserState
import com.myplaywin.app.shadowhero.engine.LevelCheckpoint
import com.myplaywin.app.shadowhero.engine.LevelEnemy
import com.myplaywin.app.shadowhero.engine.LevelEnergyCrystal
import com.myplaywin.app.shadowhero.engine.LevelEnvHazard
import com.myplaywin.app.shadowhero.engine.LevelExitPortal
import com.myplaywin.app.shadowhero.engine.LevelLaserBeam
import com.myplaywin.app.shadowhero.engine.LevelMovingSpike
import com.myplaywin.app.shadowhero.engine.LevelPlatform
import com.myplaywin.app.shadowhero.engine.LevelPowerUp
import com.myplaywin.app.shadowhero.engine.LevelRotatingBlade
import com.myplaywin.app.shadowhero.engine.LevelSpike
import com.myplaywin.app.shadowhero.engine.LevelTheme
import com.myplaywin.app.shadowhero.engine.PlayerAnimState
import com.myplaywin.app.shadowhero.engine.PowerUpType
import com.myplaywin.app.shadowhero.engine.ShadowHeroPlayer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the Shadow Hero on the Canvas at screen position (drawX, drawY).
 */
fun DrawScope.drawShadowHeroCharacter(
    player: ShadowHeroPlayer,
    drawX: Float,
    drawY: Float,
    animTime: Float,
    activeBiome: ChunkBiomeInfo? = null
) {
    val w = player.width
    val h = player.height

    // Calculate squash & stretch matrix values based on state
    var scaleX = if (player.facingRight) 1f else -1f
    var scaleY = 1f
    var bodyRotation = 0f
    var bodyOffsetY = 0f
    var alphaVal = 1f

    when (player.animState) {
        PlayerAnimState.LAND -> {
            scaleX *= 1.30f
            scaleY = 0.70f
            bodyOffsetY = 5f
        }
        PlayerAnimState.JUMP -> {
            scaleY = 1.22f
            scaleX *= 0.82f
            bodyOffsetY = -4f
        }
        PlayerAnimState.DOUBLE_JUMP -> {
            scaleY = 0.90f
            scaleX *= 0.90f
            bodyRotation = (animTime * 720f) % 360f
        }
        PlayerAnimState.FALL -> {
            scaleY = 1.16f
            scaleX *= 0.86f
            bodyOffsetY = 3f
        }
        PlayerAnimState.DASH -> {
            scaleX *= 1.45f
            scaleY = 0.65f
            bodyOffsetY = 3f
        }
        PlayerAnimState.WALL_SLIDE -> {
            scaleX *= 0.88f
            scaleY = 1.12f
            bodyRotation = if (player.isOnLeftWall) -10f else 10f
        }
        PlayerAnimState.WALL_JUMP -> {
            scaleX *= 0.92f
            scaleY = 1.14f
            bodyRotation = if (player.facingRight) -22f else 22f
        }
        PlayerAnimState.HURT -> {
            bodyRotation = if (player.facingRight) -16f else 16f
            bodyOffsetY = sin(animTime * 60f) * 4f
        }
        PlayerAnimState.DEATH -> {
            alphaVal = (player.deathTimer / 0.8f).coerceIn(0f, 1f)
            scaleX *= (0.75f + 0.25f * alphaVal)
            scaleY = 0.75f + 0.25f * alphaVal
            bodyOffsetY = (1f - alphaVal) * 12f
        }
        PlayerAnimState.RESPAWN -> {
            val progress = (1.0f - (player.respawnTimer / 0.6f)).coerceIn(0.1f, 1.0f)
            alphaVal = progress
            scaleX *= (0.3f + 0.7f * progress)
            scaleY = 0.3f + 0.7f * progress
        }
        PlayerAnimState.RUN -> {
            bodyOffsetY = abs(sin(player.runCycleTimer)) * -4f
            bodyRotation = if (player.facingRight) 7f else -7f
        }
        PlayerAnimState.IDLE -> {
            scaleY = 1f + sin(animTime * 3.5f) * 0.025f
            bodyOffsetY = sin(animTime * 3.5f) * 1.5f
        }
        PlayerAnimState.ATTACK -> {
            scaleX *= 1.25f
            scaleY = 0.90f
            bodyOffsetY = -2f
            bodyRotation = if (player.facingRight) 12f else -12f
        }
    }

    if (alphaVal <= 0.01f) return

    val centerX = drawX + w / 2f
    val centerY = drawY + h / 2f + bodyOffsetY

    // Contrast adjustment based on active biome/theme
    val isBrightBiome = activeBiome?.effectivePrimaryBgColor?.let { color ->
        (color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f) > 0.45f
    } ?: false

    val outerBorderColor = if (isBrightBiome) Color(0xFF05020B) else Color(0xFFC084FC)
    val outerBorderWidth = if (isBrightBiome) 2.8f else 2.0f

    // Save Canvas State
    val check = drawContext.canvas.save()

    // Apply Facing, Rotation & Squash Pivot
    drawContext.transform.scale(
        scaleX = scaleX,
        scaleY = scaleY,
        pivot = Offset(centerX, drawY + h)
    )

    if (bodyRotation != 0f) {
        drawContext.transform.rotate(
            degrees = bodyRotation,
            pivot = Offset(centerX, centerY)
        )
    }

    // 1. Ground Shadow (if near ground)
    if (player.isGrounded && player.animState != PlayerAnimState.DEATH) {
        drawOval(
            color = Color.Black.copy(alpha = 0.55f * alphaVal),
            topLeft = Offset(centerX - w * 0.5f, drawY + h - 4f),
            size = Size(w, 8f)
        )
    }

    // 2. Neon Energy Aura Glow / State Effect
    val auraColor = when (player.animState) {
        PlayerAnimState.HURT -> Color(0xFFEF4444)
        PlayerAnimState.DASH -> Color(0xFF38BDF8)
        PlayerAnimState.DOUBLE_JUMP -> Color(0xFFE879F9)
        PlayerAnimState.DEATH -> Color(0xFF7F1D1D)
        PlayerAnimState.RESPAWN -> Color(0xFF38BDF8)
        else -> Color(0xFFA855F7)
    }

    val alphaMultiplier = if (player.animState == PlayerAnimState.HURT && (animTime * 20f).toInt() % 2 == 0) 0.2f else alphaVal

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                auraColor.copy(alpha = 0.60f * alphaMultiplier),
                Color(0xFF6D28D9).copy(alpha = 0.25f * alphaMultiplier),
                Color.Transparent
            ),
            center = Offset(centerX, centerY - 4f),
            radius = w * 1.35f
        ),
        center = Offset(centerX, centerY - 4f),
        radius = w * 1.35f
    )

    // Double Jump Shockwave Burst Ring
    if (player.animState == PlayerAnimState.DOUBLE_JUMP) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0xFF38BDF8).copy(alpha = 0.7f), Color.Transparent),
                center = Offset(centerX, centerY),
                radius = w * 1.6f
            ),
            center = Offset(centerX, centerY),
            radius = w * 1.6f
        )
    }

    // Respawn Energy Gathering Ring
    if (player.animState == PlayerAnimState.RESPAWN) {
        val ringRadius = w * (1.8f - alphaVal * 0.8f)
        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.8f * alphaVal),
            center = Offset(centerX, centerY),
            radius = ringRadius,
            style = Stroke(width = 3f)
        )
    }

    // 3. Flowing Purple Scarf (Waving behind the hero)
    val runBounce = if (player.animState == PlayerAnimState.RUN) sin(player.runCycleTimer) * 4f else 0f
    val scarfWave1 = when (player.animState) {
        PlayerAnimState.DASH -> 2f
        PlayerAnimState.FALL -> -14f
        PlayerAnimState.RUN -> sin(player.runCycleTimer * 1.5f) * 12f
        else -> sin(animTime * 10f) * 8f
    }
    val scarfWave2 = cos(animTime * 8f) * 10f
    val scarfDirection = -1f // Tail behind facing direction

    val scarfPath = Path().apply {
        moveTo(centerX, drawY + 16f + runBounce)
        cubicTo(
            centerX + scarfDirection * 16f, drawY + 18f + scarfWave1,
            centerX + scarfDirection * 32f, drawY + 22f + scarfWave2,
            centerX + scarfDirection * 48f, drawY + 28f + scarfWave1
        )
        lineTo(centerX + scarfDirection * 44f, drawY + 36f + scarfWave1)
        cubicTo(
            centerX + scarfDirection * 30f, drawY + 28f + scarfWave2,
            centerX + scarfDirection * 15f, drawY + 22f + scarfWave1,
            centerX, drawY + 20f + runBounce
        )
        close()
    }
    drawPath(
        path = scarfPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                if (player.animState == PlayerAnimState.HURT) Color(0xFFF87171) else Color(0xFFE879F9),
                Color(0xFFA855F7),
                Color(0xFF6D28D9)
            )
        ),
        alpha = alphaVal
    )

    // 4. Dark Ninja Warrior Body & Suit
    val bodyTop = drawY + 18f + runBounce
    val bodyBottom = drawY + h - 6f

    val bodyColor = when {
        player.animState == PlayerAnimState.DEATH -> Color.Black
        player.animState == PlayerAnimState.HURT && (animTime * 25f).toInt() % 2 == 0 -> Color(0xFFEF4444)
        else -> Color(0xFF090314)
    }

    // Suit Body Fill
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(centerX - 13f, bodyTop),
        size = Size(26f, bodyBottom - bodyTop),
        cornerRadius = CornerRadius(6f, 6f),
        alpha = alphaVal
    )

    // Suit Outline Accent
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(outerBorderColor, Color(0xFF6D28D9), Color(0xFF1E1038))
        ),
        topLeft = Offset(centerX - 13f, bodyTop),
        size = Size(26f, bodyBottom - bodyTop),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = outerBorderWidth),
        alpha = alphaVal
    )

    // Glowing Chest Core Emblem
    val chestY = bodyTop + 10f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(auraColor, Color(0xFFA855F7), Color.Transparent),
            center = Offset(centerX, chestY),
            radius = 7f
        ),
        center = Offset(centerX, chestY),
        radius = 7f,
        alpha = alphaVal
    )
    drawCircle(
        color = Color.White,
        center = Offset(centerX, chestY),
        radius = 2.5f,
        alpha = alphaVal
    )

    // 5. Spiky Shadow Head with Hair Tufts
    val headTop = drawY + runBounce - 6f
    val headPath = Path().apply {
        moveTo(centerX - 15f, headTop + 14f)
        lineTo(centerX - 20f, headTop + 2f)
        lineTo(centerX - 10f, headTop + 6f)
        lineTo(centerX - 5f, headTop - 6f)
        lineTo(centerX, headTop + 2f)
        lineTo(centerX + 6f, headTop - 8f)
        lineTo(centerX + 12f, headTop + 6f)
        lineTo(centerX + 21f, headTop + 4f)
        lineTo(centerX + 16f, headTop + 16f)
        cubicTo(centerX + 16f, headTop + 28f, centerX + 10f, headTop + 34f, centerX, headTop + 34f)
        cubicTo(centerX - 10f, headTop + 34f, centerX - 16f, headTop + 28f, centerX - 16f, headTop + 16f)
        close()
    }

    // Outer Head Fill
    drawPath(
        path = headPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                if (player.animState == PlayerAnimState.DEATH) Color.Black else Color(0xFF3B0764),
                if (player.animState == PlayerAnimState.DEATH) Color.Black else Color(0xFF180828),
                Color(0xFF090314)
            )
        ),
        alpha = alphaVal
    )

    // Head Rim Accent
    drawPath(
        path = headPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                if (player.animState == PlayerAnimState.DEATH) Color(0xFF7F1D1D) else outerBorderColor,
                Color(0xFFA855F7),
                Color.Transparent
            )
        ),
        style = Stroke(width = outerBorderWidth),
        alpha = alphaVal
    )

    // 6. Vibrant Glowing Eyes
    if (player.animState != PlayerAnimState.DEATH || alphaVal > 0.4f) {
        val eyeY = headTop + 20f
        val eyeGlowRadius = 6.5f + sin(animTime * 8f) * 1.2f
        val eyeColor = when (player.animState) {
            PlayerAnimState.HURT -> Color(0xFFEF4444)
            PlayerAnimState.DASH -> Color(0xFF38BDF8)
            PlayerAnimState.RESPAWN -> Color(0xFF38BDF8)
            else -> Color(0xFFE879F9)
        }

        // Right Eye
        val eyeRightX = centerX + 5f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(eyeColor, eyeColor.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(eyeRightX, eyeY),
                radius = eyeGlowRadius * 2.2f
            ),
            center = Offset(eyeRightX, eyeY),
            radius = eyeGlowRadius * 2.2f,
            alpha = alphaVal
        )
        drawCircle(color = Color.White, center = Offset(eyeRightX, eyeY), radius = 3.2f, alpha = alphaVal)

        // Left Eye
        val eyeLeftX = centerX - 5f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(eyeColor, eyeColor.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(eyeLeftX, eyeY),
                radius = eyeGlowRadius * 1.8f
            ),
            center = Offset(eyeLeftX, eyeY),
            radius = eyeGlowRadius * 1.8f,
            alpha = alphaVal
        )
        drawCircle(color = Color.White, center = Offset(eyeLeftX, eyeY), radius = 2.6f, alpha = alphaVal)
    }

    // 7. Leg & Arm Pose Animation for RUN, WALL_SLIDE, JUMP, FALL
    when (player.animState) {
        PlayerAnimState.RUN -> {
            val leg1 = sin(player.runCycleTimer) * 10f
            val leg2 = sin(player.runCycleTimer + Math.PI.toFloat()) * 10f

            // Legs
            drawRect(
                color = Color(0xFF38BDF8),
                topLeft = Offset(centerX - 8f + leg1, drawY + h - 8f),
                size = Size(6f, 8f),
                alpha = alphaVal
            )
            drawRect(
                color = Color(0xFFA855F7),
                topLeft = Offset(centerX + 2f + leg2, drawY + h - 8f),
                size = Size(6f, 8f),
                alpha = alphaVal
            )

            // Swinging Arms
            val arm1 = cos(player.runCycleTimer) * 8f
            val arm2 = cos(player.runCycleTimer + Math.PI.toFloat()) * 8f
            drawCircle(color = Color(0xFFC084FC), center = Offset(centerX - 14f + arm1, bodyTop + 14f), radius = 3.5f, alpha = alphaVal)
            drawCircle(color = Color(0xFFC084FC), center = Offset(centerX + 14f + arm2, bodyTop + 14f), radius = 3.5f, alpha = alphaVal)
        }
        PlayerAnimState.WALL_SLIDE -> {
            // Wall slide contact sparks & leg against wall
            drawRect(
                color = Color(0xFF38BDF8),
                topLeft = Offset(centerX + 6f, drawY + h - 12f),
                size = Size(7f, 12f),
                alpha = alphaVal
            )
            drawCircle(
                color = Color(0xFFFAE8FF),
                center = Offset(centerX + 13f, drawY + h - 6f),
                radius = 3f + sin(animTime * 30f) * 1.5f,
                alpha = alphaVal
            )
        }
        PlayerAnimState.JUMP, PlayerAnimState.DOUBLE_JUMP -> {
            // Tucked leg pose for airborne jump
            drawRect(color = Color(0xFF38BDF8), topLeft = Offset(centerX - 6f, drawY + h - 12f), size = Size(5f, 9f), alpha = alphaVal)
            drawRect(color = Color(0xFFA855F7), topLeft = Offset(centerX + 1f, drawY + h - 14f), size = Size(5f, 9f), alpha = alphaVal)
        }
        PlayerAnimState.FALL -> {
            // Extended leg pose for falling
            drawRect(color = Color(0xFF38BDF8), topLeft = Offset(centerX - 7f, drawY + h - 6f), size = Size(5f, 10f), alpha = alphaVal)
            drawRect(color = Color(0xFFA855F7), topLeft = Offset(centerX + 2f, drawY + h - 6f), size = Size(5f, 10f), alpha = alphaVal)
        }
        else -> {
            // Default stance
            drawRect(color = Color(0xFF1E1038), topLeft = Offset(centerX - 7f, drawY + h - 8f), size = Size(5f, 8f), alpha = alphaVal)
            drawRect(color = Color(0xFF1E1038), topLeft = Offset(centerX + 2f, drawY + h - 8f), size = Size(5f, 8f), alpha = alphaVal)
        }
    }

    // Restore Canvas State
    drawContext.canvas.restore()
}

/**
 * Draws Dash Ghost Trails behind the player.
 */
fun DrawScope.drawDashGhostTrails(
    ghosts: List<DashGhostFrame>,
    cameraX: Float,
    cameraY: Float,
    playerWidth: Float,
    playerHeight: Float
) {
    for (ghost in ghosts) {
        val gx = ghost.x - cameraX
        val gy = ghost.y - cameraY

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFA855F7).copy(alpha = ghost.alpha * 0.7f),
                    Color(0xFF38BDF8).copy(alpha = ghost.alpha * 0.3f),
                    Color.Transparent
                ),
                center = Offset(gx + playerWidth / 2f, gy + playerHeight / 2f),
                radius = playerWidth * 1.2f
            ),
            center = Offset(gx + playerWidth / 2f, gy + playerHeight / 2f),
            radius = playerWidth * 1.2f
        )
    }
}

/**
 * Draws all particle effects (dash sparks, jump dust, wall slide dust).
 */
fun DrawScope.drawHeroParticles(
    particles: List<HeroParticle>,
    cameraX: Float,
    cameraY: Float
) {
    for (p in particles) {
        val px = p.x - cameraX
        val py = p.y - cameraY
        drawCircle(
            color = p.color.copy(alpha = p.alpha),
            center = Offset(px, py),
            radius = p.radius
        )
    }
}

/**
 * Draws particle effects and floating feedback text items.
 */
fun DrawScope.drawHeroParticlesAndFloatingTexts(
    particles: List<HeroParticle>,
    floatingTexts: List<FloatingText>,
    cameraX: Float,
    cameraY: Float
) {
    for (p in particles) {
        val px = p.x - cameraX
        val py = p.y - cameraY
        drawCircle(
            color = p.color.copy(alpha = p.alpha),
            center = Offset(px, py),
            radius = p.radius
        )
    }

    for (ft in floatingTexts) {
        val fx = ft.x - cameraX
        val fy = ft.y - cameraY
        val alpha = (ft.life / ft.maxLife).coerceIn(0f, 1f)

        // Text aura glow dot
        drawCircle(
            color = ft.color.copy(alpha = alpha * 0.7f),
            center = Offset(fx, fy),
            radius = 6f
        )
    }
}

/**
 * Draws Checkpoint Totems in the level.
 */
fun DrawScope.drawLevelCheckpoints(checkpoints: List<LevelCheckpoint>, animTime: Float) {
    for (cp in checkpoints) {
        val cx = cp.x + cp.width / 2f
        val cy = cp.y + cp.height / 2f

        // Base Pedestal
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    if (cp.isActivated) Color(0xFF38BDF8) else Color(0xFFA855F7),
                    Color(0xFF1E1038)
                )
            ),
            topLeft = Offset(cp.x, cp.y + cp.height - 12f),
            size = Size(cp.width, 12f)
        )

        // Pillar Stem
        drawRect(
            color = Color(0xFF0F0721),
            topLeft = Offset(cx - 6f, cp.y + 16f),
            size = Size(12f, cp.height - 28f)
        )

        // Top Orb
        val orbRadius = 14f
        val orbColor = if (cp.isActivated) Color(0xFF38BDF8) else Color(0xFFA855F7)
        val auraAlpha = if (cp.isActivated) 0.6f + sin(animTime * 6f) * 0.2f else 0.3f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(orbColor.copy(alpha = auraAlpha), Color.Transparent),
                center = Offset(cx, cp.y + 16f),
                radius = 32f
            ),
            center = Offset(cx, cp.y + 16f),
            radius = 32f
        )

        drawCircle(
            color = orbColor,
            center = Offset(cx, cp.y + 16f),
            radius = orbRadius
        )
    }
}

/**
 * Draws floating Energy Crystal Collectibles in the level.
 */
fun DrawScope.drawLevelEnergyCrystals(crystals: List<LevelEnergyCrystal>, animTime: Float) {
    for (crystal in crystals) {
        if (crystal.isCollected) continue

        val floatOffset = sin(animTime * 4f + crystal.x * 0.05f) * 6f
        val cx = crystal.x
        val cy = crystal.y + floatOffset
        val r = crystal.radius

        val crystalColor = if (crystal.isBonusRoute) Color(0xFFEC4899) else Color(0xFFFFD700)

        // Outer Glow Aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(crystalColor.copy(alpha = 0.5f), Color.Transparent),
                center = Offset(cx, cy),
                radius = r * 2.2f
            ),
            center = Offset(cx, cy),
            radius = r * 2.2f
        )

        // Rhombus Diamond Shape
        val path = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r * 0.8f, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r * 0.8f, cy)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White, crystalColor, Color(0xFFB45309))
            )
        )
    }
}

/**
 * Draws the Exit Portal at the end of the stage.
 */
fun DrawScope.drawLevelExitPortal(portal: LevelExitPortal, animTime: Float) {
    val cx = portal.x
    val cy = portal.y
    val r = portal.radius

    val pulse = sin(animTime * 3f) * 8f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF38BDF8).copy(alpha = 0.45f),
                Color(0xFFA855F7).copy(alpha = 0.25f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = (r + 30f + pulse)
        ),
        center = Offset(cx, cy),
        radius = r + 30f + pulse
    )

    // Rotating Vortex Rings
    for (i in 0..2) {
        val ringR = r * (0.5f + i * 0.35f)
        val strokeW = 4f + i * 1.5f
        val color = if (i % 2 == 0) Color(0xFF38BDF8) else Color(0xFFA855F7)

        drawCircle(
            color = color,
            center = Offset(cx, cy),
            radius = ringR,
            style = Stroke(width = strokeW)
        )
    }

    // Portal Inner Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFF38BDF8), Color(0xFF0F0721)),
            center = Offset(cx, cy),
            radius = r * 0.5f
        ),
        center = Offset(cx, cy),
        radius = r * 0.5f
    )
}

/**
 * Draws Static Metallic Spikes with neon hazard tips.
 */
fun DrawScope.drawLevelSpikes(spikes: List<LevelSpike>) {
    for (spike in spikes) {
        val path = Path().apply {
            moveTo(spike.x, spike.y + spike.height)
            lineTo(spike.x + spike.width / 2f, spike.y)
            lineTo(spike.x + spike.width, spike.y + spike.height)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFEF4444), Color(0xFF7F1D1D), Color(0xFF1E1038))
            )
        )
        // Red neon glow tip
        drawCircle(
            color = Color(0xFFF87171),
            center = Offset(spike.x + spike.width / 2f, spike.y + 3f),
            radius = 3f
        )
    }
}

/**
 * Draws Moving Spikes with direction indicators.
 */
fun DrawScope.drawLevelMovingSpikes(movingSpikes: List<LevelMovingSpike>) {
    for (mSpike in movingSpikes) {
        val cx = mSpike.currentX
        val cy = mSpike.currentY
        val r = mSpike.width / 2f

        // Outer Hazard Ring
        drawCircle(
            color = Color(0xFFDC2626).copy(alpha = 0.35f),
            center = Offset(cx, cy),
            radius = r + 6f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFEF4444), Color(0xFF991B1B), Color(0xFF0F0721)),
                center = Offset(cx, cy),
                radius = r
            ),
            center = Offset(cx, cy),
            radius = r
        )

        // Spike Teeth around perimeter
        val teethCount = 6
        for (i in 0 until teethCount) {
            val angle = (Math.PI * 2 / teethCount) * i
            val tx = cx + cos(angle).toFloat() * (r + 8f)
            val ty = cy + sin(angle).toFloat() * (r + 8f)
            drawLine(
                color = Color(0xFFF87171),
                start = Offset(cx, cy),
                end = Offset(tx, ty),
                strokeWidth = 3f
            )
        }
    }
}

/**
 * Draws Rotating Saw Blades with neon trail glow.
 */
fun DrawScope.drawLevelBlades(blades: List<LevelRotatingBlade>) {
    for (blade in blades) {
        val cx = blade.centerX
        val cy = blade.centerY
        val r = blade.radius

        // Outer Glow Aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFEF4444).copy(alpha = 0.5f), Color.Transparent),
                center = Offset(cx, cy),
                radius = r * 1.6f
            ),
            center = Offset(cx, cy),
            radius = r * 1.6f
        )

        drawCircle(
            color = Color(0xFF27272A),
            center = Offset(cx, cy),
            radius = r
        )

        // Rotating Blade Teeth
        val teeth = 8
        for (i in 0 until teeth) {
            val a = Math.toRadians((blade.currentAngle + i * (360f / teeth)).toDouble())
            val tx = cx + cos(a).toFloat() * (r + 10f)
            val ty = cy + sin(a).toFloat() * (r + 10f)
            drawLine(
                color = Color(0xFFF87171),
                start = Offset(cx, cy),
                end = Offset(tx, ty),
                strokeWidth = 4f
            )
        }

        // Center Axle
        drawCircle(
            color = Color(0xFFFACC15),
            center = Offset(cx, cy),
            radius = 6f
        )
    }
}

/**
 * Draws Laser Beams with Warning Line -> Active Beam animation cycle.
 */
fun DrawScope.drawLevelLasers(lasers: List<LevelLaserBeam>) {
    for (laser in lasers) {
        when (laser.state) {
            LaserState.WARNING -> {
                // Faint yellow/red warning guide line with pulse
                val alpha = (0.3f + sin(laser.cycleTimer * 20f) * 0.25f).coerceIn(0.1f, 0.9f)
                drawLine(
                    color = Color(0xFFFACC15).copy(alpha = alpha),
                    start = Offset(laser.startX, laser.startY),
                    end = Offset(laser.endX, laser.endY),
                    strokeWidth = 3f
                )
            }

            LaserState.ACTIVE -> {
                // Outer Cyan/Purple Laser Energy Beam
                drawLine(
                    color = Color(0xFF06B6D4).copy(alpha = 0.5f),
                    start = Offset(laser.startX, laser.startY),
                    end = Offset(laser.endX, laser.endY),
                    strokeWidth = 14f,
                    cap = StrokeCap.Round
                )
                // White Core Laser Beam
                drawLine(
                    color = Color.White,
                    start = Offset(laser.startX, laser.startY),
                    end = Offset(laser.endX, laser.endY),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
            }

            LaserState.INACTIVE -> {
                // Emitters
                drawCircle(color = Color(0xFF3F3F46), center = Offset(laser.startX, laser.startY), radius = 8f)
                drawCircle(color = Color(0xFF3F3F46), center = Offset(laser.endX, laser.endY), radius = 8f)
            }
        }
    }
}

/**
 * Draws Environmental Hazards (Lava floor or Electric floor).
 */
fun DrawScope.drawLevelHazards(hazards: List<LevelEnvHazard>, animTime: Float) {
    for (hz in hazards) {
        val b = hz.bounds
        when (hz.type) {
            EnvHazardType.LAVA -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFEF4444), Color(0xFFB91C1C), Color(0xFF450A0A))
                    ),
                    topLeft = Offset(b.left, b.top),
                    size = Size(b.width, b.height)
                )
            }
            EnvHazardType.ELECTRIC_FLOOR -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF06B6D4), Color(0xFF1E3A8A), Color(0xFF0F172A))
                    ),
                    topLeft = Offset(b.left, b.top),
                    size = Size(b.width, b.height)
                )
            }
            EnvHazardType.POISON_MIST -> {
                drawRect(
                    color = Color(0xFF10B981).copy(alpha = 0.45f),
                    topLeft = Offset(b.left, b.top),
                    size = Size(b.width, b.height)
                )
            }
        }
    }
}

/**
 * Draws AI Enemies across all 17 Ground, Flying, and Boss/Elite enemy types.
 */
fun DrawScope.drawLevelEnemies(enemies: List<LevelEnemy>, animTime: Float) {
    for (enemy in enemies) {
        if (enemy.state == EnemyState.DEATH) continue

        val type = enemy.effectiveType
        val cx = enemy.x + enemy.width / 2f
        val cy = enemy.y + enemy.height / 2f
        val facingRight = enemy.facingRight
        val dirSign = if (facingRight) 1f else -1f
        val isHurt = enemy.state == EnemyState.HURT || enemy.state == EnemyState.STUN

        // Ground Slam shockwave ripple effect
        if (enemy.groundSlamRadius > 0f && enemy.groundSlamRadius < 180f) {
            val slamX = cx
            val slamY = enemy.y + enemy.height
            drawCircle(
                color = Color(0xFFA855F7).copy(alpha = (1f - enemy.groundSlamRadius / 180f).coerceIn(0f, 1f)),
                center = Offset(slamX, slamY),
                radius = enemy.groundSlamRadius,
                style = Stroke(width = 4.dp.toPx())
            )
        }

        // Shadow Drone Laser Warning Line
        if (type == EnemyType.SHADOW_DRONE && enemy.laserWarningTimer > 0f) {
            drawLine(
                color = Color(0xFFEF4444).copy(alpha = 0.85f),
                start = Offset(cx, cy),
                end = Offset(cx + dirSign * 400f, cy),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        when (type) {
            // =========================================
            // 1. SHADOW_WARRIOR (Basic Melee Sword)
            // =========================================
            EnemyType.SHADOW_WARRIOR -> {
                // Scarf/Cape
                val capeOffset = sin(animTime * 8f) * 6f
                drawPath(
                    path = Path().apply {
                        moveTo(cx - dirSign * 6f, cy - 8f)
                        lineTo(cx - dirSign * 22f, cy + 10f + capeOffset)
                        lineTo(cx - dirSign * 12f, cy + 18f)
                        close()
                    },
                    color = Color(0xFF581C87)
                )
                // Dark body
                drawRoundRect(
                    color = Color(0xFF0F0721),
                    topLeft = Offset(cx - 14f, cy - 16f),
                    size = Size(28f, 32f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Horned crown helm
                drawPath(
                    path = Path().apply {
                        moveTo(cx - 12f, cy - 14f)
                        lineTo(cx - 16f, cy - 26f)
                        lineTo(cx - 6f, cy - 18f)
                        lineTo(cx, cy - 28f)
                        lineTo(cx + 6f, cy - 18f)
                        lineTo(cx + 16f, cy - 26f)
                        lineTo(cx + 12f, cy - 14f)
                        close()
                    },
                    color = Color(0xFF1E1035)
                )
                // Glowing eyes
                val eyeX = cx + dirSign * 5f
                drawCircle(color = Color(0xFF22D3EE), center = Offset(eyeX, cy - 10f), radius = 3.5f)
                drawCircle(color = Color.White, center = Offset(eyeX, cy - 10f), radius = 1.8f)

                // Sword
                val swordAngle = if (enemy.state == EnemyState.ATTACK) dirSign * 45f else dirSign * 15f
                rotate(degrees = swordAngle, pivot = Offset(cx + dirSign * 10f, cy)) {
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(Color(0xFFE879F9), Color(0xFFA855F7))),
                        topLeft = Offset(cx + dirSign * 10f, cy - 18f),
                        size = Size(dirSign * 22f, 4f)
                    )
                }
            }

            // =========================================
            // 2. SPIKE_BEAST (Ground Charger)
            // =========================================
            EnemyType.SPIKE_BEAST -> {
                val bodyColor = if (enemy.state == EnemyState.CHASE) Color(0xFF701A75) else Color(0xFF2E1065)
                // Quadruped Body
                drawRoundRect(
                    color = bodyColor,
                    topLeft = Offset(cx - 22f, cy - 12f),
                    size = Size(44f, 24f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                // Sharp Spikes on Back
                for (i in 0..4) {
                    val spikeX = cx - 18f + i * 9f
                    drawPath(
                        path = Path().apply {
                            moveTo(spikeX - 4f, cy - 12f)
                            lineTo(spikeX, cy - 26f)
                            lineTo(spikeX + 4f, cy - 12f)
                            close()
                        },
                        color = Color(0xFFE879F9)
                    )
                }
                // Eyes & Fangs
                val eyeX = cx + dirSign * 14f
                drawCircle(color = Color(0xFFF43F5E), center = Offset(eyeX, cy - 4f), radius = 4f)
                drawCircle(color = Color.White, center = Offset(eyeX, cy - 4f), radius = 2f)
            }

            // =========================================
            // 3. SHADOW_BRUTE (Heavy Slammer)
            // =========================================
            EnemyType.SHADOW_BRUTE -> {
                // Large obsidian torso
                drawRoundRect(
                    color = Color(0xFF180E29),
                    topLeft = Offset(cx - 24f, cy - 24f),
                    size = Size(48f, 48f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // Glowing purple energy cracks on chest
                drawLine(
                    color = Color(0xFFC084FC),
                    start = Offset(cx - 10f, cy - 10f),
                    end = Offset(cx + 8f, cy + 12f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color(0xFFE879F9),
                    start = Offset(cx + 10f, cy - 8f),
                    end = Offset(cx - 6f, cy + 10f),
                    strokeWidth = 2.5f
                )
                // Glowing eyes
                val eyeX = cx + dirSign * 8f
                drawCircle(color = Color(0xFFC084FC), center = Offset(eyeX, cy - 16f), radius = 4.5f)
                // Heavy fists
                drawCircle(color = Color(0xFF3B0764), center = Offset(cx - dirSign * 18f, cy + 10f), radius = 10f)
                drawCircle(color = Color(0xFF581C87), center = Offset(cx + dirSign * 18f, cy + 10f), radius = 10f)
            }

            // =========================================
            // 4. LAVA_GOLEM (Molten Rock)
            // =========================================
            EnemyType.LAVA_GOLEM -> {
                // Molten body
                drawRoundRect(
                    color = Color(0xFF451A03),
                    topLeft = Offset(cx - 25f, cy - 25f),
                    size = Size(50f, 50f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // Lava veins
                for (i in 0..3) {
                    val lavaY = cy - 15f + i * 10f
                    drawLine(
                        color = Color(0xFFF97316),
                        start = Offset(cx - 18f, lavaY),
                        end = Offset(cx + 18f, lavaY + 4f),
                        strokeWidth = 3.5f
                    )
                }
                // Fiery Skull Eyes
                val eyeX = cx + dirSign * 10f
                drawCircle(color = Color(0xFFFACC15), center = Offset(eyeX, cy - 14f), radius = 5f)
                drawCircle(color = Color(0xFFEF4444), center = Offset(eyeX, cy - 14f), radius = 2.5f)
            }

            // =========================================
            // 5. NIGHT_ARCHER (Ranged Bow)
            // =========================================
            EnemyType.NIGHT_ARCHER -> {
                // Hooded silhouette
                drawPath(
                    path = Path().apply {
                        moveTo(cx - 14f, cy - 22f)
                        lineTo(cx, cy - 28f)
                        lineTo(cx + 14f, cy - 22f)
                        lineTo(cx + 12f, cy + 18f)
                        lineTo(cx - 12f, cy + 18f)
                        close()
                    },
                    color = Color(0xFF022C22)
                )
                // Glowing eyes
                val eyeX = cx + dirSign * 5f
                drawCircle(color = Color(0xFF34D399), center = Offset(eyeX, cy - 12f), radius = 3.5f)
                // Bow
                val bowX = cx + dirSign * 14f
                drawArc(
                    color = Color(0xFFC084FC),
                    startAngle = if (facingRight) -90f else 90f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(bowX - 8f, cy - 18f),
                    size = Size(16f, 36f),
                    style = Stroke(width = 3.5f)
                )
            }

            // =========================================
            // 6. SHIELD_GUARD (Frontal Shield)
            // =========================================
            EnemyType.SHIELD_GUARD -> {
                // Horned Knight body
                drawRoundRect(
                    color = Color(0xFF1E1B4B),
                    topLeft = Offset(cx - 16f, cy - 20f),
                    size = Size(32f, 40f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Glowing eyes
                val eyeX = cx + dirSign * 4f
                drawCircle(color = Color(0xFF818CF8), center = Offset(eyeX, cy - 10f), radius = 3.5f)

                // Large Tower Shield
                val shieldX = cx + dirSign * 12f
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF3B0764))),
                    topLeft = Offset(shieldX - 8f, cy - 22f),
                    size = Size(16f, 44f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // Shield Emblem
                drawCircle(color = Color(0xFFE879F9), center = Offset(shieldX, cy), radius = 4f)
            }

            // =========================================
            // 7. DARK_BAT (Flying Bat)
            // =========================================
            EnemyType.DARK_BAT -> {
                val wingFlap = sin(animTime * 15f) * 12f
                // Bat body
                drawCircle(color = Color(0xFF180828), center = Offset(cx, cy), radius = 10f)
                // Wings
                drawPath(
                    path = Path().apply {
                        moveTo(cx, cy)
                        lineTo(cx - 24f, cy - 10f + wingFlap)
                        lineTo(cx - 12f, cy + 8f)
                        close()
                    },
                    color = Color(0xFF581C87)
                )
                drawPath(
                    path = Path().apply {
                        moveTo(cx, cy)
                        lineTo(cx + 24f, cy - 10f + wingFlap)
                        lineTo(cx + 12f, cy + 8f)
                        close()
                    },
                    color = Color(0xFF581C87)
                )
                // Eyes
                val eyeX = cx + dirSign * 3f
                drawCircle(color = Color(0xFFF43F5E), center = Offset(eyeX, cy - 2f), radius = 3f)
            }

            // =========================================
            // 8. VOID_FLYER (Flying Orb Caster)
            // =========================================
            EnemyType.VOID_FLYER -> {
                val floatY = cy + sin(animTime * 5f) * 4f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFA855F7), Color(0xFF3B0764), Color.Transparent),
                        center = Offset(cx, floatY),
                        radius = enemy.width
                    ),
                    center = Offset(cx, floatY),
                    radius = enemy.width
                )
                drawCircle(color = Color(0xFFE879F9), center = Offset(cx, floatY), radius = 8f)
                drawCircle(color = Color.White, center = Offset(cx, floatY), radius = 4f)
            }

            // =========================================
            // 9. SKULL_HAWK (Horned Skull Bird)
            // =========================================
            EnemyType.SKULL_HAWK -> {
                val wingFlap = sin(animTime * 10f) * 10f
                // Dark Bird Body
                drawCircle(color = Color(0xFF020617), center = Offset(cx, cy), radius = 12f)
                // Feather Wings
                drawPath(
                    path = Path().apply {
                        moveTo(cx, cy)
                        lineTo(cx - 26f, cy - 14f + wingFlap)
                        lineTo(cx - 8f, cy + 6f)
                        close()
                    },
                    color = Color(0xFF334155)
                )
                drawPath(
                    path = Path().apply {
                        moveTo(cx, cy)
                        lineTo(cx + 26f, cy - 14f + wingFlap)
                        lineTo(cx + 8f, cy + 6f)
                        close()
                    },
                    color = Color(0xFF334155)
                )
                // Horned Skull Mask
                drawCircle(color = Color(0xFFE2E8F0), center = Offset(cx + dirSign * 4f, cy - 2f), radius = 7f)
                drawCircle(color = Color(0xFFF43F5E), center = Offset(cx + dirSign * 6f, cy - 3f), radius = 2.5f)
            }

            // =========================================
            // 10. FLOATING_MAGE (Teleport Caster)
            // =========================================
            EnemyType.FLOATING_MAGE -> {
                val mageY = cy + sin(animTime * 4f) * 6f
                // Robe
                drawPath(
                    path = Path().apply {
                        moveTo(cx, mageY - 22f)
                        lineTo(cx - 16f, mageY + 18f)
                        lineTo(cx + 16f, mageY + 18f)
                        close()
                    },
                    color = Color(0xFF3B0764)
                )
                // Staff
                val staffX = cx + dirSign * 16f
                drawLine(
                    color = Color(0xFF78350F),
                    start = Offset(staffX, mageY - 24f),
                    end = Offset(staffX, mageY + 18f),
                    strokeWidth = 3f
                )
                // Staff Orb
                drawCircle(color = Color(0xFF22D3EE), center = Offset(staffX, mageY - 24f), radius = 6f)
                drawCircle(color = Color.White, center = Offset(staffX, mageY - 24f), radius = 3f)

                // Hood Eyes
                val eyeX = cx + dirSign * 3f
                drawCircle(color = Color(0xFFE879F9), center = Offset(eyeX, mageY - 10f), radius = 3.5f)
            }

            // =========================================
            // 11. SHADOW_DRONE (Mechanical Drone)
            // =========================================
            EnemyType.SHADOW_DRONE -> {
                val droneY = cy + sin(animTime * 8f) * 4f
                // Metallic X Frame
                drawLine(color = Color(0xFF475569), start = Offset(cx - 16f, droneY - 16f), end = Offset(cx + 16f, droneY + 16f), strokeWidth = 3f)
                drawLine(color = Color(0xFF475569), start = Offset(cx - 16f, droneY + 16f), end = Offset(cx + 16f, droneY - 16f), strokeWidth = 3f)
                // Rotors
                for (rotX in listOf(cx - 16f, cx + 16f)) {
                    for (rotY in listOf(droneY - 16f, droneY + 16f)) {
                        drawCircle(color = Color(0xFF0F172A), center = Offset(rotX, rotY), radius = 5f)
                    }
                }
                // Central Optic Eye
                drawCircle(color = Color(0xFF1E293B), center = Offset(cx, droneY), radius = 10f)
                val opticColor = if (enemy.laserWarningTimer > 0f) Color(0xFFEF4444) else Color(0xFFA855F7)
                drawCircle(color = opticColor, center = Offset(cx, droneY), radius = 5f)
            }

            // =========================================
            // 12. VOID_JELLY (Floating Jellyfish)
            // =========================================
            EnemyType.VOID_JELLY -> {
                // Neon Jelly Dome
                drawArc(
                    brush = Brush.radialGradient(listOf(Color(0xFFE879F9), Color(0xFF581C87))),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cx - 18f, cy - 18f),
                    size = Size(36f, 36f)
                )
                // Tentacles
                for (i in 0..4) {
                    val tentX = cx - 12f + i * 6f
                    val tentWave = sin(animTime * 6f + i) * 4f
                    drawLine(
                        color = Color(0xFFC084FC),
                        start = Offset(tentX, cy),
                        end = Offset(tentX + tentWave, cy + 18f),
                        strokeWidth = 2.5f
                    )
                }
            }

            // =========================================
            // 13. SHADOW_KNIGHT (Elite Boss)
            // =========================================
            EnemyType.SHADOW_KNIGHT -> {
                // Heavy Horned Armor
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF312E81), Color(0xFF0F172A))),
                    topLeft = Offset(cx - 20f, cy - 24f),
                    size = Size(40f, 48f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Horns
                drawPath(
                    path = Path().apply {
                        moveTo(cx - 10f, cy - 24f)
                        lineTo(cx - 22f, cy - 38f)
                        lineTo(cx - 4f, cy - 26f)
                        close()
                    },
                    color = Color(0xFFEC4899)
                )
                drawPath(
                    path = Path().apply {
                        moveTo(cx + 10f, cy - 24f)
                        lineTo(cx + 22f, cy - 38f)
                        lineTo(cx + 4f, cy - 26f)
                        close()
                    },
                    color = Color(0xFFEC4899)
                )
                // Visor Eyes
                val eyeX = cx + dirSign * 6f
                drawRect(color = Color(0xFFEC4899), topLeft = Offset(eyeX - 4f, cy - 14f), size = Size(10f, 3f))

                // Massive Broadsword
                val swordX = cx + dirSign * 18f
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFFF472B6), Color(0xFFA855F7))),
                    topLeft = Offset(swordX - 3f, cy - 28f),
                    size = Size(6f, 44f)
                )
            }

            // =========================================
            // 14. CORRUPTED_BEAST (Elite Area Slammer)
            // =========================================
            EnemyType.CORRUPTED_BEAST -> {
                // Large Quadruped
                drawRoundRect(
                    color = Color(0xFF2E1065),
                    topLeft = Offset(cx - 26f, cy - 18f),
                    size = Size(52f, 36f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // Back Spikes
                for (i in 0..5) {
                    val spikeX = cx - 22f + i * 8f
                    drawPath(
                        path = Path().apply {
                            moveTo(spikeX - 3f, cy - 18f)
                            lineTo(spikeX, cy - 32f)
                            lineTo(spikeX + 3f, cy - 18f)
                            close()
                        },
                        color = Color(0xFFEC4899)
                    )
                }
                // Eyes & Fangs
                val eyeX = cx + dirSign * 18f
                drawCircle(color = Color(0xFFF43F5E), center = Offset(eyeX, cy - 6f), radius = 5f)
            }

            // =========================================
            // 15. VOID_OVERLORD (Floating Boss)
            // =========================================
            EnemyType.VOID_OVERLORD -> {
                val bossY = cy + sin(animTime * 3f) * 8f
                // Robed Body
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF581C87), Color(0xFF0F0721))),
                    topLeft = Offset(cx - 22f, bossY - 26f),
                    size = Size(44f, 52f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                // Crown
                drawPath(
                    path = Path().apply {
                        moveTo(cx - 18f, bossY - 26f)
                        lineTo(cx - 22f, bossY - 40f)
                        lineTo(cx - 10f, bossY - 32f)
                        lineTo(cx, bossY - 44f)
                        lineTo(cx + 10f, bossY - 32f)
                        lineTo(cx + 22f, bossY - 40f)
                        lineTo(cx + 18f, bossY - 26f)
                        close()
                    },
                    color = Color(0xFFC084FC)
                )
                // 4 Orbiting Void Orbs
                for (i in 0..3) {
                    val orbAngle = animTime * 2.5f + i * (PI / 2.0)
                    val orbX = cx + cos(orbAngle).toFloat() * 32f
                    val orbY = bossY + sin(orbAngle).toFloat() * 18f
                    drawCircle(color = Color(0xFFE879F9), center = Offset(orbX, orbY), radius = 6f)
                }
                // Eyes
                val eyeX = cx + dirSign * 5f
                drawCircle(color = Color(0xFFF472B6), center = Offset(eyeX, bossY - 14f), radius = 4f)
            }

            // =========================================
            // 16. LAVA_TITAN (Giant Titan Boss)
            // =========================================
            EnemyType.LAVA_TITAN -> {
                // Massive Lava Torso
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF7C2D12), Color(0xFF180802))),
                    topLeft = Offset(cx - 30f, cy - 32f),
                    size = Size(60f, 64f),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                // Lava Core Glow
                drawCircle(color = Color(0xFFF97316), center = Offset(cx, cy), radius = 16f)
                drawCircle(color = Color(0xFFFACC15), center = Offset(cx, cy), radius = 8f)
                // Burning Eyes
                val eyeX = cx + dirSign * 10f
                drawCircle(color = Color(0xFFEF4444), center = Offset(eyeX, cy - 20f), radius = 5f)
            }

            // =========================================
            // 17. SHADOW_WORM (Subterranean Boss)
            // =========================================
            EnemyType.SHADOW_WORM -> {
                if (enemy.burrowDepth < 0.9f) {
                    val wormHeight = enemy.height * (1f - enemy.burrowDepth)
                    val wormTopY = enemy.y + enemy.height * enemy.burrowDepth
                    // Segmented worm body emerging
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(Color(0xFF581C87), Color(0xFF0F0721))),
                        topLeft = Offset(cx - 20f, wormTopY),
                        size = Size(40f, wormHeight),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    // Fangs / Jaws
                    drawPath(
                        path = Path().apply {
                            moveTo(cx - 18f, wormTopY + 10f)
                            lineTo(cx - 8f, wormTopY - 8f)
                            lineTo(cx, wormTopY + 10f)
                            close()
                        },
                        color = Color(0xFFE879F9)
                    )
                    drawPath(
                        path = Path().apply {
                            moveTo(cx + 18f, wormTopY + 10f)
                            lineTo(cx + 8f, wormTopY - 8f)
                            lineTo(cx, wormTopY + 10f)
                            close()
                        },
                        color = Color(0xFFE879F9)
                    )
                }
            }

            else -> {}
        }

        // Apply Red Hurt Flash Overlay
        if (isHurt) {
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = 0.55f),
                center = Offset(cx, cy),
                radius = enemy.width / 2f + 2f
            )
        }

        // --- HEALTH BAR OVERLAY ---
        if (enemy.health < enemy.maxHealth && enemy.health > 0f) {
            val barWidth = (enemy.width * 1.2f).coerceAtLeast(30f)
            val barHeight = 6f
            val barX = cx - barWidth / 2f
            val barY = enemy.y - 14f

            drawRect(
                color = Color(0x7F111827),
                topLeft = Offset(barX, barY),
                size = Size(barWidth, barHeight)
            )
            val hpRatio = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)
            val barColor = when (type) {
                EnemyType.LAVA_GOLEM, EnemyType.LAVA_TITAN -> Color(0xFFF97316)
                EnemyType.SHIELD_GUARD, EnemyType.SHADOW_KNIGHT -> Color(0xFF818CF8)
                EnemyType.NIGHT_ARCHER, EnemyType.FLOATING_MAGE -> Color(0xFF34D399)
                EnemyType.DARK_BAT, EnemyType.VOID_FLYER -> Color(0xFFE879F9)
                else -> Color(0xFF22D3EE)
            }
            drawRect(
                color = barColor,
                topLeft = Offset(barX, barY),
                size = Size(barWidth * hpRatio, barHeight)
            )
        }

        // --- WARNING INDICATOR ---
        if (enemy.state == EnemyState.DETECT) {
            drawCircle(color = Color(0xFFEF4444), center = Offset(cx, enemy.y - 25f), radius = 6f)
            drawLine(
                color = Color.White,
                start = Offset(cx, enemy.y - 28f),
                end = Offset(cx, enemy.y - 23f),
                strokeWidth = 2.5f
            )
        }
    }
}

/**
 * Draws active combat slash effects from Shadow Strike with glowing pink/purple arcs.
 */
fun DrawScope.drawActiveSlashes(slashes: List<com.myplaywin.app.shadowhero.engine.SlashEffect>) {
    for (slash in slashes) {
        val pct = slash.lifeTime / slash.maxLife
        val alpha = pct.coerceIn(0f, 1f)
        val color = if (slash.isComboFinisher) {
            Color(0xFFEC4899).copy(alpha = alpha) // Pink crimson combo finisher
        } else {
            Color(0xFFA855F7).copy(alpha = alpha) // Purple standard slash
        }

        val arcWidth = slash.width
        val arcHeight = slash.height
        val startAngle = if (slash.facingRight) -80f else 100f
        val sweepAngle = 160f

        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(slash.x, slash.y),
            size = Size(arcWidth, arcHeight),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 8f + (1f - pct) * 6f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )

        drawArc(
            color = Color.White.copy(alpha = alpha * 0.7f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(slash.x + 2f, slash.y + 2f),
            size = Size(arcWidth - 4f, arcHeight - 4f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.5f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

/**
 * Draws Power-Up collectibles in the level with animated floating & neon aura.
 */
fun DrawScope.drawLevelPowerUps(powerUps: List<LevelPowerUp>, animTime: Float) {
    for (pu in powerUps) {
        if (pu.isCollected) continue

        val floatOffset = sin(animTime * 5f + pu.x * 0.08f) * 7f
        val cx = pu.x
        val cy = pu.y + floatOffset
        val r = pu.radius

        val (mainColor, glowColor) = when (pu.type) {
            PowerUpType.SHIELD -> Pair(Color(0xFF22D3EE), Color(0xFF0891B2))
            PowerUpType.DASH_BOOST, PowerUpType.DASH_RECHARGE -> Pair(Color(0xFFE879F9), Color(0xFFC084FC))
            PowerUpType.MAGNET, PowerUpType.CRYSTAL_MAGNET -> Pair(Color(0xFFFACC15), Color(0xFFD97706))
            PowerUpType.SLOW_TIME, PowerUpType.SHADOW_TIME -> Pair(Color(0xFF38BDF8), Color(0xFF0284C7))
            PowerUpType.DOUBLE_CRYSTAL, PowerUpType.ENERGY_BOOST -> Pair(Color(0xFF4ADE80), Color(0xFF16A34A))
        }

        // Outer Aura Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor.copy(alpha = 0.6f), Color.Transparent),
                center = Offset(cx, cy),
                radius = r * 2.2f
            ),
            center = Offset(cx, cy),
            radius = r * 2.2f
        )

        // Sphere Base
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, mainColor, Color(0xFF090314)),
                center = Offset(cx - 3f, cy - 3f),
                radius = r
            ),
            center = Offset(cx, cy),
            radius = r
        )

        // Outer Ring
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            center = Offset(cx, cy),
            radius = r,
            style = Stroke(width = 2f)
        )
    }
}

/**
 * Draws Shadow Shield energy sphere around player when shield is active.
 */
fun DrawScope.drawPlayerShield(
    drawX: Float,
    drawY: Float,
    playerWidth: Float,
    playerHeight: Float,
    animTime: Float
) {
    val cx = drawX + playerWidth / 2f
    val cy = drawY + playerHeight / 2f
    val radius = playerWidth * 1.35f
    val pulse = sin(animTime * 8f) * 3f

    // Radial Energy Sphere
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFC084FC).copy(alpha = 0.25f),
                Color(0xFF38BDF8).copy(alpha = 0.45f),
                Color(0xFFA855F7).copy(alpha = 0.7f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = radius + pulse
        ),
        center = Offset(cx, cy),
        radius = radius + pulse
    )

    // Glowing Neon Rim Ring
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(Color(0xFFE879F9), Color(0xFF38BDF8), Color(0xFFA855F7), Color(0xFFE879F9))
        ),
        center = Offset(cx, cy),
        radius = radius + pulse,
        style = Stroke(width = 3.5f)
    )
}

/**
 * Draws Enemy Energy Balls / Projectiles.
 */
fun DrawScope.drawEnemyProjectiles(projectiles: List<EnemyProjectile>) {
    for (p in projectiles) {
        when (p.type) {
            ProjectileType.ENERGY_ORB -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(p.color, Color(0xFF3B0764), Color.Transparent),
                        center = Offset(p.x, p.y),
                        radius = p.radius * 2.2f
                    ),
                    center = Offset(p.x, p.y),
                    radius = p.radius * 2.2f
                )
                drawCircle(color = Color.White, center = Offset(p.x, p.y), radius = p.radius * 0.6f)
            }
            ProjectileType.LAVA_BALL -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFACC15), Color(0xFFF97316), Color(0xFFEF4444), Color.Transparent),
                        center = Offset(p.x, p.y),
                        radius = p.radius * 2.5f
                    ),
                    center = Offset(p.x, p.y),
                    radius = p.radius * 2.5f
                )
                drawCircle(color = Color(0xFFFEF08A), center = Offset(p.x, p.y), radius = p.radius * 0.7f)
            }
            ProjectileType.ARROW -> {
                val angle = atan2(p.vy.toDouble(), p.vx.toDouble()).toFloat()
                rotate(degrees = Math.toDegrees(angle.toDouble()).toFloat(), pivot = Offset(p.x, p.y)) {
                    drawLine(
                        color = p.color,
                        start = Offset(p.x - 14f, p.y),
                        end = Offset(p.x + 14f, p.y),
                        strokeWidth = 4f
                    )
                    val path = Path().apply {
                        moveTo(p.x + 14f, p.y)
                        lineTo(p.x + 6f, p.y - 6f)
                        lineTo(p.x + 6f, p.y + 6f)
                        close()
                    }
                    drawPath(path = path, color = Color(0xFFE879F9))
                }
            }
            ProjectileType.MAGIC_BOLT -> {
                drawCircle(color = Color(0xFF22D3EE).copy(alpha = 0.4f), center = Offset(p.x, p.y), radius = p.radius * 2f)
                drawCircle(color = Color(0xFF67E8F9), center = Offset(p.x, p.y), radius = p.radius)
                drawCircle(color = Color.White, center = Offset(p.x, p.y), radius = p.radius * 0.5f)
            }
            ProjectileType.LASER_BEAM -> {
                drawLine(
                    color = Color(0xFFEF4444),
                    start = Offset(p.x - p.vx * 0.05f, p.y - p.vy * 0.05f),
                    end = Offset(p.x, p.y),
                    strokeWidth = 6f
                )
                drawCircle(color = Color.White, center = Offset(p.x, p.y), radius = 5f)
            }
            ProjectileType.VOID_BURST -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFC084FC), Color(0xFF581C87), Color.Transparent),
                        center = Offset(p.x, p.y),
                        radius = p.radius * 2.4f
                    ),
                    center = Offset(p.x, p.y),
                    radius = p.radius * 2.4f
                )
                drawCircle(color = Color(0xFFF472B6), center = Offset(p.x, p.y), radius = p.radius * 0.8f)
            }
            ProjectileType.FIRE_ERUPTION -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEF4444), Color(0xFF7C2D12), Color.Transparent),
                        center = Offset(p.x, p.y),
                        radius = p.radius * 2.8f
                    ),
                    center = Offset(p.x, p.y),
                    radius = p.radius * 2.8f
                )
            }
        }
    }
}

/**
 * Draws Multi-Layer Parallax Background matching the level theme and active biome transition & event states.
 */
fun DrawScope.drawParallaxBackground(
    theme: LevelTheme,
    cameraX: Float,
    cameraY: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    animTime: Float,
    biomeInfo: ChunkBiomeInfo? = null,
    eventDirector: ShadowHeroEventDirector? = null
) {
    if (viewportWidth <= 0f || viewportHeight <= 0f) return

    val primaryBg = biomeInfo?.effectivePrimaryBgColor ?: theme.primaryBgColor
    val secondaryBg = biomeInfo?.effectiveSecondaryBgColor ?: theme.secondaryBgColor
    val accentGlow = biomeInfo?.effectiveAccentGlowColor ?: theme.accentGlowColor

    val darknessFactor = if (eventDirector?.isDarknessActive == true) 0.35f else 1.0f

    // 1. Primary Base Gradient Layer
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                primaryBg.copy(alpha = primaryBg.alpha * darknessFactor),
                secondaryBg.copy(alpha = secondaryBg.alpha * darknessFactor),
                Color(0xFF030207)
            )
        ),
        size = Size(viewportWidth, viewportHeight)
    )

    // 2. Far Layer Parallax Structures (moving at 0.15x speed)
    val farShiftX = (cameraX * 0.15f) % 300f
    for (i in -1..((viewportWidth / 150f).toInt() + 2)) {
        val sx = i * 150f - farShiftX
        val sy = viewportHeight * 0.45f + sin(i * 1.5f + animTime * 0.5f) * 15f
        val pillarPath = Path().apply {
            moveTo(sx, viewportHeight)
            lineTo(sx + 30f, sy)
            lineTo(sx + 90f, sy - 20f)
            lineTo(sx + 120f, viewportHeight)
            close()
        }
        drawPath(
            path = pillarPath,
            color = secondaryBg.copy(alpha = 0.5f * darknessFactor)
        )
    }

    // 3. Ambient Theme Environmental Particles
    val particleCount = if (eventDirector?.isShadowStormActive == true) 56 else 28
    for (p in 0 until particleCount) {
        val pSeed = p * 1337f
        val speedX = when (theme.particleType) {
            AmbientParticleType.FALLING_SNOW -> sin(animTime + pSeed) * 20f
            AmbientParticleType.FLOATING_EMBERS -> cos(animTime * 1.5f + pSeed) * 30f
            AmbientParticleType.ELECTRICAL_SPARKS -> sin(animTime * 8f + pSeed) * 60f
            AmbientParticleType.WIND_CLOUDS -> 40f
            else -> sin(animTime * 0.8f + pSeed) * 25f
        }
        val speedY = when (theme.particleType) {
            AmbientParticleType.FALLING_SNOW -> 80f
            AmbientParticleType.FLOATING_EMBERS -> -60f
            AmbientParticleType.ELECTRICAL_SPARKS -> (sin(pSeed) * 40f)
            AmbientParticleType.VOID_FRAGMENTS -> -30f
            else -> 15f
        }

        val px = (pSeed * 37f + animTime * speedX - cameraX * 0.25f) % (viewportWidth + 100f)
        val finalPx = if (px < -50f) px + viewportWidth + 100f else px
        val py = (pSeed * 53f + animTime * speedY - cameraY * 0.25f) % (viewportHeight + 100f)
        val finalPy = if (py < -50f) py + viewportHeight + 100f else py

        val radius = when (theme.particleType) {
            AmbientParticleType.WIND_CLOUDS -> 16f
            AmbientParticleType.ELECTRICAL_SPARKS -> 2.5f
            else -> 3.5f + (p % 3)
        }

        val alpha = (0.35f + sin(animTime * 3f + p) * 0.2f).coerceIn(0.1f, 0.8f)
        val particleColor = if (eventDirector?.isShadowStormActive == true && p % 2 == 0) Color(0xFFA855F7) else accentGlow

        drawCircle(
            color = particleColor.copy(alpha = alpha * darknessFactor),
            center = Offset(finalPx, finalPy),
            radius = radius
        )
    }

    // 4. Near Foreground Parallax Pillars / Silhouettes (moving at 0.35x speed)
    val nearShiftX = (cameraX * 0.35f) % 400f
    for (i in -1..((viewportWidth / 200f).toInt() + 2)) {
        val nx = i * 200f - nearShiftX
        val ny = viewportHeight * 0.68f + sin(i * 2.2f) * 20f
        drawRect(
            color = primaryBg.copy(alpha = 0.65f * darknessFactor),
            topLeft = Offset(nx, ny),
            size = Size(60f, viewportHeight - ny)
        )
    }
}

/**
 * Draws Level Platforms styled matching the current level theme.
 */
fun DrawScope.drawThemedPlatforms(
    platforms: List<LevelPlatform>,
    theme: LevelTheme,
    animTime: Float
) {
    for (plat in platforms) {
        val b = plat.bounds
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    plat.color,
                    theme.platformColor,
                    Color(0xFF0A0518)
                )
            ),
            topLeft = Offset(b.left, b.top),
            size = Size(b.width, b.height)
        )

        // Top Neon Glow Rail
        val railColor1 = theme.platformBorderColor
        val railColor2 = theme.accentGlowColor
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    railColor1,
                    railColor2,
                    railColor1
                )
            ),
            start = Offset(b.left, b.top),
            end = Offset(b.right, b.top),
            strokeWidth = 3.5f
        )

        if (plat.isWall) {
            drawLine(
                color = railColor1.copy(alpha = 0.7f),
                start = Offset(b.left, b.top),
                end = Offset(b.left, b.bottom),
                strokeWidth = 2.5f
            )
            drawLine(
                color = railColor1.copy(alpha = 0.7f),
                start = Offset(b.right, b.top),
                end = Offset(b.right, b.bottom),
                strokeWidth = 2.5f
            )
        }
    }
}
