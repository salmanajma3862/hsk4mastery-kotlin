package com.salmanajmal.hsk4mastery.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.salmanajmal.hsk4mastery.data.local.model.*

@Dao
interface WordDao {

    // saveWords -> insert/upsert list
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    // Upsert a single progress row
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: UserWordProgressEntity)

    // getAllWords - lightweight list with progress join
    @Query(
        """
        SELECT DISTINCT w._id, w.wordId, w.hanzi, w.pinyin, w.meaning,
               p.status as status, p.comfortLevel as comfortLevel
        FROM words w
        LEFT JOIN user_word_progress p ON p.word_id = w._id
        WHERE w.wordId >= :minId AND w.wordId <= :maxId
        ORDER BY w.wordId ASC
        """
    )
    fun getAllWords(minId: Int, maxId: Int): Flow<List<WordBasic>>

    // getWordById - full entity by PK
    @Query("SELECT * FROM words WHERE _id = :wordId LIMIT 1")
    suspend fun getWordById(wordId: String): WordEntity?

    // Progress by word id
    @Query("SELECT * FROM user_word_progress WHERE word_id = :wordId LIMIT 1")
    suspend fun getWordProgress(wordId: String): UserWordProgressEntity?

    // updateWordProgress - mirror the Expo behavior using a read + upsert
    @Transaction
    suspend fun updateWordProgress(progress: UserWordProgressEntity, isCorrect: Boolean?) {
        val prev = getWordProgress(progress.wordId)
        val reviewCount = (prev?.reviewCount ?: 0) + 1
        val timesCorrect = (prev?.timesCorrect ?: 0) + (if (isCorrect == true) 1 else 0)
        val timesIncorrect = (prev?.timesIncorrect ?: 0) + (if (isCorrect == false) 1 else 0)
        val isStruggling = if (isCorrect == true) 0 else 1

        upsertProgress(
            progress.copy(
                reviewCount = reviewCount,
                timesCorrect = timesCorrect,
                timesIncorrect = timesIncorrect,
                isStruggling = isStruggling,
            )
        )
    }

    // getProgressStats - returns mastered / reviewed / learning counts
    @Query(
        """
        SELECT 
          SUM(CASE WHEN LOWER(status) = 'mastered' THEN 1 ELSE 0 END) as mastered,
          SUM(CASE WHEN LOWER(status) <> 'mastered' AND COALESCE(reviewCount, 0) > 0 THEN 1 ELSE 0 END) as reviewed,
          SUM(CASE WHEN LOWER(status) <> 'mastered' AND COALESCE(reviewCount, 0) = 0 AND LOWER(status) = 'learning' THEN 1 ELSE 0 END) as learning
        FROM user_word_progress
        """
    )
    suspend fun getProgressStatsInternal(): ProgressStatsRow?

    // Helper projection for stats
    data class ProgressStatsRow(
        val mastered: Int?,
        val learning: Int?,
        val reviewed: Int?,
    )

    // getWordsForReview(limit)
    @Query(
        """
        SELECT w.* FROM user_word_progress p
        JOIN words w ON w._id = p.word_id
        WHERE p.nextReviewAt IS NOT NULL AND p.nextReviewAt <= :now
        ORDER BY p.nextReviewAt ASC
        LIMIT :limit
        """
    )
    suspend fun getDueWords(now: Long, limit: Int): List<WordEntity>

    @Query(
        """
        SELECT w.* FROM words w
        LEFT JOIN user_word_progress p ON p.word_id = w._id
        WHERE p.word_id IS NULL
        ORDER BY w.wordId ASC
        LIMIT :limit
        """
    )
    suspend fun getNewWords(limit: Int): List<WordEntity>

    // getWordCount
    @Query("SELECT COUNT(*) FROM words WHERE TRIM(_id) <> '' AND TRIM(hanzi) <> '' AND TRIM(pinyin) <> '' AND TRIM(meaning) <> ''")
    suspend fun getWordCount(): Int

    // Random word
    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWord(): WordEntity?

    // Random word by HSK level range
    @Query("SELECT * FROM words WHERE wordId >= :minId AND wordId <= :maxId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWordByLevel(minId: Int, maxId: Int): WordEntity?

    // updateWordComfort
    @Transaction
    suspend fun updateWordComfort(wordId: String, comfortLevel: Int) {
        val existing = getWordProgress(wordId)
        val now = System.currentTimeMillis()
        val firstSeen = existing?.firstSeenAt ?: now
        val status = existing?.status ?: "Learning"
        val entity = UserWordProgressEntity(
            wordId = wordId,
            status = status,
            srsLevel = existing?.srsLevel ?: 0,
            nextReviewAt = existing?.nextReviewAt,
            comfortLevel = comfortLevel,
            reviewCount = existing?.reviewCount ?: 0,
            timesCorrect = existing?.timesCorrect ?: 0,
            timesIncorrect = existing?.timesIncorrect ?: 0,
            firstSeenAt = firstSeen,
            isStruggling = existing?.isStruggling ?: 0,
        )
        upsertProgress(entity)
    }

    // Review dashboard segments
    @Query(
        """
        SELECT w.* FROM user_word_progress p
        JOIN words w ON w._id = p.word_id
        WHERE p.isStruggling = 1
        ORDER BY COALESCE(p.timesIncorrect, 0) DESC, COALESCE(w.wordId, 0) ASC
        """
    )
    suspend fun getStrugglingWords(): List<WordEntity>

    @Query(
        """
        SELECT w.* FROM user_word_progress p
        JOIN words w ON w._id = p.word_id
        WHERE p.nextReviewAt IS NOT NULL AND p.nextReviewAt <= :now
        ORDER BY p.nextReviewAt ASC
        """
    )
    suspend fun getDueWordsAll(now: Long): List<WordEntity>

    @Query(
        """
        SELECT w.*, COALESCE(p.reviewCount, 0) as reviewCount
        FROM words w
        JOIN user_word_progress p ON p.word_id = w._id
        ORDER BY p.reviewCount DESC, COALESCE(w.wordId, 0) ASC
        """
    )
    suspend fun getReviewedWithCounts(): List<ReviewedWordRow>

    data class ReviewedWordRow(
        val _id: String,
        val wordId: Int?,
        val hanzi: String,
        val pinyin: String,
        val meaning: String,
        val fullData: String,
        val reviewCount: Int,
    )

    // Reactive variants for live updates in dashboard
    @Query(
        """
        SELECT w.* FROM user_word_progress p
        JOIN words w ON w._id = p.word_id
        WHERE p.isStruggling = 1
        ORDER BY COALESCE(p.timesIncorrect, 0) DESC, COALESCE(w.wordId, 0) ASC
        """
    )
    fun observeStrugglingWords(): Flow<List<WordEntity>>

    @Query(
        """
        SELECT w.* FROM user_word_progress p
        JOIN words w ON w._id = p.word_id
        WHERE p.nextReviewAt IS NOT NULL AND p.nextReviewAt <= :now
        ORDER BY p.nextReviewAt ASC
        """
    )
    fun observeDueWordsAll(now: Long): Flow<List<WordEntity>>

    @Query(
        """
        SELECT w.*, COALESCE(p.reviewCount, 0) as reviewCount
        FROM words w
        JOIN user_word_progress p ON p.word_id = w._id
        ORDER BY p.reviewCount DESC, COALESCE(w.wordId, 0) ASC
        """
    )
    fun observeReviewedWithCounts(): Flow<List<ReviewedWordRow>>

    // Neighboring words by numeric wordId for contextual navigation within the same HSK level
    @Query(
        """
        SELECT _id, wordId, hanzi, pinyin, meaning FROM words 
        WHERE wordId < :wordId 
        AND CASE 
            WHEN :wordId BETWEEN 1 AND 600 THEN wordId BETWEEN 1 AND 600
            WHEN :wordId BETWEEN 1000 AND 1149 THEN wordId BETWEEN 1000 AND 1149
            WHEN :wordId BETWEEN 2000 AND 2149 THEN wordId BETWEEN 2000 AND 2149
            WHEN :wordId BETWEEN 3000 AND 3299 THEN wordId BETWEEN 3000 AND 3299
            ELSE 1=1
        END
        ORDER BY wordId DESC LIMIT :limit
        """
    )
    suspend fun getPreviousWords(wordId: Int, limit: Int): List<WordBasic>

    @Query(
        """
        SELECT _id, wordId, hanzi, pinyin, meaning FROM words 
        WHERE wordId > :wordId 
        AND CASE 
            WHEN :wordId BETWEEN 1 AND 600 THEN wordId BETWEEN 1 AND 600
            WHEN :wordId BETWEEN 1000 AND 1049 THEN wordId BETWEEN 1000 AND 1049
            WHEN :wordId BETWEEN 2000 AND 2049 THEN wordId BETWEEN 2000 AND 2049
            WHEN :wordId BETWEEN 3000 AND 3049 THEN wordId BETWEEN 3000 AND 3049
            ELSE 1=1
        END
        ORDER BY wordId ASC LIMIT :limit
        """
    )
    suspend fun getNextWords(wordId: Int, limit: Int): List<WordBasic>
}
