package com.playwin.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playwin.app.data.database.PlayWinDatabase
import com.playwin.app.data.model.FirebaseCoupon
import com.playwin.app.data.model.FirebaseTask
import com.playwin.app.data.model.FirebaseReferralRecord
import com.playwin.app.data.model.FirebaseTransaction
import com.playwin.app.data.model.FirebaseUser
import com.playwin.app.data.model.RewardTransaction
import com.playwin.app.data.model.UserWallet
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

    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow("All")
    val currentPage = MutableStateFlow(1)
    val pageSize = 10

    private val _firebaseTransactions = MutableStateFlow<List<FirebaseTransaction>>(emptyList())
    private var firebaseUserJob: kotlinx.coroutines.Job? = null
    private var firebaseTxJob: kotlinx.coroutines.Job? = null
    private var firebaseRedemptionJob: kotlinx.coroutines.Job? = null

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
    private var referralsHistoryJob: kotlinx.coroutines.Job? = null
    private var quizzesJob: kotlinx.coroutines.Job? = null

    val quizProgressState = MutableStateFlow<com.playwin.app.data.model.FirebaseQuizProgress?>(null)
    val referralHistoryState = MutableStateFlow<List<FirebaseReferralRecord>>(emptyList())
    val quizzesState = MutableStateFlow<List<com.playwin.app.data.model.FirebaseQuiz>>(emptyList())

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

        // Observe auth state changes to start/stop live Firebase sync
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
                                            hasUsedReferralCode = dbUser.hasUsedReferralCode,
                                            totalReferrals = dbUser.totalReferrals,
                                            remainingSpins = dbUser.remainingSpins,
                                            totalSpinRewards = dbUser.totalSpinRewards,
                                            remainingScratchCards = dbUser.remainingScratchCards,
                                            lastScratchResetTime = dbUser.lastScratchResetTime,
                                            totalScratchRewards = dbUser.totalScratchRewards,
                                            lastSpinDate = dbUser.lastSpinDate,
                                            freeSpinUsed = dbUser.freeSpinUsed,
                                            rewardAdSpinUsed = dbUser.rewardAdSpinUsed,
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
        
        val usersRef = db.getReference("users")
        
        var existingUser: FirebaseUser? = null
        try {
            android.util.Log.d("PlayWinVM", "Checking direct profile at users/$uid")
            val directSnapshot = db.getReference("users/$uid").get().awaitTask()
            if (directSnapshot.exists()) {
                existingUser = directSnapshot.getValue(FirebaseUser::class.java)
                if (existingUser != null) {
                    android.util.Log.d("PlayWinVM", "Direct profile exists at users/$uid with coins: ${existingUser.coins}")
                }
            }
        } catch (ex: Exception) {
            android.util.Log.e("PlayWinVM", "Failed to fetch direct profile at users/$uid", ex)
        }

        var bestMatch: Pair<String, FirebaseUser>? = null

        if (existingUser != null) {
            bestMatch = Pair(uid, existingUser)
        } else {
            // Use email as unique identity. Before creating or logging a user, search users collection by email.
            var querySnapshot: com.google.firebase.database.DataSnapshot? = null
            try {
                android.util.Log.d("PlayWinVM", "Searching users collection by email: $email")
                querySnapshot = usersRef.orderByChild("email").equalTo(email).get().awaitTask()
            } catch (ex: Exception) {
                android.util.Log.e("PlayWinVM", "Failed to query users by email: $email", ex)
            }

            val matches = mutableListOf<Pair<String, FirebaseUser>>()
            querySnapshot?.children?.forEach { child ->
                val key = child.key
                val userProfile = child.getValue(FirebaseUser::class.java)
                if (key != null && userProfile != null && userProfile.email.equals(email, ignoreCase = true)) {
                    matches.add(Pair(key, userProfile))
                }
            }

            if (matches.size > 1) {
                // Log duplicate email detection to console
                android.util.Log.w("PlayWinVM", "DUPLICATE EMAIL DETECTED: Found ${matches.size} records matching email: $email")
            }

            // De-duplicate / Cleanup logic: keep only the newest record and delete old duplicates
            val sortedMatches = matches.sortedWith(
                compareByDescending<Pair<String, FirebaseUser>> { it.second.createdAt }
                    .thenByDescending { it.second.joinedAt }
                    .thenByDescending { it.second.lastActiveTime }
                    .thenByDescending { it.second.coins }
            )

            // Identify the best snapshot match
            bestMatch = sortedMatches.firstOrNull()

            // Clean up duplicate records if found (keep the newest, delete old duplicates)
            if (sortedMatches.size > 1 && bestMatch != null) {
                for (i in 1 until sortedMatches.size) {
                    val oldDuplicate = sortedMatches[i]
                    val oldUid = oldDuplicate.first
                    android.util.Log.d("PlayWinVM", "Cleanup duplicate old record for email $email with UID: $oldUid")
                    try {
                        db.getReference("users/$oldUid").removeValue().awaitTask()
                    } catch (delEx: Exception) {
                        android.util.Log.e("PlayWinVM", "Failed to delete old duplicate: $oldUid", delEx)
                    }
                }
            }
        }

        if (bestMatch != null) {
            // If a user with the same email already exists: Load exact existing user data and update it
            val existingUserObj = bestMatch.second
            val bestUid = bestMatch.first
            
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
                hasUsedReferralCode = existingUserObj.hasUsedReferralCode,
                totalReferrals = existingUserObj.totalReferrals,
                remainingSpins = existingUserObj.remainingSpins,
                totalSpinRewards = existingUserObj.totalSpinRewards,
                remainingScratchCards = existingUserObj.remainingScratchCards,
                lastScratchResetTime = existingUserObj.lastScratchResetTime,
                totalScratchRewards = existingUserObj.totalScratchRewards,
                lastSpinDate = existingUserObj.lastSpinDate,
                freeSpinUsed = existingUserObj.freeSpinUsed,
                rewardAdSpinUsed = existingUserObj.rewardAdSpinUsed,
                lastCheckInDate = existingUserObj.lastCheckInDate,
                totalCheckInRewards = existingUserObj.totalCheckInRewards,
                lastRewardAdTime = existingUserObj.lastRewardAdTime
            )
            repository.saveWalletLocally(syncWallet)
            
            // Build the updated user map
            val displayNameToUse = if (existingUserObj.displayName.isNotEmpty()) {
                existingUserObj.displayName
            } else {
                firebaseUser.displayName ?: "Player"
            }

            android.util.Log.d("PlayWinVM", "Display Name Loaded: $displayNameToUse")

            try {
                if (bestUid != uid) {
                    val userMap = mapOf(
                        "uid" to uid,
                        "displayName" to displayNameToUse,
                        "email" to email,
                        "coins" to existingUserObj.coins,
                        "level" to existingUserObj.level,
                        "streak" to existingUserObj.streak,
                        "totalReferrals" to existingUserObj.totalReferrals,
                        "rewardHistoryCount" to existingUserObj.rewardHistoryCount,
                        "joinedAt" to existingUserObj.joinedAt,
                        "referrals" to existingUserObj.referrals,
                        "records" to existingUserObj.records,
                        "createdAt" to existingUserObj.createdAt,
                        
                        // Legacy compatibility fields if needed
                        "userId" to uid,
                        "dailyStreak" to existingUserObj.streak,
                        "lastCheckInTime" to existingUserObj.lastCheckInTime,
                        "lastActiveTime" to System.currentTimeMillis(),
                        "dailyAdsWatched" to existingUserObj.dailyAdsWatched,
                        "lastAdResetTime" to existingUserObj.lastAdResetTime,
                        "lastSpinDate" to existingUserObj.lastSpinDate,
                        "freeSpinUsed" to existingUserObj.freeSpinUsed,
                        "rewardAdSpinUsed" to existingUserObj.rewardAdSpinUsed,
                        "totalSpinRewards" to existingUserObj.totalSpinRewards
                    )
                    // Update/write to the current UID's path (instead of creating duplicate profile)
                    db.getReference("users/$uid").setValue(userMap).awaitTask()
                    android.util.Log.d("PlayWinVM", "Display Name Saved: $displayNameToUse")
                    android.util.Log.d("PlayWinVM", "Profile migrated, loaded and updated successfully for UID: $uid")

                    android.util.Log.d("PlayWinVM", "Migrating profile key from old $bestUid to current logged-in UID: $uid")
                    db.getReference("users/$bestUid").removeValue().awaitTask()
                } else {
                    // It is already under the correct UID on Firebase. Only update the lastActiveTime to avoid overwriting or erasing any concurrently updating fields.
                    db.getReference("users/$uid/lastActiveTime").setValue(System.currentTimeMillis()).awaitTask()
                    android.util.Log.d("PlayWinVM", "Profile loaded directly, updated lastActiveTime for UID: $uid")
                }
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
                
                // Add console log: Profile saved successfully
                android.util.Log.d("PlayWinVM", "Profile saved successfully")
                
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
                // Add console log: Database write failed
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
        if (userId.isEmpty()) {
            onComplete(false, 0, 0, "User not authenticated.")
            return
        }

        viewModelScope.launch {
            rewardMutex.withLock {
                try {
                    val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                    val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                    val userRef = db.getReference("users").child(userId)

                    android.util.Log.d("PlayWinCoinSystem", "[START] Reward Transaction for user: $userId, amount: $amount, type: $type, source: $source")

                    userRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                        var previousCoins = 0
                        var newCoins = 0
                        var errorMessage: String? = null

                        override fun doTransaction(mutableData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                            if (mutableData.value == null) {
                                errorMessage = "User node does not exist in Firebase."
                                return com.google.firebase.database.Transaction.abort()
                            }

                            // Run extra validation/check if provided (to prevent duplicate writes)
                            val checkError = extraCheck?.invoke(mutableData)
                            if (checkError != null) {
                                errorMessage = checkError
                                return com.google.firebase.database.Transaction.abort()
                            }

                            // Read current coins
                            previousCoins = mutableData.child("coins").getValue(Int::class.java) ?: 0
                            newCoins = previousCoins + amount

                            // Update user coins
                            mutableData.child("coins").value = newCoins
                            mutableData.child("lastActiveTime").value = System.currentTimeMillis()

                            // Run extra updates if provided (like check-in times, spin usage, etc.)
                            extraUpdate?.invoke(mutableData)

                            return com.google.firebase.database.Transaction.success(mutableData)
                        }

                        override fun onComplete(
                            error: com.google.firebase.database.DatabaseError?,
                            committed: Boolean,
                            currentData: com.google.firebase.database.DataSnapshot?
                        ) {
                            if (committed && error == null) {
                                val displayedUiCoinsBefore = walletState.value.coins
                                android.util.Log.d(
                                    "PlayWinCoinSystem",
                                    """
                                    ================ COIN SYNC DEBUG REPORT ================
                                    User ID: $userId
                                    Source: $source (Type: $type)
                                    Previous Coins: $previousCoins
                                    Reward Amount: $amount
                                    Transaction Result: SUCCESS
                                    Firebase Coins: $newCoins
                                    Displayed UI Coins (Before local save): $displayedUiCoinsBefore
                                    ========================================================
                                    """.trimIndent()
                                )

                                // Generate unique keys
                                val txId = db.getReference("transactions/$userId").push().key ?: "tx_${System.currentTimeMillis()}"
                                val historyId = db.getReference("rewardHistory/$userId").push().key ?: "hist_${System.currentTimeMillis()}"

                                // Write transaction history
                                val txMap = mapOf(
                                    "id" to txId,
                                    "userId" to userId,
                                    "type" to type,
                                    "title" to source,
                                    "coins" to amount,
                                    "status" to "Completed",
                                    "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP,
                                    "amount" to amount,
                                    "source" to source,
                                    "coinsBefore" to previousCoins,
                                    "coinsAfter" to newCoins
                                )
                                db.getReference("transactions/$userId/$txId").setValue(txMap)

                                // Write reward history
                                val historyMap = mapOf(
                                    "type" to type,
                                    "reward" to amount,
                                    "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP,
                                    "coinsBefore" to previousCoins,
                                    "coinsAfter" to newCoins
                                )
                                db.getReference("rewardHistory/$userId/$historyId").setValue(historyMap)

                                // Update local Room state only after Firebase transaction succeeds
                                val currentWallet = walletState.value
                                if (currentWallet.userId == userId) {
                                    val updatedWallet = currentWallet.copy(
                                        coins = newCoins,
                                        dailyStreak = currentData?.child("dailyStreak")?.getValue(Int::class.java) ?: currentWallet.dailyStreak,
                                        lastCheckInTime = currentData?.child("lastCheckInTime")?.getValue(Long::class.java) ?: currentWallet.lastCheckInTime,
                                        dailyAdsWatched = currentData?.child("dailyAdsWatched")?.getValue(Int::class.java) ?: currentWallet.dailyAdsWatched,
                                        lastAdResetTime = currentData?.child("lastAdResetTime")?.getValue(Long::class.java) ?: currentWallet.lastAdResetTime,
                                        lastRewardAdTime = currentData?.child("lastRewardAdTime")?.getValue(Long::class.java) ?: currentWallet.lastRewardAdTime,
                                        remainingSpins = currentData?.child("remainingSpins")?.getValue(Int::class.java) ?: currentWallet.remainingSpins,
                                        remainingScratchCards = currentData?.child("remainingScratchCards")?.getValue(Int::class.java) ?: currentWallet.remainingScratchCards,
                                        lastScratchResetTime = currentData?.child("lastScratchResetTime")?.getValue(Long::class.java) ?: currentWallet.lastScratchResetTime,
                                        totalSpinRewards = currentData?.child("totalSpinRewards")?.getValue(Int::class.java) ?: currentWallet.totalSpinRewards,
                                        totalScratchRewards = currentData?.child("totalScratchRewards")?.getValue(Int::class.java) ?: currentWallet.totalScratchRewards,
                                        lastSpinDate = currentData?.child("lastSpinDate")?.getValue(String::class.java) ?: currentWallet.lastSpinDate,
                                        freeSpinUsed = currentData?.child("freeSpinUsed")?.getValue(Boolean::class.java) ?: currentWallet.freeSpinUsed,
                                        rewardAdSpinUsed = currentData?.child("rewardAdSpinUsed")?.getValue(Boolean::class.java) ?: currentWallet.rewardAdSpinUsed
                                    )
                                    viewModelScope.launch {
                                        repository.saveWalletLocally(updatedWallet)
                                    }
                                }

                                onComplete(true, previousCoins, newCoins, null)
                            } else {
                                val finalError = errorMessage ?: error?.message ?: "Transaction aborted"
                                android.util.Log.e(
                                    "PlayWinCoinSystem",
                                    """
                                    ================ COIN SYNC DEBUG REPORT ================
                                    User ID: $userId
                                    Source: $source (Type: $type)
                                    Previous Coins: $previousCoins
                                    Reward Amount: $amount
                                    Transaction Result: FAILURE ($finalError)
                                    Firebase Coins: $previousCoins
                                    Displayed UI Coins: ${walletState.value.coins}
                                    ========================================================
                                    """.trimIndent()
                                )
                                onComplete(false, 0, 0, finalError)
                            }
                        }
                    })
                } catch (e: Exception) {
                    android.util.Log.e("PlayWinCoinSystem", "[EXCEPTION] Failed in reward transaction", e)
                    onComplete(false, 0, 0, e.message ?: "Unknown exception occurred")
                }
            }
        }
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
        val calendar = java.util.Calendar.getInstance()
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

    fun getLocalDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
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
            if (progress.lastQuizDate == todayDate && progress.dailyQuizCompleted) {
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
        onComplete: (Int) -> Unit
    ) {
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        val progress = quizProgressState.value ?: com.playwin.app.data.model.FirebaseQuizProgress()
        val isAlreadyCompleted = progress.completedQuizIds.contains(quizSetId)

        val totalReward = if (isAlreadyCompleted) {
            0
        } else {
            (score * rewardCoinsPerCorrect) + (if (score == totalQuestions) completionBonus else 0)
        }

        executeRewardTransaction(
            userId = currentUserId,
            amount = totalReward,
            type = "quiz_reward",
            source = "Quiz Completed: $category ($score/$totalQuestions)",
            extraCheck = if (isAlreadyCompleted) null else { mutableData ->
                val progressNode = mutableData.child("quizProgress")
                val completedQuizIds = progressNode.child("completedQuizIds").children.mapNotNull { it.getValue(String::class.java) }
                if (completedQuizIds.contains(quizSetId)) {
                    "Quiz already completed."
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
            },
            onComplete = { success, _, _, _ ->
                if (success) {
                    // Check if this user was referred and has a pending referral reward
                    val friendUid = currentUserId
                    viewModelScope.launch {
                        try {
                            val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                            val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                            
                            val friendRef = db.getReference("users/$friendUid")
                            friendRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                                var referredByUid = ""
                                var friendName = ""
                                var friendEmail = ""
                                var friendCoinsBefore = 0
                                var isAlreadyRewarded = false

                                override fun doTransaction(mutableData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                                    if (mutableData.value == null) {
                                        return com.google.firebase.database.Transaction.success(mutableData)
                                    }

                                    referredByUid = mutableData.child("referredBy").getValue(String::class.java) ?: ""
                                    val status = mutableData.child("referralStatus").getValue(String::class.java) ?: ""
                                    friendName = mutableData.child("displayName").getValue(String::class.java) ?: "Friend"
                                    friendEmail = mutableData.child("email").getValue(String::class.java) ?: ""

                                    if (referredByUid.isEmpty()) {
                                        return com.google.firebase.database.Transaction.abort()
                                    }

                                    if (status == "Rewarded") {
                                        isAlreadyRewarded = true
                                        return com.google.firebase.database.Transaction.abort()
                                    }

                                    // Update status to Rewarded and award 100 coins
                                    mutableData.child("referralStatus").value = "Rewarded"
                                    friendCoinsBefore = mutableData.child("coins").getValue(Int::class.java) ?: 0
                                    mutableData.child("coins").value = friendCoinsBefore + 100
                                    mutableData.child("lastActiveTime").value = System.currentTimeMillis()

                                    return com.google.firebase.database.Transaction.success(mutableData)
                                }

                                override fun onComplete(
                                    error: com.google.firebase.database.DatabaseError?,
                                    committed: Boolean,
                                    currentData: com.google.firebase.database.DataSnapshot?
                                ) {
                                    if (committed && error == null && referredByUid.isNotEmpty()) {
                                        android.util.Log.d("PlayWinReferral", "Friend transaction succeeded. Now rewarding referrer $referredByUid")
                                        
                                        // Update friend's local wallet and save local transaction record
                                        viewModelScope.launch {
                                            val updatedWallet = walletState.value.copy(
                                                coins = walletState.value.coins + 100
                                            )
                                            repository.saveWalletLocally(updatedWallet)
                                            repository.addTransaction(friendUid, 100, "Referral Reward (Completed First Quiz)")
                                        }

                                        // Record friend transaction in Firebase
                                        val friendTxId = db.getReference("transactions/$friendUid").push().key ?: "tx_${System.currentTimeMillis()}"
                                        val friendTxMap = mapOf(
                                            "id" to friendTxId,
                                            "userId" to friendUid,
                                            "type" to "referral_reward",
                                            "title" to "Referral Reward (Completed First Quiz)",
                                            "coins" to 100,
                                            "status" to "Completed",
                                            "timestamp" to System.currentTimeMillis(),
                                            "amount" to 100,
                                            "source" to "First Quiz Referral Reward",
                                            "coinsBefore" to friendCoinsBefore,
                                            "coinsAfter" to friendCoinsBefore + 100
                                        )
                                        db.getReference("transactions/$friendUid/$friendTxId").setValue(friendTxMap)

                                        // Now we perform the transaction on the referrer to add 500 coins and increment/decrement metrics
                                        val referrerRef = db.getReference("users/$referredByUid")
                                        referrerRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                                            var referrerCoinsBefore = 0

                                            override fun doTransaction(mutableDataReferrer: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                                                if (mutableDataReferrer.value == null) {
                                                    return com.google.firebase.database.Transaction.success(mutableDataReferrer)
                                                }

                                                referrerCoinsBefore = mutableDataReferrer.child("coins").getValue(Int::class.java) ?: 0
                                                val currentPending = mutableDataReferrer.child("pendingRewards").getValue(Int::class.java) ?: 0
                                                val totalRefs = mutableDataReferrer.child("totalReferrals").getValue(Int::class.java) ?: 0
                                                val coinsEarned = mutableDataReferrer.child("referralsCoinsEarned").getValue(Int::class.java) ?: 0

                                                mutableDataReferrer.child("coins").value = referrerCoinsBefore + 500
                                                mutableDataReferrer.child("pendingRewards").value = if (currentPending > 0) currentPending - 1 else 0
                                                mutableDataReferrer.child("totalReferrals").value = totalRefs + 1
                                                mutableDataReferrer.child("referralsCoinsEarned").value = coinsEarned + 500
                                                mutableDataReferrer.child("lastActiveTime").value = System.currentTimeMillis()

                                                return com.google.firebase.database.Transaction.success(mutableDataReferrer)
                                            }

                                            override fun onComplete(
                                                errReferrer: com.google.firebase.database.DatabaseError?,
                                                committedReferrer: Boolean,
                                                currentDataReferrer: com.google.firebase.database.DataSnapshot?
                                            ) {
                                                if (committedReferrer && errReferrer == null) {
                                                    android.util.Log.d("PlayWinReferral", "Referrer transaction succeeded. Updating referral logs.")
                                                    
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

                                                    // Record referrer transaction logs in Firebase
                                                    val referrerTxId = db.getReference("transactions/$referredByUid").push().key ?: "tx_${System.currentTimeMillis()}"
                                                    val referrerTxMap = mapOf(
                                                        "id" to referrerTxId,
                                                        "userId" to referredByUid,
                                                        "type" to "referral_bonus",
                                                        "title" to "Referral Bonus: $friendName Completed Quiz",
                                                        "coins" to 500,
                                                        "status" to "Completed",
                                                        "timestamp" to now,
                                                        "amount" to 500,
                                                        "source" to "Referral Bonus",
                                                        "coinsBefore" to referrerCoinsBefore,
                                                        "coinsAfter" to referrerCoinsBefore + 500
                                                    )
                                                    db.getReference("transactions/$referredByUid/$referrerTxId").setValue(referrerTxMap)
                                                } else {
                                                    android.util.Log.e("PlayWinReferral", "Referrer transaction failed", errReferrer?.toException())
                                                }
                                            }
                                        })
                                    } else {
                                        android.util.Log.e("PlayWinReferral", "Friend transaction failed or already rewarded: isAlreadyRewarded=$isAlreadyRewarded", error?.toException())
                                    }
                                }
                            })
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

    fun claimDailyReward(onResult: ((Boolean, String?) -> Unit)? = null): Boolean {
        val currentWallet = walletState.value
        val now = System.currentTimeMillis()
        val userId = currentWallet.userId
        if (userId.isEmpty()) {
            onResult?.invoke(false, "User not authenticated")
            return false
        }

        val isTodayClaimed = currentWallet.lastCheckInTime != 0L && isSameDay(currentWallet.lastCheckInTime, now)
        if (isTodayClaimed) {
            onResult?.invoke(false, "Daily reward already claimed today.")
            return false
        }

        viewModelScope.launch {
            rewardMutex.withLock {
                try {
                    val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                    val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                    val userRef = db.getReference("users").child(userId)

                    android.util.Log.d("PlayWinCoinSystem", "[START] Daily Check-in Transaction for user: $userId")

                    userRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                        var previousCoins = 0
                        var newCoins = 0
                        var txRewardAmount = 0
                        var txNewStreak = 0
                        var errorMessage: String? = null

                        override fun doTransaction(mutableData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                            if (mutableData.value == null) {
                                errorMessage = "User node does not exist in Firebase."
                                return com.google.firebase.database.Transaction.abort()
                            }

                            val lastCheckIn = mutableData.child("lastCheckInTime").getValue(Long::class.java) ?: 0L
                            if (isSameDay(lastCheckIn, now)) {
                                errorMessage = "Daily reward already claimed today."
                                return com.google.firebase.database.Transaction.abort()
                            }

                            val currentStreak = mutableData.child("dailyStreak").getValue(Int::class.java)
                                ?: mutableData.child("streak").getValue(Int::class.java)
                                ?: 0

                            txNewStreak = when {
                                lastCheckIn == 0L -> 1
                                isYesterday(lastCheckIn, now) -> {
                                    if (currentStreak >= 7) 1 else currentStreak + 1
                                }
                                else -> 1
                            }

                            txRewardAmount = when (txNewStreak) {
                                1 -> 20
                                2 -> 30
                                3 -> 40
                                4 -> 50
                                5 -> 60
                                6 -> 80
                                7 -> 120
                                else -> 20
                            }

                            previousCoins = mutableData.child("coins").getValue(Int::class.java) ?: 0
                            newCoins = previousCoins + txRewardAmount

                            val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            val checkInDateStr = sdfDate.format(java.util.Date(now))

                            mutableData.child("coins").value = newCoins
                            mutableData.child("lastActiveTime").value = now
                            mutableData.child("lastCheckInTime").value = now
                            mutableData.child("lastCheckInDate").value = checkInDateStr
                            mutableData.child("dailyStreak").value = txNewStreak
                            mutableData.child("streak").value = txNewStreak

                            val prevTotalRewards = mutableData.child("totalCheckInRewards").getValue(Int::class.java) ?: 0
                            mutableData.child("totalCheckInRewards").value = prevTotalRewards + txRewardAmount

                            return com.google.firebase.database.Transaction.success(mutableData)
                        }

                        override fun onComplete(
                            error: com.google.firebase.database.DatabaseError?,
                            committed: Boolean,
                            currentData: com.google.firebase.database.DataSnapshot?
                        ) {
                            if (committed && error == null) {
                                val displayedUiCoinsBefore = walletState.value.coins
                                android.util.Log.d(
                                    "PlayWinCoinSystem",
                                    """
                                    ================ DAILY CHECKIN SYNC SUCCESS ================
                                    User ID: $userId
                                    New Streak: $txNewStreak
                                    Reward Amount: $txRewardAmount
                                    Previous Coins: $previousCoins
                                    Firebase Coins: $newCoins
                                    Displayed UI Coins (Before local save): $displayedUiCoinsBefore
                                    ============================================================
                                    """.trimIndent()
                                )

                                val txId = db.getReference("transactions/$userId").push().key ?: "tx_${System.currentTimeMillis()}"
                                val historyId = db.getReference("rewardHistory/$userId").push().key ?: "hist_${System.currentTimeMillis()}"

                                val txMap = mapOf(
                                    "id" to txId,
                                    "userId" to userId,
                                    "type" to "daily_reward",
                                    "title" to "Daily Check-in Reward",
                                    "coins" to txRewardAmount,
                                    "status" to "Completed",
                                    "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP,
                                    "amount" to txRewardAmount,
                                    "source" to "Daily Check-in Reward",
                                    "coinsBefore" to previousCoins,
                                    "coinsAfter" to newCoins
                                )
                                db.getReference("transactions/$userId/$txId").setValue(txMap)

                                val historyMap = mapOf(
                                    "type" to "daily_reward",
                                    "reward" to txRewardAmount,
                                    "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP,
                                    "coinsBefore" to previousCoins,
                                    "coinsAfter" to newCoins
                                )
                                db.getReference("rewardHistory/$userId/$historyId").setValue(historyMap)

                                val currentWallet = walletState.value
                                if (currentWallet.userId == userId) {
                                    val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                    val checkInDateStr = sdfDate.format(java.util.Date(now))
                                    val updatedWallet = currentWallet.copy(
                                        coins = newCoins,
                                        dailyStreak = txNewStreak,
                                        lastCheckInTime = now,
                                        lastCheckInDate = checkInDateStr,
                                        totalCheckInRewards = (currentWallet.totalCheckInRewards + txRewardAmount)
                                    )
                                    viewModelScope.launch {
                                        repository.saveWalletLocally(updatedWallet)
                                    }
                                }

                                onResult?.invoke(true, null)
                            } else {
                                val finalError = errorMessage ?: error?.message ?: "Transaction aborted"
                                android.util.Log.e(
                                    "PlayWinCoinSystem",
                                    """
                                    ================ DAILY CHECKIN SYNC FAILURE ================
                                    User ID: $userId
                                    Reason: $finalError
                                    ============================================================
                                    """.trimIndent()
                                )
                                onResult?.invoke(false, finalError)
                            }
                        }
                    })
                } catch (e: Exception) {
                    android.util.Log.e("PlayWinCoinSystem", "Daily Check-in failed with exception: ${e.message}", e)
                    onResult?.invoke(false, e.message)
                }
            }
        }

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
                        val userRef = db.getReference("users").child(userId)
                        if (userRef == null) {
                            onResult(false, "Database reference is null.")
                            return@launch
                        }

                        android.util.Log.d("PlayWinCoinSystem", "[START] Rewarded Ad Atomic Transaction for user: $userId, type: $type")

                        val txId = db.getReference("transactions/$userId").push().key ?: "tx_${System.currentTimeMillis()}"
                        val historyId = db.getReference("rewardHistory/$userId").push().key ?: "hist_${System.currentTimeMillis()}"

                        userRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                            var previousCoins = 0
                            var newCoins = 0
                            var dbAdsWatched = 0
                            var activeResetTime = 0L
                            val rewardAmount = 50
                            var transactionError: String? = null

                            override fun doTransaction(mutableData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                                val dbLastAdTime = mutableData.child("lastRewardAdTime").getValue(Long::class.java) ?: 0L
                                val txNow = System.currentTimeMillis()
                                val dbElapsed = txNow - dbLastAdTime
                                if (dbLastAdTime != 0L && dbElapsed < 1 * 60 * 1000L) {
                                    transactionError = "Ad reward cooldown is still active in database."
                                    return com.google.firebase.database.Transaction.abort()
                                }

                                val dbResetTime = mutableData.child("lastAdResetTime").getValue(Long::class.java) ?: 0L
                                dbAdsWatched = mutableData.child("dailyAdsWatched").getValue(Int::class.java) ?: 0
                                activeResetTime = dbResetTime
                                if (!isSameDay(dbResetTime, txNow) || dbResetTime == 0L) {
                                    activeResetTime = txNow
                                    dbAdsWatched = 0
                                }

                                if (dbAdsWatched >= 10) {
                                    transactionError = "Daily Reward Ad Limit Reached. Come Back Tomorrow."
                                    return com.google.firebase.database.Transaction.abort()
                                }

                                previousCoins = mutableData.child("coins").getValue(Int::class.java) ?: 0
                                newCoins = previousCoins + rewardAmount

                                mutableData.child("coins").value = newCoins
                                mutableData.child("dailyAdsWatched").value = dbAdsWatched + 1
                                mutableData.child("lastAdResetTime").value = activeResetTime
                                mutableData.child("lastRewardAdTime").value = txNow
                                mutableData.child("lastActiveTime").value = txNow

                                return com.google.firebase.database.Transaction.success(mutableData)
                            }

                            override fun onComplete(
                                error: com.google.firebase.database.DatabaseError?,
                                committed: Boolean,
                                currentData: com.google.firebase.database.DataSnapshot?
                            ) {
                                if (committed && error == null) {
                                    val txNow = System.currentTimeMillis()
                                    val displayedUiCoinsBefore = walletState.value.coins
                                    android.util.Log.d(
                                        "PlayWinCoinSystem",
                                        """
                                        ================ REWARDED AD SYNC SUCCESS ================
                                        User ID: $userId
                                        Reward Amount: $rewardAmount
                                        Previous Coins: $previousCoins
                                        Firebase Coins: $newCoins
                                        Displayed UI Coins (Before local save): $displayedUiCoinsBefore
                                        ==========================================================
                                        """.trimIndent()
                                    )

                                    val txMap = mapOf(
                                        "id" to txId,
                                        "userId" to userId,
                                        "type" to "video_ad",
                                        "title" to "Watch Ads Reward",
                                        "coins" to rewardAmount,
                                        "status" to "Completed",
                                        "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP,
                                        "amount" to rewardAmount,
                                        "source" to "Watch Ads Reward",
                                        "coinsBefore" to previousCoins,
                                        "coinsAfter" to newCoins
                                    )
                                    db.getReference("transactions/$userId/$txId").setValue(txMap)

                                    val historyMap = mapOf(
                                        "type" to "video_ad",
                                        "reward" to rewardAmount,
                                        "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP,
                                        "coinsBefore" to previousCoins,
                                        "coinsAfter" to newCoins
                                    )
                                    db.getReference("rewardHistory/$userId/$historyId").setValue(historyMap)

                                    val walletSummaryMap = mapOf(
                                        "userId" to userId,
                                        "coins" to newCoins,
                                        "dailyAdsWatched" to (dbAdsWatched + 1),
                                        "lastRewardAdTime" to txNow,
                                        "lastAdResetTime" to activeResetTime
                                    )
                                    db.getReference("walletSummary/$userId").setValue(walletSummaryMap)

                                    val currentWallet = walletState.value
                                    if (currentWallet.userId == userId) {
                                        val updatedWallet = currentWallet.copy(
                                            coins = newCoins,
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
                                    val reason = error?.message ?: transactionError ?: "Transaction aborted."
                                    android.util.Log.e("PlayWinCoinSystem", "[FAILED] Rewarded Ad Transaction failed: $reason")
                                    onResult(false, reason)
                                }
                            }
                        })
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
                val dbUser = repository.getFirebaseUser(userId)
                if (dbUser != null) {
                    val currentTime = System.currentTimeMillis()
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    var lastSpinDateToSync = dbUser.lastSpinDate
                    var remainingSpinsToSync = dbUser.remainingSpins
                    var freeSpinUsedToSync = dbUser.freeSpinUsed
                    var rewardAdSpinUsedToSync = dbUser.rewardAdSpinUsed

                    if (lastSpinDateToSync != today) {
                        lastSpinDateToSync = today
                        remainingSpinsToSync = 1
                        freeSpinUsedToSync = false
                        rewardAdSpinUsedToSync = false
                        try {
                            val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                            val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                            val userRef = db.getReference("users/$userId")
                            userRef.child("lastSpinDate").setValue(today)
                            userRef.child("remainingSpins").setValue(1)
                            userRef.child("freeSpinUsed").setValue(false)
                            userRef.child("rewardAdSpinUsed").setValue(false)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }

                    var lastScratchDateToSync = dbUser.lastScratchDate
                    var remainingScratchCardsToSync = dbUser.remainingScratchCards
                    var freeScratchUsedToSync = dbUser.freeScratchUsed
                    var rewardAdScratchUsedToSync = dbUser.rewardAdScratchUsed

                    if (lastScratchDateToSync != today) {
                        lastScratchDateToSync = today
                        remainingScratchCardsToSync = 1
                        freeScratchUsedToSync = false
                        rewardAdScratchUsedToSync = false
                        try {
                            val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                            val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                            val userRef = db.getReference("users/$userId")
                            userRef.child("lastScratchDate").setValue(today)
                            userRef.child("remainingScratchCards").setValue(1)
                            userRef.child("freeScratchUsed").setValue(false)
                            userRef.child("rewardAdScratchUsed").setValue(false)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }

                    var lastAdResetTimeToSync = dbUser.lastAdResetTime
                    var dailyAdsWatchedToSync = dbUser.dailyAdsWatched

                    if (!isSameDay(lastAdResetTimeToSync, currentTime) || lastAdResetTimeToSync == 0L) {
                        lastAdResetTimeToSync = currentTime
                        dailyAdsWatchedToSync = 0
                        try {
                            val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
                            val db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                            val userRef = db.getReference("users/$userId")
                            userRef.child("lastAdResetTime").setValue(currentTime)
                            userRef.child("dailyAdsWatched").setValue(0)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }

                    val syncWallet = currentWallet.copy(
                        coins = dbUser.coins,
                        dailyStreak = dbUser.streak,
                        lastCheckInTime = dbUser.lastCheckInTime,
                        dailyAdsWatched = dailyAdsWatchedToSync,
                        lastAdResetTime = lastAdResetTimeToSync,
                        referredBy = dbUser.referredBy,
                        hasUsedReferralCode = dbUser.hasUsedReferralCode,
                        totalReferrals = dbUser.totalReferrals,
                        remainingSpins = remainingSpinsToSync,
                        totalSpinRewards = dbUser.totalSpinRewards,
                        remainingScratchCards = remainingScratchCardsToSync,
                        lastScratchResetTime = dbUser.lastScratchResetTime,
                        totalScratchRewards = dbUser.totalScratchRewards,
                        lastSpinDate = lastSpinDateToSync,
                        freeSpinUsed = freeSpinUsedToSync,
                        rewardAdSpinUsed = rewardAdSpinUsedToSync,
                        lastScratchDate = lastScratchDateToSync,
                        freeScratchUsed = freeScratchUsedToSync,
                        rewardAdScratchUsed = rewardAdScratchUsedToSync,
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

                        val claimed = mutableData.child("hasUsedReferralCode").getValue(Boolean::class.java) ?: false
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

    fun rollSpinReward(): SpinRewardOption {
        val roll = kotlin.random.Random.nextInt(100) // 0..99
        return when {
            roll < 35 -> SpinRewardOption(5, "+5 Coins", index = 0)
            roll < 60 -> SpinRewardOption(10, "+10 Coins", index = 1)
            roll < 80 -> SpinRewardOption(20, "+20 Coins", index = 2)
            roll < 90 -> SpinRewardOption(30, "+30 Coins", index = 3)
            roll < 97 -> SpinRewardOption(50, "+50 Coins", index = 4)
            roll < 99 -> SpinRewardOption(100, "+100 Coins", index = 5)
            else -> SpinRewardOption(200, "+200 Coins", index = 6)
        }
    }

    fun performSpinWheelTransaction(rewardIndex: Int, isAdSpin: Boolean, onResult: (Boolean, String?) -> Unit) {
        val userId = walletState.value.userId
        if (userId.isEmpty()) {
            onResult(false, "User not authenticated.")
            return
        }

        val sectorsCount = 7
        if (rewardIndex < 0 || rewardIndex >= sectorsCount) {
            onResult(false, "Invalid reward option.")
            return
        }

        val rewardRoll = when (rewardIndex) {
            0 -> SpinRewardOption(5, "+5 Coins", index = 0)
            1 -> SpinRewardOption(10, "+10 Coins", index = 1)
            2 -> SpinRewardOption(20, "+20 Coins", index = 2)
            3 -> SpinRewardOption(30, "+30 Coins", index = 3)
            4 -> SpinRewardOption(50, "+50 Coins", index = 4)
            5 -> SpinRewardOption(100, "+100 Coins", index = 5)
            6 -> SpinRewardOption(200, "+200 Coins", index = 6)
            else -> SpinRewardOption(5, "+5 Coins", index = 0)
        }

        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        executeRewardTransaction(
            userId = userId,
            amount = rewardRoll.amount,
            type = "spin_reward",
            source = "Spin Wheel Reward",
            extraCheck = { mutableData ->
                val lastSpinDate = mutableData.child("lastSpinDate").getValue(String::class.java) ?: ""
                var freeSpinUsed = mutableData.child("freeSpinUsed").getValue(Boolean::class.java) ?: false
                var rewardAdSpinUsed = mutableData.child("rewardAdSpinUsed").getValue(Boolean::class.java) ?: false
                var remainingSpins = mutableData.child("remainingSpins").getValue(Int::class.java) ?: 0

                var checkLastSpinDate = lastSpinDate
                var checkFreeSpinUsed = freeSpinUsed
                var checkRewardAdSpinUsed = rewardAdSpinUsed
                var checkRemainingSpins = remainingSpins

                if (checkLastSpinDate != today) {
                    checkLastSpinDate = today
                    checkFreeSpinUsed = false
                    checkRewardAdSpinUsed = false
                    checkRemainingSpins = 1
                }

                if (isAdSpin) {
                    if (!checkFreeSpinUsed) {
                        "Please use your free spin first."
                    } else if (!checkRewardAdSpinUsed) {
                        "Please watch a rewarded ad to unlock the extra spin."
                    } else if (checkRemainingSpins <= 0) {
                        "No extra spin available. Daily limit reached."
                    } else {
                        null
                    }
                } else {
                    if (checkFreeSpinUsed) {
                        "Free spin already used today."
                    } else if (checkRemainingSpins <= 0) {
                        "No free spin available."
                    } else {
                        null
                    }
                }
            },
            extraUpdate = { mutableData ->
                val lastSpinDate = mutableData.child("lastSpinDate").getValue(String::class.java) ?: ""
                var freeSpinUsed = mutableData.child("freeSpinUsed").getValue(Boolean::class.java) ?: false
                var rewardAdSpinUsed = mutableData.child("rewardAdSpinUsed").getValue(Boolean::class.java) ?: false
                var remainingSpins = mutableData.child("remainingSpins").getValue(Int::class.java) ?: 0

                if (lastSpinDate != today) {
                    freeSpinUsed = false
                    rewardAdSpinUsed = false
                    remainingSpins = 1
                }

                if (isAdSpin) {
                    remainingSpins = 0
                } else {
                    freeSpinUsed = true
                    remainingSpins = 0
                }

                mutableData.child("lastSpinDate").value = today
                mutableData.child("freeSpinUsed").value = freeSpinUsed
                mutableData.child("rewardAdSpinUsed").value = rewardAdSpinUsed
                mutableData.child("remainingSpins").value = remainingSpins

                val totalRewards = mutableData.child("totalSpinRewards").getValue(Int::class.java) ?: 0
                mutableData.child("totalSpinRewards").value = totalRewards + rewardRoll.amount
            },
            onComplete = { success, _, _, errorMsg ->
                onResult(success, errorMsg)
            }
        )
    }

    fun unlockAdSpin(onResult: (Boolean, String?) -> Unit) {
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

                    var lastSpinDate = mutableData.child("lastSpinDate").getValue(String::class.java) ?: ""
                    var freeSpinUsed = mutableData.child("freeSpinUsed").getValue(Boolean::class.java) ?: false
                    var rewardAdSpinUsed = mutableData.child("rewardAdSpinUsed").getValue(Boolean::class.java) ?: false
                    var remainingSpins = mutableData.child("remainingSpins").getValue(Int::class.java) ?: 0

                    if (lastSpinDate != today) {
                        lastSpinDate = today
                        freeSpinUsed = false
                        rewardAdSpinUsed = false
                        remainingSpins = 1
                    }

                    if (!freeSpinUsed || rewardAdSpinUsed) {
                        return com.google.firebase.database.Transaction.abort()
                    }

                    rewardAdSpinUsed = true
                    remainingSpins = 1

                    mutableData.child("lastSpinDate").value = today
                    mutableData.child("freeSpinUsed").value = freeSpinUsed
                    mutableData.child("rewardAdSpinUsed").value = rewardAdSpinUsed
                    mutableData.child("remainingSpins").value = remainingSpins

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
                val lastScratchDate = mutableData.child("lastScratchDate").getValue(String::class.java) ?: ""
                var freeScratchUsed = mutableData.child("freeScratchUsed").getValue(Boolean::class.java) ?: false
                var rewardAdScratchUsed = mutableData.child("rewardAdScratchUsed").getValue(Boolean::class.java) ?: false
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
                val lastScratchDate = mutableData.child("lastScratchDate").getValue(String::class.java) ?: ""
                var freeScratchUsed = mutableData.child("freeScratchUsed").getValue(Boolean::class.java) ?: false
                var rewardAdScratchUsed = mutableData.child("rewardAdScratchUsed").getValue(Boolean::class.java) ?: false
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

                    var lastScratchDate = mutableData.child("lastScratchDate").getValue(String::class.java) ?: ""
                    var freeScratchUsed = mutableData.child("freeScratchUsed").getValue(Boolean::class.java) ?: false
                    var rewardAdScratchUsed = mutableData.child("rewardAdScratchUsed").getValue(Boolean::class.java) ?: false
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
                    currentUserBlockedState.value = dbUser.isBlocked
                    val currentWallet = walletState.value
                    if (currentWallet.userId == userId || currentWallet.userId.isEmpty()) {
                        val syncWallet = currentWallet.copy(
                            coins = dbUser.coins,
                            dailyStreak = dbUser.streak,
                            lastCheckInTime = dbUser.lastCheckInTime,
                            dailyAdsWatched = dbUser.dailyAdsWatched,
                            lastAdResetTime = dbUser.lastAdResetTime,
                            referredBy = dbUser.referredBy,
                            hasUsedReferralCode = dbUser.hasUsedReferralCode,
                            totalReferrals = dbUser.totalReferrals,
                            remainingSpins = dbUser.remainingSpins,
                            totalSpinRewards = dbUser.totalSpinRewards,
                            remainingScratchCards = dbUser.remainingScratchCards,
                            lastScratchResetTime = dbUser.lastScratchResetTime,
                            totalScratchRewards = dbUser.totalScratchRewards,
                            lastSpinDate = dbUser.lastSpinDate,
                            freeSpinUsed = dbUser.freeSpinUsed,
                            rewardAdSpinUsed = dbUser.rewardAdSpinUsed,
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

        firebaseRedemptionJob = viewModelScope.launch {
            repository.getFirebaseRedemptionsFlow(userId).collect { redList ->
                redemptionsState.value = redList
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
    }

    private fun stopFirebaseSync() {
        firebaseUserJob?.cancel()
        firebaseUserJob = null
        firebaseTxJob?.cancel()
        firebaseTxJob = null
        firebaseRedemptionJob?.cancel()
        firebaseRedemptionJob = null
        withdrawRequestsJob?.cancel()
        withdrawRequestsJob = null
        allUsersJob?.cancel()
        allUsersJob = null
        couponRedemptionsJob?.cancel()
        couponRedemptionsJob = null
        firebaseQuizProgressJob?.cancel()
        firebaseQuizProgressJob = null
        referralsHistoryJob?.cancel()
        referralsHistoryJob = null
        quizzesJob?.cancel()
        quizzesJob = null
        quizProgressState.value = null
        referralHistoryState.value = emptyList()
        currentUserState.value = null
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
}
