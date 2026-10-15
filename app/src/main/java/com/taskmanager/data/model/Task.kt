package com.taskmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity tugas kuliah mahasiswa.
 * Menyimpan informasi tugas termasuk judul, deskripsi, mata kuliah,
 * deadline, prioritas, dan status penyelesaian.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val judul: String,
    val deskripsi: String = "",
    val mataKuliah: String = "",
    val deadline: Date? = null,
    val prioritas: Prioritas = Prioritas.SEDANG,
    val status: StatusTugas = StatusTugas.BELUM_DIKERJAKAN,
    val tanggalDibuat: Date = Date(),
    val tanggalDiubah: Date = Date()
)

/**
 * Enum untuk tingkat prioritas tugas.
 */
enum class Prioritas(val label: String, val nilai: Int) {
    RENDAH("Rendah", 0),
    SEDANG("Sedang", 1),
    TINGGI("Tinggi", 2),
    URGENT("Urgent", 3)
}

/**
 * Enum untuk status penyelesaian tugas.
 */
enum class StatusTugas(val label: String) {
    BELUM_DIKERJAKAN("Belum Dikerjakan"),
    SEDANG_DIKERJAKAN("Sedang Dikerjakan"),
    SELESAI("Selesai")
}
