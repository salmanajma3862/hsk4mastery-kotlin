package com.salmanajmal.hsk4mastery.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salmanajmal.hsk4mastery.data.local.model.WordBasic
import com.salmanajmal.hsk4mastery.data.repository.ReviewDashboardData
import com.salmanajmal.hsk4mastery.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewDashboardUiState(
    val dashboardData: ReviewDashboardData? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ReviewDashboardViewModel @Inject constructor(
    private val wordRepository: WordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewDashboardUiState())
    val uiState: StateFlow<ReviewDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            wordRepository.getReviewDashboardData().collect { data ->
                _uiState.update { it.copy(dashboardData = data, isLoading = false) }
            }
        }
    }

    // Placeholder for starting a session from a list of words; will be wired to navigation later
    fun startSession(words: List<WordBasic>) {
        // no-op for now
    }
}
