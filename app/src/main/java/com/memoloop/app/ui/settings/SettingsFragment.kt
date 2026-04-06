package com.memoloop.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.ProductDetails
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.memoloop.app.R
import com.memoloop.app.billing.BillingManager
import com.memoloop.app.data.model.DifficultyLevel
import com.memoloop.app.data.model.SpeechSpeed
import com.memoloop.app.data.repository.DifficultyManager
import com.memoloop.app.databinding.FragmentSettingsBinding
import com.memoloop.app.notification.ReminderManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var difficultyManager: DifficultyManager
    private lateinit var reminderManager: ReminderManager
    private var billingManager: BillingManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        difficultyManager = DifficultyManager(requireContext())
        reminderManager = ReminderManager(requireContext())
        billingManager = BillingManager(requireContext())

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        setupDifficultyChips()
        setupSpeedChips()
        setupReminder()

        binding.btnDonate.setOnClickListener { showDonateDialog() }
    }

    private fun setupDifficultyChips() {
        val levels = DifficultyLevel.entries
        val labels = arrayOf(
            getString(R.string.difficulty_junior),
            getString(R.string.difficulty_senior),
            getString(R.string.difficulty_toeic)
        )
        val current = difficultyManager.current

        levels.forEachIndexed { i, level ->
            val chip = Chip(requireContext()).apply {
                text = labels[i]
                isCheckable = true
                isChecked = level == current
                tag = level
            }
            binding.chipGroupDifficulty.addView(chip)
        }

        binding.chipGroupDifficulty.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds.first())
                difficultyManager.current = chip.tag as DifficultyLevel
            }
        }
    }

    private fun setupSpeedChips() {
        val speeds = SpeechSpeed.entries
        val labels = arrayOf(
            getString(R.string.speech_speed_slow),
            getString(R.string.speech_speed_moderate),
            getString(R.string.speech_speed_normal)
        )
        val current = difficultyManager.speechSpeed

        speeds.forEachIndexed { i, speed ->
            val chip = Chip(requireContext()).apply {
                text = labels[i]
                isCheckable = true
                isChecked = speed == current
                tag = speed
            }
            binding.chipGroupSpeed.addView(chip)
        }

        binding.chipGroupSpeed.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds.first())
                difficultyManager.speechSpeed = chip.tag as SpeechSpeed
            }
        }
    }

    private fun setupReminder() {
        binding.btnReminderTime.text = String.format(
            "%02d:%02d", reminderManager.hour, reminderManager.minute
        )
        binding.switchReminder.isChecked = reminderManager.isEnabled

        binding.btnReminderTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(reminderManager.hour)
                .setMinute(reminderManager.minute)
                .setTitleText(getString(R.string.reminder_pick_time))
                .build()
            picker.addOnPositiveButtonClickListener {
                reminderManager.updateTime(picker.hour, picker.minute)
                binding.btnReminderTime.text = String.format(
                    "%02d:%02d", picker.hour, picker.minute
                )
            }
            picker.show(parentFragmentManager, "timePicker")
        }

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            reminderManager.isEnabled = isChecked
        }
    }

    private fun showDonateDialog() {
        val bm = billingManager ?: return

        bm.connect { products ->
            activity?.runOnUiThread {
                if (products.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.donate_unavailable),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@runOnUiThread
                }
                showDonateOptions(products)
            }
        }
    }

    private fun showDonateOptions(products: List<ProductDetails>) {
        val icons = arrayOf("☕", "🍕", "🎉")
        val names = products.mapIndexed { i, p ->
            val price = p.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
            "${icons.getOrElse(i) { "" }}  ${p.name}  $price"
        }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.donate_description))
            .setItems(names) { _, which ->
                val activity = activity ?: return@setItems
                billingManager?.launchPurchase(activity, products[which]) { success ->
                    activity.runOnUiThread {
                        val msg = if (success) R.string.donate_thank_you else R.string.donate_error
                        Toast.makeText(requireContext(), getString(msg), Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        billingManager?.disconnect()
        billingManager = null
        _binding = null
        super.onDestroyView()
    }
}
