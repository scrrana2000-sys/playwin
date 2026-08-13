package com.myplaywin.app.ui.screens

import android.content.Context
import com.myplaywin.app.ui.components.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.app.Activity
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ==========================================
// DATA MODELS & ENUMS FOR BINGO GAMEPLAY
// ==========================================

enum class BingoMatchState {
    COUNTDOWN,
    PLAYING,
    PAUSED,
    VICTORY,
    DEFEAT,
    DRAW,
    EXITED
}

data class AiPlayerProfile(
    val name: String,
    val avatarEmoji: String,
    val countryFlag: String,
    val level: Int,
    val winRate: Int,
    val badge: String,
    val personality: String = "Balanced"
)

fun generateRandomAiProfile(): AiPlayerProfile {
    val names = listOf("Sophia_Win", "Marco_Pro", "Elena_Bingo", "Lucas_Vip", "Yuki_Play", "Mateo_Star", "Aarav_Dauber", "Chloe_Lucky", "Oliver_Master", "Zoe_Ace")
    val emojis = listOf("🦁", "🐯", "🦊", "🐻", "🐼", "🦄", "🦅", "🐺", "🐲", "🤖", "👑", "⚡")
    val flags = listOf("🇺🇸", "🇬🇧", "🇧🇷", "🇯🇵", "🇩🇪", "🇮🇳", "🇨🇦", "🇪🇸", "🇦🇺", "🇫🇷")
    val badges = listOf("BINGO MASTER", "SUPERSTAR", "DAUBING PRO", "LUCKY STREAK", "PLAYWIN VIP")
    val personalities = listOf("Balanced", "Aggressive", "Fast Finisher", "Defensive", "Risk Taker", "Patient", "Smart Planner")

    return AiPlayerProfile(
        name = names.random(),
        avatarEmoji = emojis.random(),
        countryFlag = flags.random(),
        level = (18..85).random(),
        winRate = (48..75).random(),
        badge = badges.random(),
        personality = personalities.random()
    )
}

data class BingoTile(
    val row: Int,
    val col: Int,
    val number: Int, // 0 for FREE tile
    val columnLetter: String, // B, I, N, G, O
    val isFreeTile: Boolean = false,
    var isMarked: Boolean = false
)

enum class BingoLineType {
    ROW_0, ROW_1, ROW_2, ROW_3, ROW_4,
    COL_0, COL_1, COL_2, COL_3, COL_4,
    DIAG_MAIN, DIAG_ANTI
}

data class BingoConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float = 1.0f
)

enum class BingoTutorialStep {
    STEP_1_WATCH_CALL,
    STEP_2_TAP_NUMBER,
    STEP_3_MARK_SUCCESS,
    STEP_4_LINE_COMPLETE,
    STEP_5_CLAIM_WIN
}

fun generatePlayerAndAiBoards(): Pair<List<List<BingoTile>>, List<List<BingoTile>>> {
    val bNums = (1..15).shuffled().take(5)
    val iNums = (16..30).shuffled().take(5)
    val nNums = (31..45).shuffled().take(5)
    val gNums = (46..60).shuffled().take(5)
    val oNums = (61..75).shuffled().take(5)

    val playerBoard = List(5) { r ->
        List(5) { c ->
            val num = when (c) {
                0 -> bNums[r]
                1 -> iNums[r]
                2 -> if (r == 2) 0 else nNums[r]
                3 -> gNums[r]
                else -> oNums[r]
            }
            val letter = listOf("B", "I", "N", "G", "O")[c]
            BingoTile(row = r, col = c, number = num, columnLetter = letter, isFreeTile = (r == 2 && c == 2), isMarked = (r == 2 && c == 2))
        }
    }

    val nNumsForAi = nNums.toMutableList()
    nNumsForAi.removeAt(2) // remove middle element since (2,2) is free tile
    val allNumbers = (bNums + iNums + nNumsForAi + gNums + oNums).shuffled()
    var numIdx = 0
    val aiBoard = List(5) { r ->
        List(5) { c ->
            val num = if (r == 2 && c == 2) 0 else allNumbers[numIdx++]
            val letter = listOf("B", "I", "N", "G", "O")[c]
            BingoTile(row = r, col = c, number = num, columnLetter = letter, isFreeTile = (r == 2 && c == 2), isMarked = (r == 2 && c == 2))
        }
    }

    return Pair(playerBoard, aiBoard)
}

fun selectSmartAiTile(
    aiBoard: List<List<BingoTile>>,
    playerBoard: List<List<BingoTile>>,
    difficulty: String,
    personality: String
): BingoTile? {
    val uncalledTiles = aiBoard.flatten().filter { !it.isMarked && !it.isFreeTile }
    if (uncalledTiles.isEmpty()) return null

    fun countMarked(tiles: List<BingoTile>) = tiles.count { it.isMarked }

    val rankedTiles = uncalledTiles.map { tile ->
        var score = 0.0

        // 1. Evaluate AI board row progress
        val rowMarked = countMarked(aiBoard[tile.row])
        score += weightForLine(rowMarked, personality)

        // 2. Evaluate AI board column progress
        val colMarked = countMarked((0..4).map { r -> aiBoard[r][tile.col] })
        score += weightForLine(colMarked, personality)

        // 3. Evaluate Main Diagonal (top-left to bottom-right)
        if (tile.row == tile.col) {
            val diagMarked = countMarked((0..4).map { i -> aiBoard[i][i] })
            score += weightForLine(diagMarked, personality) * 1.25
        }

        // 4. Evaluate Anti Diagonal (top-right to bottom-left)
        if (tile.row + tile.col == 4) {
            val antiDiagMarked = countMarked((0..4).map { i -> aiBoard[i][4 - i] })
            score += weightForLine(antiDiagMarked, personality) * 1.25
        }

        // 5. Personality strategic adjustments
        when (personality) {
            "Fast Finisher" -> {
                if (rowMarked == 4 || colMarked == 4) score += 300.0
            }
            "Aggressive" -> {
                if (rowMarked >= 3) score += 60.0
                if (colMarked >= 3) score += 60.0
            }
            "Smart Planner" -> {
                var intersectCount = 0
                if (rowMarked >= 2) intersectCount++
                if (colMarked >= 2) intersectCount++
                if (tile.row == tile.col && countMarked((0..4).map { aiBoard[it][it] }) >= 2) intersectCount++
                if (tile.row + tile.col == 4 && countMarked((0..4).map { aiBoard[it][4 - it] }) >= 2) intersectCount++
                score += intersectCount * 45.0
            }
            "Defensive" -> {
                val playerTile = playerBoard.flatten().find { it.number == tile.number && !it.isMarked }
                if (playerTile != null) {
                    val pRowMarked = countMarked(playerBoard[playerTile.row])
                    val pColMarked = countMarked((0..4).map { r -> playerBoard[r][playerTile.col] })
                    if (pRowMarked == 4 || pColMarked == 4) {
                        score += 90.0
                    } else if (pRowMarked == 3 || pColMarked == 3) {
                        score += 35.0
                    }
                }
            }
            "Risk Taker" -> {
                if (tile.row == tile.col || tile.row + tile.col == 4) score += 50.0
            }
            "Patient" -> {
                if (rowMarked in 2..3) score += 40.0
                if (colMarked in 2..3) score += 40.0
            }
            else -> { // "Balanced"
                if (rowMarked >= 3) score += 35.0
                if (colMarked >= 3) score += 35.0
            }
        }

        tile to score
    }.sortedByDescending { it.second }

    if (rankedTiles.isEmpty()) return null

    return when (difficulty) {
        "Hard" -> {
            rankedTiles.first().first
        }
        "Medium" -> {
            val roll = (1..100).random()
            if (roll <= 85 || rankedTiles.size == 1) {
                rankedTiles.first().first
            } else {
                rankedTiles.take(3).random().first
            }
        }
        else -> { // Easy
            val roll = (1..100).random()
            if (roll <= 50 || rankedTiles.size == 1) {
                rankedTiles.first().first
            } else {
                uncalledTiles.random()
            }
        }
    }
}

private fun weightForLine(markedCount: Int, personality: String): Double {
    return when (markedCount) {
        4 -> if (personality == "Fast Finisher") 600.0 else 400.0
        3 -> 70.0
        2 -> 25.0
        1 -> 5.0
        else -> 1.0
    }
}

// ==========================================
// CORE GAMEPLAY SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoGamePlayScreen(
    onExitGame: () -> Unit,
    difficulty: String = "Medium"
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("bingo_game_prefs", Context.MODE_PRIVATE) }

    val gameStartTime = remember { System.currentTimeMillis() }

    LaunchedEffect(Unit) {
        com.playwin.ads.TelemetryManager.logGameActivity(
            game = "BINGO",
            eventType = "gameStarted",
            gameSessionDuration = 0L,
            rewardedAdShown = false,
            rewardEarned = false,
            coinsEarned = 0,
            score = 0
        )
    }

    // First-time Tutorial state
    var hasSeenTutorial by remember { mutableStateOf(prefs.getBoolean("has_seen_bingo_tutorial", false)) }
    var isTutorialActive by remember { mutableStateOf(false) }
    var currentTutorialStep by remember { mutableStateOf(BingoTutorialStep.STEP_1_WATCH_CALL) }

    // Check tutorial state on first screen load
    LaunchedEffect(Unit) {
        val completed = prefs.getBoolean("has_seen_bingo_tutorial", false)
        if (!completed) {
            isTutorialActive = true
            currentTutorialStep = BingoTutorialStep.STEP_1_WATCH_CALL
        } else {
            isTutorialActive = false
        }
    }

    // Match Flow State
    var matchState by remember { mutableStateOf(BingoMatchState.COUNTDOWN) }
    var countdownNumber by remember { mutableIntStateOf(3) }

    // Sound FX state
    var isSoundMuted by remember { mutableStateOf(false) }

    // Dialog flags
    var showPauseMenu by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    androidx.activity.compose.BackHandler(enabled = true) {
        showExitConfirmDialog = true
    }
    var showHowToPlayModal by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var showRewardPopup by remember { mutableStateOf(false) }
    var hasUsedSecondChance by remember { mutableStateOf(false) }
    var showSecondChanceDialog by remember { mutableStateOf(false) }

    // Match Timer state (seconds)
    var matchTimeSeconds by remember { mutableIntStateOf(0) }

    // Shaked tile key for wrong tap feedback (row * 5 + col)
    var shakingTileKey by remember { mutableStateOf<Int?>(null) }

    // Board state: 5x5 grid of BingoTiles (Player & AI generated with same numbers)
    val initialBoards = remember { generatePlayerAndAiBoards() }
    var boardTiles by remember { mutableStateOf(initialBoards.first) }
    var aiBoardTiles by remember { mutableStateOf(initialBoards.second) }

    // AI Opponent State
    var aiProfile by remember { mutableStateOf(generateRandomAiProfile()) }
    var aiCompletedLines by remember { mutableStateOf<Set<BingoLineType>>(emptySet()) }
    var aiDaubsCount by remember { mutableIntStateOf(1) } // Free center tile starts marked
    var aiStatusText by remember { mutableStateOf("Waiting...") }
    var isReplayModeActive by remember { mutableStateOf(false) }

    // Called Numbers State
    var calledNumbersHistory by remember { mutableStateOf<List<Int>>(emptyList()) }
    var activeCalledNumber by remember { mutableStateOf<Int?>(null) }

    // Traditional Turn State (Player vs AI)
    var isPlayerTurn by remember { mutableStateOf(true) }
    var turnStatusMessage by remember { mutableStateOf("👉 YOUR TURN! Tap any uncalled number on your board") }

    // Winning lines state
    var completedLines by remember { mutableStateOf<Set<BingoLineType>>(emptySet()) }
    var totalDaubsCount by remember { mutableIntStateOf(1) }

    // Initialize/Reset Game state
    fun startNewMatch() {
        isTutorialActive = false
        val (pBoard, aBoard) = generatePlayerAndAiBoards()
        boardTiles = pBoard
        aiBoardTiles = aBoard
        aiProfile = generateRandomAiProfile()
        aiCompletedLines = emptySet()
        aiDaubsCount = 1
        aiStatusText = "Waiting..."
        calledNumbersHistory = emptyList()
        activeCalledNumber = null
        isPlayerTurn = true
        turnStatusMessage = "👉 YOUR TURN! Tap any uncalled number on your board"
        completedLines = emptySet()
        totalDaubsCount = 1
        matchTimeSeconds = 0
        countdownNumber = 3
        matchState = BingoMatchState.COUNTDOWN
        showPauseMenu = false
        showExitConfirmDialog = false
        showHowToPlayModal = false
        isReplayModeActive = false
    }

    // Lifecycle Observer for saving / pausing state on background
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                if (matchState == BingoMatchState.PLAYING) {
                    matchState = BingoMatchState.PAUSED
                    showPauseMenu = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val progressionRepo = remember { com.myplaywin.app.data.repository.BingoProgressionRepository(context) }

    // Progression Engine match result processing
    var isResultProcessed by remember { mutableStateOf(false) }
    LaunchedEffect(matchState) {
        if (!isResultProcessed) {
            when (matchState) {
                BingoMatchState.VICTORY, BingoMatchState.DEFEAT, BingoMatchState.DRAW -> {
                    showRewardPopup = true
                }
                else -> {}
            }
        }
    }

    // Match Countdown Flow
    LaunchedEffect(matchState) {
        if (matchState == BingoMatchState.COUNTDOWN) {
            countdownNumber = 3
            delay(800L)
            countdownNumber = 2
            delay(800L)
            countdownNumber = 1
            delay(800L)
            countdownNumber = 0 // "GO!"
            delay(600L)
            matchState = BingoMatchState.PLAYING
        }
    }

    // Match Timer Coroutine
    LaunchedEffect(matchState) {
        if (matchState == BingoMatchState.PLAYING) {
            while (matchState == BingoMatchState.PLAYING) {
                delay(1000L)
                matchTimeSeconds++
            }
        }
    }

    // Function to check winning lines
    fun evaluateCompletedLines(currentBoard: List<List<BingoTile>>): Set<BingoLineType> {
        val lines = mutableSetOf<BingoLineType>()

        // Check 5 Rows
        for (r in 0..4) {
            if (currentBoard[r].all { it.isMarked }) {
                lines.add(when (r) {
                    0 -> BingoLineType.ROW_0
                    1 -> BingoLineType.ROW_1
                    2 -> BingoLineType.ROW_2
                    3 -> BingoLineType.ROW_3
                    else -> BingoLineType.ROW_4
                })
            }
        }

        // Check 5 Columns
        for (c in 0..4) {
            if ((0..4).all { r -> currentBoard[r][c].isMarked }) {
                lines.add(when (c) {
                    0 -> BingoLineType.COL_0
                    1 -> BingoLineType.COL_1
                    2 -> BingoLineType.COL_2
                    3 -> BingoLineType.COL_3
                    else -> BingoLineType.COL_4
                })
            }
        }

        // Check Main Diagonal (Top-Left to Bottom-Right)
        if ((0..4).all { i -> currentBoard[i][i].isMarked }) {
            lines.add(BingoLineType.DIAG_MAIN)
        }

        // Check Anti Diagonal (Top-Right to Bottom-Left)
        if ((0..4).all { i -> currentBoard[i][4 - i].isMarked }) {
            lines.add(BingoLineType.DIAG_ANTI)
        }

        return lines
    }

    // ==========================================
    // TRADITIONAL TURN-BASED AI ENGINE
    // ==========================================
    LaunchedEffect(isPlayerTurn, matchState) {
        if (matchState == BingoMatchState.PLAYING && !isPlayerTurn) {
            aiStatusText = "Thinking..."
            turnStatusMessage = "🤖 ${aiProfile.name} is thinking & picking a number..."

            // AI thinking delay: 2 to 4 seconds
            val delayMs = (2000L..3800L).random()
            delay(delayMs)

            if (matchState == BingoMatchState.PLAYING && !isPlayerTurn) {
                val targetTile = selectSmartAiTile(aiBoardTiles, boardTiles, difficulty, aiProfile.personality)
                    ?: aiBoardTiles.flatten().filter { !it.isMarked && !it.isFreeTile }.randomOrNull()

                if (targetTile != null) {
                    val aiNum = targetTile.number
                    val colLetter = columnLetterForNum(aiNum)

                    activeCalledNumber = aiNum
                    calledNumbersHistory = listOf(aiNum) + calledNumbersHistory
                    AaaBingoAudioHaptics.playBallCallPopSound()

                    // Mark on AI board
                    aiBoardTiles = aiBoardTiles.map { row ->
                        row.map { tile ->
                            if (tile.row == targetTile.row && tile.col == targetTile.col) {
                                tile.copy(isMarked = true)
                            } else tile
                        }
                    }
                    aiDaubsCount = aiBoardTiles.flatten().count { it.isMarked }

                    // Mark on Player board if matching
                    boardTiles = boardTiles.map { row ->
                        row.map { tile ->
                            if (tile.number == aiNum) {
                                tile.copy(isMarked = true)
                            } else tile
                        }
                    }
                    totalDaubsCount = boardTiles.flatten().count { it.isMarked }

                    // Re-evaluate lines
                    val newAiLines = evaluateCompletedLines(aiBoardTiles)
                    aiCompletedLines = newAiLines
                    val newPLines = evaluateCompletedLines(boardTiles)
                    completedLines = newPLines

                    aiStatusText = "Marked $colLetter-$aiNum!"

                    if (newAiLines.size >= 5) {
                        turnStatusMessage = "💔 ${aiProfile.name} completed B-I-N-G-O!"
                        aiStatusText = "BINGO! 🌟"
                        delay(600L)
                        if (completedLines.size >= 4 && !hasUsedSecondChance) {
                            showSecondChanceDialog = true
                        } else {
                            matchState = BingoMatchState.DEFEAT
                        }
                        return@LaunchedEffect
                    }
                    if (newPLines.size >= 5) {
                        turnStatusMessage = "🎉 BINGO! You completed B-I-N-G-O!"
                        delay(600L)
                        matchState = BingoMatchState.VICTORY
                        return@LaunchedEffect
                    }

                    turnStatusMessage = "🤖 AI Called $colLetter-$aiNum! ➔ YOUR TURN!"
                } else {
                    // All numbers called
                    if (completedLines.size > aiCompletedLines.size) {
                        matchState = BingoMatchState.VICTORY
                    } else if (aiCompletedLines.size > completedLines.size) {
                        if (completedLines.size >= 4 && !hasUsedSecondChance) {
                            showSecondChanceDialog = true
                        } else {
                            matchState = BingoMatchState.DEFEAT
                        }
                    } else {
                        matchState = BingoMatchState.DRAW
                    }
                }

                // Turn finishes, pass back to Player
                isPlayerTurn = true
            }
        }
    }

    // Tile Click Handler (Player selects number from board on their turn)
    fun handleTileClick(tile: BingoTile) {
        if (matchState != BingoMatchState.PLAYING) return

        if (!isPlayerTurn) {
            notificationMessage = "⏳ Wait for AI's turn to finish!"
            coroutineScope.launch {
                delay(1800)
                if (notificationMessage?.startsWith("⏳") == true) notificationMessage = null
            }
            return
        }

        if (tile.isFreeTile) {
            notificationMessage = "★ FREE center tile is already daubed!"
            coroutineScope.launch {
                delay(1500)
                if (notificationMessage?.contains("FREE") == true) notificationMessage = null
            }
            return
        }

        if (tile.isMarked) {
            notificationMessage = "⚠️ Number ${columnLetterForNum(tile.number)}-${tile.number} is already marked!"
            coroutineScope.launch {
                delay(1800)
                if (notificationMessage?.startsWith("⚠️") == true) notificationMessage = null
            }
            return
        }

        // Correct Player Selection!
        val num = tile.number
        val colLetter = columnLetterForNum(num)
        activeCalledNumber = num
        calledNumbersHistory = listOf(num) + calledNumbersHistory
        AaaBingoAudioHaptics.playTileDaubSound()

        // Mark on Player Board
        boardTiles = boardTiles.map { row ->
            row.map { item ->
                if (item.row == tile.row && item.col == tile.col) {
                    item.copy(isMarked = true)
                } else item
            }
        }
        totalDaubsCount = boardTiles.flatten().count { it.isMarked }

        // Mark on AI Board
        aiBoardTiles = aiBoardTiles.map { row ->
            row.map { item ->
                if (item.number == num) {
                    item.copy(isMarked = true)
                } else item
            }
        }
        aiDaubsCount = aiBoardTiles.flatten().count { it.isMarked }

        // Re-evaluate lines
        val newPLines = evaluateCompletedLines(boardTiles)
        completedLines = newPLines
        val newAiLines = evaluateCompletedLines(aiBoardTiles)
        aiCompletedLines = newAiLines

        if (newPLines.size >= 5) {
            turnStatusMessage = "🎉 BINGO! You earned all 5 letters (B-I-N-G-O)!"
            notificationMessage = "🏆 BINGO VICTORY!"
            matchState = BingoMatchState.VICTORY
            return
        }
        if (newAiLines.size >= 5) {
            turnStatusMessage = "💔 ${aiProfile.name} earned B-I-N-G-O!"
            if (completedLines.size >= 4 && !hasUsedSecondChance) {
                showSecondChanceDialog = true
            } else {
                matchState = BingoMatchState.DEFEAT
            }
            return
        }

        turnStatusMessage = "Called $colLetter-$num! ➔ 🤖 AI's Turn..."
        aiStatusText = "Thinking..."

        // Switch turn to AI
        isPlayerTurn = false
    }

    // Claim BINGO Button Handler
    fun handleClaimBingo() {
        if (matchState != BingoMatchState.PLAYING) return

        if (completedLines.size >= 5) {
            AaaBingoAudioHaptics.playVictoryFanfare()
            matchState = BingoMatchState.VICTORY
        } else if (completedLines.isNotEmpty()) {
            val remaining = 5 - completedLines.size
            notificationMessage = "⚡ Completed ${completedLines.size}/5 lines! Need $remaining more line(s) for BINGO!"
            coroutineScope.launch {
                delay(2200)
                if (notificationMessage?.startsWith("⚡") == true) notificationMessage = null
            }
        } else {
            AaaBingoAudioHaptics.playWrongTileSound()
            notificationMessage = "⚠️ Complete lines to earn B - I - N - G - O letters!"
            coroutineScope.launch {
                delay(2200)
                if (notificationMessage?.startsWith("⚠️") == true) notificationMessage = null
            }
        }
    }

    // Infinite ambient animations
    val infiniteTransition = rememberInfiniteTransition(label = "BingoGameplayInfinite")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RayRotation"
    )

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
                            text = "BINGO",
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
                    IconButton(onClick = { showHowToPlayModal = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "How to Play / Help",
                            tint = Color(0xFFFFD700)
                        )
                    }
                    IconButton(onClick = { isSoundMuted = !isSoundMuted }) {
                        Icon(
                            imageVector = if (isSoundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Toggle Mute",
                            tint = Color(0xFFFFD700)
                        )
                    }
                    IconButton(onClick = {
                        if (matchState == BingoMatchState.PLAYING) {
                            matchState = BingoMatchState.PAUSED
                            showPauseMenu = true
                        } else if (matchState == BingoMatchState.PAUSED) {
                            showPauseMenu = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = "Pause Game",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
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
                // Main Gameplay Scrollable Content
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

                    // ACTIVE NUMBER BOARD MATCH HINT DEFINITIONS (Required for BingoBoardGrid)
                    val matchingTile = remember(activeCalledNumber, boardTiles) {
                        activeCalledNumber?.let { num ->
                            boardTiles.flatten().find { it.number == num && !it.isMarked && !it.isFreeTile }
                        }
                    }
                    val matchingTileKey = matchingTile?.let { it.row * 5 + it.col }

                    // 2. 5x5 BINGO BOARD GRID
                    BingoBoardGrid(
                        boardTiles = boardTiles,
                        completedLines = completedLines,
                        shakingTileKey = shakingTileKey,
                        matchingTileKey = matchingTileKey,
                        glowAlpha = glowAlpha,
                        onTileClick = { handleTileClick(it) }
                    )

                    // 3. SHARED TURN SYNCHRONIZATION BAR ("YOUR TURN TO CALL")
                    BingoTurnSynchronizationHeader(
                        isPlayerTurn = isPlayerTurn,
                        turnStatusMessage = turnStatusMessage,
                        playerCompletedLinesCount = completedLines.size,
                        aiCompletedLinesCount = aiCompletedLines.size,
                        aiName = aiProfile.name
                    )

                    // 4. LIVE GOAL & REAL-TIME PROGRESS INDICATOR ("B-I-N-G-O GOAL")
                    BingoGoalAndProgressHeader(
                        completedLines = completedLines
                    )

                    // 5. CLAIM BINGO BUTTON
                    BingoClaimButton(
                        hasBingo = completedLines.size >= 5,
                        glowAlpha = glowAlpha,
                        onClick = { handleClaimBingo() }
                    )

                    // 6. RECENTLY CALLED NUMBERS BAR
                    BingoCalledNumbersBar(
                        calledNumbersHistory = calledNumbersHistory,
                        activeCalledNumber = activeCalledNumber,
                        glowAlpha = glowAlpha
                    )

                    // 7. ACTIVE CALLED BALL DISPLAY (GLOWING POPUP BALL)
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
                                text = "ℹ️ Number ${columnLetterForNum(activeCalledNumber!!)}-$activeCalledNumber is not on your board",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // 9. Floating Toast / Notification Banner
                    AnimatedVisibility(
                        visible = notificationMessage != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        notificationMessage?.let { msg ->
                            Surface(
                                color = if (msg.contains("❌")) Color(0xFFFF1744) else if (msg.contains("⚠️")) Color(0xFFFF9100) else Color(0xFF00E676),
                                shape = RoundedCornerShape(20.dp),
                                shadowElevation = 6.dp
                            ) {
                                Text(
                                    text = msg,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // 10. PLAYER STATUS SECTION (At the very bottom)
                    // 10B. LIVE AI OPPONENT HUD CARD
                    BingoAiOpponentHeader(
                        aiProfile = aiProfile,
                        aiStatusText = aiStatusText,
                        aiCompletedLinesCount = aiCompletedLines.size,
                        aiDaubsCount = aiDaubsCount
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // First Time Interactive Tutorial Overlay
                if (isTutorialActive) {
                    val matchingTileForTut = remember(activeCalledNumber, boardTiles) {
                        activeCalledNumber?.let { num ->
                            boardTiles.flatten().find { it.number == num && !it.isMarked && !it.isFreeTile }
                        }
                    }
                    BingoTutorialOverlay(
                        currentStep = currentTutorialStep,
                        activeCalledNumber = activeCalledNumber,
                        matchingTile = matchingTileForTut,
                        onNextStep = {
                            when (currentTutorialStep) {
                                BingoTutorialStep.STEP_1_WATCH_CALL -> currentTutorialStep = BingoTutorialStep.STEP_2_TAP_NUMBER
                                BingoTutorialStep.STEP_2_TAP_NUMBER -> currentTutorialStep = BingoTutorialStep.STEP_3_MARK_SUCCESS
                                BingoTutorialStep.STEP_3_MARK_SUCCESS -> currentTutorialStep = BingoTutorialStep.STEP_4_LINE_COMPLETE
                                BingoTutorialStep.STEP_4_LINE_COMPLETE -> currentTutorialStep = BingoTutorialStep.STEP_5_CLAIM_WIN
                                BingoTutorialStep.STEP_5_CLAIM_WIN -> {
                                    isTutorialActive = false
                                    prefs.edit().putBoolean("has_seen_bingo_tutorial", true).commit()
                                    hasSeenTutorial = true
                                }
                            }
                        },
                        onSkipTutorial = {
                            isTutorialActive = false
                            prefs.edit().putBoolean("has_seen_bingo_tutorial", true).commit()
                            hasSeenTutorial = true
                        }
                    )
                }

                // 1. Countdown Overlay Flow
            if (matchState == BingoMatchState.COUNTDOWN) {
                BingoCountdownOverlay(countdownNumber = countdownNumber)
            }

            // 2. Victory Confetti & Match Outcome Overlay
            if (matchState == BingoMatchState.VICTORY) {
                AaaVictoryVfxCanvas()
            }

            if (showSecondChanceDialog) {
                BingoSecondChanceDialog(
                    onWatchAd = {
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
                                rewardType = com.playwin.ads.RewardType.BINGO_SECOND_CHANCE,
                                callbacks = object : com.playwin.ads.RewardCallback {
                                    override fun onRewardEarned(rewardType: com.playwin.ads.RewardType, amount: Int, token: String) {
                                        hasUsedSecondChance = true
                                        showSecondChanceDialog = false
                                        // Unmark 2 of AI's tiles to revert their victory lines
                                        val aiMarkedTiles = aiBoardTiles.flatten().filter { it.isMarked && !it.isFreeTile }
                                        if (aiMarkedTiles.isNotEmpty()) {
                                            aiMarkedTiles.shuffled().take(2).forEach { tile ->
                                                tile.isMarked = false
                                            }
                                        }
                                        aiCompletedLines = evaluateCompletedLines(aiBoardTiles)
                                        turnStatusMessage = "⚡ SECOND CHANCE ACTIVATED! AI's win was reverted! Keep playing!"
                                        isPlayerTurn = true
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
                    onClose = {
                        showSecondChanceDialog = false
                        matchState = BingoMatchState.DEFEAT
                    }
                )
            }

            if (matchState == BingoMatchState.VICTORY || matchState == BingoMatchState.DEFEAT || matchState == BingoMatchState.DRAW) {
                if (showRewardPopup) {
                    BingoRewardPopup(
                        matchState = matchState,
                        difficulty = difficulty,
                        onClaimed = { coinsDelta, wasAdClaimed ->
                            showRewardPopup = false
                            isResultProcessed = true
                            
                            val duration = (System.currentTimeMillis() - gameStartTime) / 1000L
                            com.playwin.ads.TelemetryManager.logGameActivity(
                                game = "BINGO",
                                eventType = "gameCompleted",
                                gameSessionDuration = duration,
                                rewardedAdShown = wasAdClaimed,
                                rewardEarned = (coinsDelta > 0),
                                coinsEarned = coinsDelta,
                                score = if (matchState == BingoMatchState.VICTORY) 1 else 0
                            )
                            
                            progressionRepo.processMatchResult(
                                matchType = "OFFLINE",
                                difficulty = difficulty.uppercase(),
                                opponentName = aiProfile.name,
                                result = matchState.name,
                                durationSeconds = matchTimeSeconds,
                                numbersCalledCount = calledNumbersHistory.size,
                                coinRewardOverride = coinsDelta
                            )
                            // Call onMatchCompleted on the live events repo
                            com.myplaywin.app.data.repository.BingoLiveEventsAndSocialRepository(context, progressionRepo).onMatchCompleted(
                                isWin = (matchState == BingoMatchState.VICTORY),
                                isOnline = false,
                                difficulty = difficulty.uppercase(),
                                numbersMarked = boardTiles.flatten().count { it.isMarked },
                                durationSeconds = matchTimeSeconds
                            )
                        }
                    )
                } else {
                    BingoPostMatchDialog(
                        matchState = matchState,
                        aiProfile = aiProfile,
                        matchTimeSeconds = matchTimeSeconds,
                        boardTiles = boardTiles,
                        aiBoardTiles = aiBoardTiles,
                        completedLines = completedLines,
                        aiCompletedLines = aiCompletedLines,
                        calledNumbersHistory = calledNumbersHistory,
                        difficulty = difficulty,
                        onPlayAgain = { startNewMatch() },
                        onReturnHome = {
                            onExitGame()
                        },
                        onWatchReplay = { isReplayModeActive = true }
                    )
                }
            }

            // 3. Match Replay Dialog
            if (isReplayModeActive) {
                BingoReplayDialog(
                    aiProfile = aiProfile,
                    playerBoardInitial = initialBoards.first,
                    aiBoardInitial = initialBoards.second,
                    calledNumbersHistory = calledNumbersHistory,
                    onClose = { isReplayModeActive = false }
                )
            }

            // 4. Pause Menu Dialog
            if (showPauseMenu) {
                BingoPauseMenuDialog(
                    onResume = {
                        matchState = BingoMatchState.PLAYING
                        showPauseMenu = false
                    },
                    onRestart = {
                        startNewMatch()
                    },
                    onHowToPlay = {
                        showHowToPlayModal = true
                    },
                    onQuit = {
                        showPauseMenu = false
                        onExitGame()
                    }
                )
            }

            // Exit Confirmation Dialog
            if (showExitConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showExitConfirmDialog = false },
                    title = {
                        Text(
                            text = "EXIT MATCH?",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to quit this Bingo match? Your current board progress will be lost.",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showExitConfirmDialog = false
                                onExitGame()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744))
                        ) {
                            Text("EXIT", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showExitConfirmDialog = false }) {
                            Text("RESUME MATCH", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFF1C133D),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // How To Play Modal
            if (showHowToPlayModal) {
                GameplayHowToPlayDialog(
                    onDismiss = { showHowToPlayModal = false },
                    onReplayTutorial = {
                        showHowToPlayModal = false
                        isTutorialActive = true
                        currentTutorialStep = BingoTutorialStep.STEP_1_WATCH_CALL
                    }
                )
            }
        }
    }
}
}

// ==========================================
// COMPONENT: SHARED TURN SYNCHRONIZATION HEADER
// ==========================================
@Composable
fun BingoTurnSynchronizationHeader(
    isPlayerTurn: Boolean,
    turnStatusMessage: String,
    playerCompletedLinesCount: Int,
    aiCompletedLinesCount: Int,
    aiName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isPlayerTurn) Color(0xFFFFD700) else Color(0xFF29B6F6)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlayerTurn) Color(0xFF1E113C) else Color(0xFF0D1B3E)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Header Title & Whose Turn Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = if (isPlayerTurn) "👉" else "🤖", fontSize = 16.sp)
                    Text(
                        text = if (isPlayerTurn) "YOUR TURN TO CALL" else "$aiName'S TURN",
                        color = if (isPlayerTurn) Color(0xFFFFD700) else Color(0xFF80D8FF),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = if (isPlayerTurn) Color(0xFF00E676) else Color(0xFF29B6F6),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isPlayerTurn) "YOUR TURN" else "AI THINKING...",
                        color = Color(0xFF100326),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Status message
            Surface(
                color = Color(0xFF231648),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = turnStatusMessage,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            // B-I-N-G-O Letters Badges for both Player & AI
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BingoLetterBadgesRow(
                    title = "👤 YOU:",
                    completedLinesCount = playerCompletedLinesCount,
                    badgeColor = Color(0xFFFFD700)
                )

                BingoLetterBadgesRow(
                    title = "🤖 AI ($aiName):",
                    completedLinesCount = aiCompletedLinesCount,
                    badgeColor = Color(0xFF29B6F6)
                )
            }
        }
    }
}

@Composable
private fun BingoLetterBadgesRow(
    title: String,
    completedLinesCount: Int,
    badgeColor: Color
) {
    val letters = listOf("B", "I", "N", "G", "O")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            letters.forEachIndexed { index, letter ->
                val isEarned = completedLinesCount >= index + 1
                Surface(
                    color = if (isEarned) badgeColor else Color(0xFF281C48),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isEarned) Color.White else Color(0xFF523B8A)
                    ),
                    modifier = Modifier.size(width = 28.dp, height = 24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = letter,
                            color = if (isEarned) Color.Black else Color.Gray,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 1: PLAYER HUD HEADER
// ==========================================
@Composable
private fun BingoMatchPlayerHeader(
    matchTimeSeconds: Int,
    completedLinesCount: Int,
    difficulty: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1238).copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFF3F51B5))),
                            CircleShape
                        )
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎯", fontSize = 18.sp)
                }

                Column {
                    Text(
                        text = "PlayWin Champion",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Mode: $difficulty",
                        color = Color(0xFF80D8FF),
                        fontSize = 10.sp
                    )
                }
            }

            // Match Timer Box
            Surface(
                color = Color(0xFF26194E),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formatTime(matchTimeSeconds),
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            // Lines Score Box
            Surface(
                color = Color(0xFF00E676).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF00E676))
            ) {
                Text(
                    text = "$completedLinesCount LINES",
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ==========================================
// COMPONENT 2: RECENTLY CALLED NUMBERS BAR
// ==========================================
@Composable
fun BingoCalledNumbersBar(
    calledNumbersHistory: List<Int>,
    activeCalledNumber: Int?,
    glowAlpha: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF3F2B75)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2D))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENTLY CALLED",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${calledNumbersHistory.size}/75 Called",
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (calledNumbersHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Waiting for first number call...",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(calledNumbersHistory.take(12)) { num ->
                        val isLatest = num == activeCalledNumber
                        val letter = columnLetterForNum(num)
                        val ballColor = colorForColumnLetter(letter)

                        Box(
                            modifier = Modifier
                                .size(if (isLatest) 40.dp else 34.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(ballColor, ballColor.copy(alpha = 0.6f))
                                    ),
                                    CircleShape
                                )
                                .border(
                                    if (isLatest) 2.dp else 1.dp,
                                    if (isLatest) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                                    CircleShape
                                )
                                .shadow(if (isLatest) 8.dp else 2.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = letter,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = if (isLatest) 8.sp else 7.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "$num",
                                    color = Color.White,
                                    fontSize = if (isLatest) 13.sp else 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 3: ACTIVE CALLED BALL ANNOUNCER
// ==========================================
@Composable
fun BingoActiveBallAnnouncer(
    activeNumber: Int?,
    glowAlpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        contentAlignment = Alignment.Center
    ) {
        if (activeNumber == null) {
            Text(
                text = "GET READY!",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 2.sp
            )
        } else {
            val letter = columnLetterForNum(activeNumber)
            val ballColor = colorForColumnLetter(letter)

            // Animated Scale Entry
            val scaleAnim by animateFloatAsState(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "BallEntryScale"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.scale(scaleAnim)
            ) {
                // Giant Glowing 3D Called Ball
                Aaa3dBingoBall(
                    letter = letter,
                    number = activeNumber,
                    modifier = Modifier.size(76.dp)
                )

                Column {
                    Text(
                        text = "NOW CALLING",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "$letter - $activeNumber",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(color = ballColor, blurRadius = 12f)
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 3B: GOAL & PROGRESS HEADER
// ==========================================
@Composable
fun BingoGoalAndProgressHeader(
    completedLines: Set<BingoLineType>
) {
    val letters = listOf("B", "I", "N", "G", "O")
    val currentLetters = if (completedLines.isEmpty()) "None" else letters.take(completedLines.size.coerceAtMost(5)).joinToString(" - ")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0F38))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Live Goal Statement
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🎯", fontSize = 15.sp)
                Text(
                    text = "B-I-N-G-O GOAL:",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Complete 5 lines to unlock B-I-N-G-O and Win!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = Color(0xFF3F2B75))

            // Progress Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Letters Earned: $currentLetters",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "${completedLines.size} / 5 Lines",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ==========================================
// COMPONENT 4: 5x5 BINGO BOARD GRID
// ==========================================
@Composable
fun BingoBoardGrid(
    boardTiles: List<List<BingoTile>>,
    completedLines: Set<BingoLineType>,
    shakingTileKey: Int?,
    matchingTileKey: Int?,
    glowAlpha: Float,
    showTutorialHand: Boolean = false,
    onTileClick: (BingoTile) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFFFD700)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(
                    Color(0xFFFFD700).copy(alpha = glowAlpha),
                    Color(0xFF7C4DFF),
                    Color(0xFFFFD700).copy(alpha = 0.5f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF130B2E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Column Headers Row (B I N G O)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("B", "I", "N", "G", "O").forEach { letter ->
                    val color = colorForColumnLetter(letter)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .background(
                                Brush.verticalGradient(listOf(color, color.copy(alpha = 0.6f))),
                                RoundedCornerShape(10.dp)
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // 5x5 Tiles Grid
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    boardTiles.forEachIndexed { rowIndex, rowList ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowList.forEachIndexed { colIndex, tile ->
                                val tileKey = rowIndex * 5 + colIndex
                                val isShaking = shakingTileKey == tileKey
                                val isMatchingCalledTile = matchingTileKey == tileKey && !tile.isMarked
                                val isWinningTile = isTileInWinningLine(rowIndex, colIndex, completedLines)

                                BingoTileView(
                                    modifier = Modifier.weight(1f),
                                    tile = tile,
                                    isWinningTile = isWinningTile,
                                    isMatchingCalledTile = isMatchingCalledTile,
                                    isShaking = isShaking,
                                    glowAlpha = glowAlpha,
                                    showTutorialHand = showTutorialHand && isMatchingCalledTile,
                                    onClick = { onTileClick(tile) }
                                )
                            }
                        }
                    }
                }

                BingoLinesOverlay(
                    completedLines = completedLines,
                    gapHorizontal = 6.dp,
                    gapVertical = 6.dp,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
fun BingoTileView(
    modifier: Modifier = Modifier,
    tile: BingoTile,
    isWinningTile: Boolean,
    isMatchingCalledTile: Boolean,
    isShaking: Boolean,
    glowAlpha: Float,
    showTutorialHand: Boolean = false,
    onClick: () -> Unit
) {
    // Shake Animatable Offset X
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isShaking) {
        if (isShaking) {
            repeat(3) {
                shakeOffset.animateTo(12f, tween(50))
                shakeOffset.animateTo(-12f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    AaaBingoTile(
        number = tile.number,
        isFreeTile = tile.isFreeTile,
        isMarked = tile.isMarked,
        isWinningTile = isWinningTile,
        isMatchingCalledTile = isMatchingCalledTile,
        isWrongTapped = isShaking,
        showTutorialHand = showTutorialHand,
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .offset(x = shakeOffset.value.dp)
    )
}

// ==========================================
// COMPONENT 5: CLAIM BINGO BUTTON
// ==========================================
@Composable
fun BingoClaimButton(
    hasBingo: Boolean,
    glowAlpha: Float,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = hasBingo,
        enter = fadeIn(tween(400)) + scaleIn(
            initialScale = 0.75f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        ) + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut(tween(300)) + slideOutVertically() + shrinkVertically()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = Color(0xFFFFD700),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = "🏆 ALL 5 LINES COMPLETE! B-I-N-G-O UNLOCKED!",
                    color = Color(0xFF100326),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            val infiniteTransition = rememberInfiniteTransition(label = "ClaimButtonBounce")
            val bounceScale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.07f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "BounceScale"
            )

            AaaGlossyButton(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .scale(bounceScale),
                containerColor = Color(0xFFFFD700),
                contentColor = Color(0xFF100326),
                borderColor = Color(0xFFFFF59D)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🎉", fontSize = 24.sp)
                    Text(
                        text = "CLAIM BINGO NOW!",
                        color = Color(0xFF100326),
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        letterSpacing = 1.5.sp
                    )
                    Text(text = "👑", fontSize = 24.sp)
                }
            }
        }
    }
}

// ==========================================
// FIRST-TIME INTERACTIVE TUTORIAL OVERLAY
// ==========================================
@Composable
private fun BingoTutorialOverlay(
    currentStep: BingoTutorialStep,
    activeCalledNumber: Int?,
    matchingTile: BingoTile?,
    onNextStep: () -> Unit,
    onSkipTutorial: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.80f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFFFD700)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, Color(0xFFFFD700)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0A33))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header & Skip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFFD700),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "TUTORIAL: STEP ${currentStep.ordinal + 1} OF 5",
                            color = Color(0xFF100326),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    TextButton(onClick = onSkipTutorial) {
                        Text("SKIP TUTORIAL ✕", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Step Specific Visual Graphic & Guidance
                when (currentStep) {
                    BingoTutorialStep.STEP_1_WATCH_CALL -> {
                        Text(text = "🤝 🎲", fontSize = 48.sp)
                        Text(
                            text = "1. TRADITIONAL TURN-BASED BINGO",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "You and AI take turns calling numbers! Both players share the same set of numbers on different board layouts. You start first!",
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    BingoTutorialStep.STEP_2_TAP_NUMBER -> {
                        Text(text = "👇 🎯", fontSize = 48.sp)
                        Text(
                            text = "2. YOUR TURN: PICK A NUMBER",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tap any uncalled number on your board. That number becomes the active called ball and automatically marks on both boards!",
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    BingoTutorialStep.STEP_3_MARK_SUCCESS -> {
                        Text(text = "🤖 ⚡", fontSize = 48.sp)
                        Text(
                            text = "3. AI'S TURN TO PICK",
                            color = Color(0xFF80D8FF),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "AI will think for 2-4 seconds and pick a number from its hidden board. If you have that number on your board, it automatically marks for you!",
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    BingoTutorialStep.STEP_4_LINE_COMPLETE -> {
                        Text(text = "🔤 🌟", fontSize = 48.sp)
                        Text(
                            text = "4. EARN B - I - N - G - O",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Every completed line (row, col, or diag) unlocks a letter: Line 1 = B, Line 2 = I, Line 3 = N, Line 4 = G, Line 5 = O!",
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    BingoTutorialStep.STEP_5_CLAIM_WIN -> {
                        Text(text = "🏆 👑", fontSize = 48.sp)
                        Text(
                            text = "5. UNLOCK ALL 5 LETTERS TO WIN!",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "The first player to unlock all 5 letters (B - I - N - G - O) wins the match! Tap CLAIM BINGO once all 5 lines are complete!",
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Action Button
                Button(
                    onClick = onNextStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = if (currentStep == BingoTutorialStep.STEP_5_CLAIM_WIN) "LET'S PLAY BINGO!" else "NEXT STEP ➔",
                        color = Color(0xFF100326),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// PAUSE & VICTORY DIALOGS
// ==========================================

@Composable
private fun BingoPauseMenuDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHowToPlay: () -> Unit,
    onQuit: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181033))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "GAME PAUSED",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.2.sp
                )

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Text("RESUME GAME", color = Color.Black, fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Text("RESTART MATCH", color = Color.White, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onHowToPlay,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700))
                ) {
                    Text("HOW TO PLAY", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onQuit,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF1744))
                ) {
                    Text("QUIT MATCH", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BingoAiOpponentHeader(
    aiProfile: AiPlayerProfile,
    aiStatusText: String,
    aiCompletedLinesCount: Int,
    aiDaubsCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFE040FB).copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF210936).copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Opponent Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFFE040FB), Color(0xFF3F51B5))),
                            CircleShape
                        )
                        .border(1.5.dp, Color(0xFFFF80AB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = aiProfile.avatarEmoji, fontSize = 18.sp)
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = aiProfile.countryFlag, fontSize = 12.sp)
                        Text(
                            text = aiProfile.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Lv.${aiProfile.level}",
                            color = Color(0xFFFF80AB),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "• ${aiProfile.badge}",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // AI Status Pill
            Surface(
                color = when {
                    aiStatusText.contains("BINGO") -> Color(0xFFFFD700).copy(alpha = 0.3f)
                    aiStatusText.contains("Marking") -> Color(0xFF00E676).copy(alpha = 0.2f)
                    else -> Color(0xFFE040FB).copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    when {
                        aiStatusText.contains("BINGO") -> Color(0xFFFFD700)
                        aiStatusText.contains("Marking") -> Color(0xFF00E676)
                        else -> Color(0xFFE040FB)
                    }
                )
            ) {
                Text(
                    text = aiStatusText,
                    color = when {
                        aiStatusText.contains("BINGO") -> Color(0xFFFFD700)
                        aiStatusText.contains("Marking") -> Color(0xFF00E676)
                        else -> Color(0xFFFF80AB)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun BingoMiniBoardGrid(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    boardTiles: List<List<BingoTile>>,
    completedLines: Set<BingoLineType>,
    primaryAccentColor: Color = Color(0xFF00E676),
    markedColor: Color = Color(0xFF00E5FF)
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11071F)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, primaryAccentColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, badgeColor)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // B I N G O Headers
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                listOf("B", "I", "N", "G", "O").forEach { letter ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF261142), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = letter, color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // 5x5 Tiles
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    boardTiles.forEachIndexed { r, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            row.forEachIndexed { c, tile ->
                                val isWinning = isTileInWinningLine(r, c, completedLines)
                                val tileBg = when {
                                    isWinning -> Color(0xFFFFD700)
                                    tile.isMarked -> markedColor
                                    else -> Color(0xFF201238)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(tileBg, RoundedCornerShape(5.dp))
                                        .border(
                                            1.dp,
                                            if (isWinning) Color(0xFFFFF59D) else if (tile.isMarked) primaryAccentColor else Color(0xFF381F5E),
                                            RoundedCornerShape(5.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (tile.isFreeTile) "★" else "${tile.number}",
                                        color = if (isWinning) Color.Black else if (tile.isMarked) Color.White else Color.LightGray,
                                        fontSize = 9.sp,
                                        fontWeight = if (tile.isMarked || isWinning) FontWeight.Black else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                BingoLinesOverlay(
                    completedLines = completedLines,
                    gapHorizontal = 3.dp,
                    gapVertical = 6.dp,
                    strokeWidthMultiplier = 0.5f,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
private fun BingoPostMatchDialog(
    matchState: BingoMatchState,
    aiProfile: AiPlayerProfile,
    matchTimeSeconds: Int,
    boardTiles: List<List<BingoTile>>,
    aiBoardTiles: List<List<BingoTile>>,
    completedLines: Set<BingoLineType>,
    aiCompletedLines: Set<BingoLineType>,
    calledNumbersHistory: List<Int>,
    difficulty: String,
    onPlayAgain: () -> Unit,
    onReturnHome: () -> Unit,
    onWatchReplay: () -> Unit
) {
    val isVictory = matchState == BingoMatchState.VICTORY
    val isDefeat = matchState == BingoMatchState.DEFEAT

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
        isVictory -> "BINGO VICTORY!"
        isDefeat -> "MATCH DEFEAT"
        else -> "MATCH DRAW!"
    }
    val titleEmoji = when {
        isVictory -> "👑🏆🎉"
        isDefeat -> "💔⚡"
        else -> "🤝🎲"
    }

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
                            else if (isDefeat) "${aiProfile.countryFlag} ${aiProfile.name} completed BINGO first."
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

                            StatRow("Match Duration", formatTime(matchTimeSeconds))
                            StatRow("Winner", if (isVictory) "You (Player)" else if (isDefeat) "${aiProfile.countryFlag} ${aiProfile.name}" else "Draw")
                            StatRow("Your Lines vs AI Lines", "${completedLines.size} Lines  vs  ${aiCompletedLines.size} Lines")
                            StatRow("Total Numbers Called", "${calledNumbersHistory.size} / 75")
                            StatRow("Difficulty Level", difficulty)
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

                // 5x5 Mini Grids Side-By-Side (Your Final Board vs AI Final Board)
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
                                title = aiProfile.name,
                                subtitle = "${aiProfile.countryFlag} AI Opponent",
                                badgeText = "${aiCompletedLines.size} Lines",
                                badgeColor = Color(0xFFE040FB),
                                boardTiles = aiBoardTiles,
                                completedLines = aiCompletedLines,
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
                        AaaGlossyButton(
                            onClick = onWatchReplay,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White,
                            borderColor = Color(0xFF38BDF8)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WATCH REPLAY (STEP-BY-STEP)", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AaaGlossyButton(
                                onClick = onReturnHome,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                containerColor = Color(0xFF334155),
                                contentColor = Color.White,
                                borderColor = Color(0xFF64748B)
                            ) {
                                Text("HOME", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            AaaGlossyButton(
                                onClick = onPlayAgain,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                containerColor = if (isVictory) Color(0xFFFFD700) else Color(0xFFFF1744),
                                contentColor = if (isVictory) Color.Black else Color.White,
                                borderColor = if (isVictory) Color(0xFFFFF59D) else Color(0xFFFF80AB)
                            ) {
                                Text(if (isVictory) "PLAY AGAIN" else "RETRY MATCH", fontWeight = FontWeight.Black, fontSize = 13.sp)
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
private fun BingoReplayDialog(
    aiProfile: AiPlayerProfile,
    playerBoardInitial: List<List<BingoTile>>,
    aiBoardInitial: List<List<BingoTile>>,
    calledNumbersHistory: List<Int>,
    onClose: () -> Unit
) {
    var replayStep by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var playbackSpeedMs by remember { mutableLongStateOf(700L) }

    LaunchedEffect(isPlaying, replayStep, playbackSpeedMs, calledNumbersHistory.size) {
        if (isPlaying && replayStep < calledNumbersHistory.size) {
            delay(playbackSpeedMs)
            replayStep += 1
        }
    }

    val calledNumbersAtStep = remember(replayStep, calledNumbersHistory) {
        calledNumbersHistory.take(replayStep).toSet()
    }

    val currentNum = remember(replayStep, calledNumbersHistory) {
        if (replayStep in 1..calledNumbersHistory.size) calledNumbersHistory[replayStep - 1] else null
    }

    val playerBoardCurrent = remember(calledNumbersAtStep, playerBoardInitial) {
        playerBoardInitial.map { row ->
            row.map { tile ->
                val isMarked = tile.isFreeTile || calledNumbersAtStep.contains(tile.number)
                tile.copy(isMarked = isMarked)
            }
        }
    }

    val aiBoardCurrent = remember(calledNumbersAtStep, aiBoardInitial) {
        aiBoardInitial.map { row ->
            row.map { tile ->
                val isMarked = tile.isFreeTile || calledNumbersAtStep.contains(tile.number)
                tile.copy(isMarked = isMarked)
            }
        }
    }

    val playerLinesAtStep = remember(playerBoardCurrent) { evaluateCompletedLines(playerBoardCurrent) }
    val aiLinesAtStep = remember(aiBoardCurrent) { evaluateCompletedLines(aiBoardCurrent) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, Color(0xFF00E5FF)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D061A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🎬", fontSize = 24.sp)
                        Column {
                            Text(
                                text = "MATCH REPLAY PLAYBACK",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Step $replayStep of ${calledNumbersHistory.size} Called Numbers",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Currently Called Ball Announcement
                Surface(
                    color = Color(0xFF1E0A3C),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentNum != null) {
                            val letter = columnLetterForNum(currentNum)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Aaa3dBingoBall(
                                    letter = letter,
                                    number = currentNum,
                                    modifier = Modifier.size(44.dp)
                                )
                                Column {
                                    Text(text = "CURRENTLY CALLED", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "$letter - $currentNum",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Text(text = "Press Play to start step-by-step match replay", fontSize = 12.sp, color = Color.LightGray)
                        }

                        // Playback Speed Toggle Button
                        Surface(
                            color = Color(0xFF2D184E),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable {
                                playbackSpeedMs = when (playbackSpeedMs) {
                                    1000L -> 500L
                                    500L -> 250L
                                    else -> 1000L
                                }
                            }
                        ) {
                            Text(
                                text = when (playbackSpeedMs) {
                                    1000L -> "1x Speed"
                                    500L -> "2x Speed"
                                    else -> "4x Speed"
                                },
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Side-by-Side Boards (Your Board vs AI Board)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        BingoMiniBoardGrid(
                            title = "YOUR BOARD",
                            subtitle = "Player Progress",
                            badgeText = "${playerLinesAtStep.size} Lines",
                            badgeColor = Color(0xFF00E676),
                            boardTiles = playerBoardCurrent,
                            completedLines = playerLinesAtStep,
                            primaryAccentColor = Color(0xFF00E676),
                            markedColor = Color(0xFF00E5FF)
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        BingoMiniBoardGrid(
                            title = aiProfile.name,
                            subtitle = "${aiProfile.countryFlag} AI Progress",
                            badgeText = "${aiLinesAtStep.size} Lines",
                            badgeColor = Color(0xFFE040FB),
                            boardTiles = aiBoardCurrent,
                            completedLines = aiLinesAtStep,
                            primaryAccentColor = Color(0xFFE040FB),
                            markedColor = Color(0xFFAB47BC)
                        )
                    }
                }

                // Step Slider & Controls Row
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Slider(
                        value = replayStep.toFloat(),
                        onValueChange = {
                            isPlaying = false
                            replayStep = it.toInt().coerceIn(0, calledNumbersHistory.size)
                        },
                        valueRange = 0f..calledNumbersHistory.size.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD700),
                            activeTrackColor = Color(0xFF00E5FF),
                            inactiveTrackColor = Color(0xFF2B1647)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isPlaying = false
                                replayStep = (replayStep - 1).coerceAtLeast(0)
                            }
                        ) {
                            Text(text = "⏮", fontSize = 22.sp)
                        }

                        AaaGlossyButton(
                            onClick = {
                                if (replayStep >= calledNumbersHistory.size) {
                                    replayStep = 0
                                }
                                isPlaying = !isPlaying
                            },
                            modifier = Modifier
                                .width(140.dp)
                                .height(44.dp),
                            containerColor = if (isPlaying) Color(0xFFFF1744) else Color(0xFF00E676),
                            contentColor = if (isPlaying) Color.White else Color.Black,
                            borderColor = Color.White
                        ) {
                            Text(
                                text = if (isPlaying) "⏸ PAUSE" else "▶ PLAY REPLAY",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        IconButton(
                            onClick = {
                                isPlaying = false
                                replayStep = (replayStep + 1).coerceAtMost(calledNumbersHistory.size)
                            }
                        ) {
                            Text(text = "⏭", fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.LightGray, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

// 60 FPS Confetti Fireworks Particles Canvas
@Composable
private fun BingoVictoryConfettiCanvas() {
    val particles = remember {
        List(40) {
            BingoConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.5f,
                vx = (Random.nextFloat() - 0.5f) * 0.02f,
                vy = Random.nextFloat() * 0.015f + 0.005f,
                color = listOf(
                    Color(0xFFFFD700), Color(0xFF00E676), Color(0xFF2979FF),
                    Color(0xFFE040FB), Color(0xFFFF1744)
                ).random(),
                size = Random.nextFloat() * 12f + 6f
            )
        }
    }

    var frame by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60 FPS
            particles.forEach { p ->
                p.y += p.vy
                p.x += p.vx
                if (p.y > 1.0f) p.y = 0.0f
            }
            frame++
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            drawCircle(
                color = p.color,
                radius = p.size,
                center = Offset(p.x * size.width, p.y * size.height)
            )
        }
    }
}

// ==========================================
// HELPER UTILITIES
// ==========================================

private fun generateFreshBingoBoard(): List<List<BingoTile>> {
    val bCols = (1..15).shuffled().take(5)
    val iCols = (16..30).shuffled().take(5)
    val nCols = (31..45).shuffled().take(4) // 4 numbers + FREE center
    val gCols = (46..60).shuffled().take(5)
    val oCols = (61..75).shuffled().take(5)

    var nIndex = 0

    return (0..4).map { r ->
        (0..4).map { c ->
            val isFree = (r == 2 && c == 2)
            val num = when (c) {
                0 -> bCols[r]
                1 -> iCols[r]
                2 -> if (isFree) 0 else nCols[nIndex++]
                3 -> gCols[r]
                else -> oCols[r]
            }
            val letter = when (c) {
                0 -> "B"
                1 -> "I"
                2 -> "N"
                3 -> "G"
                else -> "O"
            }
            BingoTile(
                row = r,
                col = c,
                number = num,
                columnLetter = letter,
                isFreeTile = isFree,
                isMarked = isFree
            )
        }
    }
}

private fun generateShuffledCalledPool(): List<Int> = (1..75).shuffled()

fun columnLetterForNum(num: Int): String = when (num) {
    in 1..15 -> "B"
    in 16..30 -> "I"
    in 31..45 -> "N"
    in 46..60 -> "G"
    else -> "O"
}

fun colorForColumnLetter(letter: String): Color = when (letter) {
    "B" -> Color(0xFF00E676)
    "I" -> Color(0xFF2979FF)
    "N" -> Color(0xFFFFD700)
    "G" -> Color(0xFFE040FB)
    else -> Color(0xFFFF1744)
}

private fun evaluateCompletedLines(currentBoard: List<List<BingoTile>>): Set<BingoLineType> {
    val lines = mutableSetOf<BingoLineType>()

    // Check 5 Rows
    for (r in 0..4) {
        if (currentBoard[r].all { it.isMarked }) {
            lines.add(when (r) {
                0 -> BingoLineType.ROW_0
                1 -> BingoLineType.ROW_1
                2 -> BingoLineType.ROW_2
                3 -> BingoLineType.ROW_3
                else -> BingoLineType.ROW_4
            })
        }
    }

    // Check 5 Columns
    for (c in 0..4) {
        if ((0..4).all { r -> currentBoard[r][c].isMarked }) {
            lines.add(when (c) {
                0 -> BingoLineType.COL_0
                1 -> BingoLineType.COL_1
                2 -> BingoLineType.COL_2
                3 -> BingoLineType.COL_3
                else -> BingoLineType.COL_4
            })
        }
    }

    // Check Main Diagonal
    if ((0..4).all { i -> currentBoard[i][i].isMarked }) {
        lines.add(BingoLineType.DIAG_MAIN)
    }

    // Check Anti Diagonal
    if ((0..4).all { i -> currentBoard[i][4 - i].isMarked }) {
        lines.add(BingoLineType.DIAG_ANTI)
    }

    return lines
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

fun isTileInWinningLine(r: Int, c: Int, lines: Set<BingoLineType>): Boolean {
    if (lines.isEmpty()) return false
    return lines.any { line ->
        when (line) {
            BingoLineType.ROW_0 -> r == 0
            BingoLineType.ROW_1 -> r == 1
            BingoLineType.ROW_2 -> r == 2
            BingoLineType.ROW_3 -> r == 3
            BingoLineType.ROW_4 -> r == 4
            BingoLineType.COL_0 -> c == 0
            BingoLineType.COL_1 -> c == 1
            BingoLineType.COL_2 -> c == 2
            BingoLineType.COL_3 -> c == 3
            BingoLineType.COL_4 -> c == 4
            BingoLineType.DIAG_MAIN -> r == c
            BingoLineType.DIAG_ANTI -> r + c == 4
        }
    }
}

@Composable
private fun GameplayHowToPlayDialog(
    onDismiss: () -> Unit,
    onReplayTutorial: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181033))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "HOW TO PLAY BINGO",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Surface(
                    color = Color(0xFF231648),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF7C4DFF))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "5x5 BINGO CARD RULES",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(5) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (it == 2) Color(0xFFFFD700) else Color(0xFF3F2B75),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (it == 2) "★" else "${(it + 1) * 7}",
                                        fontSize = 10.sp,
                                        color = if (it == 2) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier.size(28.dp).background(Color(0xFF7C4DFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("1", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Turn-Based Calling", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Player and AI take turns picking numbers. On your turn, tap any uncalled number on your board to call it!", color = Color.LightGray, fontSize = 11.sp)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier.size(28.dp).background(Color(0xFF7C4DFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("2", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Shared Marking", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("When a number is picked, it automatically marks on BOTH boards if present. AI thinks 2-4 seconds on its turn.", color = Color.LightGray, fontSize = 11.sp)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier.size(28.dp).background(Color(0xFF7C4DFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("3", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Earn B - I - N - G - O", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Complete 5 lines to unlock all 5 letters (B-I-N-G-O). First player to earn all 5 letters wins the match!", color = Color.LightGray, fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = {
                        onDismiss()
                        onReplayTutorial()
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Text("🎓 REPLAY INTERACTIVE TUTORIAL", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Text("RESUME MATCH", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// ==========================================
// PHASE 4 OVERLAYS & DIALOGS
// ==========================================

@Composable
private fun BingoCountdownOverlay(countdownNumber: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        val transition = rememberInfiniteTransition(label = "CountdownPulse")
        val scale by transition.animateFloat(
            initialValue = 0.88f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Scale"
        )

        Card(
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(3.dp, Color(0xFFFFD700)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E103E)),
            modifier = Modifier
                .padding(32.dp)
                .scale(scale)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "GET READY!",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (countdownNumber > 0) "$countdownNumber" else "GO!",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = if (countdownNumber > 0) 64.sp else 52.sp,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(color = Color(0xFFFF8F00), blurRadius = 16f)
                    )
                )
            }
        }
    }
}

@Composable
private fun BingoDrawDialog(
    aiProfile: AiPlayerProfile,
    matchTimeSeconds: Int,
    playerCompletedLines: Int,
    aiCompletedLines: Int,
    totalDaubs: Int,
    aiTotalDaubs: Int,
    calledCount: Int,
    difficulty: String,
    onPlayAgain: () -> Unit,
    onReturnHome: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(2.dp, Color(0xFFFF9100)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF22130C))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "🤝🎲", fontSize = 42.sp)

                Text(
                    text = "MATCH DRAW!",
                    color = Color(0xFFFF9100),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 1.5.sp,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(color = Color(0xFFFF6D00), blurRadius = 12f)
                    )
                )

                Text(
                    text = "All 75 numbers called without achieving a Bingo line.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = Color(0xFF331D12),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF6D00).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "POST-MATCH SUMMARY",
                            color = Color(0xFFFF9100),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        HorizontalDivider(color = Color(0xFFFF9100).copy(alpha = 0.3f))

                        StatRow("Match Duration", formatTime(matchTimeSeconds))
                        StatRow("Opponent", "${aiProfile.countryFlag} ${aiProfile.name}")
                        StatRow("Your Completed Lines", "$playerCompletedLines Lines")
                        StatRow("AI Completed Lines", "$aiCompletedLines Lines")
                        StatRow("Your Total Daubs", "$totalDaubs Tiles")
                        StatRow("AI Total Daubs", "$aiTotalDaubs Tiles")
                        StatRow("Numbers Called", "$calledCount / 75")
                        StatRow("Difficulty Level", difficulty)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReturnHome,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("HOME", color = Color.White)
                    }

                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100))
                    ) {
                        Text("PLAY AGAIN", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun BingoRewardPopup(
    matchState: BingoMatchState,
    difficulty: String,
    onClaimed: (coinsDelta: Int, wasAdClaimed: Boolean) -> Unit
) {
    val context = LocalContext.current
    val isWin = matchState == BingoMatchState.VICTORY
    val baseCoins = when {
        isWin && difficulty.uppercase() == "EASY" -> 5
        isWin && difficulty.uppercase() == "MEDIUM" -> 7
        isWin && difficulty.uppercase() == "HARD" -> 10
        else -> 1
    }

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
                        text = when {
                            isWin -> "🎉 VICTORY! Difficulty: $difficulty"
                            else -> "💔 Match Finished!"
                        },
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
                            AaaBingoAudioHaptics.playClickSound()
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

@Composable
private fun BingoSecondChanceDialog(
    onWatchAd: () -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
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
                border = BorderStroke(2.dp, Color(0xFFFF9100)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C0A12))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⚡ SECOND CHANCE OPPORTUNITY ⚡",
                        color = Color(0xFFFF9100),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "You are only 1 line away from BINGO!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Watch a video to revert AI's win, unmark some of their numbers, and get extra turns to win the match!",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Watch Ad to Continue
                    AaaGlossyButton(
                        onClick = onWatchAd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        containerColor = Color(0xFF00E676),
                        contentColor = Color(0xFF091E10),
                        borderColor = Color(0xFFB9F6CA)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WATCH AD TO CONTINUE", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }

                    // Close (Decline and accept defeat)
                    AaaGlossyButton(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        containerColor = Color(0xFF334155),
                        contentColor = Color.White,
                        borderColor = Color(0xFF64748B)
                    ) {
                        Text("CLOSE & ACCEPT DEFEAT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BingoLinesOverlay(
    completedLines: Set<BingoLineType>,
    gapHorizontal: androidx.compose.ui.unit.Dp,
    gapVertical: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    strokeWidthMultiplier: Float = 1.0f
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val gapHorizontalPx = with(density) { gapHorizontal.toPx() }
    val gapVerticalPx = with(density) { gapVertical.toPx() }

    val progressMap = BingoLineType.values().associateWith { line ->
        animateFloatAsState(
            targetValue = if (completedLines.contains(line)) 1f else 0f,
            animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
            label = "Line_${line.name}"
        )
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        fun getCellCenter(r: Int, c: Int): Offset {
            val cellW = (width - 4 * gapHorizontalPx) / 5f
            val cellH = (height - 4 * gapVerticalPx) / 5f
            val x = cellW / 2f + c * (cellW + gapHorizontalPx)
            val y = cellH / 2f + r * (cellH + gapVerticalPx)
            return Offset(x, y)
        }

        BingoLineType.values().forEach { line ->
            val progress = progressMap[line]?.value ?: 0f
            if (progress > 0f) {
                val endpoints = when (line) {
                    BingoLineType.ROW_0 -> Pair(Pair(0, 0), Pair(0, 4))
                    BingoLineType.ROW_1 -> Pair(Pair(1, 0), Pair(1, 4))
                    BingoLineType.ROW_2 -> Pair(Pair(2, 0), Pair(2, 4))
                    BingoLineType.ROW_3 -> Pair(Pair(3, 0), Pair(3, 4))
                    BingoLineType.ROW_4 -> Pair(Pair(4, 0), Pair(4, 4))
                    BingoLineType.COL_0 -> Pair(Pair(0, 0), Pair(4, 0))
                    BingoLineType.COL_1 -> Pair(Pair(0, 1), Pair(4, 1))
                    BingoLineType.COL_2 -> Pair(Pair(0, 2), Pair(4, 2))
                    BingoLineType.COL_3 -> Pair(Pair(0, 3), Pair(4, 3))
                    BingoLineType.COL_4 -> Pair(Pair(0, 4), Pair(4, 4))
                    BingoLineType.DIAG_MAIN -> Pair(Pair(0, 0), Pair(4, 4))
                    BingoLineType.DIAG_ANTI -> Pair(Pair(0, 4), Pair(4, 0))
                }

                val startOffset = getCellCenter(endpoints.first.first, endpoints.first.second)
                val endOffset = getCellCenter(endpoints.second.first, endpoints.second.second)

                val currentEndOffset = Offset(
                    x = startOffset.x + (endOffset.x - startOffset.x) * progress,
                    y = startOffset.y + (endOffset.y - startOffset.y) * progress
                )

                val glowStroke = 14f * strokeWidthMultiplier
                val coreStroke = 6f * strokeWidthMultiplier

                drawLine(
                    color = Color(0xFFFFD700).copy(alpha = 0.35f * progress),
                    start = startOffset,
                    end = currentEndOffset,
                    strokeWidth = glowStroke,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFFFFF59D).copy(alpha = 0.9f * progress),
                    start = startOffset,
                    end = currentEndOffset,
                    strokeWidth = coreStroke,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}

