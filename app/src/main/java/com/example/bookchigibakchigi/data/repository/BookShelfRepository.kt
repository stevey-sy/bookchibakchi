package com.example.bookchigibakchigi.data.repository

import com.example.bookchigibakchigi.data.datasource.BookLocalDataSource
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
    private val localDataSource: BookLocalDataSource
) {
    fun getShelfItems(bookFilterType: BookFilterType): Flow<List<BookUiModel>> {
        return when (bookFilterType) {
            BookFilterType.Reading -> localDataSource.getReadingBooks()
            BookFilterType.Finished -> localDataSource.getFinishedBooks()
            BookFilterType.All -> localDataSource.getAllBooks()
        }.map { books -> BookMapper.toUiModels(books) }
    }

    fun getBookById(itemId: Int): Flow<BookUiModel> {
        return localDataSource.getBookById(itemId).map { book -> BookMapper.toUiModel(book) }
    }

    fun observeBookById(itemId: Int): Flow<BookUiModel> {
        return localDataSource.getBookById(itemId)
            .map { book -> BookMapper.toUiModel(book) }
    }

    suspend fun insertBook(book: BookEntity) {
        localDataSource.insertBook(book)
    }

    suspend fun deleteBook(book: BookEntity) {
        localDataSource.deleteBook(book)
    }

    suspend fun deleteBook(book: BookUiModel) {
        localDataSource.deleteBook(BookMapper.toEntity(book))
    }

    suspend fun deleteAllBooks() {
        localDataSource.deleteAllBooks()
    }

    suspend fun isBookExists(isbn: String): Boolean {
        return localDataSource.isBookExists(isbn)
    }

    suspend fun updateReadingProgress(itemId: Int, page: Int, elapsedTime: Int): Boolean {
        return localDataSource.updateReadingProgress(itemId, page, elapsedTime)
    }
}