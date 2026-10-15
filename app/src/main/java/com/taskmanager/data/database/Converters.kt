package com.taskmanager.data.database

import androidx.room.TypeConverter
import com.taskmanager.data.model.Prioritas
import com.taskmanager.data.model.StatusTugas
import java.util.Date

/**
 * Konverter tipe data untuk Room Database.
 * Mengubah tipe data kompleks (Date, Enum) ke tipe primitif yang bisa disimpan di SQLite.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromPrioritas(prioritas: Prioritas): String {
        return prioritas.name
    }

    @TypeConverter
    fun toPrioritas(value: String): Prioritas {
        return Prioritas.valueOf(value)
    }

    @TypeConverter
    fun fromStatusTugas(status: StatusTugas): String {
        return status.name
    }

    @TypeConverter
    fun toStatusTugas(value: String): StatusTugas {
        return StatusTugas.valueOf(value)
    }
}
