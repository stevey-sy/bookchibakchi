package com.example.bookchigibakchigi.model

data class MemoUiModel(
    val memoId: Long = 0,
    val bookId: Int,
    val content: String,
    val pageNumber: Int,
    val background: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tags: List<TagUiModel> = emptyList()
)