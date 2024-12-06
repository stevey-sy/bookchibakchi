package com.example.bookchigibakchigi.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EbookItem(
    val itemId: Int,
    val ISBN: String,
    val isbn13: String,
    val priceSales: Int,
    val link: String
): Parcelable
