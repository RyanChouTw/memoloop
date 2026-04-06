package com.memoloop.app.data.model

data class Word(
    val id: Int,
    val word: String,
    val definition: String,
    val examples: List<String>
)
