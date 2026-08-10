package com.myplaywin.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Shadow
import android.app.Activity
import android.widget.Toast
import com.myplaywin.app.data.model.BingoMatchStatus
import com.myplaywin.app.data.model.BingoMovePayload
import com.myplaywin.app.data.repository.BingoMultiplayerEngine
import com.myplaywin.app.data.voice.BingoVoiceChatManager
import com.myplaywin.app.data.voice.VoiceConnectionState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.myplaywin.app.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

/**
 * Phase 7: Real-Time Online Synchronized Gameplay Screen - 100% Visual Parity with AI Mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoOnlineGameplayScreen(
    engine: BingoMultiplayerEngine,
    onBackToLobby: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentRoom by engine.currentRoom.collectAsState()
    val matchStatus by engine.matchStatus.collectAsState()
    val pingMs by engine.networkLatencyMs.collectAsState()
    val antiCheatAlert by engine.antiCheatAlert.collectAsState()

    val voiceConnectionState by BingoVoiceChatManager.connectionState.collectAsState()
    val isVoiceMuted by BingoVoiceChatManager.isMuted.collectAsState()
    val speakingPlayers by BingoVoiceChatManager.speakingPlayers.collectAsState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val code = currentRoom?.roomId ?: ""
            if (code.isNotBlank()) {
                BingoVoiceChatManager.joinVoiceRoom(context, code)
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice chat.", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            BingoVoiceChatManager.leaveVoiceRoom(context)
        }
    }

    LaunchedEffect(currentRoom?.roomId) {
        val rId = currentRoom?.roomId
        if (!rId.isNullOrBlank()) {
            if (voiceConnectionState == VoiceConnectionState.DISCONNECTED) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    BingoVoiceChatManager.joinVoiceRoom(context, rId)
                }
            }
        } else {
            BingoVoiceChatManager.leaveVoiceRoom(context)
        }
    }

    // Default fallback board
    val defaultBoard = remember {
        List(5) { r ->
            List(5) { c ->
                val isFree = (r == 2 && c == 2)
                val letters = listOf("B", "I", "N", "G", "O")
                BingoTile(
                    row = r,
                    col = c,
                    number = 0,
                    columnLetter = letters[c],
                    isFreeTile = isFree,
                    isMarked = isFree
                )
            }
        }
    }

    var boardState by remember { mutableStateOf(defaultBoard) }
    var completedLines by remember { mutableStateOf<Set<BingoLineType>>(emptySet()) }
    var notificationMsg by remember { mutableStateOf<String?>(null) }
    var matchTimeSeconds by remember { mutableStateOf(0) }

    val opponent = currentRoom?.players?.values?.firstOrNull { it.uid != engine.localPlayerUid }

    var disconnectTimer by remember { mutableStateOf(30) }
    val isOpponentDisconnected = opponent != null && !opponent.connected && currentRoom?.status == "playing"

    LaunchedEffect(isOpponentDisconnected) {
        if (isOpponentDisconnected) {
            disconnectTimer = 30
            while (disconnectTimer > 0) {
                kotlinx.coroutines.delay(1000)
                disconnectTimer--
            }
            engine.endMatchDueToOpponentDisconnect()
        }
    }

    val progressionRepo = remember { com.myplaywin.app.data.repository.BingoProgressionRepository(context) }
    var isResultProcessed by remember { mutableStateOf(false) }

    // Reconstruct board when room updates
    LaunchedEffect(currentRoom) {
        val room = currentRoom ?: return@LaunchedEffect
        val localPlayerObj = room.players[engine.localPlayerUid]
        if (localPlayerObj != null && localPlayerObj.card.isNotEmpty()) {
            val reconstructedBoard = listsToBoard(localPlayerObj.card, room.calledNumbers)
            boardState = reconstructedBoard
            val localLines = evaluateLinesLocal(reconstructedBoard)
            completedLines = localLines

            // Sync progress back to Firebase
            val localMarkedCount = reconstructedBoard.flatten().count { it.isMarked }
            if (localPlayerObj.completedLinesCount != localLines.size || localPlayerObj.markedCount != localMarkedCount) {
                engine.syncPlayerProgress(localLines.size, localMarkedCount)
            }
        }
    }

    // Process victory / defeat automatically
    LaunchedEffect(matchStatus) {
        if (matchStatus == BingoMatchStatus.PLAYING) {
            isResultProcessed = false
        } else if (!isResultProcessed && (matchStatus == BingoMatchStatus.VICTORY || matchStatus == BingoMatchStatus.DEFEAT)) {
            isResultProcessed = true
            val baseCoins = if (matchStatus == BingoMatchStatus.VICTORY) 12 else 3
            progressionRepo.processMatchResult(
                matchType = "ONLINE_1V1",
                difficulty = "RANKED",
                opponentName = opponent?.displayName ?: "Online Opponent",
                result = if (matchStatus == BingoMatchStatus.VICTORY) "VICTORY" else "DEFEAT",
                durationSeconds = matchTimeSeconds.coerceAtLeast(10),
                numbersCalledCount = currentRoom?.calledNumbersHistory?.size ?: 12,
                coinRewardOverride = baseCoins
            )
            if (matchStatus == BingoMatchStatus.VICTORY) {
                AaaBingoAudioHaptics.playVictoryFanfare()
            } else {
                AaaBingoAudioHaptics.playDefeatSound()
            }
        }
    }

    // Increment Match Timer
    LaunchedEffect(matchStatus) {
        if (matchStatus == BingoMatchStatus.PLAYING) {
            matchTimeSeconds = 0
            while (true) {
                delay(1000)
                matchTimeSeconds++
            }
        }
    }

    val activeCalledNumber = currentRoom?.activeCalledNumber

    // Real-Time Board Sound Cue
    val markedCount = remember { derivedStateOf { boardState.flatten().count { it.isMarked } } }
    var lastMarkedCount by remember { mutableStateOf(1) }
    LaunchedEffect(markedCount.value) {
        if (markedCount.value > lastMarkedCount) {
            AaaBingoAudioHaptics.playTileDaubSound()
            lastMarkedCount = markedCount.value
        }
    }

    // Glow Animation
    val infiniteTransition = rememberInfiniteTransition(label = "OnlineGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    androidx.activity.compose.BackHandler(enabled = true) {
        showExitConfirmDialog = true
    }

    Scaffold(
        containerColor = Color(0xFF090616),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BINGO ONLINE",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 2.sp,
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(
                                    color = Color(0xFFFFD700).copy(alpha = glowAlpha),
                                    blurRadius = 16f
                                )
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF00E676), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${pingMs}ms",
                            color = Color(0xFF80D8FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Match",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (!currentRoom?.roomId.isNullOrBlank()) {
                        IconButton(onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (!hasPermission) {
                                micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            } else if (voiceConnectionState == VoiceConnectionState.CONNECTED) {
                                BingoVoiceChatManager.toggleMute()
                            } else {
                                val code = currentRoom?.roomId ?: ""
                                if (code.isNotBlank()) {
                                    BingoVoiceChatManager.joinVoiceRoom(context, code)
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (voiceConnectionState == VoiceConnectionState.CONNECTED && !isVoiceMuted) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Voice Mic",
                                tint = when (voiceConnectionState) {
                                    VoiceConnectionState.CONNECTED -> if (isVoiceMuted) Color(0xFFEF4444) else Color(0xFF10B981)
                                    VoiceConnectionState.CONNECTING -> Color(0xFFF59E0B)
                                    else -> Color(0xFF94A3B8)
                                }
                            )
                        }
                    }
                    IconButton(onClick = { engine.simulateReconnection() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Simulate Reconnect",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF130D2B)
                )
            )
        }
    ) { innerPadding ->
        AaaCasinoBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. ADMOB BANNER AD
                    com.playwin.ads.BannerManager.BannerAd(
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Find matching tile definition for board matching hint
                    val matchingTile = remember(activeCalledNumber, boardState) {
                        activeCalledNumber?.let { num ->
                            boardState.flatten().find { it.number == num && !it.isMarked && !it.isFreeTile }
                        }
                    }
                    val matchingTileKey = matchingTile?.let { it.row * 5 + it.col }
                    val shakingTileKey = null // Online tiles don't shake on opponent calls

                    // 2. 5x5 BINGO BOARD GRID (100% Reuse of Visual Components)
                    BingoBoardGrid(
                        boardTiles = boardState,
                        completedLines = completedLines,
                        shakingTileKey = shakingTileKey,
                        matchingTileKey = matchingTileKey,
                        glowAlpha = glowAlpha,
                        onTileClick = { tile ->
                            val movePayload = BingoMovePayload(
                                roomId = currentRoom?.roomId ?: "",
                                playerUid = engine.localPlayerUid,
                                tileRow = tile.row,
                                tileCol = tile.col,
                                tileNumber = tile.number,
                                moveType = "DAUB"
                            )
                            val result = engine.submitMove(movePayload, boardState)
                            if (result.isValid) {
                                AaaBingoAudioHaptics.playTileDaubSound()
                            } else {
                                AaaBingoAudioHaptics.playWrongTileSound()
                            }
                        }
                    )

                    // 3. SHARED TURN SYNCHRONIZATION BAR
                    val isMyTurn = currentRoom?.game?.currentTurn == engine.localPlayerUid
                    val turnStatusMessage = if (isMyTurn) "👉 YOUR TURN! Tap any uncalled number" else "⏳ OPPONENT'S TURN. Wait for call..."
                    BingoTurnSynchronizationHeader(
                        isPlayerTurn = isMyTurn,
                        turnStatusMessage = turnStatusMessage,
                        playerCompletedLinesCount = completedLines.size,
                        aiCompletedLinesCount = opponent?.completedLinesCount ?: 0,
                        aiName = opponent?.displayName ?: "Opponent"
                    )

                    // 4. LIVE GOAL & REAL-TIME PROGRESS INDICATOR
                    BingoGoalAndProgressHeader(
                        completedLines = completedLines
                    )

                    // 5. CLAIM BINGO BUTTON
                    BingoClaimButton(
                        hasBingo = completedLines.size >= 5,
                        glowAlpha = glowAlpha,
                        onClick = {
                            val claimPayload = BingoMovePayload(
                                roomId = currentRoom?.roomId ?: "",
                                playerUid = engine.localPlayerUid,
                                moveType = "CLAIM_BINGO"
                            )
                            val result = engine.submitMove(claimPayload, boardState)
                            if (!result.isValid) {
                                AaaBingoAudioHaptics.playWrongTileSound()
                            }
                        }
                    )

                    // 6. RECENTLY CALLED NUMBERS BAR
                    BingoCalledNumbersBar(
                        calledNumbersHistory = currentRoom?.calledNumbersHistory ?: emptyList(),
                        activeCalledNumber = activeCalledNumber,
                        glowAlpha = glowAlpha
                    )

                    // 7. ACTIVE CALLED BALL DISPLAY
                    BingoActiveBallAnnouncer(
                        activeNumber = activeCalledNumber,
                        glowAlpha = glowAlpha
                    )

                    // 8. ACTIVE NUMBER BOARD MATCH HINT UI
                    if (activeCalledNumber != null && matchingTile == null) {
                        Surface(
                            color = Color(0xFF281C48),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "ℹ️ Number ${columnLetterForNum(activeCalledNumber)}-$activeCalledNumber is not on your board",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Anti-Cheat Notification / Info Banner
                    if (antiCheatAlert != null) {
                        Surface(
                            color = Color(0xFFFF1744).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 6.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = antiCheatAlert ?: "",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                                IconButton(onClick = { engine.clearAntiCheatAlert() }, modifier = Modifier.size(16.dp)) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }

                    // 10. LIVE ONLINE OPPONENT HUD CARD (Visual parity with AI Opponent)
                    val aiProfileOfOpponent = remember(opponent) {
                        AiPlayerProfile(
                            name = opponent?.displayName ?: "Matching...",
                            avatarEmoji = opponent?.avatarUrl?.ifEmpty { "🤖" } ?: "🤖",
                            countryFlag = "🌐",
                            level = opponent?.level ?: 12,
                            winRate = ((opponent?.winRate ?: 0.68f) * 100).toInt(),
                            badge = "LIVE COMPETITOR",
                            personality = "Balanced"
                        )
                    }
                    val oppStatusText = if (opponent?.connected == true) "🟢 Online" else "🔴 Disconnected"
                    BingoAiOpponentHeader(
                        aiProfile = aiProfileOfOpponent,
                        aiStatusText = oppStatusText,
                        aiCompletedLinesCount = opponent?.completedLinesCount ?: 0,
                        aiDaubsCount = opponent?.markedCount ?: 0
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // RECONNECTING OVERLAY
                if (matchStatus == BingoMatchStatus.RECONNECTING) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFFD700), modifier = Modifier.size(52.dp))
                            Text(
                                text = "NETWORK DISCONNECTED\nRestoring room state...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // OPPONENT DISCONNECTED OVERLAY
                if (isOpponentDisconnected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.88f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AaaGlassCard(
                            modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp),
                            borderColor = Color.Red
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Opponent Disconnected",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                                CircularProgressIndicator(
                                    color = Color.Red,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Waiting for opponent to reconnect: ${disconnectTimer}s\nMatch resumes automatically upon return.",
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // VICTORY / DEFEAT POST-MATCH DIALOG WITH PLAY AGAIN & EXIT ROOM SYSTEM
                if (matchStatus == BingoMatchStatus.VICTORY || matchStatus == BingoMatchStatus.DEFEAT) {
                    if (matchStatus == BingoMatchStatus.VICTORY) {
                        AaaVictoryVfxCanvas()
                    }
                    BingoOnlinePostMatchDialog(
                        isVictory = matchStatus == BingoMatchStatus.VICTORY,
                        isDefeat = matchStatus == BingoMatchStatus.DEFEAT,
                        opponent = opponent,
                        currentRoom = currentRoom,
                        boardTiles = boardState,
                        completedLines = completedLines,
                        onPlayAgain = {
                            engine.requestPlayAgain()
                        },
                        onExitRoom = {
                            engine.cancelMatchmaking()
                            onBackToLobby()
                        }
                    )
                }
            }
        }
    }

    // Exit Confirmation Dialog
    if (showExitConfirmDialog) {
        Dialog(onDismissRequest = { showExitConfirmDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, Color(0xFFFF1744)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F0C11))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🚨 QUIT CURRENT MATCH?",
                        color = Color(0xFFFF1744),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Leaving the match early will forfeit your entry and record a defeat. Are you sure you want to quit?",
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showExitConfirmDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Text("CANCEL", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                showExitConfirmDialog = false
                                engine.cancelMatchmaking()
                                onBackToLobby()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("QUIT MATCH", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Helpers
private fun generateBingoBoardForSeed(seed: Long): List<List<BingoTile>> {
    val rng = Random(seed)
    val cols = listOf(
        (1..15).shuffled(rng).take(5),
        (16..30).shuffled(rng).take(5),
        (31..45).shuffled(rng).take(5),
        (46..60).shuffled(rng).take(5),
        (61..75).shuffled(rng).take(5)
    )

    val grid = MutableList(5) { r ->
        MutableList(5) { c ->
            val isFree = (r == 2 && c == 2)
            val letters = listOf("B", "I", "N", "G", "O")
            BingoTile(
                row = r,
                col = c,
                number = if (isFree) 0 else cols[c][r],
                columnLetter = letters[c],
                isFreeTile = isFree,
                isMarked = isFree
            )
        }
    }
    return grid
}

private fun evaluateLinesLocal(board: List<List<BingoTile>>): Set<BingoLineType> {
    val lines = mutableSetOf<BingoLineType>()
    for (r in 0..4) if (board[r].all { it.isMarked }) lines.add(when(r) { 0 -> BingoLineType.ROW_0; 1 -> BingoLineType.ROW_1; 2 -> BingoLineType.ROW_2; 3 -> BingoLineType.ROW_3; else -> BingoLineType.ROW_4 })
    for (c in 0..4) if ((0..4).all { r -> board[r][c].isMarked }) lines.add(when(c) { 0 -> BingoLineType.COL_0; 1 -> BingoLineType.COL_1; 2 -> BingoLineType.COL_2; 3 -> BingoLineType.COL_3; else -> BingoLineType.COL_4 })
    if ((0..4).all { i -> board[i][i].isMarked }) lines.add(BingoLineType.DIAG_MAIN)
    if ((0..4).all { i -> board[i][4 - i].isMarked }) lines.add(BingoLineType.DIAG_ANTI)
    return lines
}

private fun listsToBoard(card: List<Int>, calledNumbers: List<Int>): List<List<BingoTile>> {
    val grid = MutableList(5) { r ->
        MutableList(5) { c ->
            val idx = r * 5 + c
            val num = if (idx < card.size) card[idx] else 0
            val isFree = (r == 2 && c == 2)
            val isMarked = isFree || calledNumbers.contains(num)
            val letters = listOf("B", "I", "N", "G", "O")
            BingoTile(
                row = r,
                col = c,
                number = num,
                columnLetter = letters[c],
                isFreeTile = isFree,
                isMarked = isMarked
            )
        }
    }
    return grid
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.LightGray, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun BingoOnlinePostMatchDialog(
    isVictory: Boolean,
    isDefeat: Boolean,
    opponent: com.myplaywin.app.data.model.BingoOnlinePlayer?,
    currentRoom: com.myplaywin.app.data.model.BingoOnlineRoom?,
    boardTiles: List<List<BingoTile>>,
    completedLines: Set<BingoLineType>,
    onPlayAgain: () -> Unit,
    onExitRoom: () -> Unit
) {
    val containerBg = when {
        isVictory -> Color(0xFF13092D)
        isDefeat -> Color(0xFF2C0A12)
        else -> Color(0xFF22130C)
    }
    val borderCol = when {
        isVictory -> Color(0xFFFFD700)
        isDefeat -> Color(0xFFFF1744)
        else -> Color(0xFFFF9100)
    }
    val titleText = when {
        isVictory -> "BINGO ONLINE VICTORY!"
        isDefeat -> "ONLINE MATCH DEFEAT"
        else -> "MATCH DRAW!"
    }
    val titleEmoji = when {
        isVictory -> "👑🏆🎉"
        isDefeat -> "💔"
        else -> "🤝🎲"
    }

    val opponentBoardTiles = remember(opponent?.card, currentRoom?.calledNumbers) {
        listsToBoard(opponent?.card ?: emptyList(), currentRoom?.calledNumbers ?: emptyList())
    }
    val opponentCompletedLines = remember(opponentBoardTiles) {
        evaluateLinesLocal(opponentBoardTiles)
    }
    val calledNumbersHistory = currentRoom?.calledNumbersHistory ?: emptyList()

    val localPlayer = currentRoom?.players?.get(currentRoom.players.keys.firstOrNull { it != opponent?.uid })
    val isLocalReadyToPlayAgain = localPlayer?.playAgainRequested == true
    val isOpponentReadyToPlayAgain = opponent?.playAgainRequested == true

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.92f)
                    .padding(4.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, borderCol),
                colors = CardDefaults.cardColors(containerColor = containerBg)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Banner
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = titleEmoji, fontSize = 36.sp)
                            Text(
                                text = titleText,
                                color = borderCol,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                letterSpacing = 1.5.sp,
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(color = borderCol.copy(alpha = 0.8f), blurRadius = 12f)
                                )
                            )
                            Text(
                                text = if (isVictory) "Congratulations! You claimed BINGO first!"
                                else if (isDefeat) "${opponent?.displayName ?: "Opponent"} completed BINGO first."
                                else "All 75 numbers called without achieving BINGO.",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Summary Stats Table
                    item {
                        Surface(
                            color = Color.Black.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, borderCol.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "MATCH SUMMARY STATS",
                                    color = borderCol,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                                HorizontalDivider(color = borderCol.copy(alpha = 0.3f))

                                StatRow("Match Duration", "01:15")
                                StatRow("Winner", if (isVictory) "You (Player)" else if (isDefeat) "${opponent?.displayName ?: "Opponent"}" else "Draw")
                                StatRow("Your Lines vs Opponent Lines", "${completedLines.size} Lines  vs  ${opponentCompletedLines.size} Lines")
                                StatRow("Total Numbers Called", "${calledNumbersHistory.size} / 75")
                                StatRow("Room ID", currentRoom?.roomId ?: "")
                            }
                        }
                    }

                    // Final Boards Title
                    item {
                        Text(
                            text = "FINAL BOARDS VISUALIZATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700),
                            letterSpacing = 1.sp
                        )
                    }

                    // 5x5 Mini Grids Side-By-Side
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                BingoMiniBoardGrid(
                                    title = "YOUR BOARD",
                                    subtitle = "Player",
                                    badgeText = "${completedLines.size} Lines",
                                    badgeColor = Color(0xFF00E676),
                                    boardTiles = boardTiles,
                                    completedLines = completedLines,
                                    primaryAccentColor = Color(0xFF00E676),
                                    markedColor = Color(0xFF00E5FF)
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                BingoMiniBoardGrid(
                                    title = opponent?.displayName ?: "Opponent",
                                    subtitle = "Online Opponent",
                                    badgeText = "${opponentCompletedLines.size} Lines",
                                    badgeColor = Color(0xFFE040FB),
                                    boardTiles = opponentBoardTiles,
                                    completedLines = opponentCompletedLines,
                                    primaryAccentColor = Color(0xFFE040FB),
                                    markedColor = Color(0xFFAB47BC)
                                )
                            }
                        }
                    }

                    // Called Numbers Log Section
                    item {
                        Surface(
                            color = Color.Black.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFF38235C))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "CALLED NUMBERS CHRONOLOGY (${calledNumbersHistory.size})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )

                                if (calledNumbersHistory.isEmpty()) {
                                    Text(text = "No numbers called", fontSize = 11.sp, color = Color.Gray)
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(calledNumbersHistory) { num ->
                                            val letter = columnLetterForNum(num)
                                            val ballColor = colorForColumnLetter(letter)
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(ballColor, CircleShape)
                                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$num",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons Row
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AaaGlossyButton(
                                    onClick = onExitRoom,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    containerColor = Color(0xFF334155),
                                    contentColor = Color.White,
                                    borderColor = Color(0xFF64748B)
                                ) {
                                    Text("EXIT ROOM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                val btnText = when {
                                    isLocalReadyToPlayAgain -> "WAITING... ⏳"
                                    isOpponentReadyToPlayAgain -> "PLAY AGAIN! (Ready! ⚡)"
                                    else -> "PLAY AGAIN"
                                }
                                val btnColor = if (isVictory) Color(0xFFFFD700) else Color(0xFFFF1744)
                                val btnTextCol = if (isVictory) Color.Black else Color.White
                                val btnBorderCol = if (isVictory) Color(0xFFFFF59D) else Color(0xFFFF80AB)

                                AaaGlossyButton(
                                    onClick = { if (!isLocalReadyToPlayAgain) onPlayAgain() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    containerColor = if (isLocalReadyToPlayAgain) Color.DarkGray else btnColor,
                                    contentColor = if (isLocalReadyToPlayAgain) Color.LightGray else btnTextCol,
                                    borderColor = if (isLocalReadyToPlayAgain) Color.Gray else btnBorderCol
                                ) {
                                    Text(btnText, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
