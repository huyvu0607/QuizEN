package com.lumina.app.data.repository

import com.lumina.app.data.source.local.dao.CourseDao
import com.lumina.app.data.source.local.dao.QuizDao
import com.lumina.app.data.source.local.dao.SrsDao
import com.lumina.app.data.source.local.dao.UserDao
import com.lumina.app.data.source.local.dao.VocabularyDao
import com.lumina.app.ui.home.ContinueLearningState
import com.lumina.app.ui.home.CourseCardState
import com.lumina.app.ui.home.HomeUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.ExperimentalCoroutinesApi

class HomeRepository(
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val vocabularyDao: VocabularyDao,
    private val srsDao: SrsDao,
    private val quizDao: QuizDao
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getHomeData(userId: Long): Flow<HomeUiState> {
        val now = System.currentTimeMillis()
        return combine(
            userDao.getUserByIdFlow(userId),
            courseDao.getCoursesByUser(userId),
            srsDao.countDueCards(userId, now)
        ) { user, courses, dueCount ->
            Triple(user, courses, dueCount)
        }.flatMapLatest { (user, courses, dueCount) ->
            flow {
                val courseStates = courses.take(2).map { course ->
                    val totalWords = vocabularyDao.countByCourse(course.id)
                    val learnedWords = vocabularyDao.countLearnedByCourse(course.id)
                    CourseCardState(
                        id = course.id.toString(),
                        name = course.title,
                        wordsLearned = learnedWords,
                        wordsTotal = totalWords,
                        iconRes = getIconRes(course.coverIcon),
                        iconBgRes = com.lumina.app.R.drawable.bg_icon_square_gray,
                        coverColor = course.coverColor,
                        progressBgRes = com.lumina.app.R.drawable.bg_progress_fill_primary
                    )
                }

                val totalLearnedGlobal = vocabularyDao.countTotalLearned()
                
                // Tính toán độ chính xác thực tế
                val totalCorrect = quizDao.getTotalCorrectAnswers(userId) ?: 0
                val totalQuestions = quizDao.getTotalQuestionsAsked(userId) ?: 0
                val accuracy = if (totalQuestions > 0) (totalCorrect * 100) / totalQuestions else 0
                
                emit(HomeUiState(
                    userName = user?.displayName ?: "Người dùng",
                    streakDays = user?.streakCount ?: 0,
                    totalWords = totalLearnedGlobal,
                    accuracyPercent = accuracy,
                    currentXp = user?.totalXp ?: 0,
                    goalXp = user?.goal?.toIntOrNull() ?: 500,
                    continueLearning = courses.firstOrNull()?.let {
                        val total = vocabularyDao.countByCourse(it.id)
                        val learned = vocabularyDao.countLearnedByCourse(it.id)
                        val progress = if (total > 0) (learned * 100) / total else 0
                        ContinueLearningState(
                            courseTitle = it.title,
                            level = it.level ?: "N/A",
                            progressPercent = progress,
                            iconRes = getIconRes(it.coverIcon),
                            coverColor = it.coverColor
                        )
                    },
                    reviewWordsCount = dueCount,
                    courses = courseStates
                ))
            }
        }
    }

    private fun getIconRes(iconKey: String?): Int {
        return when (iconKey) {
            "book" -> com.lumina.app.R.drawable.ic_book
            "star" -> com.lumina.app.R.drawable.ic_star
            "trophy" -> com.lumina.app.R.drawable.ic_trophy
            "brain" -> com.lumina.app.R.drawable.ic_brain
            "globe" -> com.lumina.app.R.drawable.ic_globe
            "briefcase" -> com.lumina.app.R.drawable.ic_briefcase
            "plane" -> com.lumina.app.R.drawable.ic_plane
            else -> com.lumina.app.R.drawable.ic_book
        }
    }
}
