package com.myplaywin.app.blockmaster.security

import android.content.Context
import android.os.SystemClock
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object BlockMasterAntiCheat {

    private const val TAG = "BlockMasterAntiCheat"
    private const val SECRET_SEED = "PlayWin_BlockMaster_Secure_Key_500IQ"

    // Debounce track for rapid click protection
    private var lastActionTimestamp: Long = 0L
    private const val MIN_ACTION_INTERVAL_MS = 400L

    // Track claimed reward transaction IDs to prevent duplication
    private val processedTransactionNonces = HashSet<String>()

    // Track rewarded ad usage limits
    private var dailyAdCount = 0
    private var lastAdWatchTime = 0L
    private const val MAX_DAILY_ADS = 15
    private const val MIN_AD_COOLDOWN_MS = 10_000L // 10 seconds between ads

    /**
     * Verifies whether an action passes click throttling / debouncing.
     */
    fun isActionAllowed(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastActionTimestamp < MIN_ACTION_INTERVAL_MS) {
            Log.w(TAG, "Action throttled due to rapid click abuse.")
            return false
        }
        lastActionTimestamp = now
        return true
    }

    /**
     * Validates an ad watch request against daily caps and cooldown timer.
     */
    fun canWatchRewardedAd(): Pair<Boolean, String?> {
        val now = System.currentTimeMillis()
        if (dailyAdCount >= MAX_DAILY_ADS) {
            return Pair(false, "Daily rewarded ad limit reached ($MAX_DAILY_ADS/day). Try again tomorrow.")
        }
        if (now - lastAdWatchTime < MIN_AD_COOLDOWN_MS) {
            val secondsLeft = ((MIN_AD_COOLDOWN_MS - (now - lastAdWatchTime)) / 1000) + 1
            return Pair(false, "Please wait $secondsLeft seconds before watching another ad.")
        }
        return Pair(true, null)
    }

    /**
     * Registers an ad watch attempt.
     */
    fun recordRewardedAdWatched() {
        dailyAdCount++
        lastAdWatchTime = System.currentTimeMillis()
    }

    /**
     * Validates that a transaction nonce has not been previously executed (anti-replay).
     */
    fun validateAndConsumeNonce(nonce: String): Boolean {
        if (nonce.isEmpty()) return false
        synchronized(processedTransactionNonces) {
            if (processedTransactionNonces.contains(nonce)) {
                Log.e(TAG, "Duplicate reward claim blocked for nonce: $nonce")
                return false
            }
            // Keep memory bound to last 500 transaction nonces
            if (processedTransactionNonces.size > 500) {
                processedTransactionNonces.clear()
            }
            processedTransactionNonces.add(nonce)
            return true
        }
    }

    /**
     * Validates device time to detect backward clock manipulation.
     */
    fun validateTimeIntegrity(lastSavedTimestamp: Long): Boolean {
        val currentMs = System.currentTimeMillis()
        // If current system time is more than 1 hour prior to last recorded claim, clock was set backwards
        if (lastSavedTimestamp > 0 && currentMs < lastSavedTimestamp - (3600 * 1000L)) {
            Log.e(TAG, "Offline clock manipulation detected! System time: $currentMs vs Saved: $lastSavedTimestamp")
            return false
        }
        return true
    }

    /**
     * Generates secure HMAC signature for local save state validation.
     */
    fun generateChecksum(score: Int, coins: Int, xp: Long, level: Int): String {
        return try {
            val data = "$score:$coins:$xp:$level:$SECRET_SEED"
            val mac = Mac.getInstance("HmacSHA256")
            val keySpec = SecretKeySpec(SECRET_SEED.toByteArray(), "HmacSHA256")
            mac.init(keySpec)
            val hash = mac.doFinal(data.toByteArray())
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Validates save state checksum. Returns true if untampered or if new record.
     */
    fun verifyChecksum(score: Int, coins: Int, xp: Long, level: Int, expectedChecksum: String): Boolean {
        if (expectedChecksum.isEmpty()) return true // Backward compatible initial load
        val calculated = generateChecksum(score, coins, xp, level)
        val valid = calculated == expectedChecksum
        if (!valid) {
            Log.e(TAG, "Save state checksum mismatch! Untampered integrity test failed.")
        }
        return valid
    }

    /**
     * Sanity check for earned coins in a single match to prevent integer overflow or injection.
     */
    fun validateMatchCoins(coins: Int, durationSec: Long, linesCleared: Int): Int {
        if (coins <= 0) return 0
        // Reasonable max coins achievable per minute is ~500
        val maxFeasibleCoins = maxOf(300, (durationSec / 60 + 1).toInt() * 600 + (linesCleared * 30))
        val sanitized = coins.coerceAtMost(maxFeasibleCoins)
        if (sanitized != coins) {
            Log.w(TAG, "Coins clamped by anti-cheat: Original=$coins, Sanitized=$sanitized")
        }
        return sanitized
    }
}
