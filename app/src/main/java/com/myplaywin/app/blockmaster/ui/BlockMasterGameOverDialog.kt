package com.myplaywin.app.blockmaster.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun BlockMasterGameOverDialog(
    finalScore: Int,
    coinsEarned: Int,
    highestCombo: Int,
    linesCleared: Int,
    gameTimeSec: Long,
    canContinue: Boolean,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
    onContinueWithAd: () -> Unit
) {
    val minutes = gameTimeSec / 60
    val seconds = gameTimeSec % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Dialog(
        onDismissRequest = { /* Modal dialog */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1F1A30),
                            Color(0xFF141022)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF1744),
                            Color(0xFFA855F7),
                            Color(0xFF00E5FF)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // GAME OVER TITLE
                Text(
                    text = "GAME OVER",
                    color = Color(0xFFFF1744),
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    letterSpacing = 2.sp
                )

                // STATS GRID CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF100D1B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatRow(label = "FINAL SCORE", value = "$finalScore", color = Color(0xFF00E5FF))
                        StatRow(label = "COINS EARNED", value = "+$coinsEarned 🪙", color = Color(0xFFFFD700))
                        StatRow(label = "LINES CLEARED", value = "$linesCleared", color = Color(0xFFA855F7))
                        StatRow(label = "HIGHEST COMBO", value = "x$highestCombo", color = Color(0xFF00E676))
                        StatRow(label = "TIME PLAYED", value = formattedTime, color = Color.White)
                    }
                }

                // WATCH AD CONTINUE BUTTON (Once per run)
                if (canContinue) {
                    Button(
                        onClick = onContinueWithAd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100))
                    ) {
                        Icon(imageVector = Icons.Default.OndemandVideo, contentDescription = "Watch Ad", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONTINUE (WATCH REWARD AD)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // PLAY AGAIN & HOME BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Play Again", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "PLAY AGAIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onHome,
                        modifier = Modifier
                            .weight(0.8f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF))
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "HOME", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}
