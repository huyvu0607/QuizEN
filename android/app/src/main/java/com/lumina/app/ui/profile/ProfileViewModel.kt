package com.lumina.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.app.data.repository.AuthRepository
import com.lumina.app.data.repository.UserRepository
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.data.source.local.dao.VocabularyDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val vocabularyDao: VocabularyDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        viewModelScope.launch {
            userRepository.getUserByIdFlow(userId).collect { user ->
                val totalWords = try { vocabularyDao.countTotalLearned() } catch (e: Exception) { 0 }
                user?.let {
                    _uiState.value = ProfileUiState(
                        displayName = it.displayName,
                        email = it.email,
                        avatarUrl = it.avatarUrl,
                        level = it.level.name,
                        streakDays = it.streakCount,
                        totalWords = totalWords,
                        learningDays = 42, // Mock for now
                        totalXp = it.totalXp,
                        xpGoal = 50 // Mock
                    )
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        sessionManager.clearSession()
    }
}
