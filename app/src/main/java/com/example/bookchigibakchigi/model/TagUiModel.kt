package com.example.bookchigibakchigi.model

data class TagUiModel(
    val tagId: Long = 0,
    val name: String,
    val backgroundColor: String,
    val textColor: String,
    val createdAt: Long = System.currentTimeMillis()
)