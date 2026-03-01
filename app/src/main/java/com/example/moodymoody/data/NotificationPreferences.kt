package com.example.moodymoody.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalTime

private val Context.notificationDataStore by preferencesDataStore(name = "notification_prefs")

class NotificationPreferences(private val context: Context) {
    private val reminderTimeKey = longPreferencesKey("reminder_time_minutes")
    private val iconFamilyKey = stringPreferencesKey("icon_family")

    val reminderTime: Flow<LocalTime> = context.notificationDataStore.data
        .map { prefs ->
            val minutes = prefs[reminderTimeKey] ?: DEFAULT_MINUTES
            LocalTime.of((minutes / 60).toInt(), (minutes % 60).toInt())
        }

    val iconFamily: Flow<MoodIconFamily> = context.notificationDataStore.data
        .map { prefs ->
            MoodIconFamily.fromName(prefs[iconFamilyKey])
        }

    suspend fun setReminderTime(time: LocalTime) {
        context.notificationDataStore.edit { prefs ->
            prefs[reminderTimeKey] = time.hour * 60L + time.minute
        }
    }

    suspend fun setIconFamily(family: MoodIconFamily) {
        context.notificationDataStore.edit { prefs ->
            prefs[iconFamilyKey] = family.name
        }
    }

    suspend fun currentReminderTime(): LocalTime {
        val minutes = context.notificationDataStore.data
            .map { prefs -> prefs[reminderTimeKey] ?: DEFAULT_MINUTES }
            .first()
        return LocalTime.of((minutes / 60).toInt(), (minutes % 60).toInt())
    }

    companion object {
        private const val DEFAULT_MINUTES = 8 * 60L
        val DEFAULT_TIME: LocalTime = LocalTime.of(8, 0)
    }
}
