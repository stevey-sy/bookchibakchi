package com.example.bookchigibakchigi.data.dao

import androidx.room.*
import com.example.bookchigibakchigi.data.entity.MemoTagCrossRef
import com.example.bookchigibakchigi.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags WHERE tagId IN (SELECT tagId FROM memo_tags WHERE memoId = :memoId)")
    fun getTagsByMemoId(memoId: Long): Flow<List<TagEntity>>

    @Insert
    suspend fun insertTag(tag: TagEntity): Long

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE tagId = :tagId")
    suspend fun getTagById(tagId: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE name = :name")
    suspend fun getTagByName(name: String): TagEntity?

    @Insert
    suspend fun insertMemoTagCrossRef(crossRef: MemoTagCrossRef)

    @Delete
    suspend fun deleteMemoTagCrossRef(crossRef: MemoTagCrossRef)

    @Query("DELETE FROM memo_tags WHERE memoId = :memoId")
    suspend fun deleteAllMemoTags(memoId: Long)
} 