package com.myplaywin.app.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.data.model.*
import com.myplaywin.app.data.repository.BingoProgressionRepository
import com.myplaywin.app.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Phase 8: Player Progression, Economy, Statistics & Global Leaderboards Hub
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoProgressionScreen(
    repository: BingoProgressionRepository,
    onBack: () -> Unit
) {
    val progression by repository.progression.collectAsState()
    val stats by repository.stats.collectAsState()
    val achievements by repository.achievements.collectAsState()
    val badges by repository.badges.collectAsState()
    val matchHistory by repository.matchHistory.collectAsState()
    val leaderboardEntries by repository.leaderboardEntries.collectAsState()

    val levelUpEvent by repository.levelUpEvent.collectAsState()
    val achievementEvent by repository.achievementUnlockedEvent.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("PROFILE", "LEADERBOARD", "ACHIEVEMENTS", "HISTORY", "BADGES")

    var selectedCategory by remember { mutableStateOf(LeaderboardCategory.GLOBAL) }
    var selectedSortBy by remember { mutableStateOf(LeaderboardSortBy.LEVEL) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(selectedCategory, selectedSortBy) {
        repository.loadLeaderboard(selectedCategory, selectedSortBy)
    }

    Scaffold(
        bottomBar = {
            com.playwin.ads.BannerManager.BannerAd(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0D0B18))
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "PLAYER PROGRESSION",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = Color(0xFF7C4DFF).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE040FB))
                        ) {
                            Text(
                                text = "Lvl ${progression.level}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        AaaBingoAudioHaptics.playClickSound()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Balance Badge
                    Surface(
                        color = Color(0xFF2D1654),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🪙", fontSize = 14.sp)
                            Text(
                                text = "${progression.currentCoins}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
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
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color(0xFF130427),
                    contentColor = Color(0xFFFFD700),
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 3.dp,
                            color = Color(0xFFFFD700)
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                AaaBingoAudioHaptics.playClickSound()
                                selectedTabIndex = index
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (selectedTabIndex == index) Color(0xFFFFD700) else Color.LightGray
                                )
                            }
                        )
                    }
                }

                // 2. Tab Contents
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(12.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> ProfileTabContent(progression, stats, repository)
                        1 -> LeaderboardTabContent(
                            entries = leaderboardEntries,
                            category = selectedCategory,
                            sortBy = selectedSortBy,
                            userUid = progression.playerUid,
                            onCategorySelect = { selectedCategory = it },
                            onSortSelect = { selectedSortBy = it }
                        )
                        2 -> AchievementsTabContent(achievements)
                        3 -> HistoryTabContent(matchHistory, searchQuery) { searchQuery = it }
                        4 -> BadgesTabContent(badges)
                    }
                }
            }

            // LEVEL UP CELEBRATION MODAL
            if (levelUpEvent != null) {
                AaaVictoryVfxCanvas()
                AlertDialog(
                    onDismissRequest = { repository.clearEvents() },
                    containerColor = Color(0xFF1B0C33),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👑 LEVEL UP!", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 26.sp)
                            Text(text = "REACHED LEVEL ${levelUpEvent}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Congratulations! You earned a Level Up Bonus:", color = Color.LightGray)
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFF3F2B75),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "🪙", fontSize = 22.sp)
                                    Text(
                                        text = "+${(levelUpEvent ?: 1) * 100} Coins",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        AaaGlossyButton(
                            onClick = { repository.clearEvents() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("COLLECT REWARD", fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                )
            }

            // ACHIEVEMENT UNLOCKED POPUP
            if (achievementEvent != null) {
                val ach = achievementEvent!!
                AaaVictoryVfxCanvas()
                AlertDialog(
                    onDismissRequest = { repository.clearEvents() },
                    containerColor = Color(0xFF1B0C33),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = ach.emoji, fontSize = 28.sp)
                            Column {
                                Text(text = "ACHIEVEMENT UNLOCKED!", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 16.sp)
                                Text(text = ach.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    },
                    text = {
                        Text(text = "${ach.description}\n\nReward: +${ach.rewardCoins} Coins & +${ach.rewardXp} XP!", color = Color.LightGray)
                    },
                    confirmButton = {
                        Button(
                            onClick = { repository.clearEvents() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Text("AWESOME", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileTabContent(
    progression: BingoPlayerProgression,
    stats: BingoPlayerStats,
    repository: BingoProgressionRepository
) {
    val context = LocalContext.current
    var dailyBonusClaimedMsg by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. MAIN PROFILE CARD
        item {
            AaaGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = Color(0xFFFFD700),
                glowColor = Color(0xFF7C4DFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Avatar Frame
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3F2B75))
                            .border(2.5.dp, Color(0xFFFFD700), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👑", fontSize = 36.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = progression.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Title: ${progression.currentBadgeTitle} • Region: ${progression.country}",
                            color = Color(0xFF80D8FF),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // XP Bar
                        Text(
                            text = "XP: ${progression.currentXp} / ${progression.requiredXpNextLevel} (Total: ${progression.totalXp})",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val progressRatio = (progression.currentXp.toFloat() / progression.requiredXpNextLevel.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF00E5FF),
                            trackColor = Color(0xFF1E0A3C)
                        )
                    }
                }
            }
        }

        // 2. DAILY BONUS CLAIM CARD
        item {
            val isBonusClaimable = progression.lastDailyBonusDate != SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val currentDay = repository.getDailyLoginStreak()
            val rewardCoins = repository.getDailyLoginCoinsForDay(currentDay)

            AaaGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isBonusClaimable) Color(0xFF00E676) else Color(0xFF7C4DFF)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "🎁", fontSize = 32.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DAILY LOGIN REWARD (DAY $currentDay)",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isBonusClaimable) "+$rewardCoins Coins available now!" else "Claimed today! Come back tomorrow.",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (isBonusClaimable) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AaaGlossyButton(
                                onClick = {
                                    if (repository.claimDailyBonus(doubleReward = false)) {
                                        AaaBingoAudioHaptics.playVictoryFanfare()
                                        dailyBonusClaimedMsg = "Claimed +$rewardCoins Coins!"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                containerColor = Color(0xFF334155),
                                contentColor = Color.White,
                                borderColor = Color(0xFF64748B)
                            ) {
                                Text("CLAIM REWARD (+$rewardCoins COINS)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

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
                                            rewardType = com.playwin.ads.RewardType.BINGO_DAILY_LOGIN_DOUBLE,
                                            callbacks = object : com.playwin.ads.RewardCallback {
                                                override fun onRewardEarned(rewardType: com.playwin.ads.RewardType, amount: Int, token: String) {
                                                    if (repository.claimDailyBonus(doubleReward = true)) {
                                                        AaaBingoAudioHaptics.playVictoryFanfare()
                                                        dailyBonusClaimedMsg = "Claimed +${rewardCoins * 2} Coins!"
                                                    }
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
                                Text("DOUBLE REWARD (+$(rewardCoins * 2) COINS)", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFF1E3A2B),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✅ REWARD CLAIMED FOR TODAY",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. DETAILED CAREER STATISTICS GRID
        item {
            Text(
                text = "CAREER STATISTICS",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Total Matches", "${stats.totalMatches}", "🎮", Modifier.weight(1f))
                    StatCard("Total Wins", "${stats.totalWins}", "🏆", Modifier.weight(1f))
                    StatCard("Win Rate", "${"%.1f".format(stats.winRatePercent)}%", "📊", Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Current Streak", "${stats.currentStreak} 🔥", "⚡", Modifier.weight(1f))
                    StatCard("Longest Streak", "${stats.longestStreak} 👑", "⭐", Modifier.weight(1f))
                    StatCard("Fastest Victory", if (stats.fastestVictorySec < 999) "${stats.fastestVictorySec}s" else "N/A", "⏱️", Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Coins Earned", "${stats.totalCoinsEarned}", "🪙", Modifier.weight(1f))
                    StatCard("Total XP", "${stats.totalXpEarned}", "✨", Modifier.weight(1f))
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, emoji: String, modifier: Modifier) {
    AaaGlassCard(
        modifier = modifier.height(84.dp),
        borderColor = Color(0xFF7C4DFF),
        cornerRadius = 16.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                color = Color.LightGray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LeaderboardTabContent(
    entries: List<BingoLeaderboardEntry>,
    category: LeaderboardCategory,
    sortBy: LeaderboardSortBy,
    userUid: String,
    onCategorySelect: (LeaderboardCategory) -> Unit,
    onSortSelect: (LeaderboardSortBy) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Categories Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LeaderboardCategory.values().forEach { cat ->
                val isSelected = cat == category
                Surface(
                    color = if (isSelected) Color(0xFFFFD700) else Color(0xFF2C1548),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCategorySelect(cat) }
                ) {
                    Text(
                        text = cat.name,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        // Leaderboard List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry ->
                val isMe = entry.playerUid == userUid
                val rankBorder = when (entry.rank) {
                    1 -> Color(0xFFFFD700) // Gold
                    2 -> Color(0xFFE0E0E0) // Silver
                    3 -> Color(0xFFCD7F32) // Bronze
                    else -> if (isMe) Color(0xFF00E5FF) else Color(0xFF3F2B75)
                }

                AaaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = rankBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "#${entry.rank}",
                                color = rankBorder,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(text = if (entry.rank == 1) "🥇" else if (entry.rank == 2) "🥈" else if (entry.rank == 3) "🥉" else "👤", fontSize = 20.sp)

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = entry.displayName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (isMe) {
                                        Text(text = "(YOU)", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Text(
                                    text = "Lvl ${entry.level} • Wins: ${entry.totalWins} • Streak: ${entry.streak}🔥",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "${entry.totalXp} XP",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementsTabContent(achievements: List<BingoAchievement>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(achievements) { ach ->
            AaaGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (ach.isUnlocked) Color(0xFFFFD700) else Color(0xFF3F2B75)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = ach.emoji, fontSize = 28.sp)

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = ach.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (ach.isUnlocked) "✅ UNLOCKED" else "${ach.currentProgress}/${ach.maxProgress}",
                                color = if (ach.isUnlocked) Color(0xFF00E676) else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = ach.description,
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val progressRatio = (ach.currentProgress.toFloat() / ach.maxProgress.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (ach.isUnlocked) Color(0xFFFFD700) else Color(0xFF7C4DFF),
                            trackColor = Color(0xFF1E0A3C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTabContent(
    history: List<BingoMatchHistoryRecord>,
    query: String,
    onQueryChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search match history...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFF7C4DFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        val filtered = history.filter {
            it.opponentName.contains(query, ignoreCase = true) ||
                    it.result.contains(query, ignoreCase = true) ||
                    it.difficulty.contains(query, ignoreCase = true)
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No match history found.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { item ->
                    val isWin = item.result == "VICTORY"
                    val color = if (isWin) Color(0xFF00E676) else Color(0xFFFF1744)

                    AaaGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = color
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${item.result} vs ${item.opponentName}",
                                    color = color,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Mode: ${item.matchType} (${item.difficulty}) • Duration: ${item.durationSeconds}s",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+${item.coinsEarned} 🪙",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "+${item.xpEarned} XP",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgesTabContent(badges: List<BingoBadge>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(badges) { badge ->
            val rarityColor = when (badge.rarity) {
                "LEGENDARY" -> Color(0xFFFFD700)
                "EPIC" -> Color(0xFFE040FB)
                "RARE" -> Color(0xFF00E5FF)
                else -> Color.White
            }

            AaaGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (badge.isUnlocked) rarityColor else Color.DarkGray
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = badge.emoji,
                        fontSize = 32.sp,
                        modifier = Modifier.graphicsLayer(alpha = if (badge.isUnlocked) 1f else 0.3f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = badge.name,
                        color = if (badge.isUnlocked) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = badge.rarity,
                        color = rarityColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = badge.description,
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
