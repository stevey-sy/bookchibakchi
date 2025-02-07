package com.example.bookchigibakchigi.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Int = 0, // 자동 생성되는 ID
    val title: String,
    val author: String,
    val publisher: String,
    val isbn: String,
    val coverImageUrl: String,
    val bookType: String,
    val totalPageCnt: Int,
    val currentPageCnt: Int,
    val challengePageCnt: Int,
    val startDate: String,
    val endDate: String,
    val elapsedTimeInSeconds: Int = 0,
    val completedReadingCnt: Int = 0,

) : Parcelable {
    val progressPercentage: Int
        get() = if (totalPageCnt > 0) (currentPageCnt * 100) / totalPageCnt else 0

    fun getProgressText(): String {
        return "p. $currentPageCnt / $totalPageCnt"
    }

    fun getAuthorText(): String {
        val authorText = if (author.contains("(지은이)")) {
            author.substringBefore("(지은이)").trim() // "(지은이)" 이전 문자열 추출 및 공백 제거
        } else {
            author
        }
        return authorText
    }

    fun getElapsedTimeFormatted(): String {
        val hours = elapsedTimeInSeconds / 3600
        val minutes = (elapsedTimeInSeconds % 3600) / 60
        val seconds = elapsedTimeInSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
