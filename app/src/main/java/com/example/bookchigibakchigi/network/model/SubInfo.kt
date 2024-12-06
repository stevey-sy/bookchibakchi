package com.example.bookchigibakchigi.network.model

data class SubInfo(
    val ebookList: List<EbookItem>?,
    val usedList: UsedListInfo?,
    val newBookList: List<NewBookInfo>?,
    val paperBookList: List<PaperBookInfo>?,
    val fileFormatList: List<FileFormatInfo>?
)
