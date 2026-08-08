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
import com.myplaywin.app.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Phase 7: Real-Time Online Synchronized Gameplay Screen
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

    // Generate local player board generated deterministically or initialized
    val localBoard = remember(currentRoom?.roomId) {
        generateBingoBoardForSeed(System.currentTimeMillis())
    }

    var boardState by remember { mutableStateOf(localBoard) }
    var completedLines by remember { mutableStateOf<Set<BingoLineType>>(emptySet()) }
    var notificationMsg by remember { mutableStateOf<String?>(null) }

    val opponent = currentRoom?.player2

    val progressionRepo = remember { com.myplaywin.app.data.repository.BingoProgressionRepository(context) }
    var isResultProcessed by remember { mutableStateOf(false) }
    var showRewardPopup by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoom) {
        val room = currentRoom ?: return@LaunchedEffect
        val localPlayerObj = room.players[engine.localPlayerUid]
        if (localPlayerObj != null && localPlayerObj.card.isNotEmpty() && localPlayerObj.marked.isNotEmpty()) {
            val reconstructedBoard = listsToBoard(localPlayerObj.card, localPlayerObj.marked)
            boardState = reconstructedBoard
            completedLines = evaluateLinesLocal(reconstructedBoard)
        }
    }

    LaunchedEffect(matchStatus) {
        if (!isResultProcessed) {
            when (matchStatus) {
                BingoMatchStatus.VICTORY, BingoMatchStatus.DEFEAT -> {
                    showRewardPopup = true
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ONLINE MATCH",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        // Live Signal Dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF00E676), CircleShape)
                        )
                        Text(
                            text = "${pingMs}ms",
                            color = Color(0xFF80D8FF),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        AaaBingoAudioHaptics.playClickSound()
                        engine.cancelMatchmaking()
                        onBackToLobby()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Leave",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { engine.simulateReconnection() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Simulate Reconnect",
                            tint = Color(0xFF00E5FF)
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
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. REAL-TIME OPPONENT CARD
                AaaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0xFFE040FB),
                    glowColor = Color(0xFF7C4DFF)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3F2B75))
                                    .border(1.5.dp, Color(0xFFE040FB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🤖", fontSize = 20.sp)
                            }

                            Column {
                                Text(
                                    text = opponent?.displayName ?: "Matching...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Level ${opponent?.level ?: 1} • Ping: ${opponent?.pingMs ?: 35}ms",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Opponent Live Game Progress Badges
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = Color(0xFF7C4DFF).copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF7C4DFF))
                            ) {
                                Text(
                                    text = "Daubs: ${opponent?.markedCount ?: 0}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                color = Color(0xFFFFD700).copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Text(
                                    text = "Lines: ${opponent?.completedLinesCount ?: 0}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // 2. SERVER CALLED NUMBER BALL DISPLAY & TURN STATUS
                val isMyTurn = currentRoom?.game?.currentTurn == engine.localPlayerUid
                val activeNum = currentRoom?.activeCalledNumber
                val activeLet = currentRoom?.activeLetter ?: ""

                AaaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (isMyTurn) Color(0xFFFFD700) else Color(0xFF00E5FF),
                    glowColor = if (isMyTurn) Color(0xFFFFA000) else Color(0xFF0288D1)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (activeNum != null) {
                                Aaa3dBingoBall(
                                    letter = activeLet,
                                    number = activeNum,
                                    modifier = Modifier.size(68.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .background(Color(0xFF2C1548), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "⏳", fontSize = 28.sp)
                                }
                            }

                            Column {
                                Text(
                                    text = if (isMyTurn) "👉 YOUR TURN TO CALL" else "⏳ OPPONENT'S TURN",
                                    color = if (isMyTurn) Color(0xFFFFD700) else Color(0xFF80D8FF),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (isMyTurn) "Tap an unmarked tile to CALL!" else "Waiting for ${opponent?.displayName ?: "opponent"}...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        // Recent Called Numbers History
                        val calledHistory = currentRoom?.calledNumbersHistory ?: emptyList()
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            calledHistory.takeLast(3).reversed().forEach { num ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFF3F2B75), CircleShape)
                                        .border(1.dp, Color(0xFF7C4DFF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "$num", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 3. SYNCHRONIZED BINGO BOARD (5x5 GRID)
                AaaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0xFF7C4DFF)
                ) {
                    // Header B-I-N-G-O Letters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("B", "I", "N", "G", "O").forEach { letter ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 5x5 Grid
                    for (r in 0..4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (c in 0..4) {
                                val tile = boardState[r][c]
                                val isWinning = isTileInWinningLine(tile.row, tile.col, completedLines)
                                val isMatching = !tile.isMarked && currentRoom?.calledNumbersHistory?.contains(tile.number) == true

                                AaaBingoTile(
                                    number = tile.number,
                                    isFreeTile = tile.isFreeTile,
                                    isMarked = tile.isMarked,
                                    isWinningTile = isWinning,
                                    isMatchingCalledTile = isMatching,
                                    onClick = {
                                        // Submit Daub Move to Engine for Anti-Cheat Validation
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
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }

                // 4. CLAIM BINGO BUTTON
                AnimatedVisibility(
                    visible = completedLines.size >= 5,
                    enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.8f) + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut(tween(300)) + slideOutVertically() + shrinkVertically()
                ) {
                    AaaGlossyButton(
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
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color(0xFF100326),
                        borderColor = Color(0xFFFFF59D)
                    ) {
                        Text(
                            text = "🎉 CLAIM BINGO NOW!",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Anti-Cheat Alert Snackbar / Banner
                if (antiCheatAlert != null) {
                    Surface(
                        color = Color(0xFFFF1744).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = antiCheatAlert ?: "", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { engine.clearAntiCheatAlert() }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }
            }

            // 5. RECONNECTING OVERLAY
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

            // 6. VICTORY / DEFEAT REWARD OVERLAY
            if (matchStatus == BingoMatchStatus.VICTORY || matchStatus == BingoMatchStatus.DEFEAT) {
                if (matchStatus == BingoMatchStatus.VICTORY) {
                    AaaVictoryVfxCanvas()
                }
                BingoOnlineRewardPopup(
                    matchStatus = matchStatus,
                    onClaimed = { coinsDelta, wasAdClaimed ->
                        showRewardPopup = false
                        isResultProcessed = true
                        progressionRepo.processMatchResult(
                            matchType = "ONLINE_1V1",
                            difficulty = "RANKED",
                            opponentName = opponent?.displayName ?: "Online Opponent",
                            result = if (matchStatus == BingoMatchStatus.VICTORY) "VICTORY" else "DEFEAT",
                            durationSeconds = 45,
                            numbersCalledCount = currentRoom?.calledNumbersHistory?.size ?: 12,
                            coinRewardOverride = coinsDelta
                        )
                        engine.cancelMatchmaking()
                        onBackToLobby()
                    }
                )
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

private fun isTileInWinningLine(r: Int, c: Int, lines: Set<BingoLineType>): Boolean {
    if (lines.contains(BingoLineType.ROW_0) && r == 0) return true
    if (lines.contains(BingoLineType.ROW_1) && r == 1) return true
    if (lines.contains(BingoLineType.ROW_2) && r == 2) return true
    if (lines.contains(BingoLineType.ROW_3) && r == 3) return true
    if (lines.contains(BingoLineType.ROW_4) && r == 4) return true

    if (lines.contains(BingoLineType.COL_0) && c == 0) return true
    if (lines.contains(BingoLineType.COL_1) && c == 1) return true
    if (lines.contains(BingoLineType.COL_2) && c == 2) return true
    if (lines.contains(BingoLineType.COL_3) && c == 3) return true
    if (lines.contains(BingoLineType.COL_4) && c == 4) return true

    if (lines.contains(BingoLineType.DIAG_MAIN) && r == c) return true
    if (lines.contains(BingoLineType.DIAG_ANTI) && r + c == 4) return true

    return false
}

@Composable
private fun BingoOnlineRewardPopup(
    matchStatus: BingoMatchStatus,
    onClaimed: (coinsDelta: Int, wasAdClaimed: Boolean) -> Unit
) {
    val context = LocalContext.current
    val isWin = matchStatus == BingoMatchStatus.VICTORY
    val baseCoins = if (isWin) 12 else 3

    Dialog(
        onDismissRequest = {}, // Force user to choose
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color(0xFFFFD700)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13092D))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🎁 MATCH COMPLETED REWARD 🎁",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (isWin) "🎉 ONLINE VICTORY!" else "💔 Match Finished!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Coins Earned",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )

                    Text(
                        text = "$baseCoins COINS",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(color = Color(0xFFFFD700).copy(alpha = 0.6f), blurRadius = 8f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Button 1: Claim
                    AaaGlossyButton(
                        onClick = {
                            onClaimed(baseCoins, false)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        containerColor = Color(0xFF334155),
                        contentColor = Color.White,
                        borderColor = Color(0xFF64748B)
                    ) {
                        Text("CLAIM REWARD ($baseCoins COINS)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Button 2: Watch Ad x2 Reward
                    AaaGlossyButton(
                        onClick = {
                            val activity = context as? Activity ?: run {
                                var actContext = context
                                while (actContext is android.content.ContextWrapper) {
                                    if (actContext is Activity) break
                                    actContext = actContext.baseContext
                                }
                                actContext as? Activity
                            }
                            if (activity != null && com.playwin.ads.RewardedManager.isAdReady(context)) {
                                com.playwin.ads.RewardedManager.showAd(
                                    activity = activity,
                                    rewardType = com.playwin.ads.RewardType.BINGO_DOUBLE_REWARD,
                                    callbacks = object : com.playwin.ads.RewardCallback {
                                        override fun onRewardEarned(rewardType: com.playwin.ads.RewardType, amount: Int, token: String) {
                                            onClaimed(baseCoins * 2, true)
                                        }
                                        override fun onAdFailedToLoad(errorCode: Int, errorMessage: String) {
                                            Toast.makeText(context, "Ad is currently unavailable. Please try again in a moment.", Toast.LENGTH_SHORT).show()
                                        }
                                        override fun onAdFailedToShow(errorMessage: String) {
                                            Toast.makeText(context, "Ad is currently unavailable. Please try again in a moment.", Toast.LENGTH_SHORT).show()
                                        }
                                        override fun onAdClosed(userEarnedReward: Boolean) {}
                                    }
                                )
                            } else {
                                Toast.makeText(context, "Ad is currently unavailable. Please try again in a moment.", Toast.LENGTH_SHORT).show()
                                com.playwin.ads.RewardedManager.preload(context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        containerColor = Color(0xFF00E676),
                        contentColor = Color(0xFF091E10),
                        borderColor = Color(0xFFB9F6CA)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WATCH AD (×2 REWARD - +${baseCoins * 2} COINS)", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

private fun listsToBoard(card: List<Int>, marked: List<Boolean>): List<List<BingoTile>> {
    val grid = MutableList(5) { r ->
        MutableList(5) { c ->
            val idx = r * 5 + c
            val num = if (idx < card.size) card[idx] else 0
            val isMarked = if (idx < marked.size) marked[idx] else (r == 2 && c == 2)
            val isFree = (r == 2 && c == 2)
            val letters = listOf("B", "I", "N", "G", "O")
            BingoTile(
                row = r,
                col = c,
                number = num,
                columnLetter = letters[c],
                isFreeTile = isFree,
                isMarked = isMarked || isFree
            )
        }
    }
    return grid
}

