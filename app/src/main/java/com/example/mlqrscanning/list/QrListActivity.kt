package com.example.mlqrscanning.list

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mlqrscanning.R
import com.example.mlqrscanning.model.AppDatabase

class QrListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_list)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_qrs)

        val qrDao = AppDatabase.getInstance(applicationContext)?.qrDao()!!

        val adapter = QrListAdapter(qrDao.getAll())
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}