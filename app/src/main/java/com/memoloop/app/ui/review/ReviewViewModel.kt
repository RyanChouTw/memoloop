package com.memoloop.app.ui.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.memoloop.app.data.db.AppDatabase
import com.memoloop.app.data.model.DifficultyLevel
import com.memoloop.app.data.model.Word
import com.memoloop.app.data.repository.DifficultyManager
import com.memoloop.app.data.repository.SessionRepository
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
    private val sessionRepo = SessionRepository(
        AppDatabase.getInstance(application).reviewSessionDao()
    )

    // The queue of cards to review
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

    // -1 = no new prize; 0..4 = prize index (rookie, bronze, silver, gold, platinum)
    private val _unlockedPrizeIndex = MutableLiveData<Int>(-1)
    val unlockedPrizeIndex: LiveData<Int> = _unlockedPrizeIndex

    private var timerJob: Job? = null
    private var startTimeMillis: Long = 0L

    // Streak thresholds matching PrizeFragment prize list order
    private val prizeThresholds = listOf(
        0,   // rookie  – needs 1 total session, not streak
        3,   // bronze
        7,   // silver
        14,  // gold
        30   // platinum
    )

    fun startSession() {
        val words = wordRepo.getRandomWords(SESSION_SIZE, difficultyManager.current)
        queue.clear()
        queue.addAll(words)
        _initialSize.value = words.size
        _queueSize.value = queue.size
        _elapsedSeconds.value = 0L
        _sessionComplete.value = false

        startTimeMillis = System.currentTimeMillis()
        startTimer()
        showNext()
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

    /** Again: insert after position 1 (if exists), else append at end */
    fun onAgain() {
        val card = queue.removeFirst()
        if (queue.size >= 1) {
            queue.add(1, card)
        } else {
            queue.addFirst(card)
        }
        showNext()
    }

    /** Hard: insert after position 2 (or at end) */
    fun onHard() {
        val card = queue.removeFirst()
        val insertAt = minOf(2, queue.size)
        queue.add(insertAt, card)
        showNext()
    }

    /** Good: move to end of queue */
    fun onGood() {
        val card = queue.removeFirst()
        queue.addLast(card)
        showNext()
    }

    /** Easy: remove permanently */
    fun onEasy() {
        queue.removeFirst()
        showNext()
    }

    fun saveSession() {
        viewModelScope.launch {
            val duration = _elapsedSeconds.value ?: 0L

            // Streak before saving
            val streakBefore = sessionRepo.getCurrentStreak()
            val totalBefore = sessionRepo.getTotalSessions()

            sessionRepo.saveSession(duration)

            // Streak after saving
            val streakAfter = sessionRepo.getCurrentStreak()
            val totalAfter = sessionRepo.getTotalSessions()

            // Check if rookie prize just unlocked (first ever session)
            if (totalBefore == 0 && totalAfter >= 1) {
                _unlockedPrizeIndex.value = 0
                return@launch
            }

            // Check streak-based prizes (index 1..4), pick highest newly crossed
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
