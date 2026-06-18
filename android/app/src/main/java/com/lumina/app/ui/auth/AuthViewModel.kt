package com.lumina.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.app.data.model.User
import com.lumina.app.data.repository.UserRepository
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng điền đầy đủ thông tin")
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            // TODO: Call Repository for Remote Login
            // For now, simulate success
            _authState.value = AuthState.Error("Chức năng đăng nhập đang được phát triển")
        }
    }

    fun register(name: String, email: String, pass: String, level: String, goal: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng điền đầy đủ thông tin")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            // TODO: Call Repository for Registration
            _authState.value = AuthState.Error("Chức năng đăng ký đang được phát triển")
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

    fun loginWithGoogle() {
        _authState.value = AuthState.Error("Chức năng đăng nhập Google đang được phát triển")
    }
}
