package com.myplaywin.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Phase 9: Enterprise Security, Live Operations & Server Scalability Models
 */

enum class ServerRegion(val regionCode: String, val regionName: String, val flagEmoji: String, val endpointUrl: String) {
    INDIA("IN-WEST", "India (Mumbai)", "🇮🇳", "https://in-west.playwin.net"),
    ASIA("ASIA-SE", "Asia Pacific (Singapore)", "🇸🇬", "https://asia-se.playwin.net"),
    EUROPE("EU-CENTRAL", "Europe (Frankfurt)", "🇪🇺", "https://eu-central.playwin.net"),
    NORTH_AMERICA("NA-EAST", "North America (US East)", "🇺🇸", "https://na-east.playwin.net"),
    MIDDLE_EAST("ME-SOUTH", "Middle East (Dubai)", "🇦🇪", "https://me-south.playwin.net")
}

@IgnoreExtraProperties
data class RegionLatencyResult(
    val region: ServerRegion,
    val latencyMs: Int,
    val isAvailable: Boolean = true
)

@IgnoreExtraProperties
data class SecurityThreatReport(
    val id: String = "",
    val playerUid: String = "",
    val threatType: String = "", // "ROOT_DETECTED", "MODIFIED_APK", "SPEED_HACK", "TIMER_MANIPULATION", "PACKET_REPLAY"
    val severity: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val details: String = "",
    val deviceModel: String = "",
    val appVersion: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val actionTaken: String = "FLAGGED" // "FLAGGED", "TEMPORARY_BAN", "PERMANENT_BAN", "BLOCKED_MATCH"
)

@IgnoreExtraProperties
data class LiveOpsConfig(
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "PlayWin Bingo servers are undergoing scheduled optimization. Online matches will resume shortly.",
    val isOnlineMatchmakingEnabled: Boolean = true,
    val coinRewardMultiplier: Float = 1.0f,
    val xpRewardMultiplier: Float = 1.0f,
    val minSupportedAppVersion: String = "1.0.0",
    val featureFlags: Map<String, Boolean> = mapOf(
        "enable_1v1_online" to true,
        "enable_leaderboards" to true,
        "enable_anti_cheat_strict" to true,
        "enable_automatic_reconnect" to true,
        "enable_cloud_backup" to true
    )
)

@IgnoreExtraProperties
data class LiveMetricsSummary(
    val activePlayersCount: Int = 12450,
    val concurrentMatchesCount: Int = 3820,
    val serverHealthScorePercent: Int = 99,
    val matchSuccessRatePercent: Float = 99.4f,
    val avgLatencyMs: Int = 38,
    val crashFreeSessionPercent: Float = 99.8f,
    val flaggedSuspiciousPlayers: Int = 12,
    val totalCoinsDistributedToday: Long = 1450000L,
    val totalXpDistributedToday: Long = 3200000L
)

@IgnoreExtraProperties
data class EncryptedDataBackup(
    val backupId: String = "",
    val playerUid: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val payloadHash: String = "",
    val coinBalance: Int = 0,
    val level: Int = 1,
    val totalXp: Int = 0,
    val matchCount: Int = 0,
    val winCount: Int = 0,
    val encryptedBlob: String = ""
)

data class AccessibilitySettings(
    val isHighContrastMode: Boolean = false,
    val isColorBlindMode: Boolean = false,
    val hapticIntensity: Float = 1.0f,
    val isScreenReaderOptimized: Boolean = false,
    val fontScaleFactor: Float = 1.0f
)
