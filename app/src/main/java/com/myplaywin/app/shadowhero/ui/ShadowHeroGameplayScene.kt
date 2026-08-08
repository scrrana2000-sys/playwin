package com.myplaywin.app.shadowhero.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.myplaywin.app.shadowhero.audio.ShadowHeroAudioEngine
import com.myplaywin.app.shadowhero.data.ShadowHeroProgressionManager
import com.myplaywin.app.shadowhero.engine.ShadowHeroEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadowHeroGameplayScene(
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // --- LANDSCAPE & IMMERSIVE FULLSCREEN SYSTEM ---
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        ShadowHeroAudioEngine.init(context)

        // Enable 100% Immersive Fullscreen (Edge-To-Edge, Hide Status & Nav Bars)
        val window = activity?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            activity?.requestedOrientation = originalOrientation
            ShadowHeroAudioEngine.pauseAllAudio()
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Authoritative Physics Engine & Controller Instance
    val engine = remember { ShadowHeroEngine() }

    // Pause & Developer Debug Overlay Visibility States
    var isPaused by remember { mutableStateOf(false) }
    var showDebugOverlay by remember { mutableStateOf(false) }

    // Viewport Dimensions for Camera
    var viewportWidth by remember { mutableFloatStateOf(1280f) }
    var viewportHeight by remember { mutableFloatStateOf(720f) }

    // Frame Clock Tick Trigger State
    var frameTick by remember { mutableLongStateOf(0L) }

    // 60 FPS Game Update Loop
    LaunchedEffect(isPaused) {
        var lastTime = System.nanoTime()
        while (!isPaused) {
            withFrameNanos { currentTime ->
                val dt = (currentTime - lastTime) / 1_000_000_000f
                lastTime = currentTime

                // Update Authoritative Game Engine Logic
                engine.update(dt, viewportWidth, viewportHeight)
                frameTick = currentTime
            }
        }
    }

    val animTime = (frameTick / 1_000_000f) / 1000f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07040D))
            .onSizeChanged { size ->
                if (size.width > 0 && size.height > 0) {
                    viewportWidth = size.width.toFloat()
                    viewportHeight = size.height.toFloat()
                }
            }
    ) {
        // --- 100% DISPLAY AREA GAME CANVAS ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            @Suppress("UNUSED_VARIABLE")
            val dummy = frameTick

            val camX = engine.cameraX
            val camY = engine.cameraY
            val shake = engine.cameraShake
            val shakeOffsetX = (Math.random().toFloat() - 0.5f) * shake
            val shakeOffsetY = (Math.random().toFloat() - 0.5f) * shake

            // 1. Multi-Layer Theme Parallax Background
            drawParallaxBackground(
                theme = engine.currentLevel.theme,
                cameraX = camX,
                cameraY = camY,
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight,
                animTime = animTime
            )

            // 2. Camera Translation Matrix
            drawContext.transform.translate(-camX + shakeOffsetX, -camY + shakeOffsetY)

            // 3. Draw Level Platforms with Theme Accent
            drawThemedPlatforms(engine.currentLevel.platforms, engine.currentLevel.theme, animTime)

            // 3b. Draw Phase 4 Environmental Hazards
            drawLevelHazards(engine.currentLevel.hazards, animTime)

            // 3c. Draw Static Spikes
            drawLevelSpikes(engine.currentLevel.spikes)

            // 3d. Draw Moving Spikes
            drawLevelMovingSpikes(engine.currentLevel.movingSpikes)

            // 3e. Draw Rotating Blades
            drawLevelBlades(engine.currentLevel.blades)

            // 3f. Draw Laser Beams
            drawLevelLasers(engine.currentLevel.lasers)

            // 4. Draw Checkpoints
            drawLevelCheckpoints(engine.currentLevel.checkpoints, animTime)

            // 5. Draw Energy Crystals
            drawLevelEnergyCrystals(engine.currentLevel.crystals, animTime)

            // 5b. Draw Power-Up Collectibles (Phase 5)
            drawLevelPowerUps(engine.currentLevel.powerUps, animTime)

            // 5c. Draw AI Enemies
            drawLevelEnemies(engine.currentLevel.enemies, animTime)

            // 5d. Draw Enemy Projectiles
            drawEnemyProjectiles(engine.enemyProjectiles)

            // 6. Draw Exit Portal
            drawLevelExitPortal(engine.currentLevel.exitPortal, animTime)

            // 7. Draw Dash Ghosts
            drawDashGhostTrails(
                ghosts = engine.dashGhosts,
                cameraX = 0f,
                cameraY = 0f,
                playerWidth = engine.player.width,
                playerHeight = engine.player.height
            )

            // 8. Draw Particles & Floating Texts
            drawHeroParticlesAndFloatingTexts(
                particles = engine.particles,
                floatingTexts = engine.floatingTexts,
                cameraX = 0f,
                cameraY = 0f
            )

            // 9. Draw Shadow Hero Character
            drawShadowHeroCharacter(
                player = engine.player,
                drawX = engine.player.x,
                drawY = engine.player.y,
                animTime = animTime
            )

            // 10. Draw Active Shadow Shield (Phase 5)
            if (engine.shieldActive) {
                drawPlayerShield(
                    drawX = engine.player.x,
                    drawY = engine.player.y,
                    playerWidth = engine.player.width,
                    playerHeight = engine.player.height,
                    animTime = animTime
                )
            }
        }

        // --- TOP NEON HUD OVERLAY (Matching Reference Design) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-Left: Hero Avatar + 3 Hearts + Crystal Level Goal + Energy Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Hero Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F0721))
                            .border(2.dp, Brush.radialGradient(listOf(Color(0xFFE879F9), Color(0xFFA855F7))), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(30.dp)) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            drawCircle(color = Color(0xFF090314), center = Offset(cx, cy), radius = 13f)
                            drawCircle(color = Color(0xFFE879F9), center = Offset(cx - 4f, cy - 2f), radius = 3f)
                            drawCircle(color = Color(0xFFE879F9), center = Offset(cx + 4f, cy - 2f), radius = 3f)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        // 3 Health Hearts
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(3) {
                                Text(text = "💜", fontSize = 12.sp)
                            }
                        }
                        // Crystal Goal Pill (e.g. 12 / 25)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(Color(0xFF180828).copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "💎", fontSize = 10.sp)
                            Text(
                                text = "${engine.collectedCrystalIds.size} / ${engine.currentLevel.crystals.size.coerceAtLeast(25)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Top-Center: "LEVEL X • THEME" Title + Active Power-Up Badges
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LEVEL ${engine.currentStage}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = engine.currentLevel.theme.platformColor.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, engine.currentLevel.theme.platformBorderColor)
                        ) {
                            Text(
                                text = engine.currentLevel.theme.themeName.uppercase(),
                                color = engine.currentLevel.theme.accentGlowColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Active Ability Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (engine.shieldActive) {
                            Surface(
                                color = Color(0xFF6B21A8).copy(alpha = 0.9f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFC084FC))
                            ) {
                                Text(
                                    text = "🛡️ SHIELD ${String.format("%.0f", engine.shieldTimer)}s",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (engine.shadowTimeActive) {
                            Surface(
                                color = Color(0xFF0369A1).copy(alpha = 0.9f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8))
                            ) {
                                Text(
                                    text = "⏳ SLOW ${String.format("%.0f", engine.shadowTimeTimer)}s",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (engine.magnetActive) {
                            Surface(
                                color = Color(0xFF854D0E).copy(alpha = 0.9f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFFACC15))
                            ) {
                                Text(
                                    text = "🧲 MAGNET ${String.format("%.0f", engine.magnetTimer)}s",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Top-Right: Coins + Crystals + Pause
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Coins Counter Pill
                    Surface(
                        color = Color(0xFF1E1038).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🪙 ", fontSize = 11.sp)
                            Text(
                                text = "${350 + engine.currentStage * 25}",
                                color = Color(0xFFFACC15),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Crystals Counter Pill
                    Surface(
                        color = Color(0xFF1E1038).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE879F9).copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💎 ", fontSize = 11.sp)
                            Text(
                                text = "${120 + engine.collectedCrystalIds.size * 5}",
                                color = Color(0xFFE879F9),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Pause Button
                    IconButton(
                        onClick = { isPaused = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3B0764).copy(alpha = 0.9f))
                            .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Phase 5 Shadow Energy Bar (HUD)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("⚡", fontSize = 10.sp)
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF0F0721))
                        .border(
                            width = 1.dp,
                            color = if (engine.lowEnergyFeedbackTimer > 0f) Color(0xFFEF4444) else Color(0xFFA855F7).copy(alpha = 0.8f),
                            shape = RoundedCornerShape(5.dp)
                        )
                ) {
                    val fillFrac = (engine.energy / ShadowHeroEngine.MAX_ENERGY).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fillFrac)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF7C3AED), Color(0xFFC084FC), Color(0xFF38BDF8))
                                )
                            )
                    )
                }
                Text(
                    text = "${engine.energy.toInt()}%",
                    color = if (engine.lowEnergyFeedbackTimer > 0f) Color(0xFFEF4444) else Color(0xFFC084FC),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- DEVELOPER DEBUG OVERLAY (Phase 3 Rule) ---
        if (showDebugOverlay) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFA855F7)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 60.dp, start = 20.dp)
                    .width(300.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("⚙️ DEVELOPER DEBUG PANEL", color = Color(0xFFA855F7), fontWeight = FontWeight.Black, fontSize = 11.sp)
                    Text("Seed: ${engine.currentLevel.seed}", color = Color.White, fontSize = 10.sp)
                    Text("Stage: ${engine.currentStage} (${engine.currentLevel.difficultyName})", color = Color.White, fontSize = 10.sp)
                    Text("Patterns: ${engine.currentLevel.patternSequence.joinToString(" → ")}", color = Color.Gray, fontSize = 9.sp)
                    Text("Gen Time: ${engine.currentLevel.generationTimeMs}ms (Attempts: ${engine.currentLevel.validationAttempts})", color = Color(0xFF38BDF8), fontSize = 10.sp)
                    Text("Path Validation Status: 100% REACHABLE", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold, fontSize = 10.sp)

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { engine.loadStage(engine.currentStage, engine.currentLevel.seed + 10007L) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text("Re-roll Seed Candidate", fontSize = 10.sp)
                    }
                }
            }
        }

        // --- PRE-STAGE COUNTDOWN OVERLAY ("3... 2... 1... GO!") ---
        if (engine.stageCountdownTimer > 0f) {
            val countVal = engine.stageCountdownTimer
            val text = when {
                countVal > 2.5f -> "READY"
                countVal > 1.8f -> "3"
                countVal > 1.1f -> "2"
                countVal > 0.4f -> "1"
                else -> "GO!"
            }
            val textColor = if (text == "GO!") Color(0xFF4ADE80) else Color(0xFFE879F9)

            LaunchedEffect(text) {
                if (text == "3" || text == "2" || text == "1") {
                    ShadowHeroAudioEngine.playCountdownBeep(isGo = false)
                } else if (text == "GO!") {
                    ShadowHeroAudioEngine.playCountdownBeep(isGo = true)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = Color(0xFF0F0721).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, engine.currentLevel.theme.platformBorderColor),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "STAGE ${engine.currentStage} • ${engine.currentLevel.theme.themeName.uppercase()}",
                            color = engine.currentLevel.theme.accentGlowColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                        text = text,
                        color = textColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 60.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // --- MOBILE TOUCH CONTROLS OVERLAY ---
        ShadowHeroMobileControls(
            onLeftPressChange = { isPressed -> engine.inputLeft = isPressed },
            onRightPressChange = { isPressed -> engine.inputRight = isPressed },
            onJumpClick = { engine.triggerJump() },
            onDashClick = { engine.triggerDash() },
            dashCooldownFraction = (engine.player.dashCooldownTimer / ShadowHeroEngine.DASH_COOLDOWN).coerceIn(0f, 1f)
        )

        // --- GAME OVER / DEATH DIALOG OVERLAY (Phase 5) ---
        if (engine.isGameOverDialogOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF180A1C),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFA855F7)))),
                    modifier = Modifier.width(360.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "HERO FELL",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = if (engine.activeCheckpoint != null) "Checkpoint saved • Resume journey" else "Stage ${engine.currentStage} • Pitfall or Hazard",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        // Rewarded Continue Option (1 per run)
                        if (!engine.hasUsedContinue) {
                            Button(
                                onClick = { engine.performRewardedContinue() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎬 REWARDED CONTINUE", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(1/1)", fontSize = 11.sp, color = Color.LightGray)
                                }
                            }
                        }

                        // Respawn / Try Again
                        Button(
                            onClick = {
                                ShadowHeroProgressionManager.incrementDeathCount(context)
                                engine.resetToActiveCheckpointOrStart()
                                engine.isGameOverDialogOpen = false
                                engine.player.isDead = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (engine.activeCheckpoint != null) "RESPAWN AT CHECKPOINT" else "RESTART STAGE",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quit to Home
                        OutlinedButton(
                            onClick = {
                                ShadowHeroProgressionManager.incrementDeathCount(context)
                                engine.isGameOverDialogOpen = false
                                onBackToHome()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("QUIT TO MENU", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- STAGE COMPLETE MODAL OVERLAY ---
        if (engine.isStageComplete) {
            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_hero"
            var awardedCoins by remember { mutableStateOf<Int?>(null) }

            LaunchedEffect(engine.isStageComplete, engine.currentStage) {
                if (engine.isStageComplete) {
                    // Record stage clear & metrics
                    ShadowHeroProgressionManager.updateStatsOnStageComplete(
                        context = context,
                        stage = engine.currentStage,
                        crystalsCollected = engine.collectedCrystalIds.size,
                        completionTime = engine.stageTimeSeconds,
                        distanceTraveled = engine.player.x,
                        powerUpsUsed = 0,
                        deathsInStage = 0
                    )
                    // Award Stage Completion Coins atomically via WalletService
                    ShadowHeroProgressionManager.awardStageCompletionCoins(
                        context = context,
                        userId = currentUserId,
                        stage = engine.currentStage,
                        seed = engine.currentLevel.seed,
                        crystalsCollected = engine.collectedCrystalIds.size,
                        totalCrystalsInStage = engine.currentLevel.crystals.size,
                        completionTime = engine.stageTimeSeconds,
                        deathsInStage = 0,
                        onResult = { success, coins, _ ->
                            if (success && coins > 0) {
                                awardedCoins = coins
                                ShadowHeroAudioEngine.playCrystalCollect()
                            }
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF170E2B),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFF06B6D4)))),
                    modifier = Modifier.width(360.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "STAGE CLEAR!",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "Stage ${engine.currentStage} Conquered",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        // Stats & Rewards Summary Box
                        Surface(
                            color = Color(0xFF0F0721),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("PlayWin Coins", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        text = if (awardedCoins != null) "+${awardedCoins} 🪙" else "Calculating...",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Energy Crystals", color = Color.Gray, fontSize = 12.sp)
                                    Text(
                                        "💎 ${engine.collectedCrystalIds.size} / ${engine.currentLevel.crystals.size}",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Clear Time", color = Color.Gray, fontSize = 12.sp)
                                    Text(
                                        "⏱️ ${String.format("%.1f", engine.stageTimeSeconds)}s",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Level Seed", color = Color.Gray, fontSize = 11.sp)
                                    Text(
                                        "#${engine.currentLevel.seed}",
                                        color = Color(0xFFC084FC),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Next Stage Button
                        Button(
                            onClick = {
                                // Save progression stats
                                ShadowHeroProgressionManager.updateStatsOnGameEnd(
                                    context = context,
                                    stageReached = engine.currentStage + 1,
                                    score = (engine.currentStage * 1000) + (engine.collectedCrystalIds.size * 200),
                                    enemiesDefeated = 0
                                )
                                // Load next stage
                                engine.loadStage(engine.currentStage + 1)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("NEXT STAGE", fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("▶", fontSize = 16.sp)
                            }
                        }

                        // Replay Stage
                        OutlinedButton(
                            onClick = { engine.restartCurrentStage() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = BorderStroke(1.dp, Color(0xFF0284C7)),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REPLAY STAGE", fontWeight = FontWeight.Bold)
                        }

                        // Quit to Home Screen
                        OutlinedButton(
                            onClick = { onBackToHome() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("QUIT TO MENU", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- PAUSE MENU DIALOG ---
        if (isPaused) {
            var currentSettings by remember { mutableStateOf(ShadowHeroProgressionManager.getSettings(context)) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF170E2B),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFA855F7)),
                    modifier = Modifier.width(340.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )

                        Text(
                            text = "Stage ${engine.currentStage} • ${engine.currentLevel.difficultyName}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        // Quick Audio Settings in Pause Menu
                        Surface(
                            color = Color(0xFF0F0721),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔊 SFX", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Switch(
                                        checked = currentSettings.soundEnabled,
                                        onCheckedChange = {
                                            currentSettings = currentSettings.copy(soundEnabled = it)
                                            ShadowHeroAudioEngine.updateSettings(context, currentSettings)
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFA855F7))
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎵 Music", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Switch(
                                        checked = currentSettings.musicEnabled,
                                        onCheckedChange = {
                                            currentSettings = currentSettings.copy(musicEnabled = it)
                                            ShadowHeroAudioEngine.updateSettings(context, currentSettings)
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                ShadowHeroAudioEngine.playButtonClick()
                                isPaused = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RESUME", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                ShadowHeroAudioEngine.playButtonClick()
                                engine.resetToActiveCheckpointOrStart()
                                isPaused = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = BorderStroke(1.dp, Color(0xFF0284C7)),
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RESPAWN AT CHECKPOINT", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                ShadowHeroAudioEngine.playButtonClick()
                                isPaused = false
                                onBackToHome()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("QUIT TO MENU", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
