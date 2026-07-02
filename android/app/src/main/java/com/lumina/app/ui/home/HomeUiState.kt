package com.lumina.app.ui.home

/**
 * Toàn bộ dữ liệu cần hiển thị trên màn Home gom vào 1 data class.
 */
data class HomeUiState(
    val userName: String = "",
    val streakDays: Int = 0,
    val totalWords: Int = 0,
    val accuracyPercent: Int = 0,
    val currentXp: Int = 0,
    val goalXp: Int = 500,
    val continueLearning: ContinueLearningState? = null,
    val reviewWordsCount: Int = 0,
    val courses: List<CourseCardState> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val syncProgress: Int = 0 // 0-100
)

enum class SyncStatus {
    IDLE, SYNCING, PAUSED, COMPLETED, ERROR
}

data class ContinueLearningState(
    val courseTitle: String,
    val level: String,
    val progressPercent: Int,   // 0–100
    val iconRes: Int? = null,
    val coverColor: String? = null
)

data class CourseCardState(
    val id: String,
    val name: String,
    val wordsLearned: Int,
    val wordsTotal: Int,
    val iconRes: Int,           // R.drawable.ic_*
    val iconBgRes: Int,         // R.drawable.bg_icon_square_*
    val coverColor: String? = null,
    val progressBgRes: Int      // R.drawable.bg_progress_fill_*
)
