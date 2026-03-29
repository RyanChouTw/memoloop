package com.memoloop.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long,
    val dateKey: String,        // "yyyy-MM-dd"
    val correctCount: Int,      // 0..10
    val totalCount: Int,        // always 10
    val passed: Boolean,        // true if correctCount == totalCount
    val pointsEarned: Int       // points earned (0 if not passed)
)
