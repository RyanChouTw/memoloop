package com.memoloop.app.data.repository

import android.content.Context
import com.memoloop.app.data.model.DifficultyLevel
import com.memoloop.app.data.model.SpeechSpeed

class DifficultyManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var current: DifficultyLevel
        get() = DifficultyLevel.fromKey(prefs.getString(KEY_DIFFICULTY, null) ?: DifficultyLevel.JUNIOR_HIGH.key)
        set(value) { prefs.edit().putString(KEY_DIFFICULTY, value.key).apply() }

    var speechSpeed: SpeechSpeed
        get() = SpeechSpeed.fromKey(prefs.getString(KEY_SPEECH_SPEED, null) ?: SpeechSpeed.NORMAL.key)
        set(value) { prefs.edit().putString(KEY_SPEECH_SPEED, value.key).apply() }

    companion object {
        private const val PREFS_NAME = "memoloop_settings"
        private const val KEY_DIFFICULTY = "difficulty_level"
        private const val KEY_SPEECH_SPEED = "speech_speed"
    }
}
