package com.memoloop.app.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.memoloop.app.data.db.AppDatabase
import com.memoloop.app.data.model.ListeningResult
import com.memoloop.app.data.model.QuizResult
import com.memoloop.app.data.repository.ScoreRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class VocabQuizTabState(
    val quizResults: List<QuizResult>,
    val quizDays: Set<Int>,
    val todayHasQuiz: Boolean,
    val todayResult: QuizResult?,
    val monthlyPoints: Int,
    val totalQuizzes: Int,
    val passRate: Int
)

data class ListeningQuizTabState(
    val results: List<ListeningResult>,
    val quizDays: Set<Int>,
    val todayHasQuiz: Boolean,
    val todayResult: ListeningResult?,
    val monthlyPoints: Int,
    val totalQuizzes: Int,
    val passRate: Int
)

class QuizTabViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val scoreRepo = ScoreRepository(
        db.quizResultDao(), db.listeningResultDao(), db.monthlyScoreDao(), application
    )
    private val quizResultDao = db.quizResultDao()
    private val listeningResultDao = db.listeningResultDao()

    private val cal = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _year = MutableLiveData(cal.get(Calendar.YEAR))
    val year: LiveData<Int> = _year

    private val _month = MutableLiveData(cal.get(Calendar.MONTH) + 1)
    val month: LiveData<Int> = _month

    private val _vocabState = MutableLiveData<VocabQuizTabState>()
    val vocabState: LiveData<VocabQuizTabState> = _vocabState

    private val _listeningState = MutableLiveData<ListeningQuizTabState>()
    val listeningState: LiveData<ListeningQuizTabState> = _listeningState

    private val _monthlyPoints = MutableLiveData<Int>(0)
    val monthlyPoints: LiveData<Int> = _monthlyPoints

    fun loadVocab() {
        viewModelScope.launch {
            val y = _year.value!!
            val m = _month.value!!
            val yearMonth = String.format(Locale.getDefault(), "%04d-%02d", y, m)
            val results = quizResultDao.getByMonth(yearMonth)

            val quizDays = results.map { r ->
                val c = Calendar.getInstance()
                c.timeInMillis = r.dateMillis
                c.get(Calendar.DAY_OF_MONTH)
            }.toHashSet()

            val todayKey = dateFormat.format(Date())
            val todayResult = quizResultDao.getByDateKey(todayKey)

            val totalQuizzes = results.size
            val passedCount = results.count { it.passed }
            val passRate = if (totalQuizzes > 0) (passedCount * 100 / totalQuizzes) else 0

            _vocabState.value = VocabQuizTabState(
                quizResults = results,
                quizDays = quizDays,
                todayHasQuiz = todayResult != null,
                todayResult = todayResult,
                monthlyPoints = 0,
                totalQuizzes = totalQuizzes,
                passRate = passRate
            )

            _monthlyPoints.value = scoreRepo.getCurrentMonthPoints()
        }
    }

    fun loadListening() {
        viewModelScope.launch {
            val y = _year.value!!
            val m = _month.value!!
            val yearMonth = String.format(Locale.getDefault(), "%04d-%02d", y, m)
            val results = listeningResultDao.getByMonth(yearMonth)

            val quizDays = results.map { r ->
                val c = Calendar.getInstance()
                c.timeInMillis = r.dateMillis
                c.get(Calendar.DAY_OF_MONTH)
            }.toHashSet()

            val todayKey = dateFormat.format(Date())
            val todayResult = listeningResultDao.getByDateKey(todayKey)

            val totalQuizzes = results.size
            val passedCount = results.count { it.passed }
            val passRate = if (totalQuizzes > 0) (passedCount * 100 / totalQuizzes) else 0

            _listeningState.value = ListeningQuizTabState(
                results = results,
                quizDays = quizDays,
                todayHasQuiz = todayResult != null,
                todayResult = todayResult,
                monthlyPoints = 0,
                totalQuizzes = totalQuizzes,
                passRate = passRate
            )

            _monthlyPoints.value = scoreRepo.getCurrentMonthPoints()
        }
    }

    fun prevMonth() {
        val m = _month.value!!
        val y = _year.value!!
        if (m == 1) { _month.value = 12; _year.value = y - 1 }
        else { _month.value = m - 1 }
    }

    fun nextMonth() {
        val m = _month.value!!
        val y = _year.value!!
        if (m == 12) { _month.value = 1; _year.value = y + 1 }
        else { _month.value = m + 1 }
    }
}
