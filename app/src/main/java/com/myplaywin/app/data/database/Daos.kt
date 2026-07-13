package com.myplaywin.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.myplaywin.app.data.model.RewardTransaction
import com.myplaywin.app.data.model.UserWallet
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayWinDao {
    @Query("SELECT * FROM user_wallet WHERE id = 1 LIMIT 1")
    fun getUserWallet(): Flow<UserWallet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWallet(wallet: UserWallet)

    @Query("SELECT * FROM reward_transaction ORDER BY timestamp DESC LIMIT 50")
    fun getTransactions(): Flow<List<RewardTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: RewardTransaction)

    @Query("DELETE FROM reward_transaction")
    suspend fun clearAllTransactions()
}
