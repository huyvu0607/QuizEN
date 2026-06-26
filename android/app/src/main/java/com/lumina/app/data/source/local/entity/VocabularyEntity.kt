// VocabularyEntity.kt
package com.lumina.app.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "vocabulary",
    foreignKeys = [ForeignKey(
        entity = LessonEntity::class,
        parentColumns = ["id"],
        childColumns = ["lesson_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "lesson_id", index = true) val lessonId: Long,
    @ColumnInfo(name = "word") val word: String,
    @ColumnInfo(name = "meaning") val meaning: String,
    @ColumnInfo(name = "example_sentence") val exampleSentence: String? = null,
    @ColumnInfo(name = "ipa") val ipa: String? = null,
    @ColumnInfo(name = "word_type") val wordType: String? = null,  // lưu "NOUN", "VERB"...
    @ColumnInfo(name = "image_url") val imageUrl: String? = null,
    @ColumnInfo(name = "audio_url") val audioUrl: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long
)