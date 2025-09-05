package com.salmanajmal.hsk4mastery.data.repository

import android.content.Context
import com.salmanajmal.hsk4mastery.data.local.dao.WordDao
import com.salmanajmal.hsk4mastery.data.local.model.WordBasic
import com.salmanajmal.hsk4mastery.data.local.model.WordEntity
import com.salmanajmal.hsk4mastery.data.local.model.UserWordProgressEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class WordRepositoryImpl @Inject constructor(
    private val dao: WordDao,
    @ApplicationContext private val context: Context,
) : WordRepository {

    override fun getAllWords(minId: Int, maxId: Int): Flow<List<WordBasic>> = dao.getAllWords(minId, maxId)

    override fun getWordDetails(wordId: String): Flow<WordEntity?> = flow {
        emit(dao.getWordById(wordId))
    }.flowOn(Dispatchers.IO)

    override suspend fun getNeighboringWords(wordId: Int): NeighboringWords {
        val prev = dao.getPreviousWords(wordId, 10)
        val next = dao.getNextWords(wordId, 10)
        // Reverse previous so it shows ascending order from older->current neighbor
        return NeighboringWords(previous = prev.reversed(), next = next)
    }

    override suspend fun startDatabaseSeedingIfNeeded() {
        // Read all HSK JSON assets and seed once
        val assetManager = context.assets
        val files = listOf(
            "hsk1_data_150.json",
            "hsk2_data_150.json",
            "hsk3_data_300.json",
            "hsk4_data_600.json",
        )

        // If DB already has at least the total expected, skip
        val expectedTotal = 150 + 150 + 300 + 600
        val existingCount = try { dao.getWordCount() } catch (_: Throwable) { 0 }
        if (existingCount >= expectedTotal) return

        val entities = mutableListOf<WordEntity>()
        for (name in files) {
            val jsonText = try {
                assetManager.open(name).use { it.bufferedReader().readText() }
            } catch (_: Throwable) { null }
            if (jsonText.isNullOrBlank()) continue
            val array = JSONArray(jsonText)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = when {
                    obj.has("_id") && !obj.isNull("_id") -> obj.get("_id").toString()
                    obj.has("id") && !obj.isNull("id") -> obj.get("id").toString()
                    obj.has("wordId") && !obj.isNull("wordId") -> obj.get("wordId").toString()
                    obj.has("hanzi") && !obj.isNull("hanzi") -> obj.getString("hanzi")
                    else -> "${name}#$i"
                }
                val wordId = if (obj.has("wordId") && !obj.isNull("wordId")) obj.optInt("wordId") else null
                val hanzi = obj.optString("hanzi")
                val pinyin = obj.optString("pinyin")
                val meaning = obj.optString("meaning")
                val enforced = JSONObject(obj.toString()).apply { put("_id", id) }
                entities.add(
                    WordEntity(
                        id = id,
                        wordId = wordId,
                        hanzi = hanzi,
                        pinyin = pinyin,
                        meaning = meaning,
                        fullData = enforced.toString(),
                    )
                )
            }
        }
        if (entities.isNotEmpty()) dao.insertWords(entities)
    }

    override suspend fun getRandomPracticeWord(): WordEntity? = dao.getRandomWord()

    override suspend fun getRandomPracticeWord(level: Int): WordEntity? {
        val (minId, maxId) = when (level) {
            1 -> 1000 to 1999
            2 -> 2000 to 2999
            3 -> 3000 to 3999
            4 -> 1 to 602
            else -> 1000 to 1999
        }
        return dao.getRandomWordByLevel(minId, maxId)
    }

    override suspend fun getRandomConfidentPracticeWord(level: Int): WordEntity? {
        val (minId, maxId) = when (level) {
            1 -> 1000 to 1999
            2 -> 2000 to 2999
            3 -> 3000 to 3999
            4 -> 1 to 602
            else -> 1000 to 1999
        }
        return dao.getRandomConfidentWordByLevel(minId, maxId)
    }

    override fun getProgressStats(): Flow<ProgressStats> = flow {
        val raw = dao.getProgressStatsInternal()
        val mastered = raw?.mastered ?: 0
        val learning = raw?.learning ?: 0
        val reviewed = raw?.reviewed ?: 0
        // unseen: total words minus seen (learning+mastered+reviewed)
        val total = dao.getWordCount()
        val seen = learning + mastered + reviewed
        val unseen = (total - seen).coerceAtLeast(0)
        emit(ProgressStats(learning = learning, reviewed = reviewed, mastered = mastered, unseen = unseen))
    }.flowOn(Dispatchers.IO)

    override fun getReviewDashboardData(): Flow<ReviewDashboardData> {
        val nowFlow = flow { emit(System.currentTimeMillis()) }
        val strugglingFlow = dao.observeStrugglingWords()
        val dueFlow = dao.observeDueWordsAll(System.currentTimeMillis())
        val reviewedFlow = dao.observeReviewedWithCounts()

        return combine(strugglingFlow, dueFlow, reviewedFlow) { struggling, due, reviewedRows ->
            val grouped: MutableMap<Int, MutableList<WordEntity>> = mutableMapOf()
            for (row in reviewedRows) {
                val list = grouped.getOrPut(row.reviewCount) { mutableListOf() }
                list += WordEntity(
                    id = row._id,
                    wordId = row.wordId,
                    hanzi = row.hanzi,
                    pinyin = row.pinyin,
                    meaning = row.meaning,
                    fullData = row.fullData,
                )
            }
            ReviewDashboardData(strugglingWords = struggling, dueWords = due, reviewedWordsByCount = grouped)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun updateWordComfort(wordId: String, comfortLevel: Int) {
        dao.updateWordComfort(wordId, comfortLevel)
    }

    override suspend fun submitReviewAnswer(wordId: String, isCorrect: Boolean, markAsMastered: Boolean) {
        val existing = dao.getWordProgress(wordId)
        val status = existing?.status ?: "Learning"
        val srsLevel = existing?.srsLevel ?: 0
        val nextReviewAt = existing?.nextReviewAt ?: System.currentTimeMillis()
        val updated = calculateNextReview(
            Progress(
                word_id = wordId,
                status = status,
                srsLevel = srsLevel,
                nextReviewAt = nextReviewAt,
            ),
            isCorrect,
            markAsMastered,
        )
        dao.updateWordProgress(
            UserWordProgressEntity(
                wordId = updated.word_id,
                status = updated.status,
                srsLevel = updated.srsLevel,
                nextReviewAt = updated.nextReviewAt,
                comfortLevel = existing?.comfortLevel,
                reviewCount = existing?.reviewCount,
                timesCorrect = existing?.timesCorrect,
                timesIncorrect = existing?.timesIncorrect,
                firstSeenAt = existing?.firstSeenAt,
                isStruggling = existing?.isStruggling,
            ),
            isCorrect
        )
    }

    // ----- SRS logic ported -----
    data class Progress(
        val word_id: String,
        val status: String,
        val srsLevel: Int,
        val nextReviewAt: Long?,
    )

    private fun calculateNextReview(progress: Progress, isCorrect: Boolean, markAsMastered: Boolean = false): Progress {
        if (markAsMastered) {
            return progress.copy(srsLevel = 8, status = "Mastered", nextReviewAt = null)
        }
        var level = progress.srsLevel
        level = if (isCorrect) (level + 1).coerceAtMost(8) else (level - 2).coerceAtLeast(0)
        val status = if (level >= 8) "Mastered" else "Learning"
        val next: Long? = if (status == "Learning") {
            val intervals = intArrayOf(4, 8, 24, 72, 168, 336, 720, 2880) // hours
            val idx = level.coerceIn(0, intervals.lastIndex)
            System.currentTimeMillis() + intervals[idx] * 60L * 60L * 1000L
        } else null
        return progress.copy(srsLevel = level, status = status, nextReviewAt = next)
    }
}
 
