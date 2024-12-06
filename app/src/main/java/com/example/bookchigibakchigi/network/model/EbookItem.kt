package com.example.bookchigibakchigi.network.model

data class EbookItem(
    val itemId: Int,
    val ISBN: String,
    val isbn13: String,
    val priceSales: Int,
    val link: String
)
