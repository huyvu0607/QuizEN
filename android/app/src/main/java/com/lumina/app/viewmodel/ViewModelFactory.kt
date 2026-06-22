package com.lumina.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lumina.app.data.repository.AuthRepository
import com.lumina.app.data.repository.UserRepository
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.ui.auth.AuthViewModel

class ViewModelFactory(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository? = null,
    private val sessionManager: SessionManager? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                requireNotNull(authRepository) { "AuthRepository là bắt buộc để tạo AuthViewModel" }
                requireNotNull(sessionManager) { "SessionManager là bắt buộc để tạo AuthViewModel" }
                AuthViewModel(authRepository, userRepository, sessionManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}