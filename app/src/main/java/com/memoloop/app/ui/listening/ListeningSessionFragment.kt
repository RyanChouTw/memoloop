package com.memoloop.app.ui.listening

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.memoloop.app.R
import com.memoloop.app.data.repository.DifficultyManager
import com.memoloop.app.databinding.FragmentListeningSessionBinding
import java.util.Locale

class ListeningSessionFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentListeningSessionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ListeningViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var optionButtons: List<MaterialButton>
    private var answered = false
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var transcriptVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListeningSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tts = TextToSpeech(requireContext(), this)

        optionButtons = listOf(
            binding.btnOption0, binding.btnOption1,
            binding.btnOption2, binding.btnOption3
        )

        // Determine if quiz mode from arguments
        viewModel.isQuizMode = arguments?.getBoolean("isQuizMode") ?: false

        setupListeners()
        observeViewModel()

        viewModel.startSession()
    }

    private fun setupListeners() {
        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (!answered) {
                    answered = true
                    viewModel.submitAnswer(index)
                }
            }
        }

        binding.btnPlay.setOnClickListener {
            val story = viewModel.state.value?.story ?: return@setOnClickListener
            speakText(story.text)
        }

        binding.btnToggleTranscript.setOnClickListener {
            transcriptVisible = !transcriptVisible
            binding.tvTranscript.visibility = if (transcriptVisible) View.VISIBLE else View.GONE
            binding.btnToggleTranscript.text = getString(
                if (transcriptVisible) R.string.listening_hide_transcript
                else R.string.listening_show_transcript
            )
        }

        setupTranscriptActions()
    }

    private fun setupTranscriptActions() {
        binding.tvTranscript.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                menu.add(0, MENU_TRANSLATE, 0, R.string.action_translate)
                menu.add(0, MENU_COPY, 1, R.string.action_copy)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                val selectedText = getSelectedTranscriptText() ?: return false
                return when (item.itemId) {
                    MENU_TRANSLATE -> {
                        openGoogleTranslate(selectedText)
                        mode.finish()
                        true
                    }
                    MENU_COPY -> {
                        copyToClipboard(selectedText)
                        mode.finish()
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
        }
    }

    private fun getSelectedTranscriptText(): String? {
        val start = binding.tvTranscript.selectionStart
        val end = binding.tvTranscript.selectionEnd
        if (start < 0 || end <= start) return null
        return binding.tvTranscript.text.substring(start, end)
    }

    private fun openGoogleTranslate(text: String) {
        // Try Google Translate app first
        val translateAppIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra("key_text_input", text)
            putExtra("key_text_output", "")
            putExtra("key_language_from", "en")
            putExtra("key_language_to", "zh-TW")
            putExtra("key_suggest_translation", "")
            putExtra("key_from_floating_window", false)
            type = "text/plain"
            setPackage("com.google.android.apps.translate")
        }
        try {
            startActivity(translateAppIntent)
        } catch (_: Exception) {
            // Fallback to Google Translate web
            val encoded = Uri.encode(text)
            val url = "https://translate.google.com/?sl=en&tl=zh-TW&text=$encoded"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("transcript", text))
        Toast.makeText(requireContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val MENU_TRANSLATE = 1001
        private const val MENU_COPY = 1002
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            answered = false
            transcriptVisible = false
            binding.tvFeedback.visibility = View.GONE
            binding.tvTranscript.visibility = View.GONE
            binding.btnToggleTranscript.text = getString(R.string.listening_show_transcript)

            binding.progressBar.progress = state.storyIndex
            binding.tvStoryCount.text = getString(
                R.string.quiz_progress_fmt,
                state.storyIndex + 1,
                state.totalStories
            )

            binding.tvTranscript.text = state.story.text
            binding.tvQuestion.text = state.story.question

            // Reset option buttons
            optionButtons.forEachIndexed { i, btn ->
                btn.text = state.story.options[i]
                btn.isEnabled = true
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                btn.setStrokeColorResource(R.color.border)
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            }

            // Card animation
            binding.cardStory.alpha = 0f
            binding.cardStory.translationY = 30f
            binding.cardStory.animate().alpha(1f).translationY(0f).setDuration(200).start()

            // Auto-play audio
            if (ttsReady) {
                handler.postDelayed({ speakText(state.story.text) }, 400)
            }
        }

        viewModel.answerFeedback.observe(viewLifecycleOwner) { feedback ->
            if (feedback == null) return@observe

            optionButtons.forEach { it.isEnabled = false }

            val correctBtn = optionButtons[feedback.correctIndex]
            correctBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.quiz_correct))
            correctBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            correctBtn.setStrokeColorResource(R.color.quiz_correct)

            if (!feedback.isCorrect) {
                val wrongBtn = optionButtons[feedback.selectedIndex]
                wrongBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.quiz_wrong))
                wrongBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                wrongBtn.setStrokeColorResource(R.color.quiz_wrong)

                binding.tvFeedback.text = getString(R.string.quiz_wrong_feedback)
                binding.tvFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_wrong))
            } else {
                binding.tvFeedback.text = getString(R.string.quiz_correct_feedback)
                binding.tvFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.quiz_correct))
            }
            binding.tvFeedback.visibility = View.VISIBLE

            // Show transcript after answering
            binding.tvTranscript.visibility = View.VISIBLE
            transcriptVisible = true
            binding.btnToggleTranscript.text = getString(R.string.listening_hide_transcript)

            handler.postDelayed({ viewModel.nextStory() }, 1500)
        }

        viewModel.complete.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe

            findNavController().navigate(
                R.id.action_listening_session_to_listening_result,
                bundleOf(
                    "correctCount" to result.correctCount,
                    "totalCount" to result.totalCount,
                    "passed" to result.passed,
                    "pointsEarned" to result.pointsEarned,
                    "isQuizMode" to viewModel.isQuizMode
                )
            )
        }
    }

    private fun speakText(text: String) {
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_story")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            val speed = DifficultyManager(requireContext()).speechSpeed
            tts?.setSpeechRate(speed.rate)
            ttsReady = true
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        tts?.stop()
        tts?.shutdown()
        _binding = null
        super.onDestroyView()
    }
}
