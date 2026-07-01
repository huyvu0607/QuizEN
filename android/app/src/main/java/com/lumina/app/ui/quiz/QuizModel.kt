package com.lumina.app.ui.quiz

import com.lumina.app.data.model.Vocabulary

enum class QuizType {
    MULTIPLE_CHOICE, // Dạng 1: Trắc nghiệm
    FILL_IN_THE_BLANK // Dạng 2: Điền từ vào câu
}

data class QuizQuestion(
    val id: Long,
    val type: QuizType,
    val questionText: String, // Từ vựng hoặc câu có gạch chân _____
    val correctAnswer: String,
    val options: List<String> = emptyList(), // Chỉ dành cho trắc nghiệm
    val vocabulary: Vocabulary, // Để cập nhật SRS sau khi trả lời
    val hint: String? = null
)

data class QuizResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeSpentSeconds: Int,
    val xpEarned: Int
)
