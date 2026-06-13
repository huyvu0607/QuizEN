package com.lumina.app.data.model

data class Lesson(
    val id: Long,
    val unitId: Long,
    val title: String,
    val description: String? = null,
    val orderIndex: Int
)