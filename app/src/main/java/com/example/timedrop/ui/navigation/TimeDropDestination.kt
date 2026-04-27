package com.example.timedrop.ui.navigation

sealed class TimeDropDestination(val route: String) {
    data object Home : TimeDropDestination("home")
    data object Pomodoro : TimeDropDestination("pomodoro")
    data object Stopwatch : TimeDropDestination("stopwatch")
    data object WorldClock : TimeDropDestination("world_clock")
    data object Music : TimeDropDestination("music")
    data object Settings : TimeDropDestination("settings")
    data object Calendar : TimeDropDestination("calendar")
    data object SignUp : TimeDropDestination("signup")
    data object Login : TimeDropDestination("login")
    data object Notes : TimeDropDestination("notes")
    data object NoteEditor : TimeDropDestination("note_editor/{noteId}") {
        fun createRoute(noteId: Int) = "note_editor/$noteId"
    }
}

