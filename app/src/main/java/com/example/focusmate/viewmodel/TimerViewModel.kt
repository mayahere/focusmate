package com.example.focusmate.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusmate.data.entity.FocusSessionEntity
import com.example.focusmate.data.entity.SessionStatus
import com.example.focusmate.data.entity.SessionType
import com.example.focusmate.data.entity.TaskEntity
import com.example.focusmate.data.local.AppDatabase
import com.example.focusmate.data.repository.SessionRepository
import com.example.focusmate.data.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val Application.timerDataStore: DataStore<Preferences> by preferencesDataStore(name = "timer_state")

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.timerDataStore
    private val sessionRepository: SessionRepository
    private val taskRepository: TaskRepository

    companion object {
        const val DEFAULT_FOCUS_DURATION = 25 * 60       // 25 minutes
        const val DEFAULT_SHORT_BREAK_DURATION = 5 * 60   // 5 minutes
        const val DEFAULT_LONG_BREAK_DURATION = 15 * 60   // 15 minutes
        const val DEFAULT_SESSIONS_BEFORE_LONG_BREAK = 4

        // Keep old names for backward compatibility in FocusScreen
        const val FOCUS_DURATION = DEFAULT_FOCUS_DURATION
        const val SHORT_BREAK_DURATION = DEFAULT_SHORT_BREAK_DURATION
        const val LONG_BREAK_DURATION = DEFAULT_LONG_BREAK_DURATION
        const val SESSIONS_BEFORE_LONG_BREAK = DEFAULT_SESSIONS_BEFORE_LONG_BREAK

        private val KEY_SESSION_TYPE = stringPreferencesKey("session_type")
        private val KEY_REMAINING_SECONDS = intPreferencesKey("remaining_seconds")
        private val KEY_IS_RUNNING = booleanPreferencesKey("is_running")
        private val KEY_END_TIME = longPreferencesKey("end_time")
        private val KEY_TASK_ID = intPreferencesKey("task_id")
        private val KEY_SESSION_ID = intPreferencesKey("session_id")
        private val KEY_COMPLETED_FOCUS_COUNT = intPreferencesKey("completed_focus_count")
        private val KEY_TASK_COMPLETED_SESSIONS = intPreferencesKey("task_completed_sessions")
        private val KEY_TASK_COMPLETED_SESSIONS_TASK_ID = intPreferencesKey("task_completed_sessions_task_id")
        private val KEY_CUSTOM_FOCUS_DURATION = intPreferencesKey("custom_focus_duration")
        private val KEY_CUSTOM_SHORT_BREAK_DURATION = intPreferencesKey("custom_short_break_duration")
        private val KEY_CUSTOM_LONG_BREAK_DURATION = intPreferencesKey("custom_long_break_duration")
        private val KEY_CUSTOM_SESSIONS_BEFORE_LONG_BREAK = intPreferencesKey("custom_sessions_before_long_break")
        private val KEY_AUTO_START_BREAKS = booleanPreferencesKey("auto_start_breaks")
        private val KEY_AUTO_START_FOCUS = booleanPreferencesKey("auto_start_focus")
    }

    private val _sessionType = MutableStateFlow(SessionType.FOCUS)
    val sessionType: StateFlow<SessionType> = _sessionType.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(FOCUS_DURATION)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _selectedTask = MutableStateFlow<TaskEntity?>(null)
    val selectedTask: StateFlow<TaskEntity?> = _selectedTask.asStateFlow()

    private val _completedFocusCount = MutableStateFlow(0)
    val completedFocusCount: StateFlow<Int> = _completedFocusCount.asStateFlow()

    private val _taskCompletedSessions = MutableStateFlow(0)
    val taskCompletedSessions: StateFlow<Int> = _taskCompletedSessions.asStateFlow()

    private val _timerFinished = MutableSharedFlow<SessionType>()
    val timerFinished: SharedFlow<SessionType> = _timerFinished.asSharedFlow()

    private val _suggestedNextSession = MutableStateFlow<SessionType?>(null)
    val suggestedNextSession: StateFlow<SessionType?> = _suggestedNextSession.asStateFlow()

    private var timerJob: Job? = null
    private var currentSessionId: Int? = null

    private var _focusDuration = DEFAULT_FOCUS_DURATION
    private var _shortBreakDuration = DEFAULT_SHORT_BREAK_DURATION
    private var _longBreakDuration = DEFAULT_LONG_BREAK_DURATION
    private var _sessionsBeforeLongBreak = DEFAULT_SESSIONS_BEFORE_LONG_BREAK
    private var _autoStartBreaks = true
    private var _autoStartFocus = false

    val autoStartBreaks: Boolean get() = _autoStartBreaks
    val autoStartFocus: Boolean get() = _autoStartFocus

    val focusDuration: Int get() = _focusDuration
    val shortBreakDuration: Int get() = _shortBreakDuration
    val longBreakDuration: Int get() = _longBreakDuration
    val sessionsBeforeLongBreak: Int get() = _sessionsBeforeLongBreak

    val totalDuration: Int
        get() = getDurationForType(_sessionType.value)

    init {
        val db = AppDatabase.getInstance(application)
        sessionRepository = SessionRepository(db.focusSessionDao())
        taskRepository = TaskRepository(db.taskDao())
        restoreState()
    }

    private fun getDurationForType(type: SessionType): Int = when (type) {
        SessionType.FOCUS -> _focusDuration
        SessionType.SHORT_BREAK -> _shortBreakDuration
        SessionType.LONG_BREAK -> _longBreakDuration
    }

    private fun restoreState() {
        viewModelScope.launch {
            dataStore.data.first().let { prefs ->
                // Restore custom durations first
                _focusDuration = prefs[KEY_CUSTOM_FOCUS_DURATION] ?: DEFAULT_FOCUS_DURATION
                _shortBreakDuration = prefs[KEY_CUSTOM_SHORT_BREAK_DURATION] ?: DEFAULT_SHORT_BREAK_DURATION
                _longBreakDuration = prefs[KEY_CUSTOM_LONG_BREAK_DURATION] ?: DEFAULT_LONG_BREAK_DURATION
                _sessionsBeforeLongBreak = prefs[KEY_CUSTOM_SESSIONS_BEFORE_LONG_BREAK] ?: DEFAULT_SESSIONS_BEFORE_LONG_BREAK
                _autoStartBreaks = prefs[KEY_AUTO_START_BREAKS] ?: true
                _autoStartFocus = prefs[KEY_AUTO_START_FOCUS] ?: false

                val typeStr = prefs[KEY_SESSION_TYPE]
                val wasRunning = prefs[KEY_IS_RUNNING] ?: false
                val endTime = prefs[KEY_END_TIME] ?: 0L
                val savedRemaining = prefs[KEY_REMAINING_SECONDS] ?: _focusDuration
                val taskId = prefs[KEY_TASK_ID]
                val sessionId = prefs[KEY_SESSION_ID]
                val focusCount = prefs[KEY_COMPLETED_FOCUS_COUNT] ?: 0

                _completedFocusCount.value = focusCount

                // Restore task-specific completed sessions
                val savedTaskSessionsTaskId = prefs[KEY_TASK_COMPLETED_SESSIONS_TASK_ID] ?: 0
                val savedTaskSessions = prefs[KEY_TASK_COMPLETED_SESSIONS] ?: 0

                if (typeStr != null) {
                    _sessionType.value = SessionType.valueOf(typeStr)
                }

                if (taskId != null && taskId > 0) {
                    _selectedTask.value = taskRepository.getTaskById(taskId)
                    // Restore per-task count only if it matches the current task
                    if (taskId == savedTaskSessionsTaskId) {
                        _taskCompletedSessions.value = savedTaskSessions
                    } else {
                        _taskCompletedSessions.value = 0
                    }
                } else {
                    _taskCompletedSessions.value = 0
                }

                if (sessionId != null) {
                    currentSessionId = sessionId
                }

                if (wasRunning && endTime > 0) {
                    val now = System.currentTimeMillis()
                    val remaining = ((endTime - now) / 1000).toInt()
                    if (remaining > 0) {
                        _remainingSeconds.value = remaining
                        _isRunning.value = true
                        startCountdown()
                    } else {
                        // Timer expired while app was closed
                        _remainingSeconds.value = 0
                        onTimerComplete()
                    }
                } else if (!wasRunning && savedRemaining > 0 && savedRemaining < getDurationForType(_sessionType.value)) {
                    // Was paused
                    _remainingSeconds.value = savedRemaining
                    _isPaused.value = true
                } else {
                    _remainingSeconds.value = getDurationForType(_sessionType.value)
                }
            }
        }
    }

    fun selectSessionType(type: SessionType) {
        if (_isRunning.value || _isPaused.value) return
        _sessionType.value = type
        _remainingSeconds.value = getDurationForType(type)
        _suggestedNextSession.value = null
        saveState()
    }

    fun selectTask(task: TaskEntity?) {
        val previousTaskId = _selectedTask.value?.taskId
        if (task?.taskId != previousTaskId) {
            // Cancel running/paused session and reset timer when switching tasks
            if (_isRunning.value || _isPaused.value) {
                timerJob?.cancel()
                viewModelScope.launch {
                    currentSessionId?.let { id ->
                        sessionRepository.getSessionById(id)?.let { session ->
                            if (session.status != SessionStatus.COMPLETED) {
                                val elapsed = getDurationForType(_sessionType.value) - _remainingSeconds.value
                                sessionRepository.updateSession(
                                    session.copy(
                                        status = SessionStatus.CANCELLED,
                                        actualDuration = elapsed,
                                        endedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    }
                    currentSessionId = null
                }
                _isRunning.value = false
                _isPaused.value = false
            }
            _sessionType.value = SessionType.FOCUS
            _remainingSeconds.value = getDurationForType(SessionType.FOCUS)
            _suggestedNextSession.value = null
            _taskCompletedSessions.value = 0
        }
        _selectedTask.value = task
        saveState()
    }

    fun clearLinkedTask() {
        if (_isRunning.value || _isPaused.value) {
            timerJob?.cancel()
            viewModelScope.launch {
                currentSessionId?.let { id ->
                    sessionRepository.getSessionById(id)?.let { session ->
                        if (session.status != SessionStatus.COMPLETED) {
                            val elapsed = getDurationForType(_sessionType.value) - _remainingSeconds.value
                            sessionRepository.updateSession(
                                session.copy(
                                    status = SessionStatus.CANCELLED,
                                    actualDuration = elapsed,
                                    endedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
                currentSessionId = null
            }
            _isRunning.value = false
            _isPaused.value = false
        }
        _selectedTask.value = null
        _sessionType.value = SessionType.FOCUS
        _remainingSeconds.value = getDurationForType(SessionType.FOCUS)
        _suggestedNextSession.value = null
        _taskCompletedSessions.value = 0
        saveState()
    }

    fun updateDurations(
        focusMinutes: Int,
        shortBreakMinutes: Int,
        longBreakMinutes: Int,
        sessionsBeforeLongBreak: Int,
        autoStartBreaks: Boolean,
        autoStartFocus: Boolean
    ) {
        _focusDuration = focusMinutes * 60
        _shortBreakDuration = shortBreakMinutes * 60
        _longBreakDuration = longBreakMinutes * 60
        _sessionsBeforeLongBreak = sessionsBeforeLongBreak
        _autoStartBreaks = autoStartBreaks
        _autoStartFocus = autoStartFocus

        // Reset timer to new duration if not running
        if (!_isRunning.value && !_isPaused.value) {
            _remainingSeconds.value = getDurationForType(_sessionType.value)
        }

        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_CUSTOM_FOCUS_DURATION] = _focusDuration
                prefs[KEY_CUSTOM_SHORT_BREAK_DURATION] = _shortBreakDuration
                prefs[KEY_CUSTOM_LONG_BREAK_DURATION] = _longBreakDuration
                prefs[KEY_CUSTOM_SESSIONS_BEFORE_LONG_BREAK] = _sessionsBeforeLongBreak
                prefs[KEY_AUTO_START_BREAKS] = _autoStartBreaks
                prefs[KEY_AUTO_START_FOCUS] = _autoStartFocus
            }
            saveState()
        }
    }

    fun start() {
        if (_isRunning.value) return

        viewModelScope.launch {
            if (!_isPaused.value) {
                // New session
                val session = FocusSessionEntity(
                    taskId = _selectedTask.value?.taskId,
                    sessionType = _sessionType.value,
                    plannedDuration = getDurationForType(_sessionType.value),
                    startedAt = System.currentTimeMillis(),
                    status = SessionStatus.RUNNING
                )
                currentSessionId = sessionRepository.insertSession(session).toInt()
            } else {
                // Resume from pause
                currentSessionId?.let { id ->
                    sessionRepository.getSessionById(id)?.let { session ->
                        sessionRepository.updateSession(session.copy(status = SessionStatus.RUNNING))
                    }
                }
            }
            _isRunning.value = true
            _isPaused.value = false
            _suggestedNextSession.value = null
            saveState()
            startCountdown()
        }
    }

    fun pause() {
        if (!_isRunning.value) return
        timerJob?.cancel()
        _isRunning.value = false
        _isPaused.value = true

        viewModelScope.launch {
            currentSessionId?.let { id ->
                sessionRepository.getSessionById(id)?.let { session ->
                    sessionRepository.updateSession(session.copy(status = SessionStatus.PAUSED))
                }
            }
            saveState()
        }
    }

    fun reset() {
        timerJob?.cancel()
        val wasRunning = _isRunning.value || _isPaused.value
        _isRunning.value = false
        _isPaused.value = false
        _remainingSeconds.value = getDurationForType(_sessionType.value)
        _suggestedNextSession.value = null

        if (wasRunning) {
            viewModelScope.launch {
                currentSessionId?.let { id ->
                    sessionRepository.getSessionById(id)?.let { session ->
                        val elapsed = getDurationForType(_sessionType.value) - _remainingSeconds.value
                        sessionRepository.updateSession(
                            session.copy(
                                status = SessionStatus.CANCELLED,
                                actualDuration = elapsed,
                                endedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
                currentSessionId = null
                saveState()
            }
        }
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val endTime = System.currentTimeMillis() + (_remainingSeconds.value * 1000L)
            saveEndTime(endTime)

            while (_remainingSeconds.value > 0 && _isRunning.value) {
                delay(1000)
                val now = System.currentTimeMillis()
                val remaining = ((endTime - now) / 1000).toInt().coerceAtLeast(0)
                _remainingSeconds.value = remaining
            }

            if (_remainingSeconds.value <= 0 && _isRunning.value) {
                onTimerComplete()
            }
        }
    }

    private suspend fun onTimerComplete() {
        _isRunning.value = false
        _isPaused.value = false
        _remainingSeconds.value = 0

        currentSessionId?.let { id ->
            sessionRepository.getSessionById(id)?.let { session ->
                if (session.status != SessionStatus.COMPLETED) {
                    sessionRepository.updateSession(
                        session.copy(
                            status = SessionStatus.COMPLETED,
                            actualDuration = session.plannedDuration,
                            endedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
        currentSessionId = null

        val finishedType = _sessionType.value

        if (finishedType == SessionType.FOCUS) {
            _completedFocusCount.value++
            if (_selectedTask.value != null) {
                _taskCompletedSessions.value++
            }

            // Check if current task completed all its focus sessions
            val taskDone = _selectedTask.value?.let { task ->
                _taskCompletedSessions.value >= task.focusSessions
            } ?: false

            if (taskDone) {
                // Mark task as completed
                _selectedTask.value?.let { task ->
                    val completedTask = task.copy(
                        status = com.example.focusmate.data.entity.TaskStatus.COMPLETED,
                        completedAt = System.currentTimeMillis()
                    )
                    taskRepository.updateTask(completedTask)

                    if (_autoStartBreaks) {
                        // Start break, then auto-switch task after break
                        val nextType = if (_completedFocusCount.value % _sessionsBeforeLongBreak == 0) {
                            SessionType.LONG_BREAK
                        } else {
                            SessionType.SHORT_BREAK
                        }
                        // Switch to next task now so after break it's ready
                        val nextTask = taskRepository.getNextActiveTask(task.taskId)
                        _selectedTask.value = nextTask
                        _taskCompletedSessions.value = 0

                        _sessionType.value = nextType
                        _remainingSeconds.value = getDurationForType(nextType)
                        _suggestedNextSession.value = null
                        saveState()
                        _timerFinished.emit(finishedType)
                        start()
                        return
                    } else {
                        // No auto-start: switch task and suggest break
                        val nextTask = taskRepository.getNextActiveTask(task.taskId)
                        _selectedTask.value = nextTask
                        _taskCompletedSessions.value = 0

                        val nextType = if (_completedFocusCount.value % _sessionsBeforeLongBreak == 0) {
                            SessionType.LONG_BREAK
                        } else {
                            SessionType.SHORT_BREAK
                        }
                        _suggestedNextSession.value = nextType
                    }
                }
            } else {
                val nextType = if (_completedFocusCount.value % _sessionsBeforeLongBreak == 0) {
                    SessionType.LONG_BREAK
                } else {
                    SessionType.SHORT_BREAK
                }
                if (_autoStartBreaks) {
                    _sessionType.value = nextType
                    _remainingSeconds.value = getDurationForType(nextType)
                    _suggestedNextSession.value = null
                    saveState()
                    _timerFinished.emit(finishedType)
                    start()
                    return
                } else {
                    _suggestedNextSession.value = nextType
                }
            }
        } else {
            // Break finished
            if (_autoStartFocus) {
                _sessionType.value = SessionType.FOCUS
                _remainingSeconds.value = getDurationForType(SessionType.FOCUS)
                _suggestedNextSession.value = null
                saveState()
                _timerFinished.emit(finishedType)
                start()
                return
            } else {
                _suggestedNextSession.value = SessionType.FOCUS
            }
        }

        saveState()
        _timerFinished.emit(finishedType)
    }

    fun acceptSuggestion() {
        _suggestedNextSession.value?.let { nextType ->
            _sessionType.value = nextType
            _remainingSeconds.value = getDurationForType(nextType)
            _suggestedNextSession.value = null
            saveState()
        }
    }

    fun skipToNext() {
        timerJob?.cancel()

        viewModelScope.launch {
            // Cancel current session if running/paused
            currentSessionId?.let { id ->
                sessionRepository.getSessionById(id)?.let { session ->
                    if (session.status != SessionStatus.COMPLETED) {
                        val elapsed = getDurationForType(_sessionType.value) - _remainingSeconds.value
                        sessionRepository.updateSession(
                            session.copy(
                                status = SessionStatus.CANCELLED,
                                actualDuration = elapsed,
                                endedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
            currentSessionId = null
            _isRunning.value = false
            _isPaused.value = false

            // Determine next session type
            val currentType = _sessionType.value
            val nextType = if (currentType == SessionType.FOCUS) {
                if ((_completedFocusCount.value + 1) % _sessionsBeforeLongBreak == 0) {
                    SessionType.LONG_BREAK
                } else {
                    SessionType.SHORT_BREAK
                }
            } else {
                SessionType.FOCUS
            }

            _sessionType.value = nextType
            _remainingSeconds.value = getDurationForType(nextType)
            _suggestedNextSession.value = null
            saveState()
        }
    }

    private fun saveState() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_SESSION_TYPE] = _sessionType.value.name
                prefs[KEY_REMAINING_SECONDS] = _remainingSeconds.value
                prefs[KEY_IS_RUNNING] = _isRunning.value
                prefs[KEY_TASK_ID] = _selectedTask.value?.taskId ?: 0
                prefs[KEY_SESSION_ID] = currentSessionId ?: 0
                prefs[KEY_COMPLETED_FOCUS_COUNT] = _completedFocusCount.value
                prefs[KEY_TASK_COMPLETED_SESSIONS] = _taskCompletedSessions.value
                prefs[KEY_TASK_COMPLETED_SESSIONS_TASK_ID] = _selectedTask.value?.taskId ?: 0
                if (!_isRunning.value) {
                    prefs.remove(KEY_END_TIME)
                }
            }
        }
    }

    private suspend fun saveEndTime(endTime: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_END_TIME] = endTime
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}