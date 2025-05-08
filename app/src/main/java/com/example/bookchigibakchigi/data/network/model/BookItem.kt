package com.example.bookchigibakchigi.data.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookItem(
    val title: String,
    val link: String,
    val image: String,
    val author: String,
    val price: String?,
    val discount: String?,
    val publisher: String,
    val pubdate: String?,
    val isbn: String,
    val description: String
) : Parcelable