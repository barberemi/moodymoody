package com.example.moodymoody.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.moodymoody.R
import java.time.LocalTime
import java.util.Calendar

object MoodReminderScheduler {
    const val NOTIFICATION_CHANNEL_ID = "mood_reminder_channel"
    const val REMINDER_NOTIFICATION_ID = 1001
    private const val MORNING_REQUEST_CODE = 100

    fun scheduleDailyReminders(context: Context, time: LocalTime) {
        ensureNotificationChannel(context)
        scheduleReminder(context, time)
    }

    fun cancelReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(createPendingIntent(context))
    }

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.mood_reminder_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.mood_reminder_channel_description)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun scheduleReminder(context: Context, time: LocalTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createPendingIntent(context)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MoodReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            MORNING_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
