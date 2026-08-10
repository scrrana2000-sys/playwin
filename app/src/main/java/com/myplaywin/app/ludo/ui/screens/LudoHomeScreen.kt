package com.myplaywin.app.ludo.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.ludo.data.model.LudoColor
import com.myplaywin.app.ludo.data.model.LudoGameMode
import com.myplaywin.app.ludo.data.model.LudoGameState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoHomeScreen(
    gameState: LudoGameState?,
    currentUserId: String,
    currentUserName: String,
    currentUserAvatar: String,
    onStartLocalGame: () -> Unit = {},
    onCreateRoom: (gameMode: LudoGameMode, isPrivate: Boolean, maxPlayers: Int?) -> Unit,
    onJoinRoom: (roomCode: String) -> Unit,
    onStartMatch: () -> Unit,
    onRollDice: () -> Unit,
    onMoveToken: (tokenIndex: Int) -> Unit,
    onResetGame: (() -> Unit)? = null,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var showCreatePrivateDialog by remember { mutableStateOf(false) }
    var showJoinPrivateDialog by remember { mutableStateOf(false) }
    var selectedPlayerCount by remember { mutableIntStateOf(2) }
    var inputCode by remember { mutableStateOf("") }

    // If game is in progress
    if (gameState != null && gameState.status == "PLAYING") {
        LudoGamePlayScreen(
            gameState = gameState,
            currentUserId = currentUserId,
            onRollDice = onRollDice,
            onMoveToken = onMoveToken,
            onResetGame = onResetGame,
            onExitGame = { onBackClick() }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1D1836), Color(0xFF0F0C1D))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LUDO ARENA 🎲",
                    color = Color(0xFFFFD700),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Check if user is currently inside a Waiting Room Lobby
            if (gameState != null && (gameState.status == "WAITING" || gameState.status == "CANCELLED")) {
                LudoWaitingLobby(
                    gameState = gameState,
                    currentUserId = currentUserId,
                    onStartMatch = onStartMatch,
                    onLeaveLobby = onBackClick
                )
            } else {
                // Featured Immediate Local Play Hero Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFD700))
                        .clickable { onStartLocalGame() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A47)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF9800))))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF9800)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎲", fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "START LOCAL GAME",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "Instant 4-Player Match (Red, Green, Yellow, Blue)",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Start Local",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Game Mode Options Grid
                Text(
                    text = "Online & Private Multiplayer",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                val modes = listOf(
                    ModeCardData("2 Players", "Quick 1v1 Online", Icons.Default.Person, LudoGameMode.TWO_PLAYER),
                    ModeCardData("3 Players", "3-Player Online Clash", Icons.Default.Group, LudoGameMode.THREE_PLAYER),
                    ModeCardData("4 Players", "Full 4-Player Online", Icons.Default.Group, LudoGameMode.FOUR_PLAYER),
                    ModeCardData("vs Computer", "Offline Practice", Icons.Default.SmartToy, LudoGameMode.VS_BOT),
                    ModeCardData("Create Private", "Host Friend Room", Icons.Default.VpnKey, LudoGameMode.PRIVATE_ROOM),
                    ModeCardData("Join Private", "Enter Room Code", Icons.Default.MeetingRoom, LudoGameMode.PRIVATE_ROOM)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(modes) { modeData ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .shadow(6.dp, RoundedCornerShape(16.dp))
                                .clickable {
                                    when {
                                        modeData.title == "Create Private" -> {
                                            showCreatePrivateDialog = true
                                        }
                                        modeData.title == "Join Private" -> {
                                            inputCode = ""
                                            showJoinPrivateDialog = true
                                        }
                                        else -> {
                                            onCreateRoom(modeData.mode, false, null)
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (modeData.title.contains("Private")) Color(0xFF382E63) else Color(0xFF2B254A)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = modeData.icon,
                                    contentDescription = modeData.title,
                                    tint = if (modeData.title.contains("Private")) Color(0xFFFFD700) else Color(0xFF4CAF50),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = modeData.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = modeData.subtitle,
                                    color = Color(0xFFB0B0C3),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // CREATE PRIVATE ROOM DIALOG
        if (showCreatePrivateDialog) {
            AlertDialog(
                onDismissRequest = { showCreatePrivateDialog = false },
                title = {
                    Text(
                        text = "Create Private Room 🗝️",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Select total number of players for your private room:",
                            color = Color(0xFFB0B0C3),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(2, 3, 4).forEach { count ->
                                FilterChip(
                                    selected = selectedPlayerCount == count,
                                    onClick = { selectedPlayerCount = count },
                                    label = {
                                        Text(
                                            text = "$count Players",
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedPlayerCount == count) Color.Black else Color.White
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFFD700),
                                        containerColor = Color(0xFF1B1630)
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCreatePrivateDialog = false
                            onCreateRoom(LudoGameMode.PRIVATE_ROOM, true, selectedPlayerCount)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        Text("Create Room", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePrivateDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF262040)
            )
        }

        // JOIN PRIVATE ROOM DIALOG
        if (showJoinPrivateDialog) {
            AlertDialog(
                onDismissRequest = { showJoinPrivateDialog = false },
                title = {
                    Text(
                        text = "Join Private Room 🔑",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter the 6-character room code shared by your friend:",
                            color = Color(0xFFB0B0C3),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = {
                                val clean = it.trim().uppercase().filter { c -> c.isLetterOrDigit() }
                                if (clean.length <= 6) {
                                    inputCode = clean
                                }
                            },
                            label = { Text("Room Code (e.g. A7K9PX)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputCode.length >= 4) {
                                showJoinPrivateDialog = false
                                onJoinRoom(inputCode)
                            } else {
                                Toast.makeText(context, "Please enter valid room code", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text("Join Room", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinPrivateDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF262040)
            )
        }
    }
}

@Composable
private fun LudoWaitingLobby(
    gameState: LudoGameState,
    currentUserId: String,
    onStartMatch: () -> Unit,
    onLeaveLobby: () -> Unit
) {
    val context = LocalContext.current
    val isHost = gameState.hostUid == currentUserId
    var showLeaveConfirmDialog by remember { mutableStateOf(false) }

    if (gameState.status == "CANCELLED") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF282245)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ROOM CLOSED 🚫",
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The room was cancelled or closed by the host.",
                    color = Color(0xFFB0B0C3),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLeaveLobby,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Return to Lobby", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val totalSlots = gameState.maxPlayers
    val canStart = isHost && gameState.players.size >= 2

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282245)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PRIVATE ROOM LOBBY 🗝️",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Room Code Display with Copy & Native Share
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(Color(0xFF1B1630), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ROOM CODE",
                        color = Color(0xFFB0B0C3),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = gameState.roomCode,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Ludo Room Code", gameState.roomCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Room code copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = Color(0xFFFFD700)
                    )
                }

                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Play Ludo on PlayWin")
                            putExtra(Intent.EXTRA_TEXT, "Join my Ludo game on PlayWin!\nRoom Code: ${gameState.roomCode}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Room Code"))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Code",
                        tint = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Players (${gameState.players.size}/$totalSlots):",
                color = Color(0xFFB0B0C3),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Render all capacity slots (occupied + empty)
            for (slotIdx in 0 until totalSlots) {
                val player = gameState.players.getOrNull(slotIdx)
                val colorEnum = LudoColor.entries.getOrElse(slotIdx % 4) { LudoColor.RED }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            if (player != null) Color(0xFF1F1A38) else Color(0xFF18142B),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (player != null) colorEnum.displayColor else Color.Gray.copy(alpha = 0.2f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (player != null) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(player.ludoColor.displayColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = player.name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = player.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (player.uid == gameState.hostUid) {
                            Text(
                                text = "HOST 👑",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "READY ✅",
                                color = Color(0xFF4CAF50),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Gray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "${slotIdx + 1}", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Waiting for Player ${slotIdx + 1}... ⏳",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            if (isHost) {
                Button(
                    onClick = onStartMatch,
                    enabled = canStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF43A047),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (canStart) "START MATCH 🚀" else "Waiting for players (${gameState.players.size}/$totalSlots)...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            } else {
                Text(
                    text = "Waiting for host (${gameState.hostName}) to start the game... ⏳",
                    color = Color(0xFFFFD700),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showLeaveConfirmDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("LEAVE ROOM 🚪", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Leave Room Confirmation Dialog
    if (showLeaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmDialog = false },
            title = {
                Text("Leave Room?", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Text(
                    text = if (isHost) "Leaving as host will close this private room for all joined players." else "Are you sure you want to leave this room?",
                    color = Color(0xFFB0B0C3)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveConfirmDialog = false
                        onLeaveLobby()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF262040)
        )
    }
}

private data class ModeCardData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val mode: LudoGameMode
)
