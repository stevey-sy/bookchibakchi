package com.example.bookchigibakchigi.mapper

import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.model.BookUiModel
import com.example.bookchigibakchigi.model.SearchBookUiModel
import com.example.bookchigibakchigi.network.model.AladinBookItem

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

    fun toSearchBookUiModel(aladinBookItem: AladinBookItem): SearchBookUiModel {
        return SearchBookUiModel(
            title = aladinBookItem.title,
            author = aladinBookItem.author,
            publisher = aladinBookItem.publisher,
            isbn = aladinBookItem.isbn13,
            cover = aladinBookItem.cover,
            page = aladinBookItem.subInfo?.itemPage ?: 0, // AladinBookItem에는 페이지 정보가 없어 기본값 0으로 설정
            description = aladinBookItem.description ?: "",
            rate = aladinBookItem.customerReviewRank
        )
    }

    fun toSearchBookUiModels(aladinBookItems: List<AladinBookItem>): List<SearchBookUiModel> {
        return aladinBookItems.map { toSearchBookUiModel(it) }
    }

    fun toEntityFromSearchBook(searchBook: SearchBookUiModel): BookEntity {
        return BookEntity(
            title = searchBook.title,
            author = searchBook.author,
            publisher = searchBook.publisher,
            isbn = searchBook.isbn,
            coverImageUrl = searchBook.cover,
            bookType = "0",
            totalPageCnt = searchBook.page,
            challengePageCnt = 0,
            startDate = "",
            endDate = "",
            currentPageCnt = 0,
            elapsedTimeInSeconds = 0,
            completedReadingCnt = 0
        )
    }
} 