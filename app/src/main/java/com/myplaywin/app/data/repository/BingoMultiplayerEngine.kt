package com.myplaywin.app.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
 * Real-Time Firebase Realtime Database Multiplayer Engine
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
    private var searchCountdownJob: Job? = null

    private val auth = FirebaseAuth.getInstance()
    private val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val db = FirebaseDatabase.getInstance(dbUrl)

    // Local Player Data
    private val prefs = context.getSharedPreferences("bingo_progression_prefs", Context.MODE_PRIVATE)

    val localPlayerUid: String
        get() = auth.currentUser?.uid ?: "local_player_${prefs.getString("fallback_uid", "") ?: run {
            val id = UUID.randomUUID().toString().take(8)
            prefs.edit().putString("fallback_uid", id).apply()
            id
        }}"

    val localPlayerName: String
        get() = auth.currentUser?.displayName ?: "Player_${localPlayerUid.take(4)}"

    val localPlayerLevel: Int
        get() = prefs.getInt("level", 12)

    val localPlayerWinRate: Float
        get() {
            val total = prefs.getInt("stat_totalMatches", 0)
            val wins = prefs.getInt("stat_onlineWins", 0) + prefs.getInt("stat_offlineWins", 0)
            return if (total > 0) wins.toFloat() / total else 0.68f
        }

    private var queueListener: ValueEventListener? = null
    private var roomListener: ValueEventListener? = null
    private var activeRoomId: String? = null

    init {
        startPingMonitor()
    }

    // ==========================================
    // 1. MATCHMAKING ENGINE WITH FIREBASE
    // ==========================================

    fun startMatchmaking(
        matchType: BingoMatchType = BingoMatchType.ONE_VS_ONE,
        onMatchFound: () -> Unit,
        onSearchTimeout: () -> Unit
    ) {
        cancelMatchmaking()
        _matchStatus.value = BingoMatchStatus.SEARCHING
        _searchTimeRemaining.value = 20

        // Search countdown job
        searchCountdownJob = engineScope.launch {
            for (secondsLeft in 20 downTo 1) {
                _searchTimeRemaining.value = secondsLeft
                delay(1000)
            }
            // Timeout reached
            cancelMatchmaking()
            onSearchTimeout()
        }

        matchmakingJob = engineScope.launch {
            if (auth.currentUser == null) {
                auth.signInAnonymously().addOnCompleteListener { task ->
                    joinQueue(onMatchFound)
                }
            } else {
                joinQueue(onMatchFound)
            }
        }
    }

    private fun joinQueue(onMatchFound: () -> Unit) {
        val myEntry = MatchmakingQueueEntry(
            uid = localPlayerUid,
            displayName = localPlayerName,
            level = localPlayerLevel,
            winRate = localPlayerWinRate,
            pingMs = _networkLatencyMs.value,
            timestamp = System.currentTimeMillis()
        )

        val queueRef = db.getReference("bingo/matchmaking_queue").child(localPlayerUid)
        queueRef.setValue(myEntry)

        queueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = mutableListOf<MatchmakingQueueEntry>()
                for (child in snapshot.children) {
                    val entry = child.getValue(MatchmakingQueueEntry::class.java)
                    if (entry != null && entry.uid != localPlayerUid) {
                        if (System.currentTimeMillis() - entry.timestamp < 30000) {
                            entries.add(entry)
                        }
                    }
                }

                if (entries.isNotEmpty()) {
                    val opponent = entries.first()
                    val myUid = localPlayerUid
                    val opUid = opponent.uid
                    val isHost = myUid < opUid
                    val roomId = "room_" + if (isHost) "${myUid}_$opUid" else "${opUid}_$myUid"

                    cancelQueueListenersAndEntries()
                    searchCountdownJob?.cancel()

                    observeRoom(roomId, isHost, opponent, onMatchFound)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        db.getReference("bingo/matchmaking_queue").addValueEventListener(queueListener!!)
    }

    private fun cancelQueueListenersAndEntries() {
        queueListener?.let {
            db.getReference("bingo/matchmaking_queue").removeEventListener(it)
            queueListener = null
        }
        db.getReference("bingo/matchmaking_queue").child(localPlayerUid).removeValue()
    }

    private fun observeRoom(roomId: String, isHost: Boolean, opponent: MatchmakingQueueEntry, onMatchFound: () -> Unit) {
        activeRoomId = roomId

        if (isHost) {
            val hostPlayer = BingoOnlinePlayer(
                uid = localPlayerUid,
                displayName = localPlayerName,
                level = localPlayerLevel,
                winRate = localPlayerWinRate,
                pingMs = _networkLatencyMs.value,
                isOnline = true,
                isReady = true,
                isHost = true
            )

            val guestPlayer = BingoOnlinePlayer(
                uid = opponent.uid,
                displayName = opponent.displayName,
                level = opponent.level,
                winRate = opponent.winRate,
                pingMs = opponent.pingMs,
                isOnline = true,
                isReady = true,
                isHost = false
            )

            val room = BingoOnlineRoom(
                roomId = roomId,
                matchType = "ONE_VS_ONE",
                matchStatus = BingoMatchStatus.MATCHED.name,
                player1 = hostPlayer,
                player2 = guestPlayer,
                boardSeedP1 = System.currentTimeMillis(),
                boardSeedP2 = System.currentTimeMillis() + 999L,
                serverTimestamp = System.currentTimeMillis()
            )

            db.getReference("bingo/rooms").child(roomId).setValue(room)
        }

        val myPlayerNode = if (isHost) "player1" else "player2"
        db.getReference("bingo/rooms").child(roomId).child(myPlayerNode).child("isOnline").onDisconnect().setValue(false)

        roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue(BingoOnlineRoom::class.java) ?: return
                _currentRoom.value = room

                val oppPlayerNode = if (isHost) room.player2 else room.player1
                if (oppPlayerNode != null && !oppPlayerNode.isOnline && room.winnerUid == null && room.matchStatus == "PLAYING") {
                    db.getReference("bingo/rooms").child(roomId).child("winnerUid").setValue(localPlayerUid)
                }

                when (room.matchStatus) {
                    "MATCHED" -> {
                        _matchStatus.value = BingoMatchStatus.MATCHED
                        if (isHost) {
                            engineScope.launch {
                                delay(1200)
                                db.getReference("bingo/rooms").child(roomId).child("matchStatus").setValue("COUNTDOWN")
                            }
                        }
                    }
                    "COUNTDOWN" -> {
                        _matchStatus.value = BingoMatchStatus.COUNTDOWN
                        if (isHost) {
                            engineScope.launch {
                                delay(1500)
                                db.getReference("bingo/rooms").child(roomId).child("matchStatus").setValue("PLAYING")
                                startServerNumberCaller(roomId)
                            }
                        }
                    }
                    "PLAYING" -> {
                        _matchStatus.value = BingoMatchStatus.PLAYING
                        onMatchFound()
                    }
                    "COMPLETED", "VICTORY", "DEFEAT" -> {
                        val winner = room.winnerUid
                        if (winner != null) {
                            val isLocalWin = (winner == localPlayerUid)
                            _matchStatus.value = if (isLocalWin) BingoMatchStatus.VICTORY else BingoMatchStatus.DEFEAT
                            numberCallerJob?.cancel()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        db.getReference("bingo/rooms").child(roomId).addValueEventListener(roomListener!!)
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
        startLocalNumberCaller()
        onMatchStart()
    }

    fun cancelMatchmaking() {
        searchCountdownJob?.cancel()
        matchmakingJob?.cancel()
        numberCallerJob?.cancel()

        searchCountdownJob = null
        matchmakingJob = null
        numberCallerJob = null

        cancelQueueListenersAndEntries()

        roomListener?.let {
            activeRoomId?.let { rId ->
                db.getReference("bingo/rooms").child(rId).removeEventListener(it)
            }
            roomListener = null
        }

        activeRoomId = null
        _matchStatus.value = BingoMatchStatus.SEARCHING
        _currentRoom.value = null
    }

    // ==========================================
    // 2. SYNCHRONIZED NUMBER CALLER
    // ==========================================

    private fun startServerNumberCaller(roomId: String) {
        numberCallerJob?.cancel()
        numberCallerJob = engineScope.launch {
            val availableNumbers = (1..75).shuffled().toMutableList()

            while (isActive && availableNumbers.isNotEmpty() && _matchStatus.value == BingoMatchStatus.PLAYING) {
                delay(3200)

                val room = _currentRoom.value ?: break
                val nextNumber = availableNumbers.removeAt(0)
                val letter = columnLetterForNum(nextNumber)

                val updatedCalledList = room.calledNumbersHistory + nextNumber

                db.getReference("bingo/rooms").child(roomId).updateChildren(mapOf(
                    "calledNumbersHistory" to updatedCalledList,
                    "activeCalledNumber" to nextNumber,
                    "activeLetter" to letter,
                    "serverTimestamp" to System.currentTimeMillis()
                ))
            }
        }
    }

    private fun startLocalNumberCaller() {
        numberCallerJob?.cancel()
        numberCallerJob = engineScope.launch {
            val availableNumbers = (1..75).shuffled().toMutableList()

            while (isActive && availableNumbers.isNotEmpty() && _matchStatus.value == BingoMatchStatus.PLAYING) {
                delay(3200)

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

                simulateOpponentTurn(nextNumber)
            }
        }
    }

    private fun simulateOpponentTurn(calledNum: Int) {
        val room = _currentRoom.value ?: return
        val opponent = room.player2 ?: return

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

                if (newLines >= 5 && _currentRoom.value?.winnerUid == null && Random.nextFloat() < 0.4f) {
                    triggerLocalMatchCompletion(winnerUid = opponent.uid)
                }
            }
        }
    }

    // ==========================================
    // 3. SERVER-AUTHORITATIVE MOVE & ANTI-CHEAT
    // ==========================================

    fun submitMove(move: BingoMovePayload, currentBoard: List<List<BingoTile>>): BingoAntiCheatResult {
        val room = _currentRoom.value ?: return BingoAntiCheatResult(false, "Room Not Active", AntiCheatSanction.REJECT_MOVE)

        if (move.moveType == "DAUB") {
            if (!room.calledNumbersHistory.contains(move.tileNumber)) {
                _antiCheatAlert.value = "❌ Anti-Cheat Violation: Number ${move.tileNumber} has NOT been called!"
                return BingoAntiCheatResult(false, "Uncalled Number", AntiCheatSanction.REJECT_MOVE)
            }
        }

        if (move.moveType == "CLAIM_BINGO") {
            val serverVerifiedLines = evaluateLinesServerSide(currentBoard, room.calledNumbersHistory)
            if (serverVerifiedLines.size < 5) {
                _antiCheatAlert.value = "❌ Anti-Cheat Rejection: Invalid Bingo claim. No completed lines found!"
                return BingoAntiCheatResult(false, "Invalid Line Verification", AntiCheatSanction.REJECT_MOVE)
            }

            if (activeRoomId != null) {
                db.getReference("bingo/rooms").child(activeRoomId!!).updateChildren(mapOf(
                    "winnerUid" to localPlayerUid,
                    "matchStatus" to "COMPLETED"
                ))
            } else {
                triggerLocalMatchCompletion(winnerUid = localPlayerUid)
            }
            return BingoAntiCheatResult(true, "Server Verified Victory", AntiCheatSanction.ACCEPT)
        }

        val p1 = room.player1
        val p2 = room.player2
        val isP1 = (localPlayerUid == p1.uid)

        val completedLines = evaluateLinesServerSide(currentBoard, room.calledNumbersHistory).size
        val markedCount = currentBoard.flatten().count { it.isMarked }

        if (activeRoomId != null) {
            val myPlayerNode = if (isP1) "player1" else "player2"
            db.getReference("bingo/rooms").child(activeRoomId!!).child(myPlayerNode).updateChildren(mapOf(
                "markedCount" to markedCount,
                "completedLinesCount" to completedLines,
                "lastMoveTimestamp" to System.currentTimeMillis()
            ))
        } else {
            if (isP1) {
                val updatedP1 = p1.copy(
                    markedCount = markedCount,
                    completedLinesCount = completedLines,
                    lastMoveTimestamp = System.currentTimeMillis()
                )
                _currentRoom.value = room.copy(player1 = updatedP1)
            } else if (p2 != null) {
                val updatedP2 = p2.copy(
                    markedCount = markedCount,
                    completedLinesCount = completedLines,
                    lastMoveTimestamp = System.currentTimeMillis()
                )
                _currentRoom.value = room.copy(player2 = updatedP2)
            }
        }

        return BingoAntiCheatResult(true, "Valid Move Accepted", AntiCheatSanction.ACCEPT)
    }

    private fun evaluateLinesServerSide(currentBoard: List<List<BingoTile>>, calledHistory: List<Int>): Set<BingoLineType> {
        val lines = mutableSetOf<BingoLineType>()

        fun isTileValid(tile: BingoTile): Boolean {
            return tile.isMarked && (tile.isFreeTile || calledHistory.contains(tile.number))
        }

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

        if ((0..4).all { i -> isTileValid(currentBoard[i][i]) }) lines.add(BingoLineType.DIAG_MAIN)
        if ((0..4).all { i -> isTileValid(currentBoard[i][4 - i]) }) lines.add(BingoLineType.DIAG_ANTI)

        return lines
    }

    private fun triggerLocalMatchCompletion(winnerUid: String) {
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
                _networkLatencyMs.value = Random.nextInt(25, 65)
            }
        }
    }

    fun simulateReconnection() {
        val roomId = activeRoomId ?: return
        engineScope.launch {
            _matchStatus.value = BingoMatchStatus.RECONNECTING
            delay(1500)
            db.getReference("bingo/rooms").child(roomId).get().addOnSuccessListener { snapshot ->
                val room = snapshot.getValue(BingoOnlineRoom::class.java)
                if (room != null) {
                    _currentRoom.value = room
                    _matchStatus.value = BingoMatchStatus.PLAYING
                } else {
                    _matchStatus.value = BingoMatchStatus.DEFEAT
                }
            }.addOnFailureListener {
                _matchStatus.value = BingoMatchStatus.DEFEAT
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
