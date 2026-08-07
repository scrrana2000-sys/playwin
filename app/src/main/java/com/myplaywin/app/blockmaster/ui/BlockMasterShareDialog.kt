package com.myplaywin.app.blockmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
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
import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData

@Composable
fun BlockMasterShareDialog(
    saveData: BlockMasterSaveData,
    onDismiss: () -> Unit
) {
    var isShared by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF23163B), Color(0xFF100A1F))
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFF00E5FF),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "SHARE CARD",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Shareable Result Card Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A122E)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFA855F7))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "👑 BLOCK MASTER 👑", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text(text = saveData.playerName.ifEmpty { "Block Master Player" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        Divider(color = Color.DarkGray.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "HIGH SCORE", color = Color.Gray, fontSize = 9.sp)
                                Text(text = "${saveData.highScore}", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "LEVEL", color = Color.Gray, fontSize = 9.sp)
                                Text(text = "${saveData.playerLevel}", color = Color(0xFFA855F7), fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "GAMES", color = Color.Gray, fontSize = 9.sp)
                                Text(text = "${saveData.totalGamesPlayed}", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(6.dp)) {
                                Text(text = "PlayWin Mini Game • Can you beat my score?", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Share Button
                Button(
                    onClick = { isShared = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isShared) "LINK COPIED! 🚀" else "SHARE TO FRIENDS",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
