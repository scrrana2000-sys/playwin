package com.playwin.ads

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object TelemetryManager {
    private val database: FirebaseDatabase by lazy {
        try {
            val db = FirebaseDatabase.getInstance("https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app")
            try {
                db.setPersistenceEnabled(true)
            } catch (e: Exception) {
                // Ignore if already set
            }
            db
        } catch (e: Exception) {
            Log.e("TelemetryManager", "Failed to initialize FirebaseDatabase: ${e.message}", e)
            throw e
        }
    }

    private var currentSessionId: String = ""
    private var currentSessionUid: String = ""
    private var sessionStartTime: Long = 0L

    init {
        try {
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    if (currentSessionUid.isNotEmpty() && currentSessionUid != uid) {
                        if (currentSessionId.isNotEmpty()) {
                            onSessionEnd(currentSessionUid)
                        }
                        currentSessionUid = uid
                    }
                } else {
                    if (currentSessionUid.isNotEmpty() && currentSessionId.isNotEmpty()) {
                        onSessionEnd(currentSessionUid)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TelemetryManager", "Error in AuthStateListener initialization", e)
        }
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun getUid(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    @Synchronized
    fun getSessionId(uid: String): String {
        if (uid.isEmpty()) return ""
        if (currentSessionId.isEmpty() || currentSessionUid != uid) {
            onSessionStart(uid)
        }
        return currentSessionId
    }

    @Synchronized
    fun onSessionStart(uid: String) {
        if (uid.isEmpty()) return
        if (currentSessionUid == uid && currentSessionId.isNotEmpty()) {
            Log.i("TelemetryManager", "Session already active ($currentSessionId) for UID: $uid. Skipping duplicate start.")
            return
        }
        if (currentSessionUid.isNotEmpty() && currentSessionUid != uid && currentSessionId.isNotEmpty()) {
            onSessionEnd(currentSessionUid)
        }

        currentSessionId = UUID.randomUUID().toString()
        currentSessionUid = uid
        sessionStartTime = System.currentTimeMillis()

        val sessionData = mapOf(
            "sessionId" to currentSessionId,
            "uid" to uid,
            "startedAt" to sessionStartTime,
            "endedAt" to 0L,
            "activeSeconds" to 0L
        )

        try {
            database.getReference("userSessions").child(uid).child(currentSessionId).setValue(sessionData)
            
            val dateStr = getCurrentDateString()
            updateDailySummary(uid, dateStr) { data ->
                setChildMinLong(data, "firstActiveAt", sessionStartTime)
                setChildMaxLong(data, "lastActiveAt", sessionStartTime)
                incrementChildInt(data, "totalSessions", 1)
            }
            Log.i("TelemetryManager", "Session started: $currentSessionId for UID: $uid")
        } catch (e: Exception) {
            Log.e("TelemetryManager", "Error starting session: ${e.message}")
        }
    }

    @Synchronized
    fun onSessionEnd(uid: String) {
        val sessionId = currentSessionId
        val startTime = sessionStartTime
        if (uid.isEmpty() || sessionId.isEmpty() || startTime == 0L || currentSessionUid != uid) return

        val endTime = System.currentTimeMillis()
        val activeSeconds = (endTime - startTime) / 1000L

        val updates = mapOf(
            "endedAt" to endTime,
            "activeSeconds" to activeSeconds
        )

        try {
            database.getReference("userSessions").child(uid).child(sessionId).updateChildren(updates)

            val dateStr = getCurrentDateString()
            updateDailySummary(uid, dateStr) { data ->
                setChildMaxLong(data, "lastActiveAt", endTime)
                incrementChildLong(data, "totalActiveSeconds", activeSeconds)
            }
            Log.i("TelemetryManager", "Session ended: $sessionId for UID: $uid, active seconds: $activeSeconds")
        } catch (e: Exception) {
            Log.e("TelemetryManager", "Error ending session: ${e.message}")
        } finally {
            currentSessionId = ""
            currentSessionUid = ""
            sessionStartTime = 0L
        }
    }

    /**
     * Canonical method for logging or updating ad impression and revenue events.
     * Guarantees 1 canonical adTelemetry record per eventId and prevents double counting.
     */
    fun logOrUpdateAdTelemetry(
        eventId: String,
        adFormat: String? = null,
        adUnitId: String? = null,
        source: String? = null,
        rewardType: String? = null,
        valueMicros: Long? = null,
        currencyCode: String? = null,
        precision: Int? = null,
        revenueStatus: String? = null,
        rewardGranted: Boolean? = null,
        rewardValue: Int? = null
    ) {
        val uid = getUid()
        if (uid.isEmpty() || eventId.isEmpty()) {
            Log.w("TelemetryManager", "Skipping logOrUpdateAdTelemetry: UID or eventId is empty")
            return
        }

        val timestamp = System.currentTimeMillis()
        val dateStr = getCurrentDateString()
        val sessionId = getSessionId(uid)

        val ref = database.getReference("adTelemetry").child(uid).child(eventId)

        ref.runTransaction(object : Transaction.Handler {
            private var isNewEvent = false
            private var formatToCount = "REWARDED"
            private var addPaidEvent = false
            private var revenueMicrosToAdd = 0L

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                isNewEvent = false
                formatToCount = "REWARDED"
                addPaidEvent = false
                revenueMicrosToAdd = 0L

                val existingEventId = mutableData.child("eventId").value as? String
                if (existingEventId == null) {
                    // Initialize new event
                    isNewEvent = true
                    val fmt = adFormat ?: "REWARDED"
                    formatToCount = fmt

                    mutableData.child("eventId").value = eventId
                    mutableData.child("uid").value = uid
                    mutableData.child("timestamp").value = timestamp
                    mutableData.child("createdAt").value = timestamp
                    mutableData.child("updatedAt").value = timestamp
                    mutableData.child("date").value = dateStr
                    mutableData.child("adFormat").value = fmt
                    mutableData.child("adUnitId").value = adUnitId ?: AdConstants.REWARDED_AD_UNIT_ID
                    mutableData.child("source").value = source ?: "OTHER"
                    mutableData.child("rewardType").value = rewardType ?: ""
                    mutableData.child("sessionId").value = sessionId
                    mutableData.child("valueMicros").value = valueMicros ?: 0L
                    mutableData.child("currencyCode").value = currencyCode ?: "USD"
                    mutableData.child("precision").value = precision ?: 0
                    mutableData.child("revenueStatus").value = revenueStatus ?: "PENDING"
                    mutableData.child("rewardGranted").value = rewardGranted ?: false
                    mutableData.child("rewardValue").value = rewardValue ?: 0
                    mutableData.child("revenueCounted").value = false
                } else {
                    // Update existing event
                    mutableData.child("updatedAt").value = timestamp
                    if (adFormat != null) mutableData.child("adFormat").value = adFormat
                    if (adUnitId != null) mutableData.child("adUnitId").value = adUnitId
                    if (source != null) mutableData.child("source").value = source
                    if (rewardType != null) mutableData.child("rewardType").value = rewardType
                    if (valueMicros != null) mutableData.child("valueMicros").value = valueMicros
                    if (currencyCode != null) mutableData.child("currencyCode").value = currencyCode
                    if (precision != null) mutableData.child("precision").value = precision
                    if (revenueStatus != null) mutableData.child("revenueStatus").value = revenueStatus
                    if (rewardGranted != null) mutableData.child("rewardGranted").value = rewardGranted
                    if (rewardValue != null) mutableData.child("rewardValue").value = rewardValue
                    formatToCount = mutableData.child("adFormat").value as? String ?: "REWARDED"
                }

                val currentRevenueStatus = mutableData.child("revenueStatus").value as? String ?: ""
                val currentMicros = (mutableData.child("valueMicros").value as? Long) ?: 0L
                val isRevenueAlreadyCounted = (mutableData.child("revenueCounted").value as? Boolean) ?: false

                if (!isRevenueAlreadyCounted && (currentRevenueStatus == "CONFIRMED" || currentMicros > 0)) {
                    addPaidEvent = true
                    revenueMicrosToAdd = currentMicros
                    mutableData.child("revenueCounted").value = true
                    if (currentRevenueStatus != "CONFIRMED") {
                        mutableData.child("revenueStatus").value = "CONFIRMED"
                    }
                }

                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    Log.e("TelemetryManager", "Error updating adTelemetry $eventId: ${error.message}")
                } else if (committed) {
                    Log.i("TelemetryManager", "Successfully updated adTelemetry event $eventId. isNew=$isNewEvent, addPaid=$addPaidEvent")
                    
                    updateDailySummaryForAdEvent(
                        uid = uid,
                        dateStr = dateStr,
                        timestamp = timestamp,
                        isNewImpression = isNewEvent,
                        adFormat = formatToCount,
                        addPaidEvent = addPaidEvent,
                        revenueMicros = revenueMicrosToAdd
                    )
                }
            }
        })
    }

    /**
     * Finalizes pending ad events when dismissed or closed.
     * Sets revenueStatus to UNAVAILABLE if no paid callback arrived.
     */
    fun finalizeAdEventIfPending(eventId: String) {
        val uid = getUid()
        if (uid.isEmpty() || eventId.isEmpty()) return

        val ref = database.getReference("adTelemetry").child(uid).child(eventId)
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val status = mutableData.child("revenueStatus").value as? String
                if (status == "PENDING") {
                    mutableData.child("revenueStatus").value = "UNAVAILABLE"
                    mutableData.child("updatedAt").value = System.currentTimeMillis()
                }
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
        })
    }

    /**
     * Legacy wrapper for logAdImpression - routes to logOrUpdateAdTelemetry with a unique event ID.
     */
    fun logAdImpression(
        adFormat: String,
        adUnitId: String,
        source: String,
        valueMicros: Long,
        currencyCode: String,
        precision: Int,
        revenueStatus: String
    ) {
        val eventId = UUID.randomUUID().toString()
        logOrUpdateAdTelemetry(
            eventId = eventId,
            adFormat = adFormat,
            adUnitId = adUnitId,
            source = source,
            valueMicros = valueMicros,
            currencyCode = currencyCode,
            precision = precision,
            revenueStatus = revenueStatus
        )
    }

    private fun updateDailySummaryForAdEvent(
        uid: String,
        dateStr: String,
        timestamp: Long,
        isNewImpression: Boolean,
        adFormat: String,
        addPaidEvent: Boolean,
        revenueMicros: Long
    ) {
        if (uid.isEmpty()) return
        updateDailySummary(uid, dateStr) { data ->
            setChildMaxLong(data, "lastActiveAt", timestamp)
            setChildMinLong(data, "firstActiveAt", timestamp)

            if (isNewImpression) {
                incrementChildInt(data, "totalAdImpressions", 1)
                when (adFormat) {
                    "REWARDED" -> incrementChildInt(data, "rewardedImpressions", 1)
                    "BANNER" -> incrementChildInt(data, "bannerImpressions", 1)
                    "NATIVE" -> incrementChildInt(data, "nativeImpressions", 1)
                    "INTERSTITIAL" -> incrementChildInt(data, "interstitialImpressions", 1)
                }
            }

            if (addPaidEvent) {
                incrementChildInt(data, "rewardedPaidEvents", 1)
                if (revenueMicros > 0) {
                    incrementChildLong(data, "reportedRevenueMicros", revenueMicros)
                }
            }
        }
    }

    fun logCoinReward(
        source: String,
        coins: Int,
        rewardType: String,
        referenceId: String
    ) {
        val uid = getUid()
        if (uid.isEmpty()) {
            Log.w("TelemetryManager", "Skipping coin reward logging: UID is empty")
            return
        }

        val timestamp = System.currentTimeMillis()
        val dateStr = getCurrentDateString()
        val eventId = if (referenceId.isNotEmpty()) referenceId else "coin_reward_${UUID.randomUUID()}"

        val ref = database.getReference("coinRewardEvents").child(uid).child(eventId)

        ref.runTransaction(object : Transaction.Handler {
            private var isNewEvent = false

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                isNewEvent = false
                val existingEventId = mutableData.child("eventId").value as? String
                if (existingEventId == null) {
                    isNewEvent = true
                    mutableData.child("eventId").value = eventId
                    mutableData.child("uid").value = uid
                    mutableData.child("timestamp").value = timestamp
                    mutableData.child("date").value = dateStr
                    mutableData.child("source").value = source
                    mutableData.child("coins").value = coins
                    mutableData.child("rewardType").value = rewardType
                    mutableData.child("referenceId").value = referenceId
                }
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    Log.e("TelemetryManager", "Error logging coin reward $eventId: ${error.message}")
                } else if (committed && isNewEvent) {
                    Log.i("TelemetryManager", "Successfully logged new coin reward: $eventId")
                    updateDailySummary(uid, dateStr) { data ->
                        incrementChildInt(data, "coinsEarned", coins)
                        incrementChildInt(data, "coinRewardEvents", 1)
                        setChildMaxLong(data, "lastActiveAt", timestamp)
                        setChildMinLong(data, "firstActiveAt", timestamp)
                    }
                } else {
                    Log.i("TelemetryManager", "Coin reward $eventId already recorded, skipping duplicate daily summary update")
                }
            }
        })
    }

    fun logGameActivity(
        game: String,
        eventType: String,
        gameSessionDuration: Long,
        rewardedAdShown: Boolean,
        rewardEarned: Boolean,
        coinsEarned: Int,
        score: Int = 0
    ) {
        val uid = getUid()
        if (uid.isEmpty()) {
            Log.w("TelemetryManager", "Skipping game activity logging: UID is empty")
            return
        }

        val eventId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val dateStr = getCurrentDateString()
        val sessionId = getSessionId(uid)

        val eventData = mutableMapOf<String, Any>(
            "eventId" to eventId,
            "uid" to uid,
            "timestamp" to timestamp,
            "date" to dateStr,
            "game" to game,
            "eventType" to eventType,
            "sessionId" to sessionId,
            "gameSessionDuration" to gameSessionDuration,
            "rewardedAdShown" to rewardedAdShown,
            "rewardEarned" to rewardEarned,
            "coinsEarned" to coinsEarned,
            "score" to score
        )

        try {
            database.getReference("gameTelemetry").child(uid).child(eventId).setValue(eventData)

            updateDailySummary(uid, dateStr) { data ->
                setChildMaxLong(data, "lastActiveAt", timestamp)
                setChildMinLong(data, "firstActiveAt", timestamp)

                if (eventType == "gameStarted") {
                    when (game) {
                        "SNAKE" -> incrementChildInt(data, "snakeSessions", 1)
                        "BOUNCE" -> incrementChildInt(data, "bounceSessions", 1)
                        "BINGO" -> incrementChildInt(data, "bingoSessions", 1)
                        "SHADOW_HERO" -> incrementChildInt(data, "shadowHeroSessions", 1)
                    }
                }
            }
            Log.i("TelemetryManager", "Logged game activity: $game, event: $eventType")
        } catch (e: Exception) {
            Log.e("TelemetryManager", "Error logging game activity: ${e.message}")
        }
    }

    fun logQuizActivity(
        eventType: String,
        questionsAnswered: Int,
        correctAnswers: Int,
        wrongAnswers: Int,
        lifelineUsed: Boolean,
        rewardedLifelineAd: Boolean,
        coinsEarned: Int,
        duration: Long
    ) {
        val uid = getUid()
        if (uid.isEmpty()) {
            Log.w("TelemetryManager", "Skipping quiz activity logging: UID is empty")
            return
        }

        val eventId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val dateStr = getCurrentDateString()
        val sessionId = getSessionId(uid)

        val eventData = mapOf(
            "eventId" to eventId,
            "uid" to uid,
            "timestamp" to timestamp,
            "date" to dateStr,
            "game" to "QUIZ",
            "eventType" to eventType,
            "sessionId" to sessionId,
            "gameSessionDuration" to duration,
            "questionsAnswered" to questionsAnswered,
            "correctAnswers" to correctAnswers,
            "wrongAnswers" to wrongAnswers,
            "lifelineUsed" to lifelineUsed,
            "rewardedLifelineAd" to rewardedLifelineAd,
            "coinsEarned" to coinsEarned,
            "rewardedAdShown" to rewardedLifelineAd,
            "rewardEarned" to (coinsEarned > 0)
        )

        try {
            database.getReference("gameTelemetry").child(uid).child(eventId).setValue(eventData)

            updateDailySummary(uid, dateStr) { data ->
                setChildMaxLong(data, "lastActiveAt", timestamp)
                setChildMinLong(data, "firstActiveAt", timestamp)

                if (eventType == "quizStarted") {
                    incrementChildInt(data, "quizPlays", 1)
                }
            }
            Log.i("TelemetryManager", "Logged quiz activity: $eventType")
        } catch (e: Exception) {
            Log.e("TelemetryManager", "Error logging quiz activity: ${e.message}")
        }
    }

    fun logSpinPlay(coinsEarned: Int, isRewarded: Boolean) {
        val uid = getUid()
        if (uid.isEmpty()) return
        val dateStr = getCurrentDateString()
        val timestamp = System.currentTimeMillis()

        updateDailySummary(uid, dateStr) { data ->
            incrementChildInt(data, "spinPlays", 1)
            setChildMaxLong(data, "lastActiveAt", timestamp)
            setChildMinLong(data, "firstActiveAt", timestamp)
        }
    }

    fun logScratchPlay(coinsEarned: Int, isRewarded: Boolean) {
        val uid = getUid()
        if (uid.isEmpty()) return
        val dateStr = getCurrentDateString()
        val timestamp = System.currentTimeMillis()

        updateDailySummary(uid, dateStr) { data ->
            incrementChildInt(data, "scratchPlays", 1)
            setChildMaxLong(data, "lastActiveAt", timestamp)
            setChildMinLong(data, "firstActiveAt", timestamp)
        }
    }

    private fun updateDailySummary(uid: String, dateStr: String, block: (MutableData) -> Unit) {
        if (uid.isEmpty()) return
        val ref = database.getReference("userDailyEconomy").child(uid).child(dateStr)
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                block(mutableData)
                mutableData.child("updatedAt").value = System.currentTimeMillis()
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    Log.e("TelemetryManager", "Daily summary transaction failed: ${error.message}")
                }
            }
        })
    }

    private fun incrementChildLong(data: MutableData, path: String, delta: Long) {
        val current = data.child(path).value as? Long ?: 0L
        data.child(path).value = current + delta
    }

    private fun incrementChildInt(data: MutableData, path: String, delta: Int) {
        val current = data.child(path).value as? Long ?: 0L
        data.child(path).value = current + delta
    }

    private fun setChildMinLong(data: MutableData, path: String, value: Long) {
        val current = data.child(path).value as? Long ?: 0L
        if (current == 0L || value < current) {
            data.child(path).value = value
        }
    }

    private fun setChildMaxLong(data: MutableData, path: String, value: Long) {
        val current = data.child(path).value as? Long ?: 0L
        if (value > current) {
            data.child(path).value = value
        }
    }
}
