package com.example.mlqrscanning

import android.app.Application
import androidx.room.Room
import com.example.mlqrscanning.model.AppDatabase
import com.example.mlqrscanning.model.QrScanDao

class MyApp : Application() {


    override fun onCreate() {
        super.onCreate()

        setupRoom()
    }

    private fun setupRoom() {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "QrDatabase.db")
            .allowMainThreadQueries()
            .build()

        qrDao = db.qrDao()
    }

    companion object {
        private lateinit var qrDao: QrScanDao

        fun getQrDao(): QrScanDao {
            return qrDao
        }
    }

}