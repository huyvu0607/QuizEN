package com.lumina.app.data.model

data class QuizSession(
    val id: Long,
    val userId: Long,
    val scopeType: QuizScopeType,
    val scopeId: Long?,             // null khi WEAKEST hoặc MIXED
    val totalQuestions: Int,
    val correctCount: Int,
    val score: Int,                  // điểm %, 0–100
    val completedAt: Long
)

enum class QuizScopeType {
    LESSON,       // scopeId = lessonId
    TOPIC_GROUP,  // scopeId = topicGroupId
    MIXED,        // scopeId = null
    WEAKEST       // scopeId = null, AI lọc từ yếu nhất
}

data class QuizAnswer(
    val id: Long,
    val sessionId: Long,
    val vocabularyId: Long,
    val questionType: QuizQuestionType,
    val userAnswer: String?,         // thêm mới — để xem lại câu sai
    val isCorrect: Boolean,
    val timeTakenMs: Int             // thời gian trả lời (ms)
)

enum class QuizQuestionType {
    MULTIPLE_CHOICE,  // Dạng 1: trắc nghiệm
    FILL_IN_BLANK     // Dạng 2: điền từ vào câu
}