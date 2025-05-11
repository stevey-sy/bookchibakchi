package com.example.bookchigibakchigi.data.datasource

import com.example.bookchigibakchigi.data.dao.BookDao
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.ui.main.BookFilterType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookLocalDataSource @Inject constructor(
    private val bookDao: BookDao
) {
    fun getReadingBooks(): Flow<List<BookEntity>> {
        return bookDao.getReadingBooks()
    }

    fun getFinishedBooks(): Flow<List<BookEntity>> {
        return bookDao.getFinishedBooks()
    }

    fun getAllBooks(): Flow<List<BookEntity>> {
        return bookDao.getAllBooks()
    }

    fun getBookById(itemId: Int): Flow<BookEntity> {
        return bookDao.getBookById(itemId)
    }

    suspend fun insertBook(book: BookEntity) {
        bookDao.insertBook(book)
    }

    suspend fun deleteBook(book: BookEntity) {
        bookDao.deleteBook(book)
    }

    suspend fun deleteAllBooks() {
        bookDao.deleteAllBooks()
    }

    suspend fun isBookExists(isbn: String): Boolean {
        return bookDao.isBookExists(isbn) > 0
    }

    suspend fun updateReadingProgress(itemId: Int, page: Int, elapsedTime: Int): Boolean {
        return bookDao.updateReadingProgress(itemId, page, elapsedTime) > 0
    }
} 