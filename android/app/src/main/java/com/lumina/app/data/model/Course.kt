package com.lumina.app.data.model

data class Course(
    val id: Long,
    val userId: Long,
    val title: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val coverColor: String? = null,  // hex, e.g. "#2563EB"
    val coverIcon: String? = null,   // icon key, e.g. "book"
    val level: EnglishLevel? = null,
    val createdAt: Long
)