package com.memoloop.app.ui.history

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.memoloop.app.R
import com.memoloop.app.data.model.ReviewSession
import com.memoloop.app.databinding.FragmentHistoryBinding
import java.util.Calendar

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var recordAdapter: RecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupDowHeader()
        setupObservers()
        setupNavButtons()

        viewModel.loadMonth()
    }

    private fun setupRecyclerView() {
        recordAdapter = RecordAdapter()
        binding.rvRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecords.adapter = recordAdapter
    }

    private fun setupDowHeader() {
        val days = listOf(
            getString(R.string.dow_sun), getString(R.string.dow_mon),
            getString(R.string.dow_tue), getString(R.string.dow_wed),
            getString(R.string.dow_thu), getString(R.string.dow_fri),
            getString(R.string.dow_sat)
        )
        binding.gridDow.removeAllViews()
        days.forEach { day ->
            val tv = TextView(requireContext()).apply {
                text = day
                textSize = 13f
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                layoutParams = android.widget.GridLayout.LayoutParams().apply {
                    width = 0
                    columnSpec = android.widget.GridLayout.spec(
                        android.widget.GridLayout.UNDEFINED,
                        android.widget.GridLayout.FILL,
                        1f
                    )
                }
                setPadding(0, 4, 0, 8)
            }
            binding.gridDow.addView(tv)
        }
    }

    private fun setupObservers() {
        viewModel.year.observe(viewLifecycleOwner) { updateHeader() }
        viewModel.month.observe(viewLifecycleOwner) { updateHeader() }
        viewModel.sessions.observe(viewLifecycleOwner) { sessions ->
            buildCalendarGrid(sessions)
            recordAdapter.submitList(sessions.sortedByDescending { it.dateMillis })
            updateStats(sessions)
        }
    }

    private fun updateHeader() {
        val y = viewModel.year.value ?: return
        val m = viewModel.month.value ?: return
        val cal = Calendar.getInstance()
        cal.set(y, m - 1, 1)
        val monthName = java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault()).format(cal.time)
        binding.tvMonthYear.text = getString(R.string.month_year_fmt, monthName, y)
    }

    private fun setupNavButtons() {
        binding.btnPrevMonth.setOnClickListener { viewModel.prevMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
    }

    private fun updateStats(sessions: List<ReviewSession>) {
        // Consecutive days this month
        val distinctDays = sessions.map {
            val c = Calendar.getInstance()
            c.timeInMillis = it.dateMillis
            c.get(Calendar.DAY_OF_MONTH)
        }.toHashSet().size
        binding.tvStatStreak.text = distinctDays.toString()

        // Total review time in minutes
        val totalSec = sessions.sumOf { it.durationSeconds }
        val totalMin = (totalSec / 60).toInt()
        binding.tvStatTime.text = getString(R.string.minutes_fmt, totalMin)

        // Words cleared (approx 30 cards per session)
        binding.tvStatWords.text = (sessions.size * 30).toString()
    }

    private fun buildCalendarGrid(sessions: List<ReviewSession>) {
        binding.gridCalendar.removeAllViews()

        val y = viewModel.year.value ?: return
        val m = viewModel.month.value ?: return

        val sessionDays = sessions.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.dateMillis
            cal.get(Calendar.DAY_OF_MONTH)
        }.toHashSet()

        val cal = Calendar.getInstance()
        cal.set(y, m - 1, 1)
        val firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1  // 0=Sun
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance()
        val isCurrentMonth = today.get(Calendar.YEAR) == y && today.get(Calendar.MONTH) + 1 == m
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        val cellSizeDp = 44
        val density = resources.displayMetrics.density
        val cellSizePx = (cellSizeDp * density).toInt()

        repeat(firstDow) {
            val empty = View(requireContext())
            empty.layoutParams = android.widget.GridLayout.LayoutParams().apply {
                width = cellSizePx
                height = cellSizePx
                columnSpec = android.widget.GridLayout.spec(
                    android.widget.GridLayout.UNDEFINED,
                    android.widget.GridLayout.FILL, 1f
                )
            }
            binding.gridCalendar.addView(empty)
        }

        for (day in 1..daysInMonth) {
            val tv = TextView(requireContext()).apply {
                text = if (sessionDays.contains(day)) getString(R.string.calendar_dot_day_fmt, getString(R.string.icon_bullet), day) else "$day"
                textSize = if (sessionDays.contains(day)) 10f else 13f
                gravity = Gravity.CENTER
                setPadding(2, 4, 2, 4)
                layoutParams = android.widget.GridLayout.LayoutParams().apply {
                    width = cellSizePx
                    height = cellSizePx
                    columnSpec = android.widget.GridLayout.spec(
                        android.widget.GridLayout.UNDEFINED,
                        android.widget.GridLayout.FILL, 1f
                    )
                }
                if (isCurrentMonth && day == todayDay) {
                    setBackgroundResource(R.drawable.bg_today_circle)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                } else if (sessionDays.contains(day)) {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                } else {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                }
            }
            binding.gridCalendar.addView(tv)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
