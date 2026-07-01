package com.lumina.app.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.app.data.repository.HomeRepository
import com.lumina.app.data.source.local.pref.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticeViewModel(
    private val repository: HomeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _srsCount = MutableStateFlow(0)
    val srsCount: StateFlow<Int> = _srsCount.asStateFlow()

    init {
        loadPracticeData()
    }

    private fun loadPracticeData() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            val now = System.currentTimeMillis()
            // Repository already has methods to count due cards
            // repository.getHomeData(userId) already does this, but we might want a simpler call
        }
    }
}
