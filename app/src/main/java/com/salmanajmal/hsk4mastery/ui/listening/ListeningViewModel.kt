package com.salmanajmal.hsk4mastery.ui.listening

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salmanajmal.hsk4mastery.data.local.model.WordEntity
import com.salmanajmal.hsk4mastery.data.repository.WordRepository
import com.salmanajmal.hsk4mastery.media.AudioAssetService
import com.salmanajmal.hsk4mastery.media.AudioPlayerService
import com.salmanajmal.hsk4mastery.media.AudioType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListeningViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val audioPlayer: AudioPlayerService,
    private val audioAssetService: AudioAssetService,
) : ViewModel() {

    data class UiState(
        val playlist: List<WordEntity> = emptyList(),
        val currentTrackIndex: Int = 0,
        val isPlaying: Boolean = false,
        val selectedMode: ListeningMode = ListeningMode.IMMERSION,
        val selectedLevel: Int = 1,
        val startId: String = "1000",
        val endId: String = "1005",
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var stopRequested = false

    fun onLevelSelected(level: Int) {
        val (start, end) = when (level) {
            1 -> 1000 to 1149
            2 -> 2000 to 2149
            3 -> 3000 to 3299
            4 -> 1 to 600
            else -> 1000 to 1149
        }
        _uiState.update { it.copy(selectedLevel = level, startId = start.toString(), endId = end.toString()) }
    }

    fun onStartIdChanged(value: String) { _uiState.update { it.copy(startId = value) } }
    fun onEndIdChanged(value: String) { _uiState.update { it.copy(endId = value) } }
    fun onModeSelected(mode: ListeningMode) { _uiState.update { it.copy(selectedMode = mode) } }

    fun buildPlaylist() {
        viewModelScope.launch {
            val start = _uiState.value.startId.toIntOrNull()
            val end = _uiState.value.endId.toIntOrNull()
            if (start == null || end == null || end < start) {
                _uiState.update { it.copy(error = "Invalid range") }
                return@launch
            }
            // Reuse repository seeding range function: we only have WordBasic flow; need details so map IDs
            val basics = wordRepository.getAllWords(start, end).first()
            val detailed = basics.mapNotNull { b -> wordRepository.getWordDetails(b.id).first() }
            _uiState.update { it.copy(playlist = detailed, currentTrackIndex = 0, error = null) }
        }
    }

    fun onPlayPauseTapped() {
        val current = _uiState.value
        if (current.isPlaying) {
            stopPlayback()
        } else {
            if (current.playlist.isEmpty()) buildPlaylist() // ensure playlist
            startPlaybackLoop()
        }
    }

    private fun startPlaybackLoop() {
        if (playbackJob?.isActive == true) return
        stopRequested = false
        _uiState.update { it.copy(isPlaying = true) }
        playbackJob = viewModelScope.launch {
            while (!stopRequested) {
                val state = _uiState.value
                if (state.playlist.isEmpty()) {
                    delay(500)
                    continue
                }
                val index = state.currentTrackIndex.coerceIn(0, state.playlist.lastIndex)
                val word = state.playlist[index]
                try {
                    playForMode(word, state.selectedMode)
                } catch (_: Throwable) {}
                // Advance index or loop
                val nextIndex = if (index >= state.playlist.lastIndex) 0 else index + 1
                _uiState.update { it.copy(currentTrackIndex = nextIndex) }
            }
            _uiState.update { it.copy(isPlaying = false) }
        }
    }

    private suspend fun playForMode(word: WordEntity, mode: ListeningMode) {
        when (mode) {
            ListeningMode.IMMERSION -> {
                val currentSpeed = 1.0f
                // 1) Chinese Word
                audioPlayer.play(AudioType.CHINESE_WORD, filename = "${word.hanzi}.mp3", speed = currentSpeed)
                delay(3000)
                // 2) English Meaning
                audioPlayer.play(AudioType.ENGLISH_MEANING, filename = "${word.hanzi}_en.mp3", speed = currentSpeed)
                delay(3000)
                // 3) Chinese Example Sentence
                audioPlayer.play(AudioType.CHINESE_SENTENCE, filename = "${word.hanzi}_ex1.mp3", speed = currentSpeed)
                delay(3000)
                // 4) English Sentence Translation
                audioPlayer.play(AudioType.ENGLISH_TRANSLATION, filename = "${word.hanzi}_en_ex1.mp3", speed = currentSpeed)
                delay(3000)
            }
            ListeningMode.QUIZ -> {
                // Word only then sentence reveal
                audioPlayer.play(AudioType.CHINESE_WORD, filename = "${word.hanzi}.mp3")
                delay(1800)
                // silent recall window
                delay(1500)
                audioPlayer.play(AudioType.CHINESE_SENTENCE, filename = "${word.hanzi}_ex1.mp3")
                delay(2500)
            }
            ListeningMode.DICTATION -> {
                // Sentence only, longer gap for writing
                audioPlayer.play(AudioType.CHINESE_SENTENCE, filename = "${word.hanzi}_ex1.mp3")
                delay(4000)
            }
        }
    }

    private fun stopPlayback() {
        stopRequested = true
        audioPlayer.stop()
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}

// Simple extension to mutate StateFlow immutably
private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) { this.value = block(this.value) }
