package com.memoloop.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.memoloop.app.data.db.AppDatabase
import com.memoloop.app.data.repository.SessionRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepo = SessionRepository(
        AppDatabase.getInstance(application).reviewSessionDao()
    )

    private val _streak = MutableLiveData<Int>()
    val streak: LiveData<Int> = _streak

    fun loadStreak() {
        viewModelScope.launch {
            _streak.value = sessionRepo.getCurrentStreak()
        }
    }

    fun resetData() {
        viewModelScope.launch {
            sessionRepo.resetAll()
            _streak.value = 0
        }
    }
}
