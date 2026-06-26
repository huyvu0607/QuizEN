package com.lumina.app.ui.vocabulary

enum class VocabularyStatus {
    NOT_STARTED, NEEDS_REVIEW, LEARNED
}

data class VocabularyUiItem(
    val id: Long,
    val word: String,
    val ipa: String?,
    val meaning: String,
    val wordType: String?,
    val exampleSentence: String?,
    val audioUrl: String?,
    val status: VocabularyStatus = VocabularyStatus.NOT_STARTED,
    val isFavorite: Boolean = false
)
