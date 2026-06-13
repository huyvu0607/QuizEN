package com.lumina.app.data.model

data class User(
    val id: Long,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val level: EnglishLevel,        // A1..C2
    val goal: String,               // VARCHAR: "IELTS", "TOEIC", v.v.
    val streakCount: Int = 0,
    val totalXp: Int = 0,
    val createdAt: Long             // timestamp millis
)

enum class EnglishLevel {
    A1, A2, B1, B2, C1, C2
}

data class UserBadge(
    val id: Long,
    val userId: Long,
    val badgeCode: String,          // "STREAK_7", "WORDS_100", v.v.
    val earnedAt: Long
)