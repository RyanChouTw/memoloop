package com.memoloop.app.data.repository

import android.content.Context
import com.memoloop.app.data.db.ListeningResultDao
import com.memoloop.app.data.db.MonthlyScoreDao
import com.memoloop.app.data.db.QuizResultDao
import com.memoloop.app.data.model.ListeningResult
import com.memoloop.app.data.model.MonthlyScore
import com.memoloop.app.data.model.QuizResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScoreRepository(
    private val quizResultDao: QuizResultDao,
    private val listeningResultDao: ListeningResultDao,
    private val monthlyScoreDao: MonthlyScoreDao,
    context: Context
) {

    companion object {
        private const val PREFS_NAME = "score_prefs"
        private const val KEY_LAST_ARCHIVED_MONTH = "last_archived_month"

        const val PASS_RATE = 0.8

        // Points per level, indexed by prize tier (0=rookie..4=platinum)
        val POINTS_BY_LEVEL = intArrayOf(1, 3, 7, 15, 30)

        fun isPassed(correctCount: Int, totalCount: Int): Boolean {
            return correctCount >= (totalCount * PASS_RATE).toInt()
        }

        fun getLevel(streak: Int, totalSessions: Int): Int {
            if (totalSessions < 1) return 0
            return when {
                streak >= 30 -> 4  // platinum
                streak >= 14 -> 3  // gold
                streak >= 7  -> 2  // silver
                streak >= 3  -> 1  // bronze
                else         -> 0  // rookie
            }
        }

        fun getPointsForLevel(level: Int): Int {
            return POINTS_BY_LEVEL.getOrElse(level) { 1 }
        }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    /**
     * Check if we need to archive last month's score and reset.
     * Should be called on app startup or before reading current month's points.
     */
    suspend fun archiveIfNewMonth() {
        val currentMonth = monthFormat.format(Date())
        val lastArchived = prefs.getString(KEY_LAST_ARCHIVED_MONTH, null)

        if (lastArchived != null && lastArchived < currentMonth) {
            // Archive the previous month(s) that haven't been archived yet
            val cal = Calendar.getInstance()
            cal.time = monthFormat.parse(lastArchived)!!
            cal.add(Calendar.MONTH, 1)

            while (monthFormat.format(cal.time) < currentMonth) {
                val ym = monthFormat.format(cal.time)
                archiveMonth(ym)
                cal.add(Calendar.MONTH, 1)
            }
            // Archive the month just before current
            val prevCal = Calendar.getInstance()
            prevCal.time = monthFormat.parse(currentMonth)!!
            prevCal.add(Calendar.MONTH, -1)
            val prevMonth = monthFormat.format(prevCal.time)
            if (prevMonth >= lastArchived) {
                archiveMonth(prevMonth)
            }
        }

        // Mark current month as the latest we've checked
        prefs.edit().putString(KEY_LAST_ARCHIVED_MONTH, currentMonth).apply()
    }

    private suspend fun archiveMonth(yearMonth: String) {
        val existing = monthlyScoreDao.getByYearMonth(yearMonth)
        if (existing != null) return // already archived

        val vocabPoints = quizResultDao.getMonthlyPoints(yearMonth)
        val listeningPoints = listeningResultDao.getMonthlyPoints(yearMonth)
        val total = vocabPoints + listeningPoints
        if (total > 0) {
            monthlyScoreDao.insert(MonthlyScore(yearMonth = yearMonth, totalPoints = total))
        }
    }

    suspend fun saveQuizResult(correctCount: Int, totalCount: Int, streak: Int, totalSessions: Int): QuizResult {
        val now = System.currentTimeMillis()
        val dateKey = dateFormat.format(Date(now))
        val passed = isPassed(correctCount, totalCount)
        val level = getLevel(streak, totalSessions)
        val points = if (passed) getPointsForLevel(level) else 0

        val result = QuizResult(
            dateMillis = now,
            dateKey = dateKey,
            correctCount = correctCount,
            totalCount = totalCount,
            passed = passed,
            pointsEarned = points
        )
        quizResultDao.insert(result)

        // Ensure current month is tracked
        val currentMonth = monthFormat.format(Date())
        prefs.edit().putString(KEY_LAST_ARCHIVED_MONTH, currentMonth).apply()

        return result
    }

    suspend fun saveListeningResult(correctCount: Int, totalCount: Int, streak: Int, totalSessions: Int): ListeningResult {
        val now = System.currentTimeMillis()
        val dateKey = dateFormat.format(Date(now))
        val passed = isPassed(correctCount, totalCount)
        val level = getLevel(streak, totalSessions)
        val points = if (passed) getPointsForLevel(level) else 0

        val result = ListeningResult(
            dateMillis = now,
            dateKey = dateKey,
            correctCount = correctCount,
            totalCount = totalCount,
            passed = passed,
            pointsEarned = points
        )
        listeningResultDao.insert(result)

        val currentMonth = monthFormat.format(Date())
        prefs.edit().putString(KEY_LAST_ARCHIVED_MONTH, currentMonth).apply()

        return result
    }

    suspend fun getCurrentMonthPoints(): Int {
        archiveIfNewMonth()
        val currentMonth = monthFormat.format(Date())
        val vocabPoints = quizResultDao.getMonthlyPoints(currentMonth)
        val listeningPoints = listeningResultDao.getMonthlyPoints(currentMonth)
        return vocabPoints + listeningPoints
    }

    suspend fun getArchivedScores(): List<MonthlyScore> {
        return monthlyScoreDao.getAll()
    }
}
