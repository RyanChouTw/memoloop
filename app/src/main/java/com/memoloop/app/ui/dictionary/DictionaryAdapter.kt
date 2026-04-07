package com.memoloop.app.ui.dictionary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.memoloop.app.data.model.Word
import com.memoloop.app.databinding.ItemDictionaryWordBinding

class DictionaryAdapter : ListAdapter<Word, DictionaryAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Word>() {
            override fun areItemsTheSame(a: Word, b: Word) = a.id == b.id
            override fun areContentsTheSame(a: Word, b: Word) = a == b
        }
    }

    class ViewHolder(val binding: ItemDictionaryWordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDictionaryWordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = getItem(position)
        holder.binding.tvWord.text = word.word
        holder.binding.tvDefinition.text = word.definition
        holder.binding.tvExamples.text = word.examples.joinToString("\n")
    }
}
