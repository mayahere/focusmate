package com.example.focusmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.focusmate.ui.navigation.FocusMateApp
import com.example.focusmate.ui.theme.FocusMateTheme
import com.example.focusmate.viewmodel.TaskViewModel
import com.example.focusmate.viewmodel.TimerViewModel

class MainActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
    private val timerViewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusMateTheme {
                FocusMateApp(
                    taskViewModel = taskViewModel,
                    timerViewModel = timerViewModel
                )
            }
        }
    }
}