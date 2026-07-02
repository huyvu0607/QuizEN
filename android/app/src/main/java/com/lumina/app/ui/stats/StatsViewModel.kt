package com.lumina.app.ui.stats

import androidx.lifecycle.*
import com.lumina.app.data.repository.StatsRepository
import com.lumina.app.data.repository.StatsData
import com.lumina.app.data.source.local.pref.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatsViewModel(
    private val statsRepository: StatsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _stats = MutableLiveData<StatsData>()
    val stats: LiveData<StatsData> = _stats

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1L) {
                statsRepository.getStats(userId).collectLatest {
                    _stats.value = it
                }
            }
        }
    }
}
