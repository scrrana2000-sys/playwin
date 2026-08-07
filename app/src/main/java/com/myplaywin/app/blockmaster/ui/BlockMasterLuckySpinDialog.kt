package com.myplaywin.app.blockmaster.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.myplaywin.app.blockmaster.luckyspin.LuckySpinEngine
import com.myplaywin.app.blockmaster.luckyspin.SpinRewardSlice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BlockMasterLuckySpinDialog(
    lastSpinTimestamp: Long,
    userCoins: Int,
    onSpinRewardWon: (SpinRewardSlice, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val canFree = remember(lastSpinTimestamp) { LuckySpinEngine.canFreeSpin(lastSpinTimestamp) }
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var wonReward by remember { mutableStateOf<SpinRewardSlice?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF22163B), Color(0xFF100B22))
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFE040FB), Color(0xFF00E5FF))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎰 LUCKY SPIN WHEEL",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    IconButton(
                        onClick = { if (!isSpinning) onDismiss() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Text(
                    text = if (canFree) "FREE DAILY SPIN READY! 🎁" else "Cost per spin: ${LuckySpinEngine.SPIN_COST_COINS} Coins 🪙",
                    color = if (canFree) Color(0xFF00E676) else Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Spinning Wheel Container
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Wheel Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotationAngle)
                    ) {
                        val canvasSize = size.minDimension
                        val sweepAngle = 360f / LuckySpinEngine.wheelSlices.size

                        LuckySpinEngine.wheelSlices.forEachIndexed { i, slice ->
                            drawArc(
                                color = Color(slice.colorHex),
                                startAngle = i * sweepAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = Size(canvasSize, canvasSize)
                            )
                        }

                        // Outer gold rim
                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = canvasSize / 2f,
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }

                    // Pointer Indicator (Top center)
                    Surface(
                        shape = CircleShape,
                        color = Color.Red,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopCenter)
                    ) {}

                    // Center Spin Button / Hub
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF100B22),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "SPIN",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Spin Action Button
                Button(
                    onClick = {
                        if (isSpinning) return@Button
                        if (!canFree && userCoins < LuckySpinEngine.SPIN_COST_COINS) return@Button

                        isSpinning = true
                        val selectedSlice = LuckySpinEngine.getRandomSpinResult()
                        val targetDegrees = 360f * 5 + (selectedSlice.index * (360f / 8f))

                        scope.launch {
                            val steps = 60
                            val initial = rotationAngle
                            for (step in 1..steps) {
                                val t = step.toFloat() / steps
                                val easeOut = 1f - (1f - t) * (1f - t) * (1f - t)
                                rotationAngle = initial + targetDegrees * easeOut
                                delay(30)
                            }
                            isSpinning = false
                            wonReward = selectedSlice
                            onSpinRewardWon(selectedSlice, canFree)
                        }
                    },
                    enabled = !isSpinning && (canFree || userCoins >= LuckySpinEngine.SPIN_COST_COINS),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canFree) Color(0xFF00E676) else Color(0xFFFFD700),
                        disabledContainerColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isSpinning) "SPINNING..." else if (canFree) "SPIN FOR FREE!" else "SPIN (${LuckySpinEngine.SPIN_COST_COINS} 🪙)",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                // Reward Popup Text
                wonReward?.let { reward ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(reward.colorHex).copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(reward.colorHex))
                    ) {
                        Text(
                            text = "YOU WON: ${reward.iconEmoji} ${reward.title}!",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
