package com.example.moodymoody.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

class MoodRepository(
    private val dao: MoodDao,
    private val preferences: NotificationPreferences
) {
    fun observeMonth(yearMonth: YearMonth): Flow<List<MoodEntry>> {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        return dao.observeEntriesBetween(start, end).distinctUntilChanged()
    }

    fun observeDay(date: LocalDate): Flow<MoodEntry?> =
        dao.observeEntriesForDate(date)
            .map { entries -> entries.firstOrNull { it.slot == MoodSlot.MORNING } }
            .distinctUntilChanged()

    fun observeIconFamily(): Flow<MoodIconFamily> = preferences.iconFamily

    suspend fun setIconFamily(family: MoodIconFamily) {
        preferences.setIconFamily(family)
    }

    suspend fun upsertMood(date: LocalDate, emoji: String) {
        dao.upsert(
            MoodEntry(
                date = date,
                slot = MoodSlot.MORNING,
                emoji = emoji
            )
        )
    }
}
