package com.memoloop.app.data.model

import androidx.room.Entity

@Entity(tableName = "word_progress", primaryKeys = ["wordId", "difficulty"])
data class WordProgress(
    val wordId: Int,
    val difficulty: String,        // DifficultyLevel.key
    val easeFactor: Double = 2.5,  // SM-2 ease factor (min 1.3)
    val interval: Int = 0,         // Days until next review
    val repetitions: Int = 0,      // Consecutive correct answers
    val nextReviewDate: String = "",// "yyyy-MM-dd", empty = new word
    val lastReviewDate: String = "",
    val bookmarked: Boolean = false // For feature 2
)
