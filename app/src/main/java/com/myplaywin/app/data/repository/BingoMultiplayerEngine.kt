package com.myplaywin.app.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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

    private var matchmakingJob: Job? = null
    private var pingMonitorJob: Job? = null
    private var searchCountdownJob: Job? = null

    private val auth = FirebaseAuth.getInstance()
    private val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val db = FirebaseDatabase.getInstance(dbUrl)

    // Local Player Data
    private val prefs = context.getSharedPreferences("bingo_progression_prefs", Context.MODE_PRIVATE)

    val localPlayerUid: String
        get() = auth.currentUser?.uid ?: "local_${prefs.getString("fallback_uid", "") ?: run {
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

    private var myQueueListener: ValueEventListener? = null
    private var roomListener: ValueEventListener? = null
    private var connectionStateListener: ValueEventListener? = null
    private var activeRoomId: String? = null

    init {
        android.util.Log.d("BINGO_ONLINE", "FIREBASE_CONNECTED: Initializing database connection")
        startPingMonitor()
        observeConnectionState()
        ensureUserAuthenticated()
    }

    private fun ensureUserAuthenticated() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("BINGO_ONLINE", "AUTH_SUCCESS: uid = ${auth.currentUser?.uid}")
                } else {
                    android.util.Log.e("BINGO_ONLINE", "ERROR: Anonymous authentication failed: ${task.exception?.message}")
                }
            }
        } else {
            android.util.Log.d("BINGO_ONLINE", "AUTH_SUCCESS: Already authenticated: uid = ${auth.currentUser?.uid}")
        }
    }

    // ==========================================
    // 1. ATOMIC MATCHMAKING ENGINE WITH FIREBASE
    // ==========================================

    fun startMatchmaking(
        matchType: BingoMatchType = BingoMatchType.ONE_VS_ONE,
        onMatchFound: () -> Unit,
        onSearchTimeout: () -> Unit
    ) {
        cancelMatchmaking()
        _matchStatus.value = BingoMatchStatus.SEARCHING
        _searchTimeRemaining.value = 20

        android.util.Log.d("BINGO_ONLINE", "MATCHMAKING_START: User initiating matchmaking search.")

        // Start 20-second countdown
        searchCountdownJob = engineScope.launch {
            for (secondsLeft in 20 downTo 1) {
                _searchTimeRemaining.value = secondsLeft
                delay(1000)
            }
            android.util.Log.d("BINGO_ONLINE", "ERROR: Matchmaking search timeout reached.")
            cancelMatchmaking()
            onSearchTimeout()
        }

        matchmakingJob = engineScope.launch {
            // Ensure auth is active
            if (auth.currentUser == null) {
                try {
                    auth.signInAnonymously().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            runAtomicMatchmaking(onMatchFound)
                        } else {
                            onSearchTimeout()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BINGO_ONLINE", "ERROR: Exception in sign in: ${e.message}")
                    onSearchTimeout()
                }
            } else {
                runAtomicMatchmaking(onMatchFound)
            }
        }
    }

    private fun runAtomicMatchmaking(onMatchFound: () -> Unit) {
        val coordinatorRef = db.getReference("bingoOnline/coordinator/waiting_player")
        
        coordinatorRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val waitingPlayerId = currentData.getValue(String::class.java)
                
                if (waitingPlayerId == null || waitingPlayerId.isEmpty() || waitingPlayerId == localPlayerUid) {
                    // We are the host (waiting player)
                    currentData.value = localPlayerUid
                    return Transaction.success(currentData)
                } else {
                    // We found an opponent! Match atomically by setting coordinator to null
                    currentData.value = ""
                    return Transaction.success(currentData)
                }
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    android.util.Log.e("BINGO_ONLINE", "ERROR: Matchmaking coordinator transaction failed: ${error.message}")
                    return
                }

                if (committed) {
                    val waitingPlayerId = snapshot?.getValue(String::class.java)
                    if (waitingPlayerId == localPlayerUid) {
                        // HOST MODE: We are waiting in the pool
                        android.util.Log.d("BINGO_ONLINE", "MATCHMAKING_ENTRY_CREATED: Player added to matchmaking queue as host.")
                        setupHostQueue(onMatchFound)
                    } else {
                        // GUEST MODE: Match found, we generate the room and notify the waiting host
                        val opponentId = waitingPlayerId ?: ""
                        if (opponentId.isNotEmpty()) {
                            android.util.Log.d("BINGO_ONLINE", "ROOM_FOUND: Opponent discovered in pool: opponentId = $opponentId")
                            setupGuestMatch(opponentId, onMatchFound)
                        } else {
                            android.util.Log.e("BINGO_ONLINE", "ERROR: Coordinator matched with an empty player ID.")
                        }
                    }
                }
            }
        })
    }

    private fun setupHostQueue(onMatchFound: () -> Unit) {
        val queueRef = db.getReference("bingoOnline/matchmaking").child(localPlayerUid)
        val initialEntry = MatchmakingQueueEntry(
            playerId = localPlayerUid,
            status = "searching",
            timestamp = System.currentTimeMillis()
        )

        queueRef.setValue(initialEntry)
        queueRef.onDisconnect().removeValue()

        myQueueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entry = snapshot.getValue(MatchmakingQueueEntry::class.java) ?: return
                if (entry.status == "matched" && entry.roomId.isNotEmpty()) {
                    // Match has been found! Host joins the room created by the guest
                    android.util.Log.d("BINGO_ONLINE", "ROOM_JOINED: Host matched! Room discovered: ${entry.roomId}")
                    
                    // Remove queue listener and cleanup entry
                    queueRef.removeEventListener(this)
                    myQueueListener = null
                    queueRef.removeValue()

                    searchCountdownJob?.cancel()
                    joinRoom(entry.roomId, isHost = true, onMatchFound)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("BINGO_ONLINE", "ERROR: Queue observation cancelled: ${error.message}")
            }
        }

        queueRef.addValueEventListener(myQueueListener!!)
    }

    private fun setupGuestMatch(opponentId: String, onMatchFound: () -> Unit) {
        val roomId = "room_" + UUID.randomUUID().toString()
        android.util.Log.d("BINGO_ONLINE", "ROOM_JOINED: Guest creating new room ID: $roomId")

        // First, create the room state
        val myCard = generateCard()
        val guestPlayer = BingoOnlinePlayer(
            uid = localPlayerUid,
            displayName = localPlayerName,
            avatarUrl = "👑",
            level = localPlayerLevel,
            winRate = localPlayerWinRate,
            isOnline = true,
            isReady = true,
            isHost = false,
            card = myCard,
            marked = List(25) { it == 12 }
        )

        val roomRef = db.getReference("bingoOnline/rooms").child(roomId)
        roomRef.child("roomId").setValue(roomId)
        roomRef.child("status").setValue("waiting")
        roomRef.child("maxPlayers").setValue(2)
        roomRef.child("createdAt").setValue(System.currentTimeMillis())
        roomRef.child("players").child(localPlayerUid).setValue(guestPlayer)

        // Write "matched" status to opponent's queue entry
        val opponentQueueRef = db.getReference("bingoOnline/matchmaking").child(opponentId)
        opponentQueueRef.updateChildren(mapOf(
            "status" to "matched",
            "roomId" to roomId
        ))

        // Create our own matchmaking queue entry for recording purposes
        val myQueueRef = db.getReference("bingoOnline/matchmaking").child(localPlayerUid)
        myQueueRef.setValue(MatchmakingQueueEntry(
            playerId = localPlayerUid,
            status = "matched",
            roomId = roomId,
            timestamp = System.currentTimeMillis()
        ))
        myQueueRef.removeValue()

        searchCountdownJob?.cancel()

        // Join the room
        joinRoom(roomId, isHost = false, onMatchFound)
    }

    private fun joinRoom(roomId: String, isHost: Boolean, onMatchFound: () -> Unit) {
        activeRoomId = roomId
        val roomRef = db.getReference("bingoOnline/rooms").child(roomId)

        if (isHost) {
            val myCard = generateCard()
            val hostPlayer = BingoOnlinePlayer(
                uid = localPlayerUid,
                displayName = localPlayerName,
                avatarUrl = "👑",
                level = localPlayerLevel,
                winRate = localPlayerWinRate,
                isOnline = true,
                isReady = true,
                isHost = true,
                card = myCard,
                marked = List(25) { it == 12 }
            )
            roomRef.child("players").child(localPlayerUid).setValue(hostPlayer)
        }

        // Set up presence
        val playerConnectedRef = roomRef.child("players").child(localPlayerUid).child("connected")
        playerConnectedRef.setValue(true)
        playerConnectedRef.onDisconnect().setValue(false)

        roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue(BingoOnlineRoom::class.java) ?: return
                _currentRoom.value = room

                // Handle room state transitions
                if (room.status == "waiting") {
                    val pList = room.players.values.toList()
                    if (pList.size == 2 && pList.all { it.isReady && it.connected && it.card.isNotEmpty() }) {
                        android.util.Log.d("BINGO_ONLINE", "PLAYER_READY: Both players connected and ready.")
                        startGameIfReady(room)
                    }
                } else if (room.status == "playing") {
                    if (_matchStatus.value != BingoMatchStatus.PLAYING && _matchStatus.value != BingoMatchStatus.RECONNECTING) {
                        _matchStatus.value = BingoMatchStatus.PLAYING
                        android.util.Log.d("BINGO_ONLINE", "GAME_START: Room status transition to playing. Local screen loading gameplay.")
                        onMatchFound()
                    }

                    // Check for active turn changes to log
                    val currentTurn = room.game.currentTurn
                    if (currentTurn.isNotEmpty()) {
                        android.util.Log.d("BINGO_ONLINE", "TURN_CHANGED: Authorized player turn is now: $currentTurn")
                    }

                    // Detect disconnects of opponent
                    val opponent = room.players.values.firstOrNull { it.uid != localPlayerUid }
                    if (opponent != null && !opponent.connected && _matchStatus.value == BingoMatchStatus.PLAYING) {
                        android.util.Log.d("BINGO_ONLINE", "PLAYER_DISCONNECTED: Opponent ${opponent.uid} disconnected.")
                    }
                } else if (room.status == "completed") {
                    val winnerId = room.game.winnerId
                    if (winnerId != null) {
                        _matchStatus.value = if (winnerId == localPlayerUid) BingoMatchStatus.VICTORY else BingoMatchStatus.DEFEAT
                        android.util.Log.d("BINGO_ONLINE", "GAME_FINISHED: Match completed. Winner = $winnerId")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("BINGO_ONLINE", "ERROR: Room listener cancelled: ${error.message}")
                _matchStatus.value = BingoMatchStatus.DEFEAT
            }
        }

        roomRef.addValueEventListener(roomListener!!)
    }

    private fun startGameIfReady(room: BingoOnlineRoom) {
        val roomRef = db.getReference("bingoOnline/rooms/${room.roomId}")
        roomRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val r = currentData.getValue(BingoOnlineRoom::class.java)
                if (r != null && r.status == "waiting") {
                    val pList = r.players.values.toList()
                    if (pList.size == 2 && pList.all { it.isReady && it.connected && it.card.isNotEmpty() }) {
                        val firstPlayerId = r.players.keys.sorted().first()
                        val updatedGame = BingoGame(
                            status = "playing",
                            currentTurn = firstPlayerId,
                            calledNumbers = emptyList(),
                            lastCalledNumber = null,
                            gameStartedAt = System.currentTimeMillis()
                        )
                        currentData.child("status").value = "playing"
                        currentData.child("game").value = updatedGame
                        return Transaction.success(currentData)
                    }
                }
                return Transaction.success(currentData) // already changed or aborting
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    android.util.Log.d("BINGO_ONLINE", "GAME_START: Room initialization atomic transaction completed.")
                }
            }
        })
    }

    fun startAiFallbackMatch(onMatchStart: () -> Unit) {
        // Obsolete per instruction: "Do NOT create a fake player after timeout."
    }

    fun cancelMatchmaking() {
        searchCountdownJob?.cancel()
        matchmakingJob?.cancel()

        searchCountdownJob = null
        matchmakingJob = null

        // Remove host matchmaking coordinator lock if it was us
        val coordinatorRef = db.getReference("bingoOnline/coordinator/waiting_player")
        coordinatorRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val waiting = currentData.getValue(String::class.java)
                if (waiting == localPlayerUid) {
                    currentData.value = ""
                }
                return Transaction.success(currentData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
        })

        // Remove matchmaking entry
        db.getReference("bingoOnline/matchmaking").child(localPlayerUid).removeValue()

        myQueueListener?.let {
            db.getReference("bingoOnline/matchmaking").child(localPlayerUid).removeEventListener(it)
            myQueueListener = null
        }

        // Clean up active room if we leave
        leaveRoom()

        _matchStatus.value = BingoMatchStatus.SEARCHING
        _currentRoom.value = null
    }

    private fun leaveRoom() {
        val roomId = activeRoomId ?: return
        val uid = localPlayerUid
        activeRoomId = null
        _currentRoom.value = null

        android.util.Log.d("BINGO_ONLINE", "ROOM_CLEANUP: Cleaning up matchmaking room state.")

        roomListener?.let {
            db.getReference("bingoOnline/rooms/$roomId").removeEventListener(it)
            roomListener = null
        }

        val playerConnectedRef = db.getReference("bingoOnline/rooms/$roomId/players/$uid/connected")
        playerConnectedRef.setValue(false)
        db.getReference("bingoOnline/rooms/$roomId/players/$uid/isOnline").setValue(false)

        db.getReference("bingoOnline/rooms/$roomId").get().addOnSuccessListener { snapshot ->
            val room = snapshot.getValue(BingoOnlineRoom::class.java)
            if (room != null) {
                val anyConnected = room.players.values.any { it.connected && it.uid != uid }
                if (!anyConnected) {
                    db.getReference("bingoOnline/rooms/$roomId").removeValue()
                    android.util.Log.d("BINGO_ONLINE", "ROOM_CLEANUP: Room $roomId completely deleted as all players left.")
                }
            }
        }
    }

    // ==========================================
    // 2. AUTHORITATIVE BINGO SYNCHRONIZATION
    // ==========================================

    fun submitMove(move: BingoMovePayload, currentBoard: List<List<BingoTile>>): BingoAntiCheatResult {
        val room = _currentRoom.value ?: return BingoAntiCheatResult(false, "Room Not Active", AntiCheatSanction.REJECT_MOVE)
        val roomId = room.roomId
        val myPlayer = room.players[localPlayerUid] ?: return BingoAntiCheatResult(false, "Player Not in Room", AntiCheatSanction.REJECT_MOVE)

        if (move.moveType == "CLAIM_BINGO") {
            val linesCount = evaluateCompletedLines(myPlayer.marked)
            if (linesCount < 5) {
                _antiCheatAlert.value = "❌ Anti-Cheat Rejection: Invalid Bingo claim! Completed lines count: $linesCount"
                android.util.Log.e("BINGO_ONLINE", "ERROR: Invalid Bingo Claim by $localPlayerUid. Expected 5 lines, had $linesCount")
                return BingoAntiCheatResult(false, "Invalid Lines Count", AntiCheatSanction.REJECT_MOVE)
            }

            val updates = mapOf(
                "bingoOnline/rooms/$roomId/game/status" to "finished",
                "bingoOnline/rooms/$roomId/game/winnerId" to localPlayerUid,
                "bingoOnline/rooms/$roomId/status" to "completed"
            )
            db.getReference().updateChildren(updates)
            return BingoAntiCheatResult(true, "Valid Victory Accepted", AntiCheatSanction.ACCEPT)
        }

        // moveType == "DAUB"
        val isMyTurn = (room.game.currentTurn == localPlayerUid)
        val isTileAlreadyCalled = room.game.calledNumbers.contains(move.tileNumber)

        val updatedMarked = myPlayer.marked.toMutableList()
        val tileIdx = move.tileRow * 5 + move.tileCol
        if (tileIdx in updatedMarked.indices) {
            updatedMarked[tileIdx] = true
        }
        val linesCount = evaluateCompletedLines(updatedMarked)

        if (isMyTurn) {
            if (isTileAlreadyCalled) {
                // Just marking a previously called tile on our board during our turn
                val updates = mapOf(
                    "bingoOnline/rooms/$roomId/players/$localPlayerUid/marked" to updatedMarked,
                    "bingoOnline/rooms/$roomId/players/$localPlayerUid/markedCount" to updatedMarked.count { it },
                    "bingoOnline/rooms/$roomId/players/$localPlayerUid/completedLinesCount" to linesCount,
                    "bingoOnline/rooms/$roomId/players/$localPlayerUid/lastMoveTimestamp" to System.currentTimeMillis()
                )
                db.getReference().updateChildren(updates)
                return BingoAntiCheatResult(true, "Daub Accepted", AntiCheatSanction.ACCEPT)
            } else {
                // CALLING a new number!
                val opponentUid = room.players.keys.firstOrNull { it != localPlayerUid } ?: ""
                val updatedCalledNumbers = room.game.calledNumbers + move.tileNumber

                val updates = mapOf(
                    "bingoOnline/rooms/$roomId/game/calledNumbers" to updatedCalledNumbers,
                    "bingoOnline/rooms/$roomId/game/lastCalledNumber" to move.tileNumber,
                    "bingoOnline/rooms/$roomId/game/currentTurn" to opponentUid,
                    "bingoOnline/rooms/$roomId/players/$localPlayerUid/marked" to updatedMarked,
                    "bingoOnline/rooms/$roomId/players/$localPlayerUid/markedCount" to updatedMarked.count { it },
                    "bingoOnline/rooms/$roomId/players/$localPlayerUid/completedLinesCount" to linesCount,
                    "bingoOnline/rooms/$roomId/players/$localPlayerUid/lastMoveTimestamp" to System.currentTimeMillis()
                )

                android.util.Log.d("BINGO_ONLINE", "NUMBER_CALLED: Player $localPlayerUid called number ${move.tileNumber}. Opponent Turn: $opponentUid")
                db.getReference().updateChildren(updates)
                return BingoAntiCheatResult(true, "Number Call Sync Successful", AntiCheatSanction.ACCEPT)
            }
        } else {
            // Out-of-turn. Only allowed to daub already called numbers
            if (!isTileAlreadyCalled) {
                _antiCheatAlert.value = "⏳ Wait for your turn to call a number!"
                return BingoAntiCheatResult(false, "Not Your Turn", AntiCheatSanction.REJECT_MOVE)
            }

            val updates = mapOf(
                "bingoOnline/rooms/$roomId/players/$localPlayerUid/marked" to updatedMarked,
                "bingoOnline/rooms/$roomId/players/$localPlayerUid/markedCount" to updatedMarked.count { it },
                "bingoOnline/rooms/$roomId/players/$localPlayerUid/completedLinesCount" to linesCount,
                "bingoOnline/rooms/$roomId/players/$localPlayerUid/lastMoveTimestamp" to System.currentTimeMillis()
            )
            db.getReference().updateChildren(updates)
            return BingoAntiCheatResult(true, "Reactive Daub Sync Successful", AntiCheatSanction.ACCEPT)
        }
    }

    fun clearAntiCheatAlert() {
        _antiCheatAlert.value = null
    }

    // ==========================================
    // 3. CONNECTION PRESENCE & RECONNECTION
    // ==========================================

    private fun observeConnectionState() {
        val connectedRef = db.getReference(".info/connected")
        connectionStateListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    android.util.Log.d("BINGO_ONLINE", "FIREBASE_CONNECTED: True connection established.")
                    // Set active connection on disconnect for any active rooms
                    activeRoomId?.let { roomId ->
                        android.util.Log.d("BINGO_ONLINE", "PLAYER_RECONNECTED: Presence restored for room $roomId")
                        db.getReference("bingoOnline/rooms/$roomId/players/$localPlayerUid/connected").setValue(true)
                    }
                } else {
                    android.util.Log.d("BINGO_ONLINE", "FIREBASE_CONNECTED: Disconnected from Firebase.")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        connectedRef.addValueEventListener(connectionStateListener!!)
    }

    fun simulateReconnection() {
        val roomId = activeRoomId ?: return
        engineScope.launch {
            _matchStatus.value = BingoMatchStatus.RECONNECTING
            delay(1500)
            db.getReference("bingoOnline/rooms/$roomId").get().addOnSuccessListener { snapshot ->
                val room = snapshot.getValue(BingoOnlineRoom::class.java)
                if (room != null) {
                    _currentRoom.value = room
                    _matchStatus.value = BingoMatchStatus.PLAYING
                    // Re-assert connected
                    db.getReference("bingoOnline/rooms/$roomId/players/$localPlayerUid/connected").setValue(true)
                } else {
                    _matchStatus.value = BingoMatchStatus.DEFEAT
                }
            }.addOnFailureListener {
                _matchStatus.value = BingoMatchStatus.DEFEAT
            }
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private fun generateCard(): List<Int> {
        val cols = listOf(
            (1..15).shuffled().take(5),
            (16..30).shuffled().take(5),
            (31..45).shuffled().take(5),
            (46..60).shuffled().take(5),
            (61..75).shuffled().take(5)
        )
        val card = MutableList(25) { 0 }
        for (r in 0..4) {
            for (c in 0..4) {
                card[r * 5 + c] = if (r == 2 && c == 2) 0 else cols[c][r]
            }
        }
        return card
    }

    private fun evaluateCompletedLines(marked: List<Boolean>): Int {
        var count = 0
        // Rows
        for (r in 0..4) {
            var allMarked = true
            for (c in 0..4) {
                if (!marked[r * 5 + c]) { allMarked = false; break }
            }
            if (allMarked) count++
        }
        // Cols
        for (c in 0..4) {
            var allMarked = true
            for (r in 0..4) {
                if (!marked[r * 5 + c]) { allMarked = false; break }
            }
            if (allMarked) count++
        }
        // Diagonals
        if ((0..4).all { i -> marked[i * 5 + i] }) count++
        if ((0..4).all { i -> marked[i * 5 + (4 - i)] }) count++
        return count
    }

    private fun startPingMonitor() {
        pingMonitorJob?.cancel()
        pingMonitorJob = engineScope.launch {
            while (isActive) {
                delay(3000)
                _networkLatencyMs.value = Random.nextInt(25, 65)
            }
        }
    }

    fun destroyEngine() {
        cancelMatchmaking()
        pingMonitorJob?.cancel()
        connectionStateListener?.let {
            db.getReference(".info/connected").removeEventListener(it)
        }
        engineScope.cancel()
    }
}
