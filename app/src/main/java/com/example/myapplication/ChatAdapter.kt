package com.example.myapplication.com.example.myapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MessageStatus
import com.example.myapplication.R
import com.example.myapplication.model.com.example.myapplication.ChatItem

class ChatAdapter (
    private val onChatClick: (ChatItem) -> Unit = {}
) : RecyclerView.Adapter<ChatAdapter.ChatVH>() {

    private var items = mutableListOf<ChatItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<ChatItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.chat_view, parent, false)
        return ChatVH(v, onChatClick)
    }

    override fun onBindViewHolder(holder: ChatVH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class ChatVH(
        itemView: View,
        private val onChatClick: (ChatItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val avatar: ImageView = itemView.findViewById(R.id.personview)
        private val name: TextView = itemView.findViewById(R.id.chat_full_name)
        private val lastMsg: TextView = itemView.findViewById(R.id.last_message)
        private val timeView: TextView = itemView.findViewById(R.id.time_view)
        private val statusIcon: ImageView = itemView.findViewById(R.id.imageView)

        private val unreadBadge: TextView = itemView.findViewById(R.id.unread_badge)

        fun bind(item: ChatItem) {
            name.text = item.fullName
            avatar.setImageResource(item.avatarRes)

            lastMsg.text = when {
                (item.lastMessageText.isNullOrBlank()) -> "[Фото]"
                else -> (item.lastMessageText ?: "").truncateMiddle(40)
            }

            timeView.text = formatTime(item.lastMessageTime)

            if (item.unreadCount > 0){
                unreadBadge.visibility = View.VISIBLE
                unreadBadge.text = item.unreadCount.toString()
                statusIcon.visibility = View.GONE
            } else if (item.isLastOutgoing){
                unreadBadge.visibility = View.GONE
                when (item.lastStatus) {
                    MessageStatus.READ      -> {
                        statusIcon.visibility = View.VISIBLE
                        statusIcon.setImageResource(R.drawable.galochka2)
                    }
                    MessageStatus.DELIVERED,
                    MessageStatus.SENT      -> {
                        statusIcon.visibility = View.VISIBLE
                        statusIcon.setImageResource(R.drawable.galochka)
                    }
                    null -> {
                        statusIcon.visibility = View.GONE
                    }
                }
            }
            else{
                statusIcon.visibility = View.GONE
                unreadBadge.visibility = View.GONE
            }
            itemView.setOnClickListener { onChatClick(item) }
        }

        private fun formatTime(ts: Long): String {
            val nowCal = java.util.Calendar.getInstance()
            val msgCal = java.util.Calendar.getInstance().apply { timeInMillis = ts }

            return when {
                isSameDay(nowCal, msgCal) -> {
                    // Сегодня: показываем время
                    java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(ts))
                }
                isSameWeek(nowCal, msgCal) -> {
                    // На этой неделе: показываем день недели (пн, вт, ср, ...)
                    dayOfWeekRu(msgCal.get(java.util.Calendar.DAY_OF_WEEK))
                }
                else -> {
                    // Иначе: дата dd.MM.yyyy
                    java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(ts))
                }
            }
        }

        private fun isSameDay(a: java.util.Calendar, b: java.util.Calendar): Boolean {
            return a.get(java.util.Calendar.ERA) == b.get(java.util.Calendar.ERA) &&
                    a.get(java.util.Calendar.YEAR) == b.get(java.util.Calendar.YEAR) &&
                    a.get(java.util.Calendar.DAY_OF_YEAR) == b.get(java.util.Calendar.DAY_OF_YEAR)
        }

        private fun isSameWeek(a: java.util.Calendar, b: java.util.Calendar): Boolean {
            // Неделя начинается с понедельника
            val aCopy = a.clone() as java.util.Calendar
            val bCopy = b.clone() as java.util.Calendar
            aCopy.firstDayOfWeek = java.util.Calendar.MONDAY
            bCopy.firstDayOfWeek = java.util.Calendar.MONDAY

            return aCopy.get(java.util.Calendar.ERA) == bCopy.get(java.util.Calendar.ERA) &&
                    aCopy.get(java.util.Calendar.YEAR) == bCopy.get(java.util.Calendar.YEAR) &&
                    aCopy.get(java.util.Calendar.WEEK_OF_YEAR) == bCopy.get(java.util.Calendar.WEEK_OF_YEAR)
        }

        private fun dayOfWeekRu(dow: Int): String = when (dow) {
            java.util.Calendar.MONDAY -> "пн"
            java.util.Calendar.TUESDAY -> "вт"
            java.util.Calendar.WEDNESDAY -> "ср"
            java.util.Calendar.THURSDAY -> "чт"
            java.util.Calendar.FRIDAY -> "пт"
            java.util.Calendar.SATURDAY -> "сб"
            java.util.Calendar.SUNDAY -> "вс"
            else -> ""
        }

        private fun String.truncateMiddle(maxLen: Int): String {
            if (length <= maxLen) return this
            val keep = maxLen - 1
            return substring(0, keep) + "…"
        }

    }
}