package com.example.bookchigibakchigi.mapper

import com.example.bookchigibakchigi.data.entity.TagEntity
import com.example.bookchigibakchigi.model.TagUiModel

object TagMapper {
    fun toUiModel(entity: TagEntity): TagUiModel {
        return TagUiModel(
            tagId = entity.tagId,
            name = entity.name,
            backgroundColor = entity.backgroundColor,
            textColor = entity.textColor,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(uiModel: TagUiModel): TagEntity {
        return TagEntity(
            tagId = uiModel.tagId,
            name = uiModel.name,
            backgroundColor = uiModel.backgroundColor,
            textColor = uiModel.textColor,
            createdAt = uiModel.createdAt
        )
    }
} 