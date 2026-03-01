package com.example.moodymoody.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries WHERE date BETWEEN :start AND :end ORDER BY date")
    fun observeEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE date = :date")
    fun observeEntriesForDate(date: LocalDate): Flow<List<MoodEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: MoodEntry)
}
