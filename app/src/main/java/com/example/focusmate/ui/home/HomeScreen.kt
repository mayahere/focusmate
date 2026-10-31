package com.example.focusmate.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.focusmate.R
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusmate.data.entity.Priority
import com.example.focusmate.data.entity.TaskEntity
import com.example.focusmate.data.entity.TaskStatus
import com.example.focusmate.ui.theme.*
import com.example.focusmate.viewmodel.TaskFilter
import com.example.focusmate.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    taskViewModel: TaskViewModel,
    onStartFocus: (TaskEntity?) -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val currentFilter by taskViewModel.filter.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<TaskEntity?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                HomeHeader()
            }

            item {
                FilterRow(
                    currentFilter = currentFilter,
                    onFilterChange = { taskViewModel.setFilter(it) }
                )
            }

            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (currentFilter) {
                                TaskFilter.ALL -> "Chưa có nhiệm vụ nào.\nNhấn + để thêm mới!"
                                TaskFilter.ACTIVE -> "Không có nhiệm vụ đang thực hiện."
                                TaskFilter.COMPLETED -> "Chưa có nhiệm vụ nào hoàn thành."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(tasks, key = { it.taskId }) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { taskViewModel.toggleComplete(task) },
                        onEdit = { editingTask = task },
                        onDelete = { showDeleteConfirm = task },
                        onStartFocus = { onStartFocus(task) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(56.dp),
            containerColor = AccentBrown,
            contentColor = androidx.compose.ui.graphics.Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm nhiệm vụ")
        }
    }

    if (showAddDialog) {
        TaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, dueDate, priority, focusSessions ->
                taskViewModel.addTask(title, description, dueDate, priority, focusSessions)
                showAddDialog = false
            }
        )
    }

    editingTask?.let { task ->
        TaskDialog(
            task = task,
            onDismiss = { editingTask = null },
            onConfirm = { title, description, dueDate, priority, focusSessions ->
                taskViewModel.updateTask(
                    task.copy(
                        title = title,
                        description = description?.ifBlank { null },
                        dueDate = dueDate,
                        priority = priority,
                        focusSessions = focusSessions
                    )
                )
                editingTask = null
            },
            onDelete = {
                showDeleteConfirm = task
                editingTask = null
            }
        )
    }

    showDeleteConfirm?.let { task ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Xóa nhiệm vụ") },
            text = { Text("Bạn có chắc chắn muốn xóa \"${task.title}\"?") },
            containerColor = CreamSurface,
            confirmButton = {
                TextButton(onClick = {
                    taskViewModel.deleteTask(task)
                    showDeleteConfirm = null
                }) {
                    Text("Xóa", color = PriorityHigh)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun HomeHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "FocusMate",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "FocusMate",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "Nhiệm vụ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Nhiệm vụ của tôi",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = TextPrimary
        )
    }
}

@Composable
private fun FilterRow(currentFilter: TaskFilter, onFilterChange: (TaskFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskFilter.entries.forEach { filter ->
            val selected = currentFilter == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) TabSelected else TabUnselected)
                    .clickable { onFilterChange(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = when (filter) {
                        TaskFilter.ALL -> "Tất cả"
                        TaskFilter.ACTIVE -> "Đang làm"
                        TaskFilter.COMPLETED -> "Đã xong"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) androidx.compose.ui.graphics.Color.White else TabTextUnselected
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStartFocus: () -> Unit
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val indicatorColor = if (isCompleted) CompletedGreen else PrimaryGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .then(
                        if (isCompleted) {
                            Modifier.background(CompletedGreen)
                        } else {
                            Modifier.border(2.5.dp, indicatorColor, CircleShape)
                        }
                    )
                    .clickable { onToggleComplete() },
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Đã xong",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    color = if (isCompleted) TextSecondary else TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!task.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isCompleted) {
                    Text(
                        text = "Đã xong",
                        style = MaterialTheme.typography.labelSmall,
                        color = CompletedGreen
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PriorityBadge(priority = task.priority)

                        task.dueDate?.let { date ->
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    .format(Date(date)),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }

                        // Focus sessions count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${task.focusSessions} phiên",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Action buttons row
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Focus button - only show for non-completed tasks
                    if (!isCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FocusBadgeMint)
                                .clickable { onStartFocus() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayCircleFilled,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Tập trung",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = FocusBadgeText
                                )
                            }
                        }
                    }

                    // Edit button - only show for non-completed tasks
                    if (!isCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PriorityLowBg)
                                .clickable { onEdit() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Sửa",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Delete button - always visible
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PriorityHighBg)
                            .clickable { onDelete() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = PriorityHigh,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Xóa",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = PriorityHigh
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: Priority) {
    val (bgColor, textColor, label) = when (priority) {
        Priority.HIGH -> Triple(PriorityHighBg, PriorityHigh, "Cao")
        Priority.MEDIUM -> Triple(PriorityMediumBg, PriorityMedium, "Trung bình")
        Priority.LOW -> Triple(PriorityLowBg, PriorityLow, "Thấp")
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        ),
        color = textColor,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun TaskDialog(
    task: TaskEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, dueDate: Long?, priority: Priority, focusSessions: Int) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: Priority.LOW) }
    var titleError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf(task?.dueDate) }
    var focusSessions by remember { mutableIntStateOf(task?.focusSessions ?: 1) }

    val isEdit = task != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CreamSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "FocusMate",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isEdit) "Chỉnh sửa nhiệm vụ" else "Nhiệm vụ mới",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Title
                Column {
                    Text(
                        text = "Nhiệm vụ *",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            titleError = false
                        },
                        placeholder = { Text("VD: Thiết kế kiến trúc hệ thống", color = TextTertiary) },
                        isError = titleError,
                        supportingText = if (titleError) {
                            { Text("Tiêu đề không được để trống") }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Description
                Column {
                    Text(
                        text = "MÔ TẢ (TÙY CHỌN)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Ghi chú, nội dung, kết quả cần đạt...", color = TextTertiary) },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Schedule
                Column {
                    Text(
                        text = "LỊCH TRÌNH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CreamCard)
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hạn hoàn thành",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = dueDate?.let {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                            } ?: "Chưa đặt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (dueDate != null) TextPrimary else TextTertiary
                        )
                        if (dueDate != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(PriorityLowBg)
                                    .clickable { dueDate = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("x", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                    }
                }

                // Priority
                Column {
                    Text(
                        text = "MỨC ĐỘ ƯU TIÊN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Priority.entries.forEach { p ->
                            val selected = priority == p
                            val (bg, fg) = when (p) {
                                Priority.LOW -> (if (selected) PriorityLow else PriorityLowBg) to
                                        (if (selected) androidx.compose.ui.graphics.Color.White else PriorityLow)
                                Priority.MEDIUM -> (if (selected) PriorityMedium else PriorityMediumBg) to
                                        (if (selected) androidx.compose.ui.graphics.Color.White else PriorityMedium)
                                Priority.HIGH -> (if (selected) PriorityHigh else PriorityHighBg) to
                                        (if (selected) androidx.compose.ui.graphics.Color.White else PriorityHigh)
                            }
                            val label = when (p) {
                                Priority.LOW -> "Thấp"
                                Priority.MEDIUM -> "Vừa"
                                Priority.HIGH -> "Cao"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(bg)
                                    .clickable { priority = p }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(fg)
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = fg
                                    )
                                }
                            }
                        }
                    }
                }

                // Focus Sessions stepper
                Column {
                    Text(
                        text = "PHIÊN TẬP TRUNG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CreamCard)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Phiên tập trung",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = TextPrimary
                            )
                        }
                        // Stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, DividerColor, CircleShape)
                                    .clickable { if (focusSessions > 1) focusSessions-- },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("-", fontWeight = FontWeight.Bold, color = TextSecondary)
                            }
                            Text(
                                text = "$focusSessions",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "phiên",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, DividerColor, CircleShape)
                                    .clickable { if (focusSessions < 10) focusSessions++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onConfirm(title, description.ifBlank { null }, dueDate, priority, focusSessions)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Lưu")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Xóa", color = PriorityHigh)
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Hủy", color = TextSecondary)
                }
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}