package com.playwin.app.ui.screens

import com.playwin.app.data.model.Quiz
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playwin.app.ui.theme.*
import com.playwin.app.ui.viewmodel.PlayWinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LuckySpinScreen(viewModel: PlayWinViewModel, onBack: () -> Unit) {
    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    val remainingTime by com.playwin.app.data.repository.DailyResetManager.remainingTime.collectAsStateWithLifecycle()
    var rotationAngle by remember { mutableStateOf(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var needleBounceAngle by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResultPopup by remember { mutableStateOf(false) }
    var wonRewardName by remember { mutableStateOf("") }
    var wonRewardType by remember { mutableStateOf("") }
    var wonRewardValue by remember { mutableStateOf("") }
    var showResultTitle by remember { mutableStateOf("") }
    var showResultMessage by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    var showAdLoadingDialog by remember { mutableStateOf(false) }
    var showAdOverlay by remember { mutableStateOf(false) }
    var adSecondsLeft by remember { mutableIntStateOf(3) }

    // Sound and vibration preferences
    val prefs = remember { context.getSharedPreferences("lucky_spin_settings", android.content.Context.MODE_PRIVATE) }
    var isSoundEnabled by remember { mutableStateOf(prefs.getBoolean("sound_enabled", true)) }
    var isVibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }

    val spinWheelConfig by viewModel.spinWheelConfigState.collectAsStateWithLifecycle()
    val pageTitle = remember(spinWheelConfig) { spinWheelConfig.title.ifEmpty { "Lucky Spin" } }

    val spinRewards by viewModel.spinRewardsState.collectAsStateWithLifecycle()
    val activeRewards = remember(spinRewards) {
        spinRewards.filter { it.active }.sortedBy { it.displayOrder }
    }

    val spinsLeft = remember(spinWheelConfig, wallet.dailySpinCount) {
        (spinWheelConfig.dailySpinLimit - wallet.dailySpinCount).coerceAtLeast(0)
    }

    var rewardedAd by remember { mutableStateOf<com.google.android.gms.ads.rewarded.RewardedAd?>(null) }
    var isLoadingAd by remember { mutableStateOf(false) }
    var adWatchedCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(wallet.dailySpinCount, wallet.remainingSpins, isSpinning, rewardedAd, showResultPopup) {
        val currentSpinsLeft = (spinWheelConfig.dailySpinLimit - wallet.dailySpinCount).coerceAtLeast(0)
        val isAdRequired = wallet.dailySpinCount >= spinWheelConfig.dailyFreeSpins
        val btnState = if (isSpinning) "SPINNING" else if (currentSpinsLeft == 0) "TODAYS_SPINS_COMPLETED" else if (isAdRequired) "WATCH_AD" else "SPIN_NOW"
        
        android.util.Log.d("PlayWinDebug", "STATE_REFRESHED")
        android.util.Log.d("PlayWinDebug", "SPINS_LEFT=$currentSpinsLeft")
        android.util.Log.d("PlayWinDebug", "BUTTON_STATE=$btnState")
        android.util.Log.d("PlayWinDebug", "AD_READY=${rewardedAd != null}")
    }

    val adController = remember(activity, isLoadingAd, rewardedAd, adWatchedCallback) {
        object {
            fun loadAd() {
                val act = activity ?: return
                if (isLoadingAd || rewardedAd != null) {
                    if (rewardedAd != null) {
                        android.util.Log.d("PlayWinDebug", "RewardedAd is already available (not null)")
                    }
                    return
                }
                isLoadingAd = true
                android.util.Log.d("PlayWinDebug", "MobileAds initialized: verified on MainActivity onCreate. Initiating load.")
                val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
                val adUnitId = "ca-app-pub-3940256099942544/5224354917"

                com.google.android.gms.ads.rewarded.RewardedAd.load(
                    act,
                    adUnitId,
                    adRequest,
                    object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: com.google.android.gms.ads.rewarded.RewardedAd) {
                            android.util.Log.d("PlayWinDebug", "RewardedAd loaded successfully")
                            isLoadingAd = false
                            rewardedAd = ad
                            android.util.Log.d("PlayWinDebug", "RewardedAd available: true")
                            if (showAdLoadingDialog) {
                                showAdLoadingDialog = false
                                val callback = adWatchedCallback
                                if (callback != null) {
                                    adWatchedCallback = null
                                    showAndPlayAd(callback)
                                }
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                            android.util.Log.e("PlayWinDebug", "Rewarded Ad Failed to load. Complete LoadAdError: $loadAdError, code: ${loadAdError.code}, domain: ${loadAdError.domain}, message: ${loadAdError.message}")
                            isLoadingAd = false
                            rewardedAd = null
                            if (showAdLoadingDialog) {
                                showAdLoadingDialog = false
                                errorMessage = "Failed to load ad. Please try again."
                            }
                            android.util.Log.d("PlayWinDebug", "Reload next RewardedAd scheduled in 5 seconds")
                            coroutineScope.launch {
                                delay(5000)
                                loadAd()
                            }
                        }
                    }
                )
            }

            fun showAndPlayAd(onAdWatched: () -> Unit) {
                val act = activity ?: return
                adWatchedCallback = onAdWatched
                val currentAd = rewardedAd
                if (currentAd != null) {
                    android.util.Log.d("PlayWinDebug", "RewardedAd available: true")
                    android.util.Log.d("PlayWinDebug", "RewardedAd.show() called")
                    showAdLoadingDialog = false
                    currentAd.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            android.util.Log.d("PlayWinDebug", "onAdDismissed() called")
                            rewardedAd = null
                            android.util.Log.d("PlayWinDebug", "Reload next RewardedAd")
                            loadAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                            android.util.Log.e("PlayWinDebug", "RewardedAd failed to show: ${error.message}")
                            rewardedAd = null
                            showAdLoadingDialog = false
                            errorMessage = "Ad failed to show. Preloading another..."
                            android.util.Log.d("PlayWinDebug", "Reload next RewardedAd")
                            loadAd()
                        }
                    }

                    currentAd.show(act, com.google.android.gms.ads.OnUserEarnedRewardListener { rewardItem ->
                        android.util.Log.d("PlayWinDebug", "onUserEarnedReward() triggered")
                        viewModel.grantAdSpinRewardLocally()
                        onAdWatched()
                    })
                } else {
                    android.util.Log.e("PlayWinDebug", "RewardedAd is NULL")
                    showAdLoadingDialog = true
                    loadAd()
                }
            }
        }
    }

    // Premium theme color palette
    val customPalette = listOf(
        Color(0xFF6A1B9A), // Royal Deep Purple
        Color(0xFF8E24AA), // Vibrant Magenta Purple
        Color(0xFF4A148C), // Midnight Indigo Purple
        Color(0xFF7B1FA2), // Orchid Purple
        Color(0xFF3F51B5), // Indigo Accent
        Color(0xFF9C27B0), // Soft Violet Purple
        Color(0xFF512DA8), // Deep Violet
        Color(0xFFE91E63), // Pink Highlight
        Color(0xFF2196F3)  // Cosmic Blue
    )
    val sectorColors = remember(activeRewards) {
        activeRewards.mapIndexed { index, _ ->
            customPalette[index % customPalette.size]
        }
    }
    val sectors = remember(activeRewards) {
        activeRewards.map { it.name }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshUserData()
    }

    LaunchedEffect(activity) {
        if (activity != null) {
            adController.loadAd()
        }
    }

    if (showAdLoadingDialog) {
        AlertDialog(
            onDismissRequest = { showAdLoadingDialog = false },
            confirmButton = {},
            title = {
                Text(
                    text = "🎬 Loading Video Ad...",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFD700),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Preparing your premium video ad...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = Color(0xFF140F26),
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showAdOverlay) {
        AlertDialog(
            onDismissRequest = { /* Force watch to completion */ },
            confirmButton = {},
            title = {
                Text(
                    text = "📺 Watching Premium Ad",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        progress = adSecondsLeft.toFloat() / 3f,
                        color = Color(0xFFFFD700),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sponsor video playing... extra spin in ${adSecondsLeft}s",
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = Color(0xFF140F26),
            shape = RoundedCornerShape(24.dp)
        )

        LaunchedEffect(Unit) {
            adSecondsLeft = 3
            while (adSecondsLeft > 0) {
                delay(1000L)
                adSecondsLeft--
            }
            showAdOverlay = false
            
            viewModel.performSpinWheelTransaction(viewModel.rollSpinRewardDynamic(), isAdSpin = true) { success, errorMsg ->
                if (!success) {
                    errorMessage = errorMsg ?: "Failed to complete extra spin."
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Deep Purple + Gold background with animated star particles
        AnimatedParticleBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = pageTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )

                // Sound toggle button
                IconButton(onClick = {
                    isSoundEnabled = !isSoundEnabled
                    prefs.edit().putBoolean("sound_enabled", isSoundEnabled).apply()
                }) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound Settings",
                        tint = if (isSoundEnabled) Color(0xFFFFD700) else Color.White.copy(alpha = 0.4f)
                    )
                }

                // Vibration toggle button
                IconButton(onClick = {
                    isVibrationEnabled = !isVibrationEnabled
                    prefs.edit().putBoolean("vibration_enabled", isVibrationEnabled).apply()
                }) {
                    Icon(
                        imageVector = if (isVibrationEnabled) Icons.Default.Vibration else Icons.Default.PhoneAndroid,
                        contentDescription = "Vibration Settings",
                        tint = if (isVibrationEnabled) Color(0xFFFFD700) else Color.White.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "Spin the high-roller wheel to unlock fortunes",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (activeRewards.isEmpty()) {
                Spacer(modifier = Modifier.height(48.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140F26)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚙️",
                            fontSize = 56.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "Spin Wheel is temporarily offline",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Please check back later! Our admin is optimizing the rewards.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Glassmorphism Premium Stat Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Card 1: Spins Left Today
                    GlassStatCard(
                        title = "Spins Left",
                        value = "$spinsLeft",
                        icon = "🎯",
                        modifier = Modifier.weight(1f),
                        accentColor = Color(0xFFFF5252)
                    )

                    // Card 2: Total Coins Won
                    GlassStatCard(
                        title = "Coins Won",
                        value = "${wallet.totalSpinRewards}",
                        icon = "🪙",
                        modifier = Modifier.weight(1.1f),
                        accentColor = Color(0xFFFFD700)
                    )

                    // Card 3: Current Streak
                    GlassStatCard(
                        title = "Daily Streak",
                        value = "${wallet.dailyStreak}d",
                        icon = "🔥",
                        modifier = Modifier.weight(1f),
                        accentColor = Color(0xFFFF9F1C)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Large Centered Wheel Container with Pointer
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Large Centered Wheel
                    Box(
                        modifier = Modifier
                            .padding(top = 24.dp) // Offset downward so pointer overlaps top edge
                            .size(290.dp)
                            .shadow(24.dp, CircleShape, clip = false)
                            .rotate(rotationAngle)
                            .clip(CircleShape)
                            .background(Color(0xFF140F26)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = size.width / 2
                            val radius = size.width / 2

                            // Outer gold ring
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA751), Color(0xFFFFD700))
                                ),
                                radius = radius,
                                center = Offset(center, center)
                            )

                            // Dark separator ring
                            drawCircle(
                                color = Color(0xFF0F0C20),
                                radius = radius * 0.94f,
                                center = Offset(center, center)
                            )

                            // Draw segments
                            val sweepAngle = 360f / sectors.size
                            val segmentRadius = radius * 0.92f
                            for (i in sectors.indices) {
                                drawArc(
                                    color = sectorColors[i],
                                    startAngle = i * sweepAngle - 90f,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    size = Size(segmentRadius * 2, segmentRadius * 2),
                                    topLeft = Offset(center - segmentRadius, center - segmentRadius)
                                )
                            }

                            // Separator lines
                            for (i in sectors.indices) {
                                val angleRad = (i * sweepAngle - 90f) * PI / 180f
                                drawLine(
                                    color = Color(0xFF0F0C20).copy(alpha = 0.5f),
                                    start = Offset(center, center),
                                    end = Offset(
                                        (center + segmentRadius * cos(angleRad)).toFloat(),
                                        (center + segmentRadius * sin(angleRad)).toFloat()
                                    ),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }

                            // Outer decorative gold dots (slots/pegs) on the gold rim!
                            for (i in sectors.indices) {
                                val angleRad = (i * sweepAngle - 90f) * PI / 180f
                                val pegRadius = radius * 0.97f
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.White, Color(0xFFFFD700)),
                                        radius = 3.5.dp.toPx()
                                    ),
                                    radius = 3.5.dp.toPx(),
                                    center = Offset(
                                        (center + pegRadius * cos(angleRad)).toFloat(),
                                        (center + pegRadius * sin(angleRad)).toFloat()
                                    )
                                )
                            }

                            // Glossy Lighting overlay over the top half of the wheel
                            drawArc(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                                ),
                                startAngle = -180f,
                                sweepAngle = 180f,
                                useCenter = true,
                                size = Size(segmentRadius * 2, segmentRadius * 2),
                                topLeft = Offset(center - segmentRadius, center - segmentRadius)
                            )

                            // Multi-layered Center Hub
                            // Shadow
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.4f),
                                radius = radius * 0.22f,
                                center = Offset(center, center)
                            )
                            // Outer gold rim for center
                            drawCircle(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA751))
                                ),
                                radius = radius * 0.18f,
                                center = Offset(center, center)
                            )
                            // Inner deep hub
                            drawCircle(
                                color = Color(0xFF140F26),
                                radius = radius * 0.14f,
                                center = Offset(center, center)
                            )
                            // Center golden jewel
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.White, Color(0xFFFFD700), Color(0xFFB8860B)),
                                    center = Offset(center - 2f, center - 2f),
                                    radius = radius * 0.08f
                                ),
                                radius = radius * 0.08f,
                                center = Offset(center, center)
                            )
                        }

                        // Text overlay inside segments
                        sectors.forEachIndexed { index, text ->
                            val sectorAngle = 360f / sectors.size
                            val midAngle = index * sectorAngle + sectorAngle / 2f
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(midAngle),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = text,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(top = 34.dp),
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = Color.Black.copy(alpha = 0.8f),
                                            offset = Offset(0f, 1f),
                                            blurRadius = 2f
                                        )
                                    )
                                )
                            }
                        }
                    }

                    // Golden Pointer pointing down on top, overlapping the wheel slightly!
                    GoldenPointer(
                        needleBounceAngle = needleBounceAngle,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // SPIN NOW Button configuration
                val buttonEnabled = !isSpinning && spinsLeft > 0
                val isAdRequired = wallet.dailySpinCount >= spinWheelConfig.dailyFreeSpins

                fun startSpin(isAdSpin: Boolean) {
                    if (isSpinning) return
                    android.util.Log.d("PlayWinDebug", "SPIN_STARTED")
                    val isAdReq = wallet.dailySpinCount >= spinWheelConfig.dailyFreeSpins
                    android.util.Log.d("PlayWinDebug", "BUTTON_STATE=${if (isAdReq) "WATCH_AD" else "SPIN_NOW"}")
                    val targetSector = viewModel.rollSpinRewardDynamic()
                    if (targetSector != -1) {
                        isSpinning = true
                        if (isSoundEnabled) {
                            SpinSynthPlayer.playSwipeChirp()
                        }
                        coroutineScope.launch {
                            val sectorAngle = 360f / sectors.size
                            val targetAngle = 360f - (targetSector * sectorAngle + sectorAngle / 2f)
                            val extraTurns = 5 * 360f
                            val finalRotationTarget = rotationAngle - (rotationAngle % 360f) + extraTurns + targetAngle

                            var lastTickSegment = -1
                            animate(
                                initialValue = rotationAngle,
                                targetValue = finalRotationTarget,
                                animationSpec = tween(
                                    durationMillis = 4000,
                                    easing = FastOutSlowInEasing
                                )
                            ) { value, _ ->
                                rotationAngle = value
                                val currentSegment = (value / sectorAngle).toInt()
                                if (currentSegment != lastTickSegment) {
                                    lastTickSegment = currentSegment
                                    if (isSoundEnabled) {
                                        SpinSynthPlayer.playTone(1000.0, 15, 0.4f)
                                    }
                                    if (isVibrationEnabled) {
                                        SpinVibrator.vibrate(context, 10L)
                                    }
                                    // Trigger needle bounce
                                    coroutineScope.launch {
                                        animate(0f, -12f, animationSpec = tween(40, easing = LinearEasing)) { v, _ -> needleBounceAngle = v }
                                        animate(-12f, 0f, animationSpec = tween(80, easing = LinearEasing)) { v, _ -> needleBounceAngle = v }
                                    }
                                }
                            }

                            viewModel.performSpinWheelTransaction(targetSector, isAdSpin = isAdSpin) { success, errorMsg ->
                                isSpinning = false
                                if (success) {
                                    android.util.Log.d("PlayWinDebug", "FIREBASE_UPDATED")
                                    android.util.Log.d("PlayWinDebug", "SPIN_COMPLETED")
                                    val selectedReward = activeRewards[targetSector]
                                    wonRewardName = selectedReward.name
                                    wonRewardType = selectedReward.type
                                    wonRewardValue = selectedReward.value

                                    // Play Stopped & Reward Sounds
                                    if (isSoundEnabled) {
                                        when {
                                            selectedReward.type.trim().equals("Retry", ignoreCase = true) -> {
                                                SpinSynthPlayer.playRetryChord()
                                            }
                                            selectedReward.type.trim().equals("Fail", ignoreCase = true) || selectedReward.name.contains("Luck", ignoreCase = true) -> {
                                                SpinSynthPlayer.playFailChord()
                                            }
                                            else -> {
                                                SpinSynthPlayer.playWinChord()
                                                coroutineScope.launch {
                                                    delay(600)
                                                    SpinSynthPlayer.playCoinChime()
                                                }
                                            }
                                        }
                                    }

                                    showResultTitle = if (selectedReward.type.trim().equals("Fail", ignoreCase = true) || selectedReward.name.contains("Luck", ignoreCase = true)) {
                                        "Better Luck Next Time"
                                    } else {
                                        "🎉 Congratulations!"
                                    }
                                    showResultMessage = "You won $wonRewardName"
                                    showResultPopup = true
                                } else {
                                    errorMessage = errorMsg ?: "Transaction failed."
                                }
                            }
                        }
                    } else {
                        isSpinning = false
                        errorMessage = "No active rewards available to spin."
                    }
                }

                Button(
                    onClick = {
                        if (!isSpinning) {
                            if (isAdRequired) {
                                adController.showAndPlayAd {
                                    startSpin(isAdSpin = true)
                                }
                            } else {
                                startSpin(isAdSpin = false)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp))
                        .testTag("spin_now_button"),
                    enabled = buttonEnabled,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color(0xFF2C2C2C)
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (buttonEnabled) {
                                    Modifier.background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFFFE259), Color(0xFFFFA751))
                                        )
                                    )
                                } else {
                                    Modifier.background(Color(0xFF2E2E2E))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (!buttonEnabled && spinsLeft == 0) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Spins Reset in $remainingTime",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            } else {
                                Text(
                                    text = if (isSpinning) "🌀 SPINNING..." else if (isAdRequired) "🎬 WATCH AD & SPIN" else "🎡 SPIN NOW",
                                    color = Color(0xFF130F26),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Weighted details list card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A152E).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🎡 Spinner Probabilities & Rules",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val totalWeight = activeRewards.sumOf { it.probabilityWeight }
                        val validRewards = activeRewards.filter { it.probabilityWeight > 0 }
                        
                        validRewards.forEach { reward ->
                            val percentage = if (totalWeight > 0) {
                                (reward.probabilityWeight.toFloat() / totalWeight * 100).toInt()
                            } else {
                                0
                            }
                            if (percentage > 0) {
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD700))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${reward.name} → $percentage% (${reward.type})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Success win popup
    WinnerCelebrationDialog(
        show = showResultPopup,
        title = showResultTitle,
        rewardName = wonRewardName,
        rewardType = wonRewardType,
        rewardValue = wonRewardValue,
        isSoundEnabled = isSoundEnabled,
        isVibrationEnabled = isVibrationEnabled,
        onDismiss = {
            android.util.Log.d("PlayWinDebug", "REWARD_COLLECTED")
            showResultPopup = false
            viewModel.refreshUserData()
        }
    )

    // Error Alert display dialog
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            confirmButton = {
                Button(
                    onClick = { errorMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            title = {
                Text("Information", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(errorMessage ?: "", color = Color.White.copy(alpha = 0.8f))
            },
            containerColor = Color(0xFF140F26),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun AnimatedParticleBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animState by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )

    val starParticles = remember {
        List(25) {
            Offset(Random.nextFloat(), Random.nextFloat())
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Deep purple luxurious gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F0C20), Color(0xFF1F1138), Color(0xFF0A0714))
            )
        )

        // Draw drifting background ambient gold star particles
        starParticles.forEachIndexed { index, star ->
            val driftY = (star.y + animState) % 1.0f
            val alpha = kotlin.math.sin((animState * 2 * PI) + index).toFloat() * 0.4f + 0.5f
            val starSize = (index % 3 + 1) * 1.5.dp.toPx()

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = alpha.coerceIn(0.1f, 0.9f)),
                radius = starSize,
                center = Offset(star.x * canvasWidth, driftY * canvasHeight)
            )
        }
    }
}

@Composable
fun GlassStatCard(
    title: String,
    value: String,
    icon: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    accentColor: Color
) {
    // Glassmorphism design card with glossy finish
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp), clip = false),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFD700).copy(alpha = 0.02f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                )
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Counter/Value
            AnimatedCounterText(
                value = value,
                accentColor = accentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnimatedCounterText(value: String, accentColor: Color) {
    var displayValue by remember { mutableStateOf(value) }
    
    LaunchedEffect(value) {
        displayValue = value
    }

    Text(
        text = displayValue,
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = accentColor,
        textAlign = TextAlign.Center
    )
}

@Composable
fun GoldenPointer(needleBounceAngle: Float, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    Canvas(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                rotationZ = needleBounceAngle
                transformOrigin = TransformOrigin(0.5f, 0.1f) // Pivot near top center
            }
    ) {
        val width = size.width
        val height = size.height

        // Pointer Path (Elegant downward triangle/shield shape)
        val path = Path().apply {
            moveTo(width * 0.5f, 0f)
            lineTo(width * 0.72f, height * 0.25f)
            lineTo(width * 0.56f, height * 0.85f)
            lineTo(width * 0.5f, height * 1.0f) // Sharp point tip
            lineTo(width * 0.44f, height * 0.85f)
            lineTo(width * 0.28f, height * 0.25f)
            close()
        }

        // Draw pointer shadow
        drawPath(
            path = path,
            color = Color.Black.copy(alpha = 0.4f),
            alpha = 1f
        )

        // Fill with luxurious gold metallic gradient
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF099),
                    Color(0xFFFFD700),
                    Color(0xFFFFA751),
                    Color(0xFFB8860B)
                )
            )
        )

        // Draw inner golden accent line
        val innerPath = Path().apply {
            moveTo(width * 0.5f, height * 0.15f)
            lineTo(width * 0.54f, height * 0.3f)
            lineTo(width * 0.5f, height * 0.85f)
            lineTo(width * 0.46f, height * 0.3f)
            close()
        }
        drawPath(
            path = innerPath,
            color = Color.White.copy(alpha = 0.5f)
        )

        // Glow circle at top pivot
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFFFFD700), Color.Transparent),
                radius = 6.dp.toPx()
            ),
            radius = 6.dp.toPx(),
            center = Offset(width * 0.5f, height * 0.15f)
        )
    }
}

data class CoinParticle(
    var x: Float,
    var y: Float,
    var speedY: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    val size: Float,
    val alpha: Float
)

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var speedX: Float,
    var speedY: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    val color: Color,
    val size: Float
)

@Composable
fun CoinRainOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "coin_rain")
    val animState by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "falling"
    )

    val coins = remember {
        List(30) {
            CoinParticle(
                x = Random.nextFloat(),
                y = -Random.nextFloat() * 400f,
                speedY = Random.nextFloat() * 6f + 4f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 10f - 5f,
                size = Random.nextFloat() * 12f + 16f,
                alpha = Random.nextFloat() * 0.3f + 0.7f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        coins.forEach { coin ->
            // Calculate current falling Y coordinate
            val currentY = (coin.y + (animState * height * 1.5f)) % (height + 100f)
            
            if (currentY > 0) {
                val currentRot = coin.rotation + animState * 360f * (coin.rotationSpeed / 5f)
                
                // Draw a beautiful golden coin with shadow
                drawContext.canvas.save()
                drawContext.canvas.translate(coin.x * width, currentY)
                drawContext.canvas.rotate(currentRot)
                
                // Draw coin outer ring
                drawCircle(
                    color = Color(0xFFB8860B),
                    radius = coin.size,
                    alpha = coin.alpha
                )
                // Draw coin body
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFE259))
                    ),
                    radius = coin.size * 0.85f,
                    alpha = coin.alpha
                )
                // Inner symbol marker
                drawCircle(
                    color = Color(0xFFB8860B),
                    radius = coin.size * 0.4f,
                    alpha = coin.alpha
                )
                
                drawContext.canvas.restore()
            }
        }
    }
}

@Composable
fun ConfettiRainOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti_rain")
    val animState by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "falling_confetti"
    )

    val confettiColors = listOf(
        Color(0xFFFF1744), Color(0xFF00E676), Color(0xFF29B6F6),
        Color(0xFFFFEA00), Color(0xFFD500F9), Color(0xFFFF9100)
    )

    val confettiList = remember {
        List(40) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = -Random.nextFloat() * 300f,
                speedX = Random.nextFloat() * 4f - 2f,
                speedY = Random.nextFloat() * 5f + 3f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 12f - 6f,
                color = confettiColors[Random.nextInt(confettiColors.size)],
                size = Random.nextFloat() * 8f + 10f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        confettiList.forEach { confetti ->
            val currentY = (confetti.y + (animState * height * 1.6f)) % (height + 100f)
            val currentX = (confetti.x * width + kotlin.math.sin(animState * 2 * PI + confetti.y).toFloat() * 30f) % width

            if (currentY > 0) {
                val currentRot = confetti.rotation + animState * 360f * (confetti.rotationSpeed / 5f)

                drawContext.canvas.save()
                drawContext.canvas.translate(currentX, currentY)
                drawContext.canvas.rotate(currentRot)

                // Draw rectangular confetti strip
                drawRect(
                    color = confetti.color,
                    size = Size(confetti.size * 1.5f, confetti.size * 0.6f)
                )

                drawContext.canvas.restore()
            }
        }
    }
}

@Composable
fun WinnerCelebrationDialog(
    show: Boolean,
    title: String,
    rewardName: String,
    rewardType: String,
    rewardValue: String,
    isSoundEnabled: Boolean,
    isVibrationEnabled: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return

    val context = LocalContext.current
    var countedCoins by remember { mutableStateOf(0) }
    val maxReward = remember(rewardValue) {
        rewardValue.toIntOrNull() ?: 0
    }

    // Sound effect trigger
    LaunchedEffect(show) {
        if (show) {
            if (isVibrationEnabled) {
                SpinVibrator.vibrate(context, 150L)
            }
            if (maxReward > 0) {
                // Count up animation
                val step = (maxReward / 20).coerceAtLeast(1)
                for (curr in 0..maxReward step step) {
                    countedCoins = curr
                    delay(30)
                }
                countedCoins = maxReward
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF140F26)),
            contentAlignment = Alignment.Center
        ) {
            // Coin & Confetti Rain inside Dialog
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                ConfettiRainOverlay()
                CoinRainOverlay()
            }

            // Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆",
                    fontSize = 64.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "YOU WON",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (maxReward > 0) {
                    Text(
                        text = "+$countedCoins",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700),
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xFFFFD700).copy(alpha = 0.3f),
                                offset = Offset(0f, 0f),
                                blurRadius = 12f
                            )
                        )
                    )
                    Text(
                        text = "GOLD COINS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        letterSpacing = 1.sp
                    )
                } else {
                    Text(
                        text = rewardName,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp)),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFE259), Color(0xFFFFA751))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "COLLECT NOW 🪙",
                            color = Color(0xFF130F26),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

object SpinVibrator {
    fun vibrate(context: android.content.Context, ms: Long) {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(ms, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(ms)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

object SpinSynthPlayer {
    fun playTone(frequency: Double, durationMs: Int, volume: Float = 0.5f) {
        Thread {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = FloatArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    samples[i] = kotlin.math.sin(2.0 * kotlin.math.PI * frequency * t).toFloat()
                }
                
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    buffer[i] = (samples[i] * Short.MAX_VALUE).toInt().toShort()
                }
                
                val audioTrack = android.media.AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    android.media.AudioTrack.MODE_STATIC
                )
                
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.setVolume(volume)
                audioTrack.play()
                
                android.os.SystemClock.sleep(durationMs.toLong() + 10)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun playSwipeChirp() {
        Thread {
            try {
                val sampleRate = 44100
                val durationMs = 500
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 300.0 + 1500.0 * (t / (durationMs / 1000.0))
                    val sample = kotlin.math.sin(2.0 * kotlin.math.PI * freq * t).toFloat()
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }
                val audioTrack = android.media.AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    android.media.AudioTrack.MODE_STATIC
                )
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.setVolume(0.5f)
                audioTrack.play()
                android.os.SystemClock.sleep(durationMs.toLong() + 10)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun playWinChord() {
        playTone(523.25, 120, 0.4f) // C5
        android.os.SystemClock.sleep(100)
        playTone(659.25, 120, 0.4f) // E5
        android.os.SystemClock.sleep(100)
        playTone(783.99, 120, 0.4f) // G5
        android.os.SystemClock.sleep(100)
        playTone(1046.50, 400, 0.5f) // C6
    }

    fun playFailChord() {
        playTone(392.00, 200, 0.4f) // G4
        android.os.SystemClock.sleep(180)
        playTone(349.23, 200, 0.4f) // F4
        android.os.SystemClock.sleep(180)
        playTone(311.13, 400, 0.5f) // Eb4
    }

    fun playRetryChord() {
        playTone(587.33, 150, 0.4f) // D5
        android.os.SystemClock.sleep(100)
        playTone(880.00, 300, 0.5f) // A5
    }

    fun playCoinChime() {
        playTone(987.77, 100, 0.4f) // B5
        android.os.SystemClock.sleep(80)
        playTone(1318.51, 350, 0.5f) // E6
    }
}

@Composable
fun LuckyScratchScreen(viewModel: PlayWinViewModel, onBack: () -> Unit) {
    LuckyScratchUserScreen(viewModel = viewModel, onBack = onBack)
}

// Particle class for win confetti animation
data class ScratchConfetti(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var size: Float,
    var rotation: Float,
    var rotationSpeed: Float
)

data class ScratchParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    var size: Float,
    var color: Color
)

data class FallingCoin(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    val size: Float,
    var rotation: Float,
    var rotationSpeed: Float
)

object ScratchSoundPlayer {
    private var toneGen: android.media.ToneGenerator? = null
    init {
        try {
            toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            // Ignore
        }
    }
    fun playScratchTick() {
        try {
            toneGen?.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 25)
        } catch (e: Exception) {
            // Ignore
        }
    }
    fun playWinSound() {
        try {
            Thread {
                try {
                    toneGen?.startTone(android.media.ToneGenerator.TONE_DTMF_1, 120)
                    Thread.sleep(120)
                    toneGen?.startTone(android.media.ToneGenerator.TONE_DTMF_5, 120)
                    Thread.sleep(120)
                    toneGen?.startTone(android.media.ToneGenerator.TONE_DTMF_9, 200)
                } catch (e: Exception) {}
            }.start()
        } catch (e: Exception) {
            // Ignore
        }
    }
    fun playCoinBurst() {
        try {
            Thread {
                repeat(4) {
                    try {
                        toneGen?.startTone(android.media.ToneGenerator.TONE_DTMF_S, 80)
                        Thread.sleep(120)
                    } catch (e: Exception) {}
                }
            }.start()
        } catch (e: Exception) {
            // Ignore
        }
    }
    fun playBetterLuckSound() {
        try {
            Thread {
                try {
                    toneGen?.startTone(android.media.ToneGenerator.TONE_DTMF_9, 150)
                    Thread.sleep(150)
                    toneGen?.startTone(android.media.ToneGenerator.TONE_DTMF_5, 150)
                    Thread.sleep(150)
                    toneGen?.startTone(android.media.ToneGenerator.TONE_DTMF_1, 250)
                } catch (e: Exception) {}
            }.start()
        } catch (e: Exception) {
            // Ignore
        }
    }
    fun playCoinCollectionSound() {
        try {
            Thread {
                try {
                    toneGen?.startTone(android.media.ToneGenerator.TONE_DTMF_3, 100)
                    Thread.sleep(100)
                    toneGen?.startTone(android.media.ToneGenerator.TONE_DTMF_7, 100)
                    Thread.sleep(100)
                    toneGen?.startTone(android.media.ToneGenerator.TONE_DTMF_D, 180)
                } catch (e: Exception) {}
            }.start()
        } catch (e: Exception) {
            // Ignore
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuckyScratchUserScreen(viewModel: PlayWinViewModel, onBack: () -> Unit) {
    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    val remainingTime by com.playwin.app.data.repository.DailyResetManager.remainingTime.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUserState.collectAsStateWithLifecycle()
    val settings by viewModel.scratchCardSettingsState.collectAsStateWithLifecycle()
    val rewards by viewModel.scratchCardRewardsState.collectAsStateWithLifecycle()
    val scratchState by viewModel.userScratchCardStateState.collectAsStateWithLifecycle()
    val currentServerTime by com.playwin.app.data.repository.DailyResetManager.currentServerTime.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val coroutineScope = rememberCoroutineScope()

    val buttonInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonPressed) 0.95f else 1f,
        label = "button_scale_anim"
    )

    // Local state variables
    val dragPoints = remember { mutableStateListOf<Offset>() }
    var isProcessing by remember { mutableStateOf(false) }
    var isStarted by remember { mutableStateOf(false) }
    var isFullyScratched by remember { mutableStateOf(false) }
    var scratchTxId by remember { mutableStateOf("") }
    
    // Result reward tracking
    var wonReward by remember { mutableStateOf<com.playwin.app.data.model.FirebaseScratchCardReward?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Ad management states
    var showAdLoadingDialog by remember { mutableStateOf(false) }
    var hasWatchedAdForCurrentCard by remember { mutableStateOf(false) }

    // Dynamic checks
    val scratchesToday = if (scratchState.nextResetTimestamp > 0L && currentServerTime >= scratchState.nextResetTimestamp) {
        0
    } else {
        scratchState.scratchesToday
    }
    val cardsLeft = maxOf(0, settings.dailyScratchLimit - scratchesToday)
    val userLevel = currentUser?.level ?: 1

    val freeScratchUsed = scratchState.freeScratchUsed
    val rewardedScratchUsed = scratchState.rewardedScratchUsed
    val needsAdToUnlock = scratchesToday >= settings.dailyFreeScratch && 
                           settings.rewardedScratchEnabled && 
                           rewardedScratchUsed < settings.maxRewardedScratchPerDay &&
                           scratchesToday < settings.dailyScratchLimit

    // Cooldown verification
    val lastScratchTimestamp = scratchState.lastScratchTimestamp
    val cooldownDurationMs = settings.rewardCooldownMinutes * 60 * 1000L
    val isCooldownActive = lastScratchTimestamp > 0L && currentServerTime < (lastScratchTimestamp + cooldownDurationMs)

    // Live countdown timer state
    val cooldownSecondsLeft = if (isCooldownActive) {
        maxOf(0L, (lastScratchTimestamp + cooldownDurationMs - currentServerTime) / 1000L)
    } else {
        0L
    }

    val nextResetTimestamp = scratchState.nextResetTimestamp
    val isResetTimerActive = nextResetTimestamp > 0L && currentServerTime < nextResetTimestamp
    val resetSecondsLeft = if (isResetTimerActive) {
        maxOf(0L, (nextResetTimestamp - currentServerTime) / 1000L)
    } else {
        0L
    }
    
    val resetHr = resetSecondsLeft / 3600
    val resetMin = (resetSecondsLeft % 3600) / 60
    val resetSec = resetSecondsLeft % 60
    val resetCountdownStr = String.format(java.util.Locale.US, "%02d:%02d:%02d", resetHr, resetMin, resetSec)

    LaunchedEffect(resetSecondsLeft, isResetTimerActive) {
        if (nextResetTimestamp > 0L && currentServerTime >= nextResetTimestamp) {
            viewModel.refreshUserData()
        }
    }

    var rewardedAd by remember { mutableStateOf<com.google.android.gms.ads.rewarded.RewardedAd?>(null) }
    var isLoadingAd by remember { mutableStateOf(false) }
    var adWatchedCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    val adController = remember(activity, isLoadingAd, rewardedAd, adWatchedCallback) {
        object {
            fun loadAd() {
                val act = activity ?: return
                if (isLoadingAd || rewardedAd != null) {
                    return
                }
                isLoadingAd = true
                android.util.Log.d("PlayWinDebug", "Scratch preloading Ad started.")
                val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
                val adUnitId = "ca-app-pub-3940256099942544/5224354917"

                com.google.android.gms.ads.rewarded.RewardedAd.load(
                    act,
                    adUnitId,
                    adRequest,
                    object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: com.google.android.gms.ads.rewarded.RewardedAd) {
                            android.util.Log.d("PlayWinDebug", "Scratch Ad loaded successfully")
                            isLoadingAd = false
                            rewardedAd = ad
                            if (showAdLoadingDialog) {
                                showAdLoadingDialog = false
                                val callback = adWatchedCallback
                                if (callback != null) {
                                    adWatchedCallback = null
                                    showAndPlayAd(callback)
                                }
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                            android.util.Log.e("PlayWinDebug", "Scratch Ad Failed to load: ${loadAdError.message}")
                            isLoadingAd = false
                            rewardedAd = null
                            if (showAdLoadingDialog) {
                                showAdLoadingDialog = false
                                errorMessage = "Failed to load ad. Please try again."
                            }
                            coroutineScope.launch {
                                delay(5000)
                                loadAd()
                            }
                        }
                    }
                )
            }

            fun showAndPlayAd(onAdWatched: () -> Unit) {
                val act = activity ?: return
                adWatchedCallback = onAdWatched
                val currentAd = rewardedAd
                if (currentAd != null) {
                    showAdLoadingDialog = false
                    currentAd.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            loadAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                            rewardedAd = null
                            showAdLoadingDialog = false
                            errorMessage = "Ad failed to show. Preloading another..."
                            loadAd()
                        }
                    }

                    currentAd.show(act, com.google.android.gms.ads.OnUserEarnedRewardListener { rewardItem ->
                        android.util.Log.d("PlayWinDebug", "Scratch onUserEarnedReward() triggered")
                        onAdWatched()
                    })
                } else {
                    showAdLoadingDialog = true
                    loadAd()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        adController.loadAd()
    }

    // Grid scratch area tracker (12x8 grid)
    val cols = 12
    val rows = 8
    val scratchedCells = remember { mutableStateMapOf<Int, Boolean>() }

    val scratchParticles = remember { mutableStateListOf<ScratchParticle>() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(scratchParticles.size) {
        if (scratchParticles.isNotEmpty()) {
            while (scratchParticles.isNotEmpty()) {
                val iterator = scratchParticles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.x += p.vx
                    p.y += p.vy
                    p.vx *= 0.92f
                    p.vy *= 0.92f
                    p.vy += 0.2f // gravity
                    p.alpha -= 0.04f
                    if (p.alpha <= 0f) {
                        iterator.remove()
                    }
                }
                delay(16)
            }
        }
    }

    val cardScale by animateFloatAsState(
        targetValue = if (isFullyScratched) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "card_scale"
    )

    val cardRotation by animateFloatAsState(
        targetValue = if (isFullyScratched) 360f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "card_rotation"
    )

    // Confetti particles state
    val confettiList = remember { mutableStateListOf<ScratchConfetti>() }
    var animateConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(animateConfetti) {
        if (animateConfetti) {
            confettiList.clear()
            val colors = listOf(Color(0xFFFFD700), Color(0xFF00E676), Color(0xFF00E5FF), Color(0xFFE040FB), Color(0xFFFF9100), Color(0xFFFF4081))
            repeat(80) {
                confettiList.add(
                    ScratchConfetti(
                        x = (100..700).random().toFloat(),
                        y = -50f,
                        vx = (-8..8).random().toFloat(),
                        vy = (5..15).random().toFloat(),
                        color = colors.random(),
                        size = (12..32).random().toFloat(),
                        rotation = (0..360).random().toFloat(),
                        rotationSpeed = (-10..10).random().toFloat()
                    )
                )
            }
            while (confettiList.isNotEmpty() && animateConfetti) {
                for (i in confettiList.indices) {
                    val c = confettiList[i]
                    c.x += c.vx
                    c.y += c.vy
                    c.vy += 0.2f // gravity
                    c.rotation += c.rotationSpeed
                }
                confettiList.removeAll { it.y > 2200f || it.x < -100f || it.x > 1200f }
                delay(16)
            }
            animateConfetti = false
        }
    }

    // Glowing animation border around the scratch card frame
    val infiniteTransition = rememberInfiniteTransition(label = "border_glow")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_val"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header panel (No Admin Console button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                }
                Text(
                    text = "Premium Scratch Card",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Scratch the silver holographic safe film to reveal your instant reward!",
                style = MaterialTheme.typography.bodyLarge,
                color = TextWhite.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats row cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Remaining Cards",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$cardsLeft / ${settings.dailyScratchLimit}",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldCoin,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total Coin Wins",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+${wallet.totalScratchRewards} Coins",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Eligibility Screen overlays
            if (!settings.enabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Red)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Disabled", tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Game Disabled", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("The scratch card feature is temporarily disabled by the admin. Please try again later.", color = TextWhite.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                    }
                }
            } else if (userLevel < settings.minimumUserLevel) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryDark.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PrimaryDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Level Locked", tint = PrimaryDark, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Requires Level ${settings.minimumUserLevel}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("You are currently Level $userLevel. Continue completing tasks to level up and unlock this game!", color = TextWhite.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                    }
                }
            } else if (isCooldownActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Cooldown Active", tint = Color(0xFFFF9800), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cooldown Active", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        val minutes = cooldownSecondsLeft / 60
                        val seconds = cooldownSecondsLeft % 60
                        Text(
                            text = String.format("Next card available in %02d:%02d", minutes, seconds),
                            color = GoldCoin,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            } else if (cardsLeft <= 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Daily scratch limit reached!",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scratch Cards Reset in $resetCountdownStr",
                        color = TextWhite.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            } else {
                // Scratch Card Main Frame
                val glowColor = PrimaryDark.copy(alpha = glowIntensity)
                Box(
                    modifier = Modifier
                        .size(width = 300.dp, height = 200.dp)
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                            rotationY = cardRotation
                            cameraDistance = 12f * density
                        }
                        .shadow(24.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardDark)
                        .border(BorderStroke(3.dp, glowColor), RoundedCornerShape(16.dp))
                        .testTag("scratch_card_container"),
                    contentAlignment = Alignment.Center
                ) {
                    // Underneath prize structure
                    wonReward?.let { reward ->
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = reward.icon,
                                fontSize = 54.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "YOU REVEALED",
                                color = TextWhite.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = reward.name,
                                color = try {
                                    Color(android.graphics.Color.parseColor(reward.color))
                                } catch (e: Exception) {
                                    GoldCoin
                                },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    } ?: Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎁",
                            fontSize = 50.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Scratch to Reveal Reward!",
                            color = TextWhite.copy(alpha = 0.6f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Scratch overlay layer
                    if (!isFullyScratched) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = 0.99f }
                                .pointerInput(isProcessing, needsAdToUnlock, hasWatchedAdForCurrentCard, isStarted) {
                                    if (isProcessing || isFullyScratched || !isStarted) return@pointerInput
                                    if (needsAdToUnlock && !hasWatchedAdForCurrentCard) return@pointerInput
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            if (isStarted && !isProcessing) {
                                                dragPoints.add(offset)
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            if (isStarted && !isProcessing && !isFullyScratched) {
                                                val currentPoint = change.position
                                                dragPoints.add(currentPoint)

                                                // Calculate grid cells scratched area
                                                val cellWidth = size.width / cols
                                                val cellHeight = size.height / rows
                                                val col = (currentPoint.x / cellWidth).toInt().coerceIn(0, cols - 1)
                                                val row = (currentPoint.y / cellHeight).toInt().coerceIn(0, rows - 1)
                                                val cellKey = row * cols + col
                                                if (!scratchedCells.containsKey(cellKey)) {
                                                    scratchedCells[cellKey] = true
                                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                                    ScratchSoundPlayer.playScratchTick()
                                                }

                                                // Spawn real silver glitter dust scratch particles!
                                                repeat(4) {
                                                    scratchParticles.add(
                                                        ScratchParticle(
                                                            x = currentPoint.x,
                                                            y = currentPoint.y,
                                                            vx = (-6..6).random().toFloat(),
                                                            vy = (-8..3).random().toFloat(),
                                                            alpha = 1.0f,
                                                            size = (5..12).random().toFloat(),
                                                            color = Color(0xFFCFD8DC)
                                                        )
                                                    )
                                                }

                                                val percentScratched = (scratchedCells.size.toFloat() / (cols * rows)) * 100f
                                                if (percentScratched >= 70f) {
                                                    isFullyScratched = true
                                                    animateConfetti = true
                                                    showResultDialog = true

                                                    // Play premium winning sound based on type
                                                    wonReward?.let { r ->
                                                        if (r.type == "Coins") {
                                                            ScratchSoundPlayer.playCoinBurst()
                                                        } else if (r.type == "Retry Scratch") {
                                                            ScratchSoundPlayer.playWinSound()
                                                        } else {
                                                            ScratchSoundPlayer.playBetterLuckSound()
                                                        }
                                                    }

                                                    // Rich haptic feedback vibration
                                                    try {
                                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                    } catch (ex: Exception) {}
                                                }
                                            }
                                        }
                                    )
                                }
                        ) {
                            // Authentic premium silver holographic gradient
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE0E0E0),
                                        Color(0xFFB0BEC5),
                                        Color(0xFFECEFF1),
                                        Color(0xFFCFD8DC),
                                        Color(0xFF90A4AE)
                                    ),
                                    start = Offset.Zero,
                                    end = Offset(size.width, size.height)
                                )
                            )

                            // Silver holographic grid lines
                            val spacing = 35f
                            for (x in 0..(size.width / spacing).toInt()) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.2f),
                                    start = Offset(x * spacing, 0f),
                                    end = Offset(x * spacing, size.height),
                                    strokeWidth = 2f
                                )
                            }
                            for (y in 0..(size.height / spacing).toInt()) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.2f),
                                    start = Offset(0f, y * spacing),
                                    end = Offset(size.width, y * spacing),
                                    strokeWidth = 2f
                                )
                            }

                            // Erased transparent paths
                            dragPoints.forEach { pt ->
                                drawCircle(
                                    color = Color.Transparent,
                                    radius = 48f,
                                    center = pt,
                                    blendMode = BlendMode.Clear
                                )
                            }
                        }
                    }

                    // Render active scratch particles inside the card
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        scratchParticles.forEach { p ->
                            drawCircle(
                                color = p.color.copy(alpha = p.alpha),
                                radius = p.size,
                                center = Offset(p.x, p.y)
                            )
                        }
                    }

                    // Processing / Securing State loader
                    if (isProcessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = GoldCoin, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Securing reward...", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    } else if (!isStarted) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(
                                    colors = listOf(Color(0xFF2C2C2C), Color(0xFF1C1C1C), Color(0xFF141414))
                                )),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = if (needsAdToUnlock && !hasWatchedAdForCurrentCard) "🎬" else "🎁",
                                    fontSize = 54.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (needsAdToUnlock && !hasWatchedAdForCurrentCard) "WATCH AD TO UNLOCK" else "PREMIUM SCRATCH CARD",
                                    color = GoldCoin,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (needsAdToUnlock && !hasWatchedAdForCurrentCard) "Click WATCH AD & SCRATCH below!" else "Click SCRATCH NOW below to play!",
                                    color = TextWhite.copy(alpha = 0.7f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (cardsLeft <= 0) {
                    // Hide all play buttons, show disabled grey button: "🔒 Scratch Cards Reset in HH:MM:SS"
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(64.dp)
                            .shadow(4.dp, RoundedCornerShape(28.dp))
                            .testTag("scratch_reset_timer_button"),
                        enabled = false,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFF2C2C2C),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        contentPadding = PaddingValues()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🔒 Scratch Cards Reset in",
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = resetCountdownStr,
                                color = GoldCoin,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                } else {
                    val buttonEnabled = when {
                        isProcessing -> false
                        isCooldownActive -> false
                        isStarted && !isFullyScratched -> false
                        else -> true
                    }

                    Button(
                        onClick = {
                            if (isFullyScratched) {
                                dragPoints.clear()
                                scratchedCells.clear()
                                isFullyScratched = false
                                isStarted = false
                                wonReward = null
                            } else if (needsAdToUnlock && !hasWatchedAdForCurrentCard) {
                                adController.showAndPlayAd {
                                    hasWatchedAdForCurrentCard = true
                                    isStarted = true
                                    errorMessage = null
                                    scratchTxId = "scratch_${System.currentTimeMillis()}_${(100000..999999).random()}"
                                    wonReward = viewModel.rollScratchRewardFromFirebase()
                                    dragPoints.clear()
                                    scratchedCells.clear()
                                    android.widget.Toast.makeText(context, "Scratch Card Unlocked!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else if (!isStarted) {
                                isStarted = true
                                errorMessage = null
                                scratchTxId = "scratch_${System.currentTimeMillis()}_${(100000..999999).random()}"
                                wonReward = viewModel.rollScratchRewardFromFirebase()
                                dragPoints.clear()
                                scratchedCells.clear()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(56.dp)
                            .graphicsLayer {
                                scaleX = buttonScale
                                scaleY = buttonScale
                            }
                            .shadow(8.dp, RoundedCornerShape(28.dp))
                            .testTag("scratch_action_button"),
                        enabled = buttonEnabled,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color(0xFF2C2C2C)
                        ),
                        contentPadding = PaddingValues(),
                        interactionSource = buttonInteractionSource
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (buttonEnabled) {
                                        Modifier.background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFFFFE259), Color(0xFFFFA751))
                                            )
                                        )
                                    } else {
                                        Modifier.background(Color(0xFF2E2E2E))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val buttonText = when {
                                    isProcessing -> "⏳ SECURING REWARD..."
                                    isCooldownActive -> "⏳ COOLDOWN ACTIVE"
                                    isFullyScratched -> "🎉 SCRATCH ANOTHER"
                                    isStarted && !isFullyScratched -> "🎨 SWIPE ON THE CARD TO REVEAL"
                                    needsAdToUnlock && !hasWatchedAdForCurrentCard -> "🎬 WATCH AD & SCRATCH"
                                    else -> "🪙 SCRATCH NOW"
                                }
                                
                                val textColor = if (buttonEnabled) Color.White else Color.White.copy(alpha = 0.4f)
                                Text(
                                    text = buttonText,
                                    color = textColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    // Add support text / subtitle below button for distinct states
                    if (needsAdToUnlock && !hasWatchedAdForCurrentCard && !isStarted && !isFullyScratched && cardsLeft > 0 && !isCooldownActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Watch a rewarded ad to unlock one scratch.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    if (isCooldownActive && cardsLeft > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Next card available in ${cooldownSecondsLeft / 60}:${String.format("%02d", cooldownSecondsLeft % 60)}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Rules & Probabilities Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎟️ Scratch Card Pool Probabilities",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldCoin,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val activePool = rewards.filter { it.active }
                    val totalWeight = activePool.sumOf { it.probabilityWeight }

                    if (activePool.isEmpty()) {
                        Text("No active rewards in the pool. Modify in admin console.", color = Color.Gray, fontSize = 13.sp)
                    } else {
                        activePool.forEach { reward ->
                            val pct = if (totalWeight > 0) {
                                maxOf(0.1f, (reward.probabilityWeight.toFloat() / totalWeight * 100f))
                            } else {
                                100f / activePool.size
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(reward.icon, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(reward.name, color = TextWhite.copy(alpha = 0.8f), fontSize = 14.sp, maxLines = 1)
                                }
                                Text(
                                    text = String.format("%.1f%%", pct),
                                    color = try {
                                        Color(android.graphics.Color.parseColor(reward.color))
                                    } catch (e: Exception) {
                                        GoldCoin
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Confetti Canvas layer overlaying the whole screen
        if (animateConfetti) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                confettiList.forEach { c ->
                    drawContext.canvas.save()
                    drawContext.canvas.rotate(c.rotation, c.x, c.y)
                    drawRect(
                        color = c.color,
                        topLeft = Offset(c.x, c.y),
                        size = androidx.compose.ui.geometry.Size(c.size, c.size / 2)
                    )
                    drawContext.canvas.restore()
                }
            }
        }

    // High Fidelity Post-Scratch Premium Reward Dialog
    if (showResultDialog) {
        wonReward?.let { reward ->
            val displayType = when (reward.type) {
                "Coins" -> {
                    if (reward.value.toIntOrNull() != null) "Coins"
                    else {
                        android.util.Log.e("PlayWinScratchDebug", "PARSING FAILURE: Field 'value' with content '${reward.value}' could not be parsed as an Integer for Coins reward ID '${reward.id}'.")
                        "Better Luck Next Time"
                    }
                }
                "Coupon" -> "Coupon"
                "Retry", "Retry Scratch" -> "Retry"
                "Better Luck Next Time" -> "Better Luck Next Time"
                else -> {
                    // Map unknown types gracefully to avoid unexpected state
                    when {
                        (reward.value.toIntOrNull() ?: 0) > 0 -> "Coins"
                        reward.value.isNotEmpty() && reward.value != "0" -> "Coupon"
                        reward.name.lowercase().contains("retry") -> "Retry"
                        else -> "Better Luck Next Time"
                    }
                }
            }

            val fallingCoins = remember { mutableStateListOf<FallingCoin>() }
            var scaleVal by remember { mutableStateOf(0.8f) }
            val animScale by animateFloatAsState(
                targetValue = scaleVal,
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                label = "dialog_scale"
            )
            
            // Shimmer shine animation for coupons/vouchers
            val shimmerTransition = rememberInfiniteTransition(label = "voucher_shimmer")
            val shimmerX by shimmerTransition.animateFloat(
                initialValue = -300f,
                targetValue = 600f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "shimmer_x"
            )

            LaunchedEffect(Unit) {
                scaleVal = 1.0f
                // Spawn physical coin particles exploding
                fallingCoins.clear()
                if (displayType == "Coins") {
                    repeat(25) {
                        fallingCoins.add(
                            FallingCoin(
                                x = 150f + (0..60).random().toFloat(),
                                y = 150f + (0..60).random().toFloat(),
                                vx = (-12..12).random().toFloat(),
                                vy = (-22..-8).random().toFloat(),
                                alpha = 1.0f,
                                size = (15..32).random().toFloat(),
                                rotation = (0..360).random().toFloat(),
                                rotationSpeed = (-12..12).random().toFloat()
                            )
                        )
                    }
                }
            }

            // Gravity loop for falling coins
            LaunchedEffect(fallingCoins.size) {
                if (fallingCoins.isNotEmpty()) {
                    while (fallingCoins.isNotEmpty() && showResultDialog) {
                        val iterator = fallingCoins.iterator()
                        while (iterator.hasNext()) {
                            val c = iterator.next()
                            c.x += c.vx
                            c.y += c.vy
                            c.vy += 1.0f // Gravity acceleration
                            c.rotation += c.rotationSpeed
                            c.alpha -= 0.015f
                            if (c.alpha <= 0f || c.y > 1000f) {
                                iterator.remove()
                            }
                        }
                        delay(16)
                    }
                }
            }

            Dialog(onDismissRequest = { /* Dismiss only on explicit button action */ }) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = animScale
                            scaleY = animScale
                        }
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(2.dp, try {
                            Color(android.graphics.Color.parseColor(reward.color))
                        } catch (e: Exception) {
                            PrimaryDark
                        }),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                when (displayType) {
                                    "Coins" -> {
                                        Text(
                                            text = "🎉 Congratulations!",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = GoldCoin,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        // Glowing ring container
                                        Box(
                                            modifier = Modifier
                                                .size(110.dp)
                                                .background(Color(0xFFFFD700).copy(alpha = 0.15f), RoundedCornerShape(55.dp))
                                                .border(BorderStroke(3.dp, GoldCoin), RoundedCornerShape(55.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🪙", fontSize = 56.sp)
                                        }

                                        Text(
                                            text = "You Won",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextWhite.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            text = "${reward.value} Coins",
                                            style = MaterialTheme.typography.headlineLarge,
                                            color = Color(0xFF00E676),
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            text = "Added Successfully!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextWhite.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    "Coupon" -> {
                                        Text(
                                            text = "🎁 Congratulations!",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color(0xFFE040FB),
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        // Shimmering Coupon container
                                        Box(
                                            modifier = Modifier
                                                .size(width = 180.dp, height = 100.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF263238))
                                                .border(BorderStroke(2.dp, Color(0xFFE040FB)), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Shimmer shine overlay
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                drawRect(
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color.White.copy(alpha = 0.25f),
                                                            Color.Transparent
                                                        ),
                                                        start = Offset(shimmerX, 0f),
                                                        end = Offset(shimmerX + 150f, size.height)
                                                    )
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("🎁", fontSize = 32.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(reward.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                        }

                                        Text(
                                            text = reward.value,
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = Color(0xFF00E5FF),
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            text = "Saved to Coupon Wallet",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextWhite.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    "Retry" -> {
                                        Text(
                                            text = "🔄 Lucky!",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color(0xFF00E5FF),
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(110.dp)
                                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f), RoundedCornerShape(55.dp))
                                                .border(BorderStroke(3.dp, Color(0xFF00E5FF)), RoundedCornerShape(55.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🔄", fontSize = 56.sp)
                                        }

                                        Text(
                                            text = "Extra Scratch",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = TextWhite,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            text = "Added Successfully!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextWhite.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    "Better Luck Next Time" -> {
                                        Text(
                                            text = "😔 Better Luck Next Time",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color.LightGray,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(110.dp)
                                                .background(Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(55.dp))
                                                .border(BorderStroke(3.dp, Color.Gray), RoundedCornerShape(55.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("😔", fontSize = 56.sp)
                                        }

                                        Text(
                                            text = "Try Again!",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                var isCollecting by remember { mutableStateOf(false) }

                                // Collect button
                                Button(
                                    onClick = {
                                        if (isCollecting) return@Button
                                        isCollecting = true
                                        
                                        // Play coin collection sound & trigger vibration
                                        ScratchSoundPlayer.playCoinCollectionSound()
                                        try {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        } catch (ex: Exception) {}

                                        wonReward?.let { r ->
                                            android.util.Log.d("PlayWinScratch", "[UI_CLICK] Collecting reward: ${r.name}, type: ${r.type}, value: ${r.value}, txId: $scratchTxId, isAdScratch: $needsAdToUnlock")
                                            viewModel.performScratchCardTransactionSecure(r, scratchTxId, isAdScratch = needsAdToUnlock) { success, reward, err ->
                                                isCollecting = false
                                                if (success && reward != null) {
                                                    android.util.Log.d("PlayWinScratch", "[UI_SUCCESS] Reward collection committed successfully!")
                                                    android.widget.Toast.makeText(context, "Reward Collected Successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                                    showResultDialog = false
                                                    dragPoints.clear()
                                                    scratchedCells.clear()
                                                    isFullyScratched = false
                                                    isStarted = false
                                                    wonReward = null
                                                    hasWatchedAdForCurrentCard = false // reset ad watch state for the next card!
                                                } else {
                                                    android.util.Log.e("PlayWinScratch", "[UI_FAILURE] Reward collection failed: $err")
                                                    errorMessage = err ?: "Failed to process reward transaction."
                                                }
                                            }
                                        } ?: run {
                                            isCollecting = false
                                            showResultDialog = false
                                            dragPoints.clear()
                                            scratchedCells.clear()
                                            isFullyScratched = false
                                            isStarted = false
                                            wonReward = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = try {
                                            Color(android.graphics.Color.parseColor(reward.color))
                                        } catch (e: Exception) {
                                            GoldCoin
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("collect_reward_btn"),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    if (isCollecting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = CardDark,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = if (displayType == "Better Luck Next Time") "TRY AGAIN" else "COLLECT REWARD",
                                            color = CardDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        showResultDialog = false
                                        dragPoints.clear()
                                        scratchedCells.clear()
                                        isFullyScratched = false
                                        isStarted = false
                                        wonReward = null
                                        onBack()
                                    },
                                    border = BorderStroke(1.dp, Color.Gray),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                                    modifier = Modifier.fillMaxWidth().testTag("home_button"),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text("Go Home")
                                }

                                TextButton(
                                    onClick = {
                                        showResultDialog = false
                                        dragPoints.clear()
                                        scratchedCells.clear()
                                        isFullyScratched = false
                                        isStarted = false
                                        wonReward = null
                                        android.widget.Toast.makeText(context, "Redirecting to History Logs...", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("history_button")
                                ) {
                                    Text("View Transaction History", color = GoldCoin, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (fallingCoins.isNotEmpty()) {
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    fallingCoins.forEach { coin ->
                                        drawCircle(
                                            color = Color(0xFFFFD700).copy(alpha = coin.alpha),
                                            radius = coin.size / 2f,
                                            center = Offset(coin.x, coin.y)
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
}

    // Error Alert Dialog
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            confirmButton = {
                Button(
                    onClick = { errorMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            title = { Text("Information", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage ?: "", color = Color.White.copy(alpha = 0.8f)) },
            containerColor = CardDark,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showAdLoadingDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            text = {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = GoldCoin)
                    Text("Loading Reward Ad...", color = Color.White, fontWeight = FontWeight.Medium)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/* DELETED LEFTOVER START
    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    val dragPoints = remember { mutableStateListOf<Offset>() }
    var isProcessing by remember { mutableStateOf(false) }
    var isStarted by remember { mutableStateOf(false) }
    var isFullyScratched by remember { mutableStateOf(false) }
    var wonLabel by remember { mutableStateOf("") }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResultPopup by remember { mutableStateOf(false) }
    var showResultTitle by remember { mutableStateOf("") }
    var showResultMessage by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    var showAdLoadingDialog by remember { mutableStateOf(false) }
    var showAdOverlay by remember { mutableStateOf(false) }
    var adSecondsLeft by remember { mutableStateOf(0) }

    val scratchRewardsList = remember {
        listOf(
            "+5 Coins",              // Index 0
            "+10 Coins",             // Index 1
            "+20 Coins",             // Index 2
            "+30 Coins",             // Index 3
            "+50 Coins",             // Index 4
            "+100 Coins",            // Index 5
            "+200 Coins"             // Index 6
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App top header bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            Text(
                text = "Premium Scratch Card",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Headline summary
        Text(
            text = "Scratch the silver holographic safe film to reveal your instant coin reward!",
            style = MaterialTheme.typography.bodyLarge,
            color = TextWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Multi-column Dashboard metrics panels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cards Left Today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val cardsLeft = if (wallet.freeScratchUsed && wallet.rewardAdScratchUsed) 0 else if (wallet.freeScratchUsed) 1 else 2
                    Text(
                        text = "$cardsLeft / 2",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldCoin,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Scratch Wins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+${wallet.totalScratchRewards} Coins",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Scratch Card Frame Outer
        Box(
            modifier = Modifier
                .size(width = 280.dp, height = 180.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .testTag("scratch_card_container"),
            contentAlignment = Alignment.Center
        ) {
            // Underneath prize structure
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Reward Icon",
                    tint = GoldCoin,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (wonLabel.isNotEmpty()) {
                    Text(
                        text = "YOU REVEALED",
                        color = TextWhite.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = wonLabel,
                        color = GoldCoin,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Scratch to Reveal!",
                        color = TextWhite.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Scratch overlay layer
            if (!isFullyScratched) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.99f }
                        .pointerInput(isProcessing, wallet.remainingScratchCards) {
                            if (isProcessing || wallet.remainingScratchCards <= 0 || isFullyScratched) return@pointerInput
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (!isStarted && !isProcessing) {
                                        isProcessing = true
                                        isStarted = true
                                        errorMessage = null
                                        val isAdScratch = wallet.freeScratchUsed
                                        viewModel.performScratchCardTransaction(isAdScratch) { success, rewardIndex, err ->
                                            if (success && rewardIndex in scratchRewardsList.indices) {
                                                wonLabel = scratchRewardsList[rewardIndex]
                                                isProcessing = false
                                            } else {
                                                errorMessage = err ?: "Failed to scratch."
                                                isProcessing = false
                                                isStarted = false
                                            }
                                        }
                                    }
                                    if (isStarted && !isProcessing) {
                                        dragPoints.add(offset)
                                    }
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    if (isStarted && !isProcessing && !isFullyScratched) {
                                        val currentPoint = change.position
                                        dragPoints.add(currentPoint)
                                        
                                        // Once scratched sufficiently, auto reveal!
                                        if (dragPoints.size > 26) {
                                            isFullyScratched = true
                                            showResultTitle = "🎉 Congratulations!"
                                            showResultMessage = "You won $wonLabel"
                                            showResultPopup = true
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    // Draw grey premium scratch texture
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFCFD8DC), Color(0xFF90A4AE), Color(0xFF78909C)),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height)
                        )
                    )

                    // Draw faint grid alignment lines
                    val spacing = 35f
                    for (x in 0..(size.width / spacing).toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.12f),
                            start = Offset(x * spacing, 0f),
                            end = Offset(x * spacing, size.height),
                            strokeWidth = 2f
                        )
                    }
                    for (y in 0..(size.height / spacing).toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.12f),
                            start = Offset(0f, y * spacing),
                            end = Offset(size.width, y * spacing),
                            strokeWidth = 2f
                        )
                    }

                    // Circle paths erased by dragging
                    dragPoints.forEach { pt ->
                        drawCircle(
                            color = Color.Transparent,
                            radius = 45f,
                            center = pt,
                            blendMode = BlendMode.Clear
                        )
                    }
                }

                // If loading or processing, display a safe overlay secure message
                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GoldCoin, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Securing reward...",
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (!isStarted && wallet.remainingScratchCards > 0) {
                    // Instruct text sitting on card front
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Safe Lock Icon",
                                tint = PrimaryDark,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Swipe/Scratch Here!",
                                color = PrimaryDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Reset or action buttons
        if (isFullyScratched) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        dragPoints.clear()
                        isFullyScratched = false
                        isStarted = false
                        wonLabel = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldCoin),
                    modifier = Modifier
                        .width(220.dp)
                        .height(48.dp)
                        .testTag("scratch_another_button"),
                    enabled = wallet.remainingScratchCards > 0,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "SCRATCH ANOTHER",
                        color = PrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (wallet.remainingScratchCards <= 0 && wallet.freeScratchUsed && !wallet.rewardAdScratchUsed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AdRewardButton(
                        activity = activity,
                        onUnlocked = {
                            viewModel.unlockAdScratch { success, errorMsg ->
                                if (success) {
                                    dragPoints.clear()
                                    isFullyScratched = false
                                    isStarted = false
                                    wonLabel = ""
                                    errorMessage = null
                                } else {
                                    errorMessage = errorMsg ?: "Failed to unlock extra scratch."
                                }
                            }
                        },
                        onAdFailed = { err ->
                            errorMessage = err
                        },
                        showLoading = { showAdLoadingDialog = it },
                        showOverlay = { showAdOverlay = it },
                        setSecondsLeft = { adSecondsLeft = it }
                    )
                }
            }
        } else {
            if (wallet.remainingScratchCards <= 0) {
                if (wallet.freeScratchUsed && !wallet.rewardAdScratchUsed) {
                    AdRewardButton(
                        activity = activity,
                        onUnlocked = {
                            viewModel.unlockAdScratch { success, errorMsg ->
                                if (success) {
                                    dragPoints.clear()
                                    isFullyScratched = false
                                    isStarted = false
                                    wonLabel = ""
                                    errorMessage = null
                                } else {
                                    errorMessage = errorMsg ?: "Failed to unlock extra scratch."
                                }
                            }
                        },
                        onAdFailed = { err ->
                            errorMessage = err
                        },
                        showLoading = { showAdLoadingDialog = it },
                        showOverlay = { showAdOverlay = it },
                        setSecondsLeft = { adSecondsLeft = it }
                    )
                } else {
                    Text(
                        text = "Today's Scratch Cards Finished\nReset in $remainingTime",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                Text(
                    text = "Swipe/Scratch Here!",
                    color = GoldCoin,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Weighted details ratio list card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🎟️ Scratch Cards Probabilities & Rules",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldCoin,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                val probList = listOf(
                    "+5 Coins → 35%",
                    "+10 Coins → 25%",
                    "+20 Coins → 20%",
                    "+30 Coins → 10%",
                    "+50 Coins → 7%",
                    "+100 Coins → 2% (Rare)",
                    "+200 Coins → 1% (Ultra Rare)"
                )

                probList.forEach { line ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GoldCoin)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    // Success Reward Alert dialog
    if (showResultPopup) {
        AlertDialog(
            onDismissRequest = { showResultPopup = false },
            confirmButton = {
                Button(
                    onClick = { showResultPopup = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldCoin)
                ) {
                    Text("Awesome!", color = PrimaryDark, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = showResultTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = showResultMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextWhite.copy(alpha = 0.8f)
                )
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Error message Alert dialog
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            confirmButton = {
                Button(
                    onClick = { errorMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            title = {
                Text("Information", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(errorMessage ?: "", color = Color.White.copy(alpha = 0.8f))
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showAdOverlay) {
        AlertDialog(
            onDismissRequest = { /* Force watch to completion */ },
            confirmButton = {},
            title = {
                Text("📺 Watching Rewarded Ad", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        progress = adSecondsLeft.toFloat() / 3f,
                        color = GoldCoin,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your ad is playing... extra scratch in ${adSecondsLeft}s",
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(24.dp)
        )

        LaunchedEffect(Unit) {
            adSecondsLeft = 3
            while (adSecondsLeft > 0) {
                delay(1000L)
                adSecondsLeft--
            }
            showAdOverlay = false
            viewModel.unlockAdScratch { success, errorMsg ->
                if (success) {
                    dragPoints.clear()
                    isFullyScratched = false
                    isStarted = false
                    wonLabel = ""
                    errorMessage = null
                } else {
                    errorMessage = errorMsg ?: "Failed to unlock extra scratch."
                }
            }
        }
    }

    if (showAdLoadingDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            text = {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = GoldCoin)
                    Text("Loading Reward Ad...", color = Color.White, fontWeight = FontWeight.Medium)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
DELETED LEFTOVER END */

@Composable
fun AdRewardButton(
    activity: android.app.Activity?,
    onUnlocked: () -> Unit,
    onAdFailed: (String) -> Unit,
    showLoading: (Boolean) -> Unit,
    showOverlay: (Boolean) -> Unit,
    setSecondsLeft: (Int) -> Unit
) {
    Button(
        onClick = {
            if (activity != null) {
                showLoading(true)
                val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
                val adUnitId = "ca-app-pub-3940256099942544/5224354917" // AdMob Test Rewarded Ad Unit ID
                
                com.google.android.gms.ads.rewarded.RewardedAd.load(
                    activity,
                    adUnitId,
                    adRequest,
                    object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: com.google.android.gms.ads.rewarded.RewardedAd) {
                            showLoading(false)
                            var earnedReward = false
                            
                            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    android.util.Log.d("PlayWinAds", "Scratch Ad dismissed")
                                    if (earnedReward) {
                                        onUnlocked()
                                    } else {
                                        onAdFailed("Ad skipped or closed early. No scratch unlocked.")
                                    }
                                }
                                
                                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                    android.util.Log.e("PlayWinAds", "Failed to show scratch ad: ${error.message}")
                                    // Fallback to simulation
                                    setSecondsLeft(3)
                                    showOverlay(true)
                                }
                            }
                            
                            ad.show(activity, com.google.android.gms.ads.OnUserEarnedRewardListener { rewardItem ->
                                earnedReward = true
                            })
                        }

                        override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                            android.util.Log.e("PlayWinAds", "Failed to load scratch ad: ${loadAdError.message}")
                            // Fallback to simulation so user is not blocked
                            showLoading(false)
                            setSecondsLeft(3)
                            showOverlay(true)
                        }
                    }
                )
            } else {
                // Fallback directly
                setSecondsLeft(3)
                showOverlay(true)
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD32F2F) // Beautiful active Red button for rewarded ad
        ),
        modifier = Modifier
            .width(280.dp)
            .height(52.dp)
            .testTag("watch_reward_ad_button"),
        shape = RoundedCornerShape(26.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("📺", fontSize = 18.sp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Watch Reward Ad",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Get 1 Extra Scratch",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Synthetic sound effects generator using android.media.ToneGenerator
object QuizSoundPlayer {
    private var toneGenerator: android.media.ToneGenerator? = null
    
    init {
        try {
            toneGenerator = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            // handle
        }
    }
    
    fun playTick() {
        try {
            toneGenerator?.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 100)
        } catch (e: Exception) {
            // handle
        }
    }
    
    fun playTimeUp() {
        try {
            toneGenerator?.startTone(android.media.ToneGenerator.TONE_SUP_ERROR, 300)
        } catch (e: Exception) {
            // handle
        }
    }
}

@Composable
fun QuizCircularTimer(
    secondsLeft: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val color = when {
        secondsLeft >= 6 -> Color(0xFF4CAF50) // Green
        secondsLeft >= 3 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(68.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "⏱",
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${secondsLeft}s",
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Simple Trivia Quiz
@Composable
fun TriviaQuizScreen(
    viewModel: PlayWinViewModel,
    categoryId: String,
    quizSetId: String,
    onBack: () -> Unit
) {
    val quizzes by viewModel.quizzesState.collectAsStateWithLifecycle()
    val currentQuiz = remember(quizzes, quizSetId) { quizzes.find { it.id == quizSetId } }

    val timerLimit = currentQuiz?.timerSeconds ?: 10
    val timerLimitMs = timerLimit * 1000L
    val rewardCoinsPerCorrect = if (currentQuiz != null && currentQuiz.rewardPerQuestion > 0) currentQuiz.rewardPerQuestion else currentQuiz?.rewardCoins ?: 50
    val completionBonus = if (currentQuiz != null && currentQuiz.passBonus > 0) currentQuiz.passBonus else currentQuiz?.completionBonus ?: 50

    var quizQuestions by remember { mutableStateOf<List<com.playwin.app.data.model.Quiz>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val today = viewModel.getLocalDateString()
    var isCheckingFirebase by remember { mutableStateOf(true) }
    var isAlreadyCompletedTodayFirebase by remember { mutableStateOf(false) }

    LaunchedEffect(currentQuiz, quizSetId, quizzes) {
        if (quizzes.isEmpty()) {
            return@LaunchedEffect
        }
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
            val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
            
            val quiz = currentQuiz
            if (quiz != null) {
                val qDayOfWeek = quiz.dayOfWeek
                val todayDayName = viewModel.getTodayDayOfWeekName()
                
                // 1. Strict weekday matching: Only play if quiz.dayOfWeek == today's weekday
                if (qDayOfWeek.isNotEmpty() && !qDayOfWeek.equals(todayDayName, ignoreCase = true)) {
                    android.widget.Toast.makeText(context, "This quiz is locked. It is only available on $qDayOfWeek.", android.widget.Toast.LENGTH_LONG).show()
                    onBack()
                    return@LaunchedEffect
                }
                
                // 2. Strict completion check from Firebase under dailyQuiz
                db.getReference("users/$uid/dailyQuiz")
                    .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                        override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                            val compDate = snapshot.child("lastCompletedDate").getValue(String::class.java)
                            val completed = snapshot.child("completed").getValue(Boolean::class.java) ?: false
                            if (completed && compDate == today) {
                                isAlreadyCompletedTodayFirebase = true
                                android.widget.Toast.makeText(context, "You have already completed today's quiz.", android.widget.Toast.LENGTH_LONG).show()
                                onBack()
                            }
                            isCheckingFirebase = false
                        }

                        override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                            isCheckingFirebase = false
                        }
                    })
            } else {
                isCheckingFirebase = false
            }
        } else {
            isCheckingFirebase = false
        }
    }

    LaunchedEffect(quizzes, quizSetId) {
        if (quizSetId.startsWith("set_")) {
            viewModel.generateQuizForCategory(categoryId) { questions ->
                quizQuestions = questions
                isLoading = false
            }
        } else {
            val matchingQuiz = quizzes.find { it.id == quizSetId }
            if (matchingQuiz != null) {
                quizQuestions = matchingQuiz.questions
                isLoading = false
            } else if (quizzes.isNotEmpty()) {
                isLoading = false
            }
        }
    }

    if (isLoading || isCheckingFirebase) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = ElectricPurple)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Validating status with Firebase...", color = TextWhite)
            }
        }
        return
    }

    if (isAlreadyCompletedTodayFirebase) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
        )
        return
    }

    if (quizQuestions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text("⚠️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No questions found for this category.", color = TextWhite, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    var currentQuestionIdx by remember { mutableStateOf(0) }
    var selectedAnswerIdx by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }

    // Lifelines & Quiz State
    var lifelines by remember { mutableStateOf(2) }
    var adRefillCount by remember { mutableStateOf(0) }
    var showOutofLifelines by remember { mutableStateOf(false) }
    var showAdOverlay by remember { mutableStateOf(false) }
    var adSecondsLeft by remember { mutableStateOf(3) }

    // Timer State
    var timeLeftSeconds by remember(timerLimit) { mutableStateOf(timerLimit) }
    var timeLeftFraction by remember { mutableStateOf(1.0f) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var isTimerActive by remember { mutableStateOf(true) }

    // Scoring & Tracking
    var score by remember { mutableStateOf(0) }
    val answeredQuestionIds = remember { mutableStateListOf<String>() }
    var isQuizFinished by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var earnedCoins by remember { mutableStateOf<Int?>(null) }

    val quiz = quizQuestions[currentQuestionIdx]

    // Timer Loop with Anti-Cheat running on device time
    LaunchedEffect(currentQuestionIdx, isTimerActive) {
        if (!isTimerActive || isQuizFinished) return@LaunchedEffect
        startTime = System.currentTimeMillis()
        var lastTickedSecond = timerLimit
        
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            val remainingMs = timerLimitMs - elapsed
            
            if (remainingMs <= 0) {
                timeLeftSeconds = 0
                timeLeftFraction = 0.0f
                isTimerActive = false
                
                // Play Time Up sound
                QuizSoundPlayer.playTimeUp()
                
                // Deduct 1 lifeline
                lifelines = (lifelines - 1).coerceAtLeast(0)
                viewModel.trackQuizStats(timeOut = true, lifelineLostByTimeout = true, answerTimeMs = timerLimitMs)
                
                // Track this as answered (or missed) question
                if (!answeredQuestionIds.contains(quiz.id)) {
                    answeredQuestionIds.add(quiz.id)
                }

                resultMessage = "Time Up - 1 Lifeline Lost"
                isAnswered = true
                
                // Automatically move to next question or show Reward Ad Dialog
                delay(2000L)
                if (lifelines > 0) {
                    if (currentQuestionIdx < quizQuestions.size - 1) {
                        currentQuestionIdx++
                        selectedAnswerIdx = null
                        isAnswered = false
                        resultMessage = ""
                        timeLeftSeconds = timerLimit
                        timeLeftFraction = 1.0f
                        isTimerActive = true
                    } else {
                        isQuizFinished = true
                    }
                } else {
                    showOutofLifelines = true
                }
                break
            } else {
                val sec = (remainingMs / 1000L).toInt() + 1
                val coercedSec = sec.coerceIn(0, timerLimit)
                timeLeftSeconds = coercedSec
                timeLeftFraction = remainingMs.toFloat() / timerLimitMs.toFloat()
                
                if (coercedSec in 1..3 && coercedSec != lastTickedSecond) {
                    // Play Tick sound during last 3 seconds
                    QuizSoundPlayer.playTick()
                    lastTickedSecond = coercedSec
                }
            }
            delay(50L) // Precision checking loop
        }
    }

    // Dialog when Out of Lifelines
    if (showOutofLifelines) {
        AlertDialog(
            onDismissRequest = { /* Force action */ },
            confirmButton = {
                if (adRefillCount < 3) {
                    Button(
                        onClick = {
                            showAdOverlay = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldCoin),
                        modifier = Modifier.fillMaxWidth().testTag("watch_ad_button")
                    ) {
                        Text("Watch Rewarded Ad 📺 (+2 Lifelines)", color = PrimaryDark, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { /* Disabled */ },
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ad Refill Limit Reached (3/3)", color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().testTag("exit_quiz_button")
                ) {
                    Text("Exit Quiz", color = Color.White.copy(alpha = 0.6f))
                }
            },
            title = {
                Text("💔 No Lifelines Remaining", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("You have run out of lifelines for this quiz. Watch a quick ad to get +2 lifelines and continue (Refills used: $adRefillCount/3), or exit back to the arena.", color = Color.White.copy(alpha = 0.8f))
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Rewarded Ad Simulation Overlay
    if (showAdOverlay) {
        AlertDialog(
            onDismissRequest = { /* Force watch to completion */ },
            confirmButton = {},
            title = {
                Text("📺 Watching Rewarded Ad", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        progress = adSecondsLeft.toFloat() / 3f,
                        color = GoldCoin,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your ad is playing... rewards in ${adSecondsLeft}s",
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(24.dp)
        )

        LaunchedEffect(Unit) {
            adSecondsLeft = 3
            while (adSecondsLeft > 0) {
                delay(1000L)
                adSecondsLeft--
            }
            showAdOverlay = false
            showOutofLifelines = false
            adRefillCount++
            lifelines = (lifelines + 2).coerceAtMost(8) // Gain +2 Lifelines up to max 8
            isAnswered = false
            selectedAnswerIdx = null
            resultMessage = ""
            timeLeftSeconds = timerLimit
            timeLeftFraction = 1.0f
            isTimerActive = true
        }
    }

    val categoryTitle = when (categoryId) {
        "GK" -> "General Knowledge"
        "Sports" -> "Sports Challenge"
        "Movies" -> "Cinema & Pop Culture"
        "Science" -> "Science & Logic"
        "History" -> "Past Eras & History"
        "Technology" -> "Future Tech & Gadgets"
        else -> categoryId
    }

    if (isQuizFinished) {
        // Trigger completion write to Firebase once
        LaunchedEffect(Unit) {
            isSubmitting = true
            viewModel.completeQuiz(
                category = categoryId,
                quizSetId = quizSetId,
                score = score,
                answeredQuestionIds = answeredQuestionIds.toList(),
                totalQuestions = quizQuestions.size,
                rewardCoinsPerCorrect = rewardCoinsPerCorrect,
                completionBonus = completionBonus,
                dayOfWeek = currentQuiz?.dayOfWeek ?: ""
            ) { totalEarned ->
                earnedCoins = totalEarned
                isSubmitting = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0C0721), Color(0xFF04020A))))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131024)),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌟", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Today's Quiz Completed!",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Come Back Tomorrow For New Questions",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Score Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Correct Answers:", color = Color.Gray, fontSize = 14.sp)
                        Text("$score/${quizQuestions.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Coins From Correct Answers:", color = Color.Gray, fontSize = 14.sp)
                        Text("${score * rewardCoinsPerCorrect} Coins", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Perfect Score Bonus:", color = Color.Gray, fontSize = 14.sp)
                        Text(if (score == quizQuestions.size) "+$completionBonus Coins" else "0 Coins", color = if (score == quizQuestions.size) Color(0xFF4CAF50) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Rewards Claimed:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (isSubmitting || earnedCoins == null) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFFFFD700), strokeWidth = 2.dp)
                        } else {
                            Text("${earnedCoins} Coins", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onBack,
                        enabled = !isSubmitting && earnedCoins != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Claim & Exit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            Text(
                text = categoryTitle,
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress indicators
        Text(
            text = "Question ${currentQuestionIdx + 1} of ${quizQuestions.size}",
            color = ElectricPink,
            fontWeight = FontWeight.Bold
        )

        // TIMER & LIFELINES PROMINENT HEADER
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Lifelines: ",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                repeat(lifelines.coerceIn(0, 8)) {
                    Text(
                        text = "❤️",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
                if (lifelines == 0) {
                    Text(
                        text = "💔",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
            
            QuizCircularTimer(secondsLeft = timeLeftSeconds, progress = timeLeftFraction)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Text(
                text = quiz.question,
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Answer Slices
        quiz.options.forEachIndexed { index, option ->
            val buttonColor = when {
                !isAnswered && selectedAnswerIdx == index -> ElectricPurple
                isAnswered && index == quiz.correctAnswerIdx -> Color(0xFF4CAF50)
                isAnswered && selectedAnswerIdx == index && index != quiz.correctAnswerIdx -> Color(0xFFF44336)
                else -> CardDark
            }

            Card(
                onClick = {
                    if (!isAnswered && lifelines > 0) {
                        selectedAnswerIdx = index
                        // Stop timer immediately on selection
                        isTimerActive = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("quiz_option_$index"),
                colors = CardDefaults.cardColors(containerColor = buttonColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = option,
                    color = TextWhite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isAnswered) {
            Button(
                onClick = {
                    if (selectedAnswerIdx != null) {
                        isAnswered = true
                        isTimerActive = false
                        val answerTimeMs = (System.currentTimeMillis() - startTime).coerceAtMost(10000L)
                        
                        if (!answeredQuestionIds.contains(quiz.id)) {
                            answeredQuestionIds.add(quiz.id)
                        }

                        if (selectedAnswerIdx == quiz.correctAnswerIdx) {
                            score++
                            resultMessage = "Awesome! Correct Answer +20 Coins!"
                            viewModel.trackQuizStats(timeOut = false, lifelineLostByTimeout = false, answerTimeMs = answerTimeMs)
                        } else {
                            lifelines = (lifelines - 1).coerceAtLeast(0)
                            resultMessage = "Wrong Answer - 1 Lifeline Lost"
                            viewModel.trackQuizStats(timeOut = false, lifelineLostByTimeout = false, answerTimeMs = answerTimeMs)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .testTag("submit_quiz_button"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldCoin),
                enabled = selectedAnswerIdx != null,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Submit Answer", color = PrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            // Show Feedback
            Text(
                text = resultMessage,
                color = if (selectedAnswerIdx == quiz.correctAnswerIdx) Color(0xFF81C784) else Color(0xFFE57373),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = {
                    if (lifelines > 0) {
                        if (currentQuestionIdx < quizQuestions.size - 1) {
                            currentQuestionIdx++
                            selectedAnswerIdx = null
                            isAnswered = false
                            resultMessage = ""
                            timeLeftSeconds = timerLimit
                            timeLeftFraction = 1.0f
                            isTimerActive = true
                        } else {
                            isQuizFinished = true
                        }
                    } else {
                        showOutofLifelines = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .testTag("next_quiz_button"),
                colors = ButtonDefaults.buttonColors(containerColor = if (lifelines > 0) ElectricPurple else Color(0xFFE57373)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = if (lifelines > 0) {
                        if (currentQuestionIdx < quizQuestions.size - 1) "Next Question" else "Finish Quiz"
                    } else {
                        "No Lifelines Remaining"
                    },
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Math Solver Game Screen
@Composable
fun MathSolverScreen(viewModel: PlayWinViewModel, onBack: () -> Unit) {
    var num1 by remember { mutableStateOf(Random.nextInt(5, 50)) }
    var num2 by remember { mutableStateOf(Random.nextInt(5, 50)) }
    var operationSign by remember { mutableStateOf("+") }
    var correctResult by remember { mutableStateOf(0) }

    var options by remember { mutableStateOf(listOf<Int>()) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isCorrectAnswer by remember { mutableStateOf(false) }

    // Lifelines & State
    var lifelines by remember { mutableStateOf(3) }
    var showOutofLifelines by remember { mutableStateOf(false) }
    var showAdOverlay by remember { mutableStateOf(false) }
    var adSecondsLeft by remember { mutableStateOf(3) }

    // Timer State
    var timeLeftSeconds by remember { mutableStateOf(10) }
    var timeLeftFraction by remember { mutableStateOf(1.0f) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var isTimerActive by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf("") }

    fun generatePuzzle() {
        val nextOp = listOf("+", "-", "*").random()
        num1 = Random.nextInt(5, 20)
        num2 = Random.nextInt(2, 15)
        operationSign = nextOp
        correctResult = when (nextOp) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            else -> num1 * num2
        }

        val generatedList = mutableSetOf(correctResult)
        while (generatedList.size < 4) {
            generatedList.add(correctResult + Random.nextInt(-10, 10))
        }
        options = generatedList.toList().shuffled()
        selectedOption = null
        isSubmitted = false
        resultMessage = ""
        
        // Reset Timer
        timeLeftSeconds = 10
        timeLeftFraction = 1.0f
        startTime = System.currentTimeMillis()
        isTimerActive = true
    }

    LaunchedEffect(Unit) {
        generatePuzzle()
    }

    // Timer Loop with Anti-Cheat running on device time
    LaunchedEffect(num1, num2, operationSign, isTimerActive) {
        if (!isTimerActive) return@LaunchedEffect
        startTime = System.currentTimeMillis()
        var lastTickedSecond = 10
        
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            val remainingMs = 10000L - elapsed
            
            if (remainingMs <= 0) {
                timeLeftSeconds = 0
                timeLeftFraction = 0.0f
                isTimerActive = false
                
                // Play Time Up sound
                QuizSoundPlayer.playTimeUp()
                
                // Deduct 1 lifeline
                lifelines = (lifelines - 1).coerceAtLeast(0)
                viewModel.trackQuizStats(timeOut = true, lifelineLostByTimeout = true, answerTimeMs = 10000L)
                
                if (lifelines > 0) {
                    resultMessage = "Time Up! 1 Lifeline Lost"
                    isSubmitted = true
                    isCorrectAnswer = false
                    
                    // Automatically move to next question after 2 seconds
                    delay(2000L)
                    generatePuzzle()
                } else {
                    resultMessage = "Out of Lifelines"
                    showOutofLifelines = true
                    isSubmitted = true
                }
                break
            } else {
                val sec = (remainingMs / 1000L).toInt() + 1
                val coercedSec = sec.coerceIn(0, 10)
                timeLeftSeconds = coercedSec
                timeLeftFraction = remainingMs.toFloat() / 10000.0f
                
                if (coercedSec in 1..3 && coercedSec != lastTickedSecond) {
                    // Play Tick sound during last 3 seconds
                    QuizSoundPlayer.playTick()
                    lastTickedSecond = coercedSec
                }
            }
            delay(50L) // Precision checking loop
        }
    }

    // Dialog when Out of Lifelines
    if (showOutofLifelines) {
        AlertDialog(
            onDismissRequest = { /* Force action */ },
            confirmButton = {
                Button(
                    onClick = {
                        showAdOverlay = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldCoin),
                    modifier = Modifier.fillMaxWidth().testTag("watch_ad_button_math")
                ) {
                    Text("Watch Rewarded Ad 📺 (+2 Lifelines)", color = PrimaryDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().testTag("exit_quiz_button_math")
                ) {
                    Text("Exit Quiz", color = Color.White.copy(alpha = 0.6f))
                }
            },
            title = {
                Text("💔 Out of Lifelines", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("You have run out of lifelines for this quiz. Watch a quick ad to get +2 lifelines and continue, or exit back to the arena.", color = Color.White.copy(alpha = 0.8f))
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Rewarded Ad Simulation Overlay
    if (showAdOverlay) {
        AlertDialog(
            onDismissRequest = { /* Force watch to completion */ },
            confirmButton = {},
            title = {
                Text("📺 Watching Rewarded Ad", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        progress = adSecondsLeft.toFloat() / 3f,
                        color = GoldCoin,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your ad is playing... rewards in ${adSecondsLeft}s",
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(24.dp)
        )

        LaunchedEffect(Unit) {
            adSecondsLeft = 3
            while (adSecondsLeft > 0) {
                delay(1000L)
                adSecondsLeft--
            }
            showAdOverlay = false
            showOutofLifelines = false
            lifelines = 2 // Gain +2 Lifelines!
            isSubmitted = false
            selectedOption = null
            resultMessage = ""
            timeLeftSeconds = 10
            timeLeftFraction = 1.0f
            isTimerActive = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            Text(
                text = "Fast Math Challenge",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TIMER & LIFELINES PROMINENT HEADER
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Lifelines: ",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                repeat(3) { i ->
                    Text(
                        text = if (i < lifelines) "❤️" else "🖤",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
            
            QuizCircularTimer(secondsLeft = timeLeftSeconds, progress = timeLeftFraction)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Solve the instant arithmetic puzzle as fast as possible to verify! +15 Coins",
            style = MaterialTheme.typography.bodyLarge,
            color = TextWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(6.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Text(
                text = "$num1 $operationSign $num2 = ?",
                color = GoldCoinLight,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        options.forEach { option ->
            val optionBgColor = when {
                !isSubmitted && selectedOption == option -> ElectricPurple
                isSubmitted && option == correctResult -> Color(0xFF4CAF50)
                isSubmitted && selectedOption == option && option != correctResult -> Color(0xFFF44336)
                else -> CardDark
            }

            Card(
                onClick = {
                    if (!isSubmitted && lifelines > 0) {
                        selectedOption = option
                        // Stop timer immediately on selection
                        isTimerActive = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8F)
                    .padding(vertical = 5.dp)
                    .testTag("math_option_$option"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = optionBgColor)
            ) {
                Text(
                    text = option.toString(),
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!isSubmitted) {
            Button(
                onClick = {
                    if (selectedOption != null) {
                        isSubmitted = true
                        isTimerActive = false
                        isCorrectAnswer = selectedOption == correctResult
                        val answerTimeMs = (System.currentTimeMillis() - startTime).coerceAtMost(10000L)
                        
                        if (isCorrectAnswer) {
                            viewModel.addCoins(15, "Math Solver Challenge")
                            viewModel.trackQuizStats(timeOut = false, lifelineLostByTimeout = false, answerTimeMs = answerTimeMs)
                        } else {
                            viewModel.trackQuizStats(timeOut = false, lifelineLostByTimeout = false, answerTimeMs = answerTimeMs)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldCoin),
                modifier = Modifier
                    .width(180.dp)
                    .height(48.dp)
                    .testTag("submit_math_btn"),
                enabled = selectedOption != null,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Submit", color = PrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            val displayFeedback = if (resultMessage.isNotEmpty()) {
                resultMessage
            } else if (isCorrectAnswer) {
                "✔ Correct Response! +15 Coins."
            } else {
                "❌ Wrong answer. Try next calculation!"
            }
            
            Text(
                text = displayFeedback,
                color = if (isCorrectAnswer) Color(0xFF81C784) else Color(0xFFE57373),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (lifelines > 0) {
                Button(
                    onClick = {
                        generatePuzzle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    modifier = Modifier
                        .width(180.dp)
                        .height(48.dp)
                        .testTag("next_math_btn"),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Next Calculation", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

