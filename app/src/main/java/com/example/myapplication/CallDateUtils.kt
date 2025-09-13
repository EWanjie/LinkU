package com.example.myapplication

import java.text.SimpleDateFormat
import java.util.*

fun formatDateHeader(ts: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = ts }
    val today = Calendar.getInstance()
    val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
        isSameDay(cal, today)     -> "Сегодня"
        isSameDay(cal, yesterday) -> "Вчера"
        else -> SimpleDateFormat("d MMMM", Locale("ru","RU"))
            .format(Date(ts))
    }
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

fun formatTimeHHmm(ts: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))

fun buildCallItemsWithHeaders(calls: List<CallClass>): List<CallListItem> {
    // новые звонки сверху
    val sorted = calls.sortedByDescending { it.dataTime }
    val result = mutableListOf<CallListItem>()
    var lastHeader: String? = null

    for (c in sorted) {
        val header = formatDateHeader(c.dataTime)
        if (header != lastHeader) {
            result.add(CallListItem.DateHeader(header))
            lastHeader = header
        }
        result.add(CallListItem.CallItem(c))
    }
    return result
}