package com.example.bookchigibakchigi.data.datasource

import com.example.bookchigibakchigi.data.dao.TagDao
import com.example.bookchigibakchigi.data.entity.MemoTagCrossRef
import com.example.bookchigibakchigi.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagLocalDataSource @Inject constructor(
    private val tagDao: TagDao
) {
    fun getTagsByMemoId(memoId: Long): Flow<List<TagEntity>> {
        return tagDao.getTagsByMemoId(memoId)
    }

    suspend fun insertTag(tag: TagEntity): Long {
        return tagDao.insertTag(tag)
    }

    suspend fun deleteTag(tag: TagEntity) {
        tagDao.deleteTag(tag)
    }

    suspend fun updateTag(tag: TagEntity) {
        tagDao.updateTag(tag)
    }

    fun getAllTags(): Flow<List<TagEntity>> {
        return tagDao.getAllTags()
    }

    suspend fun getTagById(tagId: Long): TagEntity? {
        return tagDao.getTagById(tagId)
    }

    suspend fun getTagByName(name: String): TagEntity? = tagDao.getTagByName(name)

    suspend fun insertMemoTagCrossRef(crossRef: MemoTagCrossRef) {
        tagDao.insertMemoTagCrossRef(crossRef)
    }

    suspend fun deleteMemoTagCrossRef(crossRef: MemoTagCrossRef) {
        tagDao.deleteMemoTagCrossRef(crossRef)
    }

    suspend fun deleteAllMemoTags(memoId: Long) {
        tagDao.deleteAllMemoTags(memoId)
    }
} 