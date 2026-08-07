package com.myplaywin.app.blockmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.myplaywin.app.blockmaster.progression.PlayerProgressionManager
import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockMasterProfileStatsDialog(
    saveData: BlockMasterSaveData,
    onDismiss: () -> Unit
) {
    val rank = PlayerProgressionManager.getRankForLevel(saveData.playerLevel)
    val (xpInLvl, xpNeeded) = PlayerProgressionManager.getXpProgressForLevel(saveData.playerXp, saveData.playerLevel)
    val progressFrac = (xpInLvl.toFloat() / xpNeeded.toFloat()).coerceIn(0f, 1f)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.85f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF130F24),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(Color(0xFF00E5FF), Color(0xFFE040FB))
                    )
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // HEADER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PLAYER PROFILE & STATS",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Permanent career records & rank progress",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF221C38), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // RANK & PROFILE CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1733)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = rank.badgeEmoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = saveData.playerName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "${rank.title} • LVL ${saveData.playerLevel}",
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // XP BAR
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "XP PROGRESS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "$xpInLvl / $xpNeeded XP", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = { progressFrac },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF00E5FF),
                                    trackColor = Color(0xFF2B2247)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "CAREER STATISTICS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // STATS GRID
                    val stats = listOf(
                        StatEntry("High Score", "${saveData.highScore}", "🏆", Color(0xFFFFD700)),
                        StatEntry("Highest Level", "LVL ${saveData.highestLevelReached}", "👑", Color(0xFFE040FB)),
                        StatEntry("Games Played", "${saveData.totalGamesPlayed}", "🎮", Color(0xFF00E5FF)),
                        StatEntry("Games Won", "${saveData.totalGamesWon}", "🎖️", Color(0xFF00E676)),
                        StatEntry("Games Lost", "${saveData.totalGamesLost}", "💔", Color(0xFFFF5252)),
                        StatEntry("Lines Cleared", "${saveData.totalLinesCleared}", "🧱", Color(0xFFA855F7)),
                        StatEntry("Max Combo", "x${saveData.highestComboAllTime}", "🔥", Color(0xFFFF9100)),
                        StatEntry("Perfect Clears", "${saveData.totalPerfectClears}", "🌟", Color(0xFFFFD700)),
                        StatEntry("Bombs Exploded", "${saveData.totalBombsExploded}", "💣", Color(0xFFFF3D00)),
                        StatEntry("Ice Shattered", "${saveData.totalIceShattered}", "🧊", Color(0xFF00E5FF)),
                        StatEntry("Power-Ups Used", "${saveData.totalPowerUpsUsed}", "🧪", Color(0xFF7C4DFF)),
                        StatEntry("Lifetime Coins", "${saveData.coins}", "🪙", Color(0xFFFFD700))
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(stats.size) { index ->
                            val stat = stats[index]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A142D)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, stat.color.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = stat.emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = stat.title, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(text = stat.value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

private data class StatEntry(
    val title: String,
    val value: String,
    val emoji: String,
    val color: Color
)
