package com.example.bookchigibakchigi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Int = 0, // 자동 생성되는 ID
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
