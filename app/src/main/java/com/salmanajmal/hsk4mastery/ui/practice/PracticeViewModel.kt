package com.salmanajmal.hsk4mastery.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salmanajmal.hsk4mastery.data.local.model.WordEntity
import com.salmanajmal.hsk4mastery.data.repository.WordRepository
import com.salmanajmal.hsk4mastery.services.PracticeService
import com.salmanajmal.hsk4mastery.ui.worddetail.components.SentenceTokenModel
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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

enum class PracticeMode { TRANSLATION, SCRAMBLE }

data class PracticeUiState(
    val practiceWord: WordEntity? = null,
    val originalTokens: List<SentenceTokenModel> = emptyList(), // original order to compare
    val scrambledTokens: List<SentenceTokenModel> = emptyList(),
    val userAttempt: List<SentenceTokenModel> = emptyList(),
    val practiceMode: PracticeMode = PracticeMode.TRANSLATION,
    val isCorrect: Boolean? = null,
    val isLoading: Boolean = false,
    val activeExampleTranslation: String? = null,
)

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val practiceService: PracticeService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeUiState(isLoading = true))
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    init {
        fetchNewPuzzle()
    }

    fun fetchNewPuzzle() {
        _uiState.update { it.copy(isLoading = true, isCorrect = null) }
        viewModelScope.launch {
            val word = try { wordRepository.getRandomPracticeWord() } catch (_: Throwable) { null }
            if (word == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            val (tokens, translation) = parseRandomExampleTokens(word.fullData)
            val scrambled = practiceService.generateScramble(tokens)
            _uiState.update {
                it.copy(
                    practiceWord = word,
                    originalTokens = tokens,
                    scrambledTokens = scrambled,
                    userAttempt = emptyList(),
                    isCorrect = null,
                    isLoading = false,
                    activeExampleTranslation = translation,
                )
            }
        }
    }

    fun onWordBankTap(index: Int) {
        val state = _uiState.value
        if (index !in state.scrambledTokens.indices) return
        val token = state.scrambledTokens[index]
        _uiState.update {
            it.copy(
                scrambledTokens = it.scrambledTokens.toMutableList().also { l -> l.removeAt(index) },
                userAttempt = it.userAttempt + token,
                isCorrect = null,
            )
        }
    }

    fun onAnswerTap(index: Int) {
        val state = _uiState.value
        if (index !in state.userAttempt.indices) return
        val token = state.userAttempt[index]
        _uiState.update {
            it.copy(
                userAttempt = it.userAttempt.toMutableList().also { l -> l.removeAt(index) },
                scrambledTokens = it.scrambledTokens + token,
                isCorrect = null,
            )
        }
    }

    fun checkAnswer() {
        val state = _uiState.value
        if (state.originalTokens.isEmpty()) return
        val correct = state.originalTokens.map { it.word ?: "" }
        val attempt = state.userAttempt.map { it.word ?: "" }
        val isCorrect = attempt.size == correct.size && attempt.indices.all { i -> attempt[i] == correct[i] }
        _uiState.update { it.copy(isCorrect = isCorrect) }
    }

    fun onModeSelected(mode: PracticeMode) {
        if (mode == _uiState.value.practiceMode) return
        // When mode changes, re-scramble and reset attempt similar to JS version
        val scrambled = practiceService.generateScramble(_uiState.value.originalTokens)
        _uiState.update {
            it.copy(
                practiceMode = mode,
                scrambledTokens = scrambled,
                userAttempt = emptyList(),
                isCorrect = null,
            )
        }
    }

    fun onReset() {
        // Reset to initial scramble used when puzzle created
        val scrambled = practiceService.generateScramble(_uiState.value.originalTokens)
        _uiState.update { it.copy(scrambledTokens = scrambled, userAttempt = emptyList(), isCorrect = null) }
    }

    private fun parseRandomExampleTokens(fullData: String?): Pair<List<SentenceTokenModel>, String?> {
        if (fullData.isNullOrBlank()) return emptyList<SentenceTokenModel>() to null
        return try {
            val root = Json.parseToJsonElement(fullData) as? JsonObject ?: return emptyList<SentenceTokenModel>() to null
            val examples = root["examples"] as? JsonArray ?: return emptyList<SentenceTokenModel>() to null
            val valid = examples.mapNotNull { it as? JsonObject }
                .filter { (it["tokens"] as? JsonArray)?.isNotEmpty() == true }
            if (valid.isEmpty()) return emptyList<SentenceTokenModel>() to null
            val picked = valid.random()
            val translation = picked["translation"].stringOrNull()
            val tokensJson = picked["tokens"] as? JsonArray ?: JsonArray(emptyList())
            val tokens = tokensJson.mapNotNull { el: JsonElement ->
                val o = el as? JsonObject ?: return@mapNotNull null
                SentenceTokenModel(
                    word = o["word"].stringOrNull(),
                    pinyin = o["pinyin"].stringOrNull(),
                    meaning = o["meaning"].stringOrNull(),
                )
            }
            tokens to translation
        } catch (_: Throwable) {
            emptyList<SentenceTokenModel>() to null
        }
    }

    private fun JsonElement?.stringOrNull(): String? {
        val p = this as? JsonPrimitive ?: return null
        return try { p.content } catch (_: Throwable) { null }
    }
}
