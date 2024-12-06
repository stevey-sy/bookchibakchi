package com.example.bookchigibakchigi.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsedDetail(
    val itemCount: Int,
    val minPrice: Int,
    val link: String
) : Parcelable
