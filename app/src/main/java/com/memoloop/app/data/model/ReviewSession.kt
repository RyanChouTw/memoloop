package com.memoloop.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_sessions")
data class ReviewSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long,       // Epoch millis of this session
    val durationSeconds: Long,  // How long the session took
    val dateKey: String         // "yyyy-MM-dd" for easy date lookup
)
