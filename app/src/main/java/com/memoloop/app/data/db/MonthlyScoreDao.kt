package com.memoloop.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.memoloop.app.data.model.MonthlyScore

@Dao
interface MonthlyScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: MonthlyScore): Long

    @Query("SELECT * FROM monthly_scores WHERE yearMonth = :yearMonth LIMIT 1")
    suspend fun getByYearMonth(yearMonth: String): MonthlyScore?

    @Query("SELECT * FROM monthly_scores ORDER BY yearMonth DESC")
    suspend fun getAll(): List<MonthlyScore>

    @Query("DELETE FROM monthly_scores")
    suspend fun deleteAll()
}
