package com.lumina.app.data.model

data class Vocabulary(
    val id: Long,
    val lessonId: Long,
    val word: String,               // từ tiếng Anh (bắt buộc)
    val meaning: String,             // nghĩa tiếng Việt (bắt buộc)
    val exampleSentence: String?,    // bắt buộc cho quiz dạng 2
    val ipa: String? = null,
    val wordType: WordType? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long
)

enum class WordType {
    NOUN, VERB, ADJECTIVE, ADVERB, PHRASE, OTHER
}