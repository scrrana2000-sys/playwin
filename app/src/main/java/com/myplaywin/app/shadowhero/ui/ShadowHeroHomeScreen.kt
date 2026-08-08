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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.shadowhero.audio.ShadowHeroAudioEngine
import com.myplaywin.app.shadowhero.data.ShadowHeroProgressionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadowHeroHomeScreen(
    onPlayClick: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Lock to Portrait Mode while on Home Screen & Init Audio Engine
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
    var settings by remember { mutableStateOf(ShadowHeroProgressionManager.getSettings(context)) }
    val achievements = remember { ShadowHeroProgressionManager.getAchievements(context) }

    // Dialog Visibility States
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var showHowToPlayDialog by remember { mutableStateOf(false) }
    var showDailyChallengeDialog by remember { mutableStateOf(false) }
    var showMissionsDialog by remember { mutableStateOf(false) }

    // Pulse transition animation for play button aura
    val infiniteTransition = rememberInfiniteTransition(label = "HomePulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseValue"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0616))
    ) {
        // Dark Neon Purple Ambient Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6D28D9).copy(alpha = 0.45f),
                        Color(0xFF3B0764).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width * 0.9f
                ),
                center = center,
                radius = size.width * 0.9f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP BAR: Back Button & Version Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1E1038))
                        .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Surface(
                    color = Color(0xFF1E1038),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "v1.0.0 • FUTURE READY",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GAME LOGO & SUBTITLE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SHADOW HERO",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Your warrior in the dark neon world",
                    color = Color(0xFFC084FC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. CHARACTER TURNAROUND SHOWCASE (FRONT, SIDE, BACK) & ANIMATIONS
            var selectedAnim by remember { mutableStateOf("RUN") }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF130926),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Turnaround Models
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // FRONT VIEW
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF090314))
                                    .border(1.5.dp, Color(0xFFC084FC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    // Spiky head
                                    drawCircle(color = Color(0xFF180828), center = Offset(cx, cy - 8f), radius = 22f)
                                    // Glowing Eyes
                                    drawCircle(color = Color(0xFFE879F9), center = Offset(cx - 7f, cy - 8f), radius = 4f)
                                    drawCircle(color = Color(0xFFE879F9), center = Offset(cx + 7f, cy - 8f), radius = 4f)
                                    // Chest Core
                                    drawCircle(color = Color(0xFFE879F9), center = Offset(cx, cy + 10f), radius = 4f)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("FRONT", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // SIDE VIEW
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF090314))
                                    .border(1.5.dp, Color(0xFFC084FC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    // Spiky head
                                    drawCircle(color = Color(0xFF180828), center = Offset(cx, cy - 8f), radius = 22f)
                                    // Single Side Eye
                                    drawCircle(color = Color(0xFFE879F9), center = Offset(cx + 6f, cy - 8f), radius = 4.5f)
                                    // Scarf Tail
                                    drawLine(color = Color(0xFFA855F7), start = Offset(cx - 10f, cy + 2f), end = Offset(cx - 25f, cy + 12f), strokeWidth = 5f)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("SIDE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // BACK VIEW
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF090314))
                                    .border(1.5.dp, Color(0xFFC084FC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    // Spiky head back
                                    drawCircle(color = Color(0xFF180828), center = Offset(cx, cy - 8f), radius = 22f)
                                    // Flowing Scarf Back
                                    drawLine(color = Color(0xFFE879F9), start = Offset(cx, cy - 2f), end = Offset(cx + 18f, cy + 18f), strokeWidth = 6f)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("BACK", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("ANIMATIONS", color = Color(0xFFC084FC), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Animation Toggle Selector Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("IDLE", "RUN", "JUMP", "DOUBLE JUMP", "WALL JUMP", "DASH").forEach { anim ->
                            val isSelected = selectedAnim == anim
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedAnim = anim },
                                color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF1E1038),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFFE879F9) else Color.Transparent)
                            ) {
                                Text(
                                    text = anim,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. ABILITIES & CHARACTER DETAILS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ABILITIES PANEL
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF130926),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ABILITIES", color = Color(0xFFC084FC), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        
                        listOf(
                            "🧗 WALL JUMP" to "Jump on walls to reach higher",
                            "⚡ DASH" to "Dash forward with shadow speed",
                            "🦘 DOUBLE JUMP" to "Jump twice in mid air",
                            "🧗 CLIMB" to "Climb vertical surfaces",
                            "🔮 SHADOW POWER" to "Temporary invincibility dash"
                        ).forEach { (ability, desc) ->
                            Column {
                                Text(ability, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(desc, color = Color.Gray, fontSize = 9.sp)
                            }
                        }
                    }
                }

                // CHARACTER DETAILS & PALETTE
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF130926),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("COLOR PALETTE", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(Color(0xFFA855F7), Color(0xFF38BDF8), Color(0xFFE879F9), Color(0xFF180828)).forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("CHARACTER DETAILS", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text("👤 Name: Shadow", color = Color.White, fontSize = 10.sp)
                        Text("🏃 Role: Explorer", color = Color.White, fontSize = 10.sp)
                        Text("✨ Power: Shadow Energy", color = Color.White, fontSize = 10.sp)
                        Text("🌌 World: Dark Realm", color = Color.White, fontSize = 10.sp)
                        Text("🎯 Goal: Collect energy crystals & escape", color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. BIOMES SELECTOR CARDS
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("ENVIRONMENT BIOMES", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "NEON CAVE" to Color(0xFFA855F7),
                        "CYBER FACTORY" to Color(0xFF06B6D4),
                        "FROZEN TEMPLE" to Color(0xFF38BDF8),
                        "LAVA CORE" to Color(0xFFF97316),
                        "SKY RUINS" to Color(0xFF6366F1)
                    ).forEach { (biome, color) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            color = Color(0xFF180828),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.7f))
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(biome, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. POWER-UPS BAR
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF130926),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("POWER-UPS AVAILABLE", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("🛡️ SHIELD", "⚡ DASH BOOST", "🧲 MAGNET", "⏳ SLOW TIME", "💎 2X CRYSTAL").forEach { item ->
                            Text(item, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. BOSS FIGHT CARD (Shadow Guardian)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayClick() },
                color = Color(0xFF280B3B),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(2.dp, Color(0xFFEF4444))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("BOSS FIGHT", color = Color(0xFFF87171), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text("SHADOW GUARDIAN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        // Boss Health Bar
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF450A0A))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.85f)
                                    .background(Color(0xFFEF4444))
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFDC2626),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "CHALLENGE BOSS",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DAILY CHALLENGE & MISSIONS BANNER ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Daily Challenge Card
                val dailyChallenge = remember { ShadowHeroProgressionManager.getDailyChallenge(context) }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDailyChallengeDialog = true },
                    color = Color(0xFF23103C),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFA855F7))))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🔥 DAILY REALM", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Text("+100 🪙", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Stage 5 Challenge", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (dailyChallenge.isClaimed) "CLAIMED ✓" else if (dailyChallenge.isCompleted) "READY TO CLAIM!" else "IN PROGRESS",
                            color = if (dailyChallenge.isClaimed) Color.Gray else if (dailyChallenge.isCompleted) Color(0xFF4ADE80) else Color(0xFF38BDF8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Daily Missions Card
                val missions = remember { ShadowHeroProgressionManager.getDailyMissions(context) }
                val readyMissionsCount = missions.count { it.isCompleted && !it.isClaimed }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showMissionsDialog = true },
                    color = Color(0xFF131836),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF06B6D4))))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📜 MISSIONS", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            if (readyMissionsCount > 0) {
                                Surface(
                                    color = Color(0xFFEF4444),
                                    shape = CircleShape
                                ) {
                                    Text("$readyMissionsCount", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Text("${missions.count { it.isCompleted }} / ${missions.size} Completed", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Earn PlayWin Coins", color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // QUICK STATS BADGES (Best Stage & High Score)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Best Stage Card
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1B0E33),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🗡️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "BEST STAGE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Stage ${stats.bestStage}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }

                // High Score Card
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1B0E33),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "👑", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "HIGH SCORE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${String.format("%,d", stats.highScore)}", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // MAIN PLAY BUTTON (Glows & Pulses)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .scale(pulseGlow.coerceIn(0.98f, 1.04f))
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFF8B5CF6)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF7C3AED),
                                Color(0xFF0284C7),
                                Color(0xFF6366F1)
                            )
                        )
                    )
                    .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                    .clickable(onClick = onPlayClick),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "▶", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "START ADVENTURE",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECONDARY ACTION BUTTONS GRID (Settings, Statistics, Achievements, How To Play)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // How To Play
                    MenuActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Info,
                        title = "How To Play",
                        accentColor = Color(0xFF38BDF8),
                        onClick = { showHowToPlayDialog = true }
                    )

                    // Statistics
                    MenuActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BarChart,
                        title = "Statistics",
                        accentColor = Color(0xFFFFD700),
                        onClick = { showStatsDialog = true }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Achievements
                    MenuActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.EmojiEvents,
                        title = "Achievements",
                        accentColor = Color(0xFFEC4899),
                        onClick = { showAchievementsDialog = true }
                    )

                    // Settings
                    MenuActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        accentColor = Color(0xFFA855F7),
                        onClick = { showSettingsDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- DIALOG MODALS ---

        // 1. Settings Dialog
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFA855F7))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Game & Audio Settings", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sound Effects", color = Color.White)
                            Switch(
                                checked = settings.soundEnabled,
                                onCheckedChange = {
                                    settings = settings.copy(soundEnabled = it)
                                    ShadowHeroAudioEngine.updateSettings(context, settings)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFA855F7))
                            )
                        }

                        if (settings.soundEnabled) {
                            Column {
                                Text("SFX Volume: ${(settings.sfxVolume * 100).toInt()}%", color = Color.Gray, fontSize = 11.sp)
                                Slider(
                                    value = settings.sfxVolume,
                                    onValueChange = {
                                        settings = settings.copy(sfxVolume = it)
                                        ShadowHeroAudioEngine.updateSettings(context, settings)
                                    },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFFA855F7), activeTrackColor = Color(0xFFA855F7))
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Background Music", color = Color.White)
                            Switch(
                                checked = settings.musicEnabled,
                                onCheckedChange = {
                                    settings = settings.copy(musicEnabled = it)
                                    ShadowHeroAudioEngine.updateSettings(context, settings)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFA855F7))
                            )
                        }

                        if (settings.musicEnabled) {
                            Column {
                                Text("Music Volume: ${(settings.musicVolume * 100).toInt()}%", color = Color.Gray, fontSize = 11.sp)
                                Slider(
                                    value = settings.musicVolume,
                                    onValueChange = {
                                        settings = settings.copy(musicVolume = it)
                                        ShadowHeroAudioEngine.updateSettings(context, settings)
                                    },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF38BDF8))
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Haptic Feedback", color = Color.White)
                            Switch(
                                checked = settings.vibrationEnabled,
                                onCheckedChange = {
                                    settings = settings.copy(vibrationEnabled = it)
                                    ShadowHeroAudioEngine.updateSettings(context, settings)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFA855F7))
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        ShadowHeroAudioEngine.playButtonClick()
                        showSettingsDialog = false
                    }) {
                        Text("Close", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 2. Statistics Dialog
        if (showStatsDialog) {
            AlertDialog(
                onDismissRequest = { showStatsDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFFFFD700))
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
                        StatRowItem("Continue Ads Used", "🎬 ${stats.continueAdsUsed}")
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

        // 3. Achievements Dialog (With PlayWin Coins Claiming)
        if (showAchievementsDialog) {
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_hero"
            var currentAchievements by remember { mutableStateOf(ShadowHeroProgressionManager.getAchievements(context)) }
            var isClaiming by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showAchievementsDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFEC4899))
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
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF38BDF8))
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
                            text = "Guide the Shadow Hero through dark realms, defeat void monsters, dodge deadly traps, and conquer stage bosses.",
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
                            text = "• Left Thumb: Analog D-Pad to Move & Dash\n• Right Thumb: Attack, Shadow Blade & Dodge Skill",
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
                        Text("🔥", fontSize = 20.sp)
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
                                Text("CLAIM 100 PLAYWIN COINS 🪙", fontWeight = FontWeight.Black, fontSize = 13.sp)
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
                                Text("START DAILY CHALLENGING STAGE ▶", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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

        // 6. Missions Dialog
        if (showMissionsDialog) {
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_hero"
            var currentMissions by remember { mutableStateOf(ShadowHeroProgressionManager.getDailyMissions(context)) }
            var claimingMissionId by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showMissionsDialog = false },
                containerColor = Color(0xFF170E2B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📜", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daily Missions", color = Color.White, fontWeight = FontWeight.Bold)
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
                        currentMissions.forEach { mission ->
                            Surface(
                                color = Color(0xFF130D26),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (mission.isCompleted) Color(0xFF38BDF8) else Color.Gray.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(mission.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(mission.description, color = Color.Gray, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("${mission.currentProgress}/${mission.maxProgress}", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("+${mission.rewardCoins} 🪙", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (mission.isClaimed) {
                                        Text("CLAIMED ✓", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    } else if (mission.isCompleted) {
                                        Button(
                                            onClick = {
                                                if (claimingMissionId == null) {
                                                    claimingMissionId = mission.id
                                                    ShadowHeroProgressionManager.claimMissionReward(
                                                        context = context,
                                                        userId = userId,
                                                        missionId = mission.id,
                                                        rewardCoins = mission.rewardCoins,
                                                        onResult = { success, _ ->
                                                            claimingMissionId = null
                                                            if (success) {
                                                                ShadowHeroAudioEngine.playCrystalCollect()
                                                                currentMissions = ShadowHeroProgressionManager.getDailyMissions(context)
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
                                        Text("IN PROGRESS", color = Color.Gray.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMissionsDialog = false }) {
                        Text("Close", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun MenuActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .clickable(onClick = onClick),
        color = Color(0xFF1B0E33),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
