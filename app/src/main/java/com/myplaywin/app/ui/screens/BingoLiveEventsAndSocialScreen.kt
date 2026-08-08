package com.myplaywin.app.ui.screens

import com.myplaywin.app.ui.components.AaaGlossyButton
import androidx.compose.foundation.BorderStroke
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.data.model.*
import com.myplaywin.app.data.model.DailyMission
import com.myplaywin.app.data.repository.BingoLiveEventsAndSocialRepository
import com.myplaywin.app.ui.components.AaaBingoAudioHaptics
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoLiveEventsAndSocialScreen(
    repository: BingoLiveEventsAndSocialRepository,
    onStartPrivateMatch: (PrivateRoomDetails) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Event Center", "Daily Missions", "Tournaments", "Private Rooms", "Friends", "Cosmetics")

    val seasonalEvents by repository.activeSeasonalEvents.collectAsState()
    val dailyMissions by repository.dailyMissions.collectAsState()
    val weeklyMissions by repository.weeklyMissions.collectAsState()
    val tournaments by repository.tournaments.collectAsState()
    val privateRoom by repository.currentPrivateRoom.collectAsState()
    val friendsList by repository.friendsList.collectAsState()
    val friendRequests by repository.pendingFriendRequests.collectAsState()
    val cosmetics by repository.cosmetics.collectAsState()
    val profile by repository.expandedProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LIVE EVENTS & SOCIAL HUB",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Phase 10 Retention, Tournaments & Community",
                            fontSize = 11.sp,
                            color = Color(0xFFFFD700)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${profile.seasonRankName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1B2A)
                )
            )
        },
        containerColor = Color(0xFF0A0E17)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scrollable Tab Header
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF1B263B),
                contentColor = Color(0xFFFFD700),
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFFFFD700),
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            AaaBingoAudioHaptics.playClickSound()
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color(0xFFFFD700) else Color.LightGray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTabIndex) {
                0 -> SeasonalEventCenterSection(
                    events = seasonalEvents,
                    profile = profile
                )

                1 -> MissionsHubSection(
                    dailyMissions = dailyMissions,
                    weeklyMissions = weeklyMissions,
                    onClaimMission = { id, isWeekly ->
                        val success = repository.claimMissionReward(id, isWeekly)
                        if (success) {
                            AaaBingoAudioHaptics.playVictoryFanfare()
                            Toast.makeText(context, "Mission Reward Claimed!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                2 -> TournamentsHubSection(
                    tournaments = tournaments,
                    onRegister = { id ->
                        val success = repository.registerForTournament(id)
                        if (success) {
                            AaaBingoAudioHaptics.playVictoryFanfare()
                            Toast.makeText(context, "Successfully Registered for Tournament!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Insufficient Coins for Entry Fee!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                3 -> PrivateRoomsSection(
                    currentRoom = privateRoom,
                    onCreateRoom = { repository.createPrivateRoom(4) },
                    onJoinRoom = { code ->
                        val success = repository.joinPrivateRoomByCode(code)
                        if (success) {
                            Toast.makeText(context, "Joined Private Room $code!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Invalid Room Code or Room Full!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onLeaveRoom = { repository.leavePrivateRoom() },
                    onStartMatch = { room ->
                        onStartPrivateMatch(room)
                    },
                    onKickPlayer = { uid -> repository.kickPlayerFromRoom(uid) }
                )

                4 -> SocialFriendsSection(
                    friendsList = friendsList,
                    pendingRequests = friendRequests,
                    onSendRequest = { name ->
                        val success = repository.sendFriendRequest(name)
                        if (success) Toast.makeText(context, "Friend Request Sent to $name!", Toast.LENGTH_SHORT).show()
                    },
                    onAcceptRequest = { uid ->
                        repository.acceptFriendRequest(uid)
                        Toast.makeText(context, "Friend Request Accepted!", Toast.LENGTH_SHORT).show()
                    },
                    onRemoveFriend = { uid -> repository.removeFriend(uid) }
                )

                5 -> CosmeticsAndProfileSection(
                    cosmetics = cosmetics,
                    profile = profile,
                    onEquipItem = { id ->
                        val success = repository.equipCosmeticItem(id)
                        if (success) {
                            AaaBingoAudioHaptics.playVictoryFanfare()
                            Toast.makeText(context, "Cosmetic Item Unlocked & Equipped!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Insufficient Coins to unlock this item!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// 1. SEASONAL EVENT CENTER
// ==========================================
@Composable
private fun SeasonalEventCenterSection(
    events: List<SeasonalEvent>,
    profile: PlayerProfileExpanded
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = profile.countryFlagEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${profile.seasonRankName} • ${profile.tournamentRankName}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFF10B981), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "WIN RATE: ${profile.winRatePercent}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "ACTIVE SEASONAL LIVE EVENTS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
        }

        items(events) { event ->
            val gradient = Brush.linearGradient(
                colors = listOf(
                    Color(android.graphics.Color.parseColor(event.bannerGradientColorsHex.getOrElse(0) { "#4C1D95" })),
                    Color(android.graphics.Color.parseColor(event.bannerGradientColorsHex.getOrElse(1) { "#831843" })),
                    Color(android.graphics.Color.parseColor(event.bannerGradientColorsHex.getOrElse(2) { "#F59E0B" }))
                )
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradient)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "LIVE EVENT • 6 DAYS LEFT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = event.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = event.subtitle,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+${event.specialBonusCoins} Event Bonus",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = "Exclusive: ${event.exclusiveRewardTitle}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. DAILY & WEEKLY MISSIONS HUB
// ==========================================
@Composable
private fun MissionsHubSection(
    dailyMissions: List<DailyMission>,
    weeklyMissions: List<DailyMission>,
    onClaimMission: (String, Boolean) -> Unit
) {
    var selectedMissionTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedMissionTab,
            containerColor = Color(0xFF111827),
            contentColor = Color(0xFFFFD700)
        ) {
            Tab(
                selected = selectedMissionTab == 0,
                onClick = { selectedMissionTab = 0 },
                text = { Text("Daily Missions", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedMissionTab == 1,
                onClick = { selectedMissionTab = 1 },
                text = { Text("Weekly Objectives", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val currentList = if (selectedMissionTab == 0) dailyMissions else weeklyMissions

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(currentList) { mission ->
                val isCompleted = mission.currentProgress >= mission.targetProgress
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mission.isClaimed) Color(0xFF1E293B).copy(alpha = 0.6f) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mission.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mission.isClaimed) Color.Gray else Color.White
                                )
                                Text(
                                    text = mission.description,
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+${mission.coinReward} Coins",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+${mission.xpReward} XP",
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress bar & claim button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val targetProgressRatio = (mission.currentProgress.toFloat() / mission.targetProgress.toFloat()).coerceIn(0f, 1f)
                                val animatedProgress by animateFloatAsState(
                                    targetValue = targetProgressRatio,
                                    animationSpec = tween(600, easing = FastOutSlowInEasing),
                                    label = "MissionProgress"
                                )
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    color = Color(0xFF10B981),
                                    trackColor = Color(0xFF374151)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Progress",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "${mission.currentProgress} / ${mission.targetProgress}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            AaaGlossyButton(
                                onClick = { onClaimMission(mission.id, selectedMissionTab == 1) },
                                enabled = isCompleted && !mission.isClaimed,
                                modifier = Modifier.height(44.dp),
                                containerColor = if (mission.isClaimed) Color(0xFF334155) else Color(0xFFFFD700),
                                contentColor = if (mission.isClaimed) Color.Gray else Color.Black,
                                borderColor = Color(0xFFFFF59D)
                            ) {
                                Text(
                                    text = if (mission.isClaimed) "CLAIMED" else if (isCompleted) "CLAIM REWARD" else "IN PROGRESS",
                                    fontSize = 12.sp,
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
// 3. TOURNAMENTS HUB
// ==========================================
@Composable
private fun TournamentsHubSection(
    tournaments: List<TournamentInfo>,
    onRegister: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(tournaments) { tourn ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row: Title & Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = tourn.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = tourn.type.displayName,
                                    fontSize = 12.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal Details Bar: Prize Pool, Entry Cost & Time Remaining
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "PRIZE POOL", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(text = "🪙 ${tourn.prizePoolCoins}", fontSize = 13.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
                            }
                            Column {
                                Text(text = "ENTRY COST", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(text = "🪙 ${tourn.entryFeeCoins}", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Black)
                            }
                            Column {
                                Text(text = "TIME REMAINING", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(text = "⏱️ Live Now", fontSize = 13.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal Premium ENTER Button
                    AaaGlossyButton(
                        onClick = { onRegister(tourn.id) },
                        enabled = !tourn.isRegistered,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        containerColor = if (tourn.isRegistered) Color(0xFF334155) else Color(0xFF0284C7),
                        contentColor = Color.White,
                        borderColor = Color(0xFF38BDF8)
                    ) {
                        Text(
                            text = if (tourn.isRegistered) "REGISTERED" else "ENTER TOURNAMENT (${tourn.entryFeeCoins} COINS)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "LEADERBOARD RANKINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    tourn.leaderboard.take(4).forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${p.rank}  ${p.displayName}",
                                fontSize = 12.sp,
                                color = if (p.displayName.contains("You")) Color(0xFFFFD700) else Color.White,
                                fontWeight = if (p.displayName.contains("You")) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "${p.score} pts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. PRIVATE ROOMS
// ==========================================
@Composable
private fun PrivateRoomsSection(
    currentRoom: PrivateRoomDetails?,
    onCreateRoom: () -> Unit,
    onJoinRoom: (String) -> Unit,
    onLeaveRoom: () -> Unit,
    onStartMatch: (PrivateRoomDetails) -> Unit,
    onKickPlayer: (String) -> Unit
) {
    val context = LocalContext.current
    var joinCodeInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (currentRoom == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "CREATE A PRIVATE ROOM",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Generate a custom 6-digit room code to play Bingo with friends.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        AaaGlossyButton(
                            onClick = onCreateRoom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.Black,
                            borderColor = Color(0xFFA7F3D0)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "CREATE ROOM NOW", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "JOIN PRIVATE ROOM VIA CODE",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = { if (it.length <= 6) joinCodeInput = it.uppercase() },
                            label = { Text("Enter 6-Digit Room Code (e.g. PW83A1)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val isValidCode = joinCodeInput.trim().length == 6

                        AaaGlossyButton(
                            onClick = { onJoinRoom(joinCodeInput.trim()) },
                            enabled = isValidCode,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White,
                            borderColor = Color(0xFFA5B4FC)
                        ) {
                            Text(
                                text = if (isValidCode) "JOIN ROOM NOW" else "ENTER 6-DIGIT CODE TO JOIN",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ROOM CODE: ${currentRoom.roomCode}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    text = "Host: ${currentRoom.hostName} • Players (${currentRoom.players.size}/${currentRoom.maxPlayers})",
                                    fontSize = 12.sp,
                                    color = Color.LightGray
                                )
                            }

                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Room Code", currentRoom.roomCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Room Code Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "ROOM LOBBY PLAYERS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        currentRoom.players.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${p.displayName} ${if (p.isHost) "(HOST)" else ""}",
                                    fontSize = 14.sp,
                                    color = if (p.isHost) Color(0xFFFFD700) else Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                if (!p.isHost) {
                                    IconButton(onClick = { onKickPlayer(p.uid) }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kick", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AaaGlossyButton(
                                onClick = onLeaveRoom,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                containerColor = Color(0xFFDC2626),
                                contentColor = Color.White,
                                borderColor = Color(0xFFEF4444)
                            ) {
                                Text("Leave Room", fontWeight = FontWeight.Bold)
                            }

                            AaaGlossyButton(
                                onClick = { onStartMatch(currentRoom) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White,
                                borderColor = Color(0xFF34D399)
                            ) {
                                Text("Start Private Match", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SOCIAL FRIENDS
// ==========================================
@Composable
private fun SocialFriendsSection(
    friendsList: List<FriendProfile>,
    pendingRequests: List<FriendProfile>,
    onSendRequest: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRemoveFriend: (String) -> Unit
) {
    var searchInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        placeholder = { Text("Search player name...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AaaGlossyButton(
                        onClick = {
                            onSendRequest(searchInput)
                            searchInput = ""
                        },
                        modifier = Modifier.height(44.dp),
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    ) {
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (pendingRequests.isNotEmpty()) {
            item {
                Text(text = "PENDING FRIEND REQUESTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
            }
            items(pendingRequests) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF312E81)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = req.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Button(
                            onClick = { onAcceptRequest(req.uid) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Accept")
                        }
                    }
                }
            }
        }

        item {
            Text(text = "FRIENDS LIST (${friendsList.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
        }

        items(friendsList) { friend ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (friend.isOnline) Color.Green else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = friend.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = friend.statusText, fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    IconButton(onClick = { onRemoveFriend(friend.uid) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. COSMETICS & PROFILE
// ==========================================
@Composable
private fun CosmeticsAndProfileSection(
    cosmetics: List<CosmeticItem>,
    profile: PlayerProfileExpanded,
    onEquipItem: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cosmetics) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = item.category.title, fontSize = 11.sp, color = Color(0xFFFFD700))
                    }

                    AaaGlossyButton(
                        onClick = { onEquipItem(item.id) },
                        modifier = Modifier.height(38.dp),
                        containerColor = if (item.isEquipped) Color(0xFF10B981) else Color(0xFF6366F1),
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (item.isEquipped) "EQUIPPED" else if (item.isUnlocked) "EQUIP" else "UNLOCK (${item.priceCoins} C)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
