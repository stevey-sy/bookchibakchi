package com.example.bookchigibakchigi.data.dao

import androidx.room.*
import com.example.bookchigibakchigi.data.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY itemId DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE itemId = :itemId")
    fun getBookById(itemId: Int): Flow<BookEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Transaction
    suspend fun insertAndGetBook(book: BookEntity): BookEntity {
        val id = insertBook(book)
        return book.copy(itemId = id.toInt())
    }

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()

    @Query("""
        UPDATE books 
        SET currentPageCnt = :currentPage, 
            elapsedTimeInSeconds = elapsedTimeInSeconds + :elapsedTime 
        WHERE itemId = :itemId
    """)
    suspend fun updateReadingProgress(itemId: Int, currentPage: Int, elapsedTime: Int): Int

    @Query("SELECT COUNT(*) FROM books WHERE isbn = :isbn")
    suspend fun isBookExists(isbn: String): Int
}