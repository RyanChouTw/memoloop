package com.memoloop.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memoloop.app.data.model.DifficultyLevel
import com.memoloop.app.data.model.Word

class WordRepository(private val context: Context) {

    private val cache = mutableMapOf<DifficultyLevel, List<Word>>()

    fun getAllWords(difficulty: DifficultyLevel): List<Word> {
        return cache.getOrPut(difficulty) {
            val resId = context.resources.getIdentifier(
                difficulty.rawResName, "raw", context.packageName
            )
            val inputStream = context.resources.openRawResource(resId)
            val json = inputStream.bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Word>>() {}.type
            Gson().fromJson(json, type)
        }
    }

    fun getRandomWords(count: Int, difficulty: DifficultyLevel): List<Word> {
        return getAllWords(difficulty).shuffled().take(count)
    }
}
