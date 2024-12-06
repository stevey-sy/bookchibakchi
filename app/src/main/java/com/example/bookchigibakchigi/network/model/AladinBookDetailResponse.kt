package com.example.bookchigibakchigi.network.model

data class AladinBookDetailResponse (
    val version: String,
    val title: String,
    val link: String,
    val pubDate: String,
    val item: List<AladinBookItem> // 상품 목록
)