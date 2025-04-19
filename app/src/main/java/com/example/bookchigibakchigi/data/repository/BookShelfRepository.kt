package com.example.bookchigibakchigi.data.repository

import com.example.bookchigibakchigi.data.dao.BookDao
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.ui.main.BookFilterType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookShelfRepository @Inject constructor(
    private val bookDao: BookDao
) {
    fun getShelfItems(bookFilterType: BookFilterType): Flow<List<BookEntity>> {
        return when (bookFilterType) {
            BookFilterType.Reading -> bookDao.getReadingBooks()
            BookFilterType.Finished -> bookDao.getFinishedBooks()
            BookFilterType.All -> bookDao.getAllBooks()
        }
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
}