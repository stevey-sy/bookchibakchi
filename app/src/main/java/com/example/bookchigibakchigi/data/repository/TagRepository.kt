package com.example.bookchigibakchigi.data.repository

import com.example.bookchigibakchigi.data.dao.TagDao
import com.example.bookchigibakchigi.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    suspend fun insertTag(tag: TagEntity): Long = tagDao.insertTag(tag)

    suspend fun updateTag(tag: TagEntity) = tagDao.updateTag(tag)

    suspend fun deleteTag(tag: TagEntity) = tagDao.deleteTag(tag)

    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()

    suspend fun getTagById(tagId: Long): TagEntity? = tagDao.getTagById(tagId)

    suspend fun getTagByName(name: String): TagEntity? = tagDao.getTagByName(name)
} 