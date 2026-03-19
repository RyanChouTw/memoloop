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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
        val days = listOf("日", "一", "二", "三", "四", "五", "六")
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
        }
    }

    private fun updateHeader() {
        val y = viewModel.year.value ?: return
        val m = viewModel.month.value ?: return
        binding.tvMonthYear.text = "${y}年 ${m}月"
    }

    private fun setupNavButtons() {
        binding.btnPrevMonth.setOnClickListener { viewModel.prevMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
    }

    private fun buildCalendarGrid(sessions: List<ReviewSession>) {
        binding.gridCalendar.removeAllViews()

        val y = viewModel.year.value ?: return
        val m = viewModel.month.value ?: return

        // Days with sessions
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sessionDays = sessions.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.dateMillis
            cal.get(Calendar.DAY_OF_MONTH)
        }.toHashSet()

        // First day of month weekday (0=Sun)
        val cal = Calendar.getInstance()
        cal.set(y, m - 1, 1)
        val firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1  // 0=Sun
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance()
        val isCurrentMonth = today.get(Calendar.YEAR) == y && today.get(Calendar.MONTH) + 1 == m
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        val cellSizeDp = 46
        val density = resources.displayMetrics.density
        val cellSizePx = (cellSizeDp * density).toInt()

        // Empty cells for first row offset
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

        // Day cells
        for (day in 1..daysInMonth) {
            val tv = TextView(requireContext()).apply {
                text = if (sessionDays.contains(day)) "📘\n$day" else "$day"
                textSize = if (sessionDays.contains(day)) 11f else 13f
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
                // Highlight today
                if (isCurrentMonth && day == todayDay) {
                    setBackgroundResource(R.drawable.bg_today_circle)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
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
