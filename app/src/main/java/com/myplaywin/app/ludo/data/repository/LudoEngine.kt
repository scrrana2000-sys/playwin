package com.myplaywin.app.ludo.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.myplaywin.app.ludo.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class LudoEngine {
    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance("https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app")
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _gameState = MutableStateFlow<LudoGameState?>(null)
    val gameState: StateFlow<LudoGameState?> = _gameState.asStateFlow()

    private var currentRoomId: String? = null
    private var roomListener: ValueEventListener? = null
    private var isLocalBotMatch: Boolean = false

    /**
     * Start a Local 4-Player Pass & Play Match (Red, Green, Yellow, Blue with 4 tokens each)
     */
    fun startLocalMatch() {
        isLocalBotMatch = true

        val localPlayers = listOf(
            LudoPlayer(
                uid = "local_red",
                name = "Player 1 (Red)",
                colorIndex = 0, // RED
                isBot = false,
                isReady = true,
                isOnline = true,
                tokens = listOf(0, 0, 0, 0)
            ),
            LudoPlayer(
                uid = "local_green",
                name = "Player 2 (Green)",
                colorIndex = 1, // GREEN
                isBot = false,
                isReady = true,
                isOnline = true,
                tokens = listOf(0, 0, 0, 0)
            ),
            LudoPlayer(
                uid = "local_yellow",
                name = "Player 3 (Yellow)",
                colorIndex = 2, // YELLOW
                isBot = false,
                isReady = true,
                isOnline = true,
                tokens = listOf(0, 0, 0, 0)
            ),
            LudoPlayer(
                uid = "local_blue",
                name = "Player 4 (Blue)",
                colorIndex = 3, // BLUE
                isBot = false,
                isReady = true,
                isOnline = true,
                tokens = listOf(0, 0, 0, 0)
            )
        )

        val localGame = LudoGameState(
            roomId = "local_match_${System.currentTimeMillis()}",
            roomCode = "LOCAL",
            hostUid = "local_red",
            hostName = "Player 1 (Red)",
            gameMode = "LOCAL_PASS_AND_PLAY",
            maxPlayers = 4,
            status = LudoRoomStatus.PLAYING.name,
            players = localPlayers,
            currentTurnIndex = 0,
            currentTurnUid = "local_red",
            diceRoll = 0,
            hasRolled = false,
            movableTokenIndices = emptyList(),
            lastActionText = "Local 4-Player Game Started! Red's turn to roll 🎲"
        )

        _gameState.value = localGame
        currentRoomId = localGame.roomId
    }

    /**
     * Create or Start a new Ludo Match
     */
    fun createRoom(
        hostUid: String,
        hostName: String,
        hostAvatar: String,
        gameMode: LudoGameMode,
        isPrivate: Boolean = false,
        maxPlayersOverride: Int? = null,
        onComplete: (String) -> Unit = {}
    ) {
        val code = if (isPrivate) generateRoomCode() else (100000..999999).random().toString()
        val roomId = "ludo_$code"
        val maxCap = maxPlayersOverride ?: gameMode.playerCapacity

        isLocalBotMatch = (gameMode == LudoGameMode.VS_BOT)

        val hostPlayer = LudoPlayer(
            uid = hostUid,
            name = hostName.ifBlank { "Player 1" },
            avatarUrl = hostAvatar,
            colorIndex = 0, // RED
            isBot = false,
            isReady = true,
            isOnline = true
        )

        val initialPlayers = mutableListOf(hostPlayer)

        if (gameMode == LudoGameMode.VS_BOT) {
            // Add Bot players for practice
            val botColors = listOf(1, 2, 3) // GREEN, YELLOW, BLUE
            val botNames = listOf("Alpha Bot 🤖", "Cyber Bot 🤖", "Nexus Bot 🤖")
            for (i in 0 until (maxCap - 1)) {
                initialPlayers.add(
                    LudoPlayer(
                        uid = "bot_${i + 1}",
                        name = botNames.getOrElse(i) { "Bot ${i + 1} 🤖" },
                        avatarUrl = "",
                        colorIndex = botColors.getOrElse(i) { (i + 1) % 4 },
                        isBot = true,
                        isReady = true,
                        isOnline = true
                    )
                )
            }
        }

        val initialStatus = if (isLocalBotMatch) LudoRoomStatus.PLAYING.name else LudoRoomStatus.WAITING.name

        val newGame = LudoGameState(
            roomId = roomId,
            roomCode = code,
            hostUid = hostUid,
            hostName = hostName,
            gameMode = gameMode.name,
            maxPlayers = maxCap,
            status = initialStatus,
            players = initialPlayers,
            currentTurnIndex = 0,
            currentTurnUid = hostUid,
            lastActionText = if (isLocalBotMatch) "Game started! Red's turn to roll." else "Waiting for players to join..."
        )

        _gameState.value = newGame
        currentRoomId = roomId

        if (!isLocalBotMatch) {
            val roomRef = database.getReference("ludo_rooms").child(roomId)
            roomRef.setValue(newGame).addOnCompleteListener {
                listenToRoom(roomId)
                onComplete(code)
            }
        } else {
            onComplete(code)
        }
    }

    /**
     * Join an existing Private / Online Room with Room Code
     */
    fun joinRoom(
        roomCode: String,
        userUid: String,
        userName: String,
        userAvatar: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val cleanCode = roomCode.trim().uppercase()
        val roomId = if (cleanCode.startsWith("LUDO_")) cleanCode else "ludo_$cleanCode"

        val roomRef = database.getReference("ludo_rooms").child(roomId)
        roomRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                onResult(false, "Room $cleanCode does not exist.")
                return@addOnSuccessListener
            }

            val game = snapshot.getValue(LudoGameState::class.java)
            if (game == null) {
                onResult(false, "Invalid room data.")
                return@addOnSuccessListener
            }

            if (game.status == LudoRoomStatus.FINISHED.name || game.status == LudoRoomStatus.CANCELLED.name) {
                onResult(false, "This game room has ended.")
                return@addOnSuccessListener
            }

            val existingPlayer = game.players.find { it.uid == userUid }
            if (existingPlayer != null) {
                // Rejoining room
                listenToRoom(roomId)
                onResult(true, "Rejoined room $cleanCode")
                return@addOnSuccessListener
            }

            if (game.players.size >= game.maxPlayers) {
                onResult(false, "Room is full (${game.players.size}/${game.maxPlayers}).")
                return@addOnSuccessListener
            }

            // Assign next unused color
            val usedColors = game.players.map { it.colorIndex }.toSet()
            val availableColors = listOf(0, 1, 2, 3).filter { it !in usedColors }
            val newColorIndex = availableColors.firstOrNull() ?: game.players.size % 4

            val newPlayer = LudoPlayer(
                uid = userUid,
                name = userName.ifBlank { "Player ${game.players.size + 1}" },
                avatarUrl = userAvatar,
                colorIndex = newColorIndex,
                isBot = false,
                isReady = true,
                isOnline = true
            )

            val updatedPlayers = game.players + newPlayer
            val updates = mapOf(
                "players" to updatedPlayers,
                "updatedAt" to System.currentTimeMillis()
            )

            roomRef.updateChildren(updates).addOnSuccessListener {
                listenToRoom(roomId)
                onResult(true, "Successfully joined room $cleanCode!")
            }.addOnFailureListener { e ->
                onResult(false, "Failed to join: ${e.message}")
            }
        }.addOnFailureListener { e ->
            onResult(false, "Error connecting to server: ${e.message}")
        }
    }

    /**
     * Start the match (Host action in waiting lobby)
     */
    fun startMatch(hostUid: String) {
        val game = _gameState.value ?: return
        if (game.hostUid != hostUid) return

        if (game.players.size < 2) {
            // Fill remaining slots with bots if host forces start
            val currentCount = game.players.size
            val updatedPlayers = game.players.toMutableList()
            val usedColors = updatedPlayers.map { it.colorIndex }.toSet()
            val availableColors = listOf(0, 1, 2, 3).filter { it !in usedColors }

            for (i in 0 until (game.maxPlayers - currentCount)) {
                val colorIdx = availableColors.getOrElse(i) { (currentCount + i) % 4 }
                updatedPlayers.add(
                    LudoPlayer(
                        uid = "bot_${i + 1}",
                        name = "Bot ${i + 1} 🤖",
                        colorIndex = colorIdx,
                        isBot = true,
                        isReady = true,
                        isOnline = true
                    )
                )
            }

            val updatedGame = game.copy(
                status = LudoRoomStatus.PLAYING.name,
                players = updatedPlayers,
                currentTurnIndex = 0,
                currentTurnUid = updatedPlayers[0].uid,
                lastActionText = "Match started! ${updatedPlayers[0].name}'s turn."
            )

            updateStateAndSync(updatedGame)
        } else {
            val updatedGame = game.copy(
                status = LudoRoomStatus.PLAYING.name,
                currentTurnIndex = 0,
                currentTurnUid = game.players[0].uid,
                lastActionText = "Match started! ${game.players[0].name}'s turn."
            )
            updateStateAndSync(updatedGame)
        }
    }

    private var isAnimatingToken: Boolean = false

    /**
     * Reset/Restart current game match
     */
    fun resetGame() {
        val game = _gameState.value ?: return
        val resetPlayers = game.players.map { player ->
            player.copy(
                tokens = listOf(0, 0, 0, 0),
                rank = 0
            )
        }

        isAnimatingToken = false

        val resetGame = game.copy(
            status = LudoRoomStatus.PLAYING.name,
            players = resetPlayers,
            currentTurnIndex = 0,
            currentTurnUid = resetPlayers.firstOrNull()?.uid ?: "",
            diceRoll = 0,
            hasRolled = false,
            movableTokenIndices = emptyList(),
            consecutiveSixes = 0,
            winnerUid = "",
            winnerName = "",
            rankings = emptyList(),
            lastActionText = "Game restarted! ${resetPlayers.firstOrNull()?.name}'s turn to roll.",
            updatedAt = System.currentTimeMillis()
        )
        updateStateAndSync(resetGame)
    }

    /**
     * Roll Dice for Current Player
     */
    fun rollDice(playerUid: String) {
        if (isAnimatingToken) return
        val game = _gameState.value ?: return
        if (game.status != LudoRoomStatus.PLAYING.name) return
        if (game.currentTurnUid != playerUid) return
        if (game.hasRolled) return

        val rolledValue = Random.nextInt(1, 7)
        val currentPlayer = game.currentTurnPlayer ?: return

        var consecutive = if (rolledValue == 6) game.consecutiveSixes + 1 else 0

        if (consecutive >= 3) {
            // Forfeit turn on 3 consecutive 6s
            val actionMsg = "${currentPlayer.name} rolled 3 consecutive 6s! Turn forfeited."
            val nextTurnIndex = getNextActiveTurnIndex(game, game.currentTurnIndex)
            val nextPlayer = game.players.getOrNull(nextTurnIndex)

            val updatedGame = game.copy(
                diceRoll = rolledValue,
                hasRolled = false,
                consecutiveSixes = 0,
                movableTokenIndices = emptyList(),
                currentTurnIndex = nextTurnIndex,
                currentTurnUid = nextPlayer?.uid ?: "",
                lastActionText = actionMsg,
                updatedAt = System.currentTimeMillis()
            )
            updateStateAndSync(updatedGame)
            checkAndTriggerBotTurn(updatedGame)
            return
        }

        // Calculate movable tokens
        val movable = calculateMovableTokens(currentPlayer, rolledValue)
        val actionText = if (movable.isEmpty()) {
            "${currentPlayer.name} rolled a $rolledValue (No legal moves)"
        } else {
            "${currentPlayer.name} rolled a $rolledValue!"
        }

        val updatedGame = game.copy(
            diceRoll = rolledValue,
            hasRolled = true,
            consecutiveSixes = consecutive,
            movableTokenIndices = movable,
            lastActionText = actionText,
            updatedAt = System.currentTimeMillis()
        )

        updateStateAndSync(updatedGame)

        if (movable.isEmpty()) {
            // Auto pass turn after 1.2s delay
            scope.launch {
                delay(1200L)
                val currentGame = _gameState.value ?: return@launch
                if (currentGame.hasRolled && currentGame.movableTokenIndices.isEmpty()) {
                    passTurnToNextPlayer(currentGame, "${currentPlayer.name} has no valid moves. Passing turn.")
                }
            }
        } else if (currentPlayer.isBot) {
            // Bot auto-selects best token move
            scope.launch {
                delay(1000L)
                val currentGame = _gameState.value ?: return@launch
                if (currentGame.currentTurnUid == playerUid && currentGame.hasRolled) {
                    val bestToken = selectBestBotTokenMove(currentPlayer, rolledValue, currentGame.movableTokenIndices, currentGame.players)
                    moveToken(playerUid, bestToken)
                }
            }
        }
    }

    /**
     * Move a specific token (0..3) for Current Player with smooth cell-by-cell animation
     */
    fun moveToken(playerUid: String, tokenIndex: Int) {
        val game = _gameState.value ?: return
        if (game.status != LudoRoomStatus.PLAYING.name) return
        if (game.currentTurnUid != playerUid) return
        if (!game.hasRolled) return
        if (tokenIndex !in game.movableTokenIndices) return
        if (isAnimatingToken) return

        isAnimatingToken = true

        scope.launch {
            val playerIndex = game.currentTurnIndex
            val player = game.players.getOrNull(playerIndex) ?: run {
                isAnimatingToken = false
                return@launch
            }
            val currentStep = player.tokens.getOrElse(tokenIndex) { 0 }
            val dice = game.diceRoll

            val startStep = currentStep
            val targetStep = if (currentStep == 0) 1 else currentStep + dice

            // Animate step by step
            if (startStep == 0) {
                val latestGame = _gameState.value ?: game
                val latestPlayers = latestGame.players.toMutableList()
                val latestPlayer = latestPlayers.getOrNull(playerIndex) ?: player
                val updatedTokens = latestPlayer.tokens.toMutableList()
                updatedTokens[tokenIndex] = 1
                latestPlayers[playerIndex] = latestPlayer.copy(tokens = updatedTokens)

                val stepGame = latestGame.copy(
                    players = latestPlayers,
                    lastActionText = "${player.name} moved Token #${tokenIndex + 1} onto starting square!"
                )
                updateStateAndSync(stepGame)
                delay(140L)
            } else {
                for (s in (startStep + 1)..targetStep) {
                    val currentGameState = _gameState.value ?: break
                    val latestPlayer = currentGameState.players.getOrNull(playerIndex) ?: break
                    val updatedTokens = latestPlayer.tokens.toMutableList()
                    updatedTokens[tokenIndex] = s
                    val updatedPlayer = latestPlayer.copy(tokens = updatedTokens)
                    val updatedPlayers = currentGameState.players.toMutableList()
                    updatedPlayers[playerIndex] = updatedPlayer
                    val stepGame = currentGameState.copy(
                        players = updatedPlayers,
                        lastActionText = "${player.name} moving Token #${tokenIndex + 1}..."
                    )
                    updateStateAndSync(stepGame)
                    delay(120L) // Smooth 120ms hop per cell
                }
            }

            // Post-movement land logic
            val finalGame = _gameState.value ?: run {
                isAnimatingToken = false
                return@launch
            }
            val finalPlayers = finalGame.players.toMutableList()
            val finalPlayer = finalPlayers.getOrNull(playerIndex) ?: run {
                isAnimatingToken = false
                return@launch
            }

            var earnedExtraTurn = (dice == 6)
            var actionSummary = "${finalPlayer.name} moved Token #${tokenIndex + 1}"

            // Check for Capturing opponent tokens
            if (targetStep in 1..51) {
                val targetTrackIndex = LudoBoardLayout.getCommonTrackIndex(finalPlayer.ludoColor, targetStep)
                val isSafe = LudoBoardLayout.isSafeCell(targetTrackIndex)

                if (!isSafe && targetTrackIndex >= 0) {
                    for (otherIdx in finalPlayers.indices) {
                        if (otherIdx == playerIndex) continue
                        val opponent = finalPlayers[otherIdx]
                        val oppTokens = opponent.tokens.toMutableList()
                        var capturedAny = false

                        for (oppTokenIdx in oppTokens.indices) {
                            val oppStep = oppTokens[oppTokenIdx]
                            val oppTrackIdx = LudoBoardLayout.getCommonTrackIndex(opponent.ludoColor, oppStep)

                            if (oppTrackIdx == targetTrackIndex) {
                                oppTokens[oppTokenIdx] = 0 // Return captured token to yard
                                capturedAny = true
                                earnedExtraTurn = true
                                actionSummary += " and CAPTURED ${opponent.name}'s token! 💥"
                            }
                        }

                        if (capturedAny) {
                            finalPlayers[otherIdx] = opponent.copy(tokens = oppTokens)
                        }
                    }
                }
            }

            if (targetStep == 57) {
                earnedExtraTurn = true
                actionSummary += " into HOME! 🎯"
            }

            // Update finished state & rankings
            val finishedPlayer = finalPlayers[playerIndex]
            var rankings = finalGame.rankings.toMutableList()
            var winnerUid = finalGame.winnerUid
            var winnerName = finalGame.winnerName

            val hasFinishedAll = finishedPlayer.tokens.all { it >= 57 }
            if (hasFinishedAll && finishedPlayer.rank == 0) {
                val place = rankings.size + 1
                rankings.add("${finishedPlayer.name} (${place}${getRankSuffix(place)})")
                finalPlayers[playerIndex] = finishedPlayer.copy(rank = place)

                if (winnerUid.isEmpty()) {
                    winnerUid = finishedPlayer.uid
                    winnerName = finishedPlayer.name
                    actionSummary = "🏆 ${finishedPlayer.name} WINS 1st PLACE! 🎉"
                }
            }

            val activePlayersRemaining = finalPlayers.count { p -> p.tokens.any { it < 57 } }
            val isGameFinished = activePlayersRemaining <= 1 || (finalGame.maxPlayers == 2 && winnerUid.isNotEmpty())

            if (isGameFinished) {
                val completedGame = finalGame.copy(
                    status = LudoRoomStatus.FINISHED.name,
                    players = finalPlayers,
                    diceRoll = 0,
                    hasRolled = false,
                    movableTokenIndices = emptyList(),
                    winnerUid = winnerUid,
                    winnerName = winnerName,
                    rankings = rankings,
                    lastActionText = "🏆 MATCH FINISHED! Winner: $winnerName",
                    updatedAt = System.currentTimeMillis()
                )
                updateStateAndSync(completedGame)
                isAnimatingToken = false
                return@launch
            }

            if (earnedExtraTurn && !hasFinishedAll) {
                val extraTurnGame = finalGame.copy(
                    players = finalPlayers,
                    diceRoll = 0,
                    hasRolled = false,
                    movableTokenIndices = emptyList(),
                    lastActionText = "$actionSummary (Earned Extra Turn! 🎲)",
                    updatedAt = System.currentTimeMillis()
                )
                updateStateAndSync(extraTurnGame)
                isAnimatingToken = false
                checkAndTriggerBotTurn(extraTurnGame)
            } else {
                val nextTurnIndex = getNextActiveTurnIndex(finalGame.copy(players = finalPlayers), playerIndex)
                val nextPlayer = finalPlayers.getOrNull(nextTurnIndex)

                val nextTurnGame = finalGame.copy(
                    players = finalPlayers,
                    currentTurnIndex = nextTurnIndex,
                    currentTurnUid = nextPlayer?.uid ?: "",
                    diceRoll = 0,
                    hasRolled = false,
                    consecutiveSixes = 0,
                    movableTokenIndices = emptyList(),
                    turnNumber = finalGame.turnNumber + 1,
                    lastActionText = "$actionSummary. Next: ${nextPlayer?.name}",
                    updatedAt = System.currentTimeMillis()
                )
                updateStateAndSync(nextTurnGame)
                isAnimatingToken = false
                checkAndTriggerBotTurn(nextTurnGame)
            }
        }
    }

    private fun passTurnToNextPlayer(game: LudoGameState, actionMsg: String) {
        val nextTurnIndex = getNextActiveTurnIndex(game, game.currentTurnIndex)
        val nextPlayer = game.players.getOrNull(nextTurnIndex)

        val updatedGame = game.copy(
            currentTurnIndex = nextTurnIndex,
            currentTurnUid = nextPlayer?.uid ?: "",
            diceRoll = 0,
            hasRolled = false,
            consecutiveSixes = 0,
            movableTokenIndices = emptyList(),
            turnNumber = game.turnNumber + 1,
            lastActionText = actionMsg,
            updatedAt = System.currentTimeMillis()
        )
        updateStateAndSync(updatedGame)
        checkAndTriggerBotTurn(updatedGame)
    }

    private fun getNextActiveTurnIndex(game: LudoGameState, currentIndex: Int): Int {
        val count = game.players.size
        if (count == 0) return 0
        var next = (currentIndex + 1) % count
        var loopCount = 0

        while (loopCount < count) {
            val player = game.players.getOrNull(next)
            if (player != null && !player.hasFinishedAllTokens) {
                return next
            }
            next = (next + 1) % count
            loopCount++
        }
        return currentIndex
    }

    private fun calculateMovableTokens(player: LudoPlayer, dice: Int): List<Int> {
        val movable = mutableListOf<Int>()
        for (i in player.tokens.indices) {
            val step = player.tokens[i]
            if (step == 0) {
                if (dice == 6) movable.add(i)
            } else if (step in 1..56) {
                if (step + dice <= 57) movable.add(i)
            }
        }
        return movable
    }

    private fun selectBestBotTokenMove(
        bot: LudoPlayer,
        dice: Int,
        movable: List<Int>,
        allPlayers: List<LudoPlayer>
    ): Int {
        if (movable.size == 1) return movable[0]

        // 1. Capture priority
        for (tokenIdx in movable) {
            val currentStep = bot.tokens[tokenIdx]
            val newStep = if (currentStep == 0) 1 else currentStep + dice
            if (newStep in 1..51) {
                val targetTrackIdx = LudoBoardLayout.getCommonTrackIndex(bot.ludoColor, newStep)
                if (!LudoBoardLayout.isSafeCell(targetTrackIdx) && targetTrackIdx >= 0) {
                    for (otherP in allPlayers) {
                        if (otherP.colorIndex == bot.colorIndex) continue
                        for (oppStep in otherP.tokens) {
                            val oppTrackIdx = LudoBoardLayout.getCommonTrackIndex(otherP.ludoColor, oppStep)
                            if (oppTrackIdx == targetTrackIdx) {
                                return tokenIdx // Capture move!
                            }
                        }
                    }
                }
            }
        }

        // 2. Finish into Home priority
        for (tokenIdx in movable) {
            val currentStep = bot.tokens[tokenIdx]
            if (currentStep + dice == 57) return tokenIdx
        }

        // 3. Open token from yard priority if rolled 6
        if (dice == 6) {
            for (tokenIdx in movable) {
                if (bot.tokens[tokenIdx] == 0) return tokenIdx
            }
        }

        // 4. Move token closest to home
        return movable.maxByOrNull { bot.tokens[it] } ?: movable[0]
    }

    private fun checkAndTriggerBotTurn(game: LudoGameState) {
        if (game.status != LudoRoomStatus.PLAYING.name) return
        val currentP = game.currentTurnPlayer ?: return
        if (currentP.isBot) {
            scope.launch {
                delay(1200L)
                val latest = _gameState.value ?: return@launch
                if (latest.currentTurnUid == currentP.uid && !latest.hasRolled) {
                    rollDice(currentP.uid)
                }
            }
        }
    }

    private fun updateStateAndSync(newState: LudoGameState) {
        _gameState.value = newState
        val rId = newState.roomId
        if (rId.isNotBlank() && !isLocalBotMatch) {
            database.getReference("ludo_rooms").child(rId).setValue(newState)
        }
    }

    private fun listenToRoom(roomId: String) {
        if (isLocalBotMatch) return
        val roomRef = database.getReference("ludo_rooms").child(roomId)

        roomListener?.let { roomRef.removeEventListener(it) }

        roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val game = snapshot.getValue(LudoGameState::class.java)
                if (game != null) {
                    _gameState.value = game
                    checkAndTriggerBotTurn(game)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        roomRef.addValueEventListener(roomListener!!)
    }

    fun leaveRoom(userUid: String? = null) {
        val game = _gameState.value
        val rId = game?.roomId.orEmpty()

        if (userUid != null && rId.isNotBlank() && !isLocalBotMatch && game != null) {
            val roomRef = database.getReference("ludo_rooms").child(rId)
            if (game.status == LudoRoomStatus.WAITING.name) {
                if (game.hostUid == userUid) {
                    // Host left in waiting lobby -> cancel room
                    roomRef.child("status").setValue(LudoRoomStatus.CANCELLED.name)
                } else {
                    // Non-host left -> remove from players list
                    val remainingPlayers = game.players.filter { it.uid != userUid }
                    roomRef.child("players").setValue(remainingPlayers)
                }
            } else if (game.status == LudoRoomStatus.PLAYING.name) {
                val updatedPlayers = game.players.map {
                    if (it.uid == userUid) it.copy(isOnline = false) else it
                }
                roomRef.child("players").setValue(updatedPlayers)
            }
        }

        roomListener?.let {
            if (rId.isNotBlank()) {
                database.getReference("ludo_rooms").child(rId).removeEventListener(it)
            }
        }
        roomListener = null
        _gameState.value = null
        currentRoomId = null
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun getRankSuffix(rank: Int): String {
        return when (rank) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}
