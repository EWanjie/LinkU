package com.example.myapplication

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import org.osmdroid.config.Configuration

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // osmdroid требует userAgent (иначе иногда не подгружает тайлы)
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        // создаём канал уведомлений (для Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                "weather_alerts",
                "Оповещения о погоде",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Показывать баннер сверху"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 150, 100, 150)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(ch)
        }
    }
}