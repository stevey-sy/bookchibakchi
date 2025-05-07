package com.example.bookchigibakchigi.data.dao

import androidx.room.*
import com.example.bookchigibakchigi.data.entity.MemoEntity
import com.example.bookchigibakchigi.data.entity.MemoTagCrossRef
import com.example.bookchigibakchigi.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: MemoEntity): Long

    @Update
    suspend fun updateMemo(memo: MemoEntity)

    @Delete
    suspend fun deleteMemo(memo: MemoEntity)

    @Query("SELECT * FROM memos WHERE bookId = :bookId ORDER BY memoId DESC")
    fun getMemosByBookId(bookId: Int): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE memoId = :memoId")
    suspend fun getMemoById(memoId: Long): MemoEntity?

    @Query("DELETE FROM memos WHERE memoId = :memoId")
    suspend fun deleteMemoById(memoId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoTagCrossRef(crossRef: MemoTagCrossRef)

    @Delete
    suspend fun deleteMemoTagCrossRef(crossRef: MemoTagCrossRef)

    @Transaction
    @Query("SELECT * FROM tags WHERE tagId IN (SELECT tagId FROM memo_tags WHERE memoId = :memoId)")
    fun getTagsForMemo(memoId: Long): Flow<List<TagEntity>>

    @Transaction
    @Query("SELECT * FROM memos WHERE memoId IN (SELECT memoId FROM memo_tags WHERE tagId = :tagId)")
    fun getMemosByTagId(tagId: Long): Flow<List<MemoEntity>>
} 