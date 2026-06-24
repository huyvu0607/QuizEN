package com.lumina.app.data.repository

import com.lumina.app.R
import com.lumina.app.ui.home.ContinueLearningState
import com.lumina.app.ui.home.CourseCardState
import com.lumina.app.ui.home.HomeUiState

/**
 * Hiện trả mock data. Khi BE/Room sẵn sàng, chỉ cần sửa hàm
 * getHomeData() để lấy từ database hoặc API — Fragment/ViewModel
 * không cần đổi gì.
 */
class HomeRepository {

    fun getHomeData(): HomeUiState {
        return HomeUiState(
            userName = "Nam",
            streakDays = 7,
            totalWords = 248,
            accuracyPercent = 84,
            currentXp = 450,
            goalXp = 500,
            continueLearning = ContinueLearningState(
                courseTitle = "Business English",
                level = "B2 - Intermediate",
                progressPercent = 65
            ),
            reviewWordsCount = 12,
            courses = listOf(
                CourseCardState(
                    id = "travel",
                    name = "Travel Vocabulary",
                    wordsLearned = 45,
                    wordsTotal = 120,
                    iconRes = R.drawable.ic_plane,
                    iconBgRes = R.drawable.bg_icon_square_purple,
                    progressBgRes = R.drawable.bg_progress_fill_purple
                ),
                CourseCardState(
                    id = "it",
                    name = "IT Jargon",
                    wordsLearned = 10,
                    wordsTotal = 50,
                    iconRes = R.drawable.ic_code,
                    iconBgRes = R.drawable.bg_icon_square_gray,
                    progressBgRes = R.drawable.bg_progress_fill_black
                )
            )
        )
    }
}