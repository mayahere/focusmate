package com.example.focusmate.ui.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusmate.data.entity.SessionType
import com.example.focusmate.data.entity.TaskEntity
import com.example.focusmate.data.entity.TaskStatus
import com.example.focusmate.ui.theme.*
import com.example.focusmate.viewmodel.TaskViewModel
import com.example.focusmate.viewmodel.TimerViewModel

@Composable
fun FocusScreen(
    timerViewModel: TimerViewModel,
    taskViewModel: TaskViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null
) {
    val sessionType by timerViewModel.sessionType.collectAsState()
    val remainingSeconds by timerViewModel.remainingSeconds.collectAsState()
    val isRunning by timerViewModel.isRunning.collectAsState()
    val isPaused by timerViewModel.isPaused.collectAsState()
    val selectedTask by timerViewModel.selectedTask.collectAsState()
    val completedCount by timerViewModel.completedFocusCount.collectAsState()
    val taskCompletedSessions by timerViewModel.taskCompletedSessions.collectAsState()
    val suggestedNext by timerViewModel.suggestedNextSession.collectAsState()
    val tasks by taskViewModel.tasks.collectAsState()

    var showTaskPicker by remember { mutableStateOf(false) }

    val totalDuration = timerViewModel.totalDuration
    val progress = if (totalDuration > 0) remainingSeconds.toFloat() / totalDuration else 1f

    // Task-specific: show "Phiên X / task.focusSessions" when a task is selected
    // Global fallback: show Pomodoro cycle counter
    val sessionNumber: Int
    val sessionTotal: Int
    if (selectedTask != null) {
        sessionNumber = taskCompletedSessions + 1
        sessionTotal = selectedTask!!.focusSessions
    } else {
        sessionNumber = (completedCount % timerViewModel.sessionsBeforeLongBreak) + 1
        sessionTotal = timerViewModel.sessionsBeforeLongBreak
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CreamCard)
                    .clickable { onNavigateBack?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Trở về",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Phiên tập trung",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CreamCard)
                    .clickable { onOpenSettings?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Cài đặt",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Current Target card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            onClick = {
                if (!isRunning && !isPaused) showTaskPicker = true
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FocusBadgeMint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nhiệm vụ hiện tại",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = selectedTask?.title ?: "Chưa chọn nhiệm vụ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (selectedTask != null) TextPrimary else TextTertiary
                    )
                }
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Chọn nhiệm vụ",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Session type tabs
        SessionTypeTabs(
            selected = sessionType,
            onSelect = { timerViewModel.selectSessionType(it) },
            enabled = !isRunning && !isPaused
        )

        // Timer circle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                TimerCircle(
                    progress = progress,
                    sessionType = sessionType,
                    modifier = Modifier.fillMaxSize()
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Session type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (sessionType) {
                                    SessionType.FOCUS -> FocusBadgeMint
                                    SessionType.SHORT_BREAK -> PriorityMediumBg
                                    SessionType.LONG_BREAK -> Color(0xFFE0E7FF)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (sessionType) {
                                            SessionType.FOCUS -> PrimaryGreen
                                            SessionType.SHORT_BREAK -> PriorityMedium
                                            SessionType.LONG_BREAK -> Color(0xFF5B6ABF)
                                        }
                                    )
                            )
                            Text(
                                text = when (sessionType) {
                                    SessionType.FOCUS -> "TẬP TRUNG"
                                    SessionType.SHORT_BREAK -> "NGHỈ NGẮN"
                                    SessionType.LONG_BREAK -> "NGHỈ DÀI"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                color = when (sessionType) {
                                    SessionType.FOCUS -> FocusBadgeText
                                    SessionType.SHORT_BREAK -> PriorityMedium
                                    SessionType.LONG_BREAK -> Color(0xFF5B6ABF)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatTime(remainingSeconds),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 60.sp,
                            letterSpacing = 2.sp
                        ),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Phiên $sessionNumber / $sessionTotal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Suggested next session
        suggestedNext?.let { next ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FocusBadgeMint)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bắt đầu ${
                            when (next) {
                                SessionType.FOCUS -> "tập trung"
                                SessionType.SHORT_BREAK -> "nghỉ ngắn"
                                SessionType.LONG_BREAK -> "nghỉ dài"
                            }
                        }?",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = FocusBadgeText
                    )
                    Button(
                        onClick = { timerViewModel.acceptSuggestion() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Bắt đầu")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CreamCard)
                    .border(1.dp, DividerColor, CircleShape)
                    .clickable(enabled = isRunning || isPaused) { timerViewModel.reset() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Replay,
                    contentDescription = "Đặt lại",
                    tint = if (isRunning || isPaused) TextPrimary else TextTertiary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Play / Pause button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AccentBrown)
                    .clickable {
                        if (isRunning) timerViewModel.pause() else timerViewModel.start()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Tạm dừng" else "Bắt đầu",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Skip / Next button - always enabled when running, paused, or has suggestion
            val skipEnabled = isRunning || isPaused || suggestedNext != null
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CreamCard)
                    .border(1.dp, DividerColor, CircleShape)
                    .clickable(enabled = skipEnabled) {
                        if (suggestedNext != null) {
                            timerViewModel.acceptSuggestion()
                        } else {
                            timerViewModel.skipToNext()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Chuyển phiên",
                    tint = if (skipEnabled) TextPrimary else TextTertiary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    if (showTaskPicker) {
        TaskPickerDialog(
            tasks = tasks.filter { it.status != TaskStatus.COMPLETED },
            onSelect = {
                timerViewModel.selectTask(it)
                showTaskPicker = false
            },
            onClear = {
                timerViewModel.clearLinkedTask()
                showTaskPicker = false
            },
            onDismiss = { showTaskPicker = false }
        )
    }
}

@Composable
private fun TimerCircle(
    progress: Float,
    sessionType: SessionType,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    val ringColor = when (sessionType) {
        SessionType.FOCUS -> TimerRingGreen
        SessionType.SHORT_BREAK -> PriorityMedium
        SessionType.LONG_BREAK -> Color(0xFF5B6ABF)
    }
    val trackColor = DividerColor

    Canvas(modifier = modifier) {
        val strokeWidth = 10.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawArc(
            color = ringColor,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun SessionTypeTabs(
    selected: SessionType,
    onSelect: (SessionType) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(TabUnselected)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        SessionType.entries.forEach { type ->
            val isSelected = selected == type
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(
                        if (isSelected) Modifier.background(TabSelected)
                        else Modifier
                    )
                    .clickable(enabled = enabled) { onSelect(type) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = when (type) {
                        SessionType.FOCUS -> "Tập trung"
                        SessionType.SHORT_BREAK -> "Nghỉ ngắn"
                        SessionType.LONG_BREAK -> "Nghỉ dài"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isSelected) Color.White else TabTextUnselected
                )
            }
        }
    }
}

@Composable
private fun TaskPickerDialog(
    tasks: List<TaskEntity>,
    onSelect: (TaskEntity) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CreamSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(FocusBadgeMint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Chọn nhiệm vụ",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClear() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Không chọn (Tập trung tự do)", color = TextSecondary)
                }

                HorizontalDivider(color = DividerColor)

                if (tasks.isEmpty()) {
                    Text(
                        "Không có nhiệm vụ nào đang thực hiện.",
                        textAlign = TextAlign.Center,
                        color = TextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(tasks) { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelect(task) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, PrimaryGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = TextSecondary)
            }
        }
    )
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}