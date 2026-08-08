package com.myplaywin.app.data.repository

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.myplaywin.app.data.model.SecurityThreatReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Enterprise Anti-Cheat & Security Integrity System
 */
class BingoSecurityAndAntiCheatEngine(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private val _threatLogs = MutableStateFlow<List<SecurityThreatReport>>(emptyList())
    val threatLogs: StateFlow<List<SecurityThreatReport>> = _threatLogs.asStateFlow()

    private val _isDeviceSecure = MutableStateFlow(true)
    val isDeviceSecure: StateFlow<Boolean> = _isDeviceSecure.asStateFlow()

    private var lastRealTimeMeasurement: Long = SystemClock.elapsedRealtime()
    private var lastSystemTimeMeasurement: Long = System.currentTimeMillis()

    init {
        performComprehensiveSecurityScan()
    }

    fun performComprehensiveSecurityScan(): Boolean {
        val rootDetected = checkRootFiles() || checkBuildTags()
        val suspiciousPackages = checkSuspiciousPackages()
        val timeManipulationDetected = checkTimeManipulation()

        var isViolated = false

        if (rootDetected) {
            isViolated = true
            reportSecurityViolation(
                threatType = "ROOT_DETECTED",
                severity = "HIGH",
                details = "Root access or su binary detected on device."
            )
        }

        if (suspiciousPackages) {
            isViolated = true
            reportSecurityViolation(
                threatType = "SUSPICIOUS_PACKAGES",
                severity = "HIGH",
                details = "Memory editing or patcher packages detected on system."
            )
        }

        if (timeManipulationDetected) {
            isViolated = true
            reportSecurityViolation(
                threatType = "TIMER_MANIPULATION",
                severity = "CRITICAL",
                details = "System clock discrepancy detected compared to hardware elapsed realtime."
            )
        }

        _isDeviceSecure.value = !isViolated
        return !isViolated
    }

    private fun checkRootFiles(): Boolean {
        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        return rootPaths.any { File(it).exists() }
    }

    private fun checkBuildTags(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkSuspiciousPackages(): Boolean {
        val knownCheatPackages = listOf(
            "com.chelpus.lackypatch",
            "com.dimonvideo.luckypatcher",
            "org.cheatengine.cheatengine",
            "catch_.me_.if_.you_.can",
            "com.gameguardian"
        )
        val pm = context.packageManager
        for (pkg in knownCheatPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (e: Exception) {
                // Package not found (normal)
            }
        }
        return false
    }

    fun checkTimeManipulation(): Boolean {
        val currentRealTime = SystemClock.elapsedRealtime()
        val currentSystemTime = System.currentTimeMillis()

        val realTimeDelta = currentRealTime - lastRealTimeMeasurement
        val systemTimeDelta = currentSystemTime - lastSystemTimeMeasurement

        lastRealTimeMeasurement = currentRealTime
        lastSystemTimeMeasurement = currentSystemTime

        // If system time jump differs by more than 5 seconds from monotonic hardware clock
        if (realTimeDelta > 0 && Math.abs(systemTimeDelta - realTimeDelta) > 5000) {
            Log.w("BingoAntiCheat", "Time manipulation discrepancy detected! Delta diff: ${Math.abs(systemTimeDelta - realTimeDelta)}ms")
            return true
        }
        return false
    }

    fun validatePacketIntegrity(packetTimestamp: Long, nonce: String): Boolean {
        val now = System.currentTimeMillis()
        if (Math.abs(now - packetTimestamp) > 10000) { // Reject requests older or ahead by > 10s
            reportSecurityViolation(
                threatType = "PACKET_REPLAY",
                severity = "MEDIUM",
                details = "Packet timestamp drift too large or replay attempt detected. Nonce: $nonce"
            )
            return false
        }
        return true
    }

    fun reportSecurityViolation(threatType: String, severity: String, details: String) {
        val uid = auth.currentUser?.uid ?: "ANONYMOUS_USER"
        val report = SecurityThreatReport(
            id = "THREAT_${System.currentTimeMillis()}_${(1000..9999).random()}",
            playerUid = uid,
            threatType = threatType,
            severity = severity,
            details = details,
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})",
            appVersion = "1.0.0",
            timestamp = System.currentTimeMillis(),
            actionTaken = if (severity == "CRITICAL" || severity == "HIGH") "BLOCKED_MATCH" else "FLAGGED"
        )

        _threatLogs.value = listOf(report) + _threatLogs.value

        try {
            database.child("security_violations").child(report.id).setValue(report)
        } catch (e: Exception) {
            Log.e("BingoAntiCheat", "Failed to upload security violation log: ${e.message}")
        }
    }
}
