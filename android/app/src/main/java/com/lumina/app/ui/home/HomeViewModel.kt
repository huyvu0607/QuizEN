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

class HomeViewModel(
    private val repository: HomeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val uiState: LiveData<HomeUiState> = flowOf(sessionManager.getUserId())
        .flatMapLatest { userId ->
            repository.getHomeData(userId)
        }.asLiveData(viewModelScope.coroutineContext)

}