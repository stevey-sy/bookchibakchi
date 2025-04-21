package com.example.bookchigibakchigi.mapper

import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.model.BookUiModel

object BookMapper {
    fun toUiModel(entity: BookEntity): BookUiModel {
        return BookUiModel(
            itemId = entity.itemId,
            title = entity.title,
            author = entity.author,
            publisher = entity.publisher,
            isbn = entity.isbn,
            coverImageUrl = entity.coverImageUrl,
            bookType = entity.bookType,
            totalPageCnt = entity.totalPageCnt,
            currentPageCnt = entity.currentPageCnt,
            challengePageCnt = entity.challengePageCnt,
            startDate = entity.startDate,
            endDate = entity.endDate,
            elapsedTimeInSeconds = entity.elapsedTimeInSeconds,
            completedReadingCnt = entity.completedReadingCnt,
            shelfPosition = -1
        )
    }

    fun toUiModels(entities: List<BookEntity>): List<BookUiModel> {
        return entities.map { toUiModel(it) }
    }

    fun toEntity(uiModel: BookUiModel): BookEntity {
        return BookEntity(
            itemId = uiModel.itemId,
            title = uiModel.title,
            author = uiModel.author,
            publisher = uiModel.publisher,
            isbn = uiModel.isbn,
            coverImageUrl = uiModel.coverImageUrl,
            bookType = uiModel.bookType,
            totalPageCnt = uiModel.totalPageCnt,
            currentPageCnt = uiModel.currentPageCnt,
            challengePageCnt = uiModel.challengePageCnt,
            startDate = uiModel.startDate,
            endDate = uiModel.endDate,
            elapsedTimeInSeconds = uiModel.elapsedTimeInSeconds,
            completedReadingCnt = uiModel.completedReadingCnt
        )
    }

    fun toEntities(uiModels: List<BookUiModel>): List<BookEntity> {
        return uiModels.map { toEntity(it) }
    }
} 