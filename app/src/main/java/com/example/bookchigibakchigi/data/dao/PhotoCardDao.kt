package com.example.bookchigibakchigi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.bookchigibakchigi.data.PhotoCardWithTextContents
import com.example.bookchigibakchigi.data.entity.CardTextEntity
import com.example.bookchigibakchigi.data.entity.PhotoCardEntity

@Dao
interface PhotoCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotoCard(photoCard: PhotoCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTextContents(textContents: List<CardTextEntity>)

    @Transaction
    suspend fun insertPhotoCardWithTexts(photoCard: PhotoCardEntity, textContents: List<CardTextEntity>) {
        val cardId = insertPhotoCard(photoCard)
        val updatedContents = textContents.map { it.copy(photoCardId = cardId) }
        insertTextContents(updatedContents)
    }

    @Transaction
    @Query("SELECT * FROM photo_card WHERE id = :photoCardId")
    suspend fun getPhotoCardWithTexts(photoCardId: Long): PhotoCardWithTextContents

    @Delete
    suspend fun deletePhotoCard(photoCard: PhotoCardEntity)
}