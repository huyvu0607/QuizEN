// QuizSessionEntity.kt
package com.lumina.app.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_sessions",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class QuizSessionEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "user_id", index = true) val userId: Long,
    @ColumnInfo(name = "scope_type") val scopeType: String,   // "LESSON", "TOPIC_GROUP"...
    @ColumnInfo(name = "scope_id") val scopeId: Long? = null,
    @ColumnInfo(name = "total_questions") val totalQuestions: Int,
    @ColumnInfo(name = "correct_count") val correctCount: Int,
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "completed_at") val completedAt: Long
)

@Entity(
    tableName = "quiz_answers",
    foreignKeys = [
        ForeignKey(
            entity = QuizSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["vocabulary_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuizAnswerEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "session_id", index = true) val sessionId: Long,
    @ColumnInfo(name = "vocabulary_id", index = true) val vocabularyId: Long,
    @ColumnInfo(name = "question_type") val questionType: String,  // "MULTIPLE_CHOICE", "FILL_IN_BLANK"
    @ColumnInfo(name = "user_answer") val userAnswer: String? = null,
    @ColumnInfo(name = "is_correct") val isCorrect: Boolean,
    @ColumnInfo(name = "time_taken_ms") val timeTakenMs: Int
)