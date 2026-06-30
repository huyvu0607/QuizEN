package com.lumina.app.ui.unit

import com.lumina.app.data.model.Vocabulary

data class TopicGroupUiItem(
    val id: Long,
    val unitId: Long,
    val name: String,
    val description: String?,
    val words: List<Vocabulary>,
    val masteryRate: Int = 0, // ✅ % hoàn thành
    val isAiGenerated: Boolean
)
