package com.example.bookchigibakchigi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val tagId: Long = 0,
    val name: String,
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) 