// SrsCardEntity.kt
package com.lumina.app.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "srs_records",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
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
data class SrsCardEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "user_id", index = true) val userId: Long,
    @ColumnInfo(name = "vocabulary_id", index = true) val vocabularyId: Long,
    @ColumnInfo(name = "interval_days") val intervalDays: Int = 1,
    @ColumnInfo(name = "ease_factor") val easeFactor: Float = 2.5f,
    @ColumnInfo(name = "repetition") val repetition: Int = 0,
    @ColumnInfo(name = "next_review_at") val nextReviewAt: Long,
    @ColumnInfo(name = "last_reviewed_at") val lastReviewedAt: Long? = null
)