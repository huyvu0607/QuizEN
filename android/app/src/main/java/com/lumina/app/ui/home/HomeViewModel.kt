package com.lumina.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lumina.app.data.repository.HomeRepository

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<HomeUiState>()
    val uiState: LiveData<HomeUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        // Hiện gọi thẳng (sync) vì mock data.
        // Khi nối Room/API, bọc trong viewModelScope.launch { } là xong.
        _uiState.value = repository.getHomeData()
    }
}