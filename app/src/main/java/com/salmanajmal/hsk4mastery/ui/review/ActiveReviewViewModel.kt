package com.salmanajmal.hsk4mastery.ui.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salmanajmal.hsk4mastery.data.local.model.WordEntity
import com.salmanajmal.hsk4mastery.data.repository.WordRepository
import com.salmanajmal.hsk4mastery.media.AudioPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

enum class CardMode { CHARACTER, MEANING, LISTENING }

data class ActiveReviewUiState(
    val sessionWords: List<WordEntity> = emptyList(),
    val currentCardIndex: Int = 0,
    val currentCardMode: CardMode = CardMode.CHARACTER,
    val isFlipped: Boolean = false,
    val isSessionFinished: Boolean = false,
)

@HiltViewModel
class ActiveReviewViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val audioPlayer: AudioPlayerService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveReviewUiState())
    val uiState: StateFlow<ActiveReviewUiState> = _uiState.asStateFlow()

    private val randomModes = listOf(CardMode.CHARACTER, CardMode.MEANING, CardMode.LISTENING)

    init {
        viewModelScope.launch {
            val idsCsv: String? = savedStateHandle.get<String>("wordIds")
            val ids: List<String> = idsCsv?.split(',')?.mapNotNull { it.trim().takeIf { t -> t.isNotEmpty() } } ?: emptyList()
            val words = withContext(Dispatchers.IO) {
                ids.mapNotNull { id ->
                    try { wordRepository.getWordDetails(id).first() } catch (_: Throwable) { null }
                }
            }
            _uiState.update { it.copy(sessionWords = words, currentCardMode = randomModes.random()) }
        }
    }

    fun onCardFlipped() {
        _uiState.update { it.copy(isFlipped = true) }
    }

    fun moveToNextCard() {
        val state = _uiState.value
        val nextIndex = state.currentCardIndex + 1
        if (nextIndex >= state.sessionWords.size) {
            _uiState.update { it.copy(isSessionFinished = true) }
        } else {
            _uiState.update {
                it.copy(
                    currentCardIndex = nextIndex,
                    isFlipped = false,
                    currentCardMode = randomModes.random(),
                )
            }
        }
    }

    fun submitAnswer(isCorrect: Boolean, markAsMastered: Boolean = false) {
        val state = _uiState.value
        val current = state.sessionWords.getOrNull(state.currentCardIndex) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                wordRepository.submitReviewAnswer(current.id, isCorrect, markAsMastered)
            } catch (_: Throwable) {}
            withContext(Dispatchers.Main) { moveToNextCard() }
        }
    }

    fun playWordAudio() {
        val state = _uiState.value
        val current = state.sessionWords.getOrNull(state.currentCardIndex) ?: return
        try { audioPlayer.playWord(current.hanzi, 1.0f) } catch (_: Throwable) {}
    }
}
