package com.lumina.app.data.repository

import com.lumina.app.data.source.local.dao.CourseDao
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
    private val vocabularyDao: VocabularyDao
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getHomeData(userId: Long): Flow<HomeUiState> {
        return combine(
            userDao.getUserByIdFlow(userId),
            courseDao.getCoursesByUser(userId)
        ) { user, courses ->
            user to courses
        }.flatMapLatest { (user, courses) ->
            flow {
                val courseStates = courses.take(2).map { course ->
                    val totalWords = vocabularyDao.countByCourse(course.id)
                    CourseCardState(
                        id = course.id.toString(),
                        name = course.title,
                        wordsLearned = 0, // Tính năng SRS sẽ cập nhật sau
                        wordsTotal = totalWords,
                        iconRes = getIconRes(course.coverIcon),
                        iconBgRes = com.lumina.app.R.drawable.bg_icon_square_gray,
                        coverColor = course.coverColor,
                        progressBgRes = com.lumina.app.R.drawable.bg_progress_fill_primary
                    )
                }

                val totalWordsGlobal = courses.sumOf { vocabularyDao.countByCourse(it.id) }

                emit(HomeUiState(
                    userName = user?.displayName ?: "Người dùng",
                    streakDays = user?.streakCount ?: 0,
                    totalWords = totalWordsGlobal,
                    accuracyPercent = 85, // Mock data
                    currentXp = user?.totalXp ?: 0,
                    goalXp = 500,
                    continueLearning = courses.firstOrNull()?.let {
                        ContinueLearningState(
                            courseTitle = it.title,
                            level = it.level ?: "N/A",
                            progressPercent = 35, // Mock progress
                            iconRes = getIconRes(it.coverIcon),
                            coverColor = it.coverColor
                        )
                    },
                    reviewWordsCount = 12, // Mock review count
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
