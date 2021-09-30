package com.example.mlqrscanning.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mlqrscanning.R
import com.example.mlqrscanning.model.QrEntity
import java.text.SimpleDateFormat
import java.util.*


class QrListAdapter(private val qrList: List<QrEntity>) :
    RecyclerView.Adapter<QrListAdapter.QrViewHolder>() {


    class QrViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var qrCode: TextView? = view.findViewById(R.id.qrCode)
        var time_taken: TextView? = view.findViewById(R.id.time_taken)
        var start_time: TextView? = view.findViewById(R.id.start_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_text_item, parent, false)
        return QrViewHolder(view)
    }

    override fun onBindViewHolder(holder: QrViewHolder, position: Int) {
        val qr = qrList[position]
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = qr.startTime

        holder.start_time?.text = sdf.format(calendar.time)

        holder.qrCode?.text = qr.qrCode
        holder.time_taken?.text = qr.totalTime.toString() + " ms"
    }

    override fun getItemCount(): Int {
        return qrList.size
    }
}