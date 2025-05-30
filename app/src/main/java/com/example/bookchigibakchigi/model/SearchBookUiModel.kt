package com.example.bookchigibakchigi.model

data class SearchBookUiModel(
    val title: String = "",
    var author: String = "",
    val publisher: String = "",
    val isbn: String = "",
    val cover: String = "",
    val page: Int = 0,
    val description: String = "",
    val rate: Float = 0f,
) {
    fun getAuthorText(): String {
        val authorText = if (author.contains("(지은이)")) {
            author.substringBefore("(지은이)").trim() // "(지은이)" 이전 문자열 추출 및 공백 제거
        } else {
            author
        }
        return authorText
    }
}