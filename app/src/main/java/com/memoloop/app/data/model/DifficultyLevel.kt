package com.memoloop.app.data.model

enum class DifficultyLevel(val key: String, val rawResName: String) {
    JUNIOR_HIGH("junior_high", "words_junior"),
    SENIOR_HIGH("senior_high", "words_senior"),
    TOEIC("toeic", "words_toeic");

    companion object {
        fun fromKey(key: String): DifficultyLevel =
            entries.find { it.key == key } ?: JUNIOR_HIGH
    }
}
