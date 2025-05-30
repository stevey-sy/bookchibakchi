package com.example.bookchigibakchigi.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "memos",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["itemId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MemoEntity(
    @PrimaryKey(autoGenerate = true) val memoId: Long = 0,
    val bookId: Int,
    val content: String,
    val pageNumber: Int,
    val background: Int = 0,
    val imgUrl: String = "",
    val createdAt: Long,
    val updatedAt: Long = System.currentTimeMillis()
) 