package com.example.myapplication

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    var idPerson: Int,
    var toFrom: Boolean,
    var dataTime: Long,
    var textMessage: String,
    var img: Int? = null,
    var status: MessageStatus?
) : Parcelable