package com.memoloop.app.data.model

data class ListeningStory(
    val id: Int,
    val text: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)
