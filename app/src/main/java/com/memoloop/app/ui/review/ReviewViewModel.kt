package com.memoloop.app.ui.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.memoloop.app.data.db.AppDatabase
import com.memoloop.app.data.model.Word
import com.memoloop.app.data.repository.DifficultyManager
import com.memoloop.app.data.repository.SessionRepository
import com.memoloop.app.data.repository.WordProgressRepository
import com.memoloop.app.data.repository.WordRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val SESSION_SIZE = 30
    }

    private val wordRepo = WordRepository(application)
    private val difficultyManager = DifficultyManager(application)
    private val db = AppDatabase.getInstance(application)
    private val sessionRepo = SessionRepository(db.reviewSessionDao())
    private val progressRepo = WordProgressRepository(db.wordProgressDao())

    private val queue = ArrayDeque<Word>()

    private val _currentCard = MutableLiveData<Word?>()
    val currentCard: LiveData<Word?> = _currentCard

    private val _queueSize = MutableLiveData<Int>()
    val queueSize: LiveData<Int> = _queueSize

    private val _initialSize = MutableLiveData<Int>()
    val initialSize: LiveData<Int> = _initialSize

    private val _elapsedSeconds = MutableLiveData<Long>(0L)
    val elapsedSeconds: LiveData<Long> = _elapsedSeconds

    private val _sessionComplete = MutableLiveData<Boolean>(false)
    val sessionComplete: LiveData<Boolean> = _sessionComplete

    private val _unlockedPrizeIndex = MutableLiveData<Int>(-1)
    val unlockedPrizeIndex: LiveData<Int> = _unlockedPrizeIndex

    private val _isBookmarked = MutableLiveData<Boolean>(false)
    val isBookmarked: LiveData<Boolean> = _isBookmarked

    private var timerJob: Job? = null
    private var startTimeMillis: Long = 0L

    private val prizeThresholds = listOf(0, 3, 7, 14, 30)

    fun startSession(bookmarkOnly: Boolean = false) {
        viewModelScope.launch {
            val difficulty = difficultyManager.current
            val allWords = wordRepo.getAllWords(difficulty)
            val sessionWords = if (bookmarkOnly) {
                progressRepo.getBookmarkedWords(allWords, difficulty.key)
                    .shuffled()
                    .take(SESSION_SIZE)
            } else {
                progressRepo.buildSession(allWords, difficulty.key, SESSION_SIZE)
            }

            if (sessionWords.isEmpty()) {
                _sessionComplete.value = true
                return@launch
            }

            queue.clear()
            queue.addAll(sessionWords)
            _initialSize.value = sessionWords.size
            _queueSize.value = queue.size
            _elapsedSeconds.value = 0L
            _sessionComplete.value = false

            startTimeMillis = System.currentTimeMillis()
            startTimer()
            showNext()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value = (System.currentTimeMillis() - startTimeMillis) / 1000
            }
        }
    }

    private fun showNext() {
        if (queue.isEmpty()) {
            timerJob?.cancel()
            _currentCard.value = null
            _sessionComplete.value = true
        } else {
            _currentCard.value = queue.first()
            _queueSize.value = queue.size
        }
    }

    /** Again: insert after position 1, record as quality 0 */
    fun onAgain() {
        val card = queue.removeFirst()
        recordProgress(card, 0)
        if (queue.size >= 1) {
            queue.add(1, card)
        } else {
            queue.addFirst(card)
        }
        showNext()
    }

    /** Hard: insert after position 2, record as quality 1 */
    fun onHard() {
        val card = queue.removeFirst()
        recordProgress(card, 1)
        val insertAt = minOf(2, queue.size)
        queue.add(insertAt, card)
        showNext()
    }

    /** Good: move to end of queue, record as quality 2 */
    fun onGood() {
        val card = queue.removeFirst()
        recordProgress(card, 2)
        queue.addLast(card)
        showNext()
    }

    /** Easy: remove permanently, record as quality 3 */
    fun onEasy() {
        val card = queue.removeFirst()
        recordProgress(card, 3)
        showNext()
    }

    private fun recordProgress(word: Word, quality: Int) {
        viewModelScope.launch {
            progressRepo.recordResponse(word.id, difficultyManager.current.key, quality)
        }
    }

    fun checkBookmark(wordId: Int) {
        viewModelScope.launch {
            _isBookmarked.value = progressRepo.isBookmarked(wordId, difficultyManager.current.key)
        }
    }

    fun toggleBookmark(wordId: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val newState = progressRepo.toggleBookmark(wordId, difficultyManager.current.key)
            _isBookmarked.value = newState
            callback(newState)
        }
    }

    fun saveSession() {
        viewModelScope.launch {
            val duration = _elapsedSeconds.value ?: 0L
            val streakBefore = sessionRepo.getCurrentStreak()
            val totalBefore = sessionRepo.getTotalSessions()

            sessionRepo.saveSession(duration)

            val streakAfter = sessionRepo.getCurrentStreak()
            val totalAfter = sessionRepo.getTotalSessions()

            if (totalBefore == 0 && totalAfter >= 1) {
                _unlockedPrizeIndex.value = 0
                return@launch
            }

            var newPrize = -1
            for (i in 1..4) {
                val threshold = prizeThresholds[i]
                if (streakBefore < threshold && streakAfter >= threshold) {
                    newPrize = i
                }
            }
            _unlockedPrizeIndex.value = newPrize
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
