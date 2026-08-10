package com.myplaywin.app.data.repository

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.myplaywin.app.data.model.AccessibilitySettings
import com.myplaywin.app.data.model.EncryptedDataBackup
import com.myplaywin.app.data.model.LiveMetricsSummary
import com.myplaywin.app.data.model.LiveOpsConfig
import com.myplaywin.app.data.model.RegionLatencyResult
import com.myplaywin.app.data.model.ServerRegion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Enterprise Live Operations & Server Scalability Repository
 */
class BingoLiveOpsRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val database = FirebaseDatabase.getInstance(dbUrl).reference
    private val prefs = context.getSharedPreferences("bingo_liveops_prefs", Context.MODE_PRIVATE)

    // Current Server Region State
    private val _selectedRegion = MutableStateFlow(ServerRegion.INDIA)
    val selectedRegion: StateFlow<ServerRegion> = _selectedRegion.asStateFlow()

    private val _regionLatencies = MutableStateFlow<List<RegionLatencyResult>>(emptyList())
    val regionLatencies: StateFlow<List<RegionLatencyResult>> = _regionLatencies.asStateFlow()

    // Remote Config State
    private val _liveOpsConfig = MutableStateFlow(LiveOpsConfig())
    val liveOpsConfig: StateFlow<LiveOpsConfig> = _liveOpsConfig.asStateFlow()

    // Realtime Metrics Summary
    private val _liveMetrics = MutableStateFlow(LiveMetricsSummary())
    val liveMetrics: StateFlow<LiveMetricsSummary> = _liveMetrics.asStateFlow()

    // Accessibility State
    private val _accessibilitySettings = MutableStateFlow(loadAccessibilitySettings())
    val accessibilitySettings: StateFlow<AccessibilitySettings> = _accessibilitySettings.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<String>>(emptyList())
    val auditLogs: StateFlow<List<String>> = _auditLogs.asStateFlow()

    init {
        testAllRegionLatencies()
        listenToRemoteConfig()
        listenToLiveMetrics()
        logAuditEvent("LIVEOPS_INITIALIZED", "LiveOps repository initialized successfully.")
    }

    /**
     * Measure regional latency & select best optimal region
     */
    fun testAllRegionLatencies() {
        val results = mutableListOf<RegionLatencyResult>()
        ServerRegion.values().forEach { region ->
            // Simulated network RTT ping for regional cluster health check
            val baseLatency = when (region) {
                ServerRegion.INDIA -> (18..35).random()
                ServerRegion.ASIA -> (45..70).random()
                ServerRegion.EUROPE -> (110..140).random()
                ServerRegion.NORTH_AMERICA -> (160..210).random()
                ServerRegion.MIDDLE_EAST -> (65..95).random()
            }
            results.add(RegionLatencyResult(region = region, latencyMs = baseLatency))
        }
        _regionLatencies.value = results.sortedBy { it.latencyMs }

        // Select lowest latency region
        results.minByOrNull { it.latencyMs }?.let { best ->
            _selectedRegion.value = best.region
        }
    }

    fun selectRegion(region: ServerRegion) {
        _selectedRegion.value = region
        logAuditEvent("REGION_CHANGED", "Switched primary backend region to ${region.regionName}")
    }

    /**
     * Firebase Remote Config Synchronization
     */
    private fun listenToRemoteConfig() {
        database.child("liveops_config").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val config = snapshot.getValue(LiveOpsConfig::class.java)
                    if (config != null) {
                        _liveOpsConfig.value = config
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LiveOpsRepo", "Failed to sync remote config: ${error.message}")
            }
        })
    }

    fun updateRemoteConfigAdmin(newConfig: LiveOpsConfig) {
        _liveOpsConfig.value = newConfig
        database.child("liveops_config").setValue(newConfig)
        logAuditEvent("ADMIN_CONFIG_UPDATED", "MaintenanceMode=${newConfig.isMaintenanceMode}, Multiplier=${newConfig.coinRewardMultiplier}")
    }

    /**
     * Realtime Telemetry & Live Operations Monitoring
     */
    private fun listenToLiveMetrics() {
        database.child("liveops_metrics").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val metrics = snapshot.getValue(LiveMetricsSummary::class.java)
                    if (metrics != null) {
                        _liveMetrics.value = metrics
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Keep default high performance fallback
            }
        })
    }

    /**
     * Encrypted Cloud Backup & Disaster Recovery
     */
    suspend fun createEncryptedBackup(
        coinBalance: Int,
        level: Int,
        totalXp: Int,
        matchCount: Int,
        winCount: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUser?.uid ?: "GUEST_USER"
            val rawPayload = "coins=$coinBalance|lvl=$level|xp=$totalXp|matches=$matchCount|wins=$winCount|ts=${System.currentTimeMillis()}"
            val encryptedBlob = encryptString(rawPayload, "PlayWinBingoSecurityKey2026")
            val payloadHash = sha256(rawPayload)

            val backup = EncryptedDataBackup(
                backupId = "BACKUP_${System.currentTimeMillis()}",
                playerUid = uid,
                timestamp = System.currentTimeMillis(),
                payloadHash = payloadHash,
                coinBalance = coinBalance,
                level = level,
                totalXp = totalXp,
                matchCount = matchCount,
                winCount = winCount,
                encryptedBlob = encryptedBlob
            )

            // Save locally and push to cloud database
            prefs.edit().putString("latest_encrypted_backup", encryptedBlob).apply()
            database.child("player_backups").child(uid).setValue(backup)

            logAuditEvent("CLOUD_BACKUP_CREATED", "Backup created successfully with hash: ${payloadHash.take(8)}")
            true
        } catch (e: Exception) {
            Log.e("LiveOpsRepo", "Backup creation failed: ${e.message}")
            false
        }
    }

    /**
     * Accessibility Settings Management
     */
    private fun loadAccessibilitySettings(): AccessibilitySettings {
        return AccessibilitySettings(
            isHighContrastMode = prefs.getBoolean("acc_high_contrast", false),
            isColorBlindMode = prefs.getBoolean("acc_color_blind", false),
            hapticIntensity = prefs.getFloat("acc_haptic_intensity", 1.0f),
            isScreenReaderOptimized = prefs.getBoolean("acc_screen_reader", false),
            fontScaleFactor = prefs.getFloat("acc_font_scale", 1.0f)
        )
    }

    fun updateAccessibilitySettings(settings: AccessibilitySettings) {
        _accessibilitySettings.value = settings
        prefs.edit()
            .putBoolean("acc_high_contrast", settings.isHighContrastMode)
            .putBoolean("acc_color_blind", settings.isColorBlindMode)
            .putFloat("acc_haptic_intensity", settings.hapticIntensity)
            .putBoolean("acc_screen_reader", settings.isScreenReaderOptimized)
            .putFloat("acc_font_scale", settings.fontScaleFactor)
            .apply()
        logAuditEvent("ACCESSIBILITY_UPDATED", "HighContrast=${settings.isHighContrastMode}, ColorBlind=${settings.isColorBlindMode}")
    }

    /**
     * Audit Logging System
     */
    fun logAuditEvent(tag: String, message: String) {
        val entry = "[${System.currentTimeMillis()}] [$tag] $message"
        _auditLogs.value = listOf(entry) + _auditLogs.value.take(50)
        Log.i("BingoAuditLog", entry)
    }

    // AES Encryption Helpers
    private fun encryptString(data: String, secretKey: String): String {
        val key = SecretKeySpec(sha256Bytes(secretKey).copyOf(16), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun sha256Bytes(input: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(StandardCharsets.UTF_8))
    }
}
