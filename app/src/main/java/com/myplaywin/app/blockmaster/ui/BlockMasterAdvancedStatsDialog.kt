package com.myplaywin.app.blockmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.myplaywin.app.blockmaster.retention.RetentionAndStatsManager
import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData

data class StatMetricItem(
    val title: String,
    val value: String,
    val iconEmoji: String,
    val colorHex: Long
)

@Composable
fun BlockMasterAdvancedStatsDialog(
    saveData: BlockMasterSaveData,
    onDismiss: () -> Unit
) {
    val avgScore = remember(saveData) { RetentionAndStatsManager.getAverageScore(saveData.lifetimeScore, saveData.totalGamesPlayed) }
    val avgSurvivalSec = remember(saveData) { RetentionAndStatsManager.getAverageSurvivalTimeSec(saveData.timePlayedSeconds, saveData.totalGamesPlayed) }
    val formattedSurvival = remember(avgSurvivalSec) { RetentionAndStatsManager.getFormattedSurvivalTime(avgSurvivalSec) }

    val metrics = listOf(
        StatMetricItem("LIFETIME SCORE", "${saveData.lifetimeScore}", "🏆", 0xFFFFD700),
        StatMetricItem("LIFETIME COINS", "${saveData.lifetimeCoinsEarned} 🪙", "💰", 0xFF00E5FF),
        StatMetricItem("AVERAGE SCORE", "$avgScore", "📊", 0xFFA855F7),
        StatMetricItem("AVG SURVIVAL", formattedSurvival, "⏱️", 0xFF00E676),
        StatMetricItem("TOTAL GAMES", "${saveData.totalGamesPlayed}", "🎮", 0xFFFF007F),
        StatMetricItem("PERFECT CLEARS", "${saveData.totalPerfectClears}", "🌟", 0xFFFFD700),
        StatMetricItem("TOTAL LINES", "${saveData.totalLinesCleared}", "🧱", 0xFF80DEEA),
        StatMetricItem("POWER-UPS USED", "${saveData.totalPowerUpsUsed}", "⚡", 0xFFFF5722)
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1430), Color(0xFF100B20))
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFF00E5FF),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📈", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ADVANCED STATISTICS",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Lifetime performance dashboard",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Player Summary Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161027))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = saveData.playerName,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Title: ${saveData.equippedTitle}",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "LVL ${saveData.playerLevel}",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Grid of 8 Metrics
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(metrics) { metric ->
                        StatMetricCard(metric = metric)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMetricCard(metric: StatMetricItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161026)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(metric.colorHex).copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = metric.iconEmoji, fontSize = 20.sp)
            Text(text = metric.title, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(
                text = metric.value,
                color = Color(metric.colorHex),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}
