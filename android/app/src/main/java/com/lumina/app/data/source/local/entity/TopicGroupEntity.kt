// TopicGroupEntity.kt
package com.lumina.app.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "topic_groups",
    foreignKeys = [ForeignKey(
        entity = StudyUnitEntity::class,
        parentColumns = ["id"],
        childColumns = ["unit_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TopicGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "unit_id", index = true) val unitId: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "is_ai_generated") val isAiGenerated: Boolean = true,
    @ColumnInfo(name = "order_index") val orderIndex: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

// Junction table — không cần @PrimaryKey riêng, dùng primaryKeys composite
@Entity(
    tableName = "topic_group_words",
    primaryKeys = ["topic_group_id", "vocabulary_id"],
    foreignKeys = [
        ForeignKey(
            entity = TopicGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["topic_group_id"],
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
data class TopicGroupWordEntity(
    @ColumnInfo(name = "topic_group_id", index = true) val topicGroupId: Long,
    @ColumnInfo(name = "vocabulary_id", index = true) val vocabularyId: Long
)