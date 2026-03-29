package com.memoloop.app.data.model

enum class SpeechSpeed(val key: String, val rate: Float) {
    SLOW("slow", 0.5f),
    MODERATE("moderate", 0.75f),
    NORMAL("normal", 1.0f);

    companion object {
        fun fromKey(key: String): SpeechSpeed =
            entries.find { it.key == key } ?: NORMAL
    }
}
