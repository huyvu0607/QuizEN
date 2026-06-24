package com.lumina.app.ui.courses

enum class UnitStatus {
    COMPLETED, IN_PROGRESS, LOCKED
}

data class UnitItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val status: UnitStatus
)
