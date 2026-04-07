package com.memoloop.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.memoloop.app.data.model.WordProgress

@Dao
interface WordProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: WordProgress)

    @Query("SELECT * FROM word_progress WHERE difficulty = :difficulty AND nextReviewDate <= :today ORDER BY nextReviewDate ASC")
    suspend fun getDueWords(difficulty: String, today: String): List<WordProgress>

    @Query("SELECT * FROM word_progress WHERE wordId = :wordId AND difficulty = :difficulty")
    suspend fun getProgress(wordId: Int, difficulty: String): WordProgress?

    @Query("SELECT wordId FROM word_progress WHERE difficulty = :difficulty")
    suspend fun getSeenWordIds(difficulty: String): List<Int>

    @Query("SELECT * FROM word_progress WHERE difficulty = :difficulty AND bookmarked = 1")
    suspend fun getBookmarkedWords(difficulty: String): List<WordProgress>

    @Query("UPDATE word_progress SET bookmarked = :bookmarked WHERE wordId = :wordId AND difficulty = :difficulty")
    suspend fun setBookmarked(wordId: Int, difficulty: String, bookmarked: Boolean)

    @Query("DELETE FROM word_progress")
    suspend fun deleteAll()
}
