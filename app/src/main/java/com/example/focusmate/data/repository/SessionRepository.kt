package com.example.focusmate.data.repository

import com.example.focusmate.data.entity.FocusSessionEntity
import com.example.focusmate.data.local.FocusSessionDao
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: FocusSessionDao) {
    suspend fun insertSession(session: FocusSessionEntity): Long = sessionDao.insertSession(session)
    suspend fun updateSession(session: FocusSessionEntity) = sessionDao.updateSession(session)
    fun getAllSessions(): Flow<List<FocusSessionEntity>> = sessionDao.getAllSessions()
    suspend fun getSessionById(sessionId: Int): FocusSessionEntity? = sessionDao.getSessionById(sessionId)
    fun getCompletedFocusCount(): Flow<Int> = sessionDao.getCompletedFocusCount()
}