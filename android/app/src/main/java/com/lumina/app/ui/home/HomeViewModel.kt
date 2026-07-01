package com.lumina.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.lumina.app.data.repository.HomeRepository
import com.lumina.app.data.source.local.pref.SessionManager
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val uiState: LiveData<HomeUiState> = flowOf(sessionManager.getUserId())
        .flatMapLatest { userId ->
            repository.getHomeData(userId)
        }.asLiveData(viewModelScope.coroutineContext)

    init {
        syncFromCloud()
    }

    private fun syncFromCloud() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            val firebaseUid = sessionManager.getFirebaseUid() ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (userId != -1L && !firebaseUid.isNullOrBlank()) {
                repository.syncFromCloud(userId, firebaseUid)
            }
        }
    }
}