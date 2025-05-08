package com.example.bookchigibakchigi.data.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewBookInfo(
    val itemId: Int,
    val isbn: String,
    val isbn13: String,
    val priceSales: Int,
    val link: String
): Parcelable
