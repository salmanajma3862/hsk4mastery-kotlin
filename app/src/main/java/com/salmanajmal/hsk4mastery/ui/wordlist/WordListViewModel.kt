package com.salmanajmal.hsk4mastery.ui.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.salmanajmal.hsk4mastery.data.local.model.WordBasic
import com.salmanajmal.hsk4mastery.data.repository.WordRepository

data class WordListUiState(
    val words: List<WordBasic> = emptyList(),
    val isLoading: Boolean = true,
    val selectedLevel: Int = 1,
)

@HiltViewModel
class WordListViewModel @Inject constructor(
    private val wordRepository: WordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordListUiState())
    val uiState: StateFlow<WordListUiState> = _uiState.asStateFlow()

    private val selectedLevel = MutableStateFlow(1)

    init {
        viewModelScope.launch {
            // Ensure DB is seeded first time
            try { wordRepository.startDatabaseSeedingIfNeeded() } catch (_: Throwable) {}

            // React to level changes -> compute range -> fetch words
            selectedLevel.flatMapLatest { level ->
                val range = when (level) {
                    1 -> 1000 to 1999
                    2 -> 2000 to 2999
                    3 -> 3000 to 3999
                    4 -> 1 to 602
                    else -> 1000 to 1999
                }
                wordRepository.getAllWords(range.first, range.second)
            }.collect { list ->
                _uiState.update { it.copy(words = list, isLoading = false, selectedLevel = selectedLevel.value) }
            }
        }
    }

    fun onLevelSelected(level: Int) {
        selectedLevel.value = level
        _uiState.update { it.copy(selectedLevel = level, isLoading = true) }
    }
}
