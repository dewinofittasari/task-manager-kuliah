package com.taskmanager.ui.list

import androidx.lifecycle.*
import com.taskmanager.data.model.Prioritas
import com.taskmanager.data.model.StatusTugas
import com.taskmanager.data.model.Task
import com.taskmanager.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel untuk daftar tugas.
 * Mengelola state pencarian, filter, dan daftar tugas.
 */
class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    /** Query pencarian saat ini */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Filter prioritas saat ini (null = semua) */
    private val _filterPrioritas = MutableStateFlow<Prioritas?>(null)
    val filterPrioritas: StateFlow<Prioritas?> = _filterPrioritas.asStateFlow()

    /** Filter status saat ini (null = semua) */
    private val _filterStatus = MutableStateFlow<StatusTugas?>(null)
    val filterStatus: StateFlow<StatusTugas?> = _filterStatus.asStateFlow()

    /**
     * Daftar tugas yang sudah difilter dan dicari.
     * Otomatis diperbarui ketika query pencarian atau filter berubah.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredTasks: StateFlow<List<Task>> = combine(
        _searchQuery,
        _filterPrioritas,
        _filterStatus
    ) { query, prioritas, status ->
        Triple(query, prioritas, status)
    }.flatMapLatest { (query, prioritas, status) ->
        when {
            query.isNotBlank() -> repository.searchTasks(query)
            prioritas != null && status != null -> repository.getTasksByPrioritasAndStatus(prioritas, status)
            prioritas != null -> repository.getTasksByPrioritas(prioritas)
            status != null -> repository.getTasksByStatus(status)
            else -> repository.allTasks
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /** Mengubah query pencarian */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Mengubah filter prioritas */
    fun setFilterPrioritas(prioritas: Prioritas?) {
        _filterPrioritas.value = prioritas
    }

    /** Mengubah filter status */
    fun setFilterStatus(status: StatusTugas?) {
        _filterStatus.value = status
    }

    /** Menghapus semua filter */
    fun clearFilters() {
        _searchQuery.value = ""
        _filterPrioritas.value = null
        _filterStatus.value = null
    }

    /** Menghapus tugas */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    /** Mengubah status tugas */
    fun updateTaskStatus(task: Task, newStatus: StatusTugas) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = newStatus))
        }
    }
}

/**
 * Factory untuk membuat TaskListViewModel dengan parameter Repository.
 */
class TaskListViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
