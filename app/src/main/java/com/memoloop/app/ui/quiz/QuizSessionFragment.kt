package com.memoloop.app.ui.quiz

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.memoloop.app.R
import com.memoloop.app.databinding.FragmentQuizSessionBinding

class QuizSessionFragment : Fragment() {

    private var _binding: FragmentQuizSessionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var optionButtons: List<MaterialButton>
    private var answered = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        optionButtons = listOf(
            binding.btnOption0, binding.btnOption1,
            binding.btnOption2, binding.btnOption3
        )

        setupOptionListeners()
        observeViewModel()

        viewModel.startQuiz()
    }

    private fun setupOptionListeners() {
        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (!answered) {
                    answered = true
                    viewModel.submitAnswer(index)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.quizState.observe(viewLifecycleOwner) { state ->
            answered = false
            binding.tvFeedback.visibility = View.GONE

            binding.progressBar.progress = state.questionIndex
            binding.tvQuestionCount.text = getString(
                R.string.quiz_progress_fmt,
                state.questionIndex + 1,
                state.totalQuestions
            )

            val q = state.currentQuestion
            binding.tvDefinition.text = when (q.questionType) {
                QuestionType.FILL_BLANK -> q.questionText
                QuestionType.DEFINITION -> q.word.definition
            }

            // Reset button styles
            optionButtons.forEachIndexed { i, btn ->
                btn.text = state.currentQuestion.options[i]
                btn.isEnabled = true
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                btn.setStrokeColorResource(R.color.border)
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            }

            // Card entrance animation
            binding.cardQuestion.alpha = 0f
            binding.cardQuestion.translationY = 30f
            binding.cardQuestion.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .start()
        }

        viewModel.answerResult.observe(viewLifecycleOwner) { feedback ->
            if (feedback == null) return@observe

            optionButtons.forEach { it.isEnabled = false }

            val correctBtn = optionButtons[feedback.correctIndex]
            correctBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.quiz_correct))
            correctBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            correctBtn.setStrokeColorResource(R.color.quiz_correct)

            if (!feedback.isCorrect) {
                val wrongBtn = optionButtons[feedback.selectedIndex]
                wrongBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.quiz_wrong))
                wrongBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                wrongBtn.setStrokeColorResource(R.color.quiz_wrong)

                binding.tvFeedback.text = getString(R.string.quiz_wrong_feedback)
                binding.tvFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_wrong))
            } else {
                binding.tvFeedback.text = getString(R.string.quiz_correct_feedback)
                binding.tvFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_correct))
            }
            binding.tvFeedback.visibility = View.VISIBLE

            handler.postDelayed({ viewModel.nextQuestion() }, 1200)
        }

        viewModel.quizComplete.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe

            findNavController().navigate(
                R.id.action_quiz_session_to_quiz_result,
                bundleOf(
                    "quizCorrectCount" to result.correctCount,
                    "quizTotalCount" to result.totalCount,
                    "quizPassed" to result.passed,
                    "quizPointsEarned" to result.pointsEarned,
                    "quizLevel" to result.level
                )
            )
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        _binding = null
        super.onDestroyView()
    }
}
