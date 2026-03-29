package com.memoloop.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.memoloop.app.data.model.ListeningResult

@Dao
interface ListeningResultDao {

    @Insert
    suspend fun insert(result: ListeningResult): Long

    @Query("SELECT COALESCE(SUM(pointsEarned), 0) FROM listening_results WHERE dateKey LIKE :yearMonth || '%'")
    suspend fun getMonthlyPoints(yearMonth: String): Int

    @Query("SELECT * FROM listening_results WHERE dateKey LIKE :yearMonth || '%' ORDER BY dateMillis DESC")
    suspend fun getByMonth(yearMonth: String): List<ListeningResult>

    @Query("SELECT COUNT(*) FROM listening_results WHERE dateKey = :dateKey")
    suspend fun countByDateKey(dateKey: String): Int

    @Query("SELECT * FROM listening_results WHERE dateKey = :dateKey ORDER BY dateMillis DESC LIMIT 1")
    suspend fun getByDateKey(dateKey: String): ListeningResult?

    @Query("DELETE FROM listening_results")
    suspend fun deleteAll()
}
