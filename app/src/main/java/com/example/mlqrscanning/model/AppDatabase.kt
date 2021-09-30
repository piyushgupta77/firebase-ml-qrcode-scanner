package com.example.mlqrscanning.model

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room

@Database(entities = [QrEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun qrDao(): QrScanDao

    companion object{
        private val DATABASE_NAME = "qrlist.db"
        private var sInstance: AppDatabase? = null
        private val LOCK = Any()
        private val LOG_TAG = AppDatabase::class.java.simpleName

        open fun getInstance(context: Context): AppDatabase? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    Log.d(LOG_TAG, "Creating new database instance")
                    sInstance = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase::class.java, DATABASE_NAME
                    )
                        .allowMainThreadQueries()
                        .setJournalMode(JournalMode.TRUNCATE)
                        .build()
                }
            }
            Log.d(LOG_TAG, "Getting the database instance")
            return sInstance
        }
    }
}