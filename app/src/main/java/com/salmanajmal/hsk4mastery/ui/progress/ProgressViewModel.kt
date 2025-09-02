package com.salmanajmal.hsk4mastery.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salmanajmal.hsk4mastery.data.repository.ProgressStats
import com.salmanajmal.hsk4mastery.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressUiState(
    val stats: ProgressStats? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    fun fetchStats() {
        if (fetchJob?.isActive == true) return
        _uiState.update { it.copy(isLoading = true) }
        fetchJob = viewModelScope.launch {
            wordRepository.getProgressStats().collectLatest { stats ->
                _uiState.update { it.copy(stats = stats, isLoading = false) }
            }
        }
    }
}
