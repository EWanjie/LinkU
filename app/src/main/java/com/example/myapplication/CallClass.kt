package com.example.myapplication

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CallClass (
    var img: Int,
    var text: String,
    var dataTime: Long
) : Parcelable