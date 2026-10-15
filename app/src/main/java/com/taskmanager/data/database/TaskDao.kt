package com.taskmanager.data.database

import androidx.room.*
import com.taskmanager.data.model.Prioritas
import com.taskmanager.data.model.StatusTugas
import com.taskmanager.data.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) untuk operasi CRUD tugas.
 * Menggunakan Flow untuk reactive data streaming.
 */
@Dao
interface TaskDao {

    /** Mendapatkan semua tugas, diurutkan berdasarkan deadline terdekat */
    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getAllTasks(): Flow<List<Task>>

    /** Mendapatkan tugas berdasarkan ID */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Long): Flow<Task?>

    /** Mencari tugas berdasarkan kata kunci (judul atau deskripsi) */
    @Query("SELECT * FROM tasks WHERE judul LIKE '%' || :query || '%' OR deskripsi LIKE '%' || :query || '%' OR mataKuliah LIKE '%' || :query || '%' ORDER BY deadline ASC")
    fun searchTasks(query: String): Flow<List<Task>>

    /** Filter tugas berdasarkan prioritas */
    @Query("SELECT * FROM tasks WHERE prioritas = :prioritas ORDER BY deadline ASC")
    fun getTasksByPrioritas(prioritas: Prioritas): Flow<List<Task>>

    /** Filter tugas berdasarkan status */
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY deadline ASC")
    fun getTasksByStatus(status: StatusTugas): Flow<List<Task>>

    /** Filter tugas berdasarkan prioritas DAN status */
    @Query("SELECT * FROM tasks WHERE prioritas = :prioritas AND status = :status ORDER BY deadline ASC")
    fun getTasksByPrioritasAndStatus(prioritas: Prioritas, status: StatusTugas): Flow<List<Task>>

    /** Mendapatkan tugas yang sudah melewati deadline dan belum selesai */
    @Query("SELECT * FROM tasks WHERE deadline < :now AND status != 'SELESAI' ORDER BY deadline ASC")
    fun getOverdueTasks(now: Long = System.currentTimeMillis()): Flow<List<Task>>

    /** Menambahkan tugas baru */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    /** Memperbarui tugas yang sudah ada */
    @Update
    suspend fun updateTask(task: Task)

    /** Menghapus tugas */
    @Delete
    suspend fun deleteTask(task: Task)

    /** Menghapus tugas berdasarkan ID */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    /** Menghitung jumlah tugas berdasarkan status */
    @Query("SELECT COUNT(*) FROM tasks WHERE status = :status")
    fun countTasksByStatus(status: StatusTugas): Flow<Int>
}
