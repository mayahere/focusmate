package com.example.focusmate.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority { LOW, MEDIUM, HIGH }
enum class TaskStatus { TODO, IN_PROGRESS, COMPLETED }

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val taskId: Int = 0,
    val title: String,
    val description: String? = null,
    val dueDate: Long? = null,
    val priority: Priority = Priority.LOW,
    val status: TaskStatus = TaskStatus.TODO,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val focusSessions: Int = 1
)