package com.example.focusmate.data.repository

import com.example.focusmate.data.entity.TaskEntity
import com.example.focusmate.data.local.TaskDao
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    fun getActiveTasks(): Flow<List<TaskEntity>> = taskDao.getActiveTasks()
    fun getCompletedTasks(): Flow<List<TaskEntity>> = taskDao.getCompletedTasks()
    suspend fun getTaskById(taskId: Int): TaskEntity? = taskDao.getTaskById(taskId)
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(taskId: Int) = taskDao.deleteTaskById(taskId)
    suspend fun getNextActiveTask(excludeTaskId: Int): TaskEntity? = taskDao.getNextActiveTask(excludeTaskId)
}