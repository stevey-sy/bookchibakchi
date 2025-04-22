package com.example.bookchigibakchigi.data.repository

import com.example.bookchigibakchigi.data.dao.BookDao
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.mapper.BookMapper
import com.example.bookchigibakchigi.model.BookUiModel
import com.example.bookchigibakchigi.ui.main.BookFilterType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookShelfRepository @Inject constructor(
    private val bookDao: BookDao
) {
    fun getShelfItems(bookFilterType: BookFilterType): Flow<List<BookUiModel>> {
        return when (bookFilterType) {
            BookFilterType.Reading -> bookDao.getReadingBooks()
            BookFilterType.Finished -> bookDao.getFinishedBooks()
            BookFilterType.All -> bookDao.getAllBooks()
        }.map { books -> BookMapper.toUiModels(books) }
    }

    fun getBookById(itemId: Int): Flow<BookUiModel> {
        return bookDao.getBookById(itemId).map { book -> BookMapper.toUiModel(book) }
    }

    fun observeBookById(itemId: Int): Flow<BookUiModel> {
        return bookDao.getBookById(itemId)
            .map { book -> BookMapper.toUiModel(book) }
    }

    suspend fun insertBook(book: BookEntity) {
        bookDao.insertBook(book)
    }

    suspend fun deleteBook(book: BookEntity) {
        bookDao.deleteBook(book)
    }

    suspend fun deleteBook(book: BookUiModel) {
        bookDao.deleteBook(BookMapper.toEntity(book))
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