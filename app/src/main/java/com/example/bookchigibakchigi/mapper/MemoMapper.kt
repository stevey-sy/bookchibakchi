package com.example.bookchigibakchigi.mapper

import com.example.bookchigibakchigi.data.entity.MemoEntity
import com.example.bookchigibakchigi.model.MemoUiModel
import com.example.bookchigibakchigi.model.TagUiModel

object MemoMapper {
    fun toUiModel(entity: MemoEntity, tags: List<TagUiModel> = emptyList()): MemoUiModel {
        return MemoUiModel(
            memoId = entity.memoId,
            bookId = entity.bookId,
            content = entity.content,
            pageNumber = entity.pageNumber,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            background = entity.background,
            tags = tags
        )
    }

    fun toEntity(uiModel: MemoUiModel): MemoEntity {
        return MemoEntity(
            memoId = uiModel.memoId,
            bookId = uiModel.bookId,
            content = uiModel.content,
            pageNumber = uiModel.pageNumber,
            createdAt = uiModel.createdAt,
            updatedAt = uiModel.updatedAt,
            background = uiModel.background
        )
    }
} 