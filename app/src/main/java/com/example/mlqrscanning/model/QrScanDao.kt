package com.example.mlqrscanning.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface QrScanDao {
    @Query("SELECT * FROM QRTABLE ORDER BY START_TIME DESC")
    fun getAll(): List<QrEntity>

    @Insert
    fun insertAll(qrEntity: ArrayList<QrEntity>)

    @Insert
    fun insert(qrEntity: QrEntity)

    @Query("DELETE FROM QRTABLE")
    fun clearAll()
}