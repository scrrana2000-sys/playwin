package com.myplaywin.app.ui.components

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ==========================================
// AAA HAPTIC & AUDIO SYSTEM MANAGER
// ==========================================

object AaaBingoAudioHaptics {
    private var toneGenerator: ToneGenerator? = null
    var isMuted by mutableStateOf(false)

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun playClickSound() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
        } catch (_: Exception) {}
    }

    fun playTileDaubSound() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 65)
        } catch (_: Exception) {}
    }

    fun playWrongTileSound() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 120)
        } catch (_: Exception) {}
    }

    fun playLineCompleteSound() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 150)
        } catch (_: Exception) {}
    }

    fun playVictoryFanfare() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 400)
        } catch (_: Exception) {}
    }

    fun playBallCallPopSound() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 50)
        } catch (_: Exception) {}
    }

    fun playDefeatSound() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_LOW_L, 300)
        } catch (_: Exception) {}
    }

    fun triggerHaptic(context: Context, type: String = "click") {
        try {
            @Suppress("DEPRECATION")
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    "heavy" -> VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                    "error" -> VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                    else -> VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator?.vibrate(effect)
            } else {
                val ms = when (type) {
                    "heavy" -> 80L
                    "error" -> 120L
                    else -> 35L
                }
                vibrator?.vibrate(ms)
            }
        } catch (_: Exception) {}
    }
}

// ==========================================
// 1. AAA ANIMATED CASINO BACKGROUND (60 FPS)
// ==========================================

@Composable
fun AaaCasinoBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CasinoBgInfinite")

    // Animated Ray Angle
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RayRotation"
    )

    // Animated Glow Pulse
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Floating Particles Offset
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ParticlePhase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D031B), // Deep Space Midnight Purple
                        Color(0xFF1B0B33), // Casino Royal Violet
                        Color(0xFF0B0521)  // Dark Royal Blue
                    )
                )
            )
    ) {
        // Dynamic Ray Beams & Bokeh Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.3f)
            val maxRadius = size.width.coerceAtLeast(size.height) * 1.2f

            // Radial Glow Behind Center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8E24AA).copy(alpha = 0.35f * pulseAlpha),
                        Color(0xFF3F51B5).copy(alpha = 0.20f * pulseAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 0.6f
                ),
                center = center,
                radius = maxRadius * 0.6f
            )

            // Rotated Light Beams
            rotate(rayRotation, pivot = center) {
                val numRays = 12
                val angleStep = 360f / numRays
                val rayPath = Path()

                for (i in 0 until numRays step 2) {
                    val startAngleRad = Math.toRadians((i * angleStep).toDouble())
                    val endAngleRad = Math.toRadians(((i + 1) * angleStep).toDouble())

                    rayPath.reset()
                    rayPath.moveTo(center.x, center.y)
                    rayPath.lineTo(
                        center.x + maxRadius * cos(startAngleRad).toFloat(),
                        center.y + maxRadius * sin(startAngleRad).toFloat()
                    )
                    rayPath.lineTo(
                        center.x + maxRadius * cos(endAngleRad).toFloat(),
                        center.y + maxRadius * sin(endAngleRad).toFloat()
                    )
                    rayPath.close()

                    drawPath(
                        path = rayPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.08f * pulseAlpha),
                                Color(0xFFE040FB).copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = maxRadius
                        )
                    )
                }
            }

            // Floating Golden Stars & Ambient Bokeh
            val random = Random(42)
            for (i in 0..24) {
                val baseX = random.nextFloat() * size.width
                val baseY = random.nextFloat() * size.height
                val radius = random.nextFloat() * 4.5f + 1.5f
                val speed = (i % 3 + 1) * 0.5f

                val dy = (sin((particlePhase + i).toDouble()) * 20f * speed).toFloat()
                val dx = (cos((particlePhase * 0.7f + i).toDouble()) * 12f * speed).toFloat()

                val pX = (baseX + dx).coerceIn(0f, size.width)
                val pY = (baseY + dy).coerceIn(0f, size.height)

                val particleColor = if (i % 2 == 0) Color(0xFFFFD700) else Color(0xFFFF80AB)
                val particleAlpha = (sin((particlePhase + i * 0.5).toDouble()).toFloat() * 0.35f + 0.65f).coerceIn(0.2f, 1f)

                drawCircle(
                    color = particleColor.copy(alpha = particleAlpha),
                    radius = radius,
                    center = Offset(pX, pY)
                )
            }
        }

        // Overlay Screen Content
        content()
    }
}

// ==========================================
// 2. AAA GLOSSY GLASS CARD
// ==========================================

@Composable
fun AaaGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderColor: Color = Color(0xFFFFD700),
    glowColor: Color = Color(0xFFE040FB),
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlassCardGlow")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderAlpha"
    )

    Card(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = glowColor.copy(alpha = 0.4f),
                spotColor = borderColor.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(cornerRadius),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    borderColor.copy(alpha = borderAlpha),
                    glowColor.copy(alpha = borderAlpha * 0.7f),
                    borderColor.copy(alpha = borderAlpha)
                )
            )
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B0C33).copy(alpha = 0.88f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

// ==========================================
// 3. AAA GLOSSY BUTTON WITH SPECULAR SHINE
// ==========================================

@Composable
fun AaaGlossyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFFFD700),
    contentColor: Color = Color(0xFF120326),
    borderColor: Color = Color(0xFFFFF59D),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "GlossyButtonScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ButtonShine")
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShineOffset"
    )

    Surface(
        onClick = {
            if (enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                AaaBingoAudioHaptics.triggerHaptic(context, "click")
                AaaBingoAudioHaptics.playClickSound()
                onClick()
            }
        },
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minHeight = 50.dp)
            .scale(scale)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = containerColor.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, if (enabled) borderColor else Color.Gray.copy(alpha = 0.4f)),
        color = if (enabled) containerColor else Color(0xFF3E3B54),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.30f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.20f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Specular Shine Sweep Canvas
            if (enabled) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val width = size.width
                    val x = width * shineOffset
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.40f),
                                Color.Transparent
                            ),
                            start = Offset(x, 0f),
                            end = Offset(x + width * 0.3f, size.height)
                        )
                    )
                }
            }

            CompositionLocalProvider(LocalContentColor provides if (enabled) contentColor else Color.LightGray) {
                ProvideTextStyle(
                    androidx.compose.ui.text.TextStyle(
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        content = content
                    )
                }
            }
        }
    }
}

// ==========================================
// 3B. REUSABLE AAA BINGO BUTTON SYSTEM
// ==========================================

enum class BingoButtonVariant {
    PRIMARY,   // Gold / Amber
    SUCCESS,   // Mint Green
    INFO,      // Electric Blue
    PURPLE,    // Violet / Pink
    DANGER,    // Crimson Red
    OUTLINE    // Glass / Translucent
}

private data class QuadrupleColor(
    val brush: Brush,
    val contentColor: Color,
    val borderColor: Color,
    val spotColor: Color
)

@Composable
fun AaaBingoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: BingoButtonVariant = BingoButtonVariant.PRIMARY,
    icon: ImageVector? = null,
    iconEmoji: String? = null,
    enabled: Boolean = true,
    height: Dp = 50.dp,
    cornerRadius: Dp = 20.dp,
    fontSize: TextUnit = 14.sp
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "BingoButtonScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ButtonShine")
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShineOffset"
    )

    val quad = when (variant) {
        BingoButtonVariant.PRIMARY -> QuadrupleColor(
            Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFB300), Color(0xFFFF8F00))),
            Color(0xFF100326),
            Color(0xFFFFF59D),
            Color(0xFFFFD700)
        )
        BingoButtonVariant.SUCCESS -> QuadrupleColor(
            Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00C853), Color(0xFF00897B))),
            Color.White,
            Color(0xFFB9F6CA),
            Color(0xFF00E676)
        )
        BingoButtonVariant.INFO -> QuadrupleColor(
            Brush.horizontalGradient(listOf(Color(0xFF2979FF), Color(0xFF1565C0), Color(0xFF0D47A1))),
            Color.White,
            Color(0xFF80D8FF),
            Color(0xFF2979FF)
        )
        BingoButtonVariant.PURPLE -> QuadrupleColor(
            Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFD946EF), Color(0xFFEC4899))),
            Color.White,
            Color(0xFFF472B6),
            Color(0xFFD946EF)
        )
        BingoButtonVariant.DANGER -> QuadrupleColor(
            Brush.horizontalGradient(listOf(Color(0xFFFF1744), Color(0xFFD50000), Color(0xFFB71C1C))),
            Color.White,
            Color(0xFFFF80AB),
            Color(0xFFFF1744)
        )
        BingoButtonVariant.OUTLINE -> QuadrupleColor(
            Brush.horizontalGradient(listOf(Color(0xFF1E1338).copy(alpha = 0.85f), Color(0xFF2A1B4D).copy(alpha = 0.85f))),
            Color.White,
            Color(0xFF7C4DFF).copy(alpha = 0.7f),
            Color(0xFF7C4DFF)
        )
    }

    Surface(
        onClick = {
            if (enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                AaaBingoAudioHaptics.triggerHaptic(context, "click")
                AaaBingoAudioHaptics.playClickSound()
                onClick()
            }
        },
        enabled = enabled,
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 8.dp else 2.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = if (enabled) quad.spotColor.copy(alpha = 0.5f) else Color.Transparent
            ),
        shape = RoundedCornerShape(cornerRadius),
        border = BorderStroke(1.5.dp, if (enabled) quad.borderColor else Color.Gray.copy(alpha = 0.4f)),
        color = Color.Transparent,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) quad.brush else Brush.horizontalGradient(listOf(Color(0xFF3E3B54), Color(0xFF2C2A3D))),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (enabled && variant != BingoButtonVariant.OUTLINE) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val x = w * shineOffset
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            start = Offset(x, 0f),
                            end = Offset(x + w * 0.3f, size.height)
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (iconEmoji != null) {
                    Text(
                        text = iconEmoji,
                        fontSize = (fontSize.value + 2).sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) quad.contentColor else Color.LightGray,
                        modifier = Modifier
                            .size((fontSize.value + 6).dp)
                            .padding(end = 6.dp)
                    )
                }

                Text(
                    text = text,
                    color = if (enabled) quad.contentColor else Color.LightGray,
                    fontWeight = FontWeight.Black,
                    fontSize = fontSize,
                    letterSpacing = 0.8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ==========================================
// 4. AAA 3D ANIMATED BINGO CALL BALL
// ==========================================

@Composable
fun Aaa3dBingoBall(
    letter: String,
    number: Int,
    modifier: Modifier = Modifier
) {
    val ballColor = when (letter.uppercase()) {
        "B" -> Color(0xFF00E5FF) // Electric Cyan/Blue
        "I" -> Color(0xFFFF4081) // Neon Pink
        "N" -> Color(0xFFFFD700) // Gold Yellow
        "G" -> Color(0xFF00E676) // Vivid Green
        else -> Color(0xFFE040FB) // Royal Purple
    }

    val transition = rememberInfiniteTransition(label = "BallPulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        modifier = modifier
            .scale(pulseScale)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                spotColor = ballColor,
                ambientColor = ballColor.copy(alpha = 0.8f)
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        ballColor,
                        ballColor.copy(alpha = 0.85f),
                        Color(0xFF0A0217)
                    ),
                    center = Offset(25f, 25f),
                    radius = 180f
                ),
                shape = CircleShape
            )
            .border(2.5.dp, Color.White.copy(alpha = 0.9f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Inner 3D Sphere Highlight
        Box(
            modifier = Modifier
                .fillMaxSize(0.72f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color(0xFFF5F5F5)
                        )
                    ),
                    shape = CircleShape
                )
                .border(1.dp, ballColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = letter,
                    color = ballColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$number",
                    color = Color(0xFF100326),
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(
                            color = ballColor.copy(alpha = 0.4f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }
    }
}

// ==========================================
// 5. AAA BINGO TILE COMPOSABLE WITH 3D SHINE
// ==========================================

@Composable
fun AaaBingoTile(
    number: Int,
    isFreeTile: Boolean,
    isMarked: Boolean,
    isWinningTile: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMatchingCalledTile: Boolean = false,
    isWrongTapped: Boolean = false,
    showTutorialHand: Boolean = false
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.90f
            isWinningTile -> 1.06f
            isMatchingCalledTile -> 1.05f
            isMarked -> 1.02f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "TileScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "WinningTileGlow")
    val winningGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WinningGlowAlpha"
    )

    val pointerYOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PointerYOffset"
    )

    val tileBg = when {
        isWinningTile -> Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFF9100),
                Color(0xFFFFD700)
            )
        )
        isMarked -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF00E676),
                Color(0xFF00A352)
            )
        )
        isFreeTile -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFF8F00)
            )
        )
        isMatchingCalledTile -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF3F51B5),
                Color(0xFF283593)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2C1548),
                Color(0xFF1B0C30)
            )
        )
    }

    val borderColor = when {
        isWrongTapped -> Color(0xFFFF1744)
        isWinningTile -> Color(0xFFFFFFFF)
        isMatchingCalledTile -> Color(0xFFFFD700)
        isMarked -> Color(0xFFB9F6CA)
        isFreeTile -> Color(0xFFFFF59D)
        else -> Color(0xFF5E35B1)
    }

    Box(
        modifier = modifier
            .scale(animatedScale)
            .shadow(
                elevation = if (isWinningTile || isMarked || isMatchingCalledTile) 12.dp else 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = if (isWinningTile) Color(0xFFFFD700) else if (isMatchingCalledTile) Color(0xFFFFD700) else if (isMarked) Color(0xFF00E676) else Color.Transparent
            )
            .background(tileBg, RoundedCornerShape(12.dp))
            .border(
                width = if (isWinningTile || isMatchingCalledTile || isWrongTapped) 2.5.dp else 1.5.dp,
                color = if (isWinningTile || isMatchingCalledTile) borderColor.copy(alpha = winningGlowAlpha) else borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                AaaBingoAudioHaptics.triggerHaptic(context, if (isMarked) "click" else "heavy")
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        // Specular Top Highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.42f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    ),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
        )

        if (isFreeTile) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "👑",
                    fontSize = 16.sp
                )
                Text(
                    text = "FREE",
                    color = Color(0xFF100326),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
        } else {
            Box(contentAlignment = Alignment.Center) {
                if (showTutorialHand && isMatchingCalledTile && !isMarked) {
                    Text(
                        text = "👇",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-14 + pointerYOffset).dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isMarked && !isWinningTile) {
                        Text(
                            text = "✓ ",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    } else if (isWinningTile) {
                        Text(
                            text = "🌟 ",
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "$number",
                        color = when {
                            isWinningTile -> Color(0xFF100326)
                            isMatchingCalledTile -> Color(0xFFFFD700)
                            isMarked -> Color.White
                            else -> Color(0xFFF3E5F5)
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        style = LocalTextStyle.current.copy(
                            shadow = if (isMarked || isMatchingCalledTile) Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 4f) else null
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// 6. AAA FIREWORKS & CONFETTI VFX CANVAS
// ==========================================

@Composable
fun AaaVictoryVfxCanvas(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VictoryVfx")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AnimTime"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val confettiColors = listOf(
            Color(0xFFFFD700), Color(0xFFFF1744), Color(0xFF00E676),
            Color(0xFF00E5FF), Color(0xFFE040FB), Color(0xFFFFFFFF)
        )

        val random = Random(1234)
        for (i in 0..60) {
            val startX = random.nextFloat() * w
            val speedY = random.nextFloat() * 120f + 60f
            val curY = ((animTime * speedY + i * 40f) % (h + 100f)) - 50f

            val swayX = startX + sin((animTime * 0.1f + i).toDouble()).toFloat() * 35f
            val color = confettiColors[i % confettiColors.size]
            val sizeDp = (i % 4 + 4).toFloat()

            drawCircle(
                color = color,
                radius = sizeDp,
                center = Offset(swayX, curY)
            )
        }
    }
}
