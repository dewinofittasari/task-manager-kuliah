package com.taskmanager

import android.app.Application
import com.taskmanager.data.database.TaskDatabase
import com.taskmanager.data.repository.TaskRepository

/**
 * Kelas Application kustom untuk inisialisasi komponen global.
 * Database dan Repository diinisialisasi secara lazy (hanya saat dibutuhkan).
 */
class TaskManagerApp : Application() {

    /** Instance database, diinisialisasi secara lazy */
    val database: TaskDatabase by lazy {
        TaskDatabase.getDatabase(this)
    }

    /** Instance repository, diinisialisasi secara lazy */
    val repository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }
}
