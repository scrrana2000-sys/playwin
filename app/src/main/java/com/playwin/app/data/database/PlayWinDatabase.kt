package com.playwin.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.playwin.app.data.model.RewardTransaction
import com.playwin.app.data.model.UserWallet

@Database(entities = [UserWallet::class, RewardTransaction::class], version = 8, exportSchema = false)
abstract class PlayWinDatabase : RoomDatabase() {
    abstract fun playWinDao(): PlayWinDao

    companion object {
        @Volatile
        private var INSTANCE: PlayWinDatabase? = null

        fun getDatabase(context: Context): PlayWinDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlayWinDatabase::class.java,
                    "play_win_database"
                )
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build().also { INSTANCE = it }
            }
        }
    }
}
