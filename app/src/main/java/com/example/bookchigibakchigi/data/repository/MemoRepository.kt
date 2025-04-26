package com.example.bookchigibakchigi.data.repository

import com.example.bookchigibakchigi.data.dao.MemoDao
import com.example.bookchigibakchigi.data.entity.MemoEntity
import com.example.bookchigibakchigi.data.entity.MemoTagCrossRef
import com.example.bookchigibakchigi.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepository @Inject constructor(
    private val memoDao: MemoDao
) {
    suspend fun insertMemo(memo: MemoEntity): Long = memoDao.insertMemo(memo)

    suspend fun updateMemo(memo: MemoEntity) = memoDao.updateMemo(memo)

    suspend fun deleteMemo(memo: MemoEntity) = memoDao.deleteMemo(memo)

    fun getMemosByBookId(bookId: Int): Flow<List<MemoEntity>> = memoDao.getMemosByBookId(bookId)

    suspend fun getMemoById(memoId: Long): MemoEntity? = memoDao.getMemoById(memoId)

    suspend fun addTagToMemo(memoId: Long, tagId: Long) {
        memoDao.insertMemoTagCrossRef(MemoTagCrossRef(memoId, tagId))
    }

    suspend fun removeTagFromMemo(memoId: Long, tagId: Long) {
        memoDao.deleteMemoTagCrossRef(MemoTagCrossRef(memoId, tagId))
    }

    fun getTagsForMemo(memoId: Long): Flow<List<TagEntity>> = memoDao.getTagsForMemo(memoId)

    fun getMemosByTagId(tagId: Long): Flow<List<MemoEntity>> = memoDao.getMemosByTagId(tagId)
} 