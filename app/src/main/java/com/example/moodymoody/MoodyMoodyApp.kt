package com.example.moodymoody

import android.app.Application
import com.example.moodymoody.data.NotificationPreferences
import com.example.moodymoody.di.AppContainer
import com.example.moodymoody.notifications.MoodReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MoodyMoodyApp : Application() {
    lateinit var container: AppContainer
        private set

    val notificationPreferences: NotificationPreferences by lazy {
        container.notificationPreferences
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch {
            val time = notificationPreferences.currentReminderTime()
            MoodReminderScheduler.scheduleDailyReminders(this@MoodyMoodyApp, time)
        }
    }
}
