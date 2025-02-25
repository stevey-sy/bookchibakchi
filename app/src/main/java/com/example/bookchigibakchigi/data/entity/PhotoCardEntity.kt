package com.example.bookchigibakchigi.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "photo_card")
data class PhotoCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageFileName: String,
    val isbn: String,
    val createdAt: Long // timestamp
) : Parcelable
