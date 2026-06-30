package com.lumina.app.ui.profile

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val level: String = "A1",
    val streakDays: Int = 0,
    val totalWords: Int = 0,
    val learningDays: Int = 0,
    val totalXp: Int = 0,
    val xpGoal: Int = 50,
    val isNotificationsEnabled: Boolean = true
)
