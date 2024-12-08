package com.example.bookchigibakchigi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val itemId: String, // 고유 ID
    val title: String,
    val author: String,
    val publisher: String,
    val isbn: String,
    val coverImageUrl: String,
    val bookType: String,
    val totalPageCnt: Int,
    val processingPageCnt: Int,
    val startDate: String,
    val endDate: String,
)
