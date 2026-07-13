package com.myplaywin.app.data.repository

import com.myplaywin.app.data.database.PlayWinDao
import com.myplaywin.app.data.model.FirebaseCoupon
import com.myplaywin.app.data.model.FirebaseTask
import com.myplaywin.app.data.model.FirebaseTransaction
import com.myplaywin.app.data.model.RewardTransaction
import com.myplaywin.app.data.model.UserWallet
import kotlinx.coroutines.flow.Flow

class PlayWinRepository(private val dao: PlayWinDao) {
    val userWallet: Flow<UserWallet?> = dao.getUserWallet()
    val transactions: Flow<List<RewardTransaction>> = dao.getTransactions()

    private val firebaseDbManager = FirebaseDbManager()

    val firebaseTasksFlow: Flow<List<FirebaseTask>> = firebaseDbManager.observeTasks()
    val firebaseCouponsFlow: Flow<List<FirebaseCoupon>> = firebaseDbManager.observeCoupons()
    val firebaseSpinRewardsFlow: Flow<List<com.myplaywin.app.data.model.FirebaseSpinReward>> = firebaseDbManager.observeSpinRewards()
    val firebaseSpinWheelConfigFlow: Flow<com.myplaywin.app.data.model.FirebaseSpinWheelConfig> = firebaseDbManager.observeSpinWheelConfig()
    val firebaseScratchCardSettingsFlow: Flow<com.myplaywin.app.data.model.FirebaseScratchCardSettings> = firebaseDbManager.observeScratchCardSettings()
    val firebaseScratchCardRewardsFlow: Flow<List<com.myplaywin.app.data.model.FirebaseScratchCardReward>> = firebaseDbManager.observeScratchCardRewards()
    val firebaseWatchAdsConfigFlow: Flow<com.myplaywin.app.data.model.FirebaseWatchAdsConfig> = firebaseDbManager.observeWatchAdsConfig()

    fun getFirebaseUserRewardAdsFlow(userId: String): Flow<com.myplaywin.app.data.model.FirebaseUserRewardAds> {
        return firebaseDbManager.observeUserRewardAds(userId)
    }

    fun getFirebaseUserScratchCardStateFlow(userId: String): Flow<com.myplaywin.app.data.model.FirebaseUserScratchCardState> {
        return firebaseDbManager.observeUserScratchCardState(userId)
    }

    fun performScratchCardDbTransaction(
        userId: String,
        reward: com.myplaywin.app.data.model.FirebaseScratchCardReward,
        transactionId: String,
        isAdScratch: Boolean,
        onComplete: (Boolean, String?, Int, Int) -> Unit
    ) {
        firebaseDbManager.performScratchCardDbTransaction(userId, reward, transactionId, isAdScratch, onComplete)
    }

    fun getFirebaseTransactionsFlow(userId: String): Flow<List<FirebaseTransaction>> {
        return firebaseDbManager.observeTransactions(userId)
    }

    fun getFirebaseUserFlow(userId: String): Flow<com.myplaywin.app.data.model.FirebaseUser?> {
        return firebaseDbManager.observeFirebaseUser(userId)
    }

    fun getFirebaseRedemptionsFlow(userId: String): Flow<List<com.myplaywin.app.data.model.FirebaseRedemption>> {
        return firebaseDbManager.observeRedemptions(userId)
    }

    fun addFirebaseRedemption(userId: String, redemption: com.myplaywin.app.data.model.FirebaseRedemption) {
        firebaseDbManager.addFirebaseRedemption(userId, redemption)
    }



    fun getFirebaseCouponRedemptionsFlow(userId: String): Flow<List<com.myplaywin.app.data.model.FirebaseCouponRedemption>> {
        return firebaseDbManager.observeUserCouponRedemptions(userId)
    }

    fun submitCouponRedemptionTransaction(
        redemption: com.myplaywin.app.data.model.FirebaseCouponRedemption,
        couponId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("PlayWin_Repository", "Entering submitCouponRedemptionTransaction:")
        android.util.Log.d("PlayWin_Repository", "  couponId parameter (coupon.id / coupon.couponId): $couponId")
        android.util.Log.d("PlayWin_Repository", "  Firebase node key: $couponId")
        android.util.Log.d("PlayWin_Repository", "  Firebase path used for lookup: /coupons/$couponId")
        firebaseDbManager.submitCouponRedemptionTransaction(redemption, couponId, onSuccess, onError)
    }



    suspend fun saveWallet(wallet: UserWallet) {
        dao.insertOrUpdateWallet(wallet)
        if (wallet.userId.isNotEmpty()) {
            firebaseDbManager.syncUserWalletWallet(wallet)
        }
    }

    suspend fun saveWalletLocally(wallet: UserWallet) {
        dao.insertOrUpdateWallet(wallet)
    }

    private fun FirebaseDbManager.syncUserWalletWallet(wallet: UserWallet) {
        syncUserWallet(
            userId = wallet.userId,
            coins = wallet.coins,
            dailyStreak = wallet.dailyStreak,
            lastCheckInTime = wallet.lastCheckInTime,
            remainingSpins = wallet.remainingSpins,
            totalSpinRewards = wallet.totalSpinRewards,
            remainingScratchCards = wallet.remainingScratchCards,
            lastScratchResetTime = wallet.lastScratchResetTime,
            totalScratchRewards = wallet.totalScratchRewards,
            lastSpinDate = wallet.lastSpinDate,
            freeSpinUsed = wallet.freeSpinUsed,
            rewardAdSpinUsed = wallet.rewardAdSpinUsed,
            dailySpinCount = wallet.dailySpinCount,
            rewardedSpinCount = wallet.rewardedSpinCount,
            lastCheckInDate = wallet.lastCheckInDate,
            totalCheckInRewards = wallet.totalCheckInRewards,
            lastRewardAdTime = wallet.lastRewardAdTime,
            freeScratchUsed = wallet.freeScratchUsed,
            rewardAdScratchUsed = wallet.rewardAdScratchUsed,
            lastScratchDate = wallet.lastScratchDate
        )
    }

    suspend fun addTransaction(userId: String, amount: Int, source: String) {
        val transaction = RewardTransaction(amount = amount, source = source)
        dao.insertTransaction(transaction)
        if (userId.isNotEmpty()) {
            firebaseDbManager.addFirebaseTransaction(userId, amount, source)
        }
    }

    fun addCoinsAtomic(userId: String, amount: Int, source: String, onComplete: (Boolean, Int, Int) -> Unit = { _, _, _ -> }) {
        firebaseDbManager.addCoinsAtomic(userId, amount, source, onComplete)
    }

    fun addReferral(userId: String, referralCode: String) {
        if (userId.isNotEmpty()) {
            firebaseDbManager.addFirebaseReferral(userId, referralCode)
        }
    }

    suspend fun getFirebaseUser(userId: String) = firebaseDbManager.getFirebaseUser(userId)

    fun resetUserDataInFirebase(userId: String) = firebaseDbManager.resetUserDataInFirebase(userId)

    fun saveNewUserInFirebase(user: com.myplaywin.app.data.model.FirebaseUser) = firebaseDbManager.saveNewUserInFirebase(user)

    fun createOriginalUserInFirebase(
        uid: String,
        email: String,
        displayName: String,
        onComplete: (Boolean) -> Unit
    ) = firebaseDbManager.createOriginalUserInFirebase(uid, email, displayName, onComplete)

    fun getFirebaseQuizProgressFlow(userId: String): Flow<com.myplaywin.app.data.model.FirebaseQuizProgress?> {
        return firebaseDbManager.observeQuizProgress(userId)
    }

    fun saveFirebaseQuizProgress(userId: String, progress: com.myplaywin.app.data.model.FirebaseQuizProgress) {
        firebaseDbManager.saveQuizProgress(userId, progress)
    }

    fun getFirebaseCompletedQuizzesFlow(userId: String): Flow<Map<String, com.myplaywin.app.data.model.FirebaseCompletedQuiz>> {
        return firebaseDbManager.observeCompletedQuizzes(userId)
    }

    fun saveFirebaseCompletedQuiz(userId: String, quizId: String, completedQuiz: com.myplaywin.app.data.model.FirebaseCompletedQuiz) {
        firebaseDbManager.saveCompletedQuiz(userId, quizId, completedQuiz)
    }

    fun getFirebaseWeeklyQuizProgressFlow(userId: String): Flow<Map<String, com.myplaywin.app.data.model.FirebaseWeeklyQuizProgress>> {
        return firebaseDbManager.observeWeeklyQuizProgress(userId)
    }

    fun saveFirebaseWeeklyQuizProgress(userId: String, dayOfWeek: String, progress: com.myplaywin.app.data.model.FirebaseWeeklyQuizProgress) {
        firebaseDbManager.saveWeeklyQuizProgress(userId, dayOfWeek, progress)
    }

    fun getQuestionsForCategory(category: String, onResult: (List<com.myplaywin.app.data.model.Quiz>) -> Unit) {
        firebaseDbManager.getQuestionsForCategory(category, onResult)
    }

    suspend fun clearLocalTransactions() {
        dao.clearAllTransactions()
    }

    // --- UPI WITHDRAW & ADMIN PANELS STREAMS ---
    fun getFirebaseWithdrawRequestsFlow(userId: String): Flow<List<com.myplaywin.app.data.model.FirebaseWithdrawRequest>> {
        return firebaseDbManager.observeWithdrawRequests(userId)
    }

    val firebaseAllUsersFlow: Flow<List<com.myplaywin.app.data.model.FirebaseUser>> =
        firebaseDbManager.observeAllUsers()

    fun submitWithdrawRequest(
        uid: String,
        userName: String,
        email: String,
        upiId: String,
        amount: Int,
        coinsSpent: Int,
        onComplete: (Boolean, String?) -> Unit
    ) {
        firebaseDbManager.submitWithdrawRequest(uid, userName, email, upiId, amount, coinsSpent, onComplete)
    }

    fun getFirebaseQuizzesFlow(): Flow<List<com.myplaywin.app.data.model.FirebaseQuiz>> {
        return firebaseDbManager.observeQuizzes()
    }
}
