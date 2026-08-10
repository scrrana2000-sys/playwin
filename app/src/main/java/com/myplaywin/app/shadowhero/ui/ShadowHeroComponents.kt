package com.myplaywin.app.shadowhero.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Custom Canvas drawing a glowing purple hooded shadow hero with cyan glowing eyes.
 */
@Composable
fun ShadowHeroAvatarCanvas(
    modifier: Modifier = Modifier,
    glowPulse: Float = 1.0f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        // Background dark aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF8B5CF6).copy(alpha = 0.5f * glowPulse),
                    Color(0xFF6D28D9).copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = center,
                radius = w * 0.6f
            ),
            center = center,
            radius = w * 0.6f
        )

        // Outer Hood Shape (Dark Purple/Violet)
        val hoodPath = Path().apply {
            moveTo(w * 0.5f, h * 0.15f) // Hood tip
            cubicTo(w * 0.15f, h * 0.25f, w * 0.1f, h * 0.7f, w * 0.15f, h * 0.85f)
            lineTo(w * 0.85f, h * 0.85f)
            cubicTo(w * 0.9f, h * 0.7f, w * 0.85f, h * 0.25f, w * 0.5f, h * 0.15f)
            close()
        }

        drawPath(
            path = hoodPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF7C3AED),
                    Color(0xFF4C1D95),
                    Color(0xFF1E1035)
                )
            )
        )

        // Hood inner outline / depth rim
        val innerHoodPath = Path().apply {
            moveTo(w * 0.5f, h * 0.25f)
            cubicTo(w * 0.25f, h * 0.35f, w * 0.22f, h * 0.65f, w * 0.25f, h * 0.8f)
            lineTo(w * 0.75f, h * 0.8f)
            cubicTo(w * 0.78f, h * 0.65f, w * 0.75f, h * 0.35f, w * 0.5f, h * 0.25f)
            close()
        }

        drawPath(
            path = innerHoodPath,
            color = Color(0xFF0F0B18)
        )

        // Face Void / Shadow Area
        drawCircle(
            color = Color(0xFF080511),
            center = Offset(w * 0.5f, h * 0.52f),
            radius = w * 0.25f
        )

        // Cyan Glowing Eyes (Left & Right)
        val eyeGlowRadius = w * 0.08f * glowPulse
        val leftEye = Offset(w * 0.40f, h * 0.50f)
        val rightEye = Offset(w * 0.60f, h * 0.50f)

        // Cyan Aura behind eyes
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF22D3EE), Color(0xFF06B6D4).copy(alpha = 0.4f), Color.Transparent),
                center = leftEye,
                radius = eyeGlowRadius * 2f
            ),
            center = leftEye,
            radius = eyeGlowRadius * 2f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF22D3EE), Color(0xFF06B6D4).copy(alpha = 0.4f), Color.Transparent),
                center = rightEye,
                radius = eyeGlowRadius * 2f
            ),
            center = rightEye,
            radius = eyeGlowRadius * 2f
        )

        // Eye Slits / Core Cyan Light
        val eyePath = Path().apply {
            // Left Slit
            moveTo(w * 0.35f, h * 0.50f)
            lineTo(w * 0.44f, h * 0.48f)
            lineTo(w * 0.42f, h * 0.52f)
            close()

            // Right Slit
            moveTo(w * 0.65f, h * 0.50f)
            lineTo(w * 0.56f, h * 0.48f)
            lineTo(w * 0.58f, h * 0.52f)
            close()
        }

        drawPath(
            path = eyePath,
            color = Color(0xFFE0F2FE)
        )

        // Shoulders / Cape Base
        val capePath = Path().apply {
            moveTo(w * 0.1f, h * 0.85f)
            lineTo(w * 0.05f, h * 0.98f)
            lineTo(w * 0.95f, h * 0.98f)
            lineTo(w * 0.9f, h * 0.85f)
            close()
        }

        drawPath(
            path = capePath,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF3B0764), Color(0xFF6B21A8), Color(0xFF3B0764))
            )
        )

        // Neon Cyan Sparkles / Floating Particles
        drawCircle(
            color = Color(0xFF22D3EE).copy(alpha = 0.7f * glowPulse),
            center = Offset(w * 0.2f, h * 0.3f),
            radius = w * 0.02f
        )
        drawCircle(
            color = Color(0xFFA855F7).copy(alpha = 0.8f * glowPulse),
            center = Offset(w * 0.8f, h * 0.35f),
            radius = w * 0.025f
        )
        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.6f * glowPulse),
            center = Offset(w * 0.75f, h * 0.65f),
            radius = w * 0.018f
        )
    }
}

/**
 * Premium Shadow Hero Mini Game Card for the PlayWin Home Screen.
 */
@Composable
fun ShadowHeroCard(
    onCardClick: () -> Unit,
    bestStage: Int = 3,
    highScore: Int = 12450,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth press scale transition
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CardPressScale"
    )

    // Pulsing neon border glow
    val infiniteTransition = rememberInfiniteTransition(label = "NeonGlowPulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseValue"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(86.dp)
            .scale(cardScale)
            .shadow(
                elevation = (12 * pulseGlow).dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFFA855F7),
                ambientColor = Color(0xFF06B6D4)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onCardClick
            ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFA855F7).copy(alpha = pulseGlow),
                    Color(0xFF06B6D4).copy(alpha = pulseGlow * 0.8f),
                    Color(0xFFEC4899).copy(alpha = pulseGlow * 0.6f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF130D24))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF231342),
                            Color(0xFF130A26),
                            Color(0xFF0A0518)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail: Glowing purple hooded shadow hero avatar
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1038))
                        .border(
                            1.dp,
                            Brush.sweepGradient(
                                listOf(Color(0xFFA855F7), Color(0xFF06B6D4), Color(0xFFA855F7))
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    ShadowHeroAvatarCanvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        glowPulse = pulseGlow
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title + Subtitle Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Shadow Hero",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Dark Adventure",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action Button: ▶ PLAY NOW
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF7C3AED),
                                    Color(0xFF0284C7),
                                    Color(0xFF6366F1)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "▶",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PLAY NOW",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
