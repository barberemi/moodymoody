package com.example.moodymoody.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "mood_entries",
    indices = [Index(value = ["date", "slot"], unique = true)]
)
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val slot: MoodSlot,
    val emoji: String
)
