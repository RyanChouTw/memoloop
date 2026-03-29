package com.memoloop.app.ui.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.memoloop.app.R
import com.memoloop.app.databinding.FragmentResultBinding

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    // Prize definitions matching ReviewViewModel.prizeThresholds order
    // (icon, titleResId, messageResId)
    private data class PrizeIconRes(val iconRes: Int, val titleRes: Int, val msgRes: Int)
    private val prizeInfoRes = listOf(
        PrizeIconRes(R.string.icon_rookie, R.string.prize_rookie_title, R.string.prize_rookie_msg),
        PrizeIconRes(R.string.icon_bronze, R.string.prize_bronze_title, R.string.prize_bronze_msg),
        PrizeIconRes(R.string.icon_silver, R.string.prize_silver_title, R.string.prize_silver_msg),
        PrizeIconRes(R.string.icon_gold, R.string.prize_gold_title, R.string.prize_gold_msg),
        PrizeIconRes(R.string.icon_platinum, R.string.prize_platinum_title, R.string.prize_platinum_msg),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val elapsed = arguments?.getLong("elapsedSeconds") ?: 0L
        val min = elapsed / 60
        val sec = elapsed % 60
        binding.tvTime.text = getString(R.string.time_fmt, min, sec)

        binding.btnHome.setOnClickListener {
            findNavController().navigate(R.id.action_result_to_home)
        }

        // Show celebration popup if a new prize was unlocked
        val prizeIndex = arguments?.getInt("unlockedPrizeIndex") ?: -1
        if (prizeIndex in prizeInfoRes.indices) {
            showPrizeCelebration(prizeIndex)
        }
    }

    private fun showPrizeCelebration(index: Int) {
        val info = prizeInfoRes[index]
        val icon = getString(info.iconRes)
        val title = getString(info.titleRes)
        val message = getString(info.msgRes)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.prize_unlocked_title, icon))
            .setMessage(getString(R.string.prize_unlocked_msg, title, message))
            .setPositiveButton(getString(R.string.awesome)) { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
