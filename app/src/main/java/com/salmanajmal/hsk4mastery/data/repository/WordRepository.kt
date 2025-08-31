package com.salmanajmal.hsk4mastery.data.repository

import kotlinx.coroutines.flow.Flow
import com.salmanajmal.hsk4mastery.data.local.model.WordBasic
import com.salmanajmal.hsk4mastery.data.local.model.WordEntity

interface WordRepository {
    // Words
    fun getAllWords(): Flow<List<WordBasic>>
    fun getWordDetails(wordId: String): Flow<WordEntity?>

    // Seeding
    suspend fun startDatabaseSeedingIfNeeded()

    // Random/practice
    suspend fun getRandomPracticeWord(): WordEntity?

    // Progress & stats
    fun getProgressStats(): Flow<ProgressStats>

    // Review dashboard
    fun getReviewDashboardData(): Flow<ReviewDashboardData>

    // Updates
    suspend fun updateWordComfort(wordId: String, comfortLevel: Int)
    suspend fun submitReviewAnswer(wordId: String, isCorrect: Boolean, markAsMastered: Boolean = false)
}

data class ProgressStats(
    val learning: Int,
    val reviewed: Int,
    val mastered: Int,
    val unseen: Int,
)

data class ReviewDashboardData(
    val strugglingWords: List<WordEntity>,
    val dueWords: List<WordEntity>,
    val reviewedWordsByCount: Map<Int, List<WordEntity>>,
)
 
