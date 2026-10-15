package com.taskmanager.ui.form

import androidx.lifecycle.*
import com.taskmanager.data.model.Prioritas
import com.taskmanager.data.model.StatusTugas
import com.taskmanager.data.model.Task
import com.taskmanager.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel untuk form tambah/edit tugas.
 */
class TaskFormViewModel(
    private val repository: TaskRepository,
    private val taskId: Long
) : ViewModel() {

    /** Apakah ini mode edit (true) atau tambah baru (false) */
    val isEditMode: Boolean = taskId != -1L

    /** Data tugas untuk mode edit */
    val existingTask: StateFlow<Task?> = if (isEditMode) {
        repository.getTaskById(taskId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    } else {
        kotlinx.coroutines.flow.MutableStateFlow<Task?>(null)
    }

    /**
     * Menyimpan tugas baru atau memperbarui tugas yang ada.
     * @return true jika berhasil disimpan
     */
    fun saveTask(
        judul: String,
        deskripsi: String,
        mataKuliah: String,
        deadline: Date?,
        prioritas: Prioritas,
        status: StatusTugas,
        onSuccess: () -> Unit
    ) {
        if (judul.isBlank()) return

        viewModelScope.launch {
            if (isEditMode) {
                existingTask.value?.let { existing ->
                    repository.updateTask(
                        existing.copy(
                            judul = judul,
                            deskripsi = deskripsi,
                            mataKuliah = mataKuliah,
                            deadline = deadline,
                            prioritas = prioritas,
                            status = status
                        )
                    )
                }
            } else {
                repository.insertTask(
                    Task(
                        judul = judul,
                        deskripsi = deskripsi,
                        mataKuliah = mataKuliah,
                        deadline = deadline,
                        prioritas = prioritas,
                        status = status
                    )
                )
            }
            onSuccess()
        }
    }
}

class TaskFormViewModelFactory(
    private val repository: TaskRepository,
    private val taskId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskFormViewModel(repository, taskId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
