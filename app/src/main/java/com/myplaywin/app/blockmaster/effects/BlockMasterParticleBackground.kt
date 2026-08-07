package com.myplaywin.app.blockmaster.effects

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.myplaywin.app.blockmaster.world.BlockWorld
import com.myplaywin.app.blockmaster.world.WorldProgressionManager
import kotlin.math.sin
import kotlin.random.Random

private data class DynamicParticle(
    val id: Int,
    var xRatio: Float,
    var yRatio: Float,
    var radius: Float,
    var alpha: Float,
    val speedY: Float,
    val speedX: Float,
    val wobbleOffset: Float,
    val colorIndex: Int
)

@Composable
fun BlockMasterParticleBackground(
    world: BlockWorld = WorldProgressionManager.WORLDS.first(),
    drawParticlesOnly: Boolean = false,
    showParticles: Boolean = true,
    modifier: Modifier = Modifier
) {
    val animTopBg by animateColorAsState(
        targetValue = world.bgGradientTop,
        animationSpec = tween(durationMillis = 800),
        label = "bgTopAnim"
    )
    val animBottomBg by animateColorAsState(
        targetValue = world.bgGradientBottom,
        animationSpec = tween(durationMillis = 800),
        label = "bgBottomAnim"
    )

    // Dynamic particles tailored to active world biome
    val particles = remember(world.id) {
        val count = when (world.id) {
            2 -> 35 // Forest leaves
            3 -> 45 // Snowflakes
            4 -> 40 // Fire embers
            5 -> 30 // Electric ions
            6 -> 35 // Cyber neon
            else -> 25
        }
        List(count) { id ->
            DynamicParticle(
                id = id,
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                radius = when (world.id) {
                    2 -> Random.nextFloat() * 4f + 3f // Leaf size
                    3 -> Random.nextFloat() * 3f + 1.5f // Snow
                    4 -> Random.nextFloat() * 3.5f + 1f // Fire spark
                    5 -> Random.nextFloat() * 2.5f + 1f // Electric ion
                    6 -> Random.nextFloat() * 3f + 2f // Cyber pulse
                    else -> Random.nextFloat() * 3f + 1.5f
                },
                alpha = Random.nextFloat() * 0.45f + 0.2f,
                speedY = when (world.id) {
                    2 -> Random.nextFloat() * 0.0012f + 0.0004f // Leaf fall down
                    3 -> Random.nextFloat() * 0.0015f + 0.0005f // Snow fall down
                    4 -> -(Random.nextFloat() * 0.0020f + 0.0006f) // Fire embers float UP
                    5 -> -(Random.nextFloat() * 0.0025f + 0.0008f) // Ions float UP
                    6 -> Random.nextFloat() * 0.0018f + 0.0005f // Cyber stream
                    else -> -(Random.nextFloat() * 0.0008f + 0.0003f)
                },
                speedX = when (world.id) {
                    2 -> Random.nextFloat() * 0.0005f - 0.00025f // Leaf wind sway
                    3 -> Random.nextFloat() * 0.0004f - 0.0002f // Snow drift
                    4 -> Random.nextFloat() * 0.0006f - 0.0003f // Fire sway
                    else -> 0f
                },
                wobbleOffset = Random.nextFloat() * 6.28f,
                colorIndex = id % world.particleColors.size
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "particle_anim")
    val animState by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_step"
    )

    val modifierWithBg = if (drawParticlesOnly) {
        modifier.fillMaxSize()
    } else {
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(animTopBg, animBottomBg)
                )
            )
    }

    Box(
        modifier = modifierWithBg
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Biome Specific Environment Background Art & Glows
            if (!drawParticlesOnly) {
                when (world.id) {
                    1 -> { // Classic City - Soft Glowing Grid Points
                        val gridCols = 8
                        val gridRows = 14
                        val stepX = w / gridCols
                        val stepY = h / gridRows
                        for (c in 0..gridCols) {
                            for (r in 0..gridRows) {
                                drawCircle(
                                    color = world.accentColor.copy(alpha = 0.06f),
                                    radius = 1.8f,
                                    center = Offset(c * stepX, r * stepY)
                                )
                            }
                        }
                    }
                    2 -> { // Forest - Bottom Foliage Ambient Glow & Nature Light Beams
                        drawCircle(
                            color = world.secondaryColor.copy(alpha = 0.08f),
                            radius = w * 0.6f,
                            center = Offset(w * 0.5f, h * 0.95f)
                        )
                    }
                    3 -> { // Ice - Sub-zero Frosted Blue Ring & Glacial Glow
                        drawCircle(
                            color = Color(0xFF80D8FF).copy(alpha = 0.06f),
                            radius = w * 0.7f,
                            center = Offset(w * 0.5f, h * 0.3f)
                        )
                    }
                    4 -> { // Volcano - Magma Chamber Bottom Fire Glow & Lava Pulse
                        val magmaPulse = (sin(animState.toDouble()) * 0.05 + 0.15).toFloat()
                        drawCircle(
                            color = Color(0xFFFF3D00).copy(alpha = magmaPulse),
                            radius = w * 0.8f,
                            center = Offset(w * 0.5f, h * 1.05f)
                        )
                    }
                    5 -> { // Electric Lab - Random Voltage Lightning Arcs
                        if (Random.nextFloat() < 0.12f) { // Random lightning flash frame
                            val boltPath = Path().apply {
                                var currX = Random.nextFloat() * w
                                var currY = 0f
                                moveTo(currX, currY)
                                while (currY < h) {
                                    currX += (Random.nextFloat() * 60f - 30f)
                                    currY += (Random.nextFloat() * 80f + 40f)
                                    lineTo(currX, currY)
                                }
                            }
                            drawPath(
                                path = boltPath,
                                color = Color(0xFFFFEA00).copy(alpha = 0.35f),
                                style = Stroke(width = 2.5f)
                            )
                        }
                    }
                    6 -> { // Cyber City - Perspective Moving Digital Grid Lines
                        val lineCount = 10
                        val stepY = h / lineCount
                        val offsetY = (animState / 6.28f) * stepY
                        for (i in 0..lineCount) {
                            val y = (i * stepY + offsetY) % h
                            drawLine(
                                color = Color(0xFFFF007F).copy(alpha = 0.07f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.2f
                            )
                        }
                    }
                }
            }

            // 2. Render Biome Environment Particles
            if (showParticles) {
                particles.forEach { particle ->
                    // Position updates
                    particle.yRatio += particle.speedY
                    val wobble = sin(animState + particle.wobbleOffset) * 0.002f
                    particle.xRatio += particle.speedX + wobble

                    // Screen Wrap handling
                    if (particle.yRatio < -0.05f) {
                        particle.yRatio = 1.05f
                        particle.xRatio = Random.nextFloat()
                    } else if (particle.yRatio > 1.05f) {
                        particle.yRatio = -0.05f
                        particle.xRatio = Random.nextFloat()
                    }

                    if (particle.xRatio < -0.05f) particle.xRatio = 1.05f
                    if (particle.xRatio > 1.05f) particle.xRatio = -0.05f

                    val cx = particle.xRatio * w
                    val cy = particle.yRatio * h
                    val colorList = world.particleColors
                    val pColor = colorList[particle.colorIndex % colorList.size]

                    when (world.id) {
                        2 -> { // Forest - Swaying Oval Leaf
                            drawOval(
                                color = pColor.copy(alpha = particle.alpha),
                                topLeft = Offset(cx - particle.radius, cy - particle.radius * 0.6f),
                                size = androidx.compose.ui.geometry.Size(particle.radius * 2f, particle.radius * 1.2f)
                            )
                        }
                        4 -> { // Volcano - Glowing Spark Core
                            drawCircle(
                                color = pColor.copy(alpha = particle.alpha),
                                radius = particle.radius,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = particle.alpha * 0.8f),
                                radius = particle.radius * 0.4f,
                                center = Offset(cx, cy)
                            )
                        }
                        5 -> { // Electric - High Voltage Diamond Ion Spark
                            drawCircle(
                                color = pColor.copy(alpha = particle.alpha),
                                radius = particle.radius,
                                center = Offset(cx, cy)
                            )
                        }
                        else -> { // Standard Glowing Sphere
                            drawCircle(
                                color = pColor.copy(alpha = particle.alpha),
                                radius = particle.radius,
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }
            }
        }
    }
}
