package com.lumina.app.ui.unit

enum class UnitStatus {
    COMPLETED, IN_PROGRESS, LOCKED
}

data class UnitItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val status: UnitStatus,
    val wordCount: Int = 0,
    val lessonCount: Int = 0
)
