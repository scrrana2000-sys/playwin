package com.myplaywin.app.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.ui.viewmodel.PlayWinViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// --- DATA STRUCTURES ---
data class BounceObstacle(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isSpike: Boolean = false,
    val isMoving: Boolean = false,
    val moveRangeX: Float = 0f,
    val moveRangeY: Float = 0f,
    val moveSpeed: Float = 0.05f,
    val initialX: Float = x,
    val initialY: Float = y
)

data class BounceCollectible(
    val x: Float,
    val y: Float,
    val isStar: Boolean = false,
    var isCollected: Boolean = false
)

data class BounceLevel(
    val number: Int,
    val name: String,
    val description: String,
    val width: Float, // Total scrollable level width
    val height: Float, // Total scrollable level height
    val startX: Float,
    val startY: Float,
    val portalX: Float,
    val portalY: Float,
    val platforms: List<BounceObstacle>,
    val collectibles: List<BounceCollectible>,
    val baseRewardCoins: Int
)

data class BounceHistoryEntry(
    val date: String,
    val levelName: String,
    val stars: Int,
    val coins: Int,
    val score: Int
)

@Composable
fun BounceClassicScreen(
    viewModel: PlayWinViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Local Persistence using SharedPreferences
    val prefs = remember { context.getSharedPreferences("bounce_game_prefs", Context.MODE_PRIVATE) }
    var unlockedLevel by remember { mutableStateOf(prefs.getInt("unlocked_level", 1)) }
    var highestScore by remember { mutableStateOf(prefs.getInt("highest_score", 0)) }
    var historyList by remember {
        mutableStateOf(parseBounceHistory(prefs.getString("bounce_history", "") ?: ""))
    }

    // Level Selection vs Active Game State
    var isPlaying by remember { mutableStateOf(false) }
    var selectedLevelNum by remember { mutableStateOf(1) }

    // Hardcoded level specifications
    val levels = remember {
        listOf(
            BounceLevel(
                number = 1,
                name = "Neon Beginnings",
                description = "Learn the ropes! Master simple jumps & collect gold stars.",
                width = 1800f,
                height = 500f,
                startX = 100f,
                startY = 350f,
                portalX = 1650f,
                portalY = 320f,
                platforms = listOf(
                    // Solid ground blocks
                    BounceObstacle(0f, 420f, 500f, 80f),
                    BounceObstacle(600f, 420f, 400f, 80f),
                    BounceObstacle(1100f, 420f, 700f, 80f),
                    // Floating safe platform
                    BounceObstacle(300f, 300f, 150f, 30f),
                    BounceObstacle(800f, 280f, 150f, 30f),
                    BounceObstacle(1300f, 320f, 180f, 30f),
                    // Dangerous static spikes in pits or platforms
                    BounceObstacle(510f, 450f, 80f, 50f, isSpike = true),
                    BounceObstacle(1010f, 450f, 80f, 50f, isSpike = true),
                    BounceObstacle(1350f, 290f, 30f, 30f, isSpike = true)
                ),
                collectibles = listOf(
                    BounceCollectible(150f, 360f), // Coin
                    BounceCollectible(370f, 240f, isStar = true), // Star
                    BounceCollectible(700f, 360f), // Coin
                    BounceCollectible(870f, 220f, isStar = true), // Star
                    BounceCollectible(1200f, 360f), // Coin
                    BounceCollectible(1390f, 260f, isStar = true), // Star
                    BounceCollectible(1500f, 360f)  // Coin
                ),
                baseRewardCoins = 20
            ),
            BounceLevel(
                number = 2,
                name = "Hazard Heights",
                description = "Dodge spinning orbs & leap across dynamic moving platforms.",
                width = 2200f,
                height = 500f,
                startX = 100f,
                startY = 350f,
                portalX = 2050f,
                portalY = 300f,
                platforms = listOf(
                    // Starting land
                    BounceObstacle(0f, 420f, 350f, 80f),
                    // Floating moving platform (horizontal)
                    BounceObstacle(450f, 320f, 150f, 30f, isMoving = true, moveRangeX = 200f, moveSpeed = 0.03f),
                    // Middle stable checkpoint
                    BounceObstacle(900f, 380f, 250f, 120f),
                    // Vertical moving platform
                    BounceObstacle(1250f, 300f, 140f, 30f, isMoving = true, moveRangeY = 120f, moveSpeed = 0.04f),
                    // Floating high islands
                    BounceObstacle(1500f, 260f, 150f, 30f),
                    BounceObstacle(1750f, 360f, 200f, 30f),
                    BounceObstacle(1950f, 400f, 250f, 100f),
                    // Spikes
                    BounceObstacle(950f, 350f, 40f, 30f, isSpike = true),
                    BounceObstacle(1050f, 350f, 40f, 30f, isSpike = true),
                    BounceObstacle(1550f, 230f, 40f, 30f, isSpike = true),
                    // Ground pits spikes
                    BounceObstacle(350f, 470f, 550f, 30f, isSpike = true),
                    BounceObstacle(1150f, 470f, 800f, 30f, isSpike = true)
                ),
                collectibles = listOf(
                    BounceCollectible(200f, 360f),
                    BounceCollectible(525f, 220f, isStar = true), // Above moving plat
                    BounceCollectible(920f, 320f),
                    BounceCollectible(1000f, 320f, isStar = true),
                    BounceCollectible(1320f, 180f, isStar = true), // High up
                    BounceCollectible(1570f, 200f),
                    BounceCollectible(1850f, 290f, isStar = true),
                    BounceCollectible(2000f, 340f)
                ),
                baseRewardCoins = 40
            ),
            BounceLevel(
                number = 3,
                name = "Abyss of Eternity",
                description = "Extreme trial! High-speed spikes and precise leaps required.",
                width = 2500f,
                height = 500f,
                startX = 100f,
                startY = 350f,
                portalX = 2350f,
                portalY = 280f,
                platforms = listOf(
                    BounceObstacle(0f, 420f, 250f, 80f),
                    // Tricky tiny stepping stones
                    BounceObstacle(350f, 340f, 80f, 30f),
                    BounceObstacle(500f, 270f, 80f, 30f),
                    BounceObstacle(650f, 200f, 80f, 30f),
                    // Horizontal fast platform
                    BounceObstacle(800f, 300f, 120f, 30f, isMoving = true, moveRangeX = 300f, moveSpeed = 0.06f),
                    // High platform with spikes
                    BounceObstacle(1250f, 250f, 300f, 40f),
                    // Falling hazard area
                    BounceObstacle(1650f, 380f, 200f, 40f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.05f),
                    BounceObstacle(1950f, 280f, 150f, 30f),
                    BounceObstacle(2200f, 380f, 300f, 120f),
                    // Spikes galore!
                    BounceObstacle(250f, 470f, 1950f, 30f, isSpike = true), // The whole floor is lava/spikes
                    BounceObstacle(1300f, 220f, 30f, 30f, isSpike = true),
                    BounceObstacle(1400f, 220f, 30f, 30f, isSpike = true),
                    BounceObstacle(2000f, 250f, 40f, 30f, isSpike = true)
                ),
                collectibles = listOf(
                    BounceCollectible(390f, 290f),
                    BounceCollectible(540f, 220f, isStar = true),
                    BounceCollectible(690f, 150f),
                    BounceCollectible(950f, 200f, isStar = true),
                    BounceCollectible(1350f, 180f, isStar = true),
                    BounceCollectible(1450f, 180f),
                    BounceCollectible(1750f, 260f, isStar = true),
                    BounceCollectible(2020f, 220f, isStar = true),
                    BounceCollectible(2250f, 320f)
                ),
                baseRewardCoins = 60
            )
        )
    }

    if (!isPlaying) {
        // --- LEVEL SELECTION SCREEN ---
        Scaffold(
            containerColor = Color(0xFF090615),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D0A1B))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "BOUNCE QUEST",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Box(modifier = Modifier.size(40.dp)) // Placeholder for balance
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF7C3AED).copy(alpha = 0.4f), Color(0xFF090615))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "🟣 Bounce Classic Platformer",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Navigate physics-based traps, collect hidden stars, and reach the final Portal safely to win PlayWin coins!",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Level list header
                item {
                    Text(
                        "CHOOSE YOUR LEVEL",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Level items
                items(levels) { level ->
                    val isLocked = level.number > unlockedLevel
                    val isCurrentUnlocked = level.number <= unlockedLevel

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isLocked) {
                                selectedLevelNum = level.number
                                isPlaying = true
                            },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.2.dp,
                            if (isCurrentUnlocked) Color(0xFF7C3AED).copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f)
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentUnlocked) Color(0xFF13111C) else Color(0xFF13111C).copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Level Badge
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        color = if (isLocked) Color.Gray.copy(alpha = 0.1f) else Color(0xFF7C3AED).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLocked) {
                                    Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(22.dp))
                                } else {
                                    Text(
                                        "Lvl ${level.number}",
                                        color = Color(0xFFA855F7),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = level.name,
                                    color = if (isLocked) Color.Gray else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = level.description,
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (!isLocked) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Recent game history logs for Bounce Game
                item {
                    Text(
                        "📜 RECENT CAMPAIGNS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }

                if (historyList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF13111C), RoundedCornerShape(12.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No campaigns completed yet.\nUnlock levels and earn PlayWin Coins!",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(historyList) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF13111C), RoundedCornerShape(12.dp))
                                .border(0.5.dp, Color(0xFF7C3AED).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(entry.levelName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(entry.date, color = Color.Gray, fontSize = 10.sp)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Stars badges
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(3) { i ->
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (i < entry.stars) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.4f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                // Coins earned
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("+${entry.coins}", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("🪙", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- ACTIVE GAMEPLAY SCREEN ---
        val activeLevel = levels.first { it.number == selectedLevelNum }
        ActiveBounceQuestGame(
            level = activeLevel,
            viewModel = viewModel,
            onBackToMenu = { isPlaying = false },
            onLevelCompleted = { starsCollected, coinsCollected, finalScore ->
                // Mark Level as complete & update unlocked level
                val nextLevel = selectedLevelNum + 1
                if (nextLevel <= levels.size && nextLevel > unlockedLevel) {
                    unlockedLevel = nextLevel
                    prefs.edit().putInt("unlocked_level", nextLevel).apply()
                }

                // Append to history log
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                val currentDateStr = sdf.format(Date())
                val reward = activeLevel.baseRewardCoins + (starsCollected * 10) + coinsCollected
                val entry = BounceHistoryEntry(
                    date = currentDateStr,
                    levelName = activeLevel.name,
                    stars = starsCollected,
                    coins = reward,
                    score = finalScore
                )
                val updated = listOf(entry) + historyList.take(19)
                historyList = updated
                prefs.edit().putString("bounce_history", serializeBounceHistory(updated)).apply()

                isPlaying = false
            }
        )
    }
}

// --- ACTIVE GAMEPLAY LAYOUT & ENGINE ---
@Composable
fun ActiveBounceQuestGame(
    level: BounceLevel,
    viewModel: PlayWinViewModel,
    onBackToMenu: () -> Unit,
    onLevelCompleted: (stars: Int, coins: Int, score: Int) -> Unit
) {
    val context = LocalContext.current

    // Physics Constants
    val ballRadius = 14f
    val gravity = 0.35f
    val jumpForce = -7.5f
    val speedH = 3.5f

    // Game variables
    var ballX by remember { mutableStateOf(level.startX) }
    var ballY by remember { mutableStateOf(level.startY) }
    var ballVx by remember { mutableStateOf(0f) }
    var ballVy by remember { mutableStateOf(0f) }

    var isGrounded by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var gameCompleted by remember { mutableStateOf(false) }

    // Collectibles & obstacles deep-copied states
    val dynamicCollectibles = remember(level) {
        level.collectibles.map { it.copy() }.toMutableStateList()
    }
    val dynamicObstacles = remember(level) {
        level.platforms.map { it.copy() }.toMutableStateList()
    }

    // Performance indicators
    var starsCollected by remember { mutableStateOf(0) }
    var coinsCollected by remember { mutableStateOf(0) }
    var currentScore by remember { mutableStateOf(0) }

    // Control buttons hold flags
    var moveLeftPressed by remember { mutableStateOf(false) }
    var moveRightPressed by remember { mutableStateOf(false) }

    // Animation / Visual updates
    var animationFrame by remember { mutableStateOf(0L) }
    val portalRotation by animateFloatAsState(
        targetValue = if (isPaused) 0f else 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portal_spin"
    )

    // Camera offset for side scrolling
    var cameraX by remember { mutableStateOf(0f) }

    // Reset function
    fun restartActiveGame() {
        ballX = level.startX
        ballY = level.startY
        ballVx = 0f
        ballVy = 0f
        isGrounded = false
        isGameOver = false
        gameCompleted = false
        starsCollected = 0
        coinsCollected = 0
        currentScore = 0
        moveLeftPressed = false
        moveRightPressed = false
        for (i in dynamicCollectibles.indices) {
            dynamicCollectibles[i] = dynamicCollectibles[i].copy(isCollected = false)
        }
    }

    // Physics Engine loop ~60 FPS
    LaunchedEffect(isPaused, isGameOver, gameCompleted) {
        while (!isPaused && !isGameOver && !gameCompleted) {
            delay(16) // roughly 60fps
            animationFrame++

            // Apply horizontal keyboard/touch inputs
            ballVx = if (moveLeftPressed) -speedH else if (moveRightPressed) speedH else 0f

            // Apply gravity
            ballVy += gravity

            // 1. Update moving obstacles first
            for (i in dynamicObstacles.indices) {
                val obs = dynamicObstacles[i]
                if (obs.isMoving) {
                    val theta = (animationFrame * obs.moveSpeed)
                    val newX = obs.initialX + cos(theta) * obs.moveRangeX
                    val newY = obs.initialY + sin(theta) * obs.moveRangeY
                    dynamicObstacles[i] = obs.copy(x = newX, y = newY)
                }
            }

            // 2. Resolve X Axis Collision
            ballX += ballVx
            // Boundary constraints
            if (ballX - ballRadius < 0) ballX = ballRadius
            if (ballX + ballRadius > level.width) ballX = level.width - ballRadius

            for (obs in dynamicObstacles) {
                if (!obs.isSpike && checkCollision(ballX, ballY, ballRadius, obs)) {
                    // Push back on X
                    if (ballVx > 0) {
                        ballX = obs.x - ballRadius
                    } else if (ballVx < 0) {
                        ballX = obs.x + obs.width + ballRadius
                    }
                    ballVx = 0f
                }
            }

            // 3. Resolve Y Axis Collision
            ballY += ballVy
            isGrounded = false

            // Check pit of death (falling out of the bottom)
            if (ballY - ballRadius > level.height) {
                isGameOver = true
            }

            for (obs in dynamicObstacles) {
                if (checkCollision(ballX, ballY, ballRadius, obs)) {
                    if (obs.isSpike) {
                        // Spikes of death!
                        isGameOver = true
                    } else {
                        // Platform solid
                        if (ballVy > 0) {
                            ballY = obs.y - ballRadius
                            ballVy = 0f
                            isGrounded = true
                        } else if (ballVy < 0) {
                            ballY = obs.y + obs.height + ballRadius
                            ballVy = 0f
                        }
                    }
                }
            }

            // 4. Collectibles checking
            for (i in dynamicCollectibles.indices) {
                val item = dynamicCollectibles[i]
                if (!item.isCollected) {
                    val distSq = (ballX - item.x) * (ballX - item.x) + (ballY - item.y) * (ballY - item.y)
                    if (distSq < (ballRadius + 14f) * (ballRadius + 14f)) {
                        dynamicCollectibles[i] = item.copy(isCollected = true)
                        if (item.isStar) {
                            starsCollected++
                            currentScore += 50
                        } else {
                            coinsCollected++
                            currentScore += 10
                        }
                    }
                }
            }

            // 5. Check portal completion
            val pDistSq = (ballX - level.portalX) * (ballX - level.portalX) + (ballY - level.portalY) * (ballY - level.portalY)
            if (pDistSq < (ballRadius + 24f) * (ballRadius + 24f)) {
                gameCompleted = true
            }

            // 6. Camera following logic (smooth follow)
            val idealCameraX = ballX - 300f
            cameraX = idealCameraX.coerceIn(0f, (level.width - 600f).coerceAtLeast(0f))
        }
    }

    Scaffold(
        containerColor = Color(0xFF090615),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0A1B))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Lvl ${level.number}: ${level.name}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("$starsCollected", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("$coinsCollected", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    IconButton(
                        onClick = { isPaused = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF090615)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Screen Size Adaptive Game Arena Box
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF131024), Color(0xFF090615))
                        )
                    )
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight

                // Drawing Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Fit level coordinate space into canvas coordinate space beautifully
                    // 1 level unit = actual layout density proportional fit
                    val scaleX = size.width / 600f // Level fits viewport width standard 600 units
                    val scaleY = size.height / 500f // Level height fits viewport height 500 units

                    withTransform({
                        scale(scaleX, scaleY, pivot = Offset.Zero)
                        translate(-cameraX, 0f) // Horizontal camera pan
                    }) {
                        // Background neon landscape shapes (subtle decoration)
                        drawCircle(
                            color = Color(0xFF7C3AED).copy(alpha = 0.04f),
                            radius = 250f,
                            center = Offset(300f, 200f)
                        )
                        drawCircle(
                            color = Color(0xFF00E5FF).copy(alpha = 0.04f),
                            radius = 180f,
                            center = Offset(1100f, 150f)
                        )

                        // 1. Draw solid safe platform rectangles
                        for (obs in dynamicObstacles) {
                            if (!obs.isSpike) {
                                // Draw beautifully styled platform card block
                                drawRoundRect(
                                    color = Color(0xFF1B1635),
                                    topLeft = Offset(obs.x, obs.y),
                                    size = Size(obs.width, obs.height),
                                    cornerRadius = CornerRadius(10f, 10f)
                                )
                                // Highlight top edge of platforms
                                drawRect(
                                    color = Color(0xFF7C3AED).copy(alpha = 0.7f),
                                    topLeft = Offset(obs.x, obs.y),
                                    size = Size(obs.width, 5f)
                                )
                            } else {
                                // Draw hazardous red/neon spikes
                                val path = Path().apply {
                                    moveTo(obs.x, obs.y + obs.height)
                                    lineTo(obs.x + obs.width / 2f, obs.y)
                                    lineTo(obs.x + obs.width, obs.y + obs.height)
                                    close()
                                }
                                drawPath(path, color = Color(0xFFFF3D00))
                                // Core fire highlight
                                val pathCore = Path().apply {
                                    moveTo(obs.x + obs.width * 0.2f, obs.y + obs.height)
                                    lineTo(obs.x + obs.width / 2f, obs.y + obs.height * 0.3f)
                                    lineTo(obs.x + obs.width * 0.8f, obs.y + obs.height)
                                    close()
                                }
                                drawPath(pathCore, color = Color(0xFFFF9100))
                            }
                        }

                        // 2. Draw active collectibles (Stars & Coins)
                        for (item in dynamicCollectibles) {
                            if (!item.isCollected) {
                                if (item.isStar) {
                                    // Golden star design
                                    drawStar(
                                        cx = item.x,
                                        cy = item.y,
                                        spikes = 5,
                                        outerRadius = 12f,
                                        innerRadius = 5f,
                                        color = Color(0xFFFFD700)
                                    )
                                } else {
                                    // Glowing Gold Coin
                                    drawCircle(
                                        color = Color(0xFF00E5FF),
                                        radius = 10f,
                                        center = Offset(item.x, item.y)
                                    )
                                    drawCircle(
                                        color = Color(0xFF090615),
                                        radius = 6f,
                                        center = Offset(item.x, item.y)
                                    )
                                    drawCircle(
                                        color = Color(0xFF00E5FF),
                                        radius = 3f,
                                        center = Offset(item.x, item.y)
                                    )
                                }
                            }
                        }

                        // 3. Draw Finish Portal (Cyberpunk spiral gateway)
                        drawCircle(
                            color = Color(0xFFFF00D6).copy(alpha = 0.15f),
                            radius = 32f,
                            center = Offset(level.portalX, level.portalY)
                        )
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(Color(0xFF00E5FF), Color(0xFFFF00D6), Color(0xFF00E5FF))
                            ),
                            radius = 24f,
                            center = Offset(level.portalX, level.portalY),
                            style = Stroke(width = 4f)
                        )
                        // Spinning portal core
                        withTransform({
                            rotate(portalRotation, pivot = Offset(level.portalX, level.portalY))
                        }) {
                            drawCircle(
                                color = Color(0xFFFF00D6),
                                radius = 10f,
                                center = Offset(level.portalX, level.portalY)
                            )
                            drawLine(
                                color = Color.White,
                                start = Offset(level.portalX - 22f, level.portalY),
                                end = Offset(level.portalX + 22f, level.portalY),
                                strokeWidth = 3f
                            )
                        }

                        // 4. Draw Ball (Glossy Purple ball with expressive cartoon eyes!)
                        // Roll rotation proportional to X movement
                        val rollAngle = (ballX * 2f) % 360f
                        withTransform({
                            rotate(rollAngle, pivot = Offset(ballX, ballY))
                        }) {
                            // Ball 3D linear gradient body
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFA855F7), Color(0xFF7C3AED), Color(0xFF3B0764)),
                                    center = Offset(ballX - 3f, ballY - 3f),
                                    radius = ballRadius
                                ),
                                radius = ballRadius,
                                center = Offset(ballX, ballY)
                            )

                            // Inner reflective sphere highlights
                            drawCircle(
                                color = Color.White.copy(alpha = 0.5f),
                                radius = 3f,
                                center = Offset(ballX - 5f, ballY - 5f)
                            )

                            // Cute Expressive Eyes (always looking slightly towards heading direction!)
                            val headingOffset = if (ballVx > 0) 3f else if (ballVx < 0) -3f else 0f
                            // Left Eye
                            drawCircle(
                                color = Color.White,
                                radius = 2.8f,
                                center = Offset(ballX - 4f + headingOffset, ballY - 2f)
                            )
                            drawCircle(
                                color = Color.Black,
                                radius = 1.2f,
                                center = Offset(ballX - 4.2f + headingOffset * 1.2f, ballY - 2f)
                            )

                            // Right Eye
                            drawCircle(
                                color = Color.White,
                                radius = 2.8f,
                                center = Offset(ballX + 2f + headingOffset, ballY - 2f)
                            )
                            drawCircle(
                                color = Color.Black,
                                radius = 1.2f,
                                center = Offset(ballX + 1.8f + headingOffset * 1.2f, ballY - 2f)
                            )
                        }
                    }
                }
            }

            // High Fidelity Controls Console Pad at Bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0A1B))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left / Right controls row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Left button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF13111C), CircleShape)
                                .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f), CircleShape)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            moveLeftPressed = true
                                            tryAwaitRelease()
                                            moveLeftPressed = false
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }

                        // Right button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF13111C), CircleShape)
                                .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f), CircleShape)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            moveRightPressed = true
                                            tryAwaitRelease()
                                            moveRightPressed = false
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                    }

                    // Jump controls button (right aligned)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF7C3AED), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable {
                                if (isGrounded) {
                                    ballVy = jumpForce
                                    isGrounded = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JUMP",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // --- PAUSE GAME OVERLAY DIALOG ---
    if (isPaused && !isGameOver && !gameCompleted) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("🎮 GAME PAUSED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text("Your campaign is currently held. Resume when you're ready!", color = Color.Gray)
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Resume Play", color = Color.White)
                    }
                    Button(
                        onClick = { restartActiveGame(); isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B2C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restart Level", color = Color.White)
                    }
                    Button(
                        onClick = onBackToMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Exit to Map", color = Color.Red)
                    }
                }
            }
        )
    }

    // --- GAME OVER OVERLAY DIALOG ---
    if (isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("💥 DEFEAT!", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("The ball collided with traps or fell off into the pit.", color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STARS", color = Color.Gray, fontSize = 11.sp)
                            Text("$starsCollected", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("COINS", color = Color.Gray, fontSize = 11.sp)
                            Text("$coinsCollected", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { restartActiveGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Try Again", color = Color.White)
                    }
                    Button(
                        onClick = onBackToMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B2C)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Exit", color = Color.White)
                    }
                }
            }
        )
    }

    // --- VICTORY GAME OVERLAY DIALOG ---
    if (gameCompleted) {
        // PlayWin rewards claim formula
        val coinsReward = level.baseRewardCoins + (starsCollected * 10) + coinsCollected
        var rewardClaimed by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("🎉 LEVEL COMPLETED!", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Splendid work! Purple Ball reached the finish portal.", color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Big reward box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF7C3AED).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("+$coinsReward", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("🪙", fontSize = 24.sp)
                            }
                            Text("PlayWin Reward Coins", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(30.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STARS", color = Color.Gray, fontSize = 11.sp)
                            Text("$starsCollected / 3", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BONUS SCORE", color = Color.Gray, fontSize = 11.sp)
                            Text("$currentScore", color = Color(0xFFFF00D6), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (!rewardClaimed) {
                                viewModel.addCoins(coinsReward, "Bounce Quest Lvl ${level.number}")
                                rewardClaimed = true
                                android.widget.Toast.makeText(context, "Claimed $coinsReward coins successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !rewardClaimed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (rewardClaimed) "REWARD CLAIMED ✓" else "CLAIM REWARD 🪙",
                            color = if (rewardClaimed) Color.White.copy(alpha = 0.6f) else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onLevelCompleted(starsCollected, coinsCollected, currentScore) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Map Screen", color = Color.White)
                        }
                    }
                }
            }
        )
    }
}

// --- COLLISION SOLVERS ---
private fun checkCollision(bx: Float, by: Float, br: Float, obs: BounceObstacle): Boolean {
    // Closest point on platform to circle center
    val closestX = bx.coerceIn(obs.x, obs.x + obs.width)
    val closestY = by.coerceIn(obs.y, obs.y + obs.height)

    // Distance squared
    val distSq = (bx - closestX) * (bx - closestX) + (by - closestY) * (by - closestY)
    return distSq < (br * br)
}

// Canvas extensions to draw vector star safely
private fun DrawScope.drawStar(
    cx: Float,
    cy: Float,
    spikes: Int,
    outerRadius: Float,
    innerRadius: Float,
    color: Color
) {
    val path = Path()
    var rot = Math.PI / 2 * 3
    val step = Math.PI / spikes

    path.moveTo(cx, cy - outerRadius)
    for (i in 0 until spikes) {
        val x1 = cx + cos(rot).toFloat() * outerRadius
        val y1 = cy + sin(rot).toFloat() * outerRadius
        path.lineTo(x1, y1)
        rot += step

        val x2 = cx + cos(rot).toFloat() * innerRadius
        val y2 = cy + sin(rot).toFloat() * innerRadius
        path.lineTo(x2, y2)
        rot += step
    }
    path.close()
    drawPath(path, color = color)
}

// --- HISTORY SERIALIZER/DESERIALIZER ---
private fun parseBounceHistory(data: String): List<BounceHistoryEntry> {
    if (data.isEmpty()) return emptyList()
    val list = mutableListOf<BounceHistoryEntry>()
    try {
        val records = data.split(";")
        for (r in records) {
            if (r.isEmpty()) continue
            val parts = r.split("|")
            if (parts.size == 5) {
                list.add(
                    BounceHistoryEntry(
                        date = parts[0],
                        levelName = parts[1],
                        stars = parts[2].toIntOrNull() ?: 0,
                        coins = parts[3].toIntOrNull() ?: 0,
                        score = parts[4].toIntOrNull() ?: 0
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

private fun serializeBounceHistory(history: List<BounceHistoryEntry>): String {
    val sb = StringBuilder()
    for (i in history.indices) {
        val e = history[i]
        sb.append(e.date).append("|").append(e.levelName).append("|").append(e.stars).append("|").append(e.coins).append("|").append(e.score)
        if (i < history.size - 1) {
            sb.append(";")
        }
    }
    return sb.toString()
}
