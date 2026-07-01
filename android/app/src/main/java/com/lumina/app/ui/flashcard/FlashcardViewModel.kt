package com.lumina.app.ui.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.app.data.model.SrsCard
import com.lumina.app.data.model.SrsRating
import com.lumina.app.data.model.Vocabulary
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.repository.SrsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToLong

class FlashcardViewModel(
    private val repository: CourseRepository,
    private val srsRepository: SrsRepository
) : ViewModel() {

    private val _vocabularies = MutableStateFlow<List<Vocabulary>>(emptyList())
    val vocabularies: StateFlow<List<Vocabulary>> = _vocabularies.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _aiExplanation = MutableStateFlow<String?>(null)
    val aiExplanation: StateFlow<String?> = _aiExplanation.asStateFlow()

    private val _isLoadingAi = MutableStateFlow(false)
    val isLoadingAi: StateFlow<Boolean> = _isLoadingAi.asStateFlow()

    fun loadVocabularies(lessonId: Long = -1, topicGroupId: Long = -1) {
        viewModelScope.launch {
            if (lessonId != -1L) {
                repository.getVocabularyByLesson(lessonId).collectLatest {
                    _vocabularies.value = it
                }
            } else if (topicGroupId != -1L) {
                // Giả sử repository có hàm lấy từ theo Topic Group
                repository.getVocabularyByTopicGroup(topicGroupId).collectLatest {
                    _vocabularies.value = it
                }
            }
        }
    }

    fun setCurrentIndex(index: Int) {
        _currentIndex.value = index
    }

    fun getAiExplanation(vocabulary: Vocabulary) {
        viewModelScope.launch {
            _isLoadingAi.value = true
            _aiExplanation.value = null
            val result = repository.explainWordWithAi(vocabulary.word, vocabulary.meaning)
            _aiExplanation.value = result
            _isLoadingAi.value = false
        }
    }

    fun resetAiExplanation() {
        _aiExplanation.value = null
    }

    fun toggleFavorite(vocab: Vocabulary) {
        viewModelScope.launch {
            val newFavStatus = !vocab.isFavorite
            repository.toggleFavorite(vocab.id, newFavStatus)
            
            // Cập nhật lại danh sách local để UI refresh
            val currentList = _vocabularies.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == vocab.id }
            if (index != -1) {
                currentList[index] = currentList[index].copy(isFavorite = newFavStatus)
                _vocabularies.value = currentList
            }
        }
    }

    fun updateSrs(vocabulary: Vocabulary, rating: SrsRating, userId: Long) {
        viewModelScope.launch {
            val existingCard = srsRepository.getCard(userId, vocabulary.id)
            val now = System.currentTimeMillis()
            
            val updatedCard = if (existingCard == null) {
                // New card
                calculateNextReview(
                    SrsCard(0, userId, vocabulary.id, nextReviewAt = now),
                    rating,
                    now
                )
            } else {
                // Update existing card
                calculateNextReview(existingCard, rating, now)
            }
            
            if (existingCard == null) {
                srsRepository.saveCard(updatedCard)
            } else {
                srsRepository.updateCard(updatedCard)
            }
        }
    }

    private fun calculateNextReview(card: SrsCard, rating: SrsRating, now: Long): SrsCard {
        var interval = card.intervalDays
        var ease = card.easeFactor
        var repetition = card.repetition

        when (rating) {
            SrsRating.FORGOT -> {
                repetition = 0
                interval = 1
                // ease doesn't change or decreases slightly
                ease = max(1.3f, ease - 0.2f)
            }
            SrsRating.HARD -> {
                repetition += 1
                interval = if (repetition == 1) 1 else (interval * 1.2).toInt()
                ease = max(1.3f, ease - 0.15f)
            }
            SrsRating.GOOD -> {
                repetition += 1
                interval = when (repetition) {
                    1 -> 1
                    2 -> 6
                    else -> (interval * ease).toInt()
                }
                // ease factor stays same or increases
            }
        }

        val nextReview = now + (interval.toLong() * 24 * 60 * 60 * 1000)
        
        return card.copy(
            intervalDays = interval,
            easeFactor = ease,
            repetition = repetition,
            nextReviewAt = nextReview,
            lastReviewedAt = now
        )
    }
}
