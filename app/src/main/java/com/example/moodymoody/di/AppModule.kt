package com.example.moodymoody.di

import android.content.Context
import androidx.room.Room
import com.example.moodymoody.data.MoodDatabase
import com.example.moodymoody.data.MoodRepository
import com.example.moodymoody.data.NotificationPreferences

class AppContainer(context: Context) {
    private val database: MoodDatabase = Room.databaseBuilder(
        context.applicationContext,
        MoodDatabase::class.java,
        "mood_db"
    ).fallbackToDestructiveMigration().build()

    val notificationPreferences = NotificationPreferences(context.applicationContext)
    val moodRepository: MoodRepository = MoodRepository(database.moodDao(), notificationPreferences)
}
