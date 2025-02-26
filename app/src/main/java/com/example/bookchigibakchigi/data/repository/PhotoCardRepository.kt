package com.example.bookchigibakchigi.data.repository

import com.example.bookchigibakchigi.data.PhotoCardWithTextContents
import com.example.bookchigibakchigi.data.dao.PhotoCardDao
import com.example.bookchigibakchigi.data.entity.PhotoCardEntity

class PhotoCardRepository(private val photoCardDao: PhotoCardDao) {
    suspend fun insertPhotoCard(photoCard: PhotoCardEntity) {
        photoCardDao.insertPhotoCard(photoCard) // ✅ Room을 사용
    }

    suspend fun getPhotoCard(photoCardId: Long): PhotoCardWithTextContents {
        return photoCardDao.getPhotoCardWithTexts(photoCardId) // ✅ Room을 사용
    }
}