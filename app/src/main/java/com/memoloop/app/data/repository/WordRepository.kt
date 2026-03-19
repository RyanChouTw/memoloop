package com.memoloop.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memoloop.app.R
import com.memoloop.app.data.model.Word

class WordRepository(private val context: Context) {

    private var cachedWords: List<Word>? = null

    fun getAllWords(): List<Word> {
        if (cachedWords != null) return cachedWords!!
        val inputStream = context.resources.openRawResource(R.raw.words)
        val json = inputStream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Word>>() {}.type
        cachedWords = Gson().fromJson(json, type)
        return cachedWords!!
    }

    fun getRandomWords(count: Int): List<Word> {
        return getAllWords().shuffled().take(count)
    }
}
