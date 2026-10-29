package com.example.focusmate.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SessionType { FOCUS, SHORT_BREAK, LONG_BREAK }
enum class SessionStatus { RUNNING, PAUSED, COMPLETED, CANCELLED }

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int = 0,
    val taskId: Int? = null,
    val sessionType: SessionType = SessionType.FOCUS,
    val plannedDuration: Int, // seconds
    val actualDuration: Int = 0, // seconds
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val status: SessionStatus = SessionStatus.RUNNING
)