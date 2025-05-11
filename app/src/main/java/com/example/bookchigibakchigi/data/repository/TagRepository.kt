package com.example.bookchigibakchigi.data.repository

import com.example.bookchigibakchigi.data.datasource.TagLocalDataSource
import com.example.bookchigibakchigi.data.entity.MemoTagCrossRef
import com.example.bookchigibakchigi.data.entity.TagEntity
import com.example.bookchigibakchigi.mapper.TagMapper
import com.example.bookchigibakchigi.model.TagUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val localDataSource: TagLocalDataSource
) {
    fun getTagsByMemoId(memoId: Long): Flow<List<TagUiModel>> {
        return localDataSource.getTagsByMemoId(memoId).map { tags ->
            tags.map { tag ->
                TagMapper.toUiModel(tag)
            }
        }
    }

    suspend fun insertTag(tag: TagEntity): Long {
        return localDataSource.insertTag(tag)
    }

    suspend fun deleteTag(tag: TagEntity) {
        localDataSource.deleteTag(tag)
    }

    suspend fun updateTag(tag: TagEntity) {
        localDataSource.updateTag(tag)
    }

    fun getAllTags(): Flow<List<TagEntity>> {
        return localDataSource.getAllTags()
    }

    suspend fun getTagById(tagId: Long): TagEntity? {
        return localDataSource.getTagById(tagId)
    }

    suspend fun getTagByName(name: String): TagEntity? = localDataSource.getTagByName(name)

    suspend fun addTagToMemo(memoId: Long, tagId: Long) {
        localDataSource.insertMemoTagCrossRef(MemoTagCrossRef(memoId, tagId))
    }

    suspend fun removeTagFromMemo(memoId: Long, tagId: Long) {
        localDataSource.deleteMemoTagCrossRef(MemoTagCrossRef(memoId, tagId))
    }

    suspend fun removeAllTagsFromMemo(memoId: Long) {
        localDataSource.deleteAllMemoTags(memoId)
    }
} 