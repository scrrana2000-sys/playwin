package com.myplaywin.app.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.myplaywin.app.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phase 10: Live Events, Social Features & Retention Repository
 */
class BingoLiveEventsAndSocialRepository(
    private val context: Context,
    private val progressionRepository: BingoProgressionRepository
) {

    private val auth = FirebaseAuth.getInstance()
    private val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val database = FirebaseDatabase.getInstance(dbUrl).reference
    private val prefs = context.getSharedPreferences("bingo_social_liveops_prefs", Context.MODE_PRIVATE)

    // 1. Daily & Weekly Missions State
    private val _dailyMissions = MutableStateFlow<List<DailyMission>>(loadDefaultDailyMissions())
    val dailyMissions: StateFlow<List<DailyMission>> = _dailyMissions.asStateFlow()

    private val _weeklyMissions = MutableStateFlow<List<DailyMission>>(loadDefaultWeeklyMissions())
    val weeklyMissions: StateFlow<List<DailyMission>> = _weeklyMissions.asStateFlow()

    // 4. Private Room State
    private val _currentPrivateRoom = MutableStateFlow<PrivateRoomDetails?>(null)
    val currentPrivateRoom: StateFlow<PrivateRoomDetails?> = _currentPrivateRoom.asStateFlow()

    // 6. Cosmetics & Expanded Profile
    private val _cosmetics = MutableStateFlow<List<CosmeticItem>>(loadDefaultCosmetics())
    val cosmetics: StateFlow<List<CosmeticItem>> = _cosmetics.asStateFlow()

    private val _expandedProfile = MutableStateFlow<PlayerProfileExpanded>(loadDefaultExpandedProfile())
    val expandedProfile: StateFlow<PlayerProfileExpanded> = _expandedProfile.asStateFlow()

    init {
        // Essential initialization
    }

    // ==========================================
    // 1. MISSIONS ENGINE
    // ==========================================
    fun onMatchCompleted(isWin: Boolean, isOnline: Boolean, difficulty: String, numbersMarked: Int, durationSeconds: Int) {
        val updatedDaily = _dailyMissions.value.map { mission ->
            if (mission.isClaimed) return@map mission
            var newProgress = mission.currentProgress
            when (mission.id) {
                "DM_1" -> newProgress = (newProgress + 1).coerceAtMost(mission.targetProgress) // Play 1 match
                "DM_2" -> if (isWin) newProgress = (newProgress + 1).coerceAtMost(mission.targetProgress) // Win 1 match
                "DM_3" -> if (isWin) newProgress = (newProgress + 1).coerceAtMost(mission.targetProgress) // Complete Bingo
                "DM_4" -> if (isOnline) newProgress = (newProgress + 1).coerceAtMost(mission.targetProgress) // Play Online
            }
            mission.copy(currentProgress = newProgress)
        }
        _dailyMissions.value = updatedDaily

        val updatedWeekly = _weeklyMissions.value.map { mission ->
            if (mission.isClaimed) return@map mission
            var newProgress = mission.currentProgress
            when (mission.id) {
                "WM_1" -> if (isWin) newProgress = (newProgress + 1).coerceAtMost(mission.targetProgress) // Win 10 matches
                "WM_2" -> newProgress = (newProgress + 1).coerceAtMost(mission.targetProgress) // Play 25 matches
            }
            mission.copy(currentProgress = newProgress)
        }
        _weeklyMissions.value = updatedWeekly
    }

    fun claimMissionReward(missionId: String, isWeekly: Boolean): Boolean {
        val list = if (isWeekly) _weeklyMissions.value else _dailyMissions.value
        val mission = list.find { it.id == missionId } ?: return false

        if (mission.currentProgress >= mission.targetProgress && !mission.isClaimed) {
            val updatedList = list.map {
                if (it.id == missionId) it.copy(isClaimed = true) else it
            }
            if (isWeekly) _weeklyMissions.value = updatedList else _dailyMissions.value = updatedList

            // Add rewards
            progressionRepository.addRewardCoinsAndXp(mission.coinReward, mission.xpReward)
            return true
        }
        return false
    }

    // ==========================================
    // 3. PRIVATE ROOM ENGINE
    // ==========================================
    private var privateRoomListener: ValueEventListener? = null
    private var activeRoomCode: String? = null
    private var activeRoomHostUid: String? = null

    fun listenToPrivateRoom(code: String) {
        val cleanCode = code.uppercase().trim()
        if (cleanCode.isBlank()) return

        privateRoomListener?.let { listener ->
            activeRoomCode?.let { oldCode ->
                try {
                    database.child("private_rooms").child(oldCode).removeEventListener(listener)
                } catch (e: Exception) {
                    Log.e("LiveEventsRepo", "Error removing old listener: ${e.message}")
                }
            }
        }
        activeRoomCode = cleanCode
        privateRoomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (activeRoomCode != cleanCode) return
                if (!snapshot.exists()) {
                    _currentPrivateRoom.value = null
                    return
                }
                val room = snapshot.getValue(PrivateRoomDetails::class.java)
                if (room == null) {
                    _currentPrivateRoom.value = null
                    return
                }
                activeRoomHostUid = room.hostUid
                _currentPrivateRoom.value = room

                val currentUid = auth.currentUser?.uid ?: ""
                if (currentUid.isNotBlank() && currentUid == room.hostUid) {
                    try {
                        database.child("private_rooms").child(cleanCode).onDisconnect().removeValue()
                    } catch (e: Exception) {
                        Log.e("LiveEventsRepo", "Failed setting onDisconnect: ${e.message}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("private_rooms").child(cleanCode).addValueEventListener(privateRoomListener!!)
    }

    fun createPrivateRoom(maxPlayers: Int = 4): PrivateRoomDetails {
        val currentUser = auth.currentUser
        val hostName = currentUser?.displayName ?: "PlayWin Host"
        val hostUid = currentUser?.uid ?: "HOST_${System.currentTimeMillis()}"

        val code = generateRoomCode()
        activeRoomCode = code
        activeRoomHostUid = hostUid

        val room = PrivateRoomDetails(
            roomCode = code,
            hostUid = hostUid,
            hostName = hostName,
            maxPlayers = maxPlayers,
            currentPlayersCount = 1,
            isMatchStarted = false,
            players = listOf(
                PrivateRoomPlayer(
                    uid = hostUid,
                    displayName = hostName,
                    avatarUrl = "",
                    isHost = true,
                    isReady = true
                )
            )
        )
        _currentPrivateRoom.value = room

        try {
            val roomRef = database.child("private_rooms").child(code)
            roomRef.setValue(room)
            roomRef.onDisconnect().removeValue()
        } catch (e: Exception) {
            Log.e("LiveEventsRepo", "Firebase private room sync failed: ${e.message}")
        }

        listenToPrivateRoom(code)
        return room
    }

    fun joinPrivateRoomByCode(code: String, onComplete: (Boolean) -> Unit) {
        val cleanCode = code.uppercase().trim()
        if (cleanCode.length != 6) {
            onComplete(false)
            return
        }

        val currentUser = auth.currentUser
        val userUid = currentUser?.uid ?: "USER_${System.currentTimeMillis()}"
        val userName = currentUser?.displayName ?: "Guest Player"

        database.child("private_rooms").child(cleanCode).get().addOnSuccessListener { snapshot ->
            val room = snapshot.getValue(PrivateRoomDetails::class.java)
            if (room != null && !room.isMatchStarted && room.players.size < room.maxPlayers) {
                activeRoomCode = cleanCode
                activeRoomHostUid = room.hostUid

                if (room.players.any { it.uid == userUid }) {
                    listenToPrivateRoom(cleanCode)
                    onComplete(true)
                    return@addOnSuccessListener
                }

                val newPlayer = PrivateRoomPlayer(
                    uid = userUid,
                    displayName = userName,
                    avatarUrl = "",
                    isHost = false,
                    isReady = true
                )
                val updatedPlayers = room.players + newPlayer
                val updatedRoom = room.copy(
                    players = updatedPlayers,
                    currentPlayersCount = updatedPlayers.size
                )

                database.child("private_rooms").child(cleanCode).setValue(updatedRoom).addOnSuccessListener {
                    listenToPrivateRoom(cleanCode)
                    onComplete(true)
                }.addOnFailureListener {
                    onComplete(false)
                }
            } else {
                onComplete(false)
            }
        }.addOnFailureListener {
            onComplete(false)
        }
    }

    fun leavePrivateRoom(roomCodeOverride: String? = null) {
        val targetCode = (roomCodeOverride ?: activeRoomCode ?: _currentPrivateRoom.value?.roomCode ?: "").uppercase().trim()
        if (targetCode.isBlank()) return

        val userUid = auth.currentUser?.uid ?: ""
        val cachedHostUid = activeRoomHostUid ?: _currentPrivateRoom.value?.hostUid ?: ""

        if (targetCode == activeRoomCode || targetCode == _currentPrivateRoom.value?.roomCode) {
            activeRoomCode = null
            activeRoomHostUid = null
            _currentPrivateRoom.value = null
        }

        privateRoomListener?.let { listener ->
            try {
                database.child("private_rooms").child(targetCode).removeEventListener(listener)
            } catch (e: Exception) {
                Log.e("LiveEventsRepo", "Error removing listener for $targetCode: ${e.message}")
            }
        }
        privateRoomListener = null

        val roomRef = database.child("private_rooms").child(targetCode)
        try {
            roomRef.onDisconnect().cancel()
        } catch (e: Exception) {
            Log.e("LiveEventsRepo", "Error cancelling onDisconnect: ${e.message}")
        }

        roomRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) return@addOnSuccessListener

            val room = snapshot.getValue(PrivateRoomDetails::class.java)
            val currentHostUid = room?.hostUid ?: cachedHostUid
            val isHost = (userUid.isNotBlank() && userUid == currentHostUid)

            if (isHost || room?.isMatchStarted == true || room?.status == "playing" || room?.status == "completed" || currentHostUid.isBlank()) {
                roomRef.removeValue().addOnSuccessListener {
                    Log.d("LiveEventsRepo", "Private room $targetCode completely deleted from Firebase.")
                }.addOnFailureListener { e ->
                    Log.e("LiveEventsRepo", "Failed to remove private room $targetCode: ${e.message}")
                }
            } else {
                val currentPlayers = room?.players ?: emptyList()
                val updatedPlayers = currentPlayers.filter { it.uid != userUid }
                if (updatedPlayers.isEmpty()) {
                    roomRef.removeValue()
                } else {
                    val updates = mapOf(
                        "players" to updatedPlayers,
                        "currentPlayersCount" to updatedPlayers.size
                    )
                    roomRef.updateChildren(updates)
                }
            }
        }.addOnFailureListener {
            if (userUid == cachedHostUid || cachedHostUid.isBlank()) {
                roomRef.removeValue()
            }
        }
    }

    fun kickPlayerFromRoom(playerUid: String) {
        val current = _currentPrivateRoom.value ?: return
        val code = current.roomCode
        val updatedPlayers = current.players.filter { it.uid != playerUid }
        database.child("private_rooms").child(code).child("players").setValue(updatedPlayers)
        database.child("private_rooms").child(code).child("currentPlayersCount").setValue(updatedPlayers.size)
    }

    fun startPrivateCountdown() {
        val current = _currentPrivateRoom.value ?: return
        if (current.players.size < 2) return
        val code = current.roomCode
        val seed = kotlin.random.Random.nextLong()
        val startTime = System.currentTimeMillis()
        Log.d("BINGO_ONLINE", "START_PRIVATE_CLICKED")
        val updates = mapOf(
            "status" to "starting",
            "seed" to seed,
            "gameStartedAt" to startTime
        )
        database.child("private_rooms").child(code).updateChildren(updates).addOnSuccessListener {
            Log.d("BINGO_ONLINE", "ROOM_STATUS_UPDATED: status = starting")
            Log.d("BINGO_ONLINE", "COUNTDOWN_STARTED")
        }
    }

    private fun generateCardForSession(seed: Long): List<Int> {
        val random = kotlin.random.Random(seed)
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

    fun completePrivateStart() {
        val current = _currentPrivateRoom.value ?: return
        val code = current.roomCode
        android.util.Log.d("BINGO_ONLINE", "CREATING_GAME_SESSION")
        
        val gameId = "game_${code}_${System.currentTimeMillis()}"
        val playersList = current.players.map { it.uid }
        val seed = current.seed
        
        val boards = current.players.associate { player ->
            player.uid to generateCardForSession(seed + player.uid.hashCode())
        }
        
        val session = GameSession(
            gameId = gameId,
            roomId = code,
            players = playersList,
            bingoBoards = boards,
            calledNumbers = emptyList(),
            seed = seed,
            currentTurn = current.hostUid,
            gameState = "playing"
        )
        
        android.util.Log.d("BINGO_ONLINE", "GAME_SESSION_CREATED")
        
        val updates = mapOf(
            "status" to "playing",
            "isMatchStarted" to true,
            "gameSession" to session
        )
        
        database.child("private_rooms").child(code).updateChildren(updates).addOnSuccessListener {
            android.util.Log.d("BINGO_ONLINE", "GAME_SESSION_SAVED")
            android.util.Log.d("BINGO_ONLINE", "GAME_CREATED")
        }.addOnFailureListener { e ->
            android.util.Log.e("BINGO_ONLINE", "ERROR: Game Session Creation Failed: ${e.message}")
        }
    }

    fun cancelPrivateCountdown() {
        val current = _currentPrivateRoom.value ?: return
        val code = current.roomCode
        val updates = mapOf(
            "status" to "waiting",
            "isMatchStarted" to false,
            "seed" to 0L,
            "gameStartedAt" to 0L
        )
        database.child("private_rooms").child(code).updateChildren(updates)
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    // ==========================================
    // 5. COSMETICS & EXPANDED PROFILE ENGINE
    // ==========================================
    fun equipCosmeticItem(itemId: String): Boolean {
        val item = _cosmetics.value.find { it.id == itemId } ?: return false
        if (!item.isUnlocked) {
            val coins = progressionRepository.progression.value.currentCoins
            if (coins < item.priceCoins) return false

            // Purchase item
            progressionRepository.addRewardCoinsAndXp(-item.priceCoins, 100)
        }

        // Equip item in category
        val updated = _cosmetics.value.map {
            if (it.category == item.category) {
                it.copy(isEquipped = (it.id == itemId), isUnlocked = if (it.id == itemId) true else it.isUnlocked)
            } else it
        }
        _cosmetics.value = updated

        // Update profile frame / board skin if applicable
        val currentProfile = _expandedProfile.value
        if (item.category == CosmeticCategory.AVATAR_FRAME) {
            _expandedProfile.value = currentProfile.copy(equippedAvatarFrame = item.name)
        } else if (item.category == CosmeticCategory.BOARD_SKIN) {
            _expandedProfile.value = currentProfile.copy(equippedBoardSkin = item.name)
        }
        return true
    }

    // ==========================================
    // REMOTE SYNC & DEFAULTS
    // ==========================================
    private fun loadDefaultDailyMissions(): List<DailyMission> {
        return listOf(
            DailyMission(id = "DM_1", title = "Play 1 Match", description = "Play 1 match in any game mode", targetProgress = 1, currentProgress = 0, coinReward = 2, xpReward = 100),
            DailyMission(id = "DM_2", title = "Win 1 Match", description = "Win 1 match against AI or Online", targetProgress = 1, currentProgress = 0, coinReward = 3, xpReward = 150),
            DailyMission(id = "DM_3", title = "Complete Bingo", description = "Achieve a Bingo victory in a match", targetProgress = 1, currentProgress = 0, coinReward = 4, xpReward = 200),
            DailyMission(id = "DM_4", title = "Play Online", description = "Play 1 Multiplayer 1v1 Online match", targetProgress = 1, currentProgress = 0, coinReward = 5, xpReward = 250)
        )
    }

    private fun loadDefaultWeeklyMissions(): List<DailyMission> {
        return listOf(
            DailyMission(id = "WM_1", title = "Win 10 Matches", description = "Win 10 matches this week", targetProgress = 10, currentProgress = 0, coinReward = 15, xpReward = 1000, period = MissionPeriod.WEEKLY),
            DailyMission(id = "WM_2", title = "Play 25 Matches", description = "Play 25 matches this week", targetProgress = 25, currentProgress = 0, coinReward = 20, xpReward = 1500, period = MissionPeriod.WEEKLY)
        )
    }

    private fun loadDefaultCosmetics(): List<CosmeticItem> {
        return listOf(
            CosmeticItem(id = "FRAME_GOLD", name = "Golden Champion Glow", category = CosmeticCategory.AVATAR_FRAME, priceCoins = 30, isUnlocked = true, isEquipped = true, gradientColorsHex = listOf("#FFD700", "#FFA500")),
            CosmeticItem(id = "FRAME_NEON", name = "Cyber Neon Pulse", category = CosmeticCategory.AVATAR_FRAME, priceCoins = 30, isUnlocked = false, isEquipped = false, gradientColorsHex = listOf("#00F2FE", "#4FACFE")),
            CosmeticItem(id = "SKIN_ROYAL", name = "Royal Velvet Board", category = CosmeticCategory.BOARD_SKIN, priceCoins = 20, isUnlocked = true, isEquipped = true, gradientColorsHex = listOf("#4C1D95", "#6D28D9")),
            CosmeticItem(id = "SKIN_DIWALI", name = "Diwali Sparkle Board", category = CosmeticCategory.BOARD_SKIN, priceCoins = 20, isUnlocked = false, isEquipped = false, gradientColorsHex = listOf("#7C2D12", "#F59E0B")),
            CosmeticItem(id = "BALL_GOLD", name = "24K Gold Sphere Dauber", category = CosmeticCategory.BALL_DESIGN, priceCoins = 15, isUnlocked = false, isEquipped = false, gradientColorsHex = listOf("#F59E0B", "#D97706")),
            CosmeticItem(id = "BADGE_VIP", name = "PlayWin VIP Master Badge", category = CosmeticCategory.PROFILE_BADGE, priceCoins = 10, isUnlocked = true, isEquipped = true, gradientColorsHex = listOf("#EC4899", "#8B5CF6"))
        )
    }

    private fun loadDefaultExpandedProfile(): PlayerProfileExpanded {
        return PlayerProfileExpanded(
            uid = auth.currentUser?.uid ?: "PW_PLAYER_101",
            displayName = auth.currentUser?.displayName ?: "PlayWin Pro Gamer",
            countryFlagEmoji = "🇮🇳",
            equippedAvatarFrame = "Golden Champion Glow",
            equippedBoardSkin = "Royal Velvet Board",
            equippedBallDesign = "24K Gold Sphere Ball",
            equippedBadge = "PlayWin VIP Master Badge",
            seasonRankName = "Diamond League II",
            tournamentRankName = "#4 Regional Blitz",
            lifetimeWins = 48,
            totalMatches = 65,
            winRatePercent = 73.8f
        )
    }
}
