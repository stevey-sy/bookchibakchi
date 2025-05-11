package com.example.bookchigibakchigi.data.repository

import com.example.bookchigibakchigi.data.datasource.MemoLocalDataSource
import com.example.bookchigibakchigi.data.entity.MemoEntity
import com.example.bookchigibakchigi.data.entity.MemoTagCrossRef
import com.example.bookchigibakchigi.data.entity.TagEntity
import com.example.bookchigibakchigi.mapper.MemoMapper
import com.example.bookchigibakchigi.model.MemoUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepository @Inject constructor(
    private val localDataSource: MemoLocalDataSource,
) {
    suspend fun insertMemo(memo: MemoEntity): Long = localDataSource.insertMemo(memo)

    suspend fun updateMemo(memo: MemoEntity) = localDataSource.updateMemo(memo)

    suspend fun deleteMemo(memo: MemoEntity) = localDataSource.deleteMemo(memo)

    fun getMemosByBookId(bookId: Int): Flow<List<MemoUiModel>> {
        return localDataSource.getMemosByBookId(bookId).map { memos ->
            memos.map { memo ->
                MemoMapper.toUiModel(memo)
            }
        }
    }

    suspend fun getMemoById(memoId: Long): MemoEntity? = localDataSource.getMemoById(memoId)

    suspend fun deleteMemoById(memoId: Long) = localDataSource.deleteMemoById(memoId)

    suspend fun addTagToMemo(memoId: Long, tagId: Long) {
        localDataSource.insertMemoTagCrossRef(MemoTagCrossRef(memoId, tagId))
    }

    suspend fun removeTagFromMemo(memoId: Long, tagId: Long) {
        localDataSource.deleteMemoTagCrossRef(MemoTagCrossRef(memoId, tagId))
    }

    fun getTagsForMemo(memoId: Long): Flow<List<TagEntity>> = localDataSource.getTagsForMemo(memoId)

    fun getMemosByTagId(tagId: Long): Flow<List<MemoEntity>> = localDataSource.getMemosByTagId(tagId)
} 