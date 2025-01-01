package com.example.bookchigibakchigi.repository

import com.example.bookchigibakchigi.data.dao.BookDao
import com.example.bookchigibakchigi.data.entity.BookEntity

class BookShelfRepository(private val bookDao: BookDao) {
    suspend fun getShelfItems(): List<BookEntity> {
        return bookDao.getAllBooks()
    }
}