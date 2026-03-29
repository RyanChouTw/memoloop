package com.memoloop.app.ui.prize

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.card.MaterialCardView
import com.memoloop.app.R
import com.memoloop.app.databinding.FragmentPrizeBinding
import com.memoloop.app.databinding.ItemPrizeBinding

data class PrizeDef(
    val icon: String,
    val title: String,
    val condition: String,
    val requiredStreak: Int,   // 0 = only needs 1 session total
    val colorRes: Int
)

class PrizeFragment : Fragment() {

    private var _binding: FragmentPrizeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrizeViewModel by viewModels()

    private lateinit var prizes: List<PrizeDef>

    private fun initPrizes() {
        prizes = listOf(
            PrizeDef(getString(R.string.icon_rookie), getString(R.string.prize_rookie_title), getString(R.string.prize_rookie_condition), 0, R.color.bronze),
            PrizeDef(getString(R.string.icon_bronze), getString(R.string.prize_bronze_title), getString(R.string.prize_bronze_condition), 3, R.color.bronze),
            PrizeDef(getString(R.string.icon_silver), getString(R.string.prize_silver_title), getString(R.string.prize_silver_condition), 7, R.color.silver),
            PrizeDef(getString(R.string.icon_gold), getString(R.string.prize_gold_title), getString(R.string.prize_gold_condition), 14, R.color.gold),
            PrizeDef(getString(R.string.icon_platinum), getString(R.string.prize_platinum_title), getString(R.string.prize_platinum_condition), 30, R.color.platinum),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPrizes()

        viewModel.prizeState.observe(viewLifecycleOwner) { state ->
            binding.tvCurrentStreak.text = getString(R.string.current_streak_fmt, state.streak)
            bindPrize(binding.prizeRookie, prizes[0], state)
            bindPrize(binding.prizeBronze, prizes[1], state)
            bindPrize(binding.prizeSilver, prizes[2], state)
            bindPrize(binding.prizeGold, prizes[3], state)
            bindPrize(binding.prizePlatinum, prizes[4], state)
        }
    }

    private fun bindPrize(b: ItemPrizeBinding, prize: PrizeDef, state: PrizeState) {
        val unlocked = if (prize.requiredStreak == 0) {
            state.totalSessions >= 1
        } else {
            state.streak >= prize.requiredStreak
        }

        b.tvPrizeIcon.text = if (unlocked) prize.icon else getString(R.string.icon_lock)
        b.tvPrizeTitle.text = prize.title
        b.tvPrizeCondition.text = prize.condition
        b.tvPrizeStatus.text = if (unlocked) getString(R.string.icon_check) else ""

        val cardView = b.root as MaterialCardView
        if (unlocked) {
            cardView.setCardBackgroundColor(
                blendWithWhite(ContextCompat.getColor(requireContext(), prize.colorRes), 0.85f)
            )
            b.tvPrizeTitle.alpha = 1f
            b.tvPrizeCondition.alpha = 0.8f
        } else {
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.divider)
            )
            b.tvPrizeTitle.alpha = 0.5f
            b.tvPrizeCondition.alpha = 0.4f
        }
    }

    /** Blend color with white at ratio (0=original, 1=white) */
    private fun blendWithWhite(color: Int, ratio: Float): Int {
        val r = ((android.graphics.Color.red(color) * (1 - ratio)) + (255 * ratio)).toInt()
        val g = ((android.graphics.Color.green(color) * (1 - ratio)) + (255 * ratio)).toInt()
        val b = ((android.graphics.Color.blue(color) * (1 - ratio)) + (255 * ratio)).toInt()
        return android.graphics.Color.rgb(r, g, b)
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
