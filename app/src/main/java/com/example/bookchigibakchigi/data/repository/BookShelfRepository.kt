package com.example.bookchigibakchigi.data.repository

import androidx.lifecycle.LiveData
import com.example.bookchigibakchigi.data.dao.BookDao
import com.example.bookchigibakchigi.data.entity.BookEntity
import javax.inject.Inject

class BookShelfRepository (private val bookDao: BookDao) {
    fun getShelfItems(): LiveData<List<BookEntity>> {
        return bookDao.getAllBooks()
    }

    fun getBookById(itemId: Int): LiveData<BookEntity> {
        return bookDao.getBookById(itemId) // ✅ LiveData 반환
    }
}