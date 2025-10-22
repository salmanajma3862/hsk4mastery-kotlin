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

enum class ScreenState { SETUP, PLAYING }

enum class PresetType { FIRST_50, NEXT_50, RANDOM_20 }

enum class PlaybackSegment { WORD, SENTENCE }

@HiltViewModel
class ListeningViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val audioPlayer: AudioPlayerService,
    private val audioAssetService: AudioAssetService,
) : ViewModel() {

    data class ListeningUiState(
        val playlist: List<WordEntity> = emptyList(),
        val currentTrackIndex: Int = 0,
        val isPlaying: Boolean = false,
        val selectedMode: ListeningMode = ListeningMode.IMMERSION,
        val selectedLevel: Int = 1,
        val startId: String = "1000",
        val endId: String = "1005",
        val playbackSpeed: Float = 1.0f,
        val screenState: ScreenState = ScreenState.SETUP,
        val error: String? = null,
        val currentSegment: PlaybackSegment = PlaybackSegment.WORD,
    )

    private val _uiState = MutableStateFlow(ListeningUiState())
    val uiState: StateFlow<ListeningUiState> = _uiState.asStateFlow()

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

    fun onPresetSelected(preset: PresetType) {
        val (levelStart, levelEnd) = when (_uiState.value.selectedLevel) {
            1 -> 1000 to 1149
            2 -> 2000 to 2149
            3 -> 3000 to 3299
            4 -> 1 to 600
            else -> 1000 to 1149
        }
        when (preset) {
            PresetType.FIRST_50 -> {
                val s = levelStart
                val e = (levelStart + 49).coerceAtMost(levelEnd)
                _uiState.update { it.copy(startId = s.toString(), endId = e.toString()) }
            }
            PresetType.NEXT_50 -> {
                val s = (levelStart + 50).coerceAtMost(levelEnd)
                val e = (s + 49).coerceAtMost(levelEnd)
                _uiState.update { it.copy(startId = s.toString(), endId = e.toString()) }
            }
            PresetType.RANDOM_20 -> {
                val window = 20
                val s = (levelStart..(levelEnd - (window - 1))).random()
                val e = (s + (window - 1)).coerceAtMost(levelEnd)
                _uiState.update { it.copy(startId = s.toString(), endId = e.toString()) }
            }
        }
    }

    fun onSpeedSelected(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
        audioPlayer.setSpeed(speed)
    }

    fun onPlayPauseTapped() {
        val current = _uiState.value
        when (current.screenState) {
            ScreenState.SETUP -> {
                startPlayback()
            }
            ScreenState.PLAYING -> {
                if (current.isPlaying) {
                    pausePlayback()
                } else {
                    // resume
                    startPlaybackLoop()
                }
            }
        }
    }

    fun startPlayback() {
        viewModelScope.launch {
            val start = _uiState.value.startId.toIntOrNull()
            val end = _uiState.value.endId.toIntOrNull()
            if (start == null || end == null || end < start) {
                _uiState.update { it.copy(error = "Invalid range") }
                return@launch
            }

            val basics = wordRepository.getAllWords(start, end).first()
            val detailed = basics.mapNotNull { b -> wordRepository.getWordDetails(b.id).first() }
            if (detailed.isEmpty()) {
                _uiState.update { it.copy(error = "No words found for range") }
                return@launch
            }
            _uiState.update { it.copy(playlist = detailed, currentTrackIndex = 0, error = null, screenState = ScreenState.PLAYING) }
            startPlaybackLoop()
        }
    }

    fun stopPlayback() {
        stopRequested = true
        try { audioPlayer.stop() } catch (_: Throwable) {}
        playbackJob?.cancel()
        _uiState.update {
            it.copy(
                isPlaying = false,
                playlist = emptyList(),
                currentTrackIndex = 0,
                screenState = ScreenState.SETUP
            )
        }
    }

    private fun pausePlayback() {
        stopRequested = true
        try { audioPlayer.stop() } catch (_: Throwable) {}
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
    }

    private fun startPlaybackLoop() {
        if (playbackJob?.isActive == true) return
        stopRequested = false
        _uiState.update { it.copy(isPlaying = true, screenState = ScreenState.PLAYING) }
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
                val currentSpeed = _uiState.value.playbackSpeed
                // 1) Chinese Word - show word content
                _uiState.update { it.copy(currentSegment = PlaybackSegment.WORD) }
                audioPlayer.play(AudioType.CHINESE_WORD, filename = "${word.hanzi}.mp3", speed = currentSpeed)
                delay(3000)
                // 2) English Meaning - keep showing word content
                audioPlayer.play(AudioType.ENGLISH_MEANING, filename = "${word.hanzi}_en.mp3", speed = currentSpeed)
                delay(3000)
                // 3) Chinese Example Sentence - switch to sentence content
                _uiState.update { it.copy(currentSegment = PlaybackSegment.SENTENCE) }
                audioPlayer.play(AudioType.CHINESE_SENTENCE, filename = "${word.hanzi}_ex1.mp3", speed = currentSpeed)
                delay(3000)
                // 4) English Sentence Translation - keep showing sentence content
                audioPlayer.play(AudioType.ENGLISH_TRANSLATION, filename = "${word.hanzi}_en_ex1.mp3", speed = currentSpeed)
                delay(3000)
            }
            ListeningMode.QUIZ -> {
                // Word only then sentence reveal
                val currentSpeed = _uiState.value.playbackSpeed
                _uiState.update { it.copy(currentSegment = PlaybackSegment.WORD) }
                audioPlayer.play(AudioType.CHINESE_WORD, filename = "${word.hanzi}.mp3", speed = currentSpeed)
                delay(1800)
                // silent recall window
                delay(1500)
                _uiState.update { it.copy(currentSegment = PlaybackSegment.SENTENCE) }
                audioPlayer.play(AudioType.CHINESE_SENTENCE, filename = "${word.hanzi}_ex1.mp3", speed = currentSpeed)
                delay(2500)
            }
            ListeningMode.DICTATION -> {
                val currentSpeed = _uiState.value.playbackSpeed
                // Sentence only, longer gap for writing
                _uiState.update { it.copy(currentSegment = PlaybackSegment.SENTENCE) }
                audioPlayer.play(AudioType.CHINESE_SENTENCE, filename = "${word.hanzi}_ex1.mp3", speed = currentSpeed)
                delay(4000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }

    fun onPrevious() {
        val state = _uiState.value
        if (state.playlist.isEmpty()) return
        
        // Stop current playback to prevent overlapping audio
        stopRequested = true
        try { audioPlayer.stop() } catch (_: Throwable) {}
        playbackJob?.cancel()
        
        val newIndex = if (state.currentTrackIndex == 0) state.playlist.lastIndex else state.currentTrackIndex - 1
        _uiState.update { it.copy(currentTrackIndex = newIndex, currentSegment = PlaybackSegment.WORD) }
        
        // Restart playback loop from new index
        startPlaybackLoop()
    }

    fun onNext() {
        val state = _uiState.value
        if (state.playlist.isEmpty()) return
        
        // Stop current playback to prevent overlapping audio
        stopRequested = true
        try { audioPlayer.stop() } catch (_: Throwable) {}
        playbackJob?.cancel()
        
        val newIndex = if (state.currentTrackIndex >= state.playlist.lastIndex) 0 else state.currentTrackIndex + 1
        _uiState.update { it.copy(currentTrackIndex = newIndex, currentSegment = PlaybackSegment.WORD) }
        
        // Restart playback loop from new index
        startPlaybackLoop()
    }
}

// Simple extension to mutate StateFlow immutably
private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) { this.value = block(this.value) }
