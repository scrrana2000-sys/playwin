package com.playwin.app.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.playwin.app.data.model.FirebaseTransaction

object WalletService {
    private const val dbUrl = "https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app"

    fun updateWallet(
        userId: String,
        coinsDelta: Int,
        source: String,
        type: String,
        extraCheck: ((MutableData) -> String?)? = null,
        extraUpdate: ((MutableData) -> Unit)? = null,
        onComplete: (Boolean, Int, Int, String?) -> Unit
    ) {
        if (userId.isEmpty()) {
            onComplete(false, 0, 0, "User not authenticated.")
            return
        }

        val database = FirebaseDatabase.getInstance(dbUrl)
        val userRef = database.getReference("users").child(userId)

        Log.d("WalletService", "[START] Unified Wallet Transaction: $userId, delta: $coinsDelta, source: $source, type: $type")

        userRef.runTransaction(object : Transaction.Handler {
            var coinsBefore = 0
            var coinsAfter = 0
            var errorMessage: String? = null

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                if (mutableData.value == null) {
                    errorMessage = "User node does not exist in Firebase."
                    return Transaction.abort()
                }

                // Run extra checks (e.g. daily limits, cooldowns, ad requirements)
                val checkError = extraCheck?.invoke(mutableData)
                if (checkError != null) {
                    errorMessage = checkError
                    return Transaction.abort()
                }

                val currentCoins = mutableData.child("coins").getValue(Int::class.java) ?: 0
                coinsBefore = currentCoins
                coinsAfter = currentCoins + coinsDelta

                if (coinsAfter < 0) {
                    errorMessage = "Insufficient coins."
                    return Transaction.abort()
                }

                mutableData.child("coins").value = coinsAfter
                mutableData.child("lastActiveTime").value = System.currentTimeMillis()

                // Run extra updates (e.g. spins used, streaks, scratch cards used)
                extraUpdate?.invoke(mutableData)

                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed && error == null) {
                    Log.d("WalletService", "[SUCCESS] Unified Wallet Transaction Committed: $coinsBefore -> $coinsAfter")

                    try {
                        // 1. Save Transaction Record under transactions/{userId}/{txId}
                        val txRef = database.getReference("transactions").child(userId).push()
                        val tx = FirebaseTransaction(
                            id = txRef.key ?: "",
                            userId = userId,
                            type = type,
                            title = source,
                            coins = coinsDelta,
                            status = "Completed",
                            timestamp = System.currentTimeMillis(),
                            amount = coinsDelta,
                            source = source,
                            coinsBefore = coinsBefore,
                            coinsAfter = coinsAfter
                        )
                        txRef.setValue(tx)

                        // 2. Save Reward History Record under rewardHistory/{userId}/{historyId}
                        val historyRef = database.getReference("rewardHistory").child(userId).push()
                        val historyMap = mapOf(
                            "id" to (historyRef.key ?: ""),
                            "type" to type,
                            "reward" to coinsDelta,
                            "source" to source,
                            "timestamp" to System.currentTimeMillis(),
                            "title" to source,
                            "coins" to coinsDelta,
                            "status" to "Completed",
                            "coinsBefore" to coinsBefore,
                            "coinsAfter" to coinsAfter
                        )
                        historyRef.setValue(historyMap)

                        // 3. Increment Reward History Count on user node
                        val currentCount = currentData?.child("rewardHistoryCount")?.getValue(Int::class.java) ?: 0
                        userRef.child("rewardHistoryCount").setValue(currentCount + 1)

                    } catch (e: Exception) {
                        Log.e("WalletService", "Error writing unified history records: ${e.message}")
                    }

                    onComplete(true, coinsBefore, coinsAfter, null)
                } else {
                    val msg = error?.message ?: errorMessage ?: "Transaction aborted."
                    Log.e("WalletService", "[FAILED] Unified Wallet Transaction Failed: $msg")
                    onComplete(false, coinsBefore, coinsBefore, msg)
                }
            }
        })
    }
}
