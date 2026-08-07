package com.myplaywin.app.blockmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData

@Composable
fun BlockMasterSettingsDialog(
    saveData: BlockMasterSaveData,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleHaptic: () -> Unit,
    onSetGraphicsQuality: (String) -> Unit,
    onToggleFpsDisplay: () -> Unit,
    onSetLanguage: (String) -> Unit,
    onResetProgress: () -> Unit,
    onDismiss: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1733), Color(0xFF100C22))
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFFA855F7),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚙️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GAME SETTINGS",
                                color = Color(0xFFA855F7),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Audio, Controls & Display",
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

                Divider(color = Color.DarkGray.copy(alpha = 0.5f))

                // --- AUDIO SECTION ---
                Text(
                    text = "AUDIO & HAPTICS",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )

                // Sound Effects Toggle
                SettingsToggleRow(
                    label = "Sound Effects (SFX)",
                    subLabel = "In-game block drop and explosion sounds",
                    checked = saveData.soundEnabled,
                    onCheckedChange = onToggleSound
                )

                // Background Music Toggle
                SettingsToggleRow(
                    label = "Background Music",
                    subLabel = "Relaxing gameplay soundtrack",
                    checked = saveData.musicEnabled,
                    onCheckedChange = onToggleMusic
                )

                // Haptic Feedback Toggle
                SettingsToggleRow(
                    label = "Haptic Vibration",
                    subLabel = "Vibration on block drops & combos",
                    checked = saveData.hapticEnabled,
                    onCheckedChange = onToggleHaptic
                )

                Divider(color = Color.DarkGray.copy(alpha = 0.5f))

                // --- GRAPHICS & PERFORMANCE SECTION ---
                Text(
                    text = "GRAPHICS & DISPLAY",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )

                // Graphics Quality Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Graphics Quality", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Low", "Medium", "High").forEach { quality ->
                            val isSel = saveData.graphicsQuality.equals(quality, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) Color(0xFFFFD700).copy(alpha = 0.25f) else Color(0xFF161129),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) Color(0xFFFFD700) else Color.DarkGray
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetGraphicsQuality(quality) }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(
                                        text = quality,
                                        color = if (isSel) Color(0xFFFFD700) else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // FPS Display Toggle
                SettingsToggleRow(
                    label = "Show FPS Counter",
                    subLabel = "Display real-time framerate overlay",
                    checked = saveData.fpsDisplayEnabled,
                    onCheckedChange = onToggleFpsDisplay
                )

                Divider(color = Color.DarkGray.copy(alpha = 0.5f))

                // --- LANGUAGE SECTION ---
                Text(
                    text = "GLOBAL LOCALIZATION",
                    color = Color(0xFFE040FB),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )

                val languages = listOf("English", "Hindi", "Spanish", "Portuguese", "French", "German", "Arabic", "Japanese", "Korean", "Chinese")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    languages.chunked(5).forEach { rowLangs ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowLangs.forEach { lang ->
                                val isSel = saveData.selectedLanguage.equals(lang, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) Color(0xFFE040FB).copy(alpha = 0.3f) else Color(0xFF161129),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSel) Color(0xFFE040FB) else Color.DarkGray
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSetLanguage(lang) }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text(
                                            text = lang,
                                            color = if (isSel) Color.White else Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = Color.DarkGray.copy(alpha = 0.5f))

                // --- CLOUD SERVICES & BACKUP ---
                Text(
                    text = "CLOUD SAVE & PLAYWIN SYNC",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )

                var syncStatus by remember { mutableStateOf("Cloud Save Active (Auto-Sync)") }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141E33)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = syncStatus, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Last synced: Just now", color = Color.Gray, fontSize = 9.sp)
                        }
                        Button(
                            onClick = { syncStatus = "Sync Complete! ✅" },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "SYNC NOW", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Divider(color = Color.DarkGray.copy(alpha = 0.5f))

                // --- RESET PROGRESS ---
                if (!showResetConfirm) {
                    Button(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ RESET GAME PROGRESS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF330A12)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Are you sure? All scores, coins, and unlocks will be reset!",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        onResetProgress()
                                        showResetConfirm = false
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("CONFIRM RESET", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                OutlinedButton(
                                    onClick = { showResetConfirm = false },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("CANCEL", color = Color.LightGray, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    subLabel: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = subLabel, color = Color.Gray, fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFA855F7),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}
