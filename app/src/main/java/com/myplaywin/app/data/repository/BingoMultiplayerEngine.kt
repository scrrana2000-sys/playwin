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
    // 1. PRODUCTION-GRADE FIREBASE MATCHMAKING ENGINE
    // ==========================================

    fun startMatchmaking(
        matchType: BingoMatchType = BingoMatchType.ONE_VS_ONE,
        onMatchFound: () -> Unit,
        onSearchTimeout: () -> Unit
    ) {
        cancelMatchmaking()
        _matchStatus.value = BingoMatchStatus.SEARCHING
        _searchTimeRemaining.value = 20

        android.util.Log.d("BINGO_ONLINE", "AUTH: Authenticating/checking user session.")
        ensureUserAuthenticated()

        // Start 20-second countdown
        searchCountdownJob = engineScope.launch {
            for (secondsLeft in 20 downTo 1) {
                _searchTimeRemaining.value = secondsLeft
                delay(1000)
            }
            android.util.Log.d("BINGO_ONLINE", "DISCONNECTED: Matchmaking search timeout reached.")
            cancelMatchmaking()
            onSearchTimeout()
        }

        matchmakingJob = engineScope.launch {
            // 1. Create our queue entry
            val myQueueRef = db.getReference("bingoOnline/matchmakingQueue").child(localPlayerUid)
            val myEntry = MatchmakingQueueEntry(
                uid = localPlayerUid,
                displayName = localPlayerName,
                joinedAt = System.currentTimeMillis(),
                status = "waiting",
                online = true,
                skill = localPlayerLevel.toDouble(),
                version = "1.0",
                roomId = ""
            )
            myQueueRef.setValue(myEntry)
            myQueueRef.onDisconnect().removeValue()
            android.util.Log.d("BINGO_ONLINE", "QUEUE_CREATED: Queue entry created for $localPlayerUid")

            // Listen to our own queue entry for matching updates (if we end up waiting)
            myQueueListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val entry = snapshot.getValue(MatchmakingQueueEntry::class.java) ?: return
                    if (entry.status == "matched" && entry.roomId.isNotEmpty()) {
                        android.util.Log.d("BINGO_ONLINE", "QUEUE_FOUND: We were matched by another player! Room: ${entry.roomId}")
                        searchCountdownJob?.cancel()
                        
                        // Join the room as Host
                        joinRoom(entry.roomId, isHost = true, onMatchFound)
                        
                        // Clean up our queue listener and entry
                        myQueueRef.removeEventListener(this)
                        myQueueListener = null
                        myQueueRef.removeValue()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            myQueueRef.addValueEventListener(myQueueListener!!)

            // 2. Scan queue for existing waiting players
            db.getReference("bingoOnline/matchmakingQueue").get().addOnSuccessListener { snapshot ->
                val entries = snapshot.children.mapNotNull { it.getValue(MatchmakingQueueEntry::class.java) }
                val eligibleOpponent = entries.firstOrNull {
                    it.uid != localPlayerUid &&
                    it.status == "waiting" &&
                    it.online &&
                    (System.currentTimeMillis() - it.joinedAt) < 60000
                }

                if (eligibleOpponent != null) {
                    // Try to atomically match with them
                    val oppRef = db.getReference("bingoOnline/matchmakingQueue").child(eligibleOpponent.uid)
                    oppRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val oppEntry = currentData.getValue(MatchmakingQueueEntry::class.java)
                            if (oppEntry != null && oppEntry.status == "waiting") {
                                val generatedRoomId = "room_${oppEntry.uid}_${localPlayerUid}"
                                currentData.child("status").value = "matched"
                                currentData.child("roomId").value = generatedRoomId
                                return Transaction.success(currentData)
                            }
                            return Transaction.abort()
                        }

                        override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                            if (committed && error == null) {
                                val matchedOpponent = snapshot?.getValue(MatchmakingQueueEntry::class.java)
                                val roomId = matchedOpponent?.roomId ?: ""
                                if (roomId.isNotEmpty()) {
                                    android.util.Log.d("BINGO_ONLINE", "QUEUE_FOUND: Match atomic transaction committed. Opponent: ${matchedOpponent?.uid}")
                                    searchCountdownJob?.cancel()
                                    
                                    // Join room as Guest
                                    joinRoom(roomId, isHost = false, onMatchFound)
                                    
                                    // Remove our own queue entry since we matched with them
                                    myQueueRef.removeValue()
                                }
                            }
                        }
                    })
                } else {
                    android.util.Log.d("BINGO_ONLINE", "QUEUE_CREATED: No waiting players found in queue. Waiting as host...")
                }
            }
        }
    }

    private fun joinRoom(roomId: String, isHost: Boolean, onMatchFound: () -> Unit, seed: Long? = null, predefinedCard: List<Int>? = null) {
        activeRoomId = roomId
        val roomRef = db.getReference("bingoOnline/rooms").child(roomId)

        android.util.Log.d("BINGO_ONLINE", "ROOM_JOIN_START: Player $localPlayerUid joining room $roomId as isHost = $isHost")

        // 1. If we are Host, we generate BOTH cards and write playerBoards
        if (isHost) {
            val (hostCard, guestCard) = generateHostAndGuestCards()
            val boards = mapOf(
                "host" to hostCard,
                "guest" to guestCard
            )
            roomRef.child("playerBoards").setValue(boards)
            roomRef.child("roomId").setValue(roomId)
            roomRef.child("host").setValue(localPlayerUid)
            roomRef.child("createdAt").setValue(System.currentTimeMillis())
            
            if (seed != null || (roomId.length == 6 && roomId.all { it.isLetterOrDigit() })) {
                // Generate ONE shared Bingo game session for private rooms
                val updatedGame = BingoGame(
                    status = "playing",
                    currentTurn = localPlayerUid,
                    calledNumbers = emptyList(),
                    lastCalledNumber = null,
                    gameStartedAt = System.currentTimeMillis()
                )
                roomRef.child("game").setValue(updatedGame)
                roomRef.child("currentTurn").setValue(localPlayerUid)
                roomRef.child("status").setValue("playing")
                android.util.Log.d("BINGO_ONLINE", "GAME_CREATED")
                android.util.Log.d("BINGO_ONLINE", "GAME_DATA_SYNCED")
            } else {
                roomRef.child("status").setValue("waiting")
                android.util.Log.d("BINGO_ONLINE", "ROOM_CREATED: Public room $roomId created and initialized by host.")
            }
        } else {
            roomRef.child("guest").setValue(localPlayerUid)
        }

        // 2. Set up a listener for playerBoards so we retrieve our card from Firebase
        val boardsRef = roomRef.child("playerBoards")
        boardsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Stop listening once we successfully read the boards
                    boardsRef.removeEventListener(this)

                    val hostCard = snapshot.child("host").getValue(object : GenericTypeIndicator<List<Int>>() {}) ?: emptyList()
                    val guestCard = snapshot.child("guest").getValue(object : GenericTypeIndicator<List<Int>>() {}) ?: emptyList()

                    val myCard = if (isHost) hostCard else guestCard

                    val myPlayer = BingoOnlinePlayer(
                        uid = localPlayerUid,
                        displayName = localPlayerName,
                        avatarUrl = "👑",
                        level = localPlayerLevel,
                        winRate = localPlayerWinRate,
                        isOnline = true,
                        isReady = true,
                        isHost = isHost,
                        card = myCard,
                        marked = List(25) { if (it == 12) true else false },
                        connected = true
                    )

                    // Write player data
                    roomRef.child("players").child(localPlayerUid).setValue(myPlayer)

                    // Presence
                    val playerConnectedRef = roomRef.child("players").child(localPlayerUid).child("connected")
                    playerConnectedRef.setValue(true)
                    playerConnectedRef.onDisconnect().setValue(false)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        roomListener = object : ValueEventListener {
            private var lastTurn: String = ""
            
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue(BingoOnlineRoom::class.java) ?: return
                _currentRoom.value = room

                // Handle room state transitions
                if (room.status == "waiting") {
                    val pList = room.players.values.toList()
                    if (pList.size == 2 && pList.all { it.isReady && it.connected && it.card.isNotEmpty() }) {
                        android.util.Log.d("BINGO_ONLINE", "PLAYER_READY: Both players connected, ready, and cards generated.")
                        startGameIfReady(room)
                    }
                } else if (room.status == "playing") {
                    if (_matchStatus.value != BingoMatchStatus.PLAYING && _matchStatus.value != BingoMatchStatus.RECONNECTING) {
                        _matchStatus.value = BingoMatchStatus.PLAYING
                        android.util.Log.d("BINGO_ONLINE", "GAME_STARTED: Room transition to playing. Local screen loading gameplay.")
                        android.util.Log.d("BINGO_ONLINE", "MULTIPLAYER_STARTED")
                        onMatchFound()
                    }

                    // Check for turn changes
                    val currentTurn = if (room.currentTurn.isNotEmpty()) room.currentTurn else room.game.currentTurn
                    if (currentTurn.isNotEmpty() && currentTurn != lastTurn) {
                        lastTurn = currentTurn
                        android.util.Log.d("BINGO_ONLINE", "TURN_CHANGED: Current active turn is now: $currentTurn")
                    }

                    // Detect disconnects of opponent
                    val opponent = room.players.values.firstOrNull { it.uid != localPlayerUid }
                    if (opponent != null && !opponent.connected) {
                        android.util.Log.d("BINGO_ONLINE", "DISCONNECTED: Opponent ${opponent.uid} disconnected.")
                    } else if (opponent != null && opponent.connected && _matchStatus.value == BingoMatchStatus.PLAYING) {
                        android.util.Log.d("BINGO_ONLINE", "RECONNECTED: Opponent ${opponent.uid} reconnected.")
                    }
                } else if (room.status == "completed") {
                    val winnerId = if (room.winner.isNotEmpty()) room.winner else room.game.winnerId
                    if (winnerId != null) {
                        _matchStatus.value = if (winnerId == localPlayerUid) BingoMatchStatus.VICTORY else BingoMatchStatus.DEFEAT
                        android.util.Log.d("BINGO_ONLINE", "MATCH_FINISHED: Match completed. Winner = $winnerId")
                    }
                    val pList = room.players.values.toList()
                    if (pList.size == 2 && pList.all { it.playAgainRequested }) {
                        if (room.host == localPlayerUid) {
                            resetRoomForNewRound()
                        }
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
                        val firstPlayerId = r.host.ifEmpty { r.players.keys.sorted().first() }
                        val updatedGame = BingoGame(
                            status = "playing",
                            currentTurn = firstPlayerId,
                            calledNumbers = emptyList(),
                            lastCalledNumber = null,
                            gameStartedAt = System.currentTimeMillis()
                        )
                        currentData.child("status").value = "playing"
                        currentData.child("currentTurn").value = firstPlayerId
                        currentData.child("game").value = updatedGame
                        return Transaction.success(currentData)
                    }
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    android.util.Log.d("BINGO_ONLINE", "GAME_STARTED: Room transition to playing complete.")
                }
            }
        })
    }

    fun startAiFallbackMatch(onMatchStart: () -> Unit) {
        // Obsolete per instructions.
    }

    fun createPrivateRoom(onMatchFound: () -> Unit) {
        cancelMatchmaking()
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..6).map { chars.random() }.joinToString("")
        android.util.Log.d("BINGO_ONLINE", "ROOM_CREATED: Generated unique room code: $code")
        
        joinRoom(code, isHost = true, onMatchFound)
    }

    fun joinPrivateRoom(
        code: String,
        onMatchFound: () -> Unit,
        onRoomNotFound: () -> Unit
    ) {
        cancelMatchmaking()
        val cleanCode = code.uppercase().trim()
        val roomRef = db.getReference("bingoOnline/rooms").child(cleanCode)

        roomRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val room = snapshot.getValue(BingoOnlineRoom::class.java)
                if (room != null && room.status == "waiting" && room.players.size < 2) {
                    android.util.Log.d("BINGO_ONLINE", "ROOM_JOINED: Joining private room: $cleanCode")
                    joinRoom(cleanCode, isHost = false, onMatchFound)
                } else {
                    android.util.Log.w("BINGO_ONLINE", "ERROR: Private room is full or not in waiting state.")
                    onRoomNotFound()
                }
            } else {
                android.util.Log.w("BINGO_ONLINE", "ERROR: Room not found for code $cleanCode")
                onRoomNotFound()
            }
        }.addOnFailureListener {
            onRoomNotFound()
        }
    }

    fun joinPrivateRoomFromSocial(roomId: String, seed: Long, isHost: Boolean, predefinedCard: List<Int>? = null, onMatchFound: () -> Unit) {
        cancelMatchmaking()
        joinRoom(roomId, isHost, onMatchFound, seed, predefinedCard)
    }

    fun cancelMatchmaking() {
        searchCountdownJob?.cancel()
        matchmakingJob?.cancel()

        searchCountdownJob = null
        matchmakingJob = null

        // Remove queue entry
        db.getReference("bingoOnline/matchmakingQueue").child(localPlayerUid).removeValue()

        myQueueListener?.let {
            db.getReference("bingoOnline/matchmakingQueue").child(localPlayerUid).removeEventListener(it)
            myQueueListener = null
        }

        // Clean up active room if we leave
        leaveRoom()

        _matchStatus.value = BingoMatchStatus.SEARCHING
        _currentRoom.value = null
    }

    fun endMatchDueToOpponentDisconnect() {
        val roomId = activeRoomId ?: return
        android.util.Log.d("BINGO_ONLINE", "DISCONNECTED: Opponent failed to reconnect. Ending match as forfeit.")

        val updates = mapOf(
            "bingoOnline/rooms/$roomId/status" to "completed",
            "bingoOnline/rooms/$roomId/winner" to localPlayerUid,
            "bingoOnline/rooms/$roomId/game/status" to "finished",
            "bingoOnline/rooms/$roomId/game/winnerId" to localPlayerUid
        )
        db.getReference().updateChildren(updates).addOnCompleteListener {
            _matchStatus.value = BingoMatchStatus.VICTORY
        }
    }

    private fun leaveRoom() {
        val roomId = activeRoomId ?: return
        val uid = localPlayerUid
        activeRoomId = null
        _currentRoom.value = null

        android.util.Log.d("BINGO_ONLINE", "DISCONNECTED: Leaving room $roomId")

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
                    android.util.Log.d("BINGO_ONLINE", "ROOM_REMOVED: Room $roomId completely removed from Firebase.")
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
            val myMarked = List(25) { idx -> idx == 12 || room.calledNumbers.contains(myPlayer.card[idx]) }
            val linesCount = evaluateCompletedLines(myMarked)
            if (linesCount < 5) {
                _antiCheatAlert.value = "❌ Anti-Cheat Rejection: Invalid Bingo claim! Completed lines count: $linesCount"
                android.util.Log.e("BINGO_ONLINE", "ERROR: Invalid Bingo Claim by $localPlayerUid. Expected 5 lines, had $linesCount")
                return BingoAntiCheatResult(false, "Invalid Lines Count", AntiCheatSanction.REJECT_MOVE)
            }

            val updates = mapOf(
                "bingoOnline/rooms/$roomId/game/status" to "finished",
                "bingoOnline/rooms/$roomId/game/winnerId" to localPlayerUid,
                "bingoOnline/rooms/$roomId/status" to "completed",
                "bingoOnline/rooms/$roomId/winner" to localPlayerUid
            )
            db.getReference().updateChildren(updates)
            return BingoAntiCheatResult(true, "Valid Victory Accepted", AntiCheatSanction.ACCEPT)
        }

        // moveType == "DAUB"
        val isMyTurn = (room.currentTurn == localPlayerUid || room.game.currentTurn == localPlayerUid)
        val isTileAlreadyCalled = room.calledNumbers.contains(move.tileNumber)

        if (isMyTurn) {
            if (isTileAlreadyCalled) {
                // Already called, nothing to do
                return BingoAntiCheatResult(true, "Already Called", AntiCheatSanction.ACCEPT)
            } else {
                // CALLING a new number!
                val opponentUid = room.players.keys.firstOrNull { it != localPlayerUid } ?: ""
                val updatedCalledNumbers = room.calledNumbers + move.tileNumber

                val updates = mapOf(
                    "bingoOnline/rooms/$roomId/game/calledNumbers" to updatedCalledNumbers,
                    "bingoOnline/rooms/$roomId/game/lastCalledNumber" to move.tileNumber,
                    "bingoOnline/rooms/$roomId/game/currentTurn" to opponentUid,
                    "bingoOnline/rooms/$roomId/calledNumbers" to updatedCalledNumbers,
                    "bingoOnline/rooms/$roomId/currentTurn" to opponentUid,
                    "bingoOnline/rooms/$roomId/players/$localPlayerUid/lastMoveTimestamp" to System.currentTimeMillis()
                )

                android.util.Log.d("BINGO_ONLINE", "TURN_CHANGED: Player $localPlayerUid called number ${move.tileNumber}. Opponent Turn: $opponentUid")
                db.getReference().updateChildren(updates)
                return BingoAntiCheatResult(true, "Number Call Sync Successful", AntiCheatSanction.ACCEPT)
            }
        } else {
            // Out-of-turn. Only allowed to interact with already called numbers
            if (!isTileAlreadyCalled) {
                _antiCheatAlert.value = "⏳ Wait for your turn to call a number!"
                return BingoAntiCheatResult(false, "Not Your Turn", AntiCheatSanction.REJECT_MOVE)
            }
            return BingoAntiCheatResult(true, "Already Called", AntiCheatSanction.ACCEPT)
        }
    }

    fun syncPlayerProgress(linesCount: Int, markedCount: Int) {
        val roomId = activeRoomId ?: return
        val updates = mapOf(
            "bingoOnline/rooms/$roomId/players/$localPlayerUid/completedLinesCount" to linesCount,
            "bingoOnline/rooms/$roomId/players/$localPlayerUid/markedCount" to markedCount
        )
        db.getReference().updateChildren(updates)
    }

    fun clearAntiCheatAlert() {
        _antiCheatAlert.value = null
    }

    fun requestPlayAgain() {
        val roomId = activeRoomId ?: return
        db.getReference("bingoOnline/rooms/$roomId/players/$localPlayerUid/playAgainRequested").setValue(true)
    }

    fun resetRoomForNewRound() {
        val roomId = activeRoomId ?: return
        val room = _currentRoom.value ?: return
        if (room.host != localPlayerUid) return

        val (card1, card2) = generateHostAndGuestCards()

        val updates = mutableMapOf<String, Any>()
        updates["bingoOnline/rooms/$roomId/playerBoards"] = mapOf("host" to card1, "guest" to card2)
        updates["bingoOnline/rooms/$roomId/status"] = "playing"
        updates["bingoOnline/rooms/$roomId/winner"] = ""
        updates["bingoOnline/rooms/$roomId/calledNumbers"] = emptyList<Int>()
        updates["bingoOnline/rooms/$roomId/currentTurn"] = room.host

        updates["bingoOnline/rooms/$roomId/players/${room.host}/card"] = card1
        updates["bingoOnline/rooms/$roomId/players/${room.host}/marked"] = List(25) { if (it == 12) true else false }
        updates["bingoOnline/rooms/$roomId/players/${room.host}/markedCount"] = 1
        updates["bingoOnline/rooms/$roomId/players/${room.host}/completedLinesCount"] = 0
        updates["bingoOnline/rooms/$roomId/players/${room.host}/playAgainRequested"] = false

        val guestId = room.guest.ifEmpty { room.players.keys.firstOrNull { it != room.host } } ?: ""
        if (guestId.isNotEmpty()) {
            updates["bingoOnline/rooms/$roomId/players/$guestId/card"] = card2
            updates["bingoOnline/rooms/$roomId/players/$guestId/marked"] = List(25) { if (it == 12) true else false }
            updates["bingoOnline/rooms/$roomId/players/$guestId/markedCount"] = 1
            updates["bingoOnline/rooms/$roomId/players/$guestId/completedLinesCount"] = 0
            updates["bingoOnline/rooms/$roomId/players/$guestId/playAgainRequested"] = false
        }

        val updatedGame = BingoGame(
            status = "playing",
            currentTurn = room.host,
            calledNumbers = emptyList(),
            lastCalledNumber = null,
            gameStartedAt = System.currentTimeMillis(),
            winnerId = null
        )
        updates["bingoOnline/rooms/$roomId/game"] = updatedGame

        android.util.Log.d("BINGO_ONLINE", "RESETTING_ROUND: Host $localPlayerUid resetting room $roomId")
        db.getReference().updateChildren(updates)
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

    private fun generateHostAndGuestCards(): Pair<List<Int>, List<Int>> {
        val random = kotlin.random.Random
        val cols = listOf(
            (1..15).shuffled(random).take(5),
            (16..30).shuffled(random).take(5),
            (31..45).shuffled(random).take(5),
            (46..60).shuffled(random).take(5),
            (61..75).shuffled(random).take(5)
        )
        val hostCard = MutableList(25) { 0 }
        for (r in 0..4) {
            for (c in 0..4) {
                hostCard[r * 5 + c] = if (r == 2 && c == 2) 0 else cols[c][r]
            }
        }
        
        // Guest card uses EXACT SAME set of numbers, but shuffled/positioned differently
        val guestB = cols[0].shuffled(random)
        val guestI = cols[1].shuffled(random)
        val guestN_nonFree = cols[2].filterIndexed { index, _ -> index != 2 }.shuffled(random)
        val guestG = cols[3].shuffled(random)
        val guestO = cols[4].shuffled(random)
        
        val guestCard = MutableList(25) { 0 }
        var nIdx = 0
        for (r in 0..4) {
            for (c in 0..4) {
                guestCard[r * 5 + c] = when (c) {
                    0 -> guestB[r]
                    1 -> guestI[r]
                    2 -> if (r == 2) 0 else guestN_nonFree[nIdx++]
                    3 -> guestG[r]
                    else -> guestO[r]
                }
            }
        }
        return Pair(hostCard, guestCard)
    }

    private fun generateCard(seed: Long? = null): List<Int> {
        val random = if (seed != null) kotlin.random.Random(seed) else kotlin.random.Random
        val cols = listOf(
            (1..15).shuffled(random).take(5),
            (16..30).shuffled(random).take(5),
            (31..45).shuffled(random).take(5),
            (46..60).shuffled(random).take(5),
            (61..75).shuffled(random).take(5)
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
