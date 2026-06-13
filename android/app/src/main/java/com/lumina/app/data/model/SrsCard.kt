package com.lumina.app.data.model

data class SrsCard(
    val id: Long,
    val userId: Long,
    val vocabularyId: Long,
    val intervalDays: Int = 1,       // ngày đến lần ôn tiếp theo
    val easeFactor: Float = 2.5f,    // hệ số SM-2, min 1.3
    val repetition: Int = 0,          // số lần đã ôn thành công liên tiếp
    val nextReviewAt: Long,            // timestamp millis
    val lastReviewedAt: Long? = null
)

// Rating người dùng tự đánh giá sau mỗi flashcard
enum class SrsRating {
    FORGOT,       //  Chưa nhớ  -> interval reset
    HARD,         //  Mang máng -> interval × 1.2
    GOOD          //  Nhớ tốt   -> SM-2 chuẩn
}