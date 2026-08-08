package com.myplaywin.app.ui.screens

import com.myplaywin.app.ui.components.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * AAA-Quality Bingo Mini-Game Card for Home Screen (Phase 1)
 *
 * Features:
 * - Matching standard 86dp height & full-width dimension
 * - 20dp rounded corners with glassmorphism & purple-blue gradient
 * - Gold glowing border with idle breathing glow
 * - 60 FPS floating & breathing animation
 * - Glossy B-I-N-G-O colorful balls & golden lucky star icon
 * - Glossy RED "NEW" badge on top-right
 * - Glossy GREEN "PLAY NOW" button with soft glow
 * - Press scale animation (100% -> 96% -> 100%)
 */
@Composable
fun BingoMiniGameCard(
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Touch Press Scale Animation (100% -> 96% -> 100%)
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BingoCardPressScale"
    )

    // Infinite 60 FPS Ambient Animations
    val infiniteTransition = rememberInfiniteTransition(label = "BingoCardInfinite")

    // Idle floating translateY offset (-2dp to +2dp)
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BingoFloatOffset"
    )

    // Glow breathing intensity (0.4f to 1.0f)
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BingoGlowAlpha"
    )

    // Tiny sparkle rotation angle
    val sparkleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BingoSparkleAngle"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(86.dp)
            .offset(y = floatOffset.dp)
            .scale(animatedScale)
            .shadow(
                elevation = (12 * glowAlpha).dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.6f),
                ambientColor = Color(0xFF7C4DFF).copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(20.dp))
            .border(
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = glowAlpha),
                            Color(0xFFFFF59D).copy(alpha = 0.9f),
                            Color(0xFFFFB300).copy(alpha = glowAlpha),
                            Color(0xFF7C4DFF).copy(alpha = 0.6f)
                        )
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color(0xFFFFD700)),
                onClick = onCardClick
            )
    ) {
        // Background Glassmorphism with Purple-Blue Gradient & Ambient Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF231747),
                            Color(0xFF1B113B),
                            Color(0xFF130B2D),
                            Color(0xFF100924)
                        )
                    )
                )
        ) {
            // Ambient Particle Canvas overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Ambient golden radial highlight behind icon
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.25f * glowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(70f, canvasHeight / 2f),
                        radius = 120f
                    ),
                    center = Offset(70f, canvasHeight / 2f),
                    radius = 120f
                )

                // Tiny sparkle particles in background
                rotate(sparkleAngle, Offset(canvasWidth * 0.85f, canvasHeight * 0.3f)) {
                    for (i in 0 until 6) {
                        val rad = Math.toRadians((i * 60).toDouble())
                        val dist = 28f
                        val sx = (canvasWidth * 0.85f) + (dist * cos(rad)).toFloat()
                        val sy = (canvasHeight * 0.3f) + (dist * sin(rad)).toFloat()
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.6f * glowAlpha),
                            radius = 2.5f,
                            center = Offset(sx, sy)
                        )
                    }
                }
            }

            // Main Content Row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bingo Icon (Glossy Balls + Lucky Star + Glow)
                BingoIconGraphic(glowAlpha = glowAlpha)

                Spacer(modifier = Modifier.width(12.dp))

                // Title + Subtitle Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BINGO",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.2.sp,
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(
                                    color = Color(0xFFFFD700).copy(alpha = 0.7f * glowAlpha),
                                    blurRadius = 10f
                                )
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Star accent icon
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Play Online & Offline",
                        color = Color(0xFFD1C4E9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play Now Button (Green Glossy Button with Soft Glow)
                AaaGlossyButton(
                    onClick = onCardClick,
                    containerColor = Color(0xFF00E676),
                    contentColor = Color(0xFF003816),
                    borderColor = Color(0xFFB9F6CA)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Now",
                        tint = Color(0xFF003816),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PLAY NOW",
                        color = Color(0xFF003816),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Top-Right RED "NEW" Glossy Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF1744),
                                        Color(0xFFD50000)
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 0.8.dp,
                                color = Color(0xFFFF8A80),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NEW",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Beautiful Icon Graphic featuring colorful B-I-N-G-O glossy balls & lucky star
 */
@Composable
private fun BingoIconGraphic(glowAlpha: Float) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = Color(0xFFFFD700)
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF3F51B5),
                        Color(0xFF1A237E),
                        Color(0xFF0D1B2A)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = glowAlpha),
                        Color(0xFF9FA8DA)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // B I N G O Glossy balls arrangement
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Row 1: B I N
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BingoBall(letter = "B", color = Color(0xFFFF1744))
                BingoBall(letter = "I", color = Color(0xFF00E676))
                BingoBall(letter = "N", color = Color(0xFFFFD700), isGold = true)
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Row 2: G O
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BingoBall(letter = "G", color = Color(0xFF2979FF))
                BingoBall(letter = "O", color = Color(0xFFE040FB))
            }
        }
    }
}

@Composable
private fun BingoBall(
    letter: String,
    color: Color,
    isGold: Boolean = false
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(15.dp)
            .shadow(2.dp, CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color,
                        if (isGold) Color(0xFFFFA000) else color.copy(alpha = 0.6f)
                    )
                ),
                shape = CircleShape
            )
            .border(0.6.dp, Color.White.copy(alpha = 0.9f), CircleShape)
    ) {
        Text(
            text = letter,
            color = if (isGold) Color.Black else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 8.sp,
            textAlign = TextAlign.Center
        )
    }
}
