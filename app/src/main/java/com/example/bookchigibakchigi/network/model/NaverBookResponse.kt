package com.example.bookchigibakchigi.network.model

data class NaverBookResponse(
    val lastBuildDate: String,
    val total: Int,
    val start: Int,
    val display: Int,
    val items: List<BookItem>
)