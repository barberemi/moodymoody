package com.example.moodymoody.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodymoody.data.MoodEntry
import com.example.moodymoody.data.MoodIconFamily
import com.example.moodymoody.data.MoodRepository
import com.example.moodymoody.data.NotificationPreferences
import com.example.moodymoody.notifications.MoodReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class MoodViewModel(
    private val repository: MoodRepository,
    private val notificationPreferences: NotificationPreferences,
    private val appContext: Context,
    private val initialMonth: YearMonth = YearMonth.now()
) : ViewModel() {
    private val today = LocalDate.now()
    private val currentMonth: YearMonth
        get() = YearMonth.now()

    private val _selectedMonth = MutableStateFlow(initialMonth)
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    val monthEntries: StateFlow<List<MoodEntry>> =
        _selectedMonth
            .flatMapLatest { repository.observeMonth(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val todayEntry: StateFlow<MoodEntry?> =
        repository.observeDay(today)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    val reminderTime = notificationPreferences.reminderTime
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LocalTime.of(8, 0)
        )

    val iconFamily = notificationPreferences.iconFamily
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MoodIconFamily.POUSSIN
        )

    fun setMood(emoji: String) {
        viewModelScope.launch {
            repository.upsertMood(today, emoji)
        }
    }

    fun updateReminderTime(time: LocalTime) {
        viewModelScope.launch {
            notificationPreferences.setReminderTime(time)
            MoodReminderScheduler.scheduleDailyReminders(appContext, time)
        }
    }

    fun setIconFamily(family: MoodIconFamily) {
        viewModelScope.launch {
            notificationPreferences.setIconFamily(family)
        }
    }

    fun previousMonth() {
        _selectedMonth.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        _selectedMonth.update { it.plusMonths(1) }
    }

    fun resetMonth() {
        _selectedMonth.value = currentMonth
    }
}
