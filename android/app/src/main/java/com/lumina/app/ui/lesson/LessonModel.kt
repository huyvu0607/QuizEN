package com.lumina.app.ui.lesson

enum class LessonStatus {
    NOT_STARTED, LEARNING, COMPLETED
}

data class LessonUiItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val status: LessonStatus = LessonStatus.NOT_STARTED,
    val progress: Int = 0, // 0-100
    val iconRes: Int? = null,
    val wordCount: Int = 0
)
