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
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.myplaywin.app.data.voice.BingoVoiceChatManager
import com.myplaywin.app.data.voice.VoiceConnectionState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.myplaywin.app.ui.components.AaaBingoAudioHaptics
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoLiveEventsAndSocialScreen(
    repository: BingoLiveEventsAndSocialRepository,
    onStartPrivateMatch: (PrivateRoomDetails) -> Unit,
    onBack: () -> Unit,
    initialTabIndex: Int = 0
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    val tabs = listOf("Daily Missions", "Private Rooms", "Cosmetics")

    val dailyMissions by repository.dailyMissions.collectAsState()
    val weeklyMissions by repository.weeklyMissions.collectAsState()
    val privateRoom by repository.currentPrivateRoom.collectAsState()
    val cosmetics by repository.cosmetics.collectAsState()
    val profile by repository.expandedProfile.collectAsState()

    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(privateRoom) {
        val room = privateRoom
        if (room != null && room.status == "playing" && room.gameSession != null) {
            android.util.Log.d("BINGO_ONLINE", "GAME_SESSION_RECEIVED")
            android.util.Log.d("BINGO_ONLINE", "OPENING_MULTIPLAYER_SCREEN")
            onStartPrivateMatch(room)
        }
    }

    LaunchedEffect(privateRoom?.status) {
        val room = privateRoom ?: return@LaunchedEffect
        if (room.status == "starting") {
            val isHost = room.hostUid == currentUserId
            if (isHost) {
                // Host handles the countdown delay of 3 seconds
                kotlinx.coroutines.delay(3000)
                val latestRoom = repository.currentPrivateRoom.value
                if (latestRoom != null && latestRoom.status == "starting" && latestRoom.players.size >= 2) {
                    android.util.Log.d("BINGO_ONLINE", "COUNTDOWN_COMPLETE")
                    repository.completePrivateStart()
                } else {
                    repository.cancelPrivateCountdown()
                }
            }
        }
    }

    LaunchedEffect(privateRoom?.players?.size, privateRoom?.status) {
        val room = privateRoom ?: return@LaunchedEffect
        if (room.status == "starting" && room.players.size < 2) {
            val isHost = room.hostUid == currentUserId
            if (isHost) {
                repository.cancelPrivateCountdown()
            }
        }
    }

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
                            text = "Missions, Private Rooms & Customizations",
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
                0 -> MissionsHubSection(
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

                1 -> PrivateRoomsSection(
                    currentRoom = privateRoom,
                    onCreateRoom = { repository.createPrivateRoom(4) },
                    onJoinRoom = { code ->
                        repository.joinPrivateRoomByCode(code) { success ->
                            if (success) {
                                Toast.makeText(context, "Joined Private Room $code!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Invalid Room Code or Room Full!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onLeaveRoom = { repository.leavePrivateRoom() },
                    onStartMatch = { room ->
                        repository.startPrivateCountdown()
                    },
                    onKickPlayer = { uid -> repository.kickPlayerFromRoom(uid) }
                )

                2 -> CosmeticsAndProfileSection(
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
            itemsIndexed(currentList) { index, mission ->
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

                if ((index + 1) % 4 == 0) {
                    com.playwin.ads.NativeManager.NativeAd(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
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

    val voiceConnectionState by BingoVoiceChatManager.connectionState.collectAsState()
    val isVoiceMuted by BingoVoiceChatManager.isMuted.collectAsState()
    val voiceStatusMsg by BingoVoiceChatManager.statusMessage.collectAsState()
    val speakingPlayers by BingoVoiceChatManager.speakingPlayers.collectAsState()
    val roomParticipants by BingoVoiceChatManager.roomParticipants.collectAsState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val code = currentRoom?.roomCode ?: ""
            if (code.isNotBlank()) {
                BingoVoiceChatManager.joinVoiceRoom(context, code)
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice chat.", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            BingoVoiceChatManager.leaveVoiceRoom(context)
        }
    }

    LaunchedEffect(currentRoom?.roomCode) {
        val roomCode = currentRoom?.roomCode
        if (!roomCode.isNullOrBlank()) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                BingoVoiceChatManager.joinVoiceRoom(context, roomCode)
            } else {
                micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        } else {
            BingoVoiceChatManager.leaveVoiceRoom(context)
        }
    }

    val handleLeaveRoom = {
        BingoVoiceChatManager.leaveVoiceRoom(context)
        onLeaveRoom()
    }

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

                        // Compact Voice Chat Bar
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (voiceConnectionState) {
                                                    VoiceConnectionState.CONNECTED -> if (isVoiceMuted) Color(0xFFEF4444) else Color(0xFF10B981)
                                                    VoiceConnectionState.CONNECTING -> Color(0xFFF59E0B)
                                                    else -> Color(0xFF475569)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val hasPermission = ContextCompat.checkSelfPermission(
                                                    context,
                                                    android.Manifest.permission.RECORD_AUDIO
                                                ) == PackageManager.PERMISSION_GRANTED

                                                if (!hasPermission) {
                                                    micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                                } else if (voiceConnectionState == VoiceConnectionState.CONNECTED) {
                                                    BingoVoiceChatManager.toggleMute()
                                                } else {
                                                    BingoVoiceChatManager.joinVoiceRoom(context, currentRoom.roomCode)
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                imageVector = if (isVoiceMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                                contentDescription = "Microphone",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = when (voiceConnectionState) {
                                                VoiceConnectionState.CONNECTED -> if (isVoiceMuted) "Mic Muted (Tap to Unmute)" else "Live Voice Chat Active"
                                                VoiceConnectionState.CONNECTING -> "Connecting Voice..."
                                                VoiceConnectionState.ERROR -> voiceStatusMsg.ifEmpty { "Voice Error" }
                                                VoiceConnectionState.DISCONNECTED -> "Tap Mic to Join Voice"
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (voiceConnectionState == VoiceConnectionState.CONNECTED) {
                                                if (speakingPlayers.isNotEmpty()) "${speakingPlayers.size} player(s) speaking" else "Room channel connected"
                                            } else "WebRTC encrypted private room voice",
                                            fontSize = 10.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                if (voiceConnectionState == VoiceConnectionState.CONNECTING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color(0xFFFFD700),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }

                        if (currentRoom.status == "starting") {
                            var secsLeft by remember { mutableStateOf(3) }
                            LaunchedEffect(currentRoom.gameStartedAt) {
                                while (true) {
                                    val elapsed = System.currentTimeMillis() - currentRoom.gameStartedAt
                                    val rem = 3 - (elapsed / 1000).toInt()
                                    secsLeft = rem.coerceIn(0, 3)
                                    if (rem <= 0) {
                                        break
                                    }
                                    kotlinx.coroutines.delay(100)
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "GAME STARTING IN",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (secsLeft > 0) secsLeft.toString() else "GO!",
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AaaGlossyButton(
                                    onClick = handleLeaveRoom,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White,
                                    borderColor = Color(0xFFEF4444)
                                ) {
                                    Text("Leave Room", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "ROOM LOBBY PLAYERS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))

                            currentRoom.players.forEach { p ->
                                val isSpeaking = speakingPlayers.contains(p.uid)
                                val pParticipant = roomParticipants[p.uid]
                                val isMuted = pParticipant?.isMuted == true

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSpeaking) Color(0xFF10B981).copy(alpha = 0.25f) else Color.Transparent)
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSpeaking) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = "Speaking",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        } else if (isMuted) {
                                            Icon(
                                                imageVector = Icons.Default.MicOff,
                                                contentDescription = "Muted",
                                                tint = Color.Red.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = "Mic On",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        Text(
                                            text = "${p.displayName} ${if (p.isHost) "(HOST)" else ""}",
                                            fontSize = 14.sp,
                                            color = if (isSpeaking) Color(0xFF34D399) else if (p.isHost) Color(0xFFFFD700) else Color.White,
                                            fontWeight = if (isSpeaking || p.isHost) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }

                                    if (!p.isHost) {
                                        IconButton(onClick = { onKickPlayer(p.uid) }) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Kick", tint = Color.Red, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }

                            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            val isUserHost = currentRoom.hostUid == currentUserId || currentRoom.players.firstOrNull { it.uid == currentUserId }?.isHost == true
                            val canStart = currentRoom.players.size >= 2

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AaaGlossyButton(
                                    onClick = handleLeaveRoom,
                                    modifier = Modifier
                                        .weight(if (isUserHost) 1f else 2f)
                                        .height(44.dp),
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White,
                                    borderColor = Color(0xFFEF4444)
                                ) {
                                    Text("Leave Room", fontWeight = FontWeight.Bold)
                                }

                                if (isUserHost) {
                                    val isStarting = currentRoom.status == "starting" || currentRoom.isMatchStarted
                                    AaaGlossyButton(
                                        onClick = { onStartMatch(currentRoom) },
                                        enabled = canStart && !isStarting,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        containerColor = Color(0xFF10B981),
                                        contentColor = Color.White,
                                        borderColor = Color(0xFF34D399)
                                    ) {
                                        Text(if (isStarting) "Starting..." else "Start Private Match", fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
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
