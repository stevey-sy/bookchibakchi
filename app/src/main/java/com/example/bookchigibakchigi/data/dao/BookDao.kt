package com.example.bookchigibakchigi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookchigibakchigi.data.entity.BookEntity

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Query("SELECT * FROM books WHERE itemId = :itemId")
    suspend fun getBookById(itemId: String): BookEntity?

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<BookEntity>

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("UPDATE books SET currentPageCnt = :currentPage WHERE itemId = :itemId")
    suspend fun updateCurrentPage(itemId: Int, currentPage: Int)

}