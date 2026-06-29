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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LuckySpinScreen(viewModel: PlayWinViewModel, onBack: () -> Unit) {
    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    var rotationAngle by remember { mutableStateOf(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResultPopup by remember { mutableStateOf(false) }
    var showResultTitle by remember { mutableStateOf("") }
    var showResultMessage by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    var showAdLoadingDialog by remember { mutableStateOf(false) }
    var showAdOverlay by remember { mutableStateOf(false) }
    var adSecondsLeft by remember { mutableIntStateOf(3) }

    val freeSpinUsed = wallet.freeSpinUsed
    val rewardAdSpinUsed = wallet.rewardAdSpinUsed
    val spinsAvailableText = when {
        !freeSpinUsed -> "1/2 Available"
        !rewardAdSpinUsed -> "Remaining: 1/2 (Reward Ad Required)"
        wallet.remainingSpins > 0 -> "1/2 Available"
        else -> "0/2 Available"
    }

    val sectors = remember {
        listOf(
            "+5 Coins",     // Index 0
            "+10 Coins",    // Index 1
            "+20 Coins",    // Index 2
            "+30 Coins",    // Index 3
            "+50 Coins",    // Index 4
            "+100 Coins",   // Index 5
            "+200 Coins"    // Index 6
        )
    }

    val sectorColors = remember {
        listOf(
            Color(0xFFFF5252), // Vivid Coral Red (+5)
            Color(0xFFFF9F1C), // Gold Orange (+10)
            Color(0xFF2EC4B6), // Bright Teal (+20)
            Color(0xFF1D3557), // Royal Navy Blue (+30)
            Color(0xFFE05780), // Rose Indigo (+50)
            Color(0xFF9C27B0), // Vibrant Purple (+100 - Rare)
            Color(0xFF4CAF50)  // Gold Green (+200 - Ultra Rare)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.refreshUserData()
    }

    if (showAdLoadingDialog) {
        AlertDialog(
            onDismissRequest = { /* Cannot dismiss while loading */ },
            confirmButton = {},
            title = {
                Text(
                    text = "🎬 Loading Ad...",
                    color = Color.White,
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
                        color = GoldCoin,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Preparing your rewarded video ad...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Rewarded Ad Simulation Overlay for Lucky Spin (fallback)
    if (showAdOverlay) {
        AlertDialog(
            onDismissRequest = { /* Force watch to completion */ },
            confirmButton = {},
            title = {
                Text("📺 Watching Fallback Ad", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
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
                        text = "Your fallback ad is playing... extra spin in ${adSecondsLeft}s",
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
            
            viewModel.unlockAdSpin { success, errorMsg ->
                if (!success) {
                    errorMessage = errorMsg ?: "Failed to unlock extra spin."
                }
            }
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
        // App bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            Text(
                text = "Lucky Spin & Win",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Headline
        Text(
            text = "Spin the premium wheel of fortune to earn instant gold coins!",
            style = MaterialTheme.typography.bodyLarge,
            color = TextWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Stats Dashboard Panel
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
                        text = "Daily Spins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = spinsAvailableText,
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
                        text = "Total Spin Wins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+${wallet.totalSpinRewards} Coins",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display Current Spin Status Text or Banner
        val statusText = when {
            !freeSpinUsed -> "FREE SPIN AVAILABLE"
            !rewardAdSpinUsed -> "WATCH AD FOR EXTRA SPIN"
            else -> "DAILY SPIN LIMIT REACHED"
        }
        val statusColor = when {
            !freeSpinUsed -> Color(0xFF4CAF50) // Green
            !rewardAdSpinUsed -> GoldCoin // Orange/Gold
            else -> Color(0xFFE57373) // Light Red
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            if (freeSpinUsed && rewardAdSpinUsed) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = statusColor,
                    modifier = Modifier.size(20.dp).padding(end = 4.dp)
                )
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pointer Indicator
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Indicator pointer",
            tint = GoldCoin,
            modifier = Modifier
                .size(44.dp)
                .rotate(90f) // Points pointing down
        )

        // Spinner Canvas Frame
        Box(
            modifier = Modifier
                .size(280.dp)
                .rotate(rotationAngle)
                .shadow(16.dp, CircleShape)
                .clip(CircleShape)
                .background(CardDark),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sweepAngle = 360f / sectors.size
                for (i in sectors.indices) {
                    drawArc(
                        color = sectorColors[i],
                        startAngle = i * sweepAngle - 90f,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                }

                // Draw white borders separating segments
                val center = size.width / 2
                val radius = size.width / 2
                for (i in sectors.indices) {
                    val angleRad = (i * sweepAngle - 90f) * PI / 180f
                    drawLine(
                        color = DarkBg.copy(alpha = 0.5f),
                        start = Offset(center, center),
                        end = Offset(
                            (center + radius * cos(angleRad)).toFloat(),
                            (center + radius * sin(angleRad)).toFloat()
                        ),
                        strokeWidth = 3f
                    )
                }

                // Draw premium gold center hub
                drawCircle(
                    color = DarkBg,
                    radius = center * 0.18f,
                    center = Offset(center, center)
                )
                drawCircle(
                    color = GoldCoin,
                    radius = center * 0.15f,
                    center = Offset(center, center)
                )
                drawCircle(
                    color = Color.White,
                    radius = center * 0.05f,
                    center = Offset(center, center)
                )
            }

            // Radial slot texts overlay
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SPIN NOW Button
        val buttonEnabled = !isSpinning && (
            !freeSpinUsed || (freeSpinUsed && rewardAdSpinUsed && wallet.remainingSpins > 0)
        )
        
        val buttonText = when {
            isSpinning -> "SPINNING..."
            !freeSpinUsed -> "SPIN NOW (FREE)"
            !rewardAdSpinUsed -> "SPIN NOW" // Will be disabled, but shown as "SPIN NOW"
            wallet.remainingSpins > 0 -> "SPIN NOW"
            else -> "Today's Spins Completed"
        }

        Button(
            onClick = {
                if (!isSpinning) {
                    when {
                        !freeSpinUsed -> {
                            isSpinning = true
                            val rolledReward = viewModel.rollSpinReward()
                            coroutineScope.launch {
                                val targetSector = rolledReward.index
                                val sectorAngle = 360f / sectors.size
                                val targetAngle = 360f - (targetSector * sectorAngle + sectorAngle / 2f)
                                val extraTurns = 5 * 360f
                                val finalRotationTarget = rotationAngle - (rotationAngle % 360f) + extraTurns + targetAngle

                                animate(
                                    initialValue = rotationAngle,
                                    targetValue = finalRotationTarget,
                                    animationSpec = tween(
                                        durationMillis = 4000,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { value, _ ->
                                    rotationAngle = value
                                }

                                viewModel.performSpinWheelTransaction(rolledReward.index, isAdSpin = false) { success, errorMsg ->
                                    isSpinning = false
                                    if (success) {
                                        val finalSector = sectors[targetSector]
                                        showResultTitle = "🎉 Congratulations!"
                                        showResultMessage = "You won $finalSector"
                                        showResultPopup = true
                                    } else {
                                        errorMessage = errorMsg ?: "Transaction failed."
                                    }
                                }
                            }
                        }
                        freeSpinUsed && rewardAdSpinUsed && wallet.remainingSpins > 0 -> {
                            isSpinning = true
                            val rolledReward = viewModel.rollSpinReward()
                            coroutineScope.launch {
                                val targetSector = rolledReward.index
                                val sectorAngle = 360f / sectors.size
                                val targetAngle = 360f - (targetSector * sectorAngle + sectorAngle / 2f)
                                val extraTurns = 5 * 360f
                                val finalRotationTarget = rotationAngle - (rotationAngle % 360f) + extraTurns + targetAngle

                                animate(
                                    initialValue = rotationAngle,
                                    targetValue = finalRotationTarget,
                                    animationSpec = tween(
                                        durationMillis = 4000,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { value, _ ->
                                    rotationAngle = value
                                }

                                viewModel.performSpinWheelTransaction(rolledReward.index, isAdSpin = true) { success, errorMsg ->
                                    isSpinning = false
                                    if (success) {
                                        val finalSector = sectors[targetSector]
                                        showResultTitle = "🎉 Congratulations!"
                                        showResultMessage = "You won $finalSector"
                                        showResultPopup = true
                                    } else {
                                        errorMessage = errorMsg ?: "Transaction failed."
                                    }
                                }
                            }
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (buttonEnabled) GoldCoin else Color.Gray,
                disabledContainerColor = Color.Gray
            ),
            modifier = Modifier
                .width(280.dp)
                .height(48.dp)
                .testTag("spin_now_button"),
            enabled = buttonEnabled,
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = buttonText,
                color = if (buttonEnabled) PrimaryDark else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // 2. Reward Ad logic:
        // After the free spin is used, disable the Spin button.
        // Show "Watch Reward Ad to Unlock 1 Extra Spin" (Only when freeSpinUsed and not rewardAdSpinUsed)
        if (freeSpinUsed && !rewardAdSpinUsed && !isSpinning) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (activity != null) {
                        showAdLoadingDialog = true
                        val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
                        val adUnitId = "ca-app-pub-3940256099942544/5224354917" // AdMob Test Rewarded Ad Unit ID
                        
                        com.google.android.gms.ads.rewarded.RewardedAd.load(
                            activity,
                            adUnitId,
                            adRequest,
                            object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
                                override fun onAdLoaded(ad: com.google.android.gms.ads.rewarded.RewardedAd) {
                                    showAdLoadingDialog = false
                                    var earnedReward = false
                                    
                                    ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                                        override fun onAdDismissedFullScreenContent() {
                                            android.util.Log.d("PlayWinAds", "Spin Ad dismissed")
                                            if (earnedReward) {
                                                viewModel.unlockAdSpin { success, errorMsg ->
                                                    if (!success) {
                                                        errorMessage = errorMsg ?: "Failed to unlock extra spin."
                                                    }
                                                }
                                            } else {
                                                errorMessage = "Ad skipped or closed early. No spin unlocked."
                                            }
                                        }
                                        
                                        override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                            android.util.Log.e("PlayWinAds", "Failed to show spin ad: ${error.message}")
                                            // Fallback to simulation
                                            adSecondsLeft = 3
                                            showAdOverlay = true
                                        }
                                    }
                                    
                                    ad.show(activity, com.google.android.gms.ads.OnUserEarnedRewardListener { rewardItem ->
                                        earnedReward = true
                                    })
                                }

                                override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                                    android.util.Log.e("PlayWinAds", "Failed to load spin ad: ${loadAdError.message}")
                                    // Fallback to simulation so user is not blocked
                                    showAdLoadingDialog = false
                                    adSecondsLeft = 3
                                    showAdOverlay = true
                                }
                            }
                        )
                    } else {
                        // Fallback directly
                        adSecondsLeft = 3
                        showAdOverlay = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F) // Beautiful active Red button for rewarded ad
                ),
                modifier = Modifier
                    .width(280.dp)
                    .height(48.dp)
                    .testTag("watch_reward_ad_spin_button"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "🎬 Watch Reward Ad to Unlock 1 Extra Spin",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        if (freeSpinUsed && rewardAdSpinUsed && wallet.remainingSpins == 0 && !isSpinning) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Today's Spins Completed. Come Back Tomorrow.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextWhite.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weighted details list card
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
                    text = "🎡 Spinner Probabilities & Rules",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldCoin,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                val items = listOf(
                    "+5 Coins → 35%",
                    "+10 Coins → 25%",
                    "+20 Coins → 20%",
                    "+30 Coins → 10%",
                    "+50 Coins → 7%",
                    "+100 Coins → 2% (Rare)",
                    "+200 Coins → 1% (Ultra Rare)"
                )
                
                items.forEach { line ->
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

    // Success win popup
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
            containerColor = CardDark,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun LuckyScratchScreen(viewModel: PlayWinViewModel, onBack: () -> Unit) {
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
                        text = "Today's Scratch Cards Finished\nCome back tomorrow.",
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
    var quizQuestions by remember { mutableStateOf<List<com.playwin.app.data.model.Quiz>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(categoryId) {
        viewModel.generateQuizForCategory(categoryId) { questions ->
            quizQuestions = questions
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = ElectricPurple)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading quiz questions from Firebase...", color = TextWhite)
            }
        }
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
    var timeLeftSeconds by remember { mutableStateOf(10) }
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
                        timeLeftSeconds = 10
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
            timeLeftSeconds = 10
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
                answeredQuestionIds = answeredQuestionIds.toList()
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
                        Text("$score/10", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Coins From Correct Answers:", color = Color.Gray, fontSize = 14.sp)
                        Text("${score * 50} Coins", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Perfect Score Bonus:", color = Color.Gray, fontSize = 14.sp)
                        Text(if (score == 10) "+50 Coins" else "0 Coins", color = if (score == 10) Color(0xFF4CAF50) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
            text = "Question ${currentQuestionIdx + 1} of 10",
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
                        if (currentQuestionIdx < 9) {
                            currentQuestionIdx++
                            selectedAnswerIdx = null
                            isAnswered = false
                            resultMessage = ""
                            timeLeftSeconds = 10
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
                        if (currentQuestionIdx < 9) "Next Question" else "Finish Quiz"
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

