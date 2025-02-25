package com.example.bookchigibakchigi.data

import androidx.room.Embedded
import androidx.room.Relation
import com.example.bookchigibakchigi.data.entity.CardTextEntity
import com.example.bookchigibakchigi.data.entity.PhotoCardEntity

data class PhotoCardWithTextContents (
    @Embedded val photoCard: PhotoCardEntity, // 1개의 포토카드 정보
    @Relation(
        parentColumn = "id", // PhotoCard 테이블의 기본 키
        entityColumn = "photoCardId" // PhotoCardTextContent 테이블의 외래 키
    )
    val textContents: List<CardTextEntity> // 해당 포토카드에 속한 텍스트 목록
)