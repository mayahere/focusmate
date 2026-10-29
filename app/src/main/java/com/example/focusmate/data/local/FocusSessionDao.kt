package com.example.focusmate.data.local

import androidx.room.*
import com.example.focusmate.data.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Insert
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: Int): FocusSessionEntity?

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE sessionType = 'FOCUS' AND status = 'COMPLETED'")
    fun getCompletedFocusCount(): Flow<Int>
}