package com.memoloop.app.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.memoloop.app.data.db.AppDatabase
import com.memoloop.app.data.model.ReviewSession
import com.memoloop.app.data.repository.SessionRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepo = SessionRepository(
        AppDatabase.getInstance(application).reviewSessionDao()
    )

    private val cal = Calendar.getInstance()

    private val _year = MutableLiveData(cal.get(Calendar.YEAR))
    val year: LiveData<Int> = _year

    private val _month = MutableLiveData(cal.get(Calendar.MONTH) + 1)
    val month: LiveData<Int> = _month

    private val _sessions = MutableLiveData<List<ReviewSession>>()
    val sessions: LiveData<List<ReviewSession>> = _sessions

    fun loadMonth() {
        viewModelScope.launch {
            val y = _year.value!!
            val m = _month.value!!
            _sessions.value = sessionRepo.getSessionsByMonth(y, m)
        }
    }

    fun prevMonth() {
        val m = _month.value!!
        val y = _year.value!!
        if (m == 1) {
            _month.value = 12
            _year.value = y - 1
        } else {
            _month.value = m - 1
        }
        loadMonth()
    }

    fun nextMonth() {
        val m = _month.value!!
        val y = _year.value!!
        if (m == 12) {
            _month.value = 1
            _year.value = y + 1
        } else {
            _month.value = m + 1
        }
        loadMonth()
    }
}
