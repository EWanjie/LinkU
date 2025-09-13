package com.example.myapplication

sealed class CallListItem {
    data class DateHeader(val title: String) : CallListItem()
    data class CallItem(val call: CallClass) : CallListItem()
}