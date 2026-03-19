package com.memoloop.app.ui.review

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.memoloop.app.R
import com.memoloop.app.databinding.FragmentReviewBinding
import java.util.Locale

class ReviewFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReviewViewModel by viewModels()
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tts = TextToSpeech(requireContext(), this)

        observeViewModel()
        setupButtons()

        viewModel.startSession()
    }

    private fun observeViewModel() {
        viewModel.currentCard.observe(viewLifecycleOwner) { word ->
            if (word != null) {
                binding.tvWord.text = word.word
                binding.tvDefinition.text = word.definition
                binding.tvExample.text = word.example

                // Card entrance animation
                binding.cardWord.alpha = 0f
                binding.cardWord.translationY = 40f
                binding.cardWord.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .start()
            }
        }

        viewModel.queueSize.observe(viewLifecycleOwner) { size ->
            val initial = viewModel.initialSize.value ?: ReviewViewModel.SESSION_SIZE
            val done = initial - size
            binding.progressBar.max = initial
            binding.progressBar.progress = done
            binding.tvCardsRemaining.text = "剩餘 $size 張"
        }

        viewModel.elapsedSeconds.observe(viewLifecycleOwner) { seconds ->
            val min = seconds / 60
            val sec = seconds % 60
            binding.tvTimer.text = String.format("%02d:%02d", min, sec)
        }

        viewModel.sessionComplete.observe(viewLifecycleOwner) { complete ->
            if (complete) {
                val elapsed = viewModel.elapsedSeconds.value ?: 0L
                viewModel.saveSession()
            }
        }

        // Navigate only after saveSession() finishes and prize is determined
        viewModel.unlockedPrizeIndex.observe(viewLifecycleOwner) { prizeIndex ->
            // unlockedPrizeIndex starts at -1; only navigate once session is complete
            if (viewModel.sessionComplete.value == true) {
                val elapsed = viewModel.elapsedSeconds.value ?: 0L
                findNavController().navigate(
                    R.id.action_review_to_result,
                    bundleOf(
                        "elapsedSeconds" to elapsed,
                        "unlockedPrizeIndex" to prizeIndex
                    )
                )
            }
        }
    }

    private fun setupButtons() {
        binding.btnTts.setOnClickListener {
            val word = viewModel.currentCard.value?.word ?: return@setOnClickListener
            speakWord(word)
        }
        binding.btnAgain.setOnClickListener {
            animateButton(binding.btnAgain)
            viewModel.onAgain()
        }
        binding.btnHard.setOnClickListener {
            animateButton(binding.btnHard)
            viewModel.onHard()
        }
        binding.btnGood.setOnClickListener {
            animateButton(binding.btnGood)
            viewModel.onGood()
        }
        binding.btnEasy.setOnClickListener {
            animateButton(binding.btnEasy)
            viewModel.onEasy()
        }
    }

    private fun animateButton(view: View) {
        view.animate()
            .scaleX(0.92f).scaleY(0.92f)
            .setDuration(80)
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }.start()
    }

    private fun speakWord(word: String) {
        if (ttsReady) {
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "tts_word")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            ttsReady = true
        }
    }

    override fun onDestroyView() {
        tts?.stop()
        tts?.shutdown()
        _binding = null
        super.onDestroyView()
    }
}
