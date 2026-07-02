package com.lumina.app.data.repository

import com.lumina.app.data.source.local.dao.*
import kotlinx.coroutines.flow.*
import java.util.*

class StatsRepository(
    private val userDao: UserDao,
    private val vocabularyDao: VocabularyDao,
    private val quizDao: QuizDao,
    private val srsDao: SrsDao,
    private val topicGroupDao: TopicGroupDao
) {
    fun getStats(userId: Long): Flow<StatsData> = flow {
        val now = System.currentTimeMillis()
        val user = userDao.getUserByIdFlow(userId).first()
        val totalLearned = vocabularyDao.countTotalLearned()
        val totalCorrect = quizDao.getTotalCorrectAnswers(userId) ?: 0
        val totalQuestions = quizDao.getTotalQuestionsAsked(userId) ?: 0
        val accuracy = if (totalQuestions > 0) (totalCorrect * 100) / totalQuestions else 0
        val dueCount = srsDao.countDueCards(userId, now).first()

        // Mock data for weekly chart (in real app, we would query the DB for reviews per day)
        val weeklyActivity = listOf(15, 25, 10, 30, 45, 20, 55) 

        // Mock topic accuracy
        val topicAccuracy = listOf(
            TopicStats("Giao tiếp văn phòng", 92, "#22C55E"),
            TopicStats("Công nghệ thông tin", 78, "#2161F3"),
            TopicStats("Động từ bất quy tắc", 65, "#9333EA")
        )

        emit(StatsData(
            totalWords = totalLearned,
            streakDays = user?.streakCount ?: 0,
            totalXp = user?.totalXp ?: 0,
            accuracyPercent = accuracy,
            weeklyActivity = weeklyActivity,
            topicAccuracy = topicAccuracy,
            dueCount = dueCount
        ))
    }
}

data class StatsData(
    val totalWords: Int,
    val streakDays: Int,
    val totalXp: Int,
    val accuracyPercent: Int,
    val weeklyActivity: List<Int>,
    val topicAccuracy: List<TopicStats>,
    val dueCount: Int
)

data class TopicStats(
    val name: String,
    val accuracy: Int,
    val color: String
)
