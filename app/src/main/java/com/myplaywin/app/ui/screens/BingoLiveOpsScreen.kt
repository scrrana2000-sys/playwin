package com.myplaywin.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.data.model.AccessibilitySettings
import com.myplaywin.app.data.model.LiveOpsConfig
import com.myplaywin.app.data.model.ServerRegion
import com.myplaywin.app.data.repository.BingoLiveOpsRepository
import com.myplaywin.app.data.repository.BingoProgressionRepository
import com.myplaywin.app.data.repository.BingoSecurityAndAntiCheatEngine
import com.myplaywin.app.ui.components.AaaBingoAudioHaptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoLiveOpsScreen(
    liveOpsRepository: BingoLiveOpsRepository,
    securityEngine: BingoSecurityAndAntiCheatEngine,
    progressionRepository: BingoProgressionRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Server & Latency", "Anti-Cheat Security", "LiveOps Metrics", "Backup & Recovery", "Accessibility")

    val selectedRegion by liveOpsRepository.selectedRegion.collectAsState()
    val regionLatencies by liveOpsRepository.regionLatencies.collectAsState()
    val liveOpsConfig by liveOpsRepository.liveOpsConfig.collectAsState()
    val liveMetrics by liveOpsRepository.liveMetrics.collectAsState()
    val accessibilitySettings by liveOpsRepository.accessibilitySettings.collectAsState()
    val auditLogs by liveOpsRepository.auditLogs.collectAsState()
    val threatLogs by securityEngine.threatLogs.collectAsState()
    val isDeviceSecure by securityEngine.isDeviceSecure.collectAsState()

    var isBackingUp by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LIVE OPERATIONS & SECURITY",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Phase 9 Enterprise Server & Security Architecture",
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
                                if (isDeviceSecure) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isDeviceSecure) "SECURE" else "THREAT FLAGGED",
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
            // Scrollable Tab Row
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
                tabTitles.forEachIndexed { index, title ->
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
                0 -> RegionalServerSection(
                    selectedRegion = selectedRegion,
                    regionLatencies = regionLatencies,
                    onSelectRegion = { region ->
                        liveOpsRepository.selectRegion(region)
                        AaaBingoAudioHaptics.playClickSound()
                    },
                    onRefreshLatencies = {
                        liveOpsRepository.testAllRegionLatencies()
                        Toast.makeText(context, "Tested all server region latencies!", Toast.LENGTH_SHORT).show()
                    }
                )

                1 -> SecurityAntiCheatSection(
                    securityEngine = securityEngine,
                    isDeviceSecure = isDeviceSecure,
                    threatLogs = threatLogs,
                    onRunScan = {
                        val isSecure = securityEngine.performComprehensiveSecurityScan()
                        Toast.makeText(
                            context,
                            if (isSecure) "Security Scan: Device Safe & Integrity Verified!" else "Security Threat Detected!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )

                2 -> LiveOpsMetricsSection(
                    liveMetrics = liveMetrics,
                    liveOpsConfig = liveOpsConfig,
                    onToggleMaintenance = { isEnabled ->
                        val updated = liveOpsConfig.copy(isMaintenanceMode = isEnabled)
                        liveOpsRepository.updateRemoteConfigAdmin(updated)
                        Toast.makeText(context, "Maintenance Mode: $isEnabled", Toast.LENGTH_SHORT).show()
                    },
                    onUpdateMultiplier = { newMultiplier ->
                        val updated = liveOpsConfig.copy(coinRewardMultiplier = newMultiplier)
                        liveOpsRepository.updateRemoteConfigAdmin(updated)
                    }
                )

                3 -> CloudBackupSection(
                    progressionRepo = progressionRepository,
                    auditLogs = auditLogs,
                    isBackingUp = isBackingUp,
                    onPerformBackup = {
                        coroutineScope.launch {
                            isBackingUp = true
                            val prog = progressionRepository.progression.value
                            val stats = progressionRepository.stats.value
                            val success = liveOpsRepository.createEncryptedBackup(
                                coinBalance = prog.currentCoins,
                                level = prog.level,
                                totalXp = prog.currentXp,
                                matchCount = stats.totalMatches,
                                winCount = stats.totalWins
                            )
                            isBackingUp = false
                            if (success) {
                                AaaBingoAudioHaptics.playVictoryFanfare()
                                Toast.makeText(context, "Encrypted Cloud Backup Created Successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                4 -> AccessibilitySection(
                    settings = accessibilitySettings,
                    onUpdateSettings = { newSettings ->
                        liveOpsRepository.updateAccessibilitySettings(newSettings)
                    }
                )
            }
        }
    }
}

@Composable
private fun RegionalServerSection(
    selectedRegion: ServerRegion,
    regionLatencies: List<com.myplaywin.app.data.model.RegionLatencyResult>,
    onSelectRegion: (ServerRegion) -> Unit,
    onRefreshLatencies: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUTOMATIC REGIONAL ROUTING",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current Connected Server: ${selectedRegion.flagEmoji} ${selectedRegion.regionName}",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Endpoint: ${selectedRegion.endpointUrl}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onRefreshLatencies,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Re-Test All Regional Latencies", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "AVAILABLE SERVER CLUSTERS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(regionLatencies) { result ->
            val isSelected = result.region == selectedRegion
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectRegion(result.region) }
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) Color(0xFFFFD700) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF111827)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = result.region.flagEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = result.region.regionName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = result.region.regionCode,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val latencyColor = when {
                            result.latencyMs < 50 -> Color(0xFF22C55E)
                            result.latencyMs < 120 -> Color(0xFFEAB308)
                            else -> Color(0xFFEF4444)
                        }
                        Box(
                            modifier = Modifier
                                .background(latencyColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${result.latencyMs} ms",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = latencyColor
                            )
                        }

                        if (isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityAntiCheatSection(
    securityEngine: BingoSecurityAndAntiCheatEngine,
    isDeviceSecure: Boolean,
    threatLogs: List<com.myplaywin.app.data.model.SecurityThreatReport>,
    onRunScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDeviceSecure) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDeviceSecure) Icons.Default.VerifiedUser else Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isDeviceSecure) "SYSTEM INTEGRITY PASSED" else "SECURITY THREAT DETECTED",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isDeviceSecure) "Root-free, official APK, real-time timer verified." else "Device flagged for anti-cheat verification.",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onRunScan,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Trigger Comprehensive Security Scan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "SECURITY AUDIT TRAIL LOGS (${threatLogs.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (threatLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No security violations detected. Clean environment.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(threatLogs) { threat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = threat.threatType,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                            Text(
                                text = threat.severity,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = threat.details,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Device: ${threat.deviceModel} | Action: ${threat.actionTaken}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveOpsMetricsSection(
    liveMetrics: com.myplaywin.app.data.model.LiveMetricsSummary,
    liveOpsConfig: LiveOpsConfig,
    onToggleMaintenance: (Boolean) -> Unit,
    onUpdateMultiplier: (Float) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REAL-TIME TELEMETRY METRICS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBox(title = "Active Players", value = "${liveMetrics.activePlayersCount}")
                        MetricBox(title = "Live Matches", value = "${liveMetrics.concurrentMatchesCount}")
                        MetricBox(title = "Server Health", value = "${liveMetrics.serverHealthScorePercent}%")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBox(title = "Match Success", value = "${liveMetrics.matchSuccessRatePercent}%")
                        MetricBox(title = "Avg Latency", value = "${liveMetrics.avgLatencyMs} ms")
                        MetricBox(title = "Crash Free", value = "${liveMetrics.crashFreeSessionPercent}%")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REMOTE CONFIG & MAINTENANCE MODE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Maintenance Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Safely block new online matchmaking",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = liveOpsConfig.isMaintenanceMode,
                            onCheckedChange = onToggleMaintenance,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFD700),
                                checkedTrackColor = Color(0xFFB71C1C)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Coin Reward Multiplier: ${liveOpsConfig.coinRewardMultiplier}x",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                    Slider(
                        value = liveOpsConfig.coinRewardMultiplier,
                        onValueChange = onUpdateMultiplier,
                        valueRange = 0.5f..3.0f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD700),
                            activeTrackColor = Color(0xFFFFD700)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBox(title: String, value: String) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(10.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
            Text(text = title, fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CloudBackupSection(
    progressionRepo: BingoProgressionRepository,
    auditLogs: List<String>,
    isBackingUp: Boolean,
    onPerformBackup: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ENCRYPTED CLOUD BACKUP & RECOVERY",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AES-256 encrypted payload backup for wallet, player statistics, and achievements.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onPerformBackup,
                        enabled = !isBackingUp,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isBackingUp) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        } else {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isBackingUp) "Encrypting & Uploading..." else "Trigger Encrypted Backup Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "SYSTEM AUDIT TRAIL LOGS (${auditLogs.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(auditLogs) { logEntry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = logEntry,
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun AccessibilitySection(
    settings: AccessibilitySettings,
    onUpdateSettings: (AccessibilitySettings) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACCESSIBILITY & INCLUSIVITY OPTIONS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "High Contrast Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Enhance border contrast for low vision", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = settings.isHighContrastMode,
                            onCheckedChange = { onUpdateSettings(settings.copy(isHighContrastMode = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Color-Blind Friendly Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Pattern indicators on bingo board tiles", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = settings.isColorBlindMode,
                            onCheckedChange = { onUpdateSettings(settings.copy(isColorBlindMode = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Haptic Vibration Intensity: ${(settings.hapticIntensity * 100).toInt()}%", fontSize = 13.sp, color = Color.LightGray)
                    Slider(
                        value = settings.hapticIntensity,
                        onValueChange = { onUpdateSettings(settings.copy(hapticIntensity = it)) },
                        valueRange = 0.0f..1.0f
                    )
                }
            }
        }
    }
}
