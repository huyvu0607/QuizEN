package com.lumina.app.ui.home

/**
 * Toàn bộ dữ liệu cần hiển thị trên màn Home gom vào 1 data class.
 * Khi BE/Room sẵn sàng, chỉ cần Repository trả đúng kiểu này là Fragment
 * tự cập nhật, không cần sửa UI.
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
    val courses: List<CourseCardState> = emptyList()
)

data class ContinueLearningState(
    val courseTitle: String,
    val level: String,
    val progressPercent: Int   // 0–100
)

data class CourseCardState(
    val id: String,
    val name: String,
    val wordsLearned: Int,
    val wordsTotal: Int,
    val iconRes: Int,           // R.drawable.ic_*
    val iconBgRes: Int,         // R.drawable.bg_icon_square_*
    val progressBgRes: Int      // R.drawable.bg_progress_fill_*
)