package com.memoloop.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.memoloop.app.R
import com.memoloop.app.data.model.DifficultyLevel
import com.memoloop.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.togglePracticeMode.check(R.id.btn_mode_vocabulary)

        viewModel.streak.observe(viewLifecycleOwner) { streak ->
            binding.tvStreakCount.text = streak.toString()
            binding.tvStreakDaysStat.text = streak.toString()
        }

        viewModel.difficulty.observe(viewLifecycleOwner) { level ->
            binding.tvSubtitle.text = when (level) {
                DifficultyLevel.JUNIOR_HIGH -> getString(R.string.difficulty_junior)
                DifficultyLevel.SENIOR_HIGH -> getString(R.string.difficulty_senior)
                DifficultyLevel.TOEIC       -> getString(R.string.difficulty_toeic)
            }
        }

        binding.btnStart.setOnClickListener {
            val isListening = binding.togglePracticeMode.checkedButtonId == R.id.btn_mode_listening
            if (isListening) {
                findNavController().navigate(
                    R.id.action_home_to_listening,
                    bundleOf("isQuizMode" to false)
                )
            } else {
                findNavController().navigate(R.id.action_home_to_review)
            }
        }

        binding.btnReviewBookmarks.setOnClickListener {
            findNavController().navigate(
                R.id.action_home_to_review,
                bundleOf("bookmarkOnly" to true)
            )
        }

        binding.btnDictionary.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_dictionary)
        }

        binding.btnDifficulty.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        binding.tvStreak.setOnLongClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.reset_title))
                .setMessage(getString(R.string.reset_message))
                .setPositiveButton(getString(R.string.reset_confirm)) { _, _ -> viewModel.resetData() }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStreak()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
