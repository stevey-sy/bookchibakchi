package com.example.bookchigibakchigi.data.repository

import com.example.bookchigibakchigi.data.PhotoCardWithTextContents
import com.example.bookchigibakchigi.data.dao.PhotoCardDao
import com.example.bookchigibakchigi.data.entity.CardTextEntity
import com.example.bookchigibakchigi.data.entity.PhotoCardEntity

class PhotoCardRepository(private val photoCardDao: PhotoCardDao) {
    /** 📌 포토카드와 텍스트를 함께 저장 */
    suspend fun insertPhotoCardWithTexts(photoCard: PhotoCardEntity, textContents: List<CardTextEntity>) {
        photoCardDao.insertPhotoCardWithTexts(photoCard, textContents)
    }

    /** 📌 특정 포토카드 ID로 포토카드 + 텍스트 리스트 가져오기 */
    suspend fun getPhotoCardWithTexts(photoCardId: Long): PhotoCardWithTextContents {
        return photoCardDao.getPhotoCardWithTexts(photoCardId)
    }

    /** 📌 포토카드 삭제 (연결된 Text도 함께 삭제됨) */
    suspend fun deletePhotoCard(photoCard: PhotoCardEntity) {
        photoCardDao.deletePhotoCard(photoCard)
    }
}