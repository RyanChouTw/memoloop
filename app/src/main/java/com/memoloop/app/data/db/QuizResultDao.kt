package com.memoloop.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.memoloop.app.data.model.QuizResult

@Dao
interface QuizResultDao {

    @Insert
    suspend fun insert(result: QuizResult): Long

    @Query("SELECT COALESCE(SUM(pointsEarned), 0) FROM quiz_results WHERE dateKey LIKE :yearMonth || '%'")
    suspend fun getMonthlyPoints(yearMonth: String): Int

    @Query("SELECT * FROM quiz_results WHERE dateKey LIKE :yearMonth || '%' ORDER BY dateMillis DESC")
    suspend fun getByMonth(yearMonth: String): List<QuizResult>

    @Query("SELECT COUNT(*) FROM quiz_results WHERE dateKey = :dateKey")
    suspend fun countByDateKey(dateKey: String): Int

    @Query("SELECT * FROM quiz_results WHERE dateKey = :dateKey ORDER BY dateMillis DESC LIMIT 1")
    suspend fun getByDateKey(dateKey: String): QuizResult?

    @Query("DELETE FROM quiz_results")
    suspend fun deleteAll()
}
