package com.example.bookchigibakchigi.network.model

data class NewBookInfo(
    val itemId: Int,
    val isbn: String,
    val isbn13: String,
    val priceSales: Int,
    val link: String
)
