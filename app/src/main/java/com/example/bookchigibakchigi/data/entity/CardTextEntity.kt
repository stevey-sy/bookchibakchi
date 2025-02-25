package com.example.bookchigibakchigi.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photo_card_text_content",
    foreignKeys = [
        ForeignKey(
            entity = CardTextEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoCardId"],
            onDelete = ForeignKey.CASCADE // PhotoCard 삭제 시 연결된 TextContent도 삭제
        )
    ],
    indices = [Index(value = ["photoCardId"])]
)
data class CardTextEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoCardId: Long, // Foreign Key
    val type: String,
    val content: String,
    val textColor: String,
    val textSize: Float,
    val textBackgroundColor: String,
    val startX: Float,
    val startY: Float,
    val font: String
)
