package com.myplaywin.app.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

@com.google.firebase.database.IgnoreExtraProperties
data class FirebaseDailySystem(
    val serverLastSync: Long = 0L,
    val lastResetTimestamp: Long = 0L,
    val nextResetTimestamp: Long = 0L,
    val dailyVersion: Long = 1L
)

object DailyResetManager {
    private const val TAG = "DailyResetManager"
    private const val DB_URL = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"

    private val _currentServerTime = MutableStateFlow<Long>(System.currentTimeMillis())
    val currentServerTime: StateFlow<Long> = _currentServerTime.asStateFlow()

    private val _nextResetTimestamp = MutableStateFlow<Long>(0L)
    val nextResetTimestamp: StateFlow<Long> = _nextResetTimestamp.asStateFlow()

    private val _remainingTime = MutableStateFlow<String>("00:00:00")
    val remainingTime: StateFlow<String> = _remainingTime.asStateFlow()

    private val _dailySystemState = MutableStateFlow<FirebaseDailySystem?>(null)
    val dailySystemState: StateFlow<FirebaseDailySystem?> = _dailySystemState.asStateFlow()

    private var serverTimeOffset = 0L
    private var isCountdownStarted = false
    private var syncJob: Job? = null
    private var resetListener: ValueEventListener? = null

    // Start listening to the server time offset and start the countdown
    fun startRealtimeCountdown(scope: CoroutineScope) {
        if (isCountdownStarted) return
        isCountdownStarted = true

        val database = FirebaseDatabase.getInstance(DB_URL)
        
        // Listen to serverTimeOffset
        database.getReference(".info/serverTimeOffset").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                serverTimeOffset = snapshot.getValue(Long::class.java) ?: 0L
                Log.d(TAG, "SYNC_SUCCESS - Offset: $serverTimeOffset ms")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "SYNC_FAILED - ${error.message}")
            }
        })

        // Countdown coroutine
        scope.launch {
            while (isActive) {
                val nowServer = System.currentTimeMillis() + serverTimeOffset
                _currentServerTime.value = nowServer

                val nextReset = getNextResetUtc(nowServer)
                _nextResetTimestamp.value = nextReset

                val diffMs = nextReset - nowServer
                if (diffMs <= 0) {
                    _remainingTime.value = "00:00:00"
                    // Auto reset trigger when nextReset is crossed
                    if (isResetRequired()) {
                        triggerLocalAutoReset()
                    }
                } else {
                    val sec = (diffMs / 1000) % 60
                    val min = (diffMs / (1000 * 60)) % 60
                    val hr = (diffMs / (1000 * 60 * 60)) % 24
                    _remainingTime.value = String.format(Locale.US, "%02d:%02d:%02d", hr, min, sec)
                }

                // Log periodically (approx. every minute)
                if (System.currentTimeMillis() % 60000 < 1000) {
                    Log.d(TAG, "SERVER_TIME: $nowServer | NEXT_RESET: $nextReset | COUNTDOWN: ${_remainingTime.value}")
                }

                delay(1000)
            }
        }
    }

    // Helper to calculate midnight start of today (00:00 UTC)
    fun getStartOfTodayUtc(serverTime: Long): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = serverTime
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Helper to calculate midnight of tomorrow (00:00 UTC)
    fun getNextResetUtc(serverTime: Long): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = serverTime
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.timeInMillis
    }

    // Check if daily reset is required
    fun isResetRequired(): Boolean {
        val nowServer = _currentServerTime.value
        val lastReset = _dailySystemState.value?.lastResetTimestamp ?: 0L
        val startOfToday = getStartOfTodayUtc(nowServer)
        return lastReset < startOfToday
    }

    // Observe user's dailyReset node
    fun observeUserDailySystem(userId: String, scope: CoroutineScope) {
        if (userId.isEmpty()) return
        
        val database = FirebaseDatabase.getInstance(DB_URL)
        val dailySystemRef = database.getReference("users").child(userId).child("dailySystem")

        resetListener?.let { dailySystemRef.removeEventListener(it) }

        resetListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dailySystem = snapshot.getValue(FirebaseDailySystem::class.java)
                _dailySystemState.value = dailySystem
                
                // If loaded data says we need reset, trigger it immediately
                if (isResetRequired()) {
                    performDailyReset(userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to observe dailySystem: ${error.message}")
            }
        }
        
        dailySystemRef.addValueEventListener(resetListener!!)
    }

    private fun triggerLocalAutoReset() {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        performDailyReset(userId)
    }

    // Execute the centralized Daily Reset transaction across the app
    fun performDailyReset(userId: String) {
        if (userId.isEmpty()) return
        Log.d(TAG, "RESET_STARTED")

        val database = FirebaseDatabase.getInstance(DB_URL)
        val userRef = database.getReference("users").child(userId)

        userRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(mutableData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                if (mutableData.value == null) {
                    return com.google.firebase.database.Transaction.success(mutableData)
                }

                val nowServer = System.currentTimeMillis() + serverTimeOffset
                val startOfToday = getStartOfTodayUtc(nowServer)
                val nextReset = getNextResetUtc(nowServer)

                // Read dailySystem block or create it
                val dsNode = mutableData.child("dailySystem")
                val lastReset = dsNode.child("lastResetTimestamp").getValue(Long::class.java) ?: 0L

                // If already reset today during this transaction, abort or succeed without duplicating
                if (lastReset >= startOfToday) {
                    return com.google.firebase.database.Transaction.success(mutableData)
                }

                // Increment version
                val currentVersion = dsNode.child("dailyVersion").getValue(Long::class.java) ?: 0L
                val nextVersion = currentVersion + 1

                // Write dailySystem fields
                dsNode.child("serverLastSync").value = nowServer
                dsNode.child("lastResetTimestamp").value = nowServer
                dsNode.child("nextResetTimestamp").value = nextReset
                dsNode.child("dailyVersion").value = nextVersion

                // Automatically reset daily limits for all features
                
                // 1. Spin Wheel Resets
                mutableData.child("remainingSpins").value = 2 // standard dailyFreeSpins
                mutableData.child("dailySpinCount").value = 0
                mutableData.child("rewardedSpinCount").value = 0
                mutableData.child("freeSpinUsed").value = false
                mutableData.child("rewardAdSpinUsed").value = false
                
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val dateStr = sdf.format(Date(nowServer))
                mutableData.child("lastSpinDate").value = dateStr

                // 2. Scratch Cards Resets
                mutableData.child("remainingScratchCards").value = 1
                mutableData.child("scratchesToday").value = 0
                mutableData.child("freeScratchUsed").value = false
                mutableData.child("rewardAdScratchUsed").value = false
                mutableData.child("lastScratchDate").value = dateStr

                // 3. Quiz Attempts Reset
                val quizProgressNode = mutableData.child("quizProgress")
                quizProgressNode.child("lastQuizDate").value = dateStr
                quizProgressNode.child("dailyQuizCompleted").value = false

                // 4. Watch Ads Daily Limits
                mutableData.child("dailyAdsWatched").value = 0
                mutableData.child("lastAdResetTime").value = nowServer

                val rewardAdsNode = mutableData.child("reward_ads")
                rewardAdsNode.child("todayCount").value = 0
                rewardAdsNode.child("todayCoins").value = 0
                rewardAdsNode.child("bonusProgress").value = 0
                rewardAdsNode.child("bonusClaimed").value = 0
                rewardAdsNode.child("lastResetTimestamp").value = 0L
                rewardAdsNode.child("status").value = "Ready"

                // 5. Daily Task Status / Reset tasks active times
                mutableData.child("lastCheckInTime").value = 0L // reset so check-in becomes available
                
                // Extra fields requested
                mutableData.child("luckyDrawRegistered").value = false
                mutableData.child("referralsToday").value = 0
                mutableData.child("missionsCompletedToday").value = 0

                return com.google.firebase.database.Transaction.success(mutableData)
            }

            override fun onComplete(
                error: com.google.firebase.database.DatabaseError?,
                committed: Boolean,
                currentData: com.google.firebase.database.DataSnapshot?
            ) {
                if (committed && error == null) {
                    Log.d(TAG, "RESET_COMPLETED")
                } else {
                    Log.e(TAG, "Reset transaction failed: ${error?.message}")
                }
            }
        })
    }
}
