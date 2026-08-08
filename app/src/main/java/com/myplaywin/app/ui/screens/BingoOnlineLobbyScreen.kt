package com.myplaywin.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.data.model.BingoMatchStatus
import com.myplaywin.app.data.model.BingoMatchType
import com.myplaywin.app.data.repository.BingoMultiplayerEngine
import com.myplaywin.app.ui.components.AaaBingoAudioHaptics
import com.myplaywin.app.ui.components.AaaCasinoBackground
import com.myplaywin.app.ui.components.AaaGlassCard
import com.myplaywin.app.ui.components.AaaGlossyButton

/**
 * Phase 7: Real-Time Online Multiplayer Lobby & Matchmaking Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoOnlineLobbyScreen(
    engine: BingoMultiplayerEngine,
    onBackClick: () -> Unit,
    onStartOnlineGameplay: () -> Unit
) {
    val context = LocalContext.current
    val matchStatus by engine.matchStatus.collectAsState()
    val pingMs by engine.networkLatencyMs.collectAsState()
    val searchTimeLeft by engine.searchTimeRemaining.collectAsState()

    var showTimeoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ONLINE MULTIPLAYER LOBBY",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        AaaBingoAudioHaptics.playClickSound()
                        engine.cancelMatchmaking()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D031B))
            )
        }
    ) { innerPadding ->
        AaaCasinoBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. PLAYER PROFILE CARD
                AaaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0xFFFFD700),
                    glowColor = Color(0xFFE040FB)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Avatar Frame
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3F2B75))
                                .border(2.dp, Color(0xFFFFD700), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👑", fontSize = 28.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = engine.localPlayerName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFFD700),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "LVL ${engine.localPlayerLevel}",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Win Rate: ${(engine.localPlayerWinRate * 100).toInt()}% • Rank: Diamond II",
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // XP Progress Bar
                            LinearProgressIndicator(
                                progress = { 0.72f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF00E676),
                                trackColor = Color.White.copy(alpha = 0.15f)
                            )
                        }
                    }
                }

                // 2. NETWORK & SERVER STATUS PANEL
                AaaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0xFF00E5FF),
                    glowColor = Color(0xFF2979FF)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (pingMs < 100) Color(0xFF00E676) else Color(0xFFFFD700),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SERVER: US-EAST-1",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Latency: ${pingMs}ms • Ping: Excellent",
                                    color = Color(0xFF80D8FF),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "EST. WAIT: 4s",
                                color = Color(0xFF80D8FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 3. MATCH TYPE SELECTION
                Text(
                    text = "SELECT MATCH TYPE",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.2.sp
                )

                // 1 VS 1 CARD
                AaaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0xFFFFD700)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "1 VS 1 REAL-TIME BINGO",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Fast-paced online match with synchronized server calls",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                        Text(text = "⚔️", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AaaGlossyButton(
                        onClick = {
                            showTimeoutDialog = false
                            engine.startMatchmaking(
                                matchType = BingoMatchType.ONE_VS_ONE,
                                onMatchFound = { onStartOnlineGameplay() },
                                onSearchTimeout = { showTimeoutDialog = true }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color(0xFF100326),
                        borderColor = Color(0xFFFFF59D),
                        enabled = (matchStatus != BingoMatchStatus.SEARCHING)
                    ) {
                        Text(
                            text = if (matchStatus == BingoMatchStatus.SEARCHING) "SEARCHING..." else "PLAY 1 VS 1 NOW",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // PLAY WITH FRIENDS (PRIVATE ROOMS)
                var showRoomNotFoundDialog by remember { mutableStateOf(false) }
                var privateRoomCodeInput by remember { mutableStateOf("") }

                Text(
                    text = "PLAY WITH FRIENDS",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.2.sp
                )

                AaaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0xFFE040FB)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PRIVATE GAME ROOM",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Create a custom room code or join an existing one directly",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = privateRoomCodeInput,
                            onValueChange = { if (it.length <= 6) privateRoomCodeInput = it.uppercase() },
                            label = { Text("Enter 6-Digit Room Code", color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE040FB),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedLabelColor = Color(0xFFE040FB),
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (privateRoomCodeInput.length == 6) {
                                        AaaBingoAudioHaptics.playClickSound()
                                        engine.joinPrivateRoom(
                                            code = privateRoomCodeInput,
                                            onMatchFound = { onStartOnlineGameplay() },
                                            onRoomNotFound = { showRoomNotFoundDialog = true }
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                enabled = privateRoomCodeInput.length == 6,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("JOIN ROOM", color = Color(0xFF100326), fontWeight = FontWeight.Black)
                            }

                            Button(
                                onClick = {
                                    AaaBingoAudioHaptics.playClickSound()
                                    engine.createPrivateRoom { onStartOnlineGameplay() }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("CREATE ROOM", color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                if (showRoomNotFoundDialog) {
                    AlertDialog(
                        onDismissRequest = { showRoomNotFoundDialog = false },
                        containerColor = Color(0xFF1B0C33),
                        title = {
                            Text(
                                text = "Room Not Found",
                                color = Color.Red,
                                fontWeight = FontWeight.Black
                            )
                        },
                        text = {
                            Text(
                                text = "The room code you entered is invalid, expired, or the room is already full.",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showRoomNotFoundDialog = false
                                    if (privateRoomCodeInput.length == 6) {
                                        engine.joinPrivateRoom(
                                            code = privateRoomCodeInput,
                                            onMatchFound = { onStartOnlineGameplay() },
                                            onRoomNotFound = { showRoomNotFoundDialog = true }
                                        )
                                    }
                                }
                            ) {
                                Text("Retry", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showRoomNotFoundDialog = false
                                }
                            ) {
                                Text("Back", color = Color.Gray)
                            }
                        }
                    )
                }

                // 2 VS 2 & TOURNAMENT PLACEHOLDERS (Architecture Ready)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0C30).copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "👥 2 VS 2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Unlocks At Lvl 20", color = Color.Gray, fontSize = 10.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0C30).copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🏆 TOURNAMENT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Future Ready", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }

            // 4. MATCHMAKING SEARCHING OVERLAY
            if (matchStatus == BingoMatchStatus.SEARCHING && !showTimeoutDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.82f)),
                    contentAlignment = Alignment.Center
                ) {
                    AaaGlassCard(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .padding(16.dp),
                        borderColor = Color(0xFF00E5FF)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "SEARCHING FOR OPPONENT...",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                textAlign = TextAlign.Center
                            )

                            // Animated Circular Search Ring
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color = Color(0xFF00E5FF),
                                strokeWidth = 4.dp
                            )

                            Text(
                                text = "Priority: Level & Ping Match • Search Time: ${20 - searchTimeLeft}s",
                                color = Color(0xFF80D8FF),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { engine.cancelMatchmaking() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("CANCEL SEARCH", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 5. TIMEOUT SEARCHING FALLBACK DIALOG
            if (showTimeoutDialog) {
                AlertDialog(
                    onDismissRequest = { showTimeoutDialog = false },
                    containerColor = Color(0xFF1B0C33),
                    title = {
                        Text(
                            text = "SEARCH TIMEOUT",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black
                        )
                    },
                    text = {
                        Text(
                            text = "No online player responded within 20 seconds. What would you like to do?",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showTimeoutDialog = false
                                engine.startMatchmaking(
                                    matchType = BingoMatchType.ONE_VS_ONE,
                                    onMatchFound = { onStartOnlineGameplay() },
                                    onSearchTimeout = { showTimeoutDialog = true }
                                )
                            }
                        ) {
                            Text("CONTINUE SEARCHING", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showTimeoutDialog = false
                                engine.cancelMatchmaking()
                            }
                        ) {
                            Text("CANCEL", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}
