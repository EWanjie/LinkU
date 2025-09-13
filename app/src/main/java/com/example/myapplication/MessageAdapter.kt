package com.example.myapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.setPadding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class MessageStatus {
    SENT, DELIVERED, READ
}

sealed class ChatListItem {
    data class DateHeader(val title: String) : ChatListItem()
    data class Msg(val message: Message) : ChatListItem()
}

class MessageAdapter(
    private var messageList: List<Message>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DATE = 0
        const val VIEW_TYPE_LEFT = 1
        const val VIEW_TYPE_RIGHT = 2
    }

    private val items = mutableListOf<ChatListItem>()

    init {
        submitList(messageList)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newMessages: List<Message>) {
        messageList = newMessages
        items.clear()
        items.addAll(buildItemsWithDates(newMessages))
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (val it = items[position]) {
            is ChatListItem.DateHeader -> TYPE_DATE
            is ChatListItem.Msg -> if (it.message.toFrom) VIEW_TYPE_RIGHT else VIEW_TYPE_LEFT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_DATE -> {
                val view = inflater.inflate(R.layout.day2, parent, false)
                DateViewHolder(view)
            }
            VIEW_TYPE_RIGHT -> {
                val view = inflater.inflate(R.layout.item_massage_right, parent, false)
                RightMessageViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_massage_left, parent, false)
                LeftMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ChatListItem.DateHeader -> (holder as DateViewHolder).bind(item.title)
            is ChatListItem.Msg -> {
                val message = item.message
                if (holder is RightMessageViewHolder) {
                    holder.bind(message)
                } else if (holder is LeftMessageViewHolder) {
                    holder.bind(message)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.daterText)
        fun bind(title: String) {
            dateText.text = title
        }
    }

    class LeftMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.left_text)
        private val timeRight: TextView = itemView.findViewById(R.id.left_meta_right_text)
        private val timeDown: TextView = itemView.findViewById(R.id.left_meta_down_text)
        private val image: ImageView = itemView.findViewById(R.id.lift_image)
        private val contain: View = itemView.findViewById(R.id.all_layout)

        // --- сохраняем базовое состояние из XML один раз ---
        private val baseLpTimeRight =
            (timeRight.layoutParams as ConstraintLayout.LayoutParams).let { ConstraintLayout.LayoutParams(it) }
        private val baseLpTimeDown  =
            (timeDown.layoutParams as ConstraintLayout.LayoutParams).let { ConstraintLayout.LayoutParams(it) }

        private val basePaddingContain = intArrayOf(
            contain.paddingLeft, contain.paddingTop, contain.paddingRight, contain.paddingBottom
        )
        private val basePaddingDown = intArrayOf(
            timeDown.paddingLeft, timeDown.paddingTop, timeDown.paddingRight, timeDown.paddingBottom
        )

        private val baseColorTimeDown = timeDown.currentTextColor

        private fun View.setPaddingLTRB(l: Int, t: Int, r: Int, b: Int) = setPadding(l, t, r, b)
        private fun Int.dp(v: View) = (this * v.resources.displayMetrics.density).toInt()

        /** жёсткий сброс ко всем значениям из XML */
        private fun resetToBase() {
            timeRight.layoutParams = ConstraintLayout.LayoutParams(baseLpTimeRight)
            timeDown.layoutParams  = ConstraintLayout.LayoutParams(baseLpTimeDown)

            contain.setPaddingLTRB(
                basePaddingContain[0], basePaddingContain[1], basePaddingContain[2], basePaddingContain[3]
            )
            timeDown.setPaddingLTRB(
                basePaddingDown[0], basePaddingDown[1], basePaddingDown[2], basePaddingDown[3]
            )
            timeDown.setTextColor(baseColorTimeDown)

            image.visibility = View.GONE
            messageText.visibility = View.VISIBLE
            timeRight.visibility = View.GONE   // по умолчанию в XML — GONE
            timeDown.visibility  = View.VISIBLE
        }

        fun bind(message: Message) {
            // 1) сброс
            resetToBase()

            // 2) данные
            messageText.text = message.textMessage
            val timeStr = TimeUtils.formatTime(message.dataTime)
            timeRight.text = timeStr
            timeDown.text  = timeStr

            // 3) режим "картинка"
            if (message.img != null) {
                image.setImageResource(message.img!!)

                val lp = timeDown.layoutParams as ConstraintLayout.LayoutParams
                lp.topToBottom = ConstraintLayout.LayoutParams.UNSET
                lp.bottomToBottom = R.id.lift_image
                lp.endToEnd = R.id.lift_image
                timeDown.layoutParams = lp

                timeDown.setPaddingLTRB(0, 0, 10.dp(contain), 5.dp(contain))
                contain.setPadding(5.dp(contain))

                timeDown.setTextColor(ContextCompat.getColor(timeDown.context, R.color.white))

                image.visibility = View.VISIBLE
                messageText.visibility = View.GONE
                timeRight.visibility = View.GONE
                timeDown.visibility  = View.VISIBLE
                return
            }

            // 4) режим "текст": старая проверка на влезание
            val metaW = TimeUtils.measureMetaWidth(messageText, timeStr, null)
            val inlineFits = TimeUtils.fitsInlineOneLine(messageText, message.textMessage, metaW)

            if (inlineFits) {
                timeRight.visibility = View.VISIBLE
                timeDown.visibility  = View.GONE
            } else {
                timeRight.visibility = View.GONE
                timeDown.visibility  = View.VISIBLE
            }
        }
    }

    class RightMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.right_text)
        private val metaRight: View = itemView.findViewById(R.id.right_meta_right)
        private val timeRight: TextView = itemView.findViewById(R.id.right_meta_right_text)
        private val imgRight: ImageView = itemView.findViewById(R.id.right_meta_right_img)
        private val metaDown: View = itemView.findViewById(R.id.right_meta_down)
        private val timeDown: TextView = itemView.findViewById(R.id.right_meta_down_text)
        private val imgDown: ImageView = itemView.findViewById(R.id.right_meta_down_img)

        fun bind(message: Message) {
            messageText.text = message.textMessage
            val time = TimeUtils.formatTime(message.dataTime)

            timeRight.text = time
            timeDown.text = time

            val iconRes = when (message.status) {
                MessageStatus.READ -> R.drawable.galochka2
                MessageStatus.SENT -> R.drawable.galochka
                else -> null
            }

            if (iconRes != null) {
                imgRight.setImageResource(iconRes)
                imgDown.setImageResource(iconRes)
            }

            val metaW = TimeUtils.measureMetaWidth(messageText, time, if (iconRes != null) imgRight else null)
            val inlineFits = TimeUtils.fitsInlineOneLine(messageText, message.textMessage, metaW)

            if (inlineFits) {
                metaRight.visibility = View.VISIBLE
                metaDown.visibility = View.GONE
            } else {
                metaRight.visibility = View.GONE
                metaDown.visibility = View.VISIBLE
            }
        }
    }

    object TimeUtils {
        fun formatTime(timestamp: Long): String {
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }

        fun measureMetaWidth(anchor: TextView, time: String, icon: ImageView?): Int {
            val paint = anchor.paint
            val timeW = paint.measureText(time).toInt()
            val gapPx = (8f * anchor.resources.displayMetrics.density).toInt()
            val iconW = icon?.let { if (it.isVisible) it.drawable?.intrinsicWidth ?: 0 else 0 } ?: 0
            return if (iconW > 0) timeW + gapPx + iconW else timeW
        }

        fun fitsInlineOneLine(tv: TextView, text: String, metaWidthPx: Int): Boolean {
            val textW = tv.paint.measureText(text).toInt()
            val maxW = tv.maxWidth.takeIf { it > 0 } ?: pxFromDp(tv, 100f)
            return textW + metaWidthPx <= maxW
        }

        fun pxFromDp(view: View, dp: Float): Int {
            val d = view.resources.displayMetrics.density
            return (dp * d).toInt()
        }
    }

    private fun buildItemsWithDates(messages: List<Message>): List<ChatListItem> {
        if (messages.isEmpty()) return emptyList()

        val out = mutableListOf<ChatListItem>()

        var lastHeaderKey: String? = null
        for (msg in messages) {
            val key = dayKey(msg.dataTime) // ключ «год-месяц-день»
            if (key != lastHeaderKey) {
                out += ChatListItem.DateHeader(formatDateHeader(msg.dataTime))
                lastHeaderKey = key
            }
            out += ChatListItem.Msg(msg)
        }
        return out
    }
    private fun dayKey(timestamp: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(y, m, d)
    }

    private fun formatDateHeader(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMMM", Locale("ru"))
        return sdf.format(Date(timestamp))
    }
}