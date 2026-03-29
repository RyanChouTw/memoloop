package com.memoloop.app.ui.listening

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.memoloop.app.R
import com.memoloop.app.databinding.FragmentListeningResultBinding

class ListeningResultFragment : Fragment() {

    private var _binding: FragmentListeningResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListeningResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val correctCount = arguments?.getInt("correctCount") ?: 0
        val totalCount = arguments?.getInt("totalCount") ?: 5
        val passed = arguments?.getBoolean("passed") ?: false
        val pointsEarned = arguments?.getInt("pointsEarned") ?: 0
        val isQuizMode = arguments?.getBoolean("isQuizMode") ?: false

        binding.tvScore.text = getString(R.string.quiz_score_fmt, correctCount, totalCount)

        if (passed) {
            binding.tvIcon.text = getString(R.string.icon_celebration)
            binding.tvTitle.text = getString(R.string.quiz_passed)
            binding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_correct))
            if (isQuizMode && pointsEarned > 0) {
                binding.tvPoints.text = getString(R.string.quiz_points_earned_fmt, pointsEarned)
                binding.tvPoints.setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_correct))
            } else {
                binding.tvPoints.text = getString(R.string.listening_practice_complete)
                binding.tvPoints.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }
        } else {
            binding.tvIcon.text = getString(R.string.icon_strong)
            binding.tvTitle.text = getString(R.string.quiz_failed)
            binding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_wrong))
            if (isQuizMode) {
                binding.tvPoints.text = getString(R.string.quiz_no_points)
            } else {
                binding.tvPoints.text = getString(R.string.listening_keep_practicing)
            }
            binding.tvPoints.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }

        if (isQuizMode) {
            binding.btnBack.text = getString(R.string.quiz_back_to_quiz)
            binding.btnBack.setOnClickListener {
                findNavController().navigate(R.id.action_listening_result_to_quiz_tab)
            }
        } else {
            binding.btnBack.text = getString(R.string.back_home)
            binding.btnBack.setOnClickListener {
                findNavController().navigate(R.id.action_listening_result_to_home)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
