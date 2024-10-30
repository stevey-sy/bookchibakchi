package com.example.bookchigibakchigi.repository

import com.example.bookchigibakchigi.data.BookShelfItem

class BookShelfRepository {
    fun getShelfItems(): List<BookShelfItem> {
        return listOf(
            BookShelfItem(1, "0", "title1", ""),
            BookShelfItem(2, "0", "title1", ""),
            BookShelfItem(3, "0", "title1", ""),
            BookShelfItem(4, "0", "title1", ""),
            BookShelfItem(5, "0", "title1", ""),
            BookShelfItem(6, "0", "title1", ""),
        )
    }
}