package com.memoloop.app.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.memoloop.app.R
import com.memoloop.app.databinding.FragmentQuizResultBinding

class QuizResultFragment : Fragment() {

    private var _binding: FragmentQuizResultBinding? = null
    private val binding get() = _binding!!

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

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_quiz_result_to_quiz_tab)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
