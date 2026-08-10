package com.myplaywin.app.shadowhero.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.myplaywin.app.shadowhero.audio.ShadowHeroAudioEngine
import com.myplaywin.app.shadowhero.data.ShadowHeroProgressionManager

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadowHeroHomeScreen(
    onPlayClick: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    // Enforce Portrait Mode on Home Screen & Init Audio Engine
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ShadowHeroAudioEngine.init(context)

        onDispose {
            activity?.requestedOrientation = originalOrientation
            ShadowHeroAudioEngine.stopBackgroundMusic()
        }
    }

    // Progression Stats & Settings State
    val stats = remember { ShadowHeroProgressionManager.getStats(context) }
    val isBossAvailable = stats.bestStage >= 10
    var settings by remember { mutableStateOf(ShadowHeroProgressionManager.getSettings(context)) }

    // Dialog Visibility States
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var showHowToPlayDialog by remember { mutableStateOf(false) }
    var showDailyChallengeDialog by remember { mutableStateOf(false) }
    var showMissionsDialog by remember { mutableStateOf(false) }
    var showAbilitiesDialog by remember { mutableStateOf(false) }
    var showPowerUpsDialog by remember { mutableStateOf(false) }
    var showWorldsDialog by remember { mutableStateOf(false) }
    var showBossDialog by remember { mutableStateOf(false) }

    // Selected character animation toggle
    var selectedAnim by remember { mutableStateOf("IDLE") }

    // Pulse transition animation for primary CTA button
    val infiniteTransition = rememberInfiniteTransition(label = "HomePulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseValue"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0616))
    ) {
        // Dark Neon Background Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerTop = Offset(size.width / 2f, size.height * 0.25f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6D28D9).copy(alpha = 0.35f),
                        Color(0xFF3B0764).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = centerTop,
                    radius = size.width * 0.85f
                ),
                center = centerTop,
                radius = size.width * 0.85f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --------------------------------------------------
            // 1. HEADER
            // --------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1038))
                        .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💎", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SHADOW HERO",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("💎", fontSize = 14.sp)
                    }
                    Text(
                        text = "Your warrior in the dark neon world",
                        color = Color(0xFFC084FC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1038))
                        .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --------------------------------------------------
            // 2. CHARACTER HERO SECTION
            // --------------------------------------------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF130926),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF38BDF8))))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left Text & Quick Stats
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("SHADOW", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text("Dark Realm Explorer", color = Color(0xFFC084FC), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💜", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("3/3", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("HEALTH", color = Color.Gray, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💎", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${stats.totalCrystals}", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CRYSTALS", color = Color.Gray, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("100%", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ENERGY", color = Color.Gray, fontSize = 9.sp)
                            }
                        }

                        // Center Showcase Canvas
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1038), CircleShape)
                                .border(
                                    2.dp,
                                    Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF38BDF8))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f

                                withTransform({
                                    scale(1.45f, 1.45f, pivot = Offset(cx, cy))
                                }) {
                                    // Pedestal Glow
                                    drawOval(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFFC084FC), Color(0xFF7C3AED).copy(alpha = 0.5f), Color.Transparent),
                                            center = Offset(cx, cy + 45f),
                                            radius = 50f
                                        ),
                                        topLeft = Offset(cx - 50f, cy + 35f),
                                        size = androidx.compose.ui.geometry.Size(100f, 22f)
                                    )
                                    drawOval(
                                        color = Color(0xFFA855F7),
                                        topLeft = Offset(cx - 40f, cy + 40f),
                                        size = androidx.compose.ui.geometry.Size(80f, 12f),
                                        style = Stroke(width = 2.5f)
                                    )

                                    // Ninja Shadow Head
                                    val headPath = Path().apply {
                                        moveTo(cx, cy - 45f)
                                        lineTo(cx + 10f, cy - 35f)
                                        lineTo(cx + 22f, cy - 40f)
                                        lineTo(cx + 18f, cy - 24f)
                                        lineTo(cx + 26f, cy - 16f)
                                        lineTo(cx + 16f, cy - 6f)
                                        lineTo(cx + 14f, cy + 6f)
                                        lineTo(cx, cy + 10f)
                                        lineTo(cx - 14f, cy + 6f)
                                        lineTo(cx - 16f, cy - 6f)
                                        lineTo(cx - 26f, cy - 16f)
                                        lineTo(cx - 18f, cy - 24f)
                                        lineTo(cx - 22f, cy - 40f)
                                        lineTo(cx - 10f, cy - 35f)
                                        close()
                                    }
                                    drawPath(path = headPath, color = Color(0xFF130528))
                                    drawPath(path = headPath, color = Color(0xFFA855F7), style = Stroke(width = 2f))

                                    // Glowing Eyes
                                    val leftEye = Path().apply {
                                        moveTo(cx - 14f, cy - 16f)
                                        lineTo(cx - 4f, cy - 10f)
                                        lineTo(cx - 6f, cy - 6f)
                                        lineTo(cx - 15f, cy - 11f)
                                        close()
                                    }
                                    val rightEye = Path().apply {
                                        moveTo(cx + 14f, cy - 16f)
                                        lineTo(cx + 4f, cy - 10f)
                                        lineTo(cx + 6f, cy - 6f)
                                        lineTo(cx + 15f, cy - 11f)
                                        close()
                                    }
                                    drawPath(path = leftEye, color = Color(0xFFF472B6))
                                    drawPath(path = rightEye, color = Color(0xFFF472B6))

                                    // Scarf
                                    val scarf = Path().apply {
                                        moveTo(cx - 10f, cy + 8f)
                                        quadraticTo(cx + 20f, cy + 2f, cx + 45f, cy - 8f)
                                        quadraticTo(cx + 38f, cy + 10f, cx + 16f, cy + 14f)
                                        close()
                                    }
                                    drawPath(path = scarf, color = Color(0xFFC084FC))

                                    // Torso
                                    val body = Path().apply {
                                        moveTo(cx - 12f, cy + 12f)
                                        lineTo(cx + 12f, cy + 12f)
                                        lineTo(cx + 15f, cy + 32f)
                                        lineTo(cx - 15f, cy + 32f)
                                        close()
                                    }
                                    drawPath(path = body, color = Color(0xFF0F0520))

                                    // Chest Core Emblem
                                    val core = Path().apply {
                                        moveTo(cx, cy + 16f)
                                        lineTo(cx + 5f, cy + 22f)
                                        lineTo(cx, cy + 28f)
                                        lineTo(cx - 5f, cy + 22f)
                                        close()
                                    }
                                    drawPath(path = core, color = Color(0xFFE879F9))

                                    // Legs
                                    drawRect(color = Color(0xFF0F0520), topLeft = Offset(cx - 12f, cy + 32f), size = androidx.compose.ui.geometry.Size(9f, 14f))
                                    drawRect(color = Color(0xFF0F0520), topLeft = Offset(cx + 3f, cy + 32f), size = androidx.compose.ui.geometry.Size(9f, 14f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Animation Action Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "IDLE" to "🧘",
                            "RUN" to "🏃",
                            "JUMP" to "🏃‍♀️",
                            "DASH" to "⚡"
                        ).forEach { (anim, iconStr) ->
                            val isSelected = selectedAnim == anim
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedAnim = anim },
                                color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF1E1038),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFFE879F9) else Color(0xFF380E54))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(iconStr, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = anim,
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --------------------------------------------------
            // 3. PLAYER PROGRESS (COMPACT CARD)
            // --------------------------------------------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF130926),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current Stage
                    Column(horizontalAlignment = Alignment.Start) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚩", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CURRENT STAGE", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Stage ${stats.stagesCompleted + 1}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))

                    // Best Stage
                    Column(horizontalAlignment = Alignment.Start) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚩", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("BEST STAGE", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Stage ${stats.bestStage}", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))

                    // High Score
                    Column(horizontalAlignment = Alignment.Start) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HIGH SCORE", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(String.format("%,d", stats.highScore), color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))

                    // Energy
                    Column(horizontalAlignment = Alignment.Start) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ENERGY", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("100%", color = Color(0xFF4ADE80), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --------------------------------------------------
            // 4. PRIMARY ACTION (▶ START ADVENTURE)
            // --------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .scale(pulseGlow)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(18.dp),
                        spotColor = Color(0xFF8B5CF6)
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF7C3AED),
                                Color(0xFF0284C7),
                                Color(0xFF6366F1)
                            )
                        )
                    )
                    .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(18.dp))
                    .clickable(onClick = onPlayClick),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "▶", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "START ADVENTURE",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --------------------------------------------------
            // 5. QUICK GAME FEATURES
            // --------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Abilities Quick Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showAbilitiesDialog = true },
                    color = Color(0xFF180A2E),
                    border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF7C3AED),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⚡", fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("ABILITIES", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Upgrade skills", color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                }

                // Power-Ups Quick Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showPowerUpsDialog = true },
                    color = Color(0xFF09162A),
                    border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💎", fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("POWER-UPS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Equip boosts", color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                }

                // Worlds Quick Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showWorldsDialog = true },
                    color = Color(0xFF091D1A),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF059669),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🌎", fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("WORLDS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Explore biomes", color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --------------------------------------------------
            // 6. DAILY / MISSIONS
            // --------------------------------------------------
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showDailyChallengeDialog = true },
                color = Color(0xFF1B0C32),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFA855F7))))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF311054),
                            modifier = Modifier.size(38.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎯", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("DAILY MISSION", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("Stage 5 Challenge", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(2f / 7f)
                                            .background(Color(0xFFE879F9))
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("2 / 7 Completed", color = Color.Gray, fontSize = 9.sp)
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("REWARD", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("+100 🪙", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --------------------------------------------------
            // 7. BOSS
            // --------------------------------------------------
            val isBossAvailable = stats.bestStage >= 10
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showBossDialog = true },
                color = Color(0xFF260B1E),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF450A0A),
                            modifier = Modifier.size(38.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👑", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("SHADOW GUARDIAN", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text(
                                text = if (isBossAvailable) "Ready to Challenge!" else "Boss available at Stage 10",
                                color = if (isBossAvailable) Color(0xFF4ADE80) else Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                        color = if (isBossAvailable) Color(0xFFDC2626) else Color(0xFF3B1522),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Text(
                            text = if (isBossAvailable) "CHALLENGE" else "VIEW",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --------------------------------------------------
            // 8. BOTTOM MENU
            // --------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactMenuButton(
                    modifier = Modifier.weight(1f),
                    iconStr = "📖",
                    title = "How to Play",
                    onClick = { showHowToPlayDialog = true }
                )
                CompactMenuButton(
                    modifier = Modifier.weight(1f),
                    iconStr = "📊",
                    title = "Statistics",
                    onClick = { showStatsDialog = true }
                )
                CompactMenuButton(
                    modifier = Modifier.weight(1f),
                    iconStr = "🏆",
                    title = "Achievements",
                    onClick = { showAchievementsDialog = true }
                )
                CompactMenuButton(
                    modifier = Modifier.weight(1f),
                    iconStr = "⚙️",
                    title = "Settings",
                    onClick = { showSettingsDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ==================================================
        // DIALOG MODALS
        // ==================================================

        // 1. Settings Dialog
        if (showSettingsDialog) {
            Dialog(onDismissRequest = { showSettingsDialog = false }) {
                Surface(
                    color = Color(0xFF170E2B),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFA855F7)),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 380.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title
                        Text(
                            text = "⚙ Game & Audio Settings",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // 1. Sound Effects
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sound Effects",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Switch(
                                    checked = settings.soundEnabled,
                                    onCheckedChange = {
                                        settings = settings.copy(soundEnabled = it)
                                        ShadowHeroAudioEngine.updateSettings(context, settings)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFFA855F7),
                                        uncheckedThumbColor = Color.Gray,
                                        uncheckedTrackColor = Color(0xFF2A1A4A)
                                    ),
                                    modifier = Modifier.scale(0.82f)
                                )
                            }

                            if (settings.soundEnabled) {
                                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                    Text(
                                        text = "SFX Volume: ${(settings.sfxVolume * 100).toInt()}%",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    Slider(
                                        value = settings.sfxVolume,
                                        onValueChange = {
                                            settings = settings.copy(sfxVolume = it)
                                            ShadowHeroAudioEngine.updateSettings(context, settings)
                                        },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFFA855F7),
                                            activeTrackColor = Color(0xFFA855F7),
                                            inactiveTrackColor = Color(0xFF2A1A4A)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.12f),
                            thickness = 1.dp
                        )

                        // 2. Background Music
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Background Music",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Switch(
                                    checked = settings.musicEnabled,
                                    onCheckedChange = {
                                        settings = settings.copy(musicEnabled = it)
                                        ShadowHeroAudioEngine.updateSettings(context, settings)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF38BDF8),
                                        uncheckedThumbColor = Color.Gray,
                                        uncheckedTrackColor = Color(0xFF2A1A4A)
                                    ),
                                    modifier = Modifier.scale(0.82f)
                                )
                            }

                            if (settings.musicEnabled) {
                                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                    Text(
                                        text = "Music Volume: ${(settings.musicVolume * 100).toInt()}%",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    Slider(
                                        value = settings.musicVolume,
                                        onValueChange = {
                                            settings = settings.copy(musicVolume = it)
                                            ShadowHeroAudioEngine.updateSettings(context, settings)
                                        },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF38BDF8),
                                            activeTrackColor = Color(0xFF38BDF8),
                                            inactiveTrackColor = Color(0xFF2A1A4A)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.12f),
                            thickness = 1.dp
                        )

                        // 3. Haptic Feedback
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Haptic Feedback",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Switch(
                                checked = settings.vibrationEnabled,
                                onCheckedChange = {
                                    settings = settings.copy(vibrationEnabled = it)
                                    ShadowHeroAudioEngine.updateSettings(context, settings)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFA855F7),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color(0xFF2A1A4A)
                                ),
                                modifier = Modifier.scale(0.82f)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    ShadowHeroAudioEngine.playButtonClick()
                                    showSettingsDialog = false
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Close",
                                    color = Color(0xFFA855F7),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Statistics Dialog
        if (showStatsDialog) {
            AlertDialog(
                onDismissRequest = { showStatsDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shadow Hero Stats", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatRowItem("Best Stage Reached", "Stage ${stats.bestStage}")
                        StatRowItem("All-Time High Score", "${String.format("%,d", stats.highScore)} pts")
                        StatRowItem("Stages Completed", "${stats.stagesCompleted}")
                        StatRowItem("Total Crystals Collected", "💎 ${stats.totalCrystals}")
                        StatRowItem("Total Dashes Performed", "⚡ ${stats.totalDashes}")
                        StatRowItem("Wall Jumps Executed", "🧗 ${stats.totalWallJumps}")
                        StatRowItem("Checkpoints Activated", "⚓ ${stats.checkpointsActivated}")
                        StatRowItem("Power-Ups Collected", "🔮 ${stats.powerUpsCollected}")
                        StatRowItem("Total Deaths", "💀 ${stats.totalDeaths}")
                        StatRowItem("Matches Played", "${stats.totalMatchesPlayed} Battles")
                        if (stats.bestCompletionTime > 0f) {
                            StatRowItem("Best Clear Time", "⏱️ ${String.format("%.1f", stats.bestCompletionTime)}s")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showStatsDialog = false }) {
                        Text("Got It", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 3. Achievements Dialog
        if (showAchievementsDialog) {
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_hero"
            var currentAchievements by remember { mutableStateOf(ShadowHeroProgressionManager.getAchievements(context)) }
            var isClaiming by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showAchievementsDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏆", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hero Achievements", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentAchievements.forEach { ach ->
                            Surface(
                                color = if (ach.isUnlocked) Color(0xFF241542) else Color(0xFF130A24),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (ach.isUnlocked) Color(0xFFEC4899) else Color.Gray.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = ach.iconEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ach.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = ach.description,
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "+${ach.rewardCoins} 🪙 Coins",
                                            color = Color(0xFFFFD700),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (ach.isClaimed) {
                                        Text(
                                            text = "CLAIMED ✓",
                                            color = Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    } else if (ach.isUnlocked) {
                                        Button(
                                            onClick = {
                                                if (!isClaiming) {
                                                    isClaiming = true
                                                    ShadowHeroProgressionManager.claimAchievementReward(
                                                        context = context,
                                                        userId = userId,
                                                        achievementId = ach.id,
                                                        rewardCoins = ach.rewardCoins,
                                                        onResult = { success, _ ->
                                                            isClaiming = false
                                                            if (success) {
                                                                ShadowHeroAudioEngine.playCrystalCollect()
                                                                currentAchievements = ShadowHeroProgressionManager.getAchievements(context)
                                                            }
                                                        }
                                                    )
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("CLAIM", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    } else {
                                        Text(
                                            text = "LOCKED",
                                            color = Color.Gray.copy(alpha = 0.6f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAchievementsDialog = false }) {
                        Text("Close", color = Color(0xFFEC4899), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 4. How To Play Dialog
        if (showHowToPlayDialog) {
            AlertDialog(
                onDismissRequest = { showHowToPlayDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📖", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("How To Play", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "🗡️ Objective:",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Guide the Shadow Hero through dark neon realms, defeat void monsters, dodge traps, and conquer stage bosses.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🎮 Controls (Landscape):",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "• Left Thumb: Analog D-Pad to Move & Dash\n• Right Thumb: Jump, Attack & Wall Jump",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHowToPlayDialog = false }) {
                        Text("Understood", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 5. Daily Challenge Dialog
        if (showDailyChallengeDialog) {
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_hero"
            var currentChallenge by remember { mutableStateOf(ShadowHeroProgressionManager.getDailyChallenge(context)) }
            var isClaiming by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showDailyChallengeDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎯", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daily Realm Challenge", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color(0xFF23103C),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(currentChallenge.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(currentChallenge.description, color = Color.Gray, fontSize = 12.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Seed: #${currentChallenge.seed}", color = Color(0xFFC084FC), fontSize = 11.sp)
                                    Text("Reward: +${currentChallenge.rewardCoins} 🪙", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        if (currentChallenge.isClaimed) {
                            Text("✓ Challenge Reward Claimed!", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        } else if (currentChallenge.isCompleted) {
                            Button(
                                onClick = {
                                    if (!isClaiming) {
                                        isClaiming = true
                                        ShadowHeroProgressionManager.claimDailyChallengeReward(
                                            context = context,
                                            userId = userId,
                                            onResult = { success, _ ->
                                                isClaiming = false
                                                if (success) {
                                                    ShadowHeroAudioEngine.playCrystalCollect()
                                                    currentChallenge = ShadowHeroProgressionManager.getDailyChallenge(context)
                                                }
                                            }
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("CLAIM ${currentChallenge.rewardCoins} PLAYWIN COINS 🪙", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    showDailyChallengeDialog = false
                                    onPlayClick()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("START DAILY CHALLENGE ▶", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDailyChallengeDialog = false }) {
                        Text("Close", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 6. Abilities Dialog
        if (showAbilitiesDialog) {
            AlertDialog(
                onDismissRequest = { showAbilitiesDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shadow Hero Abilities", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "🧗 WALL JUMP" to "Jump off vertical walls to ascend higher platforms.",
                            "⚡ SHADOW DASH" to "Dash forward instantly to pass hazards safely.",
                            "🦘 DOUBLE JUMP" to "Perform a second leap in mid-air.",
                            "🧗 WALL CLIMB" to "Attach to walls and slide or climb effortlessly.",
                            "🔮 SHADOW INVINCIBILITY" to "Temporary phase dash granting invulnerability."
                        ).forEach { (title, desc) ->
                            Surface(
                                color = Color(0xFF20103A),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(desc, color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAbilitiesDialog = false }) {
                        Text("Close", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 7. Power-Ups Dialog
        if (showPowerUpsDialog) {
            AlertDialog(
                onDismissRequest = { showPowerUpsDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💎", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Power-Ups & Boosts", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "🛡️ SHADOW SHIELD" to "Absorbs one fatal attack or hazard hit.",
                            "⚡ SPEED BOOST" to "Increases movement & dash distance.",
                            "🧲 CRYSTAL MAGNET" to "Attracts nearby crystals automatically.",
                            "⏳ TIME SLOW" to "Slows down trap rotation and enemy moves.",
                            "💎 2X CRYSTAL MULTIPLIER" to "Doubles crystals earned in stage."
                        ).forEach { (title, desc) ->
                            Surface(
                                color = Color(0xFF0F1E36),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(desc, color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPowerUpsDialog = false }) {
                        Text("Close", color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 8. Worlds / Biomes Dialog
        if (showWorldsDialog) {
            AlertDialog(
                onDismissRequest = { showWorldsDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌎", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Explore World Biomes", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "NEON CAVE" to "Deep underground cavern filled with luminous crystals.",
                            "CYBER FACTORY" to "High-tech industrial complex with laser traps.",
                            "FROZEN TEMPLE" to "Ancient icy ruins with slippery platforms.",
                            "LAVA CORE" to "Volcanic zone with molten hazards and fire traps.",
                            "SKY RUINS" to "Floating islands high in the neon atmosphere."
                        ).forEach { (title, desc) ->
                            Surface(
                                color = Color(0xFF0D241D),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(desc, color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showWorldsDialog = false }) {
                        Text("Close", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 9. Boss Dialog
        if (showBossDialog) {
            AlertDialog(
                onDismissRequest = { showBossDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shadow Guardian Boss", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color(0xFF380E1D),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("SHADOW GUARDIAN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text("The ruler of the void realm. Defeat him at Stage 10 to unlock legendary rewards!", color = Color.LightGray, fontSize = 12.sp)
                                Text(
                                    text = if (isBossAvailable) "Status: READY FOR BATTLE" else "Status: Locked (Reach Stage 10)",
                                    color = if (isBossAvailable) Color(0xFF4ADE80) else Color(0xFFF87171),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isBossAvailable) {
                            Button(
                                onClick = {
                                    showBossDialog = false
                                    onPlayClick()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("CHALLENGE BOSS NOW ▶", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBossDialog = false }) {
                        Text("Close", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun CompactMenuButton(
    modifier: Modifier = Modifier,
    iconStr: String,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color(0xFF1B0E33),
        border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(iconStr, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
