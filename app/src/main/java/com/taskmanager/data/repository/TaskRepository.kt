package com.taskmanager.data.repository

import com.taskmanager.data.database.TaskDao
import com.taskmanager.data.model.Prioritas
import com.taskmanager.data.model.StatusTugas
import com.taskmanager.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository sebagai sumber data tunggal (single source of truth).
 * Mengelola akses data dari Room Database.
 */
class TaskRepository(private val taskDao: TaskDao) {

    /** Mendapatkan semua tugas */
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    /** Mendapatkan tugas berdasarkan ID */
    fun getTaskById(taskId: Long): Flow<Task?> = taskDao.getTaskById(taskId)

    /** Mencari tugas berdasarkan query */
    fun searchTasks(query: String): Flow<List<Task>> = taskDao.searchTasks(query)

    /** Filter berdasarkan prioritas */
    fun getTasksByPrioritas(prioritas: Prioritas): Flow<List<Task>> =
        taskDao.getTasksByPrioritas(prioritas)

    /** Filter berdasarkan status */
    fun getTasksByStatus(status: StatusTugas): Flow<List<Task>> =
        taskDao.getTasksByStatus(status)

    /** Filter berdasarkan prioritas dan status */
    fun getTasksByPrioritasAndStatus(prioritas: Prioritas, status: StatusTugas): Flow<List<Task>> =
        taskDao.getTasksByPrioritasAndStatus(prioritas, status)

    /** Mendapatkan tugas yang melewati deadline */
    fun getOverdueTasks(): Flow<List<Task>> = taskDao.getOverdueTasks()

    /** Menambahkan tugas baru */
    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    /** Memperbarui tugas */
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.copy(tanggalDiubah = Date()))
    }

    /** Menghapus tugas */
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    /** Menghitung tugas berdasarkan status */
    fun countTasksByStatus(status: StatusTugas): Flow<Int> =
        taskDao.countTasksByStatus(status)
}
