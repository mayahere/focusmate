package com.example.focusmate.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusmate.ui.theme.*
import com.example.focusmate.viewmodel.TimerViewModel

@Composable
fun SetUpPomodoroScreen(
    timerViewModel: TimerViewModel,
    onNavigateBack: () -> Unit
) {
    var focusMinutes by remember { mutableIntStateOf(timerViewModel.focusDuration / 60) }
    var shortBreakMinutes by remember { mutableIntStateOf(timerViewModel.shortBreakDuration / 60) }
    var longBreakMinutes by remember { mutableIntStateOf(timerViewModel.longBreakDuration / 60) }
    var sessionsBeforeLongBreak by remember { mutableIntStateOf(timerViewModel.sessionsBeforeLongBreak) }
    var autoStartBreaks by remember { mutableStateOf(timerViewModel.autoStartBreaks) }
    var autoStartFocus by remember { mutableStateOf(timerViewModel.autoStartFocus) }

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
                    .clickable { onNavigateBack() },
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
                text = "Chỉnh Pomodoro",
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
                    .clickable {
                        focusMinutes = 25
                        shortBreakMinutes = 5
                        longBreakMinutes = 15
                        sessionsBeforeLongBreak = 4
                        autoStartBreaks = true
                        autoStartFocus = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Replay,
                    contentDescription = "Đặt lại",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen)
                    .clickable {
                        timerViewModel.updateDurations(
                            focusMinutes = focusMinutes,
                            shortBreakMinutes = shortBreakMinutes,
                            longBreakMinutes = longBreakMinutes,
                            sessionsBeforeLongBreak = sessionsBeforeLongBreak,
                            autoStartBreaks = autoStartBreaks,
                            autoStartFocus = autoStartFocus
                        )
                        onNavigateBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Áp dụng",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Section: Interval Durations
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thời lượng các phiên",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    ),
                    color = TextSecondary
                )
                Text(
                    text = "Nhấn +/- để điều chỉnh",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            DurationRow(
                icon = Icons.Default.Timer,
                iconBg = FocusBadgeMint,
                iconTint = PrimaryGreen,
                title = "Phiên tập trung",
                subtitle = "",
                value = focusMinutes,
                unit = "phút",
                onDecrement = { if (focusMinutes > 1) focusMinutes-- },
                onIncrement = { if (focusMinutes < 120) focusMinutes++ }
            )

            Spacer(modifier = Modifier.height(8.dp))

            DurationRow(
                icon = Icons.Default.Coffee,
                iconBg = PriorityMediumBg,
                iconTint = PriorityMedium,
                title = "Nghỉ ngắn",
                subtitle = "Thư giãn mắt và tư thế nào!",
                value = shortBreakMinutes,
                unit = "phút",
                onDecrement = { if (shortBreakMinutes > 1) shortBreakMinutes-- },
                onIncrement = { if (shortBreakMinutes < 30) shortBreakMinutes++ }
            )

            Spacer(modifier = Modifier.height(8.dp))

            DurationRow(
                icon = Icons.Default.SelfImprovement,
                iconBg = Color(0xFFE0E7FF),
                iconTint = Color(0xFF5B6ABF),
                title = "Nghỉ dài",
                subtitle = "Nghỉ ngơi phục hồi năng lượng nhé!",
                value = longBreakMinutes,
                unit = "phút",
                onDecrement = { if (longBreakMinutes > 1) longBreakMinutes-- },
                onIncrement = { if (longBreakMinutes < 60) longBreakMinutes++ }
            )

            Spacer(modifier = Modifier.height(8.dp))

            DurationRow(
                icon = Icons.Default.Repeat,
                iconBg = PriorityLowBg,
                iconTint = PriorityLow,
                title = "Chu kỳ nghỉ dài",
                subtitle = "Số phiên trước khi nghỉ dài",
                value = sessionsBeforeLongBreak,
                unit = "phiên",
                onDecrement = { if (sessionsBeforeLongBreak > 2) sessionsBeforeLongBreak-- },
                onIncrement = { if (sessionsBeforeLongBreak < 8) sessionsBeforeLongBreak++ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TỰ ĐỘNG HÓA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    fontSize = 11.sp
                ),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            ToggleRow(
                title = "Tự động nghỉ",
                subtitle = "Tự chuyển sang nghỉ khi hết phiên tập trung",
                checked = autoStartBreaks,
                onToggle = { autoStartBreaks = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ToggleRow(
                title = "Tự động tập trung",
                subtitle = "Tự bắt đầu phiên tập trung tiếp theo",
                checked = autoStartFocus,
                onToggle = { autoStartFocus = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DurationRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    value: Int,
    unit: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
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
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextTertiary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CreamCard)
                        .border(1.dp, DividerColor, CircleShape)
                        .clickable { onDecrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "-",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                }

                Text(
                    text = "$value",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = TextPrimary,
                    modifier = Modifier.widthIn(min = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CreamCard)
                        .border(1.dp, DividerColor, CircleShape)
                        .clickable { onIncrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "+",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextTertiary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = PrimaryGreen,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = DividerColor,
                    uncheckedThumbColor = Color.White
                )
            )
        }
    }
}