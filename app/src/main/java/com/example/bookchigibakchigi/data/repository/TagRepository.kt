package com.example.bookchigibakchigi.data.repository

import com.example.bookchigibakchigi.data.dao.TagDao
import com.example.bookchigibakchigi.data.entity.MemoTagCrossRef
import com.example.bookchigibakchigi.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    suspend fun getTagsByMemoId(memoId: Long): List<TagEntity> {
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

    suspend fun addTagToMemo(memoId: Long, tagId: Long) {
        tagDao.insertMemoTagCrossRef(MemoTagCrossRef(memoId, tagId))
    }

    suspend fun removeTagFromMemo(memoId: Long, tagId: Long) {
        tagDao.deleteMemoTagCrossRef(MemoTagCrossRef(memoId, tagId))
    }

    suspend fun removeAllTagsFromMemo(memoId: Long) {
        tagDao.deleteAllMemoTags(memoId)
    }
} 