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
    val itemPage: Int
) : Parcelable
