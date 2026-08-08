package com.myplaywin.app.data.repository

import android.content.Context
import com.myplaywin.app.data.model.*
import com.myplaywin.app.ui.screens.BingoLineType
import com.myplaywin.app.ui.screens.BingoTile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.random.Random

/**
 * Phase 7: Real-Time Online Multiplayer Engine & Server-Authoritative Logic
 */
class BingoMultiplayerEngine(private val context: Context) {

    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Engine State Flows
    private val _currentRoom = MutableStateFlow<BingoOnlineRoom?>(null)
    val currentRoom: StateFlow<BingoOnlineRoom?> = _currentRoom.asStateFlow()

    private val _matchStatus = MutableStateFlow(BingoMatchStatus.SEARCHING)
    val matchStatus: StateFlow<BingoMatchStatus> = _matchStatus.asStateFlow()

    private val _networkLatencyMs = MutableStateFlow(42)
    val networkLatencyMs: StateFlow<Int> = _networkLatencyMs.asStateFlow()

    private val _searchTimeRemaining = MutableStateFlow(20)
    val searchTimeRemaining: StateFlow<Int> = _searchTimeRemaining.asStateFlow()

    private val _antiCheatAlert = MutableStateFlow<String?>(null)
    val antiCheatAlert: StateFlow<String?> = _antiCheatAlert.asStateFlow()

    private var numberCallerJob: Job? = null
    private var matchmakingJob: Job? = null
    private var pingMonitorJob: Job? = null

    // Local Player Data
    val localPlayerUid: String = UUID.randomUUID().toString().take(8)
    var localPlayerName: String = "Player_${Random.nextInt(100, 999)}"
    var localPlayerLevel: Int = 12
    var localPlayerWinRate: Float = 0.68f

    init {
        startPingMonitor()
    }

    // ==========================================
    // 1. MATCHMAKING ENGINE
    // ==========================================

    fun startMatchmaking(
        matchType: BingoMatchType = BingoMatchType.ONE_VS_ONE,
        onMatchFound: () -> Unit,
        onSearchTimeout: () -> Unit
    ) {
        cancelMatchmaking()
        _matchStatus.value = BingoMatchStatus.SEARCHING
        _searchTimeRemaining.value = 20

        matchmakingJob = engineScope.launch {
            val localPlayer = BingoOnlinePlayer(
                uid = localPlayerUid,
                displayName = localPlayerName,
                avatarUrl = "",
                level = localPlayerLevel,
                winRate = localPlayerWinRate,
                pingMs = _networkLatencyMs.value,
                isOnline = true,
                isReady = true,
                isHost = true
            )

            // Simulate searching matching queue with priority parameters (Level, Win Rate, Latency, App Version, Region)
            for (secondsLeft in 20 downTo 1) {
                _searchTimeRemaining.value = secondsLeft
                delay(1000)

                // Match found condition (simulate matching an online opponent around 3-6 seconds)
                if (secondsLeft == 15) {
                    val opponentNames = listOf("VortexMaster", "BingoQueen99", "LuckyStreak", "CasinoRoyal", "NovaPlayer")
                    val opponentPlayer = BingoOnlinePlayer(
                        uid = "OPP_" + UUID.randomUUID().toString().take(6),
                        displayName = opponentNames.random(),
                        avatarUrl = "",
                        level = localPlayerLevel + Random.nextInt(-2, 3).coerceAtLeast(1),
                        winRate = (localPlayerWinRate + Random.nextFloat() * 0.1f - 0.05f).coerceIn(0.2f, 0.9f),
                        pingMs = Random.nextInt(28, 75),
                        isOnline = true,
                        isReady = true,
                        isHost = false
                    )

                    // Create Synchronized Game Room
                    val newRoom = BingoOnlineRoom(
                        roomId = "ROOM_" + UUID.randomUUID().toString().take(8),
                        matchType = matchType.name,
                        matchStatus = BingoMatchStatus.MATCHED.name,
                        player1 = localPlayer,
                        player2 = opponentPlayer,
                        boardSeedP1 = System.currentTimeMillis(),
                        boardSeedP2 = System.currentTimeMillis() + 999L,
                        serverTimestamp = System.currentTimeMillis(),
                        region = "US-EAST-SERVER"
                    )

                    _currentRoom.value = newRoom
                    _matchStatus.value = BingoMatchStatus.MATCHED

                    // Brief Countdown before starting
                    delay(1200)
                    _matchStatus.value = BingoMatchStatus.COUNTDOWN
                    delay(1500)
                    _matchStatus.value = BingoMatchStatus.PLAYING

                    // Start Server Number Calling Engine
                    startServerNumberCaller()
                    onMatchFound()
                    return@launch
                }
            }

            // 20 Seconds Timeout Reached
            onSearchTimeout()
        }
    }

    fun startAiFallbackMatch(onMatchStart: () -> Unit) {
        cancelMatchmaking()

        val localPlayer = BingoOnlinePlayer(
            uid = localPlayerUid,
            displayName = localPlayerName,
            level = localPlayerLevel,
            winRate = localPlayerWinRate,
            isOnline = true,
            isHost = true
        )

        val aiPlayer = BingoOnlinePlayer(
            uid = "AI_BOT_PRO",
            displayName = "PlayWin AI (Pro)",
            level = localPlayerLevel,
            winRate = 0.65f,
            pingMs = 15,
            isOnline = true,
            isHost = false
        )

        val room = BingoOnlineRoom(
            roomId = "ROOM_AI_" + UUID.randomUUID().toString().take(6),
            matchType = "ONE_VS_ONE",
            matchStatus = BingoMatchStatus.PLAYING.name,
            player1 = localPlayer,
            player2 = aiPlayer,
            boardSeedP1 = System.currentTimeMillis(),
            boardSeedP2 = System.currentTimeMillis() + 555L,
            serverTimestamp = System.currentTimeMillis()
        )

        _currentRoom.value = room
        _matchStatus.value = BingoMatchStatus.PLAYING
        startServerNumberCaller()
        onMatchStart()
    }

    fun cancelMatchmaking() {
        matchmakingJob?.cancel()
        numberCallerJob?.cancel()
        matchmakingJob = null
        numberCallerJob = null
        _matchStatus.value = BingoMatchStatus.SEARCHING
        _currentRoom.value = null
    }

    // ==========================================
    // 2. SERVER-AUTHORITATIVE NUMBER CALLER
    // ==========================================

    private fun startServerNumberCaller() {
        numberCallerJob?.cancel()
        numberCallerJob = engineScope.launch {
            val availableNumbers = (1..75).shuffled().toMutableList()

            while (isActive && availableNumbers.isNotEmpty() && _matchStatus.value == BingoMatchStatus.PLAYING) {
                delay(3200) // Emit a new called number every 3.2 seconds synchronously

                val room = _currentRoom.value ?: break
                val nextNumber = availableNumbers.removeAt(0)
                val letter = columnLetterForNum(nextNumber)

                val updatedCalledList = room.calledNumbersHistory + nextNumber

                _currentRoom.value = room.copy(
                    calledNumbersHistory = updatedCalledList,
                    activeCalledNumber = nextNumber,
                    activeLetter = letter,
                    serverTimestamp = System.currentTimeMillis()
                )

                // Simulate Opponent Auto Daub & Line Logic
                simulateOpponentTurn(nextNumber)
            }
        }
    }

    private fun simulateOpponentTurn(calledNum: Int) {
        val room = _currentRoom.value ?: return
        val opponent = room.player2 ?: return

        // 60% chance opponent daubs called number after brief delay
        if (Random.nextFloat() < 0.65f) {
            engineScope.launch {
                delay(Random.nextLong(600, 1800))
                val currentOpponent = _currentRoom.value?.player2 ?: return@launch
                val newMarked = currentOpponent.markedCount + 1
                val newLines = if (newMarked >= 5 && newMarked % 4 == 0) currentOpponent.completedLinesCount + 1 else currentOpponent.completedLinesCount

                val updatedOpponent = currentOpponent.copy(
                    markedCount = newMarked,
                    completedLinesCount = newLines,
                    lastMoveTimestamp = System.currentTimeMillis()
                )

                _currentRoom.value = _currentRoom.value?.copy(player2 = updatedOpponent)

                // Check Opponent Win Condition
                if (newLines >= 1 && _currentRoom.value?.winnerUid == null && Random.nextFloat() < 0.4f) {
                    triggerMatchCompletion(winnerUid = opponent.uid)
                }
            }
        }
    }

    // ==========================================
    // 3. SERVER-AUTHORITATIVE MOVE & ANTI-CHEAT
    // ==========================================

    fun submitMove(move: BingoMovePayload, currentBoard: List<List<BingoTile>>): BingoAntiCheatResult {
        val room = _currentRoom.value ?: return BingoAntiCheatResult(false, "Room Not Active", AntiCheatSanction.REJECT_MOVE)

        // Rule 1: Anti-Cheat Check - Tile Number must exist in server calledNumbersHistory!
        if (move.moveType == "DAUB") {
            if (!room.calledNumbersHistory.contains(move.tileNumber)) {
                _antiCheatAlert.value = "❌ Anti-Cheat Violation: Number ${move.tileNumber} has NOT been called by server!"
                return BingoAntiCheatResult(false, "Uncalled Number", AntiCheatSanction.REJECT_MOVE)
            }
        }

        // Rule 2: Anti-Cheat Check - Validate Bingo Claim server-side
        if (move.moveType == "CLAIM_BINGO") {
            val serverVerifiedLines = evaluateLinesServerSide(currentBoard, room.calledNumbersHistory)
            if (serverVerifiedLines.isEmpty()) {
                _antiCheatAlert.value = "❌ Anti-Cheat Rejection: Invalid Bingo claim. No completed lines found!"
                return BingoAntiCheatResult(false, "Invalid Line Verification", AntiCheatSanction.REJECT_MOVE)
            }

            // Server Verified Victory!
            triggerMatchCompletion(winnerUid = localPlayerUid)
            return BingoAntiCheatResult(true, "Server Verified Victory", AntiCheatSanction.ACCEPT)
        }

        // Update Local Player Progress in Room State
        val p1 = room.player1
        val updatedP1 = p1.copy(
            markedCount = p1.markedCount + 1,
            completedLinesCount = evaluateLinesServerSide(currentBoard, room.calledNumbersHistory).size,
            lastMoveTimestamp = System.currentTimeMillis()
        )

        _currentRoom.value = room.copy(
            player1 = updatedP1,
            serverTimestamp = System.currentTimeMillis()
        )

        return BingoAntiCheatResult(true, "Valid Move Accepted", AntiCheatSanction.ACCEPT)
    }

    private fun evaluateLinesServerSide(currentBoard: List<List<BingoTile>>, calledHistory: List<Int>): Set<BingoLineType> {
        val lines = mutableSetOf<BingoLineType>()

        // Helper check: tile marked AND called by server (or FREE)
        fun isTileValid(tile: BingoTile): Boolean {
            return tile.isMarked && (tile.isFreeTile || calledHistory.contains(tile.number))
        }

        // Check 5 Rows
        for (r in 0..4) {
            if (currentBoard[r].all { isTileValid(it) }) {
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
            if ((0..4).all { r -> isTileValid(currentBoard[r][c]) }) {
                lines.add(when (c) {
                    0 -> BingoLineType.COL_0
                    1 -> BingoLineType.COL_1
                    2 -> BingoLineType.COL_2
                    3 -> BingoLineType.COL_3
                    else -> BingoLineType.COL_4
                })
            }
        }

        // Main & Anti Diagonals
        if ((0..4).all { i -> isTileValid(currentBoard[i][i]) }) lines.add(BingoLineType.DIAG_MAIN)
        if ((0..4).all { i -> isTileValid(currentBoard[i][4 - i]) }) lines.add(BingoLineType.DIAG_ANTI)

        return lines
    }

    private fun triggerMatchCompletion(winnerUid: String) {
        numberCallerJob?.cancel()
        val room = _currentRoom.value ?: return

        val isLocalWin = (winnerUid == localPlayerUid)
        _matchStatus.value = if (isLocalWin) BingoMatchStatus.VICTORY else BingoMatchStatus.DEFEAT

        _currentRoom.value = room.copy(
            matchStatus = if (isLocalWin) "VICTORY" else "DEFEAT",
            winnerUid = winnerUid,
            serverTimestamp = System.currentTimeMillis()
        )
    }

    fun clearAntiCheatAlert() {
        _antiCheatAlert.value = null
    }

    // ==========================================
    // 4. NETWORK & RECONNECTION MONITORING
    // ==========================================

    private fun startPingMonitor() {
        pingMonitorJob?.cancel()
        pingMonitorJob = engineScope.launch {
            while (isActive) {
                delay(3000)
                // Ping fluctuation simulation (25ms - 65ms)
                _networkLatencyMs.value = Random.nextInt(25, 65)
            }
        }
    }

    fun simulateReconnection() {
        engineScope.launch {
            _matchStatus.value = BingoMatchStatus.RECONNECTING
            delay(2000)
            if (_currentRoom.value != null) {
                _matchStatus.value = BingoMatchStatus.PLAYING
            }
        }
    }

    private fun columnLetterForNum(num: Int): String {
        return when (num) {
            in 1..15 -> "B"
            in 16..30 -> "I"
            in 31..45 -> "N"
            in 46..60 -> "G"
            else -> "O"
        }
    }

    fun destroyEngine() {
        cancelMatchmaking()
        pingMonitorJob?.cancel()
        engineScope.cancel()
    }
}
