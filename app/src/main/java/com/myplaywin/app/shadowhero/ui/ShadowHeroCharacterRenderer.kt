package com.myplaywin.app.shadowhero.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.myplaywin.app.shadowhero.engine.AmbientParticleType
import com.myplaywin.app.shadowhero.engine.DashGhostFrame
import com.myplaywin.app.shadowhero.engine.EnemyProjectile
import com.myplaywin.app.shadowhero.engine.EnemyType
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the Shadow Hero on the Canvas at screen position (drawX, drawY).
 */
fun DrawScope.drawShadowHeroCharacter(
    player: ShadowHeroPlayer,
    drawX: Float,
    drawY: Float,
    animTime: Float
) {
    val w = player.width
    val h = player.height

    // Calculate squash & stretch matrix values
    var scaleX = if (player.facingRight) 1f else -1f
    var scaleY = 1f

    // Landing Squash
    if (player.landingSquashTimer > 0f) {
        scaleX *= 1.22f
        scaleY = 0.80f
    } else if (!player.isGrounded && player.vy < 0f) {
        // Jump Stretch
        scaleY = 1.15f
        scaleX *= 0.88f
    }

    val centerX = drawX + w / 2f
    val centerY = drawY + h / 2f

    // Save Canvas State
    val check = drawContext.canvas.save()

    // Apply Facing & Squash Pivot
    drawContext.transform.scale(
        scaleX = scaleX,
        scaleY = scaleY,
        pivot = Offset(centerX, drawY + h)
    )

    // 1. Ground Shadow (if near ground)
    if (player.isGrounded) {
        drawOval(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(centerX - w * 0.5f, drawY + h - 4f),
            size = Size(w, 8f)
        )
    }

    // 2. Purple Neon Energy Aura Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFA855F7).copy(alpha = 0.55f),
                Color(0xFF6D28D9).copy(alpha = 0.25f),
                Color.Transparent
            ),
            center = Offset(centerX, centerY - 4f),
            radius = w * 1.3f
        ),
        center = Offset(centerX, centerY - 4f),
        radius = w * 1.3f
    )

    // 3. Flowing Purple Scarf (Waving behind the hero)
    val runBounce = if (player.animState == PlayerAnimState.RUN) sin(player.runCycleTimer) * 4f else 0f
    val scarfWave1 = sin(animTime * 12f) * 8f
    val scarfWave2 = cos(animTime * 10f) * 12f
    val scarfDirection = -1f // Tail behind facing direction

    val scarfPath = Path().apply {
        moveTo(centerX, drawY + 16f + runBounce)
        cubicTo(
            centerX + scarfDirection * 15f, drawY + 18f + scarfWave1,
            centerX + scarfDirection * 30f, drawY + 22f + scarfWave2,
            centerX + scarfDirection * 45f, drawY + 28f + scarfWave1
        )
        lineTo(centerX + scarfDirection * 42f, drawY + 36f + scarfWave1)
        cubicTo(
            centerX + scarfDirection * 28f, drawY + 28f + scarfWave2,
            centerX + scarfDirection * 14f, drawY + 22f + scarfWave1,
            centerX, drawY + 20f + runBounce
        )
        close()
    }
    drawPath(
        path = scarfPath,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFE879F9), Color(0xFFA855F7), Color(0xFF6D28D9))
        )
    )

    // 4. Dark Ninja Warrior Body & Suit
    val bodyTop = drawY + 18f + runBounce
    val bodyBottom = drawY + h - 6f

    // Suit Body Fill
    drawRoundRect(
        color = Color(0xFF090314),
        topLeft = Offset(centerX - 13f, bodyTop),
        size = Size(26f, bodyBottom - bodyTop),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Violet Suit Outline Accent
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFC084FC), Color(0xFF6D28D9), Color(0xFF1E1038))
        ),
        topLeft = Offset(centerX - 13f, bodyTop),
        size = Size(26f, bodyBottom - bodyTop),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 2f)
    )

    // Glowing Chest Core Emblem (Circle/Crest on chest)
    val chestY = bodyTop + 10f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE879F9), Color(0xFFA855F7), Color.Transparent),
            center = Offset(centerX, chestY),
            radius = 7f
        ),
        center = Offset(centerX, chestY),
        radius = 7f
    )
    drawCircle(
        color = Color.White,
        center = Offset(centerX, chestY),
        radius = 2.5f
    )

    // 5. Spiky Shadow Head with Spiky Hair
    val headTop = drawY + runBounce - 6f
    val headPath = Path().apply {
        // Spiky hair tufts on top
        moveTo(centerX - 15f, headTop + 14f)
        lineTo(centerX - 20f, headTop + 2f)
        lineTo(centerX - 10f, headTop + 6f)
        lineTo(centerX - 5f, headTop - 6f)
        lineTo(centerX, headTop + 2f)
        lineTo(centerX + 6f, headTop - 8f)
        lineTo(centerX + 12f, headTop + 6f)
        lineTo(centerX + 21f, headTop + 4f)
        lineTo(centerX + 16f, headTop + 16f)
        // Chin curve
        cubicTo(centerX + 16f, headTop + 28f, centerX + 10f, headTop + 34f, centerX, headTop + 34f)
        cubicTo(centerX - 10f, headTop + 34f, centerX - 16f, headTop + 28f, centerX - 16f, headTop + 16f)
        close()
    }

    // Outer Head Fill
    drawPath(
        path = headPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF3B0764),
                Color(0xFF180828),
                Color(0xFF090314)
            )
        )
    )

    // Head Neon Purple Rim Light Accent
    drawPath(
        path = headPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE879F9), Color(0xFFA855F7), Color.Transparent)
        ),
        style = Stroke(width = 2.2f)
    )

    // 6. Vibrant Glowing Purple Eyes (As shown in reference image)
    val eyeY = headTop + 20f
    val eyeGlowRadius = 6.5f

    // Right Eye
    val eyeRightX = centerX + 5f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE879F9), Color(0xFFA855F7).copy(alpha = 0.4f), Color.Transparent),
            center = Offset(eyeRightX, eyeY),
            radius = eyeGlowRadius * 2.2f
        ),
        center = Offset(eyeRightX, eyeY),
        radius = eyeGlowRadius * 2.2f
    )
    drawCircle(color = Color(0xFFFAE8FF), center = Offset(eyeRightX, eyeY), radius = 3.2f)

    // Left Eye
    val eyeLeftX = centerX - 5f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE879F9), Color(0xFFA855F7).copy(alpha = 0.4f), Color.Transparent),
            center = Offset(eyeLeftX, eyeY),
            radius = eyeGlowRadius * 1.8f
        ),
        center = Offset(eyeLeftX, eyeY),
        radius = eyeGlowRadius * 1.8f
    )
    drawCircle(color = Color(0xFFFAE8FF), center = Offset(eyeLeftX, eyeY), radius = 2.6f)

    // 6. Running Legs Effect (if running)
    if (player.animState == PlayerAnimState.RUN) {
        val legOffset1 = sin(player.runCycleTimer) * 8f
        val legOffset2 = sin(player.runCycleTimer + Math.PI.toFloat()) * 8f

        drawRect(
            color = Color(0xFF1E1038),
            topLeft = Offset(centerX - 8f + legOffset1, drawY + h - 8f),
            size = Size(5f, 8f)
        )
        drawRect(
            color = Color(0xFF1E1038),
            topLeft = Offset(centerX + 3f + legOffset2, drawY + h - 8f),
            size = Size(5f, 8f)
        )
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
 * Draws AI Enemies (Shadow Walker, Flying Orb, Turret, Chaser).
 */
fun DrawScope.drawLevelEnemies(enemies: List<LevelEnemy>, animTime: Float) {
    for (enemy in enemies) {
        val cx = enemy.x + enemy.width / 2f
        val cy = enemy.y + enemy.height / 2f

        when (enemy.type) {
            EnemyType.SHADOW_WALKER -> {
                // Dark silhouette walker with glowing cyan eyes
                drawCircle(
                    color = Color(0xFF090314),
                    center = Offset(cx, cy),
                    radius = enemy.width / 2f
                )
                val eyeX = if (enemy.facingRight) cx + 6f else cx - 6f
                drawCircle(color = Color(0xFF22D3EE), center = Offset(eyeX, cy - 4f), radius = 3.5f)
            }

            EnemyType.FLYING_ORB -> {
                val floatY = cy + sin(animTime * 5f) * 6f
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
            }

            EnemyType.TURRET -> {
                // Mechanical Turret with red scanner eye
                drawRect(
                    color = Color(0xFF27272A),
                    topLeft = Offset(enemy.x, enemy.y),
                    size = Size(enemy.width, enemy.height)
                )
                val scannerColor = if (enemy.fireTimer < 0.3f) Color(0xFFEF4444) else Color(0xFFF59E0B)
                drawCircle(color = scannerColor, center = Offset(cx, enemy.y + 10f), radius = 5f)
            }

            EnemyType.CHASER -> {
                // Rapid dark stalker with purple trail
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFDC2626), Color(0xFF180828)),
                        center = Offset(cx, cy),
                        radius = enemy.width / 2f
                    ),
                    center = Offset(cx, cy),
                    radius = enemy.width / 2f
                )
                drawCircle(color = Color.White, center = Offset(cx, cy), radius = 4f)
            }
        }
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
            PowerUpType.SHIELD -> Pair(Color(0xFFC084FC), Color(0xFFA855F7))
            PowerUpType.SHADOW_TIME -> Pair(Color(0xFF38BDF8), Color(0xFF0284C7))
            PowerUpType.CRYSTAL_MAGNET -> Pair(Color(0xFFFACC15), Color(0xFFD97706))
            PowerUpType.ENERGY_BOOST -> Pair(Color(0xFF4ADE80), Color(0xFF16A34A))
            PowerUpType.DASH_RECHARGE -> Pair(Color(0xFFE879F9), Color(0xFFC084FC))
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
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFF87171), Color(0xFFDC2626), Color.Transparent),
                center = Offset(p.x, p.y),
                radius = p.radius * 2f
            ),
            center = Offset(p.x, p.y),
            radius = p.radius * 2f
        )
        drawCircle(
            color = Color.White,
            center = Offset(p.x, p.y),
            radius = p.radius * 0.5f
        )
    }
}

/**
 * Draws Multi-Layer Parallax Background matching the level theme.
 */
fun DrawScope.drawParallaxBackground(
    theme: LevelTheme,
    cameraX: Float,
    cameraY: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    animTime: Float
) {
    if (viewportWidth <= 0f || viewportHeight <= 0f) return

    // 1. Primary Base Gradient Layer
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                theme.primaryBgColor,
                theme.secondaryBgColor,
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
            color = theme.secondaryBgColor.copy(alpha = 0.5f)
        )
    }

    // 3. Ambient Theme Environmental Particles (Snow, Embers, Sparks, Crystal Dust, Wind, Shadows, Void)
    val particleCount = 28
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

        drawCircle(
            color = theme.accentGlowColor.copy(alpha = alpha),
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
            color = theme.primaryBgColor.copy(alpha = 0.65f),
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
