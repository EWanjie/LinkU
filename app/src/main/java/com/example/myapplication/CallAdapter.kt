package com.example.myapplication

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CallAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val callList = mutableListOf<CallListItem>()

    companion object {
        private const val TYPE_DATE = 0
        private const val TYPE_CALL = 1
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<CallListItem>) {
        callList.clear()
        callList.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        when (callList[position]) {
            is CallListItem.DateHeader -> TYPE_DATE
            is CallListItem.CallItem -> TYPE_CALL
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == TYPE_DATE) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.day, parent, false)
            DateVH(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.call_view, parent, false)
            CallVH(v)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = callList[position]) {
            is CallListItem.DateHeader -> (holder as DateVH).bind(item.title)
            is CallListItem.CallItem -> {
                val isLastForDay = isLastInDay(position)
                (holder as CallVH).bind(item.call, isLastForDay)
            }
        }
    }

    override fun getItemCount(): Int = callList.size

    class DateVH(view: View) : RecyclerView.ViewHolder(view) {
        private val dateText: TextView = view.findViewById(R.id.dateHeaderText)
        fun bind(title: String) {
            dateText.text = title
        }
    }

    class CallVH(view: View) : RecyclerView.ViewHolder(view) {
        private val img: ImageView = view.findViewById(R.id.call_view)
        private val text: TextView = view.findViewById(R.id.call_text)
        private val time: TextView = view.findViewById(R.id.call_time)

        private val line: View = view.findViewById(R.id.line1)

        private val divider: View = view.findViewById(R.id.liniya)

        fun bind(call: CallClass, isLastForDay: Boolean) {
            img.setImageResource(call.img)

            if (call.img == R.drawable.not_call) {
                img.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.red),
                    PorterDuff.Mode.SRC_IN
                )
            }
            else{
                img.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.green),
                    PorterDuff.Mode.SRC_IN
                )
            }


            text.text = call.text
            time.text = formatTimeHHmm(call.dataTime)

            divider.visibility = if (isLastForDay) View.GONE else View.VISIBLE
        }

    }

    private fun isLastInDay(position: Int): Boolean {
        if (position == callList.lastIndex) return true
        val next = callList[position + 1]
        return next is CallListItem.DateHeader
    }
}