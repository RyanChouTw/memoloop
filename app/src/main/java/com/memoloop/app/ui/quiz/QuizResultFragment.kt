package com.memoloop.app.ui.quiz

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.memoloop.app.R
import com.memoloop.app.databinding.FragmentQuizResultBinding

class QuizResultFragment : Fragment() {

    private var _binding: FragmentQuizResultBinding? = null
    private val binding get() = _binding!!
    private val quizViewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val correctCount = arguments?.getInt("quizCorrectCount") ?: 0
        val totalCount = arguments?.getInt("quizTotalCount") ?: 10
        val passed = arguments?.getBoolean("quizPassed") ?: false
        val pointsEarned = arguments?.getInt("quizPointsEarned") ?: 0

        binding.tvQuizScore.text = getString(R.string.quiz_score_fmt, correctCount, totalCount)

        if (passed) {
            binding.tvIcon.text = getString(R.string.icon_celebration)
            binding.tvTitle.text = getString(R.string.quiz_passed)
            binding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_correct))
            binding.tvPointsEarned.text = getString(R.string.quiz_points_earned_fmt, pointsEarned)
            binding.tvPointsEarned.setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_correct))
        } else {
            binding.tvIcon.text = getString(R.string.icon_strong)
            binding.tvTitle.text = getString(R.string.quiz_failed)
            binding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_wrong))
            binding.tvPointsEarned.text = getString(R.string.quiz_no_points)
            binding.tvPointsEarned.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }

        // Show wrong answers review
        val wrongAnswers = quizViewModel.wrongAnswers
        if (wrongAnswers.isNotEmpty()) {
            binding.wrongAnswersContainer.visibility = View.VISIBLE
            binding.tvWrongCount.text = getString(R.string.wrong_answers_title, wrongAnswers.size)

            for (wrong in wrongAnswers) {
                val item = buildWrongAnswerView(wrong)
                binding.wrongAnswersList.addView(item)
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_quiz_result_to_quiz_tab)
        }
    }

    private fun buildWrongAnswerView(wrong: QuizViewModel.WrongAnswer): View {
        val density = resources.displayMetrics.density
        val pad = (12 * density).toInt()

        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            setBackgroundResource(R.drawable.bg_wrong_answer_item)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (8 * density).toInt() }

            // Word
            addView(TextView(requireContext()).apply {
                text = wrong.word
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            })

            // Definition
            addView(TextView(requireContext()).apply {
                text = wrong.definition
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                setPadding(0, (2 * density).toInt(), 0, (6 * density).toInt())
            })

            // Your answer (red) vs correct answer (green)
            addView(TextView(requireContext()).apply {
                text = "${getString(R.string.your_answer)}: ${wrong.userAnswer}"
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_wrong))
            })
            addView(TextView(requireContext()).apply {
                text = "${getString(R.string.correct_answer)}: ${wrong.correctAnswer}"
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_correct))
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
