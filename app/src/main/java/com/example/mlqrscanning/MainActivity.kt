package com.example.mlqrscanning

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.mlqrscanning.list.QrListActivity
import com.example.mlqrscanning.model.AppDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.start_scan).setOnClickListener{
            startActivity(Intent(this, QrScanActivity::class.java))
        }

        findViewById<Button>(R.id.see_all_scans).setOnClickListener{
            startActivity(Intent(this, QrListActivity::class.java))
        }

        findViewById<Button>(R.id.clear_all).setOnClickListener{
            val qrDao = AppDatabase.getInstance(applicationContext)?.qrDao()!!
            qrDao.clearAll()
        }
    }
}