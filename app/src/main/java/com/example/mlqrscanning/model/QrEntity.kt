package com.example.mlqrscanning.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qrtable")
class QrEntity(
    @PrimaryKey(autoGenerate = true) var uid: Long,
    @ColumnInfo(name = "qr_code") var qrCode: String,
    @ColumnInfo(name = "start_time") var startTime: Long,
    @ColumnInfo(name = "end_time") var endTime: Long,
    @ColumnInfo(name = "total_time") var totalTime: Long
)