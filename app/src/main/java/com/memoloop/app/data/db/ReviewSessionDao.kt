package com.memoloop.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.memoloop.app.data.model.ReviewSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewSessionDao {

    @Insert
    suspend fun insert(session: ReviewSession): Long

    @Query("SELECT * FROM review_sessions ORDER BY dateMillis DESC")
    fun getAllSessions(): Flow<List<ReviewSession>>

    @Query("SELECT * FROM review_sessions WHERE dateKey LIKE :yearMonth || '%' ORDER BY dateMillis ASC")
    suspend fun getSessionsByMonth(yearMonth: String): List<ReviewSession>

    @Query("SELECT * FROM review_sessions ORDER BY dateMillis DESC LIMIT 1")
    suspend fun getLastSession(): ReviewSession?

    @Query("SELECT DISTINCT dateKey FROM review_sessions ORDER BY dateKey DESC")
    suspend fun getAllDistinctDateKeys(): List<String>

    @Query("SELECT COUNT(*) FROM review_sessions WHERE dateKey = :dateKey")
    suspend fun countByDateKey(dateKey: String): Int

    @Query("DELETE FROM review_sessions")
    suspend fun deleteAll()
}
