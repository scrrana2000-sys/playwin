package com.myplaywin.app.ludo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.ludo.data.model.LudoGameState
import com.myplaywin.app.ludo.ui.components.LudoBoardCanvas
import com.myplaywin.app.ludo.ui.components.LudoDiceButton
import com.myplaywin.app.ludo.ui.components.LudoPlayerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoGamePlayScreen(
    gameState: LudoGameState,
    currentUserId: String,
    onRollDice: () -> Unit,
    onMoveToken: (tokenIndex: Int) -> Unit,
    onResetGame: (() -> Unit)? = null,
    onExitGame: () -> Unit
) {
    var showDebugPanel by remember { mutableStateOf(false) }
    val players = gameState.players
    val p0 = players.getOrNull(0) // Red
    val p1 = players.getOrNull(1) // Green
    val p2 = players.getOrNull(2) // Yellow
    val p3 = players.getOrNull(3) // Blue

    val isFinished = gameState.status == "FINISHED"

    val currentTurnPlayer = gameState.currentTurnPlayer
    val isHumanTurn = currentTurnPlayer != null && !currentTurnPlayer.isBot
    val isMyTurnToRoll = isHumanTurn && (gameState.gameMode == "LOCAL_PASS_AND_PLAY" || gameState.gameMode != "PRIVATE_ROOM" || gameState.currentTurnUid == currentUserId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B172E), Color(0xFF0F0C1B))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onExitGame) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Ludo",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (gameState.gameMode == "LOCAL_PASS_AND_PLAY") "LOCAL LUDO MATCH 🎲" else "LUDO KING",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    if (gameState.roomCode.isNotBlank()) {
                        Text(
                            text = "MODE: ${gameState.gameMode}",
                            color = Color(0xFFB0B0C3),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row {
                    // Debug Info Toggle
                    IconButton(onClick = { showDebugPanel = !showDebugPanel }) {
                        Text(
                            text = if (showDebugPanel) "🐞" else "🐛",
                            fontSize = 20.sp
                        )
                    }
                    if (onResetGame != null) {
                        IconButton(onClick = onResetGame) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Match",
                                tint = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }

            // Debug Information Area (Section 18)
            if (showDebugPanel) {
                Surface(
                    color = Color(0xFF141024),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "⚡ GAME ENGINE DEBUG STATE",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Current Player: ${currentTurnPlayer?.name} (${currentTurnPlayer?.ludoColor?.colorName}) | Turn #: ${gameState.turnNumber}",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Dice: ${gameState.diceRoll} | Rolled: ${gameState.hasRolled} | 6s Streak: ${gameState.consecutiveSixes}",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Legal Moves (Token Indices): ${if (gameState.movableTokenIndices.isEmpty()) "None" else gameState.movableTokenIndices.joinToString(", ") { "#${it + 1}" }}",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        players.forEach { p ->
                            Text(
                                text = "${p.ludoColor.colorName} (${p.name}): tokens=${p.tokens}",
                                color = p.ludoColor.displayColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Top Player Cards Row (Red & Green)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (p0 != null) {
                    LudoPlayerCard(
                        player = p0,
                        isCurrentTurn = gameState.currentTurnUid == p0.uid,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (p1 != null) {
                    LudoPlayerCard(
                        player = p1,
                        isCurrentTurn = gameState.currentTurnUid == p1.uid,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Action Status Banner / Announcement Pill
            if (gameState.lastActionText.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFF262040),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = gameState.lastActionText,
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Center Ludo Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                LudoBoardCanvas(
                    gameState = gameState,
                    currentUserId = currentUserId,
                    onTokenClick = onMoveToken,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom Player Cards Row (Blue & Yellow)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (p3 != null) { // Blue
                    LudoPlayerCard(
                        player = p3,
                        isCurrentTurn = gameState.currentTurnUid == p3.uid,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (p2 != null) { // Yellow
                    LudoPlayerCard(
                        player = p2,
                        isCurrentTurn = gameState.currentTurnUid == p2.uid,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Dice & Control Bar
            LudoDiceButton(
                diceValue = gameState.diceRoll,
                hasRolled = gameState.hasRolled,
                isMyTurn = isMyTurnToRoll,
                onRollClick = onRollDice
            )
        }

        // Victory Dialog / Modal
        if (isFinished) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(
                        text = "🏆 MATCH FINISHED! 🏆",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Winner: ${gameState.winnerName}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Final Rankings:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB0B0C3)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        gameState.rankings.forEach { rankText ->
                            Text(
                                text = rankText,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onExitGame,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text("Exit to Lobby", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    if (onResetGame != null) {
                        OutlinedButton(
                            onClick = onResetGame,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Text("Play Again 🔄", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                containerColor = Color(0xFF231F3B),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
