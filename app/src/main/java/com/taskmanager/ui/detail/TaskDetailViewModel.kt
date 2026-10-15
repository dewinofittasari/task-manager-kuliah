package com.taskmanager.ui.detail

import androidx.lifecycle.*
import com.taskmanager.data.model.Task
import com.taskmanager.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel untuk detail tugas.
 */
class TaskDetailViewModel(
    private val repository: TaskRepository,
    private val taskId: Long
) : ViewModel() {

    /** Data tugas yang sedang ditampilkan */
    val task: StateFlow<Task?> = repository.getTaskById(taskId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /** Menghapus tugas */
    fun deleteTask(onDeleted: () -> Unit) {
        viewModelScope.launch {
            task.value?.let {
                repository.deleteTask(it)
                onDeleted()
            }
        }
    }
}

class TaskDetailViewModelFactory(
    private val repository: TaskRepository,
    private val taskId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskDetailViewModel(repository, taskId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
