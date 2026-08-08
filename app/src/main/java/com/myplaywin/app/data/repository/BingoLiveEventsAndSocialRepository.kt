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
    private val database = FirebaseDatabase.getInstance().reference
    private val prefs = context.getSharedPreferences("bingo_social_liveops_prefs", Context.MODE_PRIVATE)

    // 1. Daily & Weekly Missions State
    private val _dailyMissions = MutableStateFlow<List<DailyMission>>(loadDefaultDailyMissions())
    val dailyMissions: StateFlow<List<DailyMission>> = _dailyMissions.asStateFlow()

    private val _weeklyMissions = MutableStateFlow<List<DailyMission>>(loadDefaultWeeklyMissions())
    val weeklyMissions: StateFlow<List<DailyMission>> = _weeklyMissions.asStateFlow()

    // 2. Seasonal Events State
    private val _activeSeasonalEvents = MutableStateFlow<List<SeasonalEvent>>(loadDefaultSeasonalEvents())
    val activeSeasonalEvents: StateFlow<List<SeasonalEvent>> = _activeSeasonalEvents.asStateFlow()

    // 3. Tournaments State
    private val _tournaments = MutableStateFlow<List<TournamentInfo>>(loadDefaultTournaments())
    val tournaments: StateFlow<List<TournamentInfo>> = _tournaments.asStateFlow()

    // 4. Private Room State
    private val _currentPrivateRoom = MutableStateFlow<PrivateRoomDetails?>(null)
    val currentPrivateRoom: StateFlow<PrivateRoomDetails?> = _currentPrivateRoom.asStateFlow()

    // 5. Friends State
    private val _friendsList = MutableStateFlow<List<FriendProfile>>(loadDefaultFriendsList())
    val friendsList: StateFlow<List<FriendProfile>> = _friendsList.asStateFlow()

    private val _pendingFriendRequests = MutableStateFlow<List<FriendProfile>>(loadDefaultFriendRequests())
    val pendingFriendRequests: StateFlow<List<FriendProfile>> = _pendingFriendRequests.asStateFlow()

    // 6. Cosmetics & Expanded Profile
    private val _cosmetics = MutableStateFlow<List<CosmeticItem>>(loadDefaultCosmetics())
    val cosmetics: StateFlow<List<CosmeticItem>> = _cosmetics.asStateFlow()

    private val _expandedProfile = MutableStateFlow<PlayerProfileExpanded>(loadDefaultExpandedProfile())
    val expandedProfile: StateFlow<PlayerProfileExpanded> = _expandedProfile.asStateFlow()

    init {
        listenToRemoteMissionsAndEvents()
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
                "DM_2" -> if (isWin) newProgress = (newProgress + 1).coerceAtMost(mission.targetProgress) // Win 3 matches
                "DM_3" -> if (isWin && isOnline) newProgress = (newProgress + 1).coerceAtMost(mission.targetProgress) // Win online match
                "DM_4" -> if (isWin && difficulty.uppercase() == "HARD") newProgress = (newProgress + 1).coerceAtMost(mission.targetProgress)
                "DM_5" -> newProgress = (newProgress + numbersMarked).coerceAtMost(mission.targetProgress) // Mark 50 numbers
            }
            mission.copy(currentProgress = newProgress)
        }
        _dailyMissions.value = updatedDaily

        val updatedWeekly = _weeklyMissions.value.map { mission ->
            if (mission.isClaimed) return@map mission
            var newProgress = mission.currentProgress
            when (mission.id) {
                "WM_1" -> if (isWin) newProgress = (newProgress + 1).coerceAtMost(mission.targetProgress) // Win 20 matches
                "WM_2" -> newProgress = (newProgress + (if (isWin) 250 else 100)).coerceAtMost(mission.targetProgress) // Earn XP
                "WM_3" -> if (isOnline) newProgress = (newProgress + 1).coerceAtMost(mission.targetProgress) // Play 30 online games
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
    // 2. TOURNAMENTS ENGINE
    // ==========================================
    fun registerForTournament(tournamentId: String): Boolean {
        val tournament = _tournaments.value.find { it.id == tournamentId } ?: return false
        if (tournament.isRegistered) return true

        val currentCoins = progressionRepository.progression.value.currentCoins
        if (currentCoins < tournament.entryFeeCoins) return false

        // Deduct entry fee and register
        progressionRepository.addRewardCoinsAndXp(-tournament.entryFeeCoins, 50)

        val updated = _tournaments.value.map {
            if (it.id == tournamentId) {
                it.copy(
                    isRegistered = true,
                    registeredCount = it.registeredCount + 1,
                    userRank = (1..10).random(),
                    userScore = 500
                )
            } else it
        }
        _tournaments.value = updated
        return true
    }

    // ==========================================
    // 3. PRIVATE ROOM ENGINE
    // ==========================================
    fun createPrivateRoom(maxPlayers: Int = 4): PrivateRoomDetails {
        val currentUser = auth.currentUser
        val hostName = currentUser?.displayName ?: "PlayWin Host"
        val hostUid = currentUser?.uid ?: "HOST_${System.currentTimeMillis()}"

        val code = generateRoomCode()
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
            database.child("private_rooms").child(code).setValue(room)
        } catch (e: Exception) {
            Log.e("LiveEventsRepo", "Firebase private room sync failed: ${e.message}")
        }

        return room
    }

    fun joinPrivateRoomByCode(code: String): Boolean {
        val cleanCode = code.uppercase().trim()
        if (cleanCode.length != 6) return false

        val currentUser = auth.currentUser
        val userUid = currentUser?.uid ?: "USER_${System.currentTimeMillis()}"
        val userName = currentUser?.displayName ?: "Guest Player"

        val current = _currentPrivateRoom.value
        val existingPlayers = current?.players ?: listOf(
            PrivateRoomPlayer(uid = "HOST_101", displayName = "Room Host", isHost = true)
        )

        if (existingPlayers.any { it.uid == userUid }) return true
        if (existingPlayers.size >= 4) return false

        val newPlayer = PrivateRoomPlayer(uid = userUid, displayName = userName, isHost = false, isReady = true)
        val updatedPlayers = existingPlayers + newPlayer

        val updatedRoom = PrivateRoomDetails(
            roomCode = cleanCode,
            hostUid = current?.hostUid ?: "HOST_101",
            hostName = current?.hostName ?: "Room Host",
            maxPlayers = 4,
            currentPlayersCount = updatedPlayers.size,
            isMatchStarted = false,
            players = updatedPlayers
        )
        _currentPrivateRoom.value = updatedRoom
        return true
    }

    fun leavePrivateRoom() {
        _currentPrivateRoom.value = null
    }

    fun kickPlayerFromRoom(playerUid: String) {
        val current = _currentPrivateRoom.value ?: return
        val updatedPlayers = current.players.filter { it.uid != playerUid }
        _currentPrivateRoom.value = current.copy(
            players = updatedPlayers,
            currentPlayersCount = updatedPlayers.size
        )
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    // ==========================================
    // 4. FRIEND SYSTEM ENGINE
    // ==========================================
    fun sendFriendRequest(displayName: String): Boolean {
        if (displayName.isBlank()) return false
        val newFriend = FriendProfile(
            uid = "FRIEND_${System.currentTimeMillis()}",
            displayName = displayName,
            isOnline = true,
            statusText = "Playing Bingo",
            totalWins = (10..50).random(),
            level = (3..15).random()
        )
        _friendsList.value = listOf(newFriend) + _friendsList.value
        return true
    }

    fun acceptFriendRequest(uid: String) {
        val request = _pendingFriendRequests.value.find { it.uid == uid } ?: return
        _pendingFriendRequests.value = _pendingFriendRequests.value.filter { it.uid != uid }
        _friendsList.value = listOf(request.copy(isOnline = true)) + _friendsList.value
    }

    fun removeFriend(uid: String) {
        _friendsList.value = _friendsList.value.filter { it.uid != uid }
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
    private fun listenToRemoteMissionsAndEvents() {
        database.child("seasonal_events").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val eventsList = mutableListOf<SeasonalEvent>()
                    for (child in snapshot.children) {
                        val event = child.getValue(SeasonalEvent::class.java)
                        if (event != null) eventsList.add(event)
                    }
                    if (eventsList.isNotEmpty()) {
                        _activeSeasonalEvents.value = eventsList
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Keep default seasonal events
            }
        })
    }

    private fun loadDefaultDailyMissions(): List<DailyMission> {
        return listOf(
            DailyMission(id = "DM_1", title = "Daily Check-in Match", description = "Play 1 match in any game mode", targetProgress = 1, currentProgress = 0, coinReward = 150, xpReward = 100),
            DailyMission(id = "DM_2", title = "Bingo Conqueror", description = "Win 3 matches against AI or Online", targetProgress = 3, currentProgress = 1, coinReward = 300, xpReward = 250),
            DailyMission(id = "DM_3", title = "Online Showdown", description = "Win 1 Multiplayer 1v1 Online match", targetProgress = 1, currentProgress = 0, coinReward = 400, xpReward = 300),
            DailyMission(id = "DM_4", title = "Master Strategist", description = "Win 1 match on Hard AI difficulty", targetProgress = 1, currentProgress = 0, coinReward = 350, xpReward = 200),
            DailyMission(id = "DM_5", title = "Number Hunter", description = "Mark 50 Bingo numbers across matches", targetProgress = 50, currentProgress = 18, coinReward = 250, xpReward = 150)
        )
    }

    private fun loadDefaultWeeklyMissions(): List<DailyMission> {
        return listOf(
            DailyMission(id = "WM_1", title = "Weekly Grand Champion", description = "Win 20 matches this week", targetProgress = 20, currentProgress = 6, coinReward = 2000, xpReward = 1500, period = MissionPeriod.WEEKLY),
            DailyMission(id = "WM_2", title = "Experience Collector", description = "Accumulate 5000 XP in total", targetProgress = 5000, currentProgress = 1850, coinReward = 1500, xpReward = 1000, period = MissionPeriod.WEEKLY),
            DailyMission(id = "WM_3", title = "Multiplayer Warrior", description = "Play 30 Online Multiplayer games", targetProgress = 30, currentProgress = 12, coinReward = 2500, xpReward = 2000, period = MissionPeriod.WEEKLY)
        )
    }

    private fun loadDefaultSeasonalEvents(): List<SeasonalEvent> {
        return listOf(
            SeasonalEvent(
                id = "EVT_DIWALI_2026",
                title = "Festival of Lights Special",
                subtitle = "Collect Golden Lamp Badges & 2x XP Multiplier!",
                themeKey = "DIWALI",
                bannerGradientColorsHex = listOf("#7C2D12", "#C2410C", "#F59E0B"),
                specialBonusCoins = 1000,
                exclusiveRewardTitle = "Golden Sparkle Avatar Frame"
            ),
            SeasonalEvent(
                id = "EVT_GLOBAL_CHAMP_2026",
                title = "Global PlayWin Championship",
                subtitle = "Compete for 50,000 Coin Prize Pool & Diamond Trophy",
                themeKey = "GLOBAL",
                bannerGradientColorsHex = listOf("#1E1B4B", "#312E81", "#6366F1"),
                specialBonusCoins = 2500,
                exclusiveRewardTitle = "Global Champion Crest"
            )
        )
    }

    private fun loadDefaultTournaments(): List<TournamentInfo> {
        return listOf(
            TournamentInfo(
                id = "TOURN_HOURLY_1",
                title = "Hourly Blitz Cup",
                type = TournamentType.HOURLY,
                entryFeeCoins = 100,
                prizePoolCoins = 2500,
                registeredCount = 68,
                userRank = 4,
                userScore = 890,
                leaderboard = listOf(
                    TournamentParticipant(rank = 1, displayName = "Aarav_Pro", score = 1420, matchesWon = 5),
                    TournamentParticipant(rank = 2, displayName = "Priya_Bingo", score = 1280, matchesWon = 4),
                    TournamentParticipant(rank = 3, displayName = "Rahul_King", score = 1050, matchesWon = 4),
                    TournamentParticipant(rank = 4, displayName = "You (PlayWin)", score = 890, matchesWon = 3),
                    TournamentParticipant(rank = 5, displayName = "Vikram_Win", score = 760, matchesWon = 2)
                )
            ),
            TournamentInfo(
                id = "TOURN_DAILY_MASTER",
                title = "Daily Master League",
                type = TournamentType.DAILY,
                entryFeeCoins = 500,
                prizePoolCoins = 15000,
                registeredCount = 184,
                userRank = 12,
                userScore = 2150,
                leaderboard = listOf(
                    TournamentParticipant(rank = 1, displayName = "Shadow_Legend", score = 4800, matchesWon = 16),
                    TournamentParticipant(rank = 2, displayName = "Queen_Of_Bingo", score = 4250, matchesWon = 14),
                    TournamentParticipant(rank = 3, displayName = "Dev_Star", score = 3900, matchesWon = 12)
                )
            )
        )
    }

    private fun loadDefaultFriendsList(): List<FriendProfile> {
        return listOf(
            FriendProfile(uid = "FR_1", displayName = "Rohan Sharma", isOnline = true, statusText = "In Online Lobby", totalWins = 38, level = 8, isFavorite = true),
            FriendProfile(uid = "FR_2", displayName = "Ananya Roy", isOnline = true, statusText = "Playing Hard AI", totalWins = 52, level = 12, isFavorite = true),
            FriendProfile(uid = "FR_3", displayName = "Kabir Singh", isOnline = false, statusText = "Offline 2h ago", totalWins = 19, level = 4)
        )
    }

    private fun loadDefaultFriendRequests(): List<FriendProfile> {
        return listOf(
            FriendProfile(uid = "REQ_1", displayName = "Siddharth V.", isOnline = true, statusText = "Wants to add you", totalWins = 29, level = 6)
        )
    }

    private fun loadDefaultCosmetics(): List<CosmeticItem> {
        return listOf(
            CosmeticItem(id = "FRAME_GOLD", name = "Golden Champion Glow", category = CosmeticCategory.AVATAR_FRAME, priceCoins = 500, isUnlocked = true, isEquipped = true, gradientColorsHex = listOf("#FFD700", "#FFA500")),
            CosmeticItem(id = "FRAME_NEON", name = "Cyber Neon Pulse", category = CosmeticCategory.AVATAR_FRAME, priceCoins = 1200, isUnlocked = false, isEquipped = false, gradientColorsHex = listOf("#00F2FE", "#4FACFE")),
            CosmeticItem(id = "SKIN_ROYAL", name = "Royal Velvet Board", category = CosmeticCategory.BOARD_SKIN, priceCoins = 800, isUnlocked = true, isEquipped = true, gradientColorsHex = listOf("#4C1D95", "#6D28D9")),
            CosmeticItem(id = "SKIN_DIWALI", name = "Diwali Sparkle Board", category = CosmeticCategory.BOARD_SKIN, priceCoins = 1500, isUnlocked = false, isEquipped = false, gradientColorsHex = listOf("#7C2D12", "#F59E0B")),
            CosmeticItem(id = "BALL_GOLD", name = "24K Gold Sphere Ball", category = CosmeticCategory.BALL_DESIGN, priceCoins = 600, isUnlocked = true, isEquipped = true, gradientColorsHex = listOf("#F59E0B", "#D97706")),
            CosmeticItem(id = "BADGE_VIP", name = "PlayWin VIP Master Badge", category = CosmeticCategory.PROFILE_BADGE, priceCoins = 1000, isUnlocked = true, isEquipped = true, gradientColorsHex = listOf("#EC4899", "#8B5CF6"))
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
