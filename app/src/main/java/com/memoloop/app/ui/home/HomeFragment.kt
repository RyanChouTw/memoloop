package com.memoloop.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            findNavController().navigate(R.id.action_home_to_review)
        }

        binding.btnDifficulty.setOnClickListener {
            showDifficultyPicker()
        }

        // Long-press streak badge → reset dialog (for testing / admin)
        binding.tvStreak.setOnLongClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("重置複習紀錄")
                .setMessage("確定要清除所有複習紀錄與連續天數嗎？")
                .setPositiveButton("確定重置") { _, _ -> viewModel.resetData() }
                .setNegativeButton("取消", null)
                .show()
            true
        }
    }

    private fun showDifficultyPicker() {
        val levels = DifficultyLevel.entries
        val labels = arrayOf(
            getString(R.string.difficulty_junior),
            getString(R.string.difficulty_senior),
            getString(R.string.difficulty_toeic)
        )
        val currentIndex = levels.indexOf(viewModel.difficulty.value ?: DifficultyLevel.JUNIOR_HIGH)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_difficulty))
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                viewModel.setDifficulty(levels[which])
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStreak()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
