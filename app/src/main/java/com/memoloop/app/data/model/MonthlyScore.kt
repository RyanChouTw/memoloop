package com.memoloop.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_scores")
data class MonthlyScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val yearMonth: String,      // "yyyy-MM" e.g. "2026-03"
    val totalPoints: Int        // accumulated points for that month
)
