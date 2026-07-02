package com.lumina.app.data.model

data class StudyUnit(
    val id: Long,
    val courseId: Long,
    val title: String,
    val icon: String? = null,
    val orderIndex: Int
)