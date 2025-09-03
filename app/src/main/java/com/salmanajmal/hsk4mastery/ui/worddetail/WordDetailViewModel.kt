package com.salmanajmal.hsk4mastery.ui.worddetail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackParameters
import com.salmanajmal.hsk4mastery.data.local.model.WordEntity
import com.salmanajmal.hsk4mastery.data.repository.WordRepository
import com.salmanajmal.hsk4mastery.data.repository.NeighboringWords
import com.salmanajmal.hsk4mastery.media.AudioPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

data class ExampleToken(val word: String?, val pinyin: String?, val meaning: String?)
data class ExampleItem(val translation: String?, val audioUrl: String?, val tokens: List<ExampleToken>)
data class CharacterPiece(val hanzi: String?, val pinyin: String?, val meaning: String?)

data class WordDetailParsed(
    val mainAudioUrl: String? = null,
    val examples: List<ExampleItem> = emptyList(),
    val characterBreakdown: List<CharacterPiece> = emptyList(),
)

data class WordDetailUiState(
    val word: WordEntity? = null,
    val parsed: WordDetailParsed = WordDetailParsed(),
    val comfortLevel: Int? = null,
    val audioSpeed: Float = 1.0f,
    val isLoading: Boolean = true,
    val neighboringWords: NeighboringWords? = null,
)

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val audioPlayer: AudioPlayerService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordDetailUiState())
    val uiState: StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    // central player is managed by AudioPlayerService

    private val wordId: String = savedStateHandle.get<String>("wordId") ?: ""

    init {
        if (wordId.isNotBlank()) {
            viewModelScope.launch {
                // collect details
                wordRepository.getWordDetails(wordId).collect { entity ->
                    val parsed = parseFullData(entity?.fullData)
                    _uiState.update { it.copy(word = entity, parsed = parsed, isLoading = false) }
                    // After we have entity, fetch neighboring words using numeric wordId if available
                    val numericId = entity?.wordId
                    if (numericId != null) {
                        try {
                            val neighbors = wordRepository.getNeighboringWords(numericId)
                            _uiState.update { st -> st.copy(neighboringWords = neighbors) }
                        } catch (_: Throwable) { /* ignore */ }
                    }
                }
            }
            // also observe comfort level via list stream to match ID
            viewModelScope.launch {
                // Observe across all likely ID ranges (HSK1-4)
                wordRepository.getAllWords(1, 3999).collect { list ->
                    val match = list.firstOrNull { it.id == wordId }
                    _uiState.update { it.copy(comfortLevel = match?.comfortLevel) }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
    try { audioPlayer.release() } catch (_: Throwable) {}
    }

    fun onAudioSpeedChange(speed: Float) {
        _uiState.update { it.copy(audioSpeed = speed) }
    try { audioPlayer.setSpeed(speed) } catch (_: Throwable) {}
    }

    // Play audio from an explicit asset filename if provided; otherwise fall back to hanzi-based asset
    fun playAudio(filename: String?) {
        val speed = uiState.value.audioSpeed
        if (!filename.isNullOrBlank()) {
            // Assume provided filename is the file under words folder unless it already contains subdir
            val isSentence = filename.contains("_ex") || filename.contains("/sentences/")
            val clean = filename.substringAfterLast('/')
            audioPlayer.play(clean, isSentence = isSentence, speed = speed)
            return
        }
        // Fallback: derive from hanzi
        playWordAudio()
    }

    fun onComfortLevelSelected(level: Int) {
        val id = uiState.value.word?.id ?: return
        viewModelScope.launch {
            try {
                wordRepository.updateWordComfort(id, level)
                _uiState.update { it.copy(comfortLevel = level) }
            } catch (_: Throwable) { /* ignore */ }
        }
    }

    fun playWordAudio() {
        val word = uiState.value.word ?: return
        // Prefer explicit filename if present
        val file = uiState.value.parsed.mainAudioUrl
        if (!file.isNullOrBlank()) {
            playAudio(file)
        } else {
            audioPlayer.playWord(word.hanzi, uiState.value.audioSpeed)
        }
    }

    fun playSentenceAudio(exampleIndex1Based: Int) {
        val word = uiState.value.word ?: return
        audioPlayer.playSentence(word.hanzi, exampleIndex1Based, uiState.value.audioSpeed)
    }

    private fun parseFullData(fullData: String?): WordDetailParsed {
        if (fullData.isNullOrBlank()) return WordDetailParsed()
        return try {
            val root = Json.parseToJsonElement(fullData)
            val obj = root as? JsonObject ?: return WordDetailParsed()
            val mainAudio = obj["mainAudioUrl"]?.jsonPrimitive?.content

            val charPieces = mutableListOf<CharacterPiece>()
            (obj["characterBreakdown"] as? JsonArray)?.forEach { el: JsonElement ->
                val o = el as? JsonObject
                val hanzi = o?.get("hanzi")?.jsonPrimitive?.content ?: o?.get("char")?.jsonPrimitive?.content
                val pinyin = o?.get("pinyin")?.jsonPrimitive?.content
                val meaning = o?.get("meaning")?.jsonPrimitive?.content
                charPieces += CharacterPiece(hanzi, pinyin, meaning)
            }

            val examples = mutableListOf<ExampleItem>()
            (obj["examples"] as? JsonArray)?.forEach { el ->
                val o = el as? JsonObject
        val translation = o?.get("translation")?.jsonPrimitive?.content
        val audio = o?.get("audioUrl")?.jsonPrimitive?.content
                val tokens = mutableListOf<ExampleToken>()
                (o?.get("tokens") as? JsonArray)?.forEach { tel ->
                    val to = tel as? JsonObject
                    tokens += ExampleToken(
            word = to?.get("word")?.jsonPrimitive?.content,
            pinyin = to?.get("pinyin")?.jsonPrimitive?.content,
            meaning = to?.get("meaning")?.jsonPrimitive?.content,
                    )
                }
                examples += ExampleItem(translation = translation, audioUrl = audio, tokens = tokens)
            }

            WordDetailParsed(mainAudioUrl = mainAudio, examples = examples, characterBreakdown = charPieces)
        } catch (_: Throwable) {
            WordDetailParsed()
        }
    }
}
