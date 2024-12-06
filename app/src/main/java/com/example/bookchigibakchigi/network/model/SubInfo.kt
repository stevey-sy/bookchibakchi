package com.example.bookchigibakchigi.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubInfo(
    val ebookList: List<EbookItem>?,
    val usedList: UsedListInfo?,
    val newBookList: List<NewBookInfo>?,
    val paperBookList: List<PaperBookInfo>?,
    val fileFormatList: List<FileFormatInfo>?,
    val itemPage: Int,

    // 추가된 필드
    val subTitle: String?, // 부제
    val originalTitle: String?, // 원제
    val subbarcode: String?, // 부가기호
    val taxFree: Boolean?, // 비과세 여부
    val toc: String?, // 목차
    val previewImgList: List<String>?, // 미리보기 이미지 경로
    val cardReviewImgList: List<String>?, // 카드리뷰 이미지 경로

    val ratingInfo: RatingInfo?, // 평점 정보
    val bestSellerRank: String? // 주간 베스트셀러 순위
) : Parcelable
