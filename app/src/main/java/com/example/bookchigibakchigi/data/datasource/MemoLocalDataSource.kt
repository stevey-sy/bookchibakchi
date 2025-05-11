package com.example.bookchigibakchigi.data.datasource

import com.example.bookchigibakchigi.data.dao.MemoDao
import com.example.bookchigibakchigi.data.entity.MemoEntity
import com.example.bookchigibakchigi.data.entity.MemoTagCrossRef
import com.example.bookchigibakchigi.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoLocalDataSource @Inject constructor(
    private val memoDao: MemoDao
) {
    suspend fun insertMemo(memo: MemoEntity): Long = memoDao.insertMemo(memo)

    suspend fun updateMemo(memo: MemoEntity) = memoDao.updateMemo(memo)

    suspend fun deleteMemo(memo: MemoEntity) = memoDao.deleteMemo(memo)

    fun getMemosByBookId(bookId: Int): Flow<List<MemoEntity>> {
        return memoDao.getMemosByBookId(bookId)
    }

    suspend fun getMemoById(memoId: Long): MemoEntity? = memoDao.getMemoById(memoId)

    suspend fun deleteMemoById(memoId: Long) = memoDao.deleteMemoById(memoId)

    suspend fun insertMemoTagCrossRef(crossRef: MemoTagCrossRef) {
        memoDao.insertMemoTagCrossRef(crossRef)
    }

    suspend fun deleteMemoTagCrossRef(crossRef: MemoTagCrossRef) {
        memoDao.deleteMemoTagCrossRef(crossRef)
    }

    fun getTagsForMemo(memoId: Long): Flow<List<TagEntity>> = memoDao.getTagsForMemo(memoId)

    fun getMemosByTagId(tagId: Long): Flow<List<MemoEntity>> = memoDao.getMemosByTagId(tagId)
} 