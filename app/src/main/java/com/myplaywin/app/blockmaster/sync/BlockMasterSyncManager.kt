package com.myplaywin.app.blockmaster.sync

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.myplaywin.app.blockmaster.security.BlockMasterAntiCheat
import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData
import com.myplaywin.app.data.repository.WalletService
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

data class PendingRewardItem(
    val nonce: String,
    val coins: Int,
    val source: String,
    val type: String,
    val timestamp: Long
)

class BlockMasterSyncManager(private val context: Context) {

    private val TAG = "BlockMasterSyncManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val prefs = context.getSharedPreferences("block_master_sync_queue", Context.MODE_PRIVATE)

    init {
        // Attempt initial sync when manager is created
        syncPendingRewards()
    }

    /**
     * Credit coins to PlayWin Wallet with immediate real-time sync or offline queue fallback.
     */
    fun awardCoins(
        coins: Int,
        source: String,
        type: String,
        onComplete: ((Boolean, Int) -> Unit)? = null
    ) {
        if (coins <= 0) {
            onComplete?.invoke(true, 0)
            return
        }

        val nonce = "bm_${System.currentTimeMillis()}_${(1000..9999).random()}"
        if (!BlockMasterAntiCheat.validateAndConsumeNonce(nonce)) {
            Log.e(TAG, "Coin award blocked by anti-cheat nonce check.")
            onComplete?.invoke(false, 0)
            return
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (currentUserId.isEmpty()) {
            // Unauthenticated/Offline guest mode: Save reward to local pending queue
            savePendingReward(PendingRewardItem(nonce, coins, source, type, System.currentTimeMillis()))
            Log.d(TAG, "Saved $coins coins to offline pending reward queue.")
            onComplete?.invoke(true, coins)
            return
        }

        // Authenticated user: Execute atomic transaction on Firebase PlayWin Wallet
        WalletService.updateWallet(
            userId = currentUserId,
            coinsDelta = coins,
            source = source,
            type = type,
            onComplete = { success, _, coinsAfter, error ->
                if (success) {
                    Log.d(TAG, "[SYNC SUCCESS] Updated PlayWin Wallet: +$coins coins. New balance: $coinsAfter")
                    onComplete?.invoke(true, coinsAfter)
                } else {
                    Log.w(TAG, "[SYNC QUEUED] Firebase transaction failed ($error). Queueing locally for retry...")
                    savePendingReward(PendingRewardItem(nonce, coins, source, type, System.currentTimeMillis()))
                    onComplete?.invoke(false, 0)
                }
            }
        )
    }

    /**
     * Flushes local pending reward queue when online connection is restored.
     */
    fun syncPendingRewards() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (currentUserId.isEmpty()) return

        val pending = getPendingRewards()
        if (pending.isEmpty()) return

        Log.d(TAG, "Syncing ${pending.size} pending reward items to PlayWin Wallet...")

        scope.launch {
            val remainingList = ArrayList<PendingRewardItem>()
            for (item in pending) {
                val deferred = CompletableDeferred<Boolean>()
                WalletService.updateWallet(
                    userId = currentUserId,
                    coinsDelta = item.coins,
                    source = item.source,
                    type = item.type,
                    onComplete = { success, _, _, _ ->
                        deferred.complete(success)
                    }
                )
                val success = deferred.await()
                if (!success) {
                    remainingList.add(item)
                } else {
                    Log.d(TAG, "Successfully synced pending item: ${item.source} (+${item.coins} coins)")
                }
            }
            savePendingList(remainingList)
        }
    }

    /**
     * Synchronizes full player statistics with PlayWin profile node under users/{userId}/blockMasterStats.
     */
    fun syncPlayerProfileStats(saveData: BlockMasterSaveData) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (userId.isEmpty()) return

        try {
            val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"
            val ref = FirebaseDatabase.getInstance(dbUrl).getReference("users").child(userId).child("blockMasterStats")

            val statsMap = mapOf(
                "highScore" to saveData.highScore,
                "playerLevel" to saveData.playerLevel,
                "currentInfiniteLevel" to saveData.currentInfiniteLevel,
                "highestLevelReached" to saveData.highestLevelReached,
                "totalLinesCleared" to saveData.totalLinesCleared,
                "timePlayedSeconds" to saveData.timePlayedSeconds,
                "totalGamesPlayed" to saveData.totalGamesPlayed,
                "totalGamesWon" to saveData.totalGamesWon,
                "achievementsUnlocked" to saveData.claimedAchievements.size,
                "selectedWorldId" to saveData.selectedWorldId,
                "highestComboAllTime" to saveData.highestComboAllTime,
                "lastSyncTimestamp" to System.currentTimeMillis()
            )

            ref.updateChildren(statsMap)
            Log.d(TAG, "Successfully synced Block Master stats with PlayWin profile node.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync player profile stats to Firebase: ${e.message}")
        }
    }

    private fun savePendingReward(item: PendingRewardItem) {
        val current = getPendingRewards().toMutableList()
        current.add(item)
        savePendingList(current)
    }

    private fun savePendingList(items: List<PendingRewardItem>) {
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("nonce", item.nonce)
            obj.put("coins", item.coins)
            obj.put("source", item.source)
            obj.put("type", item.type)
            obj.put("timestamp", item.timestamp)
            jsonArray.put(obj)
        }
        prefs.edit().putString("pending_rewards_json", jsonArray.toString()).apply()
    }

    private fun getPendingRewards(): List<PendingRewardItem> {
        val raw = prefs.getString("pending_rewards_json", "[]") ?: "[]"
        val list = ArrayList<PendingRewardItem>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PendingRewardItem(
                        nonce = obj.optString("nonce", ""),
                        coins = obj.optInt("coins", 0),
                        source = obj.optString("source", "Block Master Offline Reward"),
                        type = obj.optString("type", "MINI_GAME_REWARD"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing pending rewards queue: ${e.message}")
        }
        return list
    }
}
