package com.example.bookchigibakchigi.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


data class PhotoCard(
    val id: String,
    val fileName: String,
    val textObjects: List<TextObject>,
    val backgroundImageName: String,
    val width: Float,
    val height: Float,
    val isbn: String,
    val createdAt: String,
    val likeCnt: Int,
)
