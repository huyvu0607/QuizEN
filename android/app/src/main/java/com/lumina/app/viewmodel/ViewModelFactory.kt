package com.lumina.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lumina.app.data.repository.AuthRepository
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.repository.HomeRepository
import com.lumina.app.data.repository.SrsRepository
import com.lumina.app.data.repository.UserRepository
import com.lumina.app.data.source.local.dao.QuizDao
import com.lumina.app.data.source.local.dao.SrsDao
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.ui.auth.AuthViewModel
import com.lumina.app.ui.course.CourseViewModel
import com.lumina.app.ui.flashcard.FlashcardViewModel
import com.lumina.app.ui.home.HomeViewModel

class ViewModelFactory(
    private val userRepository: UserRepository? = null,
    private val authRepository: AuthRepository? = null,
    private val sessionManager: SessionManager? = null,
    private val courseRepository: CourseRepository? = null,
    private val homeRepository: HomeRepository? = null,
    private val srsRepository: SrsRepository? = null,
    private val vocabularyDao: com.lumina.app.data.source.local.dao.VocabularyDao? = null,
    private val statsRepository: com.lumina.app.data.repository.StatsRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(com.lumina.app.ui.stats.StatsViewModel::class.java) -> {
                requireNotNull(statsRepository) { "StatsRepository là bắt buộc" }
                requireNotNull(sessionManager) { "SessionManager là bắt buộc" }
                com.lumina.app.ui.stats.StatsViewModel(statsRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                requireNotNull(homeRepository) { "HomeRepository là bắt buộc" }
                requireNotNull(sessionManager) { "SessionManager là bắt buộc" }
                HomeViewModel(homeRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                requireNotNull(authRepository) { "AuthRepository là bắt buộc để tạo AuthViewModel" }
                requireNotNull(userRepository) { "UserRepository là bắt buộc để tạo AuthViewModel" }
                requireNotNull(sessionManager) { "SessionManager là bắt buộc để tạo AuthViewModel" }
                AuthViewModel(authRepository, userRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(CourseViewModel::class.java) -> {
                requireNotNull(courseRepository) { "CourseRepository là bắt buộc để tạo CourseViewModel" }
                requireNotNull(sessionManager) { "SessionManager là bắt buộc để tạo CourseViewModel" }
                CourseViewModel(courseRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(FlashcardViewModel::class.java) -> {
                requireNotNull(courseRepository) { "CourseRepository là bắt buộc để tạo FlashcardViewModel" }
                requireNotNull(srsRepository) { "SrsRepository là bắt buộc để tạo FlashcardViewModel" }
                FlashcardViewModel(courseRepository, srsRepository) as T
            }
            modelClass.isAssignableFrom(com.lumina.app.ui.quiz.QuizViewModel::class.java) -> {
                requireNotNull(courseRepository) { "CourseRepository là bắt buộc" }
                requireNotNull(srsRepository) { "SrsRepository là bắt buộc" }
                requireNotNull(userRepository) { "UserRepository là bắt buộc" }
                com.lumina.app.ui.quiz.QuizViewModel(courseRepository, srsRepository, userRepository) as T
            }
            modelClass.isAssignableFrom(com.lumina.app.ui.practice.PracticeViewModel::class.java) -> {
                requireNotNull(homeRepository) { "HomeRepository là bắt buộc" }
                requireNotNull(sessionManager) { "SessionManager là bắt buộc" }
                com.lumina.app.ui.practice.PracticeViewModel(homeRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(com.lumina.app.ui.profile.ProfileViewModel::class.java) -> {
                requireNotNull(authRepository) { "AuthRepository là bắt buộc" }
                requireNotNull(userRepository) { "UserRepository là bắt buộc" }
                requireNotNull(sessionManager) { "SessionManager là bắt buộc" }
                requireNotNull(vocabularyDao) { "VocabularyDao là bắt buộc" }
                com.lumina.app.ui.profile.ProfileViewModel(
                    authRepository, 
                    userRepository, 
                    vocabularyDao,
                    sessionManager
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}