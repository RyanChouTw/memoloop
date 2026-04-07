package com.memoloop.app.ui.history

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.memoloop.app.R
import com.memoloop.app.data.model.ReviewSession
import com.memoloop.app.databinding.FragmentHistoryBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
            buildWeeklyChart(sessions)
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

    private fun buildWeeklyChart(sessions: List<ReviewSession>) {
        val barsLayout = binding.chartBars
        val labelsLayout = binding.chartLabels
        barsLayout.removeAllViews()
        labelsLayout.removeAllViews()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val density = resources.displayMetrics.density

        // Last 7 days of the viewed month
        val y = viewModel.year.value ?: return
        val m = viewModel.month.value ?: return
        val cal = Calendar.getInstance()
        val today = Calendar.getInstance()
        val isCurrentMonth = today.get(Calendar.YEAR) == y && today.get(Calendar.MONTH) + 1 == m

        if (isCurrentMonth) {
            cal.time = today.time
        } else {
            cal.set(y, m - 1, 1)
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        }

        // Collect 7 days of data
        val dailyMinutes = mutableListOf<Pair<String, Long>>() // label, minutes
        for (i in 6 downTo 0) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_MONTH, -i)
            val key = dateFormat.format(dayCal.time)
            val label = dayLabelFormat.format(dayCal.time)
            val totalSec = sessions
                .filter { dateFormat.format(java.util.Date(it.dateMillis)) == key }
                .sumOf { it.durationSeconds }
            dailyMinutes.add(Pair(label, totalSec / 60))
        }

        val maxMinutes = dailyMinutes.maxOf { it.second }.coerceAtLeast(1)
        val maxBarHeight = (80 * density).toInt()
        val minBarHeight = (4 * density).toInt()
        val barRadius = (4 * density)

        for ((label, minutes) in dailyMinutes) {
            // Bar
            val barHeight = if (minutes > 0) {
                ((minutes.toFloat() / maxMinutes) * maxBarHeight).toInt().coerceAtLeast(minBarHeight)
            } else {
                minBarHeight
            }

            val bar = View(requireContext()).apply {
                val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
                    setColor(ContextCompat.getColor(requireContext(),
                        if (minutes > 0) R.color.primary else R.color.border))
                    cornerRadii = floatArrayOf(barRadius, barRadius, barRadius, barRadius, 0f, 0f, 0f, 0f)
                }
                background = bgDrawable
                layoutParams = LinearLayout.LayoutParams(0, barHeight, 1f).apply {
                    marginStart = (2 * density).toInt()
                    marginEnd = (2 * density).toInt()
                }
            }
            barsLayout.addView(bar)

            // Label
            val tv = TextView(requireContext()).apply {
                text = label
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            labelsLayout.addView(tv)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
