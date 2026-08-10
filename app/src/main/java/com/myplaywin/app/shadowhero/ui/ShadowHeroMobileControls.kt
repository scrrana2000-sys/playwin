package com.myplaywin.app.shadowhero.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Mobile Touch Controls Overlay for Shadow Hero (Landscape Mode).
 * Supports simultaneous inputs (Move + Jump, Move + Dash, etc.) with haptic feedback.
 */
@Composable
fun ShadowHeroMobileControls(
    onLeftPressChange: (Boolean) -> Unit,
    onRightPressChange: (Boolean) -> Unit,
    onJumpClick: () -> Unit,
    onDashClick: () -> Unit,
    onAttackClick: () -> Unit,
    dashCooldownFraction: Float,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        // --- LEFT SIDE: MOVEMENT D-PAD (◀ and ▶) ---
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Arrow Button [ ◀ ]
            ControlButton(
                symbol = "◀",
                label = "LEFT",
                accentColor = Color(0xFFA855F7),
                sizeDp = 70,
                onPressChange = { isPressed ->
                    onLeftPressChange(isPressed)
                    if (isPressed) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
            )

            // Right Arrow Button [ ▶ ]
            ControlButton(
                symbol = "▶",
                label = "RIGHT",
                accentColor = Color(0xFFA855F7),
                sizeDp = 70,
                onPressChange = { isPressed ->
                    onRightPressChange(isPressed)
                    if (isPressed) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
            )
        }

        // --- RIGHT SIDE: JUMP, DASH & ATTACK BUTTONS ---
        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Dash Button [ DASH ] with Cooldown Arc
            DashControlButton(
                dashCooldownFraction = dashCooldownFraction,
                onClick = {
                    onDashClick()
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            )

            // Attack Button [ ATTACK ] (Phase 13)
            AttackControlButton(
                onClick = {
                    onAttackClick()
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            )

            // Jump Button [ JUMP ]
            JumpControlButton(
                onClick = {
                    onJumpClick()
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            )
        }
    }
}

@Composable
private fun ControlButton(
    symbol: String,
    label: String,
    accentColor: Color,
    sizeDp: Int,
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = 500f),
        label = "BtnScale"
    )

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isPressed)
                    accentColor.copy(alpha = 0.45f)
                else
                    Color(0xFF1E1038).copy(alpha = 0.55f)
            )
            .border(
                2.dp,
                if (isPressed) Color.White else accentColor.copy(alpha = 0.7f),
                CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPressChange(true)
                        tryAwaitRelease()
                        isPressed = false
                        onPressChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = symbol,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun JumpControlButton(
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = 500f),
        label = "JumpScale"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = if (isPressed) 0.9f else 0.7f),
                        Color(0xFF6D28D9).copy(alpha = 0.5f)
                    )
                )
            )
            .border(2.5.dp, Color(0xFFC084FC), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onClick()
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "⬆️", fontSize = 20.sp)
            Text(
                text = "JUMP",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun DashControlButton(
    dashCooldownFraction: Float,
    onClick: () -> Unit
) {
    val isReady = (dashCooldownFraction <= 0f)
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = 500f),
        label = "DashScale"
    )

    Box(
        modifier = Modifier
            .size(66.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isReady)
                    Color(0xFF0284C7).copy(alpha = if (isPressed) 0.85f else 0.6f)
                else
                    Color(0xFF1E1038).copy(alpha = 0.4f)
            )
            .border(
                2.dp,
                if (isReady) Color(0xFF38BDF8) else Color.Gray.copy(alpha = 0.4f),
                CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (isReady) {
                            isPressed = true
                            onClick()
                            tryAwaitRelease()
                            isPressed = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Cooldown Overlay Arc
        if (!isReady) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sweep = 360f * dashCooldownFraction
                drawArc(
                    color = Color.Black.copy(alpha = 0.65f),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = size
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "⚡",
                fontSize = 18.sp,
                color = if (isReady) Color.White else Color.Gray
            )
            Text(
                text = "DASH",
                color = if (isReady) Color.White else Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun AttackControlButton(
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = 500f),
        label = "AttackScale"
    )

    Box(
        modifier = Modifier
            .size(76.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFEC4899).copy(alpha = if (isPressed) 0.9f else 0.7f),
                        Color(0xFFBE185D).copy(alpha = 0.5f)
                    )
                )
            )
            .border(2.5.dp, Color(0xFFF472B6), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onClick()
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "⚔️", fontSize = 20.sp)
            Text(
                text = "ATTACK",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
    }
}
