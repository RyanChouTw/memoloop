package com.memoloop.app.ui.dictionary

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.memoloop.app.R
import com.memoloop.app.data.model.Word
import com.memoloop.app.data.repository.DifficultyManager
import com.memoloop.app.data.repository.WordRepository
import com.memoloop.app.databinding.FragmentDictionaryBinding

class DictionaryFragment : Fragment() {

    private var _binding: FragmentDictionaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DictionaryAdapter
    private var allWords: List<Word> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDictionaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DictionaryAdapter()
        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = adapter

        val wordRepo = WordRepository(requireContext())
        val difficulty = DifficultyManager(requireContext()).current
        allWords = wordRepo.getAllWords(difficulty)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                performSearch(s?.toString()?.trim() ?: "")
            }
        })

        // Show all words initially
        adapter.submitList(allWords.take(50))
        binding.tvResultCount.text = getString(R.string.dictionary_result_count, allWords.size)
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            adapter.submitList(allWords.take(50))
            binding.tvResultCount.text = getString(R.string.dictionary_result_count, allWords.size)
            return
        }

        val lowerQuery = query.lowercase()
        val results = allWords.filter { word ->
            word.word.lowercase().contains(lowerQuery) ||
            word.definition.contains(lowerQuery)
        }

        adapter.submitList(results.take(50))
        binding.tvResultCount.text = getString(R.string.dictionary_result_count, results.size)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
