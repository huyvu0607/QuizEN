package com.lumina.app.data.model

data class TopicGroup(
    val id: Long,
    val unitId: Long,
    val name: String,
    val isAiGenerated: Boolean = true,
    val orderIndex: Int = 0,         // ✅ thêm mới
    val createdAt: Long
)

// Junction table: many-to-many giữa TopicGroup và Vocabulary
data class TopicGroupWord(
    val topicGroupId: Long,
    val vocabularyId: Long
)