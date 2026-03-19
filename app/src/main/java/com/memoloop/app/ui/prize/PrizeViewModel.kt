package com.memoloop.app.ui.prize

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.memoloop.app.data.db.AppDatabase
import com.memoloop.app.data.repository.SessionRepository
import kotlinx.coroutines.launch

data class PrizeState(
    val streak: Int,
    val totalSessions: Int
)

class PrizeViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepo = SessionRepository(
        AppDatabase.getInstance(application).reviewSessionDao()
    )

    private val _prizeState = MutableLiveData<PrizeState>()
    val prizeState: LiveData<PrizeState> = _prizeState

    fun load() {
        viewModelScope.launch {
            val streak = sessionRepo.getCurrentStreak()
            val total = sessionRepo.getTotalSessions()
            _prizeState.value = PrizeState(streak, total)
        }
    }
}
