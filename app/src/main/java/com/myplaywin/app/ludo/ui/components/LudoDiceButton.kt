package com.myplaywin.app.ludo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun LudoDiceButton(
    diceValue: Int, // 0..6
    hasRolled: Boolean,
    isMyTurn: Boolean,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRollingAnim by remember { mutableStateOf(false) }
    var displayedValue by remember { mutableIntStateOf(if (diceValue > 0) diceValue else 1) }

    val rotationAnim by animateFloatAsState(
        targetValue = if (isRollingAnim) 720f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "DiceRotation"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (isRollingAnim) 1.25f else 1.0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "DiceScale"
    )

    LaunchedEffect(diceValue) {
        if (diceValue > 0) {
            isRollingAnim = true
            repeat(6) {
                displayedValue = (1..6).random()
                delay(80L)
            }
            displayedValue = diceValue
            isRollingAnim = false
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when {
                    !isMyTurn -> "Waiting for opponent's move..."
                    !hasRolled -> "It's Your Turn! Roll the Dice 🎲"
                    diceValue == 6 -> "Rolled a 6! Extra Turn! 🎉"
                    else -> "Tap a highlighted token to move!"
                },
                color = if (isMyTurn) Color(0xFFFFD700) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 3D Animated Dice Box
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scaleAnim)
                .rotate(rotationAnim)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFE0E0E0),
                            Color(0xFFB0B0B0)
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    color = if (isMyTurn && !hasRolled) Color(0xFFFFD700) else Color(0xFF4A4A4A),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(enabled = isMyTurn && !hasRolled && !isRollingAnim) {
                    onRollClick()
                },
            contentAlignment = Alignment.Center
        ) {
            // Draw Dice Face Dots
            DiceFaceDots(value = if (isRollingAnim) displayedValue else (if (diceValue > 0) diceValue else 6))
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Roll Button
        Button(
            onClick = onRollClick,
            enabled = isMyTurn && !hasRolled && !isRollingAnim,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF33304A),
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(50.dp)
        ) {
            Text(
                text = if (hasRolled) "ROLLED" else "ROLL 🎲",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun DiceFaceDots(value: Int) {
    Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        val w = size.width
        val h = size.height
        val dotRadius = w * 0.11f
        val dotColor = Color(0xFF1E1035)

        val c = Offset(w / 2f, h / 2f)
        val tl = Offset(w * 0.25f, h * 0.25f)
        val tr = Offset(w * 0.75f, h * 0.25f)
        val bl = Offset(w * 0.25f, h * 0.75f)
        val br = Offset(w * 0.75f, h * 0.75f)
        val ml = Offset(w * 0.25f, h * 0.5f)
        val mr = Offset(w * 0.75f, h * 0.5f)

        when (value) {
            1 -> drawCircle(color = Color(0xFFE53935), radius = dotRadius * 1.3f, center = c)
            2 -> {
                drawCircle(color = dotColor, radius = dotRadius, center = tl)
                drawCircle(color = dotColor, radius = dotRadius, center = br)
            }
            3 -> {
                drawCircle(color = dotColor, radius = dotRadius, center = tl)
                drawCircle(color = dotColor, radius = dotRadius, center = c)
                drawCircle(color = dotColor, radius = dotRadius, center = br)
            }
            4 -> {
                drawCircle(color = dotColor, radius = dotRadius, center = tl)
                drawCircle(color = dotColor, radius = dotRadius, center = tr)
                drawCircle(color = dotColor, radius = dotRadius, center = bl)
                drawCircle(color = dotColor, radius = dotRadius, center = br)
            }
            5 -> {
                drawCircle(color = dotColor, radius = dotRadius, center = tl)
                drawCircle(color = dotColor, radius = dotRadius, center = tr)
                drawCircle(color = dotColor, radius = dotRadius, center = c)
                drawCircle(color = dotColor, radius = dotRadius, center = bl)
                drawCircle(color = dotColor, radius = dotRadius, center = br)
            }
            6 -> {
                drawCircle(color = dotColor, radius = dotRadius, center = tl)
                drawCircle(color = dotColor, radius = dotRadius, center = tr)
                drawCircle(color = dotColor, radius = dotRadius, center = ml)
                drawCircle(color = dotColor, radius = dotRadius, center = mr)
                drawCircle(color = dotColor, radius = dotRadius, center = bl)
                drawCircle(color = dotColor, radius = dotRadius, center = br)
            }
        }
    }
}
