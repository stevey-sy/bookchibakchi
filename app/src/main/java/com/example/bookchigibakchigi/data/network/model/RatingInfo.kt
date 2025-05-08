package com.example.bookchigibakchigi.data.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RatingInfo(
    val ratingScore: Float?,
    val ratingCount: Int?
): Parcelable
