package com.example.moodymoody.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.example.moodymoody.MainActivity
import com.example.moodymoody.R

class MoodReminderReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent?) {
        val contentIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(Intent(context, MainActivity::class.java))
            getPendingIntent(
                MoodReminderScheduler.REMINDER_NOTIFICATION_ID,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val channelId = MoodReminderScheduler.NOTIFICATION_CHANNEL_ID
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.poussin_bien)
            .setContentTitle(context.getString(R.string.mood_reminder_title))
            .setContentText(context.getString(R.string.mood_reminder_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(MoodReminderScheduler.REMINDER_NOTIFICATION_ID, notification)
    }
}
