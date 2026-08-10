package com.myplaywin.app.ui.screens

import com.myplaywin.app.ui.components.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * AAA-Quality Bingo Home Screen (Phase 2)
 * Complete implementation featuring:
 * - Layered Purple+Blue premium gradient canvas with ambient light rays & particles
 * - Top Bar with Back, Title with Gold Glow, and Help/How To Play icon
 * - Player Glass Panel with Avatar, Level, XP progress, Coins, and Online status
 * - Offline Game Mode card (Robot mascot, features, Difficulty selection flow)
 * - Online Game Mode card (World mascot, features, Live Matchmaking flow)
 * - Future Features / Coming Soon glass card
 * - Bottom Action Bar (History, Leaderboard, Statistics, Settings)
 * - Complete popups & dialogs for Phase 2 interactivity
 */
sealed class BingoDialogType {
    object HowToPlay : BingoDialogType()
    object OfflineDifficulty : BingoDialogType()
    object OnlineMatchmaking : BingoDialogType()
    object History : BingoDialogType()
    object Leaderboard : BingoDialogType()
    object Statistics : BingoDialogType()
    object Settings : BingoDialogType()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoHomeScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var activeDialog by remember { mutableStateOf<BingoDialogType?>(null) }
    var activeDifficultyForMatch by remember { mutableStateOf<String?>(null) }
    var isOnlineLobbyActive by remember { mutableStateOf(false) }
    var isOnlineGameplayActive by remember { mutableStateOf(false) }
    var isProgressionActive by remember { mutableStateOf(false) }
    var isLiveOpsActive by remember { mutableStateOf(false) }
    var isSocialEventsActive by remember { mutableStateOf(false) }
    var initialSocialTabIndex by remember { mutableStateOf(0) }

    val scrollState = rememberScrollState()
    val multiplayerEngine = remember { com.myplaywin.app.data.repository.BingoMultiplayerEngine(context) }
    val progressionRepository = remember { com.myplaywin.app.data.repository.BingoProgressionRepository(context) }
    val liveOpsRepository = remember { com.myplaywin.app.data.repository.BingoLiveOpsRepository(context) }
    val securityEngine = remember { com.myplaywin.app.data.repository.BingoSecurityAndAntiCheatEngine(context) }
    val liveEventsRepository = remember { com.myplaywin.app.data.repository.BingoLiveEventsAndSocialRepository(context, progressionRepository) }

    val liveOpsConfig by liveOpsRepository.liveOpsConfig.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            multiplayerEngine.destroyEngine()
        }
    }

    val isAnySubScreenActive = isSocialEventsActive || isLiveOpsActive || isProgressionActive || isOnlineLobbyActive
    androidx.activity.compose.BackHandler(enabled = isAnySubScreenActive) {
        when {
            isSocialEventsActive -> isSocialEventsActive = false
            isLiveOpsActive -> isLiveOpsActive = false
            isProgressionActive -> isProgressionActive = false
            isOnlineLobbyActive -> isOnlineLobbyActive = false
        }
    }

    if (isSocialEventsActive) {
        BingoLiveEventsAndSocialScreen(
            repository = liveEventsRepository,
            onStartPrivateMatch = { room ->
                isSocialEventsActive = false
                val myPredefinedCard = room.gameSession?.bingoBoards?.get(multiplayerEngine.localPlayerUid)
                multiplayerEngine.joinPrivateRoomFromSocial(room.roomCode, room.seed, room.hostUid == multiplayerEngine.localPlayerUid, myPredefinedCard) {
                    isOnlineGameplayActive = true
                }
            },
            onBack = { isSocialEventsActive = false },
            initialTabIndex = initialSocialTabIndex
        )
        return
    }

    if (isLiveOpsActive) {
        BingoLiveOpsScreen(
            liveOpsRepository = liveOpsRepository,
            securityEngine = securityEngine,
            progressionRepository = progressionRepository,
            onBack = { isLiveOpsActive = false }
        )
        return
    }

    if (isProgressionActive) {
        BingoProgressionScreen(
            repository = progressionRepository,
            onBack = { isProgressionActive = false }
        )
        return
    }

    if (isOnlineGameplayActive) {
        BingoOnlineGameplayScreen(
            engine = multiplayerEngine,
            onBackToLobby = {
                isOnlineGameplayActive = false
                isOnlineLobbyActive = true
            }
        )
        return
    }

    if (isOnlineLobbyActive) {
        BingoOnlineLobbyScreen(
            engine = multiplayerEngine,
            onBackClick = { isOnlineLobbyActive = false },
            onStartOnlineGameplay = {
                isOnlineLobbyActive = false
                isOnlineGameplayActive = true
            }
        )
        return
    }

    if (activeDifficultyForMatch != null) {
        BingoGamePlayScreen(
            onExitGame = { activeDifficultyForMatch = null },
            difficulty = activeDifficultyForMatch!!
        )
        return
    }

    // 60 FPS Ambient Animations
    val infiniteTransition = rememberInfiniteTransition(label = "BingoHomePhase2")

    // Glow pulse alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    // Gentle floating offset
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatOffset"
    )

    // Ray rotation angle
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RayRotation"
    )

    Scaffold(
        containerColor = Color(0xFF0D0B18),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BINGO",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 2.sp,
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(
                                    color = Color(0xFFFFD700).copy(alpha = 0.8f * glowAlpha),
                                    blurRadius = 14f
                                )
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to PlayWin Home",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        initialSocialTabIndex = 0
                        isSocialEventsActive = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Live Events & Social",
                            tint = Color(0xFFFFD700)
                        )
                    }
                    IconButton(onClick = { isLiveOpsActive = true }) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "LiveOps & Security",
                            tint = Color(0xFF38BDF8)
                        )
                    }
                    IconButton(onClick = { activeDialog = BingoDialogType.HowToPlay }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "How To Play",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF140F2D)
                )
            )
        }
    ) { innerPadding ->
        AaaCasinoBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content Scrollable View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2. GAME MODES (Offline & Online Cards)
                Text(
                    text = "SELECT GAME MODE",
                    color = Color(0xFFFFD700),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                // CARD 1: OFFLINE MODE
                BingoOfflineCard(
                    glowAlpha = glowAlpha,
                    floatOffset = floatOffset,
                    onPlayClick = { activeDialog = BingoDialogType.OfflineDifficulty }
                )

                // CARD 1.5: PRIVATE ROOM CARD
                BingoPrivateCard(
                    glowAlpha = glowAlpha,
                    floatOffset = -floatOffset,
                    onPlayClick = {
                        initialSocialTabIndex = 1
                        isSocialEventsActive = true
                    }
                )

                // CARD 2: ONLINE MULTIPLAYER
                BingoOnlineCard(
                    glowAlpha = glowAlpha,
                    floatOffset = floatOffset,
                    onPlayClick = {
                        if (liveOpsConfig.isMaintenanceMode) {
                            android.widget.Toast.makeText(
                                context,
                                liveOpsConfig.maintenanceMessage,
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            isOnlineLobbyActive = true
                        }
                    }
                )

                // CARD 3: MISSIONS, PRIVATE ROOMS & COSMETICS HUB (PHASE 10)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFFFFD700))
                        .clickable {
                            initialSocialTabIndex = 0
                            isSocialEventsActive = true
                        },
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFFFFD700))
                        )
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1B4B).copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        Brush.radialGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))),
                                        CircleShape
                                    )
                                    .border(1.5.dp, Color(0xFFFFD700), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎯", fontSize = 26.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MISSIONS & CUSTOMIZATIONS",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Daily Missions • Private Rooms • Cosmetics",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Surface(
                                color = Color(0xFFEC4899).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFEC4899))
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = Color(0xFFF472B6),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        AaaBingoButton(
                            text = "OPEN MISSIONS & COSMETICS",
                            onClick = {
                                initialSocialTabIndex = 0
                                isSocialEventsActive = true
                            },
                            variant = BingoButtonVariant.PURPLE,
                            modifier = Modifier.fillMaxWidth(),
                            height = 48.dp
                        )
                    }
                }

                // 3. COMING SOON / FUTURE FEATURES CARD
                BingoComingSoonCard(glowAlpha = glowAlpha)

                // 4. BOTTOM BUTTONS ROW (History, Leaderboard, Statistics, Settings)
                BingoBottomActionsRow(
                    onHistoryClick = { isProgressionActive = true },
                    onLeaderboardClick = { isProgressionActive = true },
                    onStatsClick = { isProgressionActive = true },
                    onSettingsClick = { activeDialog = BingoDialogType.Settings }
                )



                Spacer(modifier = Modifier.height(16.dp))
            }

            // Active Dialog / Modal Overlay Management
            when (activeDialog) {
                BingoDialogType.HowToPlay -> HowToPlayDialog(onDismiss = { activeDialog = null })
                BingoDialogType.OfflineDifficulty -> OfflineDifficultyDialog(
                    onDismiss = { activeDialog = null },
                    onStartMatch = { selectedDifficulty ->
                        activeDialog = null
                        activeDifficultyForMatch = selectedDifficulty
                    }
                )
                BingoDialogType.OnlineMatchmaking -> OnlineMatchmakingDialog(onDismiss = { activeDialog = null })
                BingoDialogType.History -> HistoryDialog(onDismiss = { activeDialog = null })
                BingoDialogType.Leaderboard -> LeaderboardDialog(onDismiss = { activeDialog = null })
                BingoDialogType.Statistics -> StatisticsDialog(onDismiss = { activeDialog = null })
                BingoDialogType.Settings -> SettingsDialog(onDismiss = { activeDialog = null })
                null -> {}
            }
        }
    }
}

// ==========================================
// COMPONENT 1: PLAYER PANEL
// ==========================================
@Composable
private fun BingoPlayerPanel(glowAlpha: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFD700)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = glowAlpha),
                    Color(0xFF7C4DFF),
                    Color(0xFFFFD700).copy(alpha = 0.5f)
                )
            )
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B133A).copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Avatar with Crown
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFFF8F00), Color(0xFF3F51B5))
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Text(
                    text = "🎯",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + Level + XP Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "PlayWin Champion",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        color = Color(0xFF00E676).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFF00E676)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "🟢 Online",
                            color = Color(0xFF00E676),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // XP Progress Track
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFFFFD700),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "LVL 12",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { 0.72f },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFFFD700),
                        trackColor = Color(0xFF2A1F52),
                    )

                    Text(
                        text = "1,450/2k XP",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Coins Balance Box
            Surface(
                color = Color(0xFF2A1F52),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "💰", fontSize = 14.sp)
                    Text(
                        text = "2,500",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 2: OFFLINE GAME MODE CARD
// ==========================================
@Composable
private fun BingoOfflineCard(
    glowAlpha: Float,
    floatOffset: Float,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = floatOffset.dp)
            .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF00E676)),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF00E676).copy(alpha = glowAlpha),
                    Color(0xFFFFD700).copy(alpha = 0.6f),
                    Color(0xFF00E676).copy(alpha = 0.4f)
                )
            )
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF132238).copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row (Icon + Titles + Badge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF00E676), Color(0xFF00695C))),
                            CircleShape
                        )
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🤖", fontSize = 26.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OFFLINE BINGO",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Play Anytime Without Internet",
                        color = Color(0xFFB9F6CA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = Color(0xFF00E676).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E676))
                ) {
                    Text(
                        text = "VS AI",
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Feature Checklist Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BingoCheckItem(text = "No Internet Connection Required")
                BingoCheckItem(text = "Easy, Medium & Hard AI Difficulties")
                BingoCheckItem(text = "Single & Multi-Card Challenges")
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Glossy Green PLAY OFFLINE Button
            AaaBingoButton(
                text = "PLAY OFFLINE",
                onClick = onPlayClick,
                variant = BingoButtonVariant.SUCCESS,
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.fillMaxWidth(),
                height = 52.dp
            )
        }
    }
}

// ==========================================
// COMPONENT 2.5: PRIVATE GAME MODE CARD
// ==========================================
@Composable
private fun BingoPrivateCard(
    glowAlpha: Float,
    floatOffset: Float,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = floatOffset.dp)
            .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF8B5CF6)),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF8B5CF6).copy(alpha = glowAlpha),
                    Color(0xFFD946EF).copy(alpha = 0.8f),
                    Color(0xFF8B5CF6).copy(alpha = 0.4f)
                )
            )
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151230).copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row (Icon + Titles + Badge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF8B5CF6), Color(0xFF4C1D95))),
                            CircleShape
                        )
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👥", fontSize = 26.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PRIVATE ROOM",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Play With Friends Using Room Code",
                        color = Color(0xFFE9D5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = Color(0xFFD946EF).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFD946EF))
                ) {
                    Text(
                        text = "PRIVATE",
                        color = Color(0xFFD946EF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Feature Checklist Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BingoCheckItem(text = "Create a Private Room")
                BingoCheckItem(text = "Join Using 6-Digit Room Code")
                BingoCheckItem(text = "Play with Friends in Real-Time")
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Glossy Purple CREATE / JOIN Button
            AaaBingoButton(
                text = "CREATE / JOIN ROOM",
                onClick = onPlayClick,
                variant = BingoButtonVariant.PURPLE,
                iconEmoji = "🎮",
                modifier = Modifier.fillMaxWidth(),
                height = 52.dp
            )
        }
    }
}

// ==========================================
// COMPONENT 3: ONLINE GAME MODE CARD
// ==========================================
@Composable
private fun BingoOnlineCard(
    glowAlpha: Float,
    floatOffset: Float,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = floatOffset.dp)
            .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF2979FF)),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF2979FF).copy(alpha = glowAlpha),
                    Color(0xFFE040FB).copy(alpha = 0.8f),
                    Color(0xFF2979FF).copy(alpha = 0.4f)
                )
            )
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131B38).copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF2979FF), Color(0xFF1A237E))),
                            CircleShape
                        )
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🌐", fontSize = 26.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ONLINE MULTIPLAYER",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Compete With Real Players",
                        color = Color(0xFF80D8FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = Color(0xFFFF1744),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "LIVE 1v4",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Feature Checklist Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BingoCheckItem(text = "Live Matchmaking with Global Players")
                BingoCheckItem(text = "Win Massive Coin Jackpots")
                BingoCheckItem(text = "Climb the Global Leaderboards")
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Glossy Blue PLAY ONLINE Button
            AaaBingoButton(
                text = "PLAY ONLINE",
                onClick = onPlayClick,
                variant = BingoButtonVariant.INFO,
                icon = Icons.Default.Public,
                modifier = Modifier.fillMaxWidth(),
                height = 52.dp
            )
        }
    }
}

// ==========================================
// COMPONENT 4: COMING SOON CARD
// ==========================================
@Composable
private fun BingoComingSoonCard(glowAlpha: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF171033).copy(alpha = 0.75f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FUTURE FEATURES",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )

                Surface(
                    color = Color(0xFF7C4DFF).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF7C4DFF))
                ) {
                    Text(
                        text = "COMING SOON",
                        color = Color(0xFFD1C4E9),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ComingSoonItem(icon = "🏆", label = "Tournament")
                ComingSoonItem(icon = "👥", label = "Private Room")
                ComingSoonItem(icon = "🎁", label = "Daily Events")
                ComingSoonItem(icon = "🎖️", label = "Ranked Season")
            }
        }
    }
}

@Composable
private fun ComingSoonItem(icon: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFF221748).copy(alpha = 0.6f), CircleShape)
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp, modifier = Modifier.scale(0.85f))
        }
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==========================================
// COMPONENT 5: BOTTOM ACTIONS ROW
// ==========================================
@Composable
private fun BingoBottomActionsRow(
    onHistoryClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BingoActionButton(
            modifier = Modifier.weight(1f),
            icon = "📜",
            label = "History",
            onClick = onHistoryClick
        )
        BingoActionButton(
            modifier = Modifier.weight(1f),
            icon = "🏆",
            label = "Leaderboard",
            onClick = onLeaderboardClick
        )
        BingoActionButton(
            modifier = Modifier.weight(1f),
            icon = "📊",
            label = "Stats",
            onClick = onStatsClick
        )
        BingoActionButton(
            modifier = Modifier.weight(1f),
            icon = "⚙️",
            label = "Settings",
            onClick = onSettingsClick
        )
    }
}

@Composable
private fun BingoActionButton(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1C133D),
        border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun BingoCheckItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF00E676),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// ==========================================
// PHASE 2 DIALOGS & POPUPS
// ==========================================

/**
 * 1. HOW TO PLAY DIALOG
 */
@Composable
private fun HowToPlayDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181033))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "HOW TO PLAY BINGO",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                // Visual Mini Grid Illustration
                Surface(
                    color = Color(0xFF231648),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF7C4DFF))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "5x5 CLASSIC CARD PATTERNS",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(5) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (it == 2) Color(0xFFFFD700) else Color(0xFF3F2B75),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (it == 2) "★" else "${(it + 1) * 7}",
                                        fontSize = 10.sp,
                                        color = if (it == 2) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Step-by-step instructions
                HowToPlayStep(
                    stepNum = "1",
                    title = "Match Called Numbers",
                    desc = "Listen or watch numbers drawn randomly. Tap matching cells on your Bingo card to daub them."
                )

                HowToPlayStep(
                    stepNum = "2",
                    title = "Form Winning Lines",
                    desc = "Complete 5 in a row horizontally, vertically, or diagonally across your card."
                )

                HowToPlayStep(
                    stepNum = "3",
                    title = "Shout BINGO First!",
                    desc = "Hit the BINGO button as soon as line is completed to claim instant victory & bonus coins!"
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("GOT IT!", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun HowToPlayStep(stepNum: String, title: String, desc: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF7C4DFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stepNum, color = Color.White, fontWeight = FontWeight.Black)
        }
        Column {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = desc, color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

/**
 * 2. OFFLINE DIFFICULTY SELECTION DIALOG
 */
@Composable
private fun OfflineDifficultyDialog(
    onDismiss: () -> Unit,
    onStartMatch: (String) -> Unit
) {
    var selectedDiff by remember { mutableStateOf("Medium") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, Color(0xFF00E676)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF132238))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SELECT DIFFICULTY",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )

                DifficultyOption(
                    title = "🟢 Easy",
                    desc = "1 Card • Slow Call Speed • 1.0x Rewards",
                    isSelected = selectedDiff == "Easy",
                    onClick = { selectedDiff = "Easy" }
                )

                DifficultyOption(
                    title = "🟡 Medium",
                    desc = "2 Cards • Normal Speed • 1.5x Rewards",
                    isSelected = selectedDiff == "Medium",
                    onClick = { selectedDiff = "Medium" }
                )

                DifficultyOption(
                    title = "🔴 Hard",
                    desc = "4 Cards • Fast Speed • 2.5x Rewards",
                    isSelected = selectedDiff == "Hard",
                    onClick = { selectedDiff = "Hard" }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text("CANCEL", color = Color.White)
                    }

                    Button(
                        onClick = { onStartMatch(selectedDiff) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Text("START", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultyOption(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFF1E2F4A),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) Color(0xFF00E676) else Color.Gray.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = desc, color = Color.LightGray, fontSize = 11.sp)
        }
    }
}

/**
 * 3. ONLINE MATCHMAKING DIALOG
 */
@Composable
private fun OnlineMatchmakingDialog(onDismiss: () -> Unit) {
    var foundPlayers by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        while (foundPlayers < 4) {
            delay(1200)
            foundPlayers++
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, Color(0xFF2979FF)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B38))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "SEARCHING MATCH...",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )

                CircularProgressIndicator(
                    color = Color(0xFF2979FF),
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = "Found $foundPlayers / 4 Players",
                    color = Color(0xFF80D8FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(4) { idx ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (idx < foundPlayers) Color(0xFF2979FF) else Color.Gray.copy(alpha = 0.3f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (idx < foundPlayers) "👤" else "❓",
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("CANCEL MATCHMAKING", color = Color.White)
                }
            }
        }
    }
}

/**
 * 4. HISTORY DIALOG
 */
@Composable
private fun HistoryDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181033))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "MATCH HISTORY",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )

                val historyLogs = listOf(
                    Triple("Online 1v4", "VICTORY", "+500 Coins"),
                    Triple("Offline AI", "VICTORY", "+250 Coins"),
                    Triple("Online 1v4", "2nd Place", "+100 Coins"),
                    Triple("Offline AI", "DEFEAT", "0 Coins")
                )

                historyLogs.forEach { (mode, outcome, prize) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF231648), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = mode, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = outcome,
                                color = if (outcome.contains("VICTORY")) Color(0xFF00E676) else Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                        Text(text = prize, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Text("CLOSE", color = Color.White)
                }
            }
        }
    }
}

/**
 * 5. LEADERBOARD DIALOG
 */
@Composable
private fun LeaderboardDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181033))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "GLOBAL LEADERBOARD",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )

                val leaders = listOf(
                    Triple("#1 👑", "BingoMaster99", "12,450 Wins"),
                    Triple("#2 🥈", "LuckyStar_07", "9,820 Wins"),
                    Triple("#3 🥉", "SpeedDauber", "8,100 Wins"),
                    Triple("#4", "PlayWin_Pro", "6,540 Wins")
                )

                leaders.forEach { (rank, name, wins) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF231648), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = rank, color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
                        Text(text = name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = wins, color = Color.LightGray, fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Text("CLOSE", color = Color.White)
                }
            }
        }
    }
}

/**
 * 6. STATISTICS DIALOG
 */
@Composable
private fun StatisticsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181033))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "YOUR STATISTICS",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )

                StatRow("Total Matches Played", "142")
                StatRow("Bingo Win Rate", "68.5%")
                StatRow("Total Coins Won", "48,200 💰")
                StatRow("Current Win Streak", "5 🔥")

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Text("CLOSE", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF231648), RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.LightGray, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

/**
 * 7. SETTINGS DIALOG
 */
@Composable
private fun SettingsDialog(onDismiss: () -> Unit) {
    var soundOn by remember { mutableStateOf(true) }
    var musicOn by remember { mutableStateOf(true) }
    var vibrateOn by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181033))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "BINGO SETTINGS",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )

                SettingToggle("Sound Effects 🔊", soundOn) { soundOn = it }
                SettingToggle("Background Music 🎵", musicOn) { musicOn = it }
                SettingToggle("Vibration 📳", vibrateOn) { vibrateOn = it }

                val context = androidx.compose.ui.platform.LocalContext.current
                val prefs = remember { context.getSharedPreferences("bingo_game_prefs", android.content.Context.MODE_PRIVATE) }
                OutlinedButton(
                    onClick = {
                        prefs.edit().putBoolean("has_seen_bingo_tutorial", false).commit()
                        android.widget.Toast.makeText(context, "Tutorial reset! Will play on your next game.", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700))
                ) {
                    Text("🎓 REPLAY / RESET TUTORIAL", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Text("SAVE & CLOSE", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700))
        )
    }
}
