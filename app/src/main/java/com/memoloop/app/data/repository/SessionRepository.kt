package com.memoloop.app.data.repository

import com.memoloop.app.data.db.ReviewSessionDao
import com.memoloop.app.data.model.ReviewSession
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionRepository(private val dao: ReviewSessionDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun saveSession(durationSeconds: Long) {
        val now = System.currentTimeMillis()
        val dateKey = dateFormat.format(Date(now))
        dao.insert(ReviewSession(dateMillis = now, durationSeconds = durationSeconds, dateKey = dateKey))
    }

    fun getAllSessions(): Flow<List<ReviewSession>> = dao.getAllSessions()

    suspend fun getSessionsByMonth(year: Int, month: Int): List<ReviewSession> {
        val monthStr = String.format(Locale.getDefault(), "%04d-%02d", year, month)
        return dao.getSessionsByMonth(monthStr)
    }

    /**
     * Calculates current consecutive-day streak.
     * A streak increments for each calendar day (in descending order) that has at least one session.
     * If today has no session yet, we still count yesterday as streak start.
     */
    suspend fun getCurrentStreak(): Int {
        val dateKeys = dao.getAllDistinctDateKeys()
        if (dateKeys.isEmpty()) return 0

        val today = dateFormat.format(Date())
        val cal = java.util.Calendar.getInstance()
        var streak = 0

        // Start checking from today or yesterday
        val startDate = if (dateKeys.first() == today) today else {
            // Most recent session date
            dateKeys.first()
        }

        val checkCal = java.util.Calendar.getInstance()
        checkCal.time = dateFormat.parse(startDate)!!

        val dateSet = dateKeys.toHashSet()

        while (true) {
            val key = dateFormat.format(checkCal.time)
            if (dateSet.contains(key)) {
                streak++
                checkCal.add(java.util.Calendar.DAY_OF_MONTH, -1)
            } else {
                break
            }
        }
        return streak
    }

    suspend fun getTotalSessions(): Int {
        return dao.getAllDistinctDateKeys().size
    }

    suspend fun resetAll() {
        dao.deleteAll()
    }
}
