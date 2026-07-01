package com.lumina.app.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.app.data.model.SrsRating
import com.lumina.app.data.model.Vocabulary
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.repository.SrsRepository
import com.lumina.app.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuizViewModel(
    private val repository: CourseRepository,
    private val srsRepository: SrsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val questions: StateFlow<List<QuizQuestion>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _wrongVocabularies = MutableStateFlow<List<Vocabulary>>(emptyList())
    val wrongVocabularies: StateFlow<List<Vocabulary>> = _wrongVocabularies.asStateFlow()

    fun generateQuiz(
        lessonId: Long = -1,
        topicGroupId: Long = -1,
        count: Int = 10,
        types: List<QuizType>
    ) {
        viewModelScope.launch {
            val vocabList = if (lessonId != -1L) {
                repository.getVocabularyByLesson(lessonId).first()
            } else if (topicGroupId != -1L) {
                repository.getVocabularyByTopicGroup(topicGroupId).first()
            } else {
                emptyList()
            }

            if (vocabList.isEmpty()) return@launch

            val generatedQuestions = mutableListOf<QuizQuestion>()
            val shuffledVocab = vocabList.shuffled().take(count)

            shuffledVocab.forEach { vocab ->
                val type = if (types.size > 1) types.random() else types.firstOrNull() ?: QuizType.MULTIPLE_CHOICE
                
                if (type == QuizType.FILL_IN_THE_BLANK && !vocab.exampleSentence.isNullOrBlank()) {
                    generatedQuestions.add(createFillBlankQuestion(vocab))
                } else {
                    generatedQuestions.add(createMultipleChoiceQuestion(vocab, vocabList))
                }
            }

            _questions.value = generatedQuestions
        }
    }

    private fun createMultipleChoiceQuestion(target: Vocabulary, allVocab: List<Vocabulary>): QuizQuestion {
        val distractors = allVocab.filter { it.id != target.id }
            .shuffled()
            .take(3)
            .map { it.meaning }
        
        val options = (distractors + target.meaning).shuffled()
        
        return QuizQuestion(
            id = target.id,
            type = QuizType.MULTIPLE_CHOICE,
            questionText = target.word,
            correctAnswer = target.meaning,
            options = options,
            vocabulary = target
        )
    }

    private fun createFillBlankQuestion(vocab: Vocabulary): QuizQuestion {
        val sentence = vocab.exampleSentence!!
        // Replace target word with blank, case insensitive
        val pattern = "(?i)\\b${vocab.word}\\b".toRegex()
        val blankedSentence = sentence.replace(pattern, "_____")
        
        return QuizQuestion(
            id = vocab.id,
            type = QuizType.FILL_IN_THE_BLANK,
            questionText = blankedSentence,
            correctAnswer = vocab.word,
            vocabulary = vocab,
            hint = vocab.word.firstOrNull()?.toString()
        )
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun submitAnswer(answer: String, userId: Long) {
        val currentQuestion = questions.value[currentQuestionIndex.value]
        val isCorrect = answer.trim().equals(currentQuestion.correctAnswer.trim(), ignoreCase = true)

        if (isCorrect) {
            _score.value += 1
            updateSrs(currentQuestion.vocabulary, SrsRating.GOOD, userId)
            // Thêm 5 XP cho mỗi câu đúng
            viewModelScope.launch {
                userRepository.addXp(userId, 5)
            }
        } else {
            val currentWrong = _wrongVocabularies.value.toMutableList()
            if (!currentWrong.contains(currentQuestion.vocabulary)) {
                currentWrong.add(currentQuestion.vocabulary)
                _wrongVocabularies.value = currentWrong
            }
            updateSrs(currentQuestion.vocabulary, SrsRating.FORGOT, userId)
        }
    }

    private fun updateSrs(vocabulary: Vocabulary, rating: SrsRating, userId: Long) {
        viewModelScope.launch {
            val existingCard = srsRepository.getCard(userId, vocabulary.id)
            val now = System.currentTimeMillis()
            
            val updatedCard = if (existingCard == null) {
                calculateNextReview(
                    com.lumina.app.data.model.SrsCard(0, userId, vocabulary.id, nextReviewAt = now),
                    rating,
                    now
                )
            } else {
                calculateNextReview(existingCard, rating, now)
            }
            
            if (existingCard == null) {
                srsRepository.saveCard(updatedCard)
            } else {
                srsRepository.updateCard(updatedCard)
            }
        }
    }

    private fun calculateNextReview(card: com.lumina.app.data.model.SrsCard, rating: SrsRating, now: Long): com.lumina.app.data.model.SrsCard {
        var interval = card.intervalDays
        var ease = card.easeFactor
        var repetition = card.repetition

        when (rating) {
            SrsRating.FORGOT -> {
                repetition = 0
                interval = 1
                ease = kotlin.math.max(1.3f, ease - 0.2f)
            }
            SrsRating.HARD -> {
                repetition += 1
                interval = if (repetition == 1) 1 else (interval * 1.2).toInt()
                ease = kotlin.math.max(1.3f, ease - 0.15f)
            }
            SrsRating.GOOD -> {
                repetition += 1
                interval = when (repetition) {
                    1 -> 1
                    2 -> 6
                    else -> (interval * ease).toInt()
                }
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
