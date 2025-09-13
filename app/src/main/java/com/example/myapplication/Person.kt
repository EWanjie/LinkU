package com.example.myapplication

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Person(
    var idPerson: Int,
    var fullName: String,
    var img: Int,
    var fullImg: Int,
    var online: String,
    var userName: String
) : Parcelable