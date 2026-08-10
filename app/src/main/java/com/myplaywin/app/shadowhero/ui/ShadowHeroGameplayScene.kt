package com.myplaywin.app.shadowhero.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.myplaywin.app.shadowhero.audio.ShadowHeroAudioEngine
import com.myplaywin.app.shadowhero.data.ShadowHeroProgressionManager
import com.myplaywin.app.shadowhero.engine.ShadowHeroEngine
import com.myplaywin.app.shadowhero.engine.WorldEventType

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadowHeroGameplayScene(
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    // --- LANDSCAPE & IMMERSIVE FULLSCREEN SYSTEM ---
    SideEffect {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
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
    val stats = remember { ShadowHeroProgressionManager.getStats(context) }

    // Load saved progression stage on start adventure launch & preload ads
    LaunchedEffect(Unit) {
        val targetStage = stats.bestStage.coerceAtLeast(1)
        if (engine.currentStage != targetStage) {
            engine.loadStage(targetStage)
        }
        com.playwin.ads.RewardedManager.preload(context)
    }

    // Pause & Developer Debug Overlay Visibility States
    var isPaused by remember { mutableStateOf(false) }
    var showDebugOverlay by remember { mutableStateOf(false) }

    // Centralized Exit Function for Shadow Hero
    val exitShadowHero = {
        isPaused = true
        ShadowHeroAudioEngine.playButtonClick()
        ShadowHeroAudioEngine.pauseAllAudio()
        engine.isGameOverDialogOpen = false
        // Save progression stats
        ShadowHeroProgressionManager.updateStatsOnGameEnd(
            context = context,
            stageReached = engine.currentStage,
            score = (engine.currentStage * 1000) + (engine.collectedCrystalIds.size * 200),
            enemiesDefeated = 0
        )
        // Restore Portrait Orientation before navigating
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Navigate back to PlayWin Mini Games / Home screen
        onBackToHome()
    }

    // Android Back Button Navigation Handling
    BackHandler {
        if (isPaused) {
            isPaused = false
        } else {
            isPaused = true
        }
    }

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
                engine.update(dt, viewportWidth, viewportHeight, context)
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

            val targetWorldHeight = 440f
            val scale = if (viewportHeight > 0f) (viewportHeight / targetWorldHeight).coerceAtLeast(1.0f) else 1f

            val camX = engine.cameraX
            val camY = engine.cameraY
            val shake = engine.cameraShake
            val shakeOffsetX = (Math.random().toFloat() - 0.5f) * shake
            val shakeOffsetY = (Math.random().toFloat() - 0.5f) * shake

            val activeChunkBiome = engine.chunkManager.getChunkAtPlayerPosition(engine.player.x)?.biomeInfo

            // 1. Multi-Layer Theme Parallax Background
            drawParallaxBackground(
                theme = engine.currentLevel.theme,
                cameraX = camX,
                cameraY = camY,
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight,
                animTime = animTime,
                biomeInfo = activeChunkBiome,
                eventDirector = engine.eventDirector
            )

            // 2. Camera Scaling & Translation Matrix (Safe non-accumulating transform)
            withTransform({
                scale(scale, scale, pivot = Offset.Zero)
                translate(-camX + shakeOffsetX, -camY + shakeOffsetY)
            }) {
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

                // Dynamic World Events Graphics (Phase 11B)
                // Falling Crystals (Crystal Rain)
                for (fc in engine.eventDirector.fallingCrystals) {
                    if (!fc.isCollected) {
                        drawCircle(
                            color = Color(0xFF38BDF8),
                            center = Offset(fc.x, fc.y),
                            radius = 6f + kotlin.math.sin(animTime * 8f + fc.x) * 2f
                        )
                    }
                }

                // Meteors (Meteor Fall Event)
                for (m in engine.eventDirector.activeMeteors) {
                    if (!m.isImpacted) {
                        // Telegraph warning circle on ground
                        val pulse = kotlin.math.sin(animTime * 12f) * 0.2f + 0.8f
                        drawCircle(
                            color = Color(0xFFEF4444).copy(alpha = 0.5f * pulse),
                            center = Offset(m.targetX, m.targetY),
                            radius = 32f,
                            style = Stroke(width = 3f)
                        )
                        val fireY = m.targetY - (m.telegraphTimer / m.totalTelegraphTime) * 350f
                        drawCircle(
                            color = Color(0xFFF97316),
                            center = Offset(m.targetX, fireY),
                            radius = 14f
                        )
                    } else {
                        // Impact shockwave
                        drawCircle(
                            color = Color(0xFFEF4444).copy(alpha = (m.impactDurationTimer / 0.6f).coerceIn(0f, 1f)),
                            center = Offset(m.targetX, m.targetY),
                            radius = 45f * (1f - m.impactDurationTimer / 0.6f)
                        )
                    }
                }

                // 5c. Draw AI Enemies
                drawLevelEnemies(engine.currentLevel.enemies, animTime)

                // 5c2. Draw Purple Combat Slashes (Phase 13)
                drawActiveSlashes(engine.activeSlashes)

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
                val activeChunkForChar = engine.chunkManager.getChunkAtPlayerPosition(engine.player.x)
                drawShadowHeroCharacter(
                    player = engine.player,
                    drawX = engine.player.x,
                    drawY = engine.player.y,
                    animTime = animTime,
                    activeBiome = activeChunkForChar?.biomeInfo
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
        }

        // --- TOP NEON HUD OVERLAY (Final Clean Compact HUD) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SINGLE COMPACT HORIZONTAL HUD ROW
            // Order: LIVES → ENERGY → LEVEL → WORLD/BIOME → [Spacer] → GEMS → SETTINGS → PAUSE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. LIVES (❤️ 2/3)
                Surface(
                    color = Color(0xFF130924).copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "LIVES",
                            color = Color.Gray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                            for (i in 1..engine.maxLives) {
                                val isFilled = i <= engine.lives
                                Text(
                                    text = if (isFilled) "❤️" else "🖤",
                                    fontSize = 8.sp,
                                    modifier = Modifier.testTag("health_heart_$i")
                                )
                            }
                        }
                        Text(
                            text = "${engine.lives}/${engine.maxLives}",
                            color = Color(0xFFEF4444),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 2. ENERGY (⚡ [bar] 100%)
                Surface(
                    color = Color(0xFF130924).copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "⚡", fontSize = 8.sp)
                        Box(
                            modifier = Modifier
                                .width(45.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF0F0721))
                                .border(
                                    width = 0.5.dp,
                                    color = if (engine.lowEnergyFeedbackTimer > 0f) Color(0xFFEF4444) else Color(0xFFA855F7).copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(3.dp)
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
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3. LEVEL (LEVEL 2)
                Surface(
                    color = Color(0xFF180828).copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "LEVEL ${engine.currentStage}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 8.5.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // 4. WORLD/BIOME (NEON CAVES)
                Surface(
                    color = engine.currentLevel.theme.platformColor.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, engine.currentLevel.theme.platformBorderColor)
                ) {
                    Text(
                        text = engine.currentLevel.theme.themeName.uppercase(),
                        color = engine.currentLevel.theme.accentGlowColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 5. GEMS (💎 295)
                Surface(
                    color = Color(0xFF1E1038).copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE879F9).copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(text = "💎", fontSize = 8.5.sp)
                        Text(
                            text = "${120 + engine.collectedCrystalIds.size * 5}",
                            color = Color(0xFFE879F9),
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp
                        )
                    }
                }

                // 6. SETTINGS / TOOLS (Box with clickable - no IconButton touch padding overlap!)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (engine.isDebugOverlayVisible) Color(0xFF10B981) else Color(0xFF1E293B).copy(alpha = 0.9f))
                        .border(1.dp, if (engine.isDebugOverlayVisible) Color(0xFF34D399) else Color(0xFF64748B), RoundedCornerShape(6.dp))
                        .clickable { engine.isDebugOverlayVisible = !engine.isDebugOverlayVisible },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛠️", fontSize = 11.sp)
                }

                // 7. PAUSE (Box with clickable - no IconButton touch padding overlap!)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3B0764).copy(alpha = 0.9f))
                        .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(6.dp))
                        .clickable { isPaused = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Optional Active Ability Badges row
            if (engine.shieldActive || engine.shadowTimeActive || engine.dashBoostActive || engine.magnetActive || engine.doubleCrystalActive) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (engine.shieldActive) {
                        Surface(
                            color = Color(0xFF6B21A8).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFFC084FC))
                        ) {
                            Text(
                                text = "🛡️ SHIELD ${String.format("%.0f", engine.shieldTimer)}s",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (engine.shadowTimeActive) {
                        Surface(
                            color = Color(0xFF0369A1).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Text(
                                text = "⏳ SLOW ${String.format("%.0f", engine.shadowTimeTimer)}s",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (engine.dashBoostActive) {
                        Surface(
                            color = Color(0xFF701A75).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFFE879F9))
                        ) {
                            Text(
                                text = "⚡ DASH ${String.format("%.0f", engine.dashBoostTimer)}s",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (engine.magnetActive) {
                        Surface(
                            color = Color(0xFF854D0E).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFFFACC15))
                        ) {
                            Text(
                                text = "🧲 MAGNET ${String.format("%.0f", engine.magnetTimer)}s",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (engine.doubleCrystalActive) {
                        Surface(
                            color = Color(0xFF065F46).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFF4ADE80))
                        ) {
                            Text(
                                text = "💎 2X ${String.format("%.0f", engine.doubleCrystalTimer)}s",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Event Telegraph Warning Banner (Phase 11B Section 16)
            if (engine.eventDirector.telegraphTimer > 0f) {
                val bannerTitle = engine.eventDirector.telegraphBannerTitle
                Surface(
                    color = engine.eventDirector.activeEvent.primaryColor.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color.White),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = bannerTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            // Developer Debug Information Overlay (Phase 11A Section 19 & Phase 11B)
            if (engine.isDebugOverlayVisible) {
                val diffParams = engine.difficultyDirector.getDifficultyParameters(engine.currentStage)
                val activeChunk = engine.chunkManager.getChunkAtPlayerPosition(engine.player.x)
                val biome = activeChunk?.biomeInfo
                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .widthIn(max = 380.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text("🛠️ DEVELOPER WORLD DEBUG OVERLAY", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Chunk Index: #${engine.chunkManager.currentChunkIndex}", color = Color.White, fontSize = 10.sp)
                        Text("Chunk Seed: ${engine.chunkManager.stageSeed}", color = Color(0xFF38BDF8), fontSize = 10.sp)
                        if (biome != null) {
                            if (biome.isTransitionChunk) {
                                Text("Biome: ${biome.primaryTheme.themeName} → ${biome.nextTheme?.themeName} (${(biome.transitionProgress * 100).toInt()}%)", color = Color(0xFFE879F9), fontSize = 10.sp)
                            } else {
                                Text("Biome: ${biome.primaryTheme.themeName}", color = Color(0xFFE879F9), fontSize = 10.sp)
                            }
                        }
                        val ev = engine.eventDirector.activeEvent
                        if (ev != WorldEventType.NONE) {
                            Text("Active Event: ${ev.eventName} (${String.format("%.1f", engine.eventDirector.eventTimer)}s remaining)", color = Color(0xFFFB7185), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        } else {
                            Text("Active Event: None (Cooldown: ${String.format("%.1f", engine.eventDirector.cooldownTimer)}s)", color = Color.Gray, fontSize = 10.sp)
                        }
                        Text("Base Difficulty: Stage ${engine.currentStage} | Effective: ${String.format("%.2f", diffParams.effectiveDifficulty)}", color = Color(0xFFFACC15), fontSize = 10.sp)
                        Text("Adaptive Modifier: ${String.format("%+.2f", diffParams.adaptiveModifier)}", color = Color(0xFFFB7185), fontSize = 10.sp)
                        Text("Validation Status: ${if (engine.chunkManager.totalValidationFailuresCount == 0) "VALIDATED (0 RETRIES)" else "REGENERATED (${engine.chunkManager.totalValidationFailuresCount} RETRIES)"}", color = Color(0xFF4ADE80), fontSize = 10.sp)
                        Text("Generation Time: ${engine.chunkManager.lastGenerationTimeMs} ms | Active Entities: ${engine.currentLevel.platforms.size} plats, ${engine.currentLevel.crystals.size} cr", color = Color.LightGray, fontSize = 9.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("📷 CAMERA DEBUG DATA", color = Color(0xFFF1F5F9), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text("Cam Pos: (${String.format("%.1f", engine.cameraX)}, ${String.format("%.1f", engine.cameraY)})", color = Color(0xFF38BDF8), fontSize = 10.sp)
                        Text("Player Pos: (${String.format("%.1f", engine.player.x)}, ${String.format("%.1f", engine.player.y)})", color = Color(0xFF34D399), fontSize = 10.sp)
                        Text("Cam Target: (${String.format("%.1f", engine.cameraController.lastTargetX)}, ${String.format("%.1f", engine.cameraController.lastTargetY)})", color = Color(0xFFE879F9), fontSize = 10.sp)
                        Text("Cam Bounds: Horiz[${String.format("%.0f", engine.cameraController.minBoundX)}, ${String.format("%.0f", engine.cameraController.maxBoundX)}], Vert[${String.format("%.0f", engine.cameraController.minBoundY)}, ${String.format("%.0f", engine.cameraController.maxBoundY)}]", color = Color.Gray, fontSize = 9.sp)
                        Text(engine.chunkManager.debugSummary, color = Color(0xFFA7F3D0), fontSize = 8.sp)
                    }
                }
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
            onAttackClick = { engine.triggerAttack() },
            dashCooldownFraction = (engine.player.dashCooldownTimer / ShadowHeroEngine.DASH_COOLDOWN).coerceIn(0f, 1f)
        )

        // (AdMob banner moved to Top HUD)

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
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight(0.92f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
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
                            text = "Checkpoint saved • Resume your journey",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Text(
                            text = "❤️ ${engine.lives} / ${engine.maxLives}",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        // 1. WATCH AD — GET 3 LIVES
                        if (!engine.rewardContinueUsed) {
                            Button(
                                onClick = {
                                    engine.adErrorMessage = null
                                    if (com.playwin.ads.RewardedManager.isAdReady(context)) {
                                        activity?.let { act ->
                                            com.playwin.ads.RewardedManager.showAd(
                                                activity = act,
                                                rewardType = com.playwin.ads.RewardType.BLOCK_MASTER_CONTINUE,
                                                callbacks = object : com.playwin.ads.RewardCallback {
                                                    override fun onRewardEarned(rewardType: com.playwin.ads.RewardType, amount: Int, token: String) {
                                                        engine.lives = 3
                                                        engine.rewardContinueUsed = true
                                                        engine.resetToActiveCheckpointOrStart()
                                                        engine.isGameOverDialogOpen = false
                                                        engine.player.isDead = false
                                                        engine.adErrorMessage = null
                                                    }

                                                    override fun onAdFailedToLoad(errorCode: Int, errorMessage: String) {
                                                        engine.adErrorMessage = "Rewarded ad unavailable. Please try again."
                                                    }

                                                    override fun onAdFailedToShow(errorMessage: String) {
                                                        engine.adErrorMessage = "Rewarded ad unavailable. Please try again."
                                                    }

                                                    override fun onAdClosed(userEarnedReward: Boolean) {
                                                        if (!userEarnedReward) {
                                                            engine.adErrorMessage = "Watch the full ad to claim your lives!"
                                                        }
                                                    }
                                                }
                                            )
                                        } ?: run {
                                            engine.adErrorMessage = "System error: Host activity not found."
                                        }
                                    } else {
                                        engine.adErrorMessage = "Ad not ready, please try again."
                                        com.playwin.ads.RewardedManager.preload(context)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎬 WATCH AD — GET 3 LIVES", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    Text("Watch a rewarded ad to restore 3 lives", fontSize = 10.sp, color = Color.LightGray)
                                }
                            }
                        }

                        // Display Ad Error if any
                        engine.adErrorMessage?.let { errMsg ->
                            Text(
                                text = errMsg,
                                color = Color(0xFFF87171),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // 2. RESTART GAME
                        Button(
                            onClick = {
                                ShadowHeroProgressionManager.incrementDeathCount(context)
                                engine.lives = 3
                                engine.rewardContinueUsed = false
                                engine.isProcessingDeath = false
                                engine.restartCurrentStage()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🔄 RESTART GAME", fontWeight = FontWeight.Bold)
                        }

                        // 3. QUIT GAME
                        OutlinedButton(
                            onClick = {
                                exitShadowHero()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🏠 QUIT GAME", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- STAGE COMPLETE MODAL OVERLAY ---
        if (engine.isStageComplete) {
            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_hero"
            var awardedCoins by remember(engine.currentStage, engine.currentLevel.seed) { mutableStateOf<Int?>(null) }
            var doubleRewardClaimed by remember(engine.currentStage, engine.currentLevel.seed) { mutableStateOf(false) }
            var adLoadingOrWatching by remember(engine.currentStage, engine.currentLevel.seed) { mutableStateOf(false) }
            var adErrorMessage by remember(engine.currentStage, engine.currentLevel.seed) { mutableStateOf<String?>(null) }

            LaunchedEffect(engine.isStageComplete, engine.currentStage, engine.currentLevel.seed) {
                if (engine.isStageComplete) {
                    doubleRewardClaimed = ShadowHeroProgressionManager.isDoubleRewardClaimed(
                        context, currentUserId, engine.currentStage, engine.currentLevel.seed
                    )
                    adErrorMessage = null
                    
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
                        totalCrystalsInStage = engine.levelCompletionManager.requiredCrystals,
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
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFF06B6D4)))),
                    modifier = Modifier
                        .widthIn(max = 380.dp)
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.92f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "STAGE CLEAR!",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "Stage ${engine.currentStage} Conquered",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        // Stats & Rewards Summary Box
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
                                        "💎 ${engine.collectedCrystalIds.size} / ${engine.levelCompletionManager.requiredCrystals}",
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

                        // Ad Error Message
                        if (adErrorMessage != null) {
                            Text(
                                text = adErrorMessage!!,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Double Reward Watch Ad Option
                        if (doubleRewardClaimed) {
                            Surface(
                                color = Color(0xFF06B6D4).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF06B6D4)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "✓ DOUBLE REWARD CLAIMED",
                                        color = Color(0xFF22C55E),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (!adLoadingOrWatching && awardedCoins != null) {
                                        if (com.playwin.ads.RewardedManager.isAdReady(context)) {
                                            adLoadingOrWatching = true
                                            adErrorMessage = null
                                            activity?.let { act ->
                                                com.playwin.ads.RewardedManager.showAd(
                                                    activity = act,
                                                    rewardType = com.playwin.ads.RewardType.SHADOW_HERO_DOUBLE_REWARD,
                                                    callbacks = object : com.playwin.ads.RewardCallback {
                                                        override fun onRewardEarned(rewardType: com.playwin.ads.RewardType, amount: Int, token: String) {
                                                            // Award doubled coins
                                                            ShadowHeroProgressionManager.awardDoubleStageReward(
                                                                context = context,
                                                                userId = currentUserId,
                                                                stage = engine.currentStage,
                                                                seed = engine.currentLevel.seed,
                                                                baseRewardAwarded = awardedCoins!!,
                                                                onResult = { success, extraCoins, error ->
                                                                    adLoadingOrWatching = false
                                                                    if (success) {
                                                                        awardedCoins = awardedCoins!! + extraCoins
                                                                        doubleRewardClaimed = true
                                                                        ShadowHeroAudioEngine.playCrystalCollect()
                                                                    } else {
                                                                        adErrorMessage = error ?: "Failed to double coins."
                                                                    }
                                                                }
                                                            )
                                                        }

                                                        override fun onAdFailedToLoad(errorCode: Int, errorMessage: String) {
                                                            adLoadingOrWatching = false
                                                            adErrorMessage = "Ad failed to load. Please try again."
                                                        }

                                                        override fun onAdFailedToShow(errorMessage: String) {
                                                            adLoadingOrWatching = false
                                                            adErrorMessage = "Ad failed to load. Please try again."
                                                        }

                                                        override fun onAdClosed(userEarnedReward: Boolean) {
                                                            adLoadingOrWatching = false
                                                            if (!userEarnedReward) {
                                                                adErrorMessage = "Watch the full ad to claim your double coins!"
                                                            }
                                                        }
                                                    }
                                                )
                                            } ?: run {
                                                adLoadingOrWatching = false
                                                adErrorMessage = "System error: Host activity not found."
                                            }
                                        } else {
                                            adErrorMessage = "Ad not ready, please try again."
                                            com.playwin.ads.RewardedManager.preload(context)
                                        }
                                    }
                                },
                                enabled = !adLoadingOrWatching && awardedCoins != null,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (adLoadingOrWatching) {
                                        CircularProgressIndicator(
                                            color = Color.Black,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("LOADING AD...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    } else {
                                        Text("🎬 WATCH AD — DOUBLE COINS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // PRIMARY ACTION: Next Stage Button (Prominent, ALWAYS VISIBLE & ENABLED)
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22C55E),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("NEXT STAGE", fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("▶", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Secondary Action: Replay Stage
                        OutlinedButton(
                            onClick = { engine.restartCurrentStage() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = BorderStroke(1.dp, Color(0xFF0284C7)),
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("REPLAY STAGE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Tertiary Action: Quit to Menu
                        OutlinedButton(
                            onClick = { exitShadowHero() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("QUIT TO MENU", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight(0.92f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
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
                            onClick = { exitShadowHero() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                            border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🏠", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("QUIT GAME", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
