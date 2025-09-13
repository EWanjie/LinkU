package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun Context.showWeatherNotification() {
    val pi = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )


    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.p_5)
    val notif = NotificationCompat.Builder(this, "weather_alerts") // новый канал
        .setSmallIcon(R.drawable.small_logo)
        .setLargeIcon(bitmap)
        .setContentTitle("Алина")
        .setContentText("Ты где?")
        .setStyle(NotificationCompat.BigTextStyle().bigText("Ты где?"))
        .setAutoCancel(true)
        .setContentIntent(pi)

        .setPriority(NotificationCompat.PRIORITY_HIGH)                 // < Android 8
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)              // или EVENT/ALARM
        .setDefaults(NotificationCompat.DEFAULT_ALL)                   // звук/вибра/свет
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .build()

    NotificationManagerCompat.from(this).notify(1001, notif)
}

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun Context.showWeatherNotification2() {
    val pi = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )


    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.p_5)
    val notif = NotificationCompat.Builder(this, "weather_alerts") // новый канал
        .setSmallIcon(R.drawable.small_logo)
        .setLargeIcon(bitmap)
        .setContentTitle("Алина")
        .setContentText("Голосовое сообщение")
        .setStyle(NotificationCompat.BigTextStyle().bigText("Голосовое сообщение"))
        .setAutoCancel(true)
        .setContentIntent(pi)

        .setPriority(NotificationCompat.PRIORITY_HIGH)                 // < Android 8
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)              // или EVENT/ALARM
        .setDefaults(NotificationCompat.DEFAULT_ALL)                   // звук/вибра/свет
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .build()

    NotificationManagerCompat.from(this).notify(1001, notif)
}
