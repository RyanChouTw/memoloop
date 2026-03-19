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
    private val prizeInfo = listOf(
        Triple("🥉", "新手學員", "完成了第一次複習，正式踏上單字之旅！"),
        Triple("🔶", "青銅學徒", "連續複習 3 天，習慣正在形成！"),
        Triple("🥈", "銀牌達人", "連續複習 7 天，堅持就是勝利！"),
        Triple("🥇", "黃金大師", "連續複習 14 天，你的努力令人敬佩！"),
        Triple("💎", "白金傳奇", "連續複習 30 天，你已是單字傳奇！"),
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
        binding.tvTime.text = String.format("%02d:%02d", min, sec)

        binding.btnHome.setOnClickListener {
            findNavController().navigate(R.id.action_result_to_home)
        }

        // Show celebration popup if a new prize was unlocked
        val prizeIndex = arguments?.getInt("unlockedPrizeIndex") ?: -1
        if (prizeIndex in prizeInfo.indices) {
            showPrizeCelebration(prizeIndex)
        }
    }

    private fun showPrizeCelebration(index: Int) {
        val (icon, title, message) = prizeInfo[index]
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("$icon  恭喜解鎖新成就！")
            .setMessage("「$title」\n\n$message")
            .setPositiveButton("太棒了！") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
