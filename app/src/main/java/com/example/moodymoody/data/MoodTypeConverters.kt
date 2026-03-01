package com.example.moodymoody.data

import androidx.room.TypeConverter
import java.time.LocalDate

class MoodTypeConverters {
    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromSlotName(value: String?): MoodSlot? = value?.let {
        try {
            MoodSlot.valueOf(it)
        } catch (_: IllegalArgumentException) {
            MoodSlot.MORNING
        }
    }

    @TypeConverter
    fun toSlotName(slot: MoodSlot?): String? = slot?.name
}
