package com.memoloop.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memoloop.app.data.model.DifficultyLevel
import com.memoloop.app.data.model.ListeningStory

class ListeningRepository(private val context: Context) {

    private val cache = mutableMapOf<DifficultyLevel, List<ListeningStory>>()

    fun getAllStories(difficulty: DifficultyLevel): List<ListeningStory> {
        return cache.getOrPut(difficulty) {
            val resName = when (difficulty) {
                DifficultyLevel.JUNIOR_HIGH -> "stories_junior"
                DifficultyLevel.SENIOR_HIGH -> "stories_senior"
                DifficultyLevel.TOEIC -> "stories_toeic"
            }
            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
            val json = context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<ListeningStory>>() {}.type
            Gson().fromJson(json, type)
        }
    }

    fun getRandomStories(count: Int, difficulty: DifficultyLevel): List<ListeningStory> {
        return getAllStories(difficulty).shuffled().take(count)
    }
}
