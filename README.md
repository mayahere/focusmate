# FocusMate

A personal task management and Pomodoro timer Android app. Manage your daily to-do list and stay focused with timed work sessions — all offline, no account required.

## Features

### To-Do List (F01)
- Create, edit, and delete tasks
- Set priority (Low / Medium / High) and due dates
- Mark tasks as completed
- Filter tasks: All, Active, Completed
- Link a task to a focus session

### Pomodoro Timer (F02)
- 25-minute focus sessions with short (5 min) and long (15 min) breaks
- Pause, resume, reset, and skip sessions
- Auto-suggests the next session type (break after focus, focus after break)
- Long break every 4 completed focus sessions
- Timer persists across app restarts
- Link a task to track what you're working on

## Screenshots

<!-- Add screenshots here -->

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM + Repository Pattern |
| Database | Room (SQLite) |
| State Persistence | DataStore (timer state) |
| Navigation | Jetpack Navigation Compose |
| Min SDK | 24 |
| Target SDK | 37 |

## Project Structure

```
com.example.focusmate/
├── data/
│   ├── entity/          # TaskEntity, FocusSessionEntity
│   ├── local/           # AppDatabase, TaskDao, FocusSessionDao
│   └── repository/      # TaskRepository, SessionRepository
├── ui/
│   ├── home/            # HomeScreen (task list)
│   ├── timer/           # FocusScreen, SetUpPomodoroScreen
│   ├── navigation/      # Bottom nav, routes
│   └── theme/           # Colors, typography, theme
├── viewmodel/           # TaskViewModel, TimerViewModel
└── MainActivity.kt
```

## Getting Started

1. Clone the repository
2. Open the project in Android Studio (Ladybug or newer recommended)
3. Sync Gradle and run on an emulator or device (API 24+)

## License

This project is for personal/educational use.