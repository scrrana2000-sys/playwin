package com.myplaywin.app.blockmaster.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myplaywin.app.blockmaster.constants.BlockMasterConstants
import com.myplaywin.app.blockmaster.effects.BlockMasterParticleBackground
import com.myplaywin.app.blockmaster.engine.GameMode
import com.myplaywin.app.blockmaster.progression.PlayerProgressionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockMasterScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember(context) { BlockMasterViewModel(context) }

    val saveData by viewModel.saveData.collectAsStateWithLifecycle()
    val fps by viewModel.fps.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val isWorldMapOpen by viewModel.isWorldMapOpen.collectAsStateWithLifecycle()

    // Phase 7 States
    val isLiveHubOpen by viewModel.isLiveHubOpen.collectAsStateWithLifecycle()
    val isAchievementsOpen by viewModel.isAchievementsOpen.collectAsStateWithLifecycle()
    val isProfileStatsOpen by viewModel.isProfileStatsOpen.collectAsStateWithLifecycle()
    val chestRewardOutcome by viewModel.chestRewardOutcome.collectAsStateWithLifecycle()
    val dailyMissions by viewModel.dailyMissions.collectAsStateWithLifecycle()
    val weeklyMissions by viewModel.weeklyMissions.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val activeEvent = viewModel.activeEvent

    // Phase 8 States
    val isAdRewardCenterOpen by viewModel.isAdRewardCenterOpen.collectAsStateWithLifecycle()
    val isCoinAnimationTriggered by viewModel.isCoinAnimationTriggered.collectAsStateWithLifecycle()
    val hasDoubledMatchCoins by viewModel.hasDoubledMatchCoins.collectAsStateWithLifecycle()

    // Phase 9 States
    val isSeasonPassOpen by viewModel.isSeasonPassOpen.collectAsStateWithLifecycle()
    val isDailyStoreOpen by viewModel.isDailyStoreOpen.collectAsStateWithLifecycle()
    val isLuckySpinOpen by viewModel.isLuckySpinOpen.collectAsStateWithLifecycle()
    val isCollectionsOpen by viewModel.isCollectionsOpen.collectAsStateWithLifecycle()
    val isAdvancedStatsOpen by viewModel.isAdvancedStatsOpen.collectAsStateWithLifecycle()
    val isLeaderboardOpen by viewModel.isLeaderboardOpen.collectAsStateWithLifecycle()
    val selectedGameMode by viewModel.selectedGameMode.collectAsStateWithLifecycle()
    val isModeSelectorOpen by viewModel.isModeSelectorOpen.collectAsStateWithLifecycle()
    val isShareDialogOpen by viewModel.isShareDialogOpen.collectAsStateWithLifecycle()
    val isAiAssistantOpen by viewModel.isAiAssistantOpen.collectAsStateWithLifecycle()
    val isPhotoModeActive by viewModel.isPhotoModeActive.collectAsStateWithLifecycle()

    val activePiece by viewModel.activePiece.collectAsStateWithLifecycle()
    val nextPiece by viewModel.nextPiece.collectAsStateWithLifecycle()
    val holdPiece by viewModel.holdPiece.collectAsStateWithLifecycle()
    val canHold by viewModel.canHold.collectAsStateWithLifecycle()
    val ghostY by viewModel.ghostY.collectAsStateWithLifecycle()

    val score by viewModel.score.collectAsStateWithLifecycle()
    val level by viewModel.level.collectAsStateWithLifecycle()
    val lines by viewModel.lines.collectAsStateWithLifecycle()
    val coinsEarned by viewModel.coinsEarned.collectAsStateWithLifecycle()
    val comboCount by viewModel.comboCount.collectAsStateWithLifecycle()
    val highestCombo by viewModel.highestCombo.collectAsStateWithLifecycle()
    val isGameOver by viewModel.isGameOver.collectAsStateWithLifecycle()
    val floatingPopups by viewModel.floatingPopups.collectAsStateWithLifecycle()
    val hasContinuedThisGame by viewModel.hasContinuedThisGame.collectAsStateWithLifecycle()
    val engineState by viewModel.engineState.collectAsStateWithLifecycle()

    val currentWorld by viewModel.currentWorld.collectAsStateWithLifecycle()
    val activeMissions by viewModel.activeMissions.collectAsStateWithLifecycle()
    val worldUnlockEvent by viewModel.worldUnlockEvent.collectAsStateWithLifecycle()

    // Phase 5 Power-Up states
    val freezeTimeRemaining by viewModel.freezeTimeRemaining.collectAsStateWithLifecycle()
    val scoreBoosterRemaining by viewModel.scoreBoosterRemaining.collectAsStateWithLifecycle()
    val coinBoosterRemaining by viewModel.coinBoosterRemaining.collectAsStateWithLifecycle()

    val playerRank = remember(level) { PlayerProgressionManager.getRankForLevel(level) }
    val activity = context as? android.app.Activity
    var isQuickHubOpen by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        viewModel.onResume()
        if (activity != null) {
            com.myplaywin.app.blockmaster.ads.BlockMasterAdEngine.preload(activity)
        }
        onDispose {
            viewModel.onPause()
            viewModel.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentWorld.bgGradientBottom)
    ) {
        // Floating Particle Animated Background dynamically matching World theme (Draw Background & Glows ONLY)
        BlockMasterParticleBackground(world = currentWorld, drawParticlesOnly = false, showParticles = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- TOP BAR (PROPORTIONAL HEIGHT) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E1430), CircleShape)
                        .border(1.dp, currentWorld.accentColor.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { viewModel.toggleWorldMap(true) }
                ) {
                    Text(
                        text = BlockMasterConstants.GAME_TITLE,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${currentWorld.iconEmoji} ${currentWorld.name} • MAP 🗺️",
                            color = currentWorld.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // EXPANDABLE QUICK ACTIONS HUB BUTTON
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE040FB).copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE040FB)),
                        modifier = Modifier.clickable { isQuickHubOpen = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ HUB",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.toggleSettingsDialog(true) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E1430), CircleShape)
                            .border(1.dp, currentWorld.accentColor.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- PLAYER STATS & RANK BAR (PROPORTIONAL 38.dp HIGH) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clickable { viewModel.toggleProfileStats(true) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D26)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(
                            currentWorld.accentColor.copy(alpha = 0.5f),
                            currentWorld.secondaryColor.copy(alpha = 0.5f)
                        )
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Score
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "SCORE: ", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "$score", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }

                    // Level & Rank Title
                    Text(
                        text = "LVL $level • ${playerRank.badgeEmoji} ${playerRank.title}",
                        color = currentWorld.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Lines
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "LINES: ", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "$lines", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }

                    // Coins
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFFFD700).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "🪙 +$coinsEarned", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- MISSION OBJECTIVES CARD ---
            BlockMasterMissionCard(
                missions = activeMissions,
                accentColor = currentWorld.accentColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- EXPANDED GAME BOARD AREA WITH COMPACT HOLD & NEXT PREVIEWS ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // HOLD PIECE AND POWER-UPS VERTICAL SECTION
                    BlockMasterLeftPowerUpsColumn(
                        saveData = saveData,
                        holdPiece = holdPiece,
                        onUsePowerUp = { puType -> viewModel.usePowerUp(puType) },
                        modifier = Modifier
                            .width(52.dp)
                            .fillMaxHeight()
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // 10x20 MAIN TETRIS BOARD (EXPANDED TO TAKE MAXIMUM SCREEN ROOM)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .aspectRatio(0.50f),
                        contentAlignment = Alignment.Center
                    ) {
                        BlockMasterGridBoard(
                            gridState = viewModel.gridState,
                            activePiece = activePiece,
                            ghostY = ghostY,
                            world = currentWorld,
                            onMoveLeft = { viewModel.onMoveLeft() },
                            onMoveRight = { viewModel.onMoveRight() },
                            onRotate = { viewModel.onRotate() },
                            onSoftDrop = { viewModel.onSoftDrop() },
                            onHardDrop = { viewModel.onHardDrop() },
                            onHold = { viewModel.onHold() },
                            engineState = engineState,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (engineState == com.myplaywin.app.blockmaster.engine.GameEngineState.IDLE) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = { viewModel.startGame() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE040FB),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF)),
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .shadow(8.dp, RoundedCornerShape(12.dp))
                                        .testTag("start_game_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "🎮 START GAME",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // NEXT PIECE PREVIEW BOX & VERTICAL CONTROLS
                    Column(
                        modifier = Modifier
                            .width(52.dp)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BlockMasterPreviewBox(
                            title = "NEXT",
                            piece = nextPiece,
                            modifier = Modifier
                                .width(52.dp)
                                .height(72.dp)
                        )

                        // START Button (Green)
                        val isIdle = (engineState == com.myplaywin.app.blockmaster.engine.GameEngineState.IDLE)
                        RightSideRectControlButton(
                            label = "START",
                            icon = Icons.Default.PlayArrow,
                            color = Color(0xFF00E676),
                            onClick = {
                                if (isIdle) {
                                    viewModel.startGame()
                                } else {
                                    viewModel.onPlayAgain()
                                }
                            },
                            enabled = true
                        )

                        // RESUME Button (Amber)
                        val isPlaying = (engineState == com.myplaywin.app.blockmaster.engine.GameEngineState.PLAYING)
                        val isPaused = (engineState == com.myplaywin.app.blockmaster.engine.GameEngineState.PAUSED)
                        val resumeLabel = if (isPlaying) "PAUSE" else "RESUME"
                        val resumeIcon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow
                        RightSideRectControlButton(
                            label = resumeLabel,
                            icon = resumeIcon,
                            color = Color(0xFFFFA000),
                            onClick = {
                                if (isPlaying) {
                                    viewModel.onPause()
                                } else if (isPaused) {
                                    viewModel.onResume()
                                }
                            },
                            enabled = isPlaying || isPaused
                        )

                        // EXIT Button (Red)
                        RightSideRectControlButton(
                            label = "EXIT",
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            color = Color(0xFFFF1744),
                            onClick = onBack,
                            enabled = true
                        )
                    }
                }

                // FLOATING SCORE & EVENT POPUPS OVERLAY
                BlockMasterFloatingPopups(
                    popups = floatingPopups,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // --- ACTIVE BUFFS STATUS ROW (Only shown when active) ---
            BlockMasterActiveBuffsRow(
                freezeTimeRemaining = freezeTimeRemaining,
                scoreBoosterRemaining = scoreBoosterRemaining,
                coinBoosterRemaining = coinBoosterRemaining
            )

            // --- MINIMAL ENGINE STATUS & RULE CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D26)),
                border = androidx.compose.foundation.BorderStroke(1.dp, currentWorld.accentColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Status",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentWorld.gameplayModifier.description,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp
                        )
                    }

                    Text(
                        text = "✓ $fps FPS | HIGH: ${saveData.highScore}",
                        color = currentWorld.accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- ADMOB BANNER ---
            com.playwin.ads.BannerManager.BannerAd(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }

        // --- EXPANDABLE QUICK ACTIONS HUB MODAL DIALOG ---
        if (isQuickHubOpen) {
            Dialog(onDismissRequest = { isQuickHubOpen = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF160C2E)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .wrapContentWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ GAME HUB & FEATURES",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            IconButton(
                                onClick = { isQuickHubOpen = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.Gray
                                )
                            }
                        }

                        val actions = listOf(
                            Triple("Season Pass", "👑", Color(0xFFFFD700)) to { viewModel.toggleSeasonPass(true) },
                            Triple("Daily Store", "🛍️", Color(0xFF00E5FF)) to { viewModel.toggleDailyStore(true) },
                            Triple("Lucky Spin", "🎰", Color(0xFFE040FB)) to { viewModel.toggleLuckySpin(true) },
                            Triple("Collections", "🏆", Color(0xFFA855F7)) to { viewModel.toggleCollections(true) },
                            Triple("Advanced Stats", "📈", Color(0xFF00E676)) to { viewModel.toggleAdvancedStats(true) },
                            Triple("Reward Ads", "📺", Color(0xFFFFD700)) to { viewModel.toggleAdRewardCenter(true) },
                            Triple("Live Hub", "🎁", Color(0xFF00E5FF)) to { viewModel.toggleLiveHub(true) },
                            Triple("Achievements", "🎖️", Color(0xFFFFD700)) to { viewModel.toggleAchievements(true) },
                            Triple("Leaderboards", "🥇", Color(0xFFFFD700)) to { viewModel.toggleLeaderboard(true) },
                            Triple("Game Modes", "🎮", Color(0xFF00E5FF)) to { viewModel.toggleModeSelector(true) },
                            Triple("Share Score", "📤", Color(0xFFA855F7)) to { viewModel.toggleShareDialog(true) },
                            Triple("AI Advisor", "🤖", Color(0xFFE040FB)) to { viewModel.toggleAiAssistant(true) },
                            Triple("Photo Mode", "📸", Color(0xFFFFD700)) to { viewModel.togglePhotoMode(true) }
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            actions.chunked(4).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    row.forEach { (item, onClick) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable {
                                                isQuickHubOpen = false
                                                onClick()
                                            }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .background(Color(0xFF22163D), CircleShape)
                                                    .border(1.dp, item.third, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(item.second, fontSize = 18.sp)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.first,
                                                color = Color.White,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- TOP PARTICLE OVERLAY (Rendered above gameplay screen elements but below popups) ---
        BlockMasterParticleBackground(world = currentWorld, drawParticlesOnly = true, showParticles = true)

        // --- LIVE OPS HUB DIALOG ---
        if (isLiveHubOpen) {
            BlockMasterLiveHubDialog(
                dailyMissions = dailyMissions,
                weeklyMissions = weeklyMissions,
                claimedDailyIds = saveData.claimedDailyMissionIds,
                claimedWeeklyIds = saveData.claimedWeeklyMissionIds,
                claimedLoginDays = saveData.loginCalendarClaimedDays,
                activeEvent = activeEvent,
                onClaimDailyMission = { viewModel.claimDailyMission(it) },
                onClaimWeeklyMission = { viewModel.claimWeeklyMission(it) },
                onClaimLoginDay = { viewModel.claimLoginDay(it) },
                onOpenChest = { viewModel.openChest(it) },
                onDismiss = { viewModel.toggleLiveHub(false) }
            )
        }

        // --- ACHIEVEMENTS DIALOG ---
        if (isAchievementsOpen) {
            BlockMasterAchievementsDialog(
                achievements = achievements,
                onClaimAchievement = { viewModel.claimAchievement(it) },
                onDismiss = { viewModel.toggleAchievements(false) }
            )
        }

        // --- PROFILE & STATS DIALOG ---
        if (isProfileStatsOpen) {
            BlockMasterProfileStatsDialog(
                saveData = saveData,
                onDismiss = { viewModel.toggleProfileStats(false) }
            )
        }

        // --- CHEST REWARD OPENING OVERLAY ---
        chestRewardOutcome?.let { outcome ->
            BlockMasterMysteryChestOverlay(
                outcome = outcome,
                onClaimReward = { viewModel.dismissChestOverlay() }
            )
        }

        // --- WORLD MAP DIALOG ---
        if (isWorldMapOpen) {
            BlockMasterWorldMapDialog(
                playerLevel = saveData.playerLevel,
                activeWorldId = currentWorld.id,
                claimedWorldRewards = saveData.claimedWorldRewards,
                onSelectWorld = { worldId ->
                    viewModel.selectWorld(worldId)
                    viewModel.toggleWorldMap(false)
                },
                onClaimReward = { worldId ->
                    viewModel.claimWorldReward(worldId)
                },
                onDismiss = { viewModel.toggleWorldMap(false) }
            )
        }

        // --- WORLD UNLOCK DIALOG ---
        worldUnlockEvent?.let { unlockedWorld ->
            BlockMasterWorldUnlockDialog(
                world = unlockedWorld,
                onDismiss = { viewModel.dismissWorldUnlockDialog() }
            )
        }

        // --- GAME OVER DIALOG ---
        if (isGameOver) {
            BlockMasterGameOverDialog(
                finalScore = score,
                coinsEarned = if (hasDoubledMatchCoins) coinsEarned * 2 else coinsEarned,
                highestCombo = highestCombo,
                linesCleared = lines,
                gameTimeSec = 0L,
                canContinue = !hasContinuedThisGame,
                onPlayAgain = { viewModel.onPlayAgain() },
                onHome = onBack,
                onContinueWithAd = {
                    if (activity != null) {
                        viewModel.watchAdForContinue(activity)
                    }
                }
            )
        }

        // --- AD REWARD CENTER DIALOG ---
        if (isAdRewardCenterOpen) {
            BlockMasterAdRewardDialog(
                matchCoins = coinsEarned,
                canDoubleCoins = !hasDoubledMatchCoins,
                onWatchAdForDouble = {
                    if (activity != null) {
                        viewModel.watchAdForDoubleReward(activity)
                    }
                },
                onWatchAdForBonusCoins = {
                    if (activity != null) {
                        viewModel.watchAdForBonusCoins(activity)
                    }
                },
                onWatchAdForPowerUp = {
                    if (activity != null) {
                        viewModel.watchAdForPowerUp(activity)
                    }
                },
                onDismiss = { viewModel.toggleAdRewardCenter(false) }
            )
        }

        // --- COIN REWARD PARTICLES ANIMATION OVERLAY ---
        BlockMasterCoinAnimationOverlay(
            trigger = isCoinAnimationTriggered,
            onAnimationEnd = { viewModel.dismissCoinAnimation() }
        )

        // --- SETTINGS DIALOG ---
        if (isSettingsOpen) {
            BlockMasterSettingsDialog(
                saveData = saveData,
                onToggleSound = { viewModel.toggleSound() },
                onToggleMusic = { viewModel.toggleMusic() },
                onToggleHaptic = { viewModel.toggleHaptic() },
                onSetGraphicsQuality = { q -> viewModel.setGraphicsQuality(q) },
                onToggleFpsDisplay = { viewModel.toggleFpsDisplay() },
                onSetLanguage = { lang -> viewModel.setSelectedLanguage(lang) },
                onResetProgress = { viewModel.resetProgress() },
                onDismiss = { viewModel.toggleSettingsDialog(false) }
            )
        }

        // --- PHASE 9 DIALOGS ---

        // SEASON PASS DIALOG
        if (isSeasonPassOpen) {
            BlockMasterSeasonPassDialog(
                currentSeasonId = saveData.currentSeasonId,
                seasonXp = saveData.seasonXp,
                claimedFree = saveData.claimedSeasonFreeRewards,
                claimedPremium = saveData.claimedSeasonPremiumRewards,
                onClaimFree = { lvl -> viewModel.claimSeasonFreeReward(saveData.currentSeasonId, lvl) },
                onClaimPremium = { lvl -> viewModel.claimSeasonPremiumReward(saveData.currentSeasonId, lvl) },
                onDismiss = { viewModel.toggleSeasonPass(false) }
            )
        }

        // DAILY STORE DIALOG
        if (isDailyStoreOpen) {
            BlockMasterDailyStoreDialog(
                userCoins = saveData.coins,
                unlockedCosmetics = saveData.unlockedCosmeticIds,
                onBuyOffer = { offer -> viewModel.buyStoreOffer(offer) },
                onDismiss = { viewModel.toggleDailyStore(false) }
            )
        }

        // LUCKY SPIN DIALOG
        if (isLuckySpinOpen) {
            BlockMasterLuckySpinDialog(
                lastSpinTimestamp = saveData.lastLuckySpinTimestamp,
                userCoins = saveData.coins,
                onSpinRewardWon = { reward, wasFree -> viewModel.processLuckySpinReward(reward, wasFree) },
                onDismiss = { viewModel.toggleLuckySpin(false) }
            )
        }

        // COLLECTIONS DIALOG
        if (isCollectionsOpen) {
            BlockMasterCollectionsDialog(
                unlockedCosmeticIds = saveData.unlockedCosmeticIds,
                equippedSkin = saveData.equippedBlockSkin,
                equippedTheme = saveData.equippedGridTheme,
                equippedFrame = saveData.equippedBoardFrame,
                equippedBackground = saveData.equippedBackground,
                equippedTitle = saveData.equippedTitle,
                onEquipCosmetic = { cat, id -> viewModel.equipCosmetic(cat, id) },
                onDismiss = { viewModel.toggleCollections(false) }
            )
        }

        // ADVANCED STATS DIALOG
        if (isAdvancedStatsOpen) {
            BlockMasterAdvancedStatsDialog(
                saveData = saveData,
                onDismiss = { viewModel.toggleAdvancedStats(false) }
            )
        }

        // LEADERBOARD DIALOG
        if (isLeaderboardOpen) {
            BlockMasterLeaderboardDialog(
                saveData = saveData,
                onDismiss = { viewModel.toggleLeaderboard(false) }
            )
        }

        // MODE SELECTOR DIALOG
        if (isModeSelectorOpen) {
            BlockMasterModeSelectorDialog(
                currentMode = selectedGameMode,
                saveData = saveData,
                onSelectMode = { mode -> viewModel.setGameMode(mode) },
                onDismiss = { viewModel.toggleModeSelector(false) }
            )
        }

        // SHARE DIALOG
        if (isShareDialogOpen) {
            BlockMasterShareDialog(
                saveData = saveData,
                onDismiss = { viewModel.toggleShareDialog(false) }
            )
        }

        // AI ASSISTANT DIALOG
        if (isAiAssistantOpen) {
            BlockMasterAiAssistantDialog(
                saveData = saveData,
                onDismiss = { viewModel.toggleAiAssistant(false) }
            )
        }

        // --- PHOTO MODE OVERLAY ---
        if (isPhotoModeActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E1430),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📸", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PHOTO MODE ACTIVE",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Ready to capture pristine screenshot",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.togglePhotoMode(false) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Text(text = "EXIT PHOTO MODE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // --- FPS COUNTER OVERLAY ---
        if (saveData.fpsDisplayEnabled) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
            ) {
                Text(
                    text = "FPS: 60 | ${saveData.graphicsQuality.uppercase()}",
                    color = Color(0xFF00E5FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun RightSideRectControlButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val activeColor = if (enabled) color else Color.Gray.copy(alpha = 0.3f)
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(72.dp)
            .shadow(if (enabled) 4.dp else 0.dp, RoundedCornerShape(10.dp), spotColor = activeColor)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF140D26))
            .border(
                width = 1.dp,
                color = activeColor.copy(alpha = if (enabled) 0.5f else 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                enabled = enabled,
                onClick = {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {}
                    onClick()
                }
            )
            .testTag("control_${label.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = activeColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (enabled) Color.White else Color.Gray.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

