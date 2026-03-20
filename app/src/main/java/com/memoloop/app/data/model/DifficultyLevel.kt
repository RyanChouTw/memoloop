package com.memoloop.app.data.model

enum class DifficultyLevel(val key: String, val displayName: String, val rawResName: String) {
    JUNIOR_HIGH("junior_high", "國中", "words_junior"),
    SENIOR_HIGH("senior_high", "高中", "words_senior"),
    TOEIC("toeic", "多益 TOEIC", "words_toeic");

    companion object {
        fun fromKey(key: String): DifficultyLevel =
            entries.find { it.key == key } ?: JUNIOR_HIGH
    }
}
