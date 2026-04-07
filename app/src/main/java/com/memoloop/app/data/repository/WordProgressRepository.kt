package com.memoloop.app.data.repository

import com.memoloop.app.data.db.WordProgressDao
import com.memoloop.app.data.model.Word
import com.memoloop.app.data.model.WordProgress
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

class WordProgressRepository(private val dao: WordProgressDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun today(): String = dateFormat.format(Date())

    /**
     * Build a review session: due words first, then unseen words to fill up to [sessionSize].
     */
    suspend fun buildSession(
        allWords: List<Word>,
        difficulty: String,
        sessionSize: Int
    ): List<Word> {
        val today = today()
        val wordMap = allWords.associateBy { it.id }

        // 1. Words due for review
        val dueProgress = dao.getDueWords(difficulty, today)
        val dueWords = dueProgress.mapNotNull { wordMap[it.wordId] }

        // 2. If not enough due words, add unseen words
        val result = dueWords.take(sessionSize).toMutableList()
        if (result.size < sessionSize) {
            val seenIds = dao.getSeenWordIds(difficulty).toSet()
            val unseenWords = allWords.filter { it.id !in seenIds }.shuffled()
            for (word in unseenWords) {
                if (result.size >= sessionSize) break
                result.add(word)
            }
        }

        // 3. If still not enough (all words seen, none due), pick words with oldest review
        if (result.size < sessionSize) {
            val resultIds = result.map { it.id }.toSet()
            val remaining = allWords.filter { it.id !in resultIds }.shuffled()
            for (word in remaining) {
                if (result.size >= sessionSize) break
                result.add(word)
            }
        }

        return result.shuffled()
    }

    /**
     * SM-2 algorithm update after user response.
     * quality: 0=Again, 1=Hard, 2=Good, 3=Easy (mapped from app's 4 buttons)
     */
    suspend fun recordResponse(wordId: Int, difficulty: String, quality: Int) {
        val today = today()
        val existing = dao.getProgress(wordId, difficulty)
        val progress = existing ?: WordProgress(
            wordId = wordId,
            difficulty = difficulty
        )

        val (newEF, newInterval, newReps) = calculateSM2(
            quality = quality,
            easeFactor = progress.easeFactor,
            interval = progress.interval,
            repetitions = progress.repetitions
        )

        val nextDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, newInterval)
        }

        dao.upsert(
            progress.copy(
                easeFactor = newEF,
                interval = newInterval,
                repetitions = newReps,
                nextReviewDate = dateFormat.format(nextDate.time),
                lastReviewDate = today
            )
        )
    }

    /**
     * SM-2 calculation.
     * quality mapping: 0=Again(fail), 1=Hard(pass), 2=Good(pass), 3=Easy(pass)
     */
    private fun calculateSM2(
        quality: Int,
        easeFactor: Double,
        interval: Int,
        repetitions: Int
    ): Triple<Double, Int, Int> {
        // Map to SM-2 scale: Again=0, Hard=3, Good=4, Easy=5
        val q = when (quality) {
            0 -> 0  // Again
            1 -> 3  // Hard
            2 -> 4  // Good
            3 -> 5  // Easy
            else -> 4
        }

        return if (q < 3) {
            // Failed: reset repetitions, review again soon
            Triple(max(1.3, easeFactor - 0.2), 1, 0)
        } else {
            val newReps = repetitions + 1
            val newInterval = when (newReps) {
                1 -> 1
                2 -> 3
                else -> (interval * easeFactor).toInt().coerceAtLeast(1)
            }
            val newEF = max(1.3, easeFactor + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)))
            Triple(newEF, newInterval, newReps)
        }
    }

    suspend fun getBookmarkedWords(
        allWords: List<Word>,
        difficulty: String
    ): List<Word> {
        val bookmarked = dao.getBookmarkedWords(difficulty)
        val ids = bookmarked.map { it.wordId }.toSet()
        return allWords.filter { it.id in ids }
    }

    suspend fun toggleBookmark(wordId: Int, difficulty: String): Boolean {
        val existing = dao.getProgress(wordId, difficulty)
        val newState = !(existing?.bookmarked ?: false)
        if (existing != null) {
            dao.setBookmarked(wordId, difficulty, newState)
        } else {
            dao.upsert(
                WordProgress(
                    wordId = wordId,
                    difficulty = difficulty,
                    bookmarked = newState
                )
            )
        }
        return newState
    }

    suspend fun isBookmarked(wordId: Int, difficulty: String): Boolean {
        return dao.getProgress(wordId, difficulty)?.bookmarked ?: false
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}
