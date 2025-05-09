package com.example.bookchigibakchigi.model

import com.example.bookchigibakchigi.util.DateUtil

data class AddMemoFormUiModel(
    val isModify: Boolean = false,
    val page: String = "0",
    val backgroundPosition: Int = 2,
    val content: String = "내용을 입력해 주세요.",
    val tagList: List<TagUiModel> = emptyList(),
    val createdAt: String = DateUtil.getFormattedToday(),
    val isContentValid: Boolean = false,
    val memoId: Long? = null,
    val bookId: Int? = null,
)
