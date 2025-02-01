package com.example.bookchigibakchigi.repository

import androidx.lifecycle.LiveData
import com.example.bookchigibakchigi.data.dao.BookDao
import com.example.bookchigibakchigi.data.entity.BookEntity

class BookShelfRepository(private val bookDao: BookDao) {
    fun getShelfItems(): LiveData<List<BookEntity>> {
        return bookDao.getAllBooks()
    }
}