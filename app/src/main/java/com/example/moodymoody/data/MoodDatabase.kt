package com.example.moodymoody.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MoodEntry::class], version = 1, exportSchema = false)
@TypeConverters(MoodTypeConverters::class)
abstract class MoodDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
}

