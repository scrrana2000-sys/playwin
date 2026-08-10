package com.myplaywin.app.ludo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.ludo.data.model.LudoPlayer

@Composable
fun LudoPlayerCard(
    player: LudoPlayer,
    isCurrentTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val ludoColor = player.ludoColor
    val finishedCount = player.tokens.count { it >= 57 }

    // Glow pulse animation for current turn player
    val infiniteTransition = rememberInfiniteTransition(label = "TurnGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    Box(
        modifier = modifier
            .padding(4.dp)
            .shadow(
                elevation = if (isCurrentTurn) 8.dp else 2.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (isCurrentTurn) {
                        listOf(
                            ludoColor.displayColor.copy(alpha = 0.35f),
                            Color(0xFF221F38)
                        )
                    } else {
                        listOf(Color(0xFF2A2744), Color(0xFF1B192E))
                    }
                )
            )
            .border(
                width = if (isCurrentTurn) 2.dp else 1.dp,
                color = if (isCurrentTurn) Color(0xFFFFD700).copy(alpha = glowAlpha) else Color(0xFF3D3A5C),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Player Avatar Circle with Online Status Indicator
            Box {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ludoColor.displayColor)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Online / Offline Status Dot
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(if (player.isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E))
                        .border(1.5.dp, Color(0xFF221F38), CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = player.name,
                        color = Color.White,
                        fontWeight = if (isCurrentTurn) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isCurrentTurn) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFD700))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TURN 🎲",
                                color = Color(0xFF1B172E),
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Color tag
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ludoColor.displayColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (player.rank > 0) "Rank #${player.rank} 🏆" else "Home: $finishedCount/4 🎯",
                        color = Color(0xFFB0B0C3),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
