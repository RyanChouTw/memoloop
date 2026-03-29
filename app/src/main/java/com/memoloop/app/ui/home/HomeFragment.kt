package com.memoloop.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.memoloop.app.R
import com.memoloop.app.data.model.DifficultyLevel
import com.memoloop.app.data.model.SpeechSpeed
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

        // Default to vocabulary mode
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

        binding.btnDifficulty.setOnClickListener {
            showSettingsDialog()
        }

        // Long-press streak badge → reset dialog
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

    private fun showSettingsDialog() {
        val density = resources.displayMetrics.density
        val pad = (24 * density).toInt()
        val sectionPad = (16 * density).toInt()

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, sectionPad, pad, 0)
        }

        // ── Difficulty section ──
        val diffLabel = TextView(requireContext()).apply {
            text = getString(R.string.select_difficulty)
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
        container.addView(diffLabel)

        val difficultyLevels = DifficultyLevel.entries
        val diffLabels = arrayOf(
            getString(R.string.difficulty_junior),
            getString(R.string.difficulty_senior),
            getString(R.string.difficulty_toeic)
        )
        val currentDiff = viewModel.difficulty.value ?: DifficultyLevel.JUNIOR_HIGH

        val diffChipGroup = ChipGroup(requireContext()).apply {
            isSingleSelection = true
            isSelectionRequired = true
        }
        difficultyLevels.forEachIndexed { i, level ->
            val chip = Chip(requireContext()).apply {
                text = diffLabels[i]
                isCheckable = true
                isChecked = level == currentDiff
                tag = level
            }
            diffChipGroup.addView(chip)
        }
        container.addView(diffChipGroup)

        // ── Speech speed section ──
        val speedLabel = TextView(requireContext()).apply {
            text = getString(R.string.select_speech_speed)
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            setPadding(0, sectionPad, 0, 0)
        }
        container.addView(speedLabel)

        val speeds = SpeechSpeed.entries
        val speedLabels = arrayOf(
            getString(R.string.speech_speed_slow),
            getString(R.string.speech_speed_moderate),
            getString(R.string.speech_speed_normal)
        )
        val currentSpeed = viewModel.speechSpeed.value ?: SpeechSpeed.NORMAL

        val speedChipGroup = ChipGroup(requireContext()).apply {
            isSingleSelection = true
            isSelectionRequired = true
        }
        speeds.forEachIndexed { i, speed ->
            val chip = Chip(requireContext()).apply {
                text = speedLabels[i]
                isCheckable = true
                isChecked = speed == currentSpeed
                tag = speed
            }
            speedChipGroup.addView(chip)
        }
        container.addView(speedChipGroup)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.settings_title))
            .setView(container)
            .setPositiveButton(getString(R.string.awesome)) { dialog, _ ->
                // Apply difficulty
                val selectedDiffChip = diffChipGroup.findViewById<Chip>(diffChipGroup.checkedChipId)
                if (selectedDiffChip != null) {
                    viewModel.setDifficulty(selectedDiffChip.tag as DifficultyLevel)
                }
                // Apply speech speed
                val selectedSpeedChip = speedChipGroup.findViewById<Chip>(speedChipGroup.checkedChipId)
                if (selectedSpeedChip != null) {
                    viewModel.setSpeechSpeed(selectedSpeedChip.tag as SpeechSpeed)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
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
