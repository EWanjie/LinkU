package com.example.myapplication

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.personview)
    val nameView: TextView = itemView.findViewById(R.id.full_name)
    val onlineView: TextView = itemView.findViewById(R.id.online)
}