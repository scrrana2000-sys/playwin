package com.myplaywin.app.blockmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.myplaywin.app.blockmaster.ai.BlockMasterAiAssistant
import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData

@Composable
fun BlockMasterAiAssistantDialog(
    saveData: BlockMasterSaveData,
    onDismiss: () -> Unit
) {
    val aiInsight = remember(saveData) {
        BlockMasterAiAssistant.analyzePlayerSkill(saveData)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1035), Color(0xFF0D071B))
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFFE040FB),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color(0xFFE040FB),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AI GAMEPLAY ADVISOR",
                                color = Color(0xFFE040FB),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Personalized Skill & Strategy Insights",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Divider(color = Color.DarkGray.copy(alpha = 0.5f))

                // Skill Grade Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF271542)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE040FB).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "CURRENT SKILL RATING", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(text = aiInsight.skillGrade, color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE040FB).copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "AI VERIFIED",
                                color = Color(0xFFE040FB),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // AI Tactical Advice Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF170E28)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💡 TACTICAL TIP", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                        Text(
                            text = aiInsight.description,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Recommended Power-Up
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF170E28)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9100).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "RECOMMENDED POWER-UP", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(text = aiInsight.recommendedPowerUp, color = Color(0xFFFF9100), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB))
                ) {
                    Text(text = "GOT IT, THANKS AI!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}
