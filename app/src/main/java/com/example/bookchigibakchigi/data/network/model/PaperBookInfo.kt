package com.example.bookchigibakchigi.data.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaperBookInfo(
    val itemId: Int,
    val isbn: String,
    val priceSales: Int,
    val link: String
): Parcelable
