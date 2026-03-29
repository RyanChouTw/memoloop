package com.memoloop.app.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.memoloop.app.data.db.AppDatabase
import com.memoloop.app.data.model.Word
import com.memoloop.app.data.repository.DifficultyManager
import com.memoloop.app.data.repository.ScoreRepository
import com.memoloop.app.data.repository.SessionRepository
import com.memoloop.app.data.repository.WordRepository
import kotlinx.coroutines.launch

data class QuizQuestion(
    val word: Word,
    val options: List<String>,   // 4 English words, shuffled
    val correctIndex: Int        // index of correct answer in options
)

data class QuizState(
    val currentQuestion: QuizQuestion,
    val questionIndex: Int,      // 0-based
    val totalQuestions: Int
)

data class QuizFinalResult(
    val correctCount: Int,
    val totalCount: Int,
    val passed: Boolean,
    val pointsEarned: Int,
    val level: Int
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val QUIZ_SIZE = 10
    }

    private val wordRepo = WordRepository(application)
    private val difficultyManager = DifficultyManager(application)
    private val db = AppDatabase.getInstance(application)
    private val sessionRepo = SessionRepository(db.reviewSessionDao())
    private val scoreRepo = ScoreRepository(db.quizResultDao(), db.listeningResultDao(), db.monthlyScoreDao(), application)

    private val _quizState = MutableLiveData<QuizState>()
    val quizState: LiveData<QuizState> = _quizState

    private val _answerResult = MutableLiveData<AnswerFeedback?>()
    val answerResult: LiveData<AnswerFeedback?> = _answerResult

    private val _quizComplete = MutableLiveData<QuizFinalResult?>()
    val quizComplete: LiveData<QuizFinalResult?> = _quizComplete

    private var questions = listOf<QuizQuestion>()
    private var currentIndex = 0
    private var correctCount = 0
    private var allCorrect = true

    data class AnswerFeedback(
        val selectedIndex: Int,
        val correctIndex: Int,
        val isCorrect: Boolean
    )

    fun startQuiz() {
        val difficulty = difficultyManager.current
        val allWords = wordRepo.getAllWords(difficulty)
        val quizWords = allWords.shuffled().take(QUIZ_SIZE)

        questions = quizWords.map { word ->
            val distractors = allWords
                .filter { it.id != word.id }
                .shuffled()
                .take(3)
                .map { it.word }

            val options = (distractors + word.word).shuffled()
            val correctIdx = options.indexOf(word.word)

            QuizQuestion(word = word, options = options, correctIndex = correctIdx)
        }

        currentIndex = 0
        correctCount = 0
        allCorrect = true
        _quizComplete.value = null
        _answerResult.value = null
        showCurrentQuestion()
    }

    fun submitAnswer(selectedIndex: Int) {
        if (currentIndex >= questions.size) return

        val question = questions[currentIndex]
        val isCorrect = selectedIndex == question.correctIndex

        if (isCorrect) {
            correctCount++
        } else {
            allCorrect = false
        }

        _answerResult.value = AnswerFeedback(
            selectedIndex = selectedIndex,
            correctIndex = question.correctIndex,
            isCorrect = isCorrect
        )
    }

    fun nextQuestion() {
        currentIndex++
        _answerResult.value = null

        if (currentIndex >= questions.size) {
            finishQuiz()
        } else {
            showCurrentQuestion()
        }
    }

    private fun showCurrentQuestion() {
        _quizState.value = QuizState(
            currentQuestion = questions[currentIndex],
            questionIndex = currentIndex,
            totalQuestions = questions.size
        )
    }

    private fun finishQuiz() {
        viewModelScope.launch {
            val streak = sessionRepo.getCurrentStreak()
            val totalSessions = sessionRepo.getTotalSessions()
            val result = scoreRepo.saveQuizResult(correctCount, QUIZ_SIZE, streak, totalSessions)

            _quizComplete.value = QuizFinalResult(
                correctCount = correctCount,
                totalCount = QUIZ_SIZE,
                passed = result.passed,
                pointsEarned = result.pointsEarned,
                level = ScoreRepository.getLevel(streak, totalSessions)
            )
        }
    }
}
