package com.lumina.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.app.data.model.EnglishLevel
import com.lumina.app.data.model.User
import com.lumina.app.data.repository.AuthRepository
import com.lumina.app.data.repository.UserRepository
import com.lumina.app.data.source.local.pref.SessionManager
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng điền đầy đủ thông tin")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val firebaseUser = authRepository.loginWithEmail(email, pass)
                val localUser = userRepository.upsertFromAuth(
                    email = firebaseUser.email ?: email,
                    displayName = firebaseUser.displayName ?: email.substringBefore("@"),
                    avatarUrl = firebaseUser.photoUrl?.toString()
                )
                sessionManager.saveSession(localUser.id, localUser.email, firebaseUser.uid)
                _authState.value = AuthState.Success(localUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapAuthError(e))
            }
        }
    }

    fun register(name: String, email: String, pass: String, level: String, goal: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng điền đầy đủ thông tin")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val firebaseUser = authRepository.registerWithEmail(email, pass)
                val englishLevel = runCatching { EnglishLevel.valueOf(level) }
                    .getOrDefault(EnglishLevel.A1)
                val localUser = userRepository.upsertFromAuth(
                    email = firebaseUser.email ?: email,
                    displayName = name,
                    avatarUrl = null,
                    level = englishLevel,
                    goal = goal
                )
                sessionManager.saveSession(localUser.id, localUser.email, firebaseUser.uid)
                _authState.value = AuthState.Success(localUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapAuthError(e))
            }
        }
    }

    /** @param webClientId Web Client ID lấy từ google-services.json (oauth_client) */
    fun loginWithGoogle(activity: android.app.Activity, webClientId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val firebaseUser = authRepository.signInWithGoogle(activity, webClientId)
                val localUser = userRepository.upsertFromAuth(
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "Người dùng",
                    avatarUrl = firebaseUser.photoUrl?.toString()
                )
                sessionManager.saveSession(localUser.id, localUser.email, firebaseUser.uid)
                _authState.value = AuthState.Success(localUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Đăng nhập Google thất bại")
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            // Create a default guest user in Room
            // val guest = User(...)
            // userRepository.insertUser(guest)
            // _authState.value = AuthState.Success(guest)
            _authState.value = AuthState.Error("Chức năng khách đang được phát triển")
        }
    }

    private fun mapAuthError(e: Exception): String = when (e) {
        is FirebaseAuthInvalidUserException -> "Tài khoản không tồn tại"
        is FirebaseAuthInvalidCredentialsException -> "Email hoặc mật khẩu không đúng"
        is FirebaseAuthUserCollisionException -> "Email này đã được đăng ký"
        is FirebaseAuthWeakPasswordException -> "Mật khẩu quá yếu (cần ít nhất 6 ký tự)"
        else -> e.message ?: "Đã có lỗi xảy ra, vui lòng thử lại"
    }
}