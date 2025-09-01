package com.salmanajmal.hsk4mastery.ui.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.salmanajmal.hsk4mastery.data.local.model.WordBasic
import com.salmanajmal.hsk4mastery.data.repository.WordRepository

data class WordListUiState(
    val words: List<WordBasic> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class WordListViewModel @Inject constructor(
    private val wordRepository: WordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordListUiState())
    val uiState: StateFlow<WordListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Ensure DB is seeded first time
            try {
                wordRepository.startDatabaseSeedingIfNeeded()
            } catch (_: Throwable) {
                // best-effort; keep going to collect
            }

            // Collect words stream
            wordRepository.getAllWords().collect { list ->
                _uiState.update { it.copy(words = list, isLoading = false) }
            }
        }
    }
}
