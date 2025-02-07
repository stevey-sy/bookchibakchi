package com.example.bookchigibakchigi.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AladinBookItem(
    val title: String,
    val link: String,
    val author: String,
    val pubDate: String,
    val description: String?,
    val isbn: String,
    val isbn13: String,
    val pricesales: Int,
    val pricestandard: Int,
    val mallType: String,
    val stockstatus: String?,
    val mileage: Int,
    val cover: String,
    val publisher: String,
    val salesPoint: Int,
    val adult: Boolean,
    val fixedPrice: Boolean,
    val customerReviewRank: Float,
    val bestDuration: String?,
    val bestRank: Int?,
    val seriesInfo: SeriesInfo?,
    val subInfo: SubInfo?
) : Parcelable {
    fun getAuthorText(): String {
        val authorText = if (author.contains("(지은이)")) {
            author.substringBefore("(지은이)").trim() // "(지은이)" 이전 문자열 추출 및 공백 제거
        } else {
            author
        }
        return authorText
    }
}