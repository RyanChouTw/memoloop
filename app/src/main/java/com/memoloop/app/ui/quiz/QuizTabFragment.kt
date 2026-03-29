package com.memoloop.app.ui.quiz

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.memoloop.app.R
import com.memoloop.app.databinding.FragmentQuizBinding
import java.util.Calendar

class QuizTabFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizTabViewModel by viewModels()
    private lateinit var quizRecordAdapter: QuizRecordAdapter
    private lateinit var listeningRecordAdapter: ListeningRecordAdapter

    private var isListeningMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupDowHeader()
        setupObservers()
        setupNavButtons()
        setupToggle()

        binding.toggleQuizType.check(R.id.btn_type_vocabulary)
        loadCurrentMode()
    }

    private fun setupAdapters() {
        quizRecordAdapter = QuizRecordAdapter()
        listeningRecordAdapter = ListeningRecordAdapter()
        binding.rvQuizRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuizRecords.adapter = quizRecordAdapter
    }

    private fun setupToggle() {
        binding.toggleQuizType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            isListeningMode = checkedId == R.id.btn_type_listening

            // Update header text
            binding.tvHeader.text = getString(
                if (isListeningMode) R.string.quiz_tab_header_listening
                else R.string.quiz_tab_header
            )

            // Switch adapter
            binding.rvQuizRecords.adapter = if (isListeningMode) listeningRecordAdapter else quizRecordAdapter

            loadCurrentMode()
        }
    }

    private fun loadCurrentMode() {
        if (isListeningMode) viewModel.loadListening() else viewModel.loadVocab()
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
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                }
                setPadding(0, 4, 0, 8)
            }
            binding.gridDow.addView(tv)
        }
    }

    private fun setupObservers() {
        viewModel.year.observe(viewLifecycleOwner) { updateHeader() }
        viewModel.month.observe(viewLifecycleOwner) { updateHeader() }

        viewModel.vocabState.observe(viewLifecycleOwner) { state ->
            if (isListeningMode) return@observe
            buildCalendarGrid(state.quizDays, state.todayHasQuiz, state.todayResult?.pointsEarned, false)
            quizRecordAdapter.submitList(state.quizResults)
            binding.tvStatQuizzes.text = state.totalQuizzes.toString()
            binding.tvStatPassRate.text = getString(R.string.percent_fmt, state.passRate)
        }

        viewModel.listeningState.observe(viewLifecycleOwner) { state ->
            if (!isListeningMode) return@observe
            buildCalendarGrid(state.quizDays, state.todayHasQuiz, state.todayResult?.pointsEarned, true)
            listeningRecordAdapter.submitList(state.results)
            binding.tvStatQuizzes.text = state.totalQuizzes.toString()
            binding.tvStatPassRate.text = getString(R.string.percent_fmt, state.passRate)
        }

        viewModel.monthlyPoints.observe(viewLifecycleOwner) { points ->
            binding.tvMonthlyPoints.text = getString(R.string.monthly_points_fmt, points)
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
        binding.btnPrevMonth.setOnClickListener {
            viewModel.prevMonth()
            loadCurrentMode()
        }
        binding.btnNextMonth.setOnClickListener {
            viewModel.nextMonth()
            loadCurrentMode()
        }
    }

    private fun buildCalendarGrid(
        quizDays: Set<Int>,
        todayHasQuiz: Boolean,
        todayPoints: Int?,
        isListening: Boolean
    ) {
        binding.gridCalendar.removeAllViews()

        val y = viewModel.year.value ?: return
        val m = viewModel.month.value ?: return

        val cal = Calendar.getInstance()
        cal.set(y, m - 1, 1)
        val firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance()
        val isCurrentMonth = today.get(Calendar.YEAR) == y && today.get(Calendar.MONTH) + 1 == m
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        val cellSizeDp = 44
        val density = resources.displayMetrics.density
        val cellSizePx = (cellSizeDp * density).toInt()

        repeat(firstDow) {
            val empty = View(requireContext())
            empty.layoutParams = GridLayout.LayoutParams().apply {
                width = cellSizePx; height = cellSizePx
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
            }
            binding.gridCalendar.addView(empty)
        }

        for (day in 1..daysInMonth) {
            val isToday = isCurrentMonth && day == todayDay
            val hasQuiz = quizDays.contains(day)

            if (isToday && !todayHasQuiz) {
                // Show GO button
                val container = FrameLayout(requireContext()).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = cellSizePx; height = cellSizePx
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                    }
                }
                val goBtn = MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonStyle).apply {
                    text = getString(R.string.quiz_go)
                    textSize = 11f
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.quiz_correct))
                    cornerRadius = (8 * density).toInt()
                    insetTop = 0; insetBottom = 0
                    setPadding(0, 0, 0, 0)
                    minimumWidth = 0; minimumHeight = 0; minWidth = 0; minHeight = 0
                    layoutParams = FrameLayout.LayoutParams(
                        (38 * density).toInt(), (32 * density).toInt(), Gravity.CENTER
                    )
                    setOnClickListener {
                        if (isListening) {
                            findNavController().navigate(
                                R.id.action_quiz_tab_to_listening_session,
                                bundleOf("isQuizMode" to true)
                            )
                        } else {
                            findNavController().navigate(R.id.action_quiz_tab_to_quiz_session)
                        }
                    }
                }
                container.addView(goBtn)
                binding.gridCalendar.addView(container)
            } else if (isToday && todayHasQuiz) {
                val tv = TextView(requireContext()).apply {
                    val pts = todayPoints ?: 0
                    text = if (pts > 0) getString(R.string.points_plus_fmt, pts) else "$day"
                    textSize = if (pts > 0) 12f else 13f
                    gravity = Gravity.CENTER
                    setPadding(2, 4, 2, 4)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = cellSizePx; height = cellSizePx
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                    }
                    setBackgroundResource(R.drawable.bg_today_circle)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
                binding.gridCalendar.addView(tv)
            } else if (hasQuiz) {
                val tv = TextView(requireContext()).apply {
                    text = getString(R.string.calendar_dot_day_fmt, getString(R.string.icon_bullet), day)
                    textSize = 10f
                    gravity = Gravity.CENTER
                    setPadding(2, 4, 2, 4)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = cellSizePx; height = cellSizePx
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                    }
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_correct))
                }
                binding.gridCalendar.addView(tv)
            } else {
                val tv = TextView(requireContext()).apply {
                    text = "$day"
                    textSize = 13f
                    gravity = Gravity.CENTER
                    setPadding(2, 4, 2, 4)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = cellSizePx; height = cellSizePx
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                    }
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                }
                binding.gridCalendar.addView(tv)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCurrentMode()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
