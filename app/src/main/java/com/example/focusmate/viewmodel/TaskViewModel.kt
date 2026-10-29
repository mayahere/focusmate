package com.example.focusmate.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusmate.data.entity.Priority
import com.example.focusmate.data.entity.TaskEntity
import com.example.focusmate.data.entity.TaskStatus
import com.example.focusmate.data.local.AppDatabase
import com.example.focusmate.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TaskFilter { ALL, ACTIVE, COMPLETED }

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter.asStateFlow()

    private val allTasks: Flow<List<TaskEntity>>
    private val activeTasks: Flow<List<TaskEntity>>
    private val completedTasks: Flow<List<TaskEntity>>

    val tasks: StateFlow<List<TaskEntity>>

    init {
        val dao = AppDatabase.getInstance(application).taskDao()
        repository = TaskRepository(dao)
        allTasks = repository.getAllTasks()
        activeTasks = repository.getActiveTasks()
        completedTasks = repository.getCompletedTasks()

        tasks = _filter.flatMapLatest { filter ->
            when (filter) {
                TaskFilter.ALL -> allTasks
                TaskFilter.ACTIVE -> activeTasks
                TaskFilter.COMPLETED -> completedTasks
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun addTask(
        title: String,
        description: String?,
        dueDate: Long?,
        priority: Priority,
        focusSessions: Int = 1
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title.trim(),
                    description = description?.trim()?.ifBlank { null },
                    dueDate = dueDate,
                    priority = priority,
                    focusSessions = focusSessions
                )
            )
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun toggleComplete(task: TaskEntity) {
        viewModelScope.launch {
            val updated = if (task.status == TaskStatus.COMPLETED) {
                task.copy(status = TaskStatus.TODO, completedAt = null)
            } else {
                task.copy(status = TaskStatus.COMPLETED, completedAt = System.currentTimeMillis())
            }
            repository.updateTask(updated)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    suspend fun getTaskById(taskId: Int): TaskEntity? {
        return repository.getTaskById(taskId)
    }
}