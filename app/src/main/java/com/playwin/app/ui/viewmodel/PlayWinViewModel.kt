package com.playwin.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playwin.app.data.database.PlayWinDatabase
import com.playwin.app.data.model.*
import com.playwin.app.data.repository.PlayWinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.sync.withLock

sealed interface AuthState {
    object Loading : AuthState
    data class Authenticated(val userId: String) : AuthState
    object Unauthenticated : AuthState
}

class PlayWinViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlayWinRepository

    val walletState: StateFlow<UserWallet>
    val transactionsState: StateFlow<List<FirebaseTransaction>>
    val tasksState: StateFlow<List<FirebaseTask>>
    val couponsState: StateFlow<List<FirebaseCoupon>>
    val spinRewardsState: StateFlow<List<com.playwin.app.data.model.FirebaseSpinReward>>
    val spinWheelConfigState: StateFlow<com.playwin.app.data.model.FirebaseSpinWheelConfig>
    val scratchCardSettingsState: StateFlow<com.playwin.app.data.model.FirebaseScratchCardSettings>
    val scratchCardRewardsState: StateFlow<List<com.playwin.app.data.model.FirebaseScratchCardReward>>

    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow("All")
    val currentPage = MutableStateFlow(1)
    val pageSize = 10

    private val _firebaseTransactions = MutableStateFlow<List<FirebaseTransaction>>(emptyList())
    private var firebaseUserJob: kotlinx.coroutines.Job? = null
    private var firebaseTxJob: kotlinx.coroutines.Job? = null
    private var firebaseRedemptionJob: kotlinx.coroutines.Job? = null
    private var firebaseUserDailyCheckInJob: kotlinx.coroutines.Job? = null
    private var firebaseUserScratchCardJob: kotlinx.coroutines.Job? = null
    private var dailyQuizJob: kotlinx.coroutines.Job? = null

    val redemptionsState = MutableStateFlow<List<com.playwin.app.data.model.FirebaseRedemption>>(emptyList())
    private val _isRedeeming = MutableStateFlow(false)
    val isRedeeming: StateFlow<Boolean> = _isRedeeming.asStateFlow()

    // UPI WITHDRAWAL AND GLOBAL STATE FLOWS
    val withdrawRequestsState = MutableStateFlow<List<com.playwin.app.data.model.FirebaseWithdrawRequest>>(emptyList())
    val allUsersState = MutableStateFlow<List<com.playwin.app.data.model.FirebaseUser>>(emptyList())
    val couponRedemptionsState = MutableStateFlow<List<com.playwin.app.data.model.FirebaseCouponRedemption>>(emptyList())
    val currentUserBlockedState = MutableStateFlow(false)
    val currentUserState = MutableStateFlow<com.playwin.app.data.model.FirebaseUser?>(null)

    private val _isSubmittingWithdraw = MutableStateFlow(false)
    val isSubmittingWithdraw: StateFlow<Boolean> = _isSubmittingWithdraw.asStateFlow()

    private var withdrawRequestsJob: kotlinx.coroutines.Job? = null
    private var allUsersJob: kotlinx.coroutines.Job? = null
    private var couponRedemptionsJob: kotlinx.coroutines.Job? = null
    private var firebaseQuizProgressJob: kotlinx.coroutines.Job? = null
    private var firebaseCompletedQuizzesJob: kotlinx.coroutines.Job? = null
    private var firebaseWeeklyQuizProgressJob: kotlinx.coroutines.Job? = null
    private var referralsHistoryJob: kotlinx.coroutines.Job? = null
    private var quizzesJob: kotlinx.coroutines.Job? = null

    val quizProgressState = MutableStateFlow<com.playwin.app.data.model.FirebaseQuizProgress?>(null)
    
    // Realtime listeners for direct Firebase synchronization
    private var firebaseWalletListener: com.google.firebase.database.ValueEventListener? = null
    private var firebaseWalletSummaryListener: com.google.firebase.database.ValueEventListener? = null
    private var firebaseTransactionsListener: com.google.firebase.database.ValueEventListener? = null
    private var firebaseHistoryListener: com.google.firebase.database.ValueEventListener? = null
    private var firebaseScratchHistoryListener: com.google.firebase.database.ValueEventListener? = null
    val completedQuizzesState = MutableStateFlow<Map<String, com.playwin.app.data.model.FirebaseCompletedQuiz>>(emptyMap())
    val weeklyQuizProgressState = MutableStateFlow<Map<String, com.playwin.app.data.model.FirebaseWeeklyQuizProgress>>(emptyMap())
    val referralHistoryState = MutableStateFlow<List<FirebaseReferralRecord>>(emptyList())
    val quizzesState = MutableStateFlow<List<com.playwin.app.data.model.FirebaseQuiz>>(emptyList())
    
    val dailyCheckInSettingsState = MutableStateFlow<com.playwin.app.data.model.FirebaseDailyCheckInSettings?>(null)
    val dailyCheckInLoadingState = MutableStateFlow(true)
    val userDailyCheckInState = MutableStateFlow<com.playwin.app.data.model.FirebaseUserDailyCheckIn?>(null)
    val userScratchCardStateState = MutableStateFlow<com.playwin.app.data.model.FirebaseUserScratchCardState>(com.playwin.app.data.model.FirebaseUserScratchCardState())
    val dailyQuizState = MutableStateFlow<com.playwin.app.data.model.FirebaseUserDailyQuiz?>(null)
    var serverTimeOffset = 0L

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userCreatedStatus = MutableStateFlow<Boolean?>(null)
    val userCreatedStatus: StateFlow<Boolean?> = _userCreatedStatus.asStateFlow()

    private val _verificationEmailStatus = MutableStateFlow<Boolean?>(null)
    val verificationEmailStatus: StateFlow<Boolean?> = _verificationEmailStatus.asStateFlow()

    private val _verificationEmailError = MutableStateFlow<String?>(null)
    val verificationEmailError: StateFlow<String?> = _verificationEmailError.asStateFlow()

    fun clearDebugStatus() {
        _userCreatedStatus.value = null
        _verificationEmailStatus.value = null
        _verificationEmailError.value = null
    }

    init {
        val database = PlayWinDatabase.getDatabase(application)
        repository = PlayWinRepository(database.playWinDao())

        walletState = repository.userWallet
            .map { it ?: UserWallet() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserWallet()
            )

        transactionsState = kotlinx.coroutines.flow.combine(
            _firebaseTransactions,
            searchQuery,
            selectedFilter,
            currentPage
        ) { txList, query, filter, page ->
            var filtered = txList
            if (filter != "All") {
                filtered = filtered.filter { tx ->
                    when (filter) {
                        "Rewards" -> tx.type == "daily_reward" || tx.type == "spin_reward" || tx.type == "scratch_reward" || tx.type == "reward"
                        "Referrals" -> tx.type == "referral"
                        "Ads" -> tx.type == "video_ad"
                        "Spins" -> tx.type == "spin_reward"
                        "Scratch Cards" -> tx.type == "scratch_reward"
                        "Redeemed" -> tx.type == "coupon_redeemed" || tx.coins < 0
                        else -> true
                    }
                }
            }
            if (query.trim().isNotEmpty()) {
                val q = query.trim().lowercase(java.util.Locale.ROOT)
                filtered = filtered.filter { tx ->
                    tx.title.lowercase(java.util.Locale.ROOT).contains(q) || 
                    tx.type.lowercase(java.util.Locale.ROOT).contains(q)
                }
            }
            filtered.take(page * pageSize)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        tasksState = repository.firebaseTasksFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        couponsState = repository.firebaseCouponsFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        spinWheelConfigState = repository.firebaseSpinWheelConfigFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = com.playwin.app.data.model.FirebaseSpinWheelConfig()
            )

        spinRewardsState = spinWheelConfigState
            .map { config ->
                if (!config.enabled) {
                    emptyList()
                } else {
                    config.segments.filter { it.active || it.enabled }.sortedBy { it.order }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        scratchCardSettingsState = repository.firebaseScratchCardSettingsFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = com.playwin.app.data.model.FirebaseScratchCardSettings()
            )

        scratchCardRewardsState = repository.firebaseScratchCardRewardsFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Start centralized daily countdown
        com.playwin.app.data.repository.DailyResetManager.startRealtimeCountdown(viewModelScope)

        // Observe auth state changes to start/stop live Firebase sync
        try {
            val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
            val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
            db.getReference(".info/serverTimeOffset").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val offset = snapshot.getValue(Long::class.java) ?: 0L
                    serverTimeOffset = offset
                    android.util.Log.d("PlayWinTime", "Synced Server Time Offset: $offset ms")
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })

            val checkInSettingsRef = db.getReference("dailyCheckIn")
            checkInSettingsRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    if (snapshot.exists()) {
                        val settings = snapshot.getValue(com.playwin.app.data.model.FirebaseDailyCheckInSettings::class.java)
                        if (settings != null) {
                            dailyCheckInSettingsState.value = settings
                            val r = settings.rewards
                            if (r != null && r.size >= 7) {
                                android.util.Log.d("PlayWinCheckIn", "Loaded rewards:\nDay1=${r[0]}\nDay2=${r[1]}\nDay3=${r[2]}\nDay4=${r[3]}\nDay5=${r[4]}\nDay6=${r[5]}\nDay7=${r[6]}")
                            } else {
                                android.util.Log.w("PlayWinCheckIn", "Loaded rewards but list is missing or incomplete: $r")
                            }
                        }
                    } else {
                        val defaultSettings = com.playwin.app.data.model.FirebaseDailyCheckInSettings(
                            enabled = true,
                            rewards = listOf(20, 30, 40, 50, 60, 80, 120),
                            maxRewardLimit = 500
                        )
                        checkInSettingsRef.setValue(defaultSettings)
                        dailyCheckInSettingsState.value = defaultSettings
                    }
                    dailyCheckInLoadingState.value = false
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    dailyCheckInLoadingState.value = false
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("PlayWinTime", "Failed to register server time or settings listeners", e)
        }

        viewModelScope.launch {
            authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        startFirebaseSync(state.userId)
                    }
                    else -> {
                        stopFirebaseSync()
                    }
                }
            }
        }

        // Boot checking auth session
        checkAuthSession()
    }

    private fun checkAuthSession() {
        viewModelScope.launch {
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val sharedPrefs = getApplication<Application>().getSharedPreferences("playwin_prefs", Context.MODE_PRIVATE)
                val sessionActiveVal = sharedPrefs.getBoolean("session_active", false)
                val rememberMeVal = sharedPrefs.getBoolean("remember_me", false)
                val firebaseUser = auth.currentUser

                if (firebaseUser != null && sessionActiveVal && rememberMeVal) {
                    firebaseUser.reload().addOnCompleteListener { reloadTask ->
                        val updatedUser = auth.currentUser
                        if (updatedUser != null) {
                            if (!updatedUser.isEmailVerified) {
                                auth.signOut()
                                sharedPrefs.edit().putBoolean("session_active", false).apply()
                                _authState.value = AuthState.Unauthenticated
                            } else {
                                val uid = updatedUser.uid
                                viewModelScope.launch {
                                    val dbUser = repository.getFirebaseUser(uid)
                                    val currentLocalWallet = repository.userWallet.first()

                                    if (dbUser != null) {
                                        val syncWallet = UserWallet(
                                            id = 1,
                                            coins = dbUser.coins,
                                            dailyStreak = dbUser.streak,
                                            lastCheckInTime = dbUser.lastCheckInTime,
                                            userId = uid,
                                            dailyAdsWatched = dbUser.dailyAdsWatched,
                                            lastAdResetTime = dbUser.lastAdResetTime,
                                            referredBy = dbUser.referredBy,
                                            hasUsedReferralCode = dbUser.hasUsedReferralCodeBool,
                                            totalReferrals = dbUser.totalReferrals,
                                            remainingSpins = dbUser.remainingSpins,
                                            totalSpinRewards = dbUser.totalSpinRewards,
                                            remainingScratchCards = dbUser.remainingScratchCards,
                                            lastScratchResetTime = dbUser.lastScratchResetTime,
                                            totalScratchRewards = dbUser.totalScratchRewards,
                                            lastSpinDate = dbUser.lastSpinDate,
                                            freeSpinUsed = dbUser.freeSpinUsedBool,
                                            rewardAdSpinUsed = dbUser.rewardAdSpinUsedBool,
                                            dailySpinCount = dbUser.dailySpinCount,
                                            rewardedSpinCount = dbUser.rewardedSpinCount,
                                            lastCheckInDate = dbUser.lastCheckInDate,
                                            totalCheckInRewards = dbUser.totalCheckInRewards,
                                            lastRewardAdTime = dbUser.lastRewardAdTime,
                                            pendingRewards = dbUser.pendingRewards,
                                            referralsCoinsEarned = dbUser.referralsCoinsEarned
                                            )
                                        repository.saveWalletLocally(syncWallet)
                                    } else if (currentLocalWallet != null && currentLocalWallet.userId == uid) {
                                        // Local exists and is correct user, sync up to firebase
                                        repository.saveWalletLocally(currentLocalWallet)
                                    } else {
                                        // Build fresh with 0 coins
                                        repository.saveWalletLocally(UserWallet(id = 1, coins = 0, userId = uid))
                                    }

                                    _authState.value = AuthState.Authenticated(uid)
                                }
                            }
                        } else {
                            // User session mismatch, force signout
                            auth.signOut()
                            sharedPrefs.edit().putBoolean("session_active", false).apply()
                            _authState.value = AuthState.Unauthenticated
                        }
                    }
                } else {
                    // Sign out the user because either rememberMe is false or session is not active
                    if (firebaseUser != null) {
                        auth.signOut()
                    }
                    sharedPrefs.edit().putBoolean("session_active", false).apply()
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String, rememberMe: Boolean, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PlayWinVM", "Starting signInWithEmailAndPassword.")
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                
                // Save remember_me preference locally
                val sharedPrefs = getApplication<Application>().getSharedPreferences("playwin_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putBoolean("remember_me", rememberMe).apply()

                // Sign in with email and password using awaitTask()
                val authResult = auth.signInWithEmailAndPassword(email, password).awaitTask()
                val firebaseUser = authResult.user
                if (firebaseUser == null) {
                    throw Exception("Failed to obtain user session after sign in.")
                }
                
                // Add console log: Auth success
                android.util.Log.d("PlayWinVM", "Auth success")
                
                val uid = firebaseUser.uid
                // Add console log: UID obtained
                android.util.Log.d("PlayWinVM", "UID obtained")
                
                // Check if the user is email verified
                if (!firebaseUser.isEmailVerified) {
                    // Sign out immediately
                    auth.signOut()
                    onResult(false, "Please verify your email first. Check your inbox and spam folder.")
                } else {
                    handleVerifiedUserSuccess(firebaseUser, uid, email, onResult)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("PlayWinVM", "SignIn exception: ${e.localizedMessage}", e)
                onResult(false, e.localizedMessage ?: "Sign In failed.")
            }
        }
    }

    private suspend fun handleVerifiedUserSuccess(
        firebaseUser: com.google.firebase.auth.FirebaseUser,
        uid: String,
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        // Fetch user profile from users/{uid} with DB URL
        val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
        val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
        
        var existingUserObj: FirebaseUser? = null
        try {
            android.util.Log.d("PlayWinVM", "Checking direct profile at users/$uid")
            val directSnapshot = db.getReference("users/$uid").get().awaitTask()
            if (directSnapshot.exists()) {
                existingUserObj = directSnapshot.getValue(FirebaseUser::class.java)
                if (existingUserObj != null) {
                    android.util.Log.d("PlayWinVM", "Direct profile exists at users/$uid with coins: ${existingUserObj.coins}")
                }
            }
        } catch (ex: Exception) {
            android.util.Log.e("PlayWinVM", "Failed to fetch direct profile at users/$uid", ex)
        }

        if (existingUserObj != null) {
            // Profile exists, load state into local wallet
            val syncWallet = UserWallet(
                id = 1,
                coins = existingUserObj.coins,
                dailyStreak = existingUserObj.streak,
                lastCheckInTime = existingUserObj.lastCheckInTime,
                userId = uid,
                dailyAdsWatched = existingUserObj.dailyAdsWatched,
                lastAdResetTime = existingUserObj.lastAdResetTime,
                referredBy = existingUserObj.referredBy,
                hasUsedReferralCode = existingUserObj.hasUsedReferralCodeBool,
                totalReferrals = existingUserObj.totalReferrals,
                remainingSpins = existingUserObj.remainingSpins,
                totalSpinRewards = existingUserObj.totalSpinRewards,
                remainingScratchCards = existingUserObj.remainingScratchCards,
                lastScratchResetTime = existingUserObj.lastScratchResetTime,
                totalScratchRewards = existingUserObj.totalScratchRewards,
                lastSpinDate = existingUserObj.lastSpinDate,
                freeSpinUsed = existingUserObj.freeSpinUsedBool,
                rewardAdSpinUsed = existingUserObj.rewardAdSpinUsedBool,
                dailySpinCount = existingUserObj.dailySpinCount,
                rewardedSpinCount = existingUserObj.rewardedSpinCount,
                lastCheckInDate = existingUserObj.lastCheckInDate,
                totalCheckInRewards = existingUserObj.totalCheckInRewards,
                lastRewardAdTime = existingUserObj.lastRewardAdTime
            )
            repository.saveWalletLocally(syncWallet)
            
            try {
                // Direct update to lastActiveTime for UID
                db.getReference("users/$uid/lastActiveTime").setValue(System.currentTimeMillis()).awaitTask()
                android.util.Log.d("PlayWinVM", "Profile loaded directly, updated lastActiveTime for UID: $uid")
            } catch (dbEx: Exception) {
                android.util.Log.e("PlayWinVM", "Database update failed", dbEx)
                throw dbEx
            }
        } else {
            // Profile does not exist! Create it automatically.
            android.util.Log.d("PlayWinVM", "Writing brand new profile to users/$uid")
            
            val displayName = firebaseUser.displayName ?: "Player"
            android.util.Log.d("PlayWinVM", "Display Name Loaded: $displayName")
            val userMap = mapOf(
                "uid" to uid,
                "displayName" to displayName,
                "email" to email,
                "coins" to 0,
                "level" to 1,
                "streak" to 0,
                "referrals" to 0,
                "records" to 0,
                "createdAt" to System.currentTimeMillis(),
                
                // Legacy compatibility fields if needed
                "userId" to uid,
                "dailyStreak" to 0,
                "lastCheckInTime" to 0L,
                "lastActiveTime" to System.currentTimeMillis(),
                "dailyAdsWatched" to 0,
                "lastAdResetTime" to 0L,
                "lastSpinDate" to "",
                "freeSpinUsed" to false,
                "rewardAdSpinUsed" to false,
                "totalSpinRewards" to 0
            )
            
            try {
                // Await database write before navigating to Home
                db.getReference("users/$uid").setValue(userMap).awaitTask()
                android.util.Log.d("PlayWinVM", "Display Name Saved: $displayName")
                
                // Keep database initialized fields
                db.getReference("transactions/$uid").setValue("").awaitTask()
                db.getReference("rewardHistory/$uid").setValue("").awaitTask()
                db.getReference("dailyRewards/$uid").setValue("").awaitTask()
                db.getReference("referrals/$uid").setValue("").awaitTask()
                
                val restoredWallet = UserWallet(
                    id = 1,
                    coins = 0,
                    dailyStreak = 0,
                    lastCheckInTime = 0L,
                    userId = uid,
                    dailyAdsWatched = 0,
                    lastAdResetTime = 0L
                )
                repository.saveWalletLocally(restoredWallet)
            } catch (dbEx: Exception) {
                android.util.Log.e("PlayWinVM", "Database write failed")
                throw dbEx // Propagate to display exact error
            }
        }
        
        repository.clearLocalTransactions()
        val sharedPrefs = getApplication<Application>().getSharedPreferences("playwin_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("session_active", true).apply()
        
        _authState.value = AuthState.Authenticated(uid)
        onResult(true, null)
    }

    fun checkEmailVerification(email: String, password: String, rememberMe: Boolean, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PlayWinVM", "Checking email verification.")
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                
                // Save remember_me preference locally
                val sharedPrefs = getApplication<Application>().getSharedPreferences("playwin_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putBoolean("remember_me", rememberMe).apply()

                // Sign in with email and password first
                val authResult = auth.signInWithEmailAndPassword(email, password).awaitTask()
                val firebaseUser = authResult.user
                if (firebaseUser == null) {
                    throw Exception("Failed to obtain user session.")
                }
                
                // Reload to get fresh status
                firebaseUser.reload().awaitTask()
                
                if (firebaseUser.isEmailVerified) {
                    android.util.Log.d("PlayWinVM", "Auth success")
                    val uid = firebaseUser.uid
                    android.util.Log.d("PlayWinVM", "UID obtained")
                    
                    // Proceed to save profile/login
                    handleVerifiedUserSuccess(firebaseUser, uid, email, onResult)
                } else {
                    auth.signOut()
                    onResult(false, "Email not verified yet.")
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayWinVM", "Verification check failed", e)
                onResult(false, e.localizedMessage ?: "Email not verified yet.")
            }
        }
    }

    // Extension helper to await Tasks in suspended coroutines safely with cancellation support
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                @Suppress("UNCHECKED_CAST")
                continuation.resume(task.result as T) {
                    // Cleanup / handle cancellation if needed
                }
            } else {
                val exception = task.exception ?: Exception("Unknown Firebase Error")
                continuation.resumeWith(Result.failure(exception))
            }
        }
    }

    private fun formatFirebaseErrorCode(ex: Exception): String {
        if (ex is com.google.firebase.auth.FirebaseAuthException) {
            val code = ex.errorCode
            if (code.startsWith("ERROR_")) {
                return "auth/" + code.substring(6).lowercase().replace('_', '-')
            }
            return "auth/" + code.lowercase().replace('_', '-')
        }
        val msg = ex.message ?: ""
        if (msg.contains("network-request-failed") || msg.contains("network")) {
            return "auth/network-request-failed"
        } else if (msg.contains("too-many-requests")) {
            return "auth/too-many-requests"
        } else if (msg.contains("internal-error")) {
            return "auth/internal-error"
        }
        return "auth/internal-error"
    }

    fun signUpWithEmailAndPassword(email: String, password: String, displayName: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PlayWinVM", "Starting signUpWithEmailAndPassword. Email: $email, DisplayName: $displayName")
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

                _userCreatedStatus.value = null
                _verificationEmailStatus.value = null
                _verificationEmailError.value = null

                // Create user using Firebase Email and Password with optional timeout
                val authResult = kotlinx.coroutines.withTimeout(15000L) {
                    android.util.Log.d("PlayWinVM", "Calling createUserWithEmailAndPassword")
                    auth.createUserWithEmailAndPassword(email, password).awaitTask()
                }
                val firebaseUser = authResult.user
                if (firebaseUser == null) {
                    throw Exception("Failed to obtain registered user session.")
                }
                
                // Add console log: Auth success
                android.util.Log.d("PlayWinVM", "Auth success")
                _userCreatedStatus.value = true
                
                val uid = firebaseUser.uid
                // Add console log: UID obtained
                android.util.Log.d("PlayWinVM", "UID obtained")

                // 1. Log Display Name Entered
                android.util.Log.d("PlayWinVM", "Display Name Entered: $displayName")

                // 2. Update FirebaseAuth user profile
                try {
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    firebaseUser.updateProfile(profileUpdates).awaitTask()
                    android.util.Log.d("PlayWinVM", "Firebase auth user profile updated with displayName: $displayName")
                } catch (profileEx: Exception) {
                    android.util.Log.e("PlayWinVM", "Failed to update Firebase Auth user profile.", profileEx)
                }

                // 3. Immediately save exact Display Name and initial profile to Realtime Database
                try {
                    val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                    val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                    
                    val userMap = mapOf(
                        "uid" to uid,
                        "displayName" to displayName,
                        "email" to email,
                        "coins" to 0,
                        "level" to 1,
                        "streak" to 0,
                        "referrals" to 0,
                        "records" to 0,
                        "createdAt" to System.currentTimeMillis(),
                        
                        // Legacy compatibility fields if needed
                        "userId" to uid,
                        "dailyStreak" to 0,
                        "lastCheckInTime" to 0L,
                        "lastActiveTime" to System.currentTimeMillis(),
                        "dailyAdsWatched" to 0,
                        "lastAdResetTime" to 0L
                    )
                    
                    db.getReference("users/$uid").setValue(userMap).awaitTask()
                    android.util.Log.d("PlayWinVM", "Display Name Saved: $displayName")
                    
                    // Keep database initialized fields
                    db.getReference("transactions/$uid").setValue("").awaitTask()
                    db.getReference("rewardHistory/$uid").setValue("").awaitTask()
                    db.getReference("dailyRewards/$uid").setValue("").awaitTask()
                    db.getReference("referrals/$uid").setValue("").awaitTask()
                    
                    android.util.Log.d("PlayWinVM", "Brand new profile written to database during signUp for UID: $uid")
                } catch (dbEx: Exception) {
                    android.util.Log.e("PlayWinVM", "Failed to save profile during signUp to Realtime Database.", dbEx)
                }

                // Immediately send verification email
                try {
                    firebaseUser.sendEmailVerification().awaitTask()
                    // Add console log: Verification email sent
                    android.util.Log.d("PlayWinVM", "Verification email sent.")
                    _verificationEmailStatus.value = true
                } catch (evEx: Exception) {
                    // Print full error to console
                    android.util.Log.e("PlayWinVM", "Failed to send verification email.", evEx)
                    _verificationEmailStatus.value = false
                    val errCode = formatFirebaseErrorCode(evEx)
                    val errMsg = evEx.localizedMessage ?: "Unknown verification error"
                    _verificationEmailError.value = "$errCode: $errMsg"

                    // Automatically sign out the user immediately
                    auth.signOut()
                    _authState.value = AuthState.Unauthenticated
                    val sharedPrefs = getApplication<Application>().getSharedPreferences("playwin_prefs", Context.MODE_PRIVATE)
                    sharedPrefs.edit().putBoolean("session_active", false).apply()

                    onResult(false, "Verification email failed ($errCode) - $errMsg")
                    return@launch
                }

                // Automatically sign out the user immediately
                auth.signOut()
                _authState.value = AuthState.Unauthenticated

                val sharedPrefs = getApplication<Application>().getSharedPreferences("playwin_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putBoolean("session_active", false).apply()

                onResult(true, "Verification email sent successfully. Please check Inbox, Spam and Promotions folders.")

            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _userCreatedStatus.value = false
                val errCode = "auth/network-timeout"
                _verificationEmailError.value = "$errCode: Network timeout."
                android.util.Log.e("PlayWinVM", "Timeout error: Account registration exceeded limit.", e)
                onResult(false, "Network timeout. Please check your internet connection and try again.")
            } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                _userCreatedStatus.value = false
                val errCode = formatFirebaseErrorCode(e)
                _verificationEmailError.value = "$errCode: This email address is already registered."
                android.util.Log.e("PlayWinVM", "Collision error: Email address already exists.", e)
                onResult(false, "This email address is already registered.")
            } catch (e: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
                _userCreatedStatus.value = false
                val errCode = formatFirebaseErrorCode(e)
                val reasonMsg = e.reason ?: "The password is too weak. Please use at least 6 characters."
                _verificationEmailError.value = "$errCode: $reasonMsg"
                android.util.Log.e("PlayWinVM", "Weak password error: ${e.reason}", e)
                onResult(false, reasonMsg)
            } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                _userCreatedStatus.value = false
                val errCode = formatFirebaseErrorCode(e)
                val invalidMsg = e.localizedMessage ?: "The email address is badly formatted."
                _verificationEmailError.value = "$errCode: $invalidMsg"
                android.util.Log.e("PlayWinVM", "Invalid credentials error: ${e.localizedMessage}", e)
                onResult(false, invalidMsg)
            } catch (e: com.google.firebase.auth.FirebaseAuthException) {
                _userCreatedStatus.value = false
                val errCode = formatFirebaseErrorCode(e)
                val firebaseMsg = e.localizedMessage ?: "Firebase error."
                _verificationEmailError.value = "$errCode: $firebaseMsg"
                android.util.Log.e("PlayWinVM", "Firebase auth exception", e)
                onResult(false, firebaseMsg)
            } catch (e: com.google.firebase.FirebaseNetworkException) {
                _userCreatedStatus.value = false
                val errCode = "auth/network-request-failed"
                _verificationEmailError.value = "$errCode: Network request failed."
                android.util.Log.e("PlayWinVM", "Network failure exception", e)
                onResult(false, "Network failure. Please check your network connection and try again.")
            } catch (e: Exception) {
                _userCreatedStatus.value = false
                val errCode = "auth/internal-error"
                val genericMsg = e.localizedMessage ?: "Failed to create account."
                _verificationEmailError.value = "$errCode: $genericMsg"
                android.util.Log.e("PlayWinVM", "Exception during registration sequence: ${e.localizedMessage}", e)
                onResult(false, genericMsg)
            }
        }
    }

    fun resendVerificationEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                user.sendEmailVerification()
                                    .addOnCompleteListener { sendTask ->
                                        auth.signOut()
                                        if (sendTask.isSuccessful) {
                                            onResult(true, "Verification email resent successfully!")
                                        } else {
                                            onResult(false, sendTask.exception?.localizedMessage ?: "Failed to resend email.")
                                        }
                                    }
                            } else {
                                onResult(false, "User session not found.")
                            }
                        } else {
                            onResult(false, task.exception?.localizedMessage ?: "Credential validation failed.")
                        }
                    }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to resend validation link.")
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, task.exception?.localizedMessage ?: "Failed to send reset email")
                    }
                }
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Failed to send reset email")
        }
    }

    fun signInWithGoogleSimulated(email: String, onResult: (Boolean, String?) -> Unit) {
        signInWithEmailAndPassword(email, "Google_${email.hashCode()}_PlayWin!1", true, onResult)
    }

    fun logout() {
        android.util.Log.d("PlayWinVM", "User Logout - Session Cleared")
        viewModelScope.launch {
            try {
                // 1. Sign out from Firebase Auth
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {}

            // 2. Clear SharedPreferences
            val sharedPrefs = getApplication<Application>().getSharedPreferences("playwin_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()

            // 3. Reset local database (clear Room and insert default empty wallet)
            repository.clearLocalTransactions()
            repository.saveWalletLocally(UserWallet(id = 1, coins = 0, dailyStreak = 0, lastCheckInTime = 0L, userId = ""))

            // 4. Update auth state to Unauthenticated
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun clearCurrentAccountProgress() {
        viewModelScope.launch {
            val currentWallet = walletState.value
            val userId = currentWallet.userId
            if (userId.isNotEmpty()) {
                // 1. Reset in Room database to 0 coins
                val resetWallet = UserWallet(id = 1, coins = 0, dailyStreak = 0, lastCheckInTime = 0L, userId = userId)
                repository.saveWalletLocally(resetWallet)
                repository.clearLocalTransactions()

                // 2. Reset in Firebase Realtime Database
                repository.resetUserDataInFirebase(userId)
            }
        }
    }

    private val rewardMutex = kotlinx.coroutines.sync.Mutex()

    fun executeRewardTransaction(
        userId: String,
        amount: Int,
        type: String,
        source: String,
        extraCheck: ((com.google.firebase.database.MutableData) -> String?)? = null,
        extraUpdate: ((com.google.firebase.database.MutableData) -> Unit)? = null,
        onComplete: (Boolean, Int, Int, String?) -> Unit
    ) {
        com.playwin.app.data.repository.WalletService.updateWallet(
            userId = userId,
            coinsDelta = amount,
            source = source,
            type = type,
            extraCheck = extraCheck,
            extraUpdate = extraUpdate,
            onComplete = onComplete
        )
    }

    fun addCoins(amount: Int, source: String) {
        val currentWallet = walletState.value
        val userId = currentWallet.userId
        if (userId.isEmpty()) return
        
        executeRewardTransaction(
            userId = userId,
            amount = amount,
            type = "reward",
            source = source,
            onComplete = { _, _, _, _ -> }
        )
    }

    fun trackQuizStats(timeOut: Boolean, lifelineLostByTimeout: Boolean, answerTimeMs: Long) {
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                val userRef = db.getReference("users/$currentUserId")
                
                userRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                    override fun doTransaction(mutableData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                        val totalTimeOuts = mutableData.child("totalTimeOuts").getValue(Int::class.java) ?: 0
                        val lifelinesLostByTimeoutVal = mutableData.child("lifelinesLostByTimeout").getValue(Int::class.java) ?: 0
                        val totalAnswersCount = mutableData.child("totalAnswersCount").getValue(Int::class.java) ?: 0
                        val totalAnswersTimeSum = mutableData.child("totalAnswersTimeSum").getValue(Long::class.java) ?: 0L
                        
                        if (timeOut) {
                            mutableData.child("totalTimeOuts").value = totalTimeOuts + 1
                        }
                        if (lifelineLostByTimeout) {
                            mutableData.child("lifelinesLostByTimeout").value = lifelinesLostByTimeoutVal + 1
                        }
                        
                        if (answerTimeMs > 0) {
                            val newCount = totalAnswersCount + 1
                            val newSum = totalAnswersTimeSum + answerTimeMs
                            val avgSec = (newSum.toDouble() / newCount.toDouble()) / 1000.0
                            
                            mutableData.child("totalAnswersCount").value = newCount
                            mutableData.child("totalAnswersTimeSum").value = newSum
                            mutableData.child("averageAnswerTime").value = avgSec
                        }
                        
                        return com.google.firebase.database.Transaction.success(mutableData)
                    }
                    
                    override fun onComplete(error: com.google.firebase.database.DatabaseError?, committed: Boolean, snapshot: com.google.firebase.database.DataSnapshot?) {
                        if (error != null) {
                            android.util.Log.e("PlayWinVM", "Failed to update quiz stats in Firebase", error.toException())
                        } else {
                            android.util.Log.d("PlayWinVM", "Quiz stats updated successfully in Firebase")
                        }
                    }
                })
            } catch (ex: Exception) {
                android.util.Log.e("PlayWinVM", "Exception tracking quiz stats", ex)
            }
        }
    }

    fun getDayOfWeek(): Int {
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = getServerTimeMs()
        return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> 1
            java.util.Calendar.TUESDAY -> 2
            java.util.Calendar.WEDNESDAY -> 3
            java.util.Calendar.THURSDAY -> 4
            java.util.Calendar.FRIDAY -> 5
            java.util.Calendar.SATURDAY -> 6
            java.util.Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun getServerTimeMs(): Long {
        return com.playwin.app.data.repository.DailyResetManager.currentServerTime.value
    }

    fun getLocalDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(getServerTimeMs()))
    }

    fun getTodayDayOfWeekName(): String {
        val sdf = java.text.SimpleDateFormat("EEEE", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(getServerTimeMs()))
    }

    fun getQuizSetStatus(setIndex: Int): String {
        val todayIndex = getDayOfWeek()
        if (setIndex < todayIndex) {
            val progress = quizProgressState.value ?: com.playwin.app.data.model.FirebaseQuizProgress()
            if (progress.completedQuizIds.contains("set_$setIndex")) {
                return "COMPLETED"
            }
            return "LOCKED"
        } else if (setIndex > todayIndex) {
            return "LOCKED"
        } else {
            val progress = quizProgressState.value ?: com.playwin.app.data.model.FirebaseQuizProgress()
            val todayDate = getLocalDateString()
            if (progress.lastQuizDate == todayDate && progress.dailyQuizCompletedBool) {
                return "COMPLETED"
            }
            return "UNLOCKED"
        }
    }

    fun generateQuizForCategory(category: String, onComplete: (List<com.playwin.app.data.model.Quiz>) -> Unit) {
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        repository.getQuestionsForCategory(category) { allQuestions ->
            if (allQuestions.isEmpty()) {
                onComplete(emptyList())
                return@getQuestionsForCategory
            }
            val progress = quizProgressState.value ?: com.playwin.app.data.model.FirebaseQuizProgress()
            val completedIds = progress.completedQuestionIds.toSet()

            var available = allQuestions.filter { !completedIds.contains(it.id) }
            
            if (available.size < 10) {
                available = allQuestions
                if (currentUserId.isNotEmpty()) {
                    val updatedProgress = progress.copy(completedQuestionIds = emptyList())
                    repository.saveFirebaseQuizProgress(currentUserId, updatedProgress)
                }
            }

            val selected = available.shuffled().take(10)
            onComplete(selected)
        }
    }

    fun completeQuiz(
        category: String,
        quizSetId: String,
        score: Int,
        answeredQuestionIds: List<String>,
        totalQuestions: Int = 10,
        rewardCoinsPerCorrect: Int = 50,
        completionBonus: Int = 50,
        dayOfWeek: String = "",
        onComplete: (Int) -> Unit
    ) {
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        val progress = quizProgressState.value ?: com.playwin.app.data.model.FirebaseQuizProgress()
        val today = getLocalDateString()
        val currentDayName = getTodayDayOfWeekName()
        val quizDayOfWeek = if (dayOfWeek.isNotEmpty()) {
            dayOfWeek
        } else {
            quizzesState.value.find { it.id == quizSetId }?.dayOfWeek?.ifEmpty { currentDayName } ?: currentDayName
        }

        val weeklyProgressRecord = weeklyQuizProgressState.value[quizDayOfWeek]
        val isAlreadyCompletedTodayWeekly = weeklyProgressRecord != null && weeklyProgressRecord.completed && weeklyProgressRecord.date == today

        val dailyQuiz = dailyQuizState.value
        val isAlreadyCompletedTodayQuizNode = dailyQuiz != null && dailyQuiz.completed && dailyQuiz.lastCompletedDate == today

        val isAlreadyCompletedToday = (completedQuizzesState.value[quizSetId]?.completed == true && completedQuizzesState.value[quizSetId]?.completedDate == today) ||
            (progress.completedQuizIds.contains(quizSetId) && progress.lastQuizDate == today) ||
            isAlreadyCompletedTodayWeekly ||
            isAlreadyCompletedTodayQuizNode

        val totalReward = if (isAlreadyCompletedToday) {
            0
        } else {
            (score * rewardCoinsPerCorrect) + (if (score == totalQuestions) completionBonus else 0)
        }

        executeRewardTransaction(
            userId = currentUserId,
            amount = totalReward,
            type = "quiz_reward",
            source = "Quiz Completed: $category ($score/$totalQuestions)",
            extraCheck = { mutableData ->
                verifyAndLogBooleans(mutableData)
                val progressNode = mutableData.child("quizProgress")
                val completedQuizIds = progressNode.child("completedQuizIds").children.mapNotNull { it.getValue(String::class.java) }
                val lastQuizDate = progressNode.child("lastQuizDate").getValue(String::class.java) ?: ""
                
                val dailyQuizNode = mutableData.child("dailyQuiz")
                val dailyQuizCompleted = dailyQuizNode.getSafeBoolean("completed")
                val dailyQuizLastCompletedDate = dailyQuizNode.child("lastCompletedDate").getValue(String::class.java) ?: ""
                
                val completedTodayFromProgress = completedQuizIds.contains(quizSetId) && lastQuizDate == today
                val completedTodayFromCompletedQuizzes = mutableData.child("completedQuizzes").child(quizSetId).child("completedDate").getValue(String::class.java) == today
                val completedTodayFromWeekly = mutableData.child("weeklyQuizProgress").child(quizDayOfWeek).child("date").getValue(String::class.java) == today
                val completedTodayFromDailyNode = dailyQuizCompleted && dailyQuizLastCompletedDate == today
                
                if (completedTodayFromProgress || completedTodayFromCompletedQuizzes || completedTodayFromWeekly || completedTodayFromDailyNode) {
                    "Quiz already completed today."
                } else {
                    null
                }
            },
            extraUpdate = { mutableData ->
                val progressNode = mutableData.child("quizProgress")
                val completedQuizIds = progressNode.child("completedQuizIds").children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
                if (!completedQuizIds.contains(quizSetId)) {
                    completedQuizIds.add(quizSetId)
                }
                progressNode.child("completedQuizIds").value = completedQuizIds

                val completedQuestionIds = progressNode.child("completedQuestionIds").children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
                for (qId in answeredQuestionIds) {
                    if (!completedQuestionIds.contains(qId)) {
                        completedQuestionIds.add(qId)
                    }
                }
                progressNode.child("completedQuestionIds").value = completedQuestionIds

                val localDate = getLocalDateString()
                progressNode.child("lastQuizDate").value = localDate
                progressNode.child("dailyQuizCompleted").value = true

                val catNode = progressNode.child("categoryHistory").child(category)
                val currentHistoryScore = catNode.getValue(Int::class.java) ?: 0
                if (score > currentHistoryScore) {
                    catNode.value = score
                }

                // Daily Quiz database node updates
                val dailyQuizNode = mutableData.child("dailyQuiz")
                dailyQuizNode.child("lastCompletedDay").value = currentDayName
                dailyQuizNode.child("lastCompletedDate").value = localDate
                dailyQuizNode.child("lastQuizId").value = quizSetId
                dailyQuizNode.child("completed").value = true
                dailyQuizNode.child("rewardClaimed").value = true
                
                dailyQuizNode.child("currentServerDay").value = currentDayName
                dailyQuizNode.child("lastPlayedQuiz").value = quizSetId
                dailyQuizNode.child("completedQuizDate").value = localDate
                dailyQuizNode.child("serverTimestamp").value = System.currentTimeMillis() + serverTimeOffset

                // Save completion to weeklyQuizProgress/{dayOfWeek}
                val weeklyProgressNode = mutableData.child("weeklyQuizProgress").child(quizDayOfWeek)
                weeklyProgressNode.child("completed").value = true
                weeklyProgressNode.child("completedAt").value = System.currentTimeMillis()
                weeklyProgressNode.child("quizId").value = quizSetId
                weeklyProgressNode.child("coinsEarned").value = totalReward
                weeklyProgressNode.child("score").value = score
                weeklyProgressNode.child("date").value = localDate

                // 2. Save Completion in Firebase under users/{uid}/completedQuizzes/{quizId}
                val completedQuizNode = mutableData.child("completedQuizzes").child(quizSetId)
                completedQuizNode.child("completed").value = true
                completedQuizNode.child("completedAt").value = System.currentTimeMillis()
                completedQuizNode.child("completedDate").value = localDate
                completedQuizNode.child("score").value = score
                completedQuizNode.child("correctAnswers").value = score
                completedQuizNode.child("wrongAnswers").value = totalQuestions - score
                completedQuizNode.child("coinsEarned").value = totalReward

                // 8. After quiz completion, save Completed, Score, Coins Earned, Correct Answers, Wrong Answers, Completion Time inside Firebase.
                val historyNode = mutableData.child("quizHistory").child(quizSetId)
                historyNode.child("completed").value = true
                historyNode.child("score").value = score
                historyNode.child("coinsEarned").value = totalReward
                historyNode.child("correctAnswers").value = score
                historyNode.child("wrongAnswers").value = totalQuestions - score
                historyNode.child("completionTime").value = System.currentTimeMillis()

                val resultsNode = mutableData.child("quizResults").child(quizSetId)
                resultsNode.child("completed").value = true
                resultsNode.child("score").value = score
                resultsNode.child("coinsEarned").value = totalReward
                resultsNode.child("correctAnswers").value = score
                resultsNode.child("wrongAnswers").value = totalQuestions - score
                resultsNode.child("completionTime").value = System.currentTimeMillis()
            },
            onComplete = { success, _, _, _ ->
                if (success) {
                    val friendUid = currentUserId
                    viewModelScope.launch {
                        try {
                            var referredByUid = ""
                            var friendName = ""
                            var friendEmail = ""

                            com.playwin.app.data.repository.WalletService.updateWallet(
                                userId = friendUid,
                                coinsDelta = 100,
                                source = "Referral Reward (Completed First Quiz)",
                                type = "referral_reward",
                                extraCheck = { mutableData ->
                                    val refBy = mutableData.child("referredBy").getValue(String::class.java) ?: ""
                                    val status = mutableData.child("referralStatus").getValue(String::class.java) ?: ""
                                    if (refBy.isEmpty() || status == "Rewarded") {
                                        "Not eligible or already rewarded."
                                    } else {
                                        null
                                    }
                                },
                                extraUpdate = { mutableData ->
                                    referredByUid = mutableData.child("referredBy").getValue(String::class.java) ?: ""
                                    friendName = mutableData.child("displayName").getValue(String::class.java) ?: "Friend"
                                    friendEmail = mutableData.child("email").getValue(String::class.java) ?: ""
                                    mutableData.child("referralStatus").value = "Rewarded"
                                },
                                onComplete = { friendSuccess, _, _, friendError ->
                                    if (friendSuccess && referredByUid.isNotEmpty()) {
                                        android.util.Log.d("PlayWinReferral", "Friend transaction succeeded. Now rewarding referrer $referredByUid")
                                        
                                        // Update friend's local transaction record
                                        viewModelScope.launch {
                                            repository.addTransaction(friendUid, 100, "Referral Reward (Completed First Quiz)")
                                        }

                                        // Reward Referrer
                                        viewModelScope.launch {
                                            com.playwin.app.data.repository.WalletService.updateWallet(
                                                userId = referredByUid,
                                                coinsDelta = 500,
                                                source = "Referral Bonus",
                                                type = "referral_bonus",
                                                extraCheck = { null },
                                                extraUpdate = { mutableDataReferrer ->
                                                    val currentPending = mutableDataReferrer.child("pendingRewards").getValue(Int::class.java) ?: 0
                                                    val totalRefs = mutableDataReferrer.child("totalReferrals").getValue(Int::class.java) ?: 0
                                                    val coinsEarned = mutableDataReferrer.child("referralsCoinsEarned").getValue(Int::class.java) ?: 0

                                                    mutableDataReferrer.child("pendingRewards").value = if (currentPending > 0) currentPending - 1 else 0
                                                    mutableDataReferrer.child("totalReferrals").value = totalRefs + 1
                                                    mutableDataReferrer.child("referralsCoinsEarned").value = coinsEarned + 500
                                                },
                                                onComplete = { referrerSuccess, _, referrerCoinsAfter, referrerError ->
                                                    if (referrerSuccess) {
                                                        android.util.Log.d("PlayWinReferral", "Referrer transaction succeeded. Updating referral logs.")
                                                        val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                                                        val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                                                        val now = System.currentTimeMillis()

                                                        // Update main referrals record
                                                        val referralRefRecord = db.getReference("referrals/$referredByUid/$friendUid")
                                                        val referralUpdate = mapOf(
                                                            "status" to "Rewarded",
                                                            "coinsEarned" to 500
                                                        )
                                                        referralRefRecord.updateChildren(referralUpdate)

                                                        // Delete from pendingRewards node
                                                        db.getReference("pendingRewards/$referredByUid/$friendUid").removeValue()

                                                        // Write to referralHistory node
                                                        val referralHistoryRef = db.getReference("referralHistory/$referredByUid/$friendUid")
                                                        val referralHistoryMap = mapOf(
                                                            "friendUid" to friendUid,
                                                            "friendName" to friendName,
                                                            "friendEmail" to friendEmail,
                                                            "status" to "Rewarded",
                                                            "coinsEarned" to 500,
                                                            "timestamp" to now
                                                        )
                                                        referralHistoryRef.setValue(referralHistoryMap)
                                                    } else {
                                                        android.util.Log.e("PlayWinReferral", "Referrer transaction failed: $referrerError")
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        android.util.Log.e("PlayWinReferral", "Friend referral transaction failed or not needed: $friendError")
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("PlayWinReferral", "Error in referral completion logic", e)
                        }
                    }
                }
                onComplete(if (success) totalReward else 0)
            }
        )
    }

    suspend fun spendCoins(amount: Int, purpose: String): Boolean = suspendCoroutine { continuation ->
        val currentWallet = walletState.value
        val userId = currentWallet.userId
        if (userId.isEmpty() || currentWallet.coins < amount) {
            continuation.resume(false)
            return@suspendCoroutine
        }

        executeRewardTransaction(
            userId = userId,
            amount = -amount,
            type = "coupon_redeemed",
            source = purpose,
            extraCheck = { mutableData ->
                val currentCoins = mutableData.child("coins").getValue(Int::class.java) ?: 0
                if (currentCoins < amount) {
                    "Insufficient Coins in database"
                } else {
                    null
                }
            },
            onComplete = { success, _, _, _ ->
                continuation.resume(success)
            }
        )
    }

    fun applyReferral(referralCode: String) {
        viewModelScope.launch {
            val currentWallet = walletState.value
            repository.addReferral(currentWallet.userId, referralCode)
        }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        if (time1 == 0L || time2 == 0L) return false
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        return sdf.format(java.util.Date(time1)) == sdf.format(java.util.Date(time2))
    }

    private fun isYesterday(lastTime: Long, now: Long): Boolean {
        if (lastTime == 0L) return false
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = now
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)
        val lastStr = sdf.format(java.util.Date(lastTime))
        return lastStr == yesterdayStr
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        return false
    }

    fun refreshDailyCheckInSettings() {
        dailyCheckInLoadingState.value = true
        val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
        val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
        db.getReference("dailyCheckIn").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                if (snapshot != null && snapshot.exists()) {
                    val settings = snapshot.getValue(com.playwin.app.data.model.FirebaseDailyCheckInSettings::class.java)
                    dailyCheckInSettingsState.value = settings
                    if (settings != null) {
                        val r = settings.rewards
                        if (r != null && r.size >= 7) {
                            android.util.Log.d("PlayWinCheckIn", "Loaded rewards:\nDay1=${r[0]}\nDay2=${r[1]}\nDay3=${r[2]}\nDay4=${r[3]}\nDay5=${r[4]}\nDay6=${r[5]}\nDay7=${r[6]}")
                        }
                    }
                } else {
                    val defaultSettings = com.playwin.app.data.model.FirebaseDailyCheckInSettings(
                        enabled = true,
                        rewards = listOf(20, 30, 40, 50, 60, 80, 120),
                        maxRewardLimit = 500
                    )
                    db.getReference("dailyCheckIn").setValue(defaultSettings)
                    dailyCheckInSettingsState.value = defaultSettings
                }
            }
            dailyCheckInLoadingState.value = false
        }
    }

    fun claimDailyReward(onResult: ((Boolean, String?) -> Unit)? = null): Boolean {
        if (!isNetworkAvailable()) {
            onResult?.invoke(false, "Connect to internet to claim today's reward.")
            return false
        }

        val currentWallet = walletState.value
        val userId = currentWallet.userId
        if (userId.isEmpty()) {
            onResult?.invoke(false, "User not authenticated")
            return false
        }

        val settings = dailyCheckInSettingsState.value
        if (settings == null) {
            onResult?.invoke(false, "Configuration unavailable.")
            return false
        }
        if (!settings.enabled) {
            onResult?.invoke(false, "Daily Check-In is currently disabled.")
            return false
        }

        val rewardsList = settings.rewards
        if (rewardsList == null || rewardsList.size < 7) {
            onResult?.invoke(false, "Configuration unavailable.")
            return false
        }

        val serverTime = com.playwin.app.data.repository.DailyResetManager.currentServerTime.value
        val startOfToday = com.playwin.app.data.repository.DailyResetManager.getStartOfTodayUtc(serverTime)
        val userCheckIn = userDailyCheckInState.value ?: com.playwin.app.data.model.FirebaseUserDailyCheckIn()
        
        val lastClaim = userCheckIn.lastClaimTimestamp
        if (lastClaim >= startOfToday) {
            val countdown = com.playwin.app.data.repository.DailyResetManager.remainingTime.value
            onResult?.invoke(false, "Daily reward already claimed today. Next reset in $countdown")
            return false
        }

        val isMissed = lastClaim > 0L && lastClaim < (startOfToday - 86400000L)
        val currentStreak = userCheckIn.streak
        val currentDayVal = userCheckIn.currentDay

        val newStreak = if (isMissed || currentStreak == 0) 1 else currentStreak + 1
        var newDay = if (isMissed || currentDayVal == 0) 1 else currentDayVal + 1
        if (newDay > 7) {
            newDay = 1
        }

        val rewardAmount = when (newDay) {
            1 -> rewardsList[0]
            2 -> rewardsList[1]
            3 -> rewardsList[2]
            4 -> rewardsList[3]
            5 -> rewardsList[4]
            6 -> rewardsList[5]
            7 -> rewardsList[6]
            else -> rewardsList[0]
        }
        val finalReward = if (rewardAmount > settings.maxRewardLimit) settings.maxRewardLimit else rewardAmount

        executeRewardTransaction(
            userId = userId,
            amount = finalReward,
            type = "daily_reward",
            source = "Daily Check-In Day $newDay Reward",
            extraCheck = { mutableData ->
                val dbLastClaim = mutableData.child("dailyCheckIn").child("lastClaimTimestamp").getValue(Long::class.java) ?: 0L
                if (dbLastClaim >= startOfToday) {
                    "Daily reward already claimed today."
                } else {
                    val dbStreak = mutableData.child("dailyCheckIn").child("streak").getValue(Int::class.java) ?: 0
                    val dbDay = mutableData.child("dailyCheckIn").child("currentDay").getValue(Int::class.java) ?: 0
                    val dbIsMissed = dbLastClaim > 0L && dbLastClaim < (startOfToday - 86400000L)
                    
                    val expectedNewDay = if (dbIsMissed || dbDay == 0) 1 else {
                        val temp = dbDay + 1
                        if (temp > 7) 1 else temp
                    }
                    val dbRewards = settings.rewards
                    if (dbRewards == null || dbRewards.size < 7) {
                        "Configuration unavailable."
                    } else {
                        val expectedReward = when (expectedNewDay) {
                            1 -> dbRewards[0]
                            2 -> dbRewards[1]
                            3 -> dbRewards[2]
                            4 -> dbRewards[3]
                            5 -> dbRewards[4]
                            6 -> dbRewards[5]
                            7 -> dbRewards[6]
                            else -> dbRewards[0]
                        }
                        val expectedFinalReward = if (expectedReward > settings.maxRewardLimit) settings.maxRewardLimit else expectedReward
                        
                        if (finalReward != expectedFinalReward) {
                            "Value tampering detected."
                        } else {
                            null
                        }
                    }
                }
            },
            extraUpdate = { mutableData ->
                val dbLastClaim = mutableData.child("dailyCheckIn").child("lastClaimTimestamp").getValue(Long::class.java) ?: 0L
                val dbIsMissed = dbLastClaim > 0L && (serverTime - dbLastClaim >= 172800000L)
                
                val streakNode = mutableData.child("dailyCheckIn").child("streak")
                val currentDayNode = mutableData.child("dailyCheckIn").child("currentDay")
                val totalClaimsNode = mutableData.child("dailyCheckIn").child("totalClaims")

                val dbStreak = streakNode.getValue(Int::class.java) ?: 0
                val dbDay = currentDayNode.getValue(Int::class.java) ?: 0
                val totalClaimsVal = totalClaimsNode.getValue(Int::class.java) ?: 0

                val dbNewStreak = if (dbIsMissed || dbStreak == 0) 1 else dbStreak + 1
                var dbNewDay = if (dbIsMissed || dbDay == 0) 1 else dbDay + 1
                if (dbNewDay > 7) {
                    dbNewDay = 1
                }

                mutableData.child("dailyCheckIn").child("lastClaimTimestamp").value = serverTime
                mutableData.child("dailyCheckIn").child("nextEligibleTimestamp").value = serverTime + 86400000L
                mutableData.child("dailyCheckIn").child("currentDay").value = dbNewDay
                mutableData.child("dailyCheckIn").child("streak").value = dbNewStreak
                mutableData.child("dailyCheckIn").child("totalClaims").value = totalClaimsVal + 1

                // Sync backward compatibility fields
                mutableData.child("dailyStreak").value = dbNewStreak
                mutableData.child("lastCheckInTime").value = serverTime
                mutableData.child("lastCheckInDate").value = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(serverTime))

                val prevTotalCheckIn = mutableData.child("totalCheckInRewards").getValue(Int::class.java) ?: 0
                mutableData.child("totalCheckInRewards").value = prevTotalCheckIn + finalReward
            },
            onComplete = { success, _, _, errorMsg ->
                if (success) {
                    viewModelScope.launch {
                        val currentWallet = walletState.value
                        val updatedWallet = currentWallet.copy(
                            coins = currentWallet.coins + finalReward,
                            dailyStreak = newStreak,
                            lastCheckInTime = serverTime,
                            lastCheckInDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(serverTime)),
                            totalCheckInRewards = currentWallet.totalCheckInRewards + finalReward
                        )
                        repository.saveWalletLocally(updatedWallet)
                    }
                }
                onResult?.invoke(success, errorMsg)
            }
        )
        return true
    }

    private fun validateFirebaseAndUser(context: Context, onResult: (Boolean, String?, com.google.firebase.database.FirebaseDatabase?, String?) -> Unit): Boolean {
        val app = try {
            com.google.firebase.FirebaseApp.getInstance()
        } catch (e: IllegalStateException) {
            android.util.Log.e("PlayWinAds", "FirebaseApp is not initialized", e)
            onResult(false, "Firebase initialization failed. Please restart the app.", null, null)
            return false
        }

        val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
        val db = try {
            com.google.firebase.database.FirebaseDatabase.getInstance(app, dbUrl)
        } catch (e: Exception) {
            android.util.Log.e("PlayWinAds", "Failed to get FirebaseDatabase instance", e)
            onResult(false, "Failed to connect to the database. Please try again.", null, null)
            return false
        }

        if (db == null) {
            onResult(false, "Database connection is not available.", null, null)
            return false
        }

        val auth = com.google.firebase.auth.FirebaseAuth.getInstance(app)
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        if (currentUser == null || uid.isNullOrEmpty()) {
            android.util.Log.e("PlayWinAds", "Current user is null or empty")
            onResult(false, "You must be signed in to claim ad rewards.", null, null)
            return false
        }

        onResult(true, null, db, uid)
        return true
    }

    private fun claimRewardedAdRewardInternal(type: String, source: String, onResult: (Boolean, String?) -> Unit) {
        val context = getApplication<Application>()
        validateFirebaseAndUser(context) { isValid, errorMsg, db, userId ->
            if (!isValid || db == null || userId.isNullOrEmpty()) {
                onResult(false, errorMsg ?: "Authentication check failed.")
                return@validateFirebaseAndUser
            }

            val currentWallet = walletState.value
            val now = System.currentTimeMillis()
            val elapsed = now - currentWallet.lastRewardAdTime
            if (currentWallet.lastRewardAdTime != 0L && elapsed < 1 * 60 * 1000L) {
                onResult(false, "Cooldown is active. Please try again later.")
                return@validateFirebaseAndUser
            }

            viewModelScope.launch {
                rewardMutex.withLock {
                    try {
                        var activeResetTime = 0L
                        var dbAdsWatched = 0
                        val rewardAmount = 50

                        com.playwin.app.data.repository.WalletService.updateWallet(
                            userId = userId,
                            coinsDelta = rewardAmount,
                            source = "Watch Ads Reward",
                            type = "video_ad",
                            extraCheck = { mutableData ->
                                val dbLastAdTime = mutableData.child("lastRewardAdTime").getValue(Long::class.java) ?: 0L
                                val txNow = System.currentTimeMillis()
                                val dbElapsed = txNow - dbLastAdTime
                                if (dbLastAdTime != 0L && dbElapsed < 1 * 60 * 1000L) {
                                    "Ad reward cooldown is still active in database."
                                } else {
                                    val dbResetTime = mutableData.child("lastAdResetTime").getValue(Long::class.java) ?: 0L
                                    var adsWatched = mutableData.child("dailyAdsWatched").getValue(Int::class.java) ?: 0
                                    if (!isSameDay(dbResetTime, txNow) || dbResetTime == 0L) {
                                        adsWatched = 0
                                    }
                                    if (adsWatched >= 10) {
                                        "Daily Reward Ad Limit Reached. Come Back Tomorrow."
                                    } else {
                                        null
                                    }
                                }
                            },
                            extraUpdate = { mutableData ->
                                val txNow = System.currentTimeMillis()
                                val dbResetTime = mutableData.child("lastAdResetTime").getValue(Long::class.java) ?: 0L
                                dbAdsWatched = mutableData.child("dailyAdsWatched").getValue(Int::class.java) ?: 0
                                activeResetTime = dbResetTime
                                if (!isSameDay(dbResetTime, txNow) || dbResetTime == 0L) {
                                    activeResetTime = txNow
                                    dbAdsWatched = 0
                                }
                                mutableData.child("dailyAdsWatched").value = dbAdsWatched + 1
                                mutableData.child("lastAdResetTime").value = activeResetTime
                                mutableData.child("lastRewardAdTime").value = txNow
                            },
                            onComplete = { success, coinsBefore, coinsAfter, error ->
                                if (success) {
                                    val txNow = System.currentTimeMillis()
                                    val walletSummaryMap = mapOf(
                                        "userId" to userId,
                                        "coins" to coinsAfter,
                                        "dailyAdsWatched" to (dbAdsWatched + 1),
                                        "lastRewardAdTime" to txNow,
                                        "lastAdResetTime" to activeResetTime
                                    )
                                    db.getReference("walletSummary/$userId").setValue(walletSummaryMap)

                                    val currentWallet = walletState.value
                                    if (currentWallet.userId == userId) {
                                        val updatedWallet = currentWallet.copy(
                                            coins = coinsAfter,
                                            dailyAdsWatched = dbAdsWatched + 1,
                                            lastRewardAdTime = txNow,
                                            lastAdResetTime = activeResetTime
                                        )
                                        viewModelScope.launch {
                                            repository.saveWalletLocally(updatedWallet)
                                        }
                                    }
                                    onResult(true, null)
                                } else {
                                    onResult(false, error ?: "Transaction aborted.")
                                }
                            }
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("PlayWinCoinSystem", "Unexpected error during Rewarded Ad Transaction", e)
                        onResult(false, e.message ?: "An unexpected error occurred.")
                    }
                }
            }
        }
    }

    fun claimRewardedAdReward(onResult: (Boolean, String?) -> Unit) {
        claimRewardedAdRewardInternal("video_ad", "Watch Ads Reward", onResult)
    }

    fun checkAdCountReset() {
        val currentWallet = walletState.value
        if (currentWallet.userId.isEmpty()) return
        val now = System.currentTimeMillis()
        if (currentWallet.lastAdResetTime != 0L && !isSameDay(currentWallet.lastAdResetTime, now)) {
            viewModelScope.launch {
                val updatedWallet = currentWallet.copy(
                    dailyAdsWatched = 0,
                    lastAdResetTime = now
                )
                repository.saveWalletLocally(updatedWallet)
            }
        }
    }

    fun claimVideoAdReward(onResult: (Boolean, String?) -> Unit) {
        claimRewardedAdRewardInternal("video_ad", "Watch Ads Reward", onResult)
    }

    val isProcessingReferral = MutableStateFlow(false)

    fun refreshUserData() {
        val currentWallet = walletState.value
        val userId = currentWallet.userId
        if (userId.isEmpty()) return
        viewModelScope.launch {
            try {
                if (com.playwin.app.data.repository.DailyResetManager.isResetRequired()) {
                    com.playwin.app.data.repository.DailyResetManager.performDailyReset(userId)
                }

                val dbUser = repository.getFirebaseUser(userId)
                if (dbUser != null) {
                    val syncWallet = currentWallet.copy(
                        coins = dbUser.coins,
                        dailyStreak = dbUser.streak,
                        lastCheckInTime = dbUser.lastCheckInTime,
                        dailyAdsWatched = dbUser.dailyAdsWatched,
                        lastAdResetTime = dbUser.lastAdResetTime,
                        referredBy = dbUser.referredBy,
                        hasUsedReferralCode = dbUser.hasUsedReferralCodeBool,
                        totalReferrals = dbUser.totalReferrals,
                        remainingSpins = dbUser.remainingSpins,
                        totalSpinRewards = dbUser.totalSpinRewards,
                        remainingScratchCards = dbUser.remainingScratchCards,
                        lastScratchResetTime = dbUser.lastScratchResetTime,
                        totalScratchRewards = dbUser.totalScratchRewards,
                        lastSpinDate = dbUser.lastSpinDate,
                        freeSpinUsed = dbUser.freeSpinUsedBool,
                        rewardAdSpinUsed = dbUser.rewardAdSpinUsedBool,
                        dailySpinCount = dbUser.dailySpinCount,
                        rewardedSpinCount = dbUser.rewardedSpinCount,
                        lastScratchDate = dbUser.lastScratchDate,
                        freeScratchUsed = dbUser.freeScratchUsedBool,
                        rewardAdScratchUsed = dbUser.rewardAdScratchUsedBool,
                        lastCheckInDate = dbUser.lastCheckInDate,
                        totalCheckInRewards = dbUser.totalCheckInRewards,
                        lastRewardAdTime = dbUser.lastRewardAdTime
                    )
                    repository.saveWalletLocally(syncWallet)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    suspend fun findReferrerUid(enteredCode: String): String? {
        val cleanCode = enteredCode.trim().uppercase()
        if (!cleanCode.startsWith("PLAYWIN_") || cleanCode.length <= 8) {
            return null
        }
        val candidateSuffix = cleanCode.substring(8) // "PLAYWIN_XXXXXX" -> "XXXXXX"
        
        val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
        val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
        
        // 1. Try referralCodes mapping
        try {
            val mappingSnapshot = db.getReference("referralCodes/$cleanCode").get().awaitTask()
            val mappedUid = mappingSnapshot.getValue(String::class.java)
            if (!mappedUid.isNullOrEmpty()) {
                return mappedUid
            }
        } catch (e: Exception) {
            // ignore
        }
        
        // 2. Fallback: Search prefix
        try {
            val usersSnapshot = db.getReference("users").get().awaitTask()
            for (child in usersSnapshot.children) {
                val uid = child.key ?: ""
                if (uid.take(6).uppercase() == candidateSuffix) {
                    return uid
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        
        return null
    }

    fun claimFriendReferral(enteredCode: String, onResult: (Boolean, String?) -> Unit) {
        val currentWallet = walletState.value
        val referredUid = currentWallet.userId
        if (referredUid.isEmpty()) {
            onResult(false, "User not authenticated")
            return
        }

        if (currentWallet.hasUsedReferralCode) {
            onResult(false, "Referral already linked.")
            return
        }

        if (isProcessingReferral.value) return
        isProcessingReferral.value = true

        var cleanCode = enteredCode.trim().uppercase()
        if (!cleanCode.startsWith("PLAYWIN_")) {
            cleanCode = "PLAYWIN_$cleanCode"
        }

        val ownCode = "PLAYWIN_${referredUid.take(6).uppercase()}"
        if (cleanCode == ownCode) {
            isProcessingReferral.value = false
            onResult(false, "You cannot use your own referral code.")
            return
        }

        val context = getApplication<Application>()
        val deviceId = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: ""
        val friendName = currentUserState.value?.displayName ?: "Friend"
        val friendEmail = currentUserState.value?.email ?: "friend@gmail.com"

        viewModelScope.launch {
            try {
                android.util.Log.d("PlayWinReferral", "Starting lookup for code: $cleanCode")
                val referrerUid = findReferrerUid(cleanCode)
                if (referrerUid == null) {
                    isProcessingReferral.value = false
                    android.util.Log.e("PlayWinReferral", "No referrer found for code: $cleanCode")
                    onResult(false, "Invalid Referral Code")
                    return@launch
                }

                if (referrerUid == referredUid) {
                    isProcessingReferral.value = false
                    onResult(false, "You cannot use your own referral code.")
                    return@launch
                }

                val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)

                // 1. Verify referrer exists in users node
                val referrerSnapshot = db.getReference("users/$referrerUid").get().awaitTask()
                if (!referrerSnapshot.exists()) {
                    isProcessingReferral.value = false
                    android.util.Log.e("PlayWinReferral", "Referrer snapshot does not exist in users/$referrerUid")
                    onResult(false, "Invalid Referral Code")
                    return@launch
                }

                // Device and accounts check
                val referrerDevId = referrerSnapshot.child("deviceId").getValue(String::class.java) ?: ""
                val referrerEmail = referrerSnapshot.child("email").getValue(String::class.java) ?: ""
                if (referrerEmail.isNotEmpty() && referrerEmail.uppercase() == friendEmail.uppercase()) {
                    isProcessingReferral.value = false
                    onResult(false, "You cannot use your own referral code.")
                    return@launch
                }

                if (referrerDevId.isNotEmpty() && referrerDevId == deviceId) {
                    isProcessingReferral.value = false
                    android.util.Log.e("PlayWinReferral", "Referrer has same device ID: $deviceId")
                    onResult(false, "Same device referral abuse detected.")
                    return@launch
                }

                // 2. runTransaction on SPECIFIC referred user node
                val userRef = db.getReference("users/$referredUid")
                userRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                    var isAlreadyClaimedError = false

                    override fun doTransaction(mutableData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                        if (mutableData.value == null) {
                            return com.google.firebase.database.Transaction.success(mutableData)
                        }

                        verifyAndLogBooleans(mutableData)
                        val claimed = mutableData.getSafeBoolean("hasUsedReferralCode")
                        val referredBy = mutableData.child("referredBy").getValue(String::class.java) ?: ""
                        if (claimed || referredBy.isNotEmpty()) {
                            isAlreadyClaimedError = true
                            return com.google.firebase.database.Transaction.abort()
                        }

                        mutableData.child("referredBy").value = referrerUid
                        mutableData.child("referralLinked").value = true
                        mutableData.child("hasUsedReferralCode").value = true
                        mutableData.child("referralStatus").value = "Pending"
                        mutableData.child("linkedAt").value = com.google.firebase.database.ServerValue.TIMESTAMP
                        mutableData.child("lastActiveTime").value = System.currentTimeMillis()

                        return com.google.firebase.database.Transaction.success(mutableData)
                    }

                    override fun onComplete(
                        error: com.google.firebase.database.DatabaseError?,
                        committed: Boolean,
                        currentData: com.google.firebase.database.DataSnapshot?
                    ) {
                        if (committed && error == null) {
                            android.util.Log.d("PlayWinReferral", "Referral atomically linked successfully for user $referredUid to $referrerUid")
                            
                            val timestamp = System.currentTimeMillis()
                            val updates = hashMapOf<String, Any>()
                            
                            updates["referrals/$referrerUid/$referredUid/friendUid"] = referredUid
                            updates["referrals/$referrerUid/$referredUid/friendName"] = friendName
                            updates["referrals/$referrerUid/$referredUid/friendEmail"] = friendEmail
                            updates["referrals/$referrerUid/$referredUid/joinDate"] = timestamp
                            updates["referrals/$referrerUid/$referredUid/status"] = "Pending"
                            updates["referrals/$referrerUid/$referredUid/coinsEarned"] = 0
                            updates["referrals/$referrerUid/$referredUid/timestamp"] = timestamp

                            updates["pendingRewards/$referrerUid/$referredUid/friendUid"] = referredUid
                            updates["pendingRewards/$referrerUid/$referredUid/friendName"] = friendName
                            updates["pendingRewards/$referrerUid/$referredUid/friendEmail"] = friendEmail
                            updates["pendingRewards/$referrerUid/$referredUid/joinDate"] = timestamp
                            updates["pendingRewards/$referrerUid/$referredUid/status"] = "Pending"
                            updates["pendingRewards/$referrerUid/$referredUid/coinsEarned"] = 0
                            updates["pendingRewards/$referrerUid/$referredUid/timestamp"] = timestamp

                            db.reference.updateChildren(updates).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    android.util.Log.d("PlayWinReferral", "Referrals tracking logs written successfully.")
                                } else {
                                    android.util.Log.e("PlayWinReferral", "Failed to write referrals tracking logs", task.exception)
                                }
                            }

                            // Update referrer's pendingRewards count atomically
                            db.getReference("users/$referrerUid").runTransaction(object : com.google.firebase.database.Transaction.Handler {
                                override fun doTransaction(mutableDataReferrer: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                                    if (mutableDataReferrer.value != null) {
                                        val pending = mutableDataReferrer.child("pendingRewards").getValue(Int::class.java) ?: 0
                                        mutableDataReferrer.child("pendingRewards").value = pending + 1
                                        mutableDataReferrer.child("lastActiveTime").value = System.currentTimeMillis()
                                    }
                                    return com.google.firebase.database.Transaction.success(mutableDataReferrer)
                                }
                                override fun onComplete(err: com.google.firebase.database.DatabaseError?, committedRef: Boolean, snap: com.google.firebase.database.DataSnapshot?) {
                                    if (committedRef && err == null) {
                                        android.util.Log.d("PlayWinReferral", "Referrer pending rewards count incremented.")
                                    } else {
                                        android.util.Log.e("PlayWinReferral", "Failed to increment referrer pending rewards count", err?.toException())
                                    }
                                }
                            })

                            // Save locally to local database (Room)
                            viewModelScope.launch {
                                val updatedWallet = currentWallet.copy(
                                    hasUsedReferralCode = true,
                                    referredBy = referrerUid
                                )
                                repository.saveWalletLocally(updatedWallet)
                            }

                            isProcessingReferral.value = false
                            onResult(true, "Referral linked successfully.")
                        } else {
                            isProcessingReferral.value = false
                            val msg = when {
                                isAlreadyClaimedError -> "Referral already linked."
                                error != null -> error.message
                                else -> "Transaction failed."
                            }
                            android.util.Log.e("PlayWinReferral", "Transaction failed to link referral: $msg", error?.toException())
                            onResult(false, msg)
                        }
                    }
                })
            } catch (e: Exception) {
                isProcessingReferral.value = false
                android.util.Log.e("PlayWinReferral", "Unexpected error in claimFriendReferral", e)
                onResult(false, e.message ?: "Referral verification failed.")
            }
        }
    }

    data class SpinRewardOption(val amount: Int, val label: String, val index: Int)

    fun rollSpinRewardDynamic(): Int {
        val activeRewards = spinRewardsState.value.filter { it.active }.sortedBy { it.displayOrder }
        if (activeRewards.isEmpty()) return -1
        val totalWeight = activeRewards.sumOf { it.probabilityWeight }
        if (totalWeight <= 0) return 0
        val roll = kotlin.random.Random.nextInt(totalWeight)
        var cumulative = 0
        for (i in activeRewards.indices) {
            cumulative += activeRewards[i].probabilityWeight
            if (roll < cumulative) {
                return i
            }
        }
        return 0
    }

    fun performSpinWheelTransaction(rewardIndex: Int, isAdSpin: Boolean, onResult: (Boolean, String?) -> Unit) {
        val userId = walletState.value.userId
        if (userId.isEmpty()) {
            onResult(false, "User not authenticated.")
            return
        }

        val activeRewards = spinRewardsState.value.filter { it.active }.sortedBy { it.displayOrder }
        if (rewardIndex < 0 || rewardIndex >= activeRewards.size) {
            onResult(false, "Invalid reward option.")
            return
        }

        val selectedReward = activeRewards[rewardIndex]
        val rewardAmount = if (selectedReward.type.trim().equals("Coins", ignoreCase = true)) {
            selectedReward.value.toIntOrNull() ?: 0
        } else {
            0
        }

        val config = spinWheelConfigState.value
        val dailyFreeSpins = config.dailyFreeSpins
        val dailySpinLimit = config.dailySpinLimit
        val maxRewardedAdSpinsPerDay = config.maxRewardedAdSpinsPerDay

        val today = getLocalDateString()

        executeRewardTransaction(
            userId = userId,
            amount = rewardAmount,
            type = "spin_reward",
            source = "Spin Wheel Reward",
            extraCheck = { mutableData ->
                verifyAndLogBooleans(mutableData)
                var lastSpinDate = mutableData.child("lastSpinDate").getValue(String::class.java) ?: ""
                var dailySpinCount = mutableData.child("dailySpinCount").getValue(Int::class.java) ?: 0
                var rewardedSpinCount = mutableData.child("rewardedSpinCount").getValue(Int::class.java) ?: 0

                if (lastSpinDate != today) {
                    lastSpinDate = today
                    dailySpinCount = 0
                    rewardedSpinCount = 0
                }

                if (dailySpinCount >= dailySpinLimit) {
                    "Daily Spin Limit Reached. Max limit is $dailySpinLimit."
                } else if (isAdSpin && (rewardedSpinCount >= maxRewardedAdSpinsPerDay)) {
                    "Daily Rewarded Ad Spin Limit Reached."
                } else if (!isAdSpin && (dailySpinCount >= dailyFreeSpins)) {
                    "Free spins exhausted. Please watch a rewarded ad to spin."
                } else {
                    null
                }
            },
            extraUpdate = { mutableData ->
                verifyAndLogBooleans(mutableData)
                var lastSpinDate = mutableData.child("lastSpinDate").getValue(String::class.java) ?: ""
                var dailySpinCount = mutableData.child("dailySpinCount").getValue(Int::class.java) ?: 0
                var rewardedSpinCount = mutableData.child("rewardedSpinCount").getValue(Int::class.java) ?: 0
                var freeSpinUsed = mutableData.getSafeBoolean("freeSpinUsed")
                var rewardAdSpinUsed = mutableData.getSafeBoolean("rewardAdSpinUsed")
                var remainingSpins = mutableData.child("remainingSpins").getValue(Int::class.java) ?: dailyFreeSpins

                if (lastSpinDate != today) {
                    lastSpinDate = today
                    dailySpinCount = 0
                    rewardedSpinCount = 0
                    freeSpinUsed = false
                    rewardAdSpinUsed = false
                    remainingSpins = dailyFreeSpins
                }

                val isRetry = selectedReward.type.trim().equals("Retry", ignoreCase = true) || 
                              selectedReward.type.trim().equals("Spin Again", ignoreCase = true)

                if (!isRetry) {
                    dailySpinCount++
                    if (isAdSpin) {
                        rewardedSpinCount++
                        rewardAdSpinUsed = true
                    } else {
                        freeSpinUsed = true
                    }
                    remainingSpins = (dailySpinLimit - dailySpinCount).coerceAtLeast(0)
                }

                mutableData.child("lastSpinDate").value = today
                mutableData.child("dailySpinCount").value = dailySpinCount
                mutableData.child("rewardedSpinCount").value = rewardedSpinCount
                mutableData.child("freeSpinUsed").value = (dailySpinCount >= dailyFreeSpins)
                mutableData.child("rewardAdSpinUsed").value = rewardAdSpinUsed
                mutableData.child("remainingSpins").value = remainingSpins
                mutableData.child("lastSpinTimestamp").value = com.google.firebase.database.ServerValue.TIMESTAMP
                if (isAdSpin) {
                    mutableData.child("lastRewardedAdTimestamp").value = com.google.firebase.database.ServerValue.TIMESTAMP
                }

                val totalRewards = mutableData.child("totalSpinRewards").getValue(Int::class.java) ?: 0
                mutableData.child("totalSpinRewards").value = totalRewards + rewardAmount
            },
            onComplete = { success, coinsBefore, coinsAfter, errorMsg ->
                if (success) {
                    android.util.Log.d("PlayWinDebug", "Reward granted: ${selectedReward.name} of type ${selectedReward.type} (value: ${selectedReward.value})")
                    android.util.Log.d("PlayWinDebug", "Wallet updated: coinsBefore = $coinsBefore, coinsAfter = $coinsAfter")

                    viewModelScope.launch {
                        try {
                            val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                            val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                            val historyId = db.getReference("spinHistory/$userId").push().key ?: "spin_${System.currentTimeMillis()}"
                            
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                            val formattedDate = sdf.format(java.util.Date())

                            val historyMap = mapOf(
                                "userId" to userId,
                                "dateTime" to formattedDate,
                                "timestamp" to System.currentTimeMillis(),
                                "rewardName" to selectedReward.name,
                                "rewardType" to selectedReward.type,
                                "rewardValue" to selectedReward.value,
                                "spinType" to if (isAdSpin) "Rewarded Ad" else "Free",
                                "serverTimestamp" to com.google.firebase.database.ServerValue.TIMESTAMP,
                                "walletHistoryTimestamp" to com.google.firebase.database.ServerValue.TIMESTAMP
                            )

                            db.getReference("spinHistory/$userId/$historyId").setValue(historyMap)
                            db.getReference("spinHistoryGlobal/$historyId").setValue(historyMap)

                            if (selectedReward.type.trim().equals("Coupon", ignoreCase = true)) {
                                val requestId = "spin_coupon_" + System.currentTimeMillis()
                                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                                val currentUser = auth.currentUser
                                val dName = currentUser?.displayName ?: "Player"
                                val emailVal = currentUser?.email ?: ""
                                val couponCodeVal = "SPIN-" + selectedReward.name.take(4).uppercase() + "-" + (100000..999999).random()
                                val couponRedemption = com.playwin.app.data.model.FirebaseCouponRedemption(
                                    requestId = requestId,
                                    userUid = userId,
                                    displayName = dName,
                                    email = emailVal,
                                    mobileNumber = "",
                                    couponName = selectedReward.name,
                                    requiredCoins = 0,
                                    giftCardOrRechargeNumber = couponCodeVal,
                                    status = "Approved",
                                    createdAt = System.currentTimeMillis()
                                )
                                db.getReference("couponRedemptions/$requestId").setValue(couponRedemption)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("PlayWinViewModel", "Error saving spin reward history / coupon redemption: ${e.message}")
                        }
                    }
                }
                onResult(success, errorMsg)
            }
        )
    }

    fun grantAdSpinRewardLocally() {
        viewModelScope.launch {
            try {
                val current = walletState.value
                val updated = current.copy(
                    remainingSpins = current.remainingSpins + 1,
                    dailySpinCount = (current.dailySpinCount - 1).coerceAtLeast(0)
                )
                repository.saveWalletLocally(updated)
                android.util.Log.d("PlayWinDebug", "STATE_REFRESHED")
                android.util.Log.d("PlayWinDebug", "SPINS_LEFT=${updated.remainingSpins}")
                val isAdRequired = updated.dailySpinCount >= spinWheelConfigState.value.dailyFreeSpins
                android.util.Log.d("PlayWinDebug", "BUTTON_STATE=${if (updated.remainingSpins == 0) "TODAYS_SPINS_COMPLETED" else if (isAdRequired) "WATCH_AD" else "SPIN_NOW"}")
            } catch (e: Exception) {
                android.util.Log.e("PlayWinDebug", "Error granting ad spin locally: ${e.message}")
            }
        }
    }

    fun unlockAdSpin(onResult: (Boolean, String?) -> Unit) {
        onResult(true, null)
    }

    fun rollScratchReward(): SpinRewardOption {
        val roll = (1..100).random()
        return when {
            roll <= 35 -> SpinRewardOption(5, "+5 Coins", index = 0)
            roll <= 60 -> SpinRewardOption(10, "+10 Coins", index = 1)
            roll <= 80 -> SpinRewardOption(20, "+20 Coins", index = 2)
            roll <= 90 -> SpinRewardOption(30, "+30 Coins", index = 3)
            roll <= 97 -> SpinRewardOption(50, "+50 Coins", index = 4)
            roll <= 99 -> SpinRewardOption(100, "+100 Coins", index = 5)
            else -> SpinRewardOption(200, "+200 Coins", index = 6)
        }
    }

    fun performScratchCardTransaction(isAdScratch: Boolean, onResult: (Boolean, Int, String?) -> Unit) {
        val currentWallet = walletState.value
        val userId = currentWallet.userId
        if (userId.isEmpty()) {
            onResult(false, -1, "User not authenticated.")
            return
        }

        val rewardRoll = rollScratchReward()
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        executeRewardTransaction(
            userId = userId,
            amount = rewardRoll.amount,
            type = "scratch_reward",
            source = "Scratch Card Reward",
            extraCheck = { mutableData ->
                verifyAndLogBooleans(mutableData)
                val lastScratchDate = mutableData.child("lastScratchDate").getValue(String::class.java) ?: ""
                var freeScratchUsed = mutableData.getSafeBoolean("freeScratchUsed")
                var rewardAdScratchUsed = mutableData.getSafeBoolean("rewardAdScratchUsed")
                var remainingScratchCards = mutableData.child("remainingScratchCards").getValue(Int::class.java) ?: 0

                var checkLastScratchDate = lastScratchDate
                var checkFreeScratchUsed = freeScratchUsed
                var checkRewardAdScratchUsed = rewardAdScratchUsed
                var checkRemainingScratchCards = remainingScratchCards

                if (checkLastScratchDate != today) {
                    checkLastScratchDate = today
                    checkFreeScratchUsed = false
                    checkRewardAdScratchUsed = false
                    checkRemainingScratchCards = 1
                }

                if (isAdScratch) {
                    if (!checkFreeScratchUsed) {
                        "Please use your free scratch first."
                    } else if (!checkRewardAdScratchUsed) {
                        "Please watch a rewarded ad to unlock the extra scratch."
                    } else if (checkRemainingScratchCards <= 0) {
                        "No extra scratch available. Daily limit reached."
                    } else {
                        null
                    }
                } else {
                    if (checkFreeScratchUsed) {
                        "Free scratch already used today."
                    } else if (checkRemainingScratchCards <= 0) {
                        "No free scratch available."
                    } else {
                        null
                    }
                }
            },
            extraUpdate = { mutableData ->
                verifyAndLogBooleans(mutableData)
                val lastScratchDate = mutableData.child("lastScratchDate").getValue(String::class.java) ?: ""
                var freeScratchUsed = mutableData.getSafeBoolean("freeScratchUsed")
                var rewardAdScratchUsed = mutableData.getSafeBoolean("rewardAdScratchUsed")
                var remainingScratchCards = mutableData.child("remainingScratchCards").getValue(Int::class.java) ?: 0

                if (lastScratchDate != today) {
                    freeScratchUsed = false
                    rewardAdScratchUsed = false
                    remainingScratchCards = 1
                }

                if (isAdScratch) {
                    remainingScratchCards = 0
                } else {
                    freeScratchUsed = true
                    remainingScratchCards = 0
                }

                mutableData.child("lastScratchDate").value = today
                mutableData.child("freeScratchUsed").value = freeScratchUsed
                mutableData.child("rewardAdScratchUsed").value = rewardAdScratchUsed
                mutableData.child("remainingScratchCards").value = remainingScratchCards

                if (rewardRoll.amount > 0) {
                    val totalRewards = mutableData.child("totalScratchRewards").getValue(Int::class.java) ?: 0
                    mutableData.child("totalScratchRewards").value = totalRewards + rewardRoll.amount
                }
            },
            onComplete = { success, _, _, errorMsg ->
                if (success) {
                    refreshUserData()
                }
                onResult(success, rewardRoll.index, errorMsg)
            }
        )
    }

    fun unlockAdScratch(onResult: (Boolean, String?) -> Unit) {
        val userId = walletState.value.userId
        if (userId.isEmpty()) {
            onResult(false, "User not authenticated.")
            return
        }

        try {
            val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
            val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

            val userRef = db.getReference("users/$userId")
            userRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(mutableData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                    if (mutableData.value == null) {
                        return com.google.firebase.database.Transaction.success(mutableData)
                    }

                    verifyAndLogBooleans(mutableData)
                    var lastScratchDate = mutableData.child("lastScratchDate").getValue(String::class.java) ?: ""
                    var freeScratchUsed = mutableData.getSafeBoolean("freeScratchUsed")
                    var rewardAdScratchUsed = mutableData.getSafeBoolean("rewardAdScratchUsed")
                    var remainingScratchCards = mutableData.child("remainingScratchCards").getValue(Int::class.java) ?: 0

                    if (lastScratchDate != today) {
                        lastScratchDate = today
                        freeScratchUsed = false
                        rewardAdScratchUsed = false
                        remainingScratchCards = 1
                    }

                    if (!freeScratchUsed || rewardAdScratchUsed) {
                        return com.google.firebase.database.Transaction.abort()
                    }

                    rewardAdScratchUsed = true
                    remainingScratchCards = 1

                    mutableData.child("lastScratchDate").value = today
                    mutableData.child("freeScratchUsed").value = freeScratchUsed
                    mutableData.child("rewardAdScratchUsed").value = rewardAdScratchUsed
                    mutableData.child("remainingScratchCards").value = remainingScratchCards

                    return com.google.firebase.database.Transaction.success(mutableData)
                }

                override fun onComplete(error: com.google.firebase.database.DatabaseError?, committed: Boolean, currentData: com.google.firebase.database.DataSnapshot?) {
                    if (committed) {
                        refreshUserData()
                        onResult(true, null)
                    } else {
                        onResult(false, error?.message ?: "Unlock transaction failed.")
                    }
                }
            })
        } catch (ex: Exception) {
            onResult(false, ex.message ?: "Operation failed.")
        }
    }

    val hasMoreTransactions: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        _firebaseTransactions,
        searchQuery,
        selectedFilter,
        currentPage
    ) { txList, query, filter, page ->
        var filtered = txList
        if (filter != "All") {
            filtered = filtered.filter { tx ->
                when (filter) {
                    "Rewards" -> tx.type == "daily_reward" || tx.type == "spin_reward" || tx.type == "scratch_reward" || tx.type == "reward"
                    "Referrals" -> tx.type == "referral"
                    "Ads" -> tx.type == "video_ad"
                    "Spins" -> tx.type == "spin_reward"
                    "Scratch Cards" -> tx.type == "scratch_reward"
                    "Redeemed" -> tx.type == "coupon_redeemed" || tx.coins < 0
                    else -> true
                }
            }
        }
        if (query.trim().isNotEmpty()) {
            val q = query.trim().lowercase(java.util.Locale.ROOT)
            filtered = filtered.filter { tx ->
                tx.title.lowercase(java.util.Locale.ROOT).contains(q) || 
                tx.type.lowercase(java.util.Locale.ROOT).contains(q)
            }
        }
        filtered.size > page * pageSize
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun loadNextTransactionPage() {
        currentPage.value = currentPage.value + 1
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
        currentPage.value = 1
    }

    fun setFilter(filter: String) {
        selectedFilter.value = filter
        currentPage.value = 1
    }

    private fun startFirebaseSync(userId: String) {
        stopFirebaseSync()

        // Start observing the centralized daily system node for this user
        com.playwin.app.data.repository.DailyResetManager.observeUserDailySystem(userId, viewModelScope)

        val context = getApplication<Application>()
        val deviceId = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: ""
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                val userRef = db.getReference("users/$userId")
                if (deviceId.isNotEmpty()) {
                    userRef.child("deviceId").setValue(deviceId)
                }
                val autoCode = "PLAYWIN_${userId.take(6).uppercase()}"
                userRef.child("referralCode").setValue(autoCode)
                db.getReference("referralCodes/$autoCode").setValue(userId)
            } catch (e: Exception) {
                // ignore
            }
        }

        firebaseUserJob = viewModelScope.launch {
            repository.getFirebaseUserFlow(userId).collect { dbUser ->
                if (dbUser != null) {
                    android.util.Log.d(
                        "PlayWinCoinSystem",
                        """
                        ================ FIREBASE SYNC OBSERVER TRIGGERED ================
                        User ID: $userId
                        Firebase Coins Observed: ${dbUser.coins}
                        Local Room Coins (Before update): ${walletState.value.coins}
                        Action: Syncing Firebase value to local Room database.
                        ==================================================================
                        """.trimIndent()
                    )
                    android.util.Log.d("PlayWinVM", "Display Name Loaded: ${dbUser.displayName}")
                    currentUserState.value = dbUser
                    currentUserBlockedState.value = dbUser.isBlockedBool
                    val currentWallet = walletState.value
                    if (currentWallet.userId == userId || currentWallet.userId.isEmpty()) {
                        val syncWallet = currentWallet.copy(
                            coins = dbUser.coins,
                            dailyStreak = dbUser.streak,
                            lastCheckInTime = dbUser.lastCheckInTime,
                            dailyAdsWatched = dbUser.dailyAdsWatched,
                            lastAdResetTime = dbUser.lastAdResetTime,
                            referredBy = dbUser.referredBy,
                            hasUsedReferralCode = dbUser.hasUsedReferralCodeBool,
                            totalReferrals = dbUser.totalReferrals,
                            remainingSpins = dbUser.remainingSpins,
                            totalSpinRewards = dbUser.totalSpinRewards,
                            remainingScratchCards = dbUser.remainingScratchCards,
                            lastScratchResetTime = dbUser.lastScratchResetTime,
                            totalScratchRewards = dbUser.totalScratchRewards,
                            lastSpinDate = dbUser.lastSpinDate,
                            freeSpinUsed = dbUser.freeSpinUsedBool,
                            rewardAdSpinUsed = dbUser.rewardAdSpinUsedBool,
                            dailySpinCount = dbUser.dailySpinCount,
                            rewardedSpinCount = dbUser.rewardedSpinCount,
                            freeScratchUsed = dbUser.freeScratchUsedBool,
                            rewardAdScratchUsed = dbUser.rewardAdScratchUsedBool,
                            lastScratchDate = dbUser.lastScratchDate,
                            lastCheckInDate = dbUser.lastCheckInDate,
                            totalCheckInRewards = dbUser.totalCheckInRewards,
                            lastRewardAdTime = dbUser.lastRewardAdTime,
                            pendingRewards = dbUser.pendingRewards,
                            referralsCoinsEarned = dbUser.referralsCoinsEarned,
                            userId = userId
                        )
                        repository.saveWalletLocally(syncWallet)
                    }
                }
            }
        }

        firebaseTxJob = viewModelScope.launch {
            repository.getFirebaseTransactionsFlow(userId).collect { txList ->
                _firebaseTransactions.value = txList
            }
        }

        firebaseUserScratchCardJob = viewModelScope.launch {
            repository.getFirebaseUserScratchCardStateFlow(userId).collect { state ->
                userScratchCardStateState.value = state
            }
        }

        firebaseRedemptionJob = viewModelScope.launch {
            repository.getFirebaseRedemptionsFlow(userId).collect { redList ->
                redemptionsState.value = redList
            }
        }

        firebaseUserDailyCheckInJob = viewModelScope.launch {
            try {
                val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                val checkInRef = db.getReference("users/$userId/dailyCheckIn")
                val listener = object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        if (snapshot.exists()) {
                            val userCheckIn = snapshot.getValue(com.playwin.app.data.model.FirebaseUserDailyCheckIn::class.java)
                            userDailyCheckInState.value = userCheckIn
                        } else {
                            userDailyCheckInState.value = com.playwin.app.data.model.FirebaseUserDailyCheckIn()
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                }
                checkInRef.addValueEventListener(listener)
                try {
                    kotlinx.coroutines.awaitCancellation()
                } finally {
                    checkInRef.removeEventListener(listener)
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayWinCheckIn", "Error listening to user daily check-in", e)
            }
        }

        dailyQuizJob = viewModelScope.launch {
            try {
                val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                val dailyQuizRef = db.getReference("users/$userId/dailyQuiz")
                val listener = object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        if (snapshot.exists()) {
                            val userDailyQuiz = snapshot.getValue(com.playwin.app.data.model.FirebaseUserDailyQuiz::class.java)
                            dailyQuizState.value = userDailyQuiz
                        } else {
                            dailyQuizState.value = com.playwin.app.data.model.FirebaseUserDailyQuiz()
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                }
                dailyQuizRef.addValueEventListener(listener)
                try {
                    kotlinx.coroutines.awaitCancellation()
                } finally {
                    dailyQuizRef.removeEventListener(listener)
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayWinDailyQuiz", "Error listening to user daily quiz state", e)
            }
        }

        couponRedemptionsJob = viewModelScope.launch {
            repository.getFirebaseCouponRedemptionsFlow(userId).collect { list ->
                couponRedemptionsState.value = list
            }
        }

        withdrawRequestsJob = viewModelScope.launch {
            repository.getFirebaseWithdrawRequestsFlow(userId).collect { list ->
                withdrawRequestsState.value = list
            }
        }

        firebaseQuizProgressJob = viewModelScope.launch {
            repository.getFirebaseQuizProgressFlow(userId).collect { progress ->
                quizProgressState.value = progress
            }
        }

        firebaseCompletedQuizzesJob = viewModelScope.launch {
            repository.getFirebaseCompletedQuizzesFlow(userId).collect { completedMap ->
                completedQuizzesState.value = completedMap
            }
        }

        firebaseWeeklyQuizProgressJob = viewModelScope.launch {
            repository.getFirebaseWeeklyQuizProgressFlow(userId).collect { progressMap ->
                weeklyQuizProgressState.value = progressMap
            }
        }

        allUsersJob = viewModelScope.launch {
            repository.firebaseAllUsersFlow.collect { list ->
                allUsersState.value = list
            }
        }

        quizzesJob = viewModelScope.launch {
            repository.getFirebaseQuizzesFlow().collect { list ->
                quizzesState.value = list
            }
        }



        referralsHistoryJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
            val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
            val ref = db.getReference("referrals/$userId")
            val listener = object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val list = mutableListOf<FirebaseReferralRecord>()
                    for (child in snapshot.children) {
                        try {
                            val record = child.getValue(FirebaseReferralRecord::class.java)
                            if (record != null) {
                                list.add(record)
                            }
                        } catch (e: Exception) {
                            // ignore parse issues
                        }
                    }
                    list.sortByDescending { it.timestamp }
                    referralHistoryState.value = list
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            }
            ref.addValueEventListener(listener)
            try {
                kotlinx.coroutines.awaitCancellation()
            } finally {
                ref.removeEventListener(listener)
            }
        }

        // Realtime listeners for direct Firebase synchronization
        viewModelScope.launch {
            try {
                val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)

                // 1. wallet listener: users/{userId}/wallet
                firebaseWalletListener = object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        if (snapshot.exists()) {
                            val coins = snapshot.child("coins").getValue(Int::class.java) ?: 0
                            val totalScratchRewards = snapshot.child("totalScratchRewards").getValue(Int::class.java) ?: 0
                            android.util.Log.d("PlayWinRealtime", "Realtime wallet updated: coins=$coins, totalScratchRewards=$totalScratchRewards")
                            
                            viewModelScope.launch {
                                val currentWallet = walletState.value
                                if (currentWallet.userId == userId) {
                                    val updated = currentWallet.copy(
                                        coins = coins,
                                        totalScratchRewards = totalScratchRewards
                                    )
                                    repository.saveWalletLocally(updated)
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                }
                db.getReference("users").child(userId).child("wallet").addValueEventListener(firebaseWalletListener!!)

                // 2. walletSummary listener: walletSummary/{userId}
                firebaseWalletSummaryListener = object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        if (snapshot.exists()) {
                            val coins = snapshot.child("coins").getValue(Int::class.java) ?: 0
                            android.util.Log.d("PlayWinRealtime", "Realtime walletSummary updated: coins=$coins")
                            viewModelScope.launch {
                                val currentWallet = walletState.value
                                if (currentWallet.userId == userId && currentWallet.coins != coins) {
                                    val updated = currentWallet.copy(coins = coins)
                                    repository.saveWalletLocally(updated)
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                }
                db.getReference("walletSummary").child(userId).addValueEventListener(firebaseWalletSummaryListener!!)

                // 3. transactions listener: transactions/{userId}
                firebaseTransactionsListener = object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        val txList = mutableListOf<com.playwin.app.data.model.FirebaseTransaction>()
                        for (child in snapshot.children) {
                            val tx = child.getValue(com.playwin.app.data.model.FirebaseTransaction::class.java)
                            if (tx != null) {
                                txList.add(tx.copy(id = child.key ?: tx.id))
                            }
                        }
                        txList.sortByDescending { it.timestamp }
                        android.util.Log.d("PlayWinRealtime", "Realtime transactions updated: size=${txList.size}")
                        _firebaseTransactions.value = txList
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                }
                db.getReference("transactions").child(userId).addValueEventListener(firebaseTransactionsListener!!)

                // 4. history listener: users/{userId}/wallet/history
                firebaseHistoryListener = object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        if (snapshot.exists()) {
                            val totalScratchRewardsVal = snapshot.child("totalScratchRewards").getValue(Int::class.java) ?: 0
                            android.util.Log.d("PlayWinRealtime", "Realtime wallet/history updated: totalScratchRewards=$totalScratchRewardsVal")
                            viewModelScope.launch {
                                val currentWallet = walletState.value
                                if (currentWallet.userId == userId && currentWallet.totalScratchRewards != totalScratchRewardsVal) {
                                    val updated = currentWallet.copy(totalScratchRewards = totalScratchRewardsVal)
                                    repository.saveWalletLocally(updated)
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                }
                db.getReference("users").child(userId).child("wallet").child("history").addValueEventListener(firebaseHistoryListener!!)

                // 5. scratchHistory listener: users/{userId}/scratchHistory
                firebaseScratchHistoryListener = object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        android.util.Log.d("PlayWinRealtime", "Realtime scratchHistory updated")
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                }
                db.getReference("users").child(userId).child("scratchHistory").addValueEventListener(firebaseScratchHistoryListener!!)

            } catch (e: Exception) {
                android.util.Log.e("PlayWinRealtime", "Error starting realtime observers", e)
            }
        }
    }

    private fun stopFirebaseSync() {
        val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
        val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
        val uid = walletState.value.userId
        if (uid.isNotEmpty()) {
            firebaseWalletListener?.let { db.getReference("users").child(uid).child("wallet").removeEventListener(it) }
            firebaseWalletSummaryListener?.let { db.getReference("walletSummary").child(uid).removeEventListener(it) }
            firebaseTransactionsListener?.let { db.getReference("transactions").child(uid).removeEventListener(it) }
            firebaseHistoryListener?.let { db.getReference("users").child(uid).child("wallet").child("history").removeEventListener(it) }
            firebaseScratchHistoryListener?.let { db.getReference("users").child(uid).child("scratchHistory").removeEventListener(it) }
        }
        firebaseWalletListener = null
        firebaseWalletSummaryListener = null
        firebaseTransactionsListener = null
        firebaseHistoryListener = null
        firebaseScratchHistoryListener = null

        firebaseUserJob?.cancel()
        firebaseUserJob = null
        firebaseTxJob?.cancel()
        firebaseTxJob = null
        firebaseRedemptionJob?.cancel()
        firebaseRedemptionJob = null
        firebaseUserDailyCheckInJob?.cancel()
        firebaseUserDailyCheckInJob = null
        firebaseUserScratchCardJob?.cancel()
        firebaseUserScratchCardJob = null
        dailyQuizJob?.cancel()
        dailyQuizJob = null
        dailyQuizState.value = null
        withdrawRequestsJob?.cancel()
        withdrawRequestsJob = null
        allUsersJob?.cancel()
        allUsersJob = null
        couponRedemptionsJob?.cancel()
        couponRedemptionsJob = null
        firebaseQuizProgressJob?.cancel()
        firebaseQuizProgressJob = null
        firebaseCompletedQuizzesJob?.cancel()
        firebaseCompletedQuizzesJob = null
        firebaseWeeklyQuizProgressJob?.cancel()
        firebaseWeeklyQuizProgressJob = null
        referralsHistoryJob?.cancel()
        referralsHistoryJob = null
        quizzesJob?.cancel()
        quizzesJob = null
        quizProgressState.value = null
        completedQuizzesState.value = emptyMap()
        weeklyQuizProgressState.value = emptyMap()
        referralHistoryState.value = emptyList()
        currentUserState.value = null
        userDailyCheckInState.value = null
        currentUserBlockedState.value = false
    }

    fun redeemCoupon(
        coupon: FirebaseCoupon,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentWallet = walletState.value
        val userId = currentWallet.userId
        
        if (userId.isEmpty()) {
            onError("User not authenticated.")
            return
        }
        
        if (currentWallet.coins < coupon.requiredCoins) {
            onError("Insufficient Coins")
            return
        }
        
        if (_isRedeeming.value) {
            onError("Redemption is already in progress.")
            return
        }
        
        _isRedeeming.value = true
        
        viewModelScope.launch {
            try {
                // Deduct coins safely using spendCoins which syncs with Room and Realtime Database
                val purpose = "Redeemed ${coupon.couponName}"
                val ok = spendCoins(coupon.requiredCoins, purpose)
                if (ok) {
                    val redemptionId = "red_" + System.currentTimeMillis() + "_" + (1000..9999).random()
                    val redemption = com.playwin.app.data.model.FirebaseRedemption(
                        id = redemptionId,
                        userId = userId,
                        couponId = coupon.couponId,
                        couponName = coupon.couponName,
                        coinsSpent = coupon.requiredCoins,
                        status = "Pending",
                        timestamp = System.currentTimeMillis(),
                        couponCode = coupon.code,
                        expiryDate = coupon.expiryDate
                    )
                    repository.addFirebaseRedemption(userId, redemption)
                    onSuccess()
                } else {
                    onError("Insufficient Coins")
                }
            } catch (e: Exception) {
                onError("Failed to redeem. Please try again.")
            } finally {
                _isRedeeming.value = false
            }
        }
    }



    fun redeemCouponWithForm(
        coupon: FirebaseCoupon,
        fullName: String,
        mobileNumber: String,
        email: String,
        rechargeNumber: String,
        additionalNotes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("PlayWin_ViewModel", "Entering redeemCouponWithForm:")
        android.util.Log.d("PlayWin_ViewModel", "  coupon.id: ${coupon.id}")
        android.util.Log.d("PlayWin_ViewModel", "  coupon.couponId: ${coupon.couponId}")
        android.util.Log.d("PlayWin_ViewModel", "  Firebase node key: ${coupon.couponId}")
        android.util.Log.d("PlayWin_ViewModel", "  Firebase path used for lookup: /coupons/${coupon.id}")

        val currentWallet = walletState.value
        val userId = currentWallet.userId

        if (userId.isEmpty()) {
            onError("User not authenticated.")
            return
        }

        if (currentWallet.coins < coupon.requiredCoins) {
            onError("Insufficient Coins")
            return
        }

        if (coupon.remainingStock <= 0) {
            onError("This coupon is out of stock.")
            return
        }

        if (_isRedeeming.value) {
            onError("Redemption is already in progress.")
            return
        }

        _isRedeeming.value = true

        viewModelScope.launch {
            try {
                val requestId = "req_" + System.currentTimeMillis() + "_" + (1000..9999).random()
                val redemption = com.playwin.app.data.model.FirebaseCouponRedemption(
                    requestId = requestId,
                    userUid = userId,
                    displayName = fullName,
                    email = email,
                    mobileNumber = mobileNumber,
                    couponName = coupon.couponName,
                    requiredCoins = coupon.requiredCoins,
                    giftCardOrRechargeNumber = rechargeNumber,
                    additionalNotes = additionalNotes,
                    status = "Pending",
                    createdAt = System.currentTimeMillis()
                )

                repository.submitCouponRedemptionTransaction(
                    redemption = redemption,
                    couponId = coupon.couponId,
                    onSuccess = {
                        viewModelScope.launch {
                            val newCoins = currentWallet.coins - coupon.requiredCoins
                            val updatedWallet = currentWallet.copy(coins = newCoins)
                            repository.saveWalletLocally(updatedWallet)
                        }
                        _isRedeeming.value = false
                        onSuccess()
                    },
                    onError = { err ->
                        _isRedeeming.value = false
                        onError(err)
                    }
                )
            } catch (e: Exception) {
                _isRedeeming.value = false
                onError("Failed to submit redemption request.")
            }
        }
    }

    // --- UPI WITHDRAW ACTIONS ---

    fun submitWithdraw(
        upiId: String,
        holderName: String,
        coinAmount: Int,
        rupeeAmount: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentWallet = walletState.value
        val userId = currentWallet.userId
        val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: currentWallet.userId
        val userName = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName ?: holderName
        
        if (userId.isEmpty()) {
            onError("Authentication Required")
            return
        }
        
        if (currentWallet.coins < coinAmount) {
            onError("Insufficient Coins")
            return
        }

        _isSubmittingWithdraw.value = true
        repository.submitWithdrawRequest(
            uid = userId,
            userName = userName,
            email = email,
            upiId = upiId,
            amount = rupeeAmount,
            coinsSpent = coinAmount,
            onComplete = { success, errorMsg ->
                _isSubmittingWithdraw.value = false
                if (success) {
                    viewModelScope.launch {
                        val updatedWallet = currentWallet.copy(coins = currentWallet.coins - coinAmount)
                        repository.saveWalletLocally(updatedWallet)
                        repository.addTransaction(userId, -coinAmount, "Withdraw Request")
                    }
                    onSuccess()
                } else {
                    onError(errorMsg ?: "Failed to submit request")
                }
            }
        )
    }

    fun rollScratchRewardFromFirebase(): com.playwin.app.data.model.FirebaseScratchCardReward {
        android.util.Log.d("PlayWinScratchDebug", "--- [STEP 4 & 5 & 6] ROLLING SCRATCH REWARD FROM FIREBASE ---")
        val activeRewards = scratchCardRewardsState.value.filter { it.active }
        android.util.Log.d("PlayWinScratchDebug", "Active rewards available for roll: ${activeRewards.size}")

        // 10. Remove every fallback that automatically returns "Better Luck Next Time"
        // 11. Show "Better Luck Next Time" ONLY if the selected Firebase reward type is exactly "Better Luck Next Time".
        val selectedReward = if (activeRewards.isEmpty()) {
            com.playwin.app.data.model.FirebaseScratchCardReward(
                id = "empty_error",
                name = "Error: No active rewards configured in database",
                type = "Error",
                value = "0",
                icon = "❌",
                color = "#FF0000"
            )
        } else {
            val totalWeight = activeRewards.sumOf { it.probabilityWeight }
            if (totalWeight <= 0) {
                activeRewards.random()
            } else {
                val randomVal = (0 until totalWeight).random()
                var currentSum = 0
                var rolled: com.playwin.app.data.model.FirebaseScratchCardReward? = null
                for (reward in activeRewards) {
                    currentSum += reward.probabilityWeight
                    if (randomVal < currentSum) {
                        rolled = reward
                        break
                    }
                }
                rolled ?: activeRewards.last()
            }
        }

        // 4. Print the selected reward ID, type, name and value.
        android.util.Log.d("PlayWinScratchDebug", "[STEP 4] SELECTED REWARD: ID: ${selectedReward.id}, Type: ${selectedReward.type}, Name: ${selectedReward.name}, Value: ${selectedReward.value}")

        // 5. Verify reward.value is parsed correctly as an integer.
        val parsedValue = selectedReward.value.toIntOrNull()
        if (selectedReward.type == "Coins" && parsedValue == null) {
            android.util.Log.e("PlayWinScratchDebug", "PARSING FAILURE: Field 'value' with content '${selectedReward.value}' could not be parsed as an Integer for Coins reward ID '${selectedReward.id}'.")
        } else if (parsedValue != null) {
            android.util.Log.d("PlayWinScratchDebug", "[STEP 5] VERIFIED: reward.value (${selectedReward.value}) successfully parsed as integer: $parsedValue")
        } else {
            android.util.Log.d("PlayWinScratchDebug", "[STEP 5] reward.value (${selectedReward.value}) is not an integer, but reward type is ${selectedReward.type}")
        }

        // 6. Verify reward.type matches exactly.
        val validTypes = listOf("Coins", "Coupon", "Retry", "Retry Scratch", "Better Luck Next Time")
        if (selectedReward.type in validTypes) {
            android.util.Log.d("PlayWinScratchDebug", "[STEP 6] VERIFIED: reward.type (${selectedReward.type}) matches exactly a valid type.")
        } else {
            android.util.Log.e("PlayWinScratchDebug", "[STEP 6] FAILURE: reward.type (${selectedReward.type}) is invalid or is an error/fallback state! Valid types: $validTypes")
        }

        return selectedReward
    }

    fun performScratchCardTransactionSecure(
        rolledReward: com.playwin.app.data.model.FirebaseScratchCardReward,
        transactionId: String,
        isAdScratch: Boolean,
        onResult: (Boolean, com.playwin.app.data.model.FirebaseScratchCardReward?, String?) -> Unit
    ) {
        val userId = walletState.value.userId
        if (userId.isEmpty()) {
            onResult(false, null, "User not authenticated.")
            return
        }

        // 7. Verify the selected reward is passed into the wallet transaction.
        android.util.Log.d("PlayWinScratchDebug", "[STEP 7] PASSING SELECTED REWARD TO WALLET TRANSACTION: ID: ${rolledReward.id}, Type: ${rolledReward.type}, Value: ${rolledReward.value}, txId: $transactionId, isAdScratch: $isAdScratch")

        repository.performScratchCardDbTransaction(userId, rolledReward, transactionId, isAdScratch) { success, error, _, coinsAfter ->
            if (success) {
                viewModelScope.launch {
                    val currentWallet = walletState.value
                    val typeSafe = rolledReward.type?.trim() ?: "Coins"
                    val isCoins = typeSafe.equals("Coins", ignoreCase = true) || 
                                  (!typeSafe.equals("Coupon", ignoreCase = true) && 
                                   !typeSafe.contains("retry", ignoreCase = true) && 
                                   !typeSafe.contains("luck", ignoreCase = true) && 
                                   ((rolledReward.value ?: "0").toIntOrNull() ?: 0) > 0)
                    val rewardValueCoins = if (isCoins) (rolledReward.value ?: "0").toIntOrNull() ?: 0 else 0
                    val updatedWallet = currentWallet.copy(
                        coins = coinsAfter,
                        totalScratchRewards = currentWallet.totalScratchRewards + rewardValueCoins
                    )
                    repository.saveWalletLocally(updatedWallet)

                    // Force instant client-side update of transactions list
                    val newTx = FirebaseTransaction(
                        id = transactionId,
                        userId = userId,
                        type = "scratch_reward",
                        title = "Scratch Card: ${rolledReward.name}",
                        coins = rewardValueCoins,
                        status = "Completed",
                        timestamp = System.currentTimeMillis(),
                        amount = rewardValueCoins,
                        source = "Scratch Card: ${rolledReward.name}",
                        coinsBefore = currentWallet.coins,
                        coinsAfter = coinsAfter
                    )
                    _firebaseTransactions.value = (listOf(newTx) + _firebaseTransactions.value).sortedByDescending { it.timestamp }

                    // Force instant client-side update of user scratch card state (scratches today, cooldown)
                    val currentScratchState = userScratchCardStateState.value
                    val updatedScratchState = currentScratchState.copy(
                        scratchesToday = currentScratchState.scratchesToday + 1,
                        freeScratchUsed = if (isAdScratch) currentScratchState.freeScratchUsed else currentScratchState.freeScratchUsed + 1,
                        rewardedScratchUsed = if (isAdScratch) currentScratchState.rewardedScratchUsed + 1 else currentScratchState.rewardedScratchUsed,
                        lastScratchTimestamp = System.currentTimeMillis()
                    )
                    userScratchCardStateState.value = updatedScratchState

                    // Force instant client-side update of current user state
                    currentUserState.value = currentUserState.value?.copy(
                        coins = coinsAfter,
                        totalScratchRewards = (currentUserState.value?.totalScratchRewards ?: 0) + rewardValueCoins,
                        scratchesToday = (currentUserState.value?.scratchesToday ?: 0) + 1
                    )

                    refreshUserData()
                    onResult(true, rolledReward, null)
                }
            } else {
                onResult(false, null, error ?: "Transaction aborted.")
            }
        }
    }

    fun adminModifyCoinsManually(targetUid: String, amount: Int, reason: String, onComplete: (Boolean) -> Unit) {
        executeRewardTransaction(
            userId = targetUid,
            amount = amount,
            type = "admin_adjustment",
            source = reason,
            onComplete = { success, _, _, _ ->
                onComplete(success)
            }
        )
    }

    fun adminUpdateDailyCheckInSettings(settings: com.playwin.app.data.model.FirebaseDailyCheckInSettings, onComplete: (Boolean) -> Unit) {
        val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
        val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
        db.getReference("dailyCheckIn").setValue(settings).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun adminResetAllUsersDailyCheckIn(onComplete: (Boolean) -> Unit) {
        val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
        val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
        val users = allUsersState.value
        if (users.isEmpty()) {
            onComplete(true)
            return
        }
        var completedCount = 0
        var success = true
        for (user in users) {
            val userCheckInRef = db.getReference("users").child(user.uid)
            val updates = mapOf(
                "dailyCheckIn/streak" to 0,
                "dailyCheckIn/currentDay" to 0,
                "dailyCheckIn/lastClaimTimestamp" to 0L,
                "dailyCheckIn/nextEligibleTimestamp" to 0L,
                "dailyStreak" to 0,
                "lastCheckInTime" to 0L,
                "lastCheckInDate" to ""
            )
            userCheckInRef.updateChildren(updates).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    success = false
                }
                completedCount++
                if (completedCount == users.size) {
                    onComplete(success)
                }
            }
        }
    }
}
