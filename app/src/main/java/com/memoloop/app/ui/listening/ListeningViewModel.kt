package com.memoloop.app.ui.listening

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.memoloop.app.data.db.AppDatabase
import com.memoloop.app.data.model.ListeningStory
import com.memoloop.app.data.repository.DifficultyManager
import com.memoloop.app.data.repository.ListeningRepository
import com.memoloop.app.data.repository.ScoreRepository
import com.memoloop.app.data.repository.SessionRepository
import kotlinx.coroutines.launch

data class ListeningState(
    val story: ListeningStory,
    val storyIndex: Int,        // 0-based
    val totalStories: Int
)

data class ListeningFinalResult(
    val correctCount: Int,
    val totalCount: Int,
    val passed: Boolean,
    val pointsEarned: Int,
    val level: Int
)

class ListeningViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val SESSION_SIZE = 5
    }

    private val listeningRepo = ListeningRepository(application)
    private val difficultyManager = DifficultyManager(application)
    private val db = AppDatabase.getInstance(application)
    private val sessionRepo = SessionRepository(db.reviewSessionDao())
    private val scoreRepo = ScoreRepository(
        db.quizResultDao(), db.listeningResultDao(), db.monthlyScoreDao(), application
    )

    private val _state = MutableLiveData<ListeningState>()
    val state: LiveData<ListeningState> = _state

    private val _answerFeedback = MutableLiveData<AnswerFeedback?>()
    val answerFeedback: LiveData<AnswerFeedback?> = _answerFeedback

    private val _complete = MutableLiveData<ListeningFinalResult?>()
    val complete: LiveData<ListeningFinalResult?> = _complete

    private var stories = listOf<ListeningStory>()
    private var currentIndex = 0
    private var correctCount = 0

    // Whether this is a quiz (scored, daily limit) or practice (unscored)
    var isQuizMode = false

    data class AnswerFeedback(
        val selectedIndex: Int,
        val correctIndex: Int,
        val isCorrect: Boolean
    )

    fun startSession() {
        stories = listeningRepo.getRandomStories(SESSION_SIZE, difficultyManager.current)
        currentIndex = 0
        correctCount = 0
        _complete.value = null
        _answerFeedback.value = null
        showCurrent()
    }

    fun submitAnswer(selectedIndex: Int) {
        if (currentIndex >= stories.size) return
        val story = stories[currentIndex]
        val isCorrect = selectedIndex == story.correctIndex
        if (isCorrect) correctCount++

        _answerFeedback.value = AnswerFeedback(
            selectedIndex = selectedIndex,
            correctIndex = story.correctIndex,
            isCorrect = isCorrect
        )
    }

    fun nextStory() {
        currentIndex++
        _answerFeedback.value = null

        if (currentIndex >= stories.size) {
            finishSession()
        } else {
            showCurrent()
        }
    }

    private fun showCurrent() {
        _state.value = ListeningState(
            story = stories[currentIndex],
            storyIndex = currentIndex,
            totalStories = stories.size
        )
    }

    private fun finishSession() {
        if (isQuizMode) {
            viewModelScope.launch {
                val streak = sessionRepo.getCurrentStreak()
                val totalSessions = sessionRepo.getTotalSessions()
                val result = scoreRepo.saveListeningResult(
                    correctCount, SESSION_SIZE, streak, totalSessions
                )
                _complete.value = ListeningFinalResult(
                    correctCount = correctCount,
                    totalCount = SESSION_SIZE,
                    passed = result.passed,
                    pointsEarned = result.pointsEarned,
                    level = ScoreRepository.getLevel(streak, totalSessions)
                )
            }
        } else {
            _complete.value = ListeningFinalResult(
                correctCount = correctCount,
                totalCount = SESSION_SIZE,
                passed = correctCount == SESSION_SIZE,
                pointsEarned = 0,
                level = 0
            )
        }
    }
}
