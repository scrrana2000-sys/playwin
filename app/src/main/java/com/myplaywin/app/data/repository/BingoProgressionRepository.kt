package com.myplaywin.app.data.repository

import android.content.Context
import android.content.SharedPreferences
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

/**
 * Phase 8: Central Progression, Economy & Leaderboard Repository
 */
class BingoProgressionRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("bingo_progression_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"

    private val _progression = MutableStateFlow(BingoPlayerProgression())
    val progression: StateFlow<BingoPlayerProgression> = _progression.asStateFlow()

    private val _stats = MutableStateFlow(BingoPlayerStats())
    val stats: StateFlow<BingoPlayerStats> = _stats.asStateFlow()

    private val _matchHistory = MutableStateFlow<List<BingoMatchHistoryRecord>>(emptyList())
    val matchHistory: StateFlow<List<BingoMatchHistoryRecord>> = _matchHistory.asStateFlow()

    private val _achievements = MutableStateFlow<List<BingoAchievement>>(emptyList())
    val achievements: StateFlow<List<BingoAchievement>> = _achievements.asStateFlow()

    private val _badges = MutableStateFlow<List<BingoBadge>>(emptyList())
    val badges: StateFlow<List<BingoBadge>> = _badges.asStateFlow()

    private val _leaderboardEntries = MutableStateFlow<List<BingoLeaderboardEntry>>(emptyList())
    val leaderboardEntries: StateFlow<List<BingoLeaderboardEntry>> = _leaderboardEntries.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<Int?>(null)
    val levelUpEvent: StateFlow<Int?> = _levelUpEvent.asStateFlow()

    private val _achievementUnlockedEvent = MutableStateFlow<BingoAchievement?>(null)
    val achievementUnlockedEvent: StateFlow<BingoAchievement?> = _achievementUnlockedEvent.asStateFlow()

    val currentUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "local_player_${prefs.getString("fallback_uid", "") ?: run {
            val id = UUID.randomUUID().toString().take(8)
            prefs.edit().putString("fallback_uid", id).apply()
            id
        }}"

    init {
        loadLocalProgression()
        initDefaultAchievements()
        initDefaultBadges()
        syncWithCloud()
    }

    private fun loadLocalProgression() {
        val level = prefs.getInt("level", 1)
        val totalXp = prefs.getInt("totalXp", 0)
        val currentXp = prefs.getInt("currentXp", 0)
        val coins = prefs.getInt("currentCoins", 500)
        val currentStreak = prefs.getInt("currentStreak", 0)
        val longestStreak = prefs.getInt("longestStreak", 0)
        val badge = prefs.getString("currentBadge", "Beginner") ?: "Beginner"
        val frame = prefs.getString("profileFrame", "FRAME_GOLD") ?: "FRAME_GOLD"
        val country = prefs.getString("country", "US") ?: "US"

        val reqXp = calculateRequiredXpForLevel(level)

        _progression.value = BingoPlayerProgression(
            playerUid = currentUid,
            displayName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Bingo Master",
            level = level,
            currentXp = currentXp,
            totalXp = totalXp,
            requiredXpNextLevel = reqXp,
            currentCoins = coins,
            currentWinStreak = currentStreak,
            longestWinStreak = longestStreak,
            currentBadgeTitle = badge,
            profileFrameId = frame,
            country = country
        )

        // Stats
        val totalMatches = prefs.getInt("stat_totalMatches", 0)
        val offlineMatches = prefs.getInt("stat_offlineMatches", 0)
        val onlineMatches = prefs.getInt("stat_onlineMatches", 0)
        val wins = prefs.getInt("stat_wins", 0)
        val losses = prefs.getInt("stat_losses", 0)
        val draws = prefs.getInt("stat_draws", 0)
        val fastestVic = prefs.getInt("stat_fastestVic", 999)
        val totalCoinsEarned = prefs.getInt("stat_totalCoinsEarned", 0)
        val totalXpEarned = prefs.getInt("stat_totalXpEarned", 0)

        val winRate = if (totalMatches > 0) (wins.toFloat() / totalMatches.toFloat()) * 100f else 0f

        _stats.value = BingoPlayerStats(
            playerUid = currentUid,
            totalMatches = totalMatches,
            offlineMatches = offlineMatches,
            onlineMatches = onlineMatches,
            totalWins = wins,
            totalLosses = losses,
            totalDraws = draws,
            winRatePercent = winRate,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            fastestVictorySec = fastestVic,
            totalCoinsEarned = totalCoinsEarned,
            totalXpEarned = totalXpEarned,
            highestLevel = level
        )
    }

    private fun initDefaultAchievements() {
        val list = listOf(
            BingoAchievement("ACH_1ST_WIN", "First Victory", "Win your first Bingo match", "🏆", 0, 1, false, 100, 150),
            BingoAchievement("ACH_10_WINS", "Bingo Veteran", "Win 10 Bingo matches", "🥉", 0, 10, false, 300, 400),
            BingoAchievement("ACH_50_WINS", "Bingo Master", "Win 50 Bingo matches", "🥈", 0, 50, false, 1000, 1500),
            BingoAchievement("ACH_100_WINS", "Bingo Legend", "Win 100 Bingo matches", "🥇", 0, 100, false, 2500, 3000),
            BingoAchievement("ACH_STREAK_3", "Hot Streak", "Achieve a 3-match winning streak", "🔥", 0, 3, false, 200, 250),
            BingoAchievement("ACH_STREAK_10", "Unstoppable", "Achieve a 10-match winning streak", "⚡", 0, 10, false, 1200, 1500),
            BingoAchievement("ACH_FAST_VIC", "Speed Demon", "Win a match in under 40 seconds", "⏱️", 0, 1, false, 250, 300),
            BingoAchievement("ACH_HARD_CHAMP", "Hardcore Champion", "Win a Hard difficulty match", "💀", 0, 1, false, 350, 400),
            BingoAchievement("ACH_ONLINE_PRO", "Online Gladiator", "Win an online 1v1 multiplayer match", "🌐", 0, 1, false, 400, 500),
            BingoAchievement("ACH_LEVEL_10", "High Roller", "Reach Level 10", "⭐", 0, 10, false, 500, 600),
            BingoAchievement("ACH_DAILY_HERO", "Dedicated Gamer", "Play 5 matches in a single day", "📅", 0, 5, false, 200, 250),
            BingoAchievement("ACH_COIN_KING", "Coin Hoarder", "Earn a total of 5,000 Coins", "💰", 0, 5000, false, 500, 500)
        )

        val updated = list.map { ach ->
            val isUnlocked = prefs.getBoolean("ach_unlocked_${ach.id}", false)
            val prog = prefs.getInt("ach_prog_${ach.id}", 0)
            ach.copy(isUnlocked = isUnlocked, currentProgress = if (isUnlocked) ach.maxProgress else prog)
        }
        _achievements.value = updated
    }

    private fun initDefaultBadges() {
        val list = listOf(
            BingoBadge("BADGE_BEGINNER", "Beginner", "Awarded upon starting your Bingo journey", "🌱", "COMMON", true),
            BingoBadge("BADGE_EXPERT", "Expert", "Awarded after reaching Level 5", "🎯", "RARE", false),
            BingoBadge("BADGE_MASTER", "Master", "Awarded after winning 25 matches", "👑", "EPIC", false),
            BingoBadge("BADGE_LEGEND", "Legend", "Awarded after reaching Level 15", "🌟", "LEGENDARY", false),
            BingoBadge("BADGE_CHAMPION", "Champion", "Awarded for a 10-match win streak", "🏆", "EPIC", false),
            BingoBadge("BADGE_ELITE", "Elite", "Awarded for 100 Total Victories", "💎", "LEGENDARY", false)
        )

        val updated = list.map { b ->
            val unlocked = prefs.getBoolean("badge_unlocked_${b.id}", b.isUnlocked)
            b.copy(isUnlocked = unlocked)
        }
        _badges.value = updated
    }

    /**
     * Centralized Match Completion Logic:
     * Calculates coins, XP, handles wallet updates atomically, level ups, streaks, and stats.
     */
    fun processMatchResult(
        matchType: String, // "OFFLINE", "ONLINE_1V1"
        difficulty: String, // "EASY", "MEDIUM", "HARD", "RANKED"
        opponentName: String,
        result: String, // "VICTORY", "DEFEAT", "DRAW"
        durationSeconds: Int,
        numbersCalledCount: Int,
        coinRewardOverride: Int? = null
    ) {
        val isWin = result == "VICTORY"
        val isLoss = result == "DEFEAT"
        val isDraw = result == "DRAW"

        // 1. Calculate Base Coin & XP Rewards
        var coinReward = coinRewardOverride ?: when {
            matchType == "ONLINE_1V1" && isWin -> 12
            matchType == "ONLINE_1V1" && (isLoss || isDraw) -> 3
            difficulty == "HARD" && isWin -> 10
            difficulty == "MEDIUM" && isWin -> 7
            difficulty == "EASY" && isWin -> 5
            else -> 1 // Offline lose / draw
        }

        var xpReward = when {
            matchType == "ONLINE_1V1" && isWin -> 300
            matchType == "ONLINE_1V1" && isLoss -> 50
            difficulty == "HARD" && isWin -> 180
            difficulty == "MEDIUM" && isWin -> 100
            difficulty == "EASY" && isWin -> 50
            else -> 25
        }

        // Streak Multiplier
        val currentStreak = if (isWin) _progression.value.currentWinStreak + 1 else 0
        if (isWin && currentStreak > 1) {
            val bonusPercent = (currentStreak - 1).coerceAtMost(5) * 15 // Up to +75% XP
            xpReward += (xpReward * (bonusPercent / 100f)).toInt()
        }

        // Speed Bonus (Keep XP bonus only, no extra coins to respect new economy)
        if (isWin && durationSeconds < 40) {
            xpReward += 50
        }

        // 2. Perform Atomic Wallet Transaction using existing WalletService
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (userId.isNotEmpty()) {
            WalletService.updateWallet(
                userId = userId,
                coinsDelta = coinReward,
                source = "Bingo Match Result ($result)",
                type = "GAME_REWARD"
            ) { success, _, newBalance, _ ->
                if (success) {
                    _progression.value = _progression.value.copy(currentCoins = newBalance)
                    prefs.edit().putInt("currentCoins", newBalance).apply()
                }
            }
        } else {
            // Local fallback
            val newCoins = _progression.value.currentCoins + coinReward
            _progression.value = _progression.value.copy(currentCoins = newCoins)
            prefs.edit().putInt("currentCoins", newCoins).apply()
        }

        // 3. Update Progression & XP
        val oldLevel = _progression.value.level
        val oldTotalXp = _progression.value.totalXp
        val newTotalXp = oldTotalXp + xpReward

        var calcLevel = oldLevel
        var reqXp = calculateRequiredXpForLevel(calcLevel)
        var calcCurrentXp = _progression.value.currentXp + xpReward

        while (calcCurrentXp >= reqXp) {
            calcCurrentXp -= reqXp
            calcLevel++
            reqXp = calculateRequiredXpForLevel(calcLevel)

            // Trigger Level-Up Celebration & Bonus Coins
            _levelUpEvent.value = calcLevel
            val levelUpBonusCoins = calcLevel * 100
            _progression.value = _progression.value.copy(
                currentCoins = _progression.value.currentCoins + levelUpBonusCoins
            )
        }

        val newLongestStreak = max(_progression.value.longestWinStreak, currentStreak)

        _progression.value = _progression.value.copy(
            level = calcLevel,
            totalXp = newTotalXp,
            currentXp = calcCurrentXp,
            requiredXpNextLevel = reqXp,
            currentWinStreak = currentStreak,
            longestWinStreak = newLongestStreak
        )

        // Save local progression
        prefs.edit()
            .putInt("level", calcLevel)
            .putInt("totalXp", newTotalXp)
            .putInt("currentXp", calcCurrentXp)
            .putInt("currentStreak", currentStreak)
            .putInt("longestStreak", newLongestStreak)
            .apply()

        // 4. Update Stats
        val curStats = _stats.value
        val newTotalMatches = curStats.totalMatches + 1
        val newOfflineMatches = curStats.offlineMatches + (if (matchType == "OFFLINE") 1 else 0)
        val newOnlineMatches = curStats.onlineMatches + (if (matchType == "ONLINE_1V1") 1 else 0)
        val newWins = curStats.totalWins + (if (isWin) 1 else 0)
        val newLosses = curStats.totalLosses + (if (isLoss) 1 else 0)
        val newDraws = curStats.totalDraws + (if (isDraw) 1 else 0)
        val newFastestVic = if (isWin) kotlin.math.min(curStats.fastestVictorySec, durationSeconds) else curStats.fastestVictorySec
        val newCoinsEarned = curStats.totalCoinsEarned + coinReward
        val newXpEarned = curStats.totalXpEarned + xpReward
        val newWinRate = (newWins.toFloat() / newTotalMatches.toFloat()) * 100f

        _stats.value = curStats.copy(
            totalMatches = newTotalMatches,
            offlineMatches = newOfflineMatches,
            onlineMatches = newOnlineMatches,
            totalWins = newWins,
            totalLosses = newLosses,
            totalDraws = newDraws,
            winRatePercent = newWinRate,
            currentStreak = currentStreak,
            longestStreak = newLongestStreak,
            fastestVictorySec = newFastestVic,
            totalCoinsEarned = newCoinsEarned,
            totalXpEarned = newXpEarned,
            highestLevel = max(curStats.highestLevel, calcLevel)
        )

        prefs.edit()
            .putInt("stat_totalMatches", newTotalMatches)
            .putInt("stat_offlineMatches", newOfflineMatches)
            .putInt("stat_onlineMatches", newOnlineMatches)
            .putInt("stat_wins", newWins)
            .putInt("stat_losses", newLosses)
            .putInt("stat_draws", newDraws)
            .putInt("stat_fastestVic", newFastestVic)
            .putInt("stat_totalCoinsEarned", newCoinsEarned)
            .putInt("stat_totalXpEarned", newXpEarned)
            .apply()

        // 5. Add Match History Record
        val record = BingoMatchHistoryRecord(
            id = UUID.randomUUID().toString().take(10),
            matchType = matchType,
            difficulty = difficulty,
            opponentName = opponentName,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            result = result,
            coinsEarned = coinReward,
            xpEarned = xpReward,
            numbersCalledCount = numbersCalledCount
        )
        val newHistory = (listOf(record) + _matchHistory.value).take(50)
        _matchHistory.value = newHistory

        // 6. Check & Unlock Achievements
        evaluateAchievements(isWin, matchType, difficulty, durationSeconds, newWins, currentStreak, calcLevel, newCoinsEarned)

        // 7. Check & Unlock Badges
        evaluateBadges(calcLevel, newWins, currentStreak)

        // 8. Cloud Sync
        pushProgressionToCloud()
    }

    private fun evaluateAchievements(
        isWin: Boolean,
        matchType: String,
        difficulty: String,
        durationSeconds: Int,
        totalWins: Int,
        currentStreak: Int,
        level: Int,
        totalCoinsEarned: Int
    ) {
        val list = _achievements.value.toMutableList()
        var newlyUnlocked: BingoAchievement? = null

        list.forEachIndexed { index, ach ->
            if (!ach.isUnlocked) {
                var newProg = ach.currentProgress
                var shouldUnlock = false

                when (ach.id) {
                    "ACH_1ST_WIN" -> if (isWin) shouldUnlock = true
                    "ACH_10_WINS" -> { newProg = totalWins; if (totalWins >= 10) shouldUnlock = true }
                    "ACH_50_WINS" -> { newProg = totalWins; if (totalWins >= 50) shouldUnlock = true }
                    "ACH_100_WINS" -> { newProg = totalWins; if (totalWins >= 100) shouldUnlock = true }
                    "ACH_STREAK_3" -> { newProg = currentStreak; if (currentStreak >= 3) shouldUnlock = true }
                    "ACH_STREAK_10" -> { newProg = currentStreak; if (currentStreak >= 10) shouldUnlock = true }
                    "ACH_FAST_VIC" -> if (isWin && durationSeconds < 40) shouldUnlock = true
                    "ACH_HARD_CHAMP" -> if (isWin && difficulty == "HARD") shouldUnlock = true
                    "ACH_ONLINE_PRO" -> if (isWin && matchType == "ONLINE_1V1") shouldUnlock = true
                    "ACH_LEVEL_10" -> { newProg = level; if (level >= 10) shouldUnlock = true }
                    "ACH_COIN_KING" -> { newProg = totalCoinsEarned; if (totalCoinsEarned >= 5000) shouldUnlock = true }
                }

                if (shouldUnlock) {
                    val unlockedAch = ach.copy(isUnlocked = true, currentProgress = ach.maxProgress)
                    list[index] = unlockedAch
                    prefs.edit().putBoolean("ach_unlocked_${ach.id}", true).apply()
                    newlyUnlocked = unlockedAch

                    // Grant reward coins
                    _progression.value = _progression.value.copy(
                        currentCoins = _progression.value.currentCoins + ach.rewardCoins
                    )
                } else if (newProg != ach.currentProgress) {
                    list[index] = ach.copy(currentProgress = newProg)
                    prefs.edit().putInt("ach_prog_${ach.id}", newProg).apply()
                }
            }
        }

        _achievements.value = list
        if (newlyUnlocked != null) {
            _achievementUnlockedEvent.value = newlyUnlocked
        }
    }

    private fun evaluateBadges(level: Int, totalWins: Int, currentStreak: Int) {
        val list = _badges.value.toMutableList()
        list.forEachIndexed { index, badge ->
            var unlock = badge.isUnlocked
            when (badge.id) {
                "BADGE_EXPERT" -> if (level >= 5) unlock = true
                "BADGE_MASTER" -> if (totalWins >= 25) unlock = true
                "BADGE_LEGEND" -> if (level >= 15) unlock = true
                "BADGE_CHAMPION" -> if (currentStreak >= 10) unlock = true
                "BADGE_ELITE" -> if (totalWins >= 100) unlock = true
            }

            if (unlock && !badge.isUnlocked) {
                list[index] = badge.copy(isUnlocked = true, unlockedTimestamp = System.currentTimeMillis())
                prefs.edit().putBoolean("badge_unlocked_${badge.id}", true).apply()
            }
        }
        _badges.value = list
    }

    fun getDailyLoginStreak(): Int {
        val lastDate = prefs.getString("lastDailyBonusDate", "") ?: ""
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (lastDate.isEmpty()) return 1
        
        val streak = prefs.getInt("bingoDailyLoginStreak", 1)
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val last = sdf.parse(lastDate)
            val today = sdf.parse(todayStr)
            val diff = today.time - last.time
            val diffDays = diff / (24 * 60 * 60 * 1000)
            if (diffDays > 1) {
                return 1
            } else if (diffDays == 1L) {
                return streak
            } else {
                return streak
            }
        } catch (e: Exception) {
            return 1
        }
    }

    fun getDailyLoginCoinsForDay(day: Int): Int {
        return when (day) {
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> 6
            6 -> 8
            7 -> 10
            else -> 2
        }
    }

    fun claimDailyBonus(doubleReward: Boolean = false): Boolean {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val lastDate = prefs.getString("lastDailyBonusDate", "") ?: ""
        if (lastDate == todayStr) {
            return false // Already claimed
        }

        var streak = prefs.getInt("bingoDailyLoginStreak", 1)
        if (lastDate.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val last = sdf.parse(lastDate)
                val today = sdf.parse(todayStr)
                val diffDays = (today.time - last.time) / (24 * 60 * 60 * 1000)
                if (diffDays > 1) {
                    streak = 1
                } else if (diffDays == 1L) {
                    streak = if (streak >= 7) 1 else streak + 1
                }
            } catch (e: Exception) {
                streak = 1
            }
        } else {
            streak = 1
        }

        val baseCoins = getDailyLoginCoinsForDay(streak)
        val finalCoins = if (doubleReward) baseCoins * 2 else baseCoins
        val bonusXp = 100

        val newCoins = _progression.value.currentCoins + finalCoins
        _progression.value = _progression.value.copy(
            currentCoins = newCoins,
            lastDailyBonusDate = todayStr
        )
        prefs.edit()
            .putString("lastDailyBonusDate", todayStr)
            .putInt("bingoDailyLoginStreak", streak)
            .putInt("currentCoins", newCoins)
            .apply()

        // Also issue atomic wallet update if authenticated
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (uid.isNotEmpty()) {
            WalletService.updateWallet(uid, finalCoins, "Bingo Daily Login Day $streak", "BONUS") { _, _, _, _ -> }
        }
        return true
    }

    fun clearEvents() {
        _levelUpEvent.value = null
        _achievementUnlockedEvent.value = null
    }

    fun loadLeaderboard(category: LeaderboardCategory, sortBy: LeaderboardSortBy) {
        val database = FirebaseDatabase.getInstance(dbUrl)
        val ref = database.getReference("bingo_leaderboards").child("global")

        ref.limitToLast(50).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BingoLeaderboardEntry>()
                for (child in snapshot.children) {
                    val entry = child.getValue(BingoLeaderboardEntry::class.java)
                    if (entry != null) {
                        list.add(entry)
                    }
                }

                if (list.isEmpty()) {
                    // Populate high quality realistic competitive entries if cloud database is fresh
                    list.addAll(generateFallbackLeaderboard(category))
                }

                // Apply Sorting
                val sorted = when (sortBy) {
                    LeaderboardSortBy.LEVEL -> list.sortedByDescending { it.level }
                    LeaderboardSortBy.WINS -> list.sortedByDescending { it.totalWins }
                    LeaderboardSortBy.XP -> list.sortedByDescending { it.totalXp }
                    LeaderboardSortBy.WIN_RATE -> list.sortedByDescending { it.winRatePercent }
                    LeaderboardSortBy.STREAK -> list.sortedByDescending { it.streak }
                }.mapIndexed { idx, item -> item.copy(rank = idx + 1) }

                _leaderboardEntries.value = sorted
            }

            override fun onCancelled(error: DatabaseError) {
                _leaderboardEntries.value = generateFallbackLeaderboard(category)
            }
        })
    }

    private fun generateFallbackLeaderboard(category: LeaderboardCategory): List<BingoLeaderboardEntry> {
        val names = listOf(
            "BingoKing_99", "PlayWinGamer", "CyberDauber", "ApexWinner", "VegasMaster",
            "LuckyStrike", "RoyalFlush", "GoldDigger", "BingoPro_IN", "NeonDaub",
            "JackpotQueen", "MatrixPlayer", "CasinoPro", "SuperStriker", "VictorySeeker"
        )
        return names.mapIndexed { idx, name ->
            BingoLeaderboardEntry(
                rank = idx + 1,
                playerUid = "bot_$idx",
                displayName = name,
                level = 30 - idx,
                totalWins = 180 - (idx * 10),
                totalXp = (30 - idx) * 500,
                winRatePercent = 85f - (idx * 2f),
                streak = (12 - idx).coerceAtLeast(0),
                country = if (idx % 2 == 0) "IN" else "US",
                badgeTitle = if (idx < 3) "Legend" else "Pro"
            )
        }
    }

    /**
     * Directly adds or deducts coins and XP (e.g. for Mission rewards, Event bonuses, Cosmetic purchases).
     */
    fun addRewardCoinsAndXp(coinsDelta: Int, xpDelta: Int) {
        val current = _progression.value
        val newCoins = (current.currentCoins + coinsDelta).coerceAtLeast(0)
        var newXp = current.currentXp + xpDelta
        var newTotalXp = current.totalXp + xpDelta
        var newLevel = current.level
        var reqXp = current.requiredXpNextLevel

        while (newXp >= reqXp) {
            newXp -= reqXp
            newLevel++
            reqXp = calculateRequiredXpForLevel(newLevel)
            _levelUpEvent.value = newLevel
        }

        val updatedProgression = current.copy(
            currentCoins = newCoins,
            currentXp = newXp,
            totalXp = newTotalXp,
            level = newLevel,
            requiredXpNextLevel = reqXp
        )
        _progression.value = updatedProgression

        prefs.edit()
            .putInt("currentCoins", newCoins)
            .putInt("currentXp", newXp)
            .putInt("totalXp", newTotalXp)
            .putInt("level", newLevel)
            .apply()

        pushProgressionToCloud()
    }

    private fun pushProgressionToCloud() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance(dbUrl)

        val profileRef = database.getReference("bingo_profiles").child(uid)
        profileRef.setValue(_progression.value)

        val leaderboardEntry = BingoLeaderboardEntry(
            playerUid = uid,
            displayName = _progression.value.displayName,
            level = _progression.value.level,
            totalWins = _stats.value.totalWins,
            totalXp = _progression.value.totalXp,
            winRatePercent = _stats.value.winRatePercent,
            streak = _progression.value.currentWinStreak,
            country = _progression.value.country,
            badgeTitle = _progression.value.currentBadgeTitle
        )
        database.getReference("bingo_leaderboards").child("global").child(uid).setValue(leaderboardEntry)
    }

    private fun syncWithCloud() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance(dbUrl)

        database.getReference("bingo_profiles").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cloudProfile = snapshot.getValue(BingoPlayerProgression::class.java)
                if (cloudProfile != null && cloudProfile.totalXp > _progression.value.totalXp) {
                    _progression.value = cloudProfile
                    prefs.edit()
                        .putInt("level", cloudProfile.level)
                        .putInt("totalXp", cloudProfile.totalXp)
                        .putInt("currentXp", cloudProfile.currentXp)
                        .apply()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun calculateRequiredXpForLevel(level: Int): Int {
        return 100 * level + 50
    }
}
