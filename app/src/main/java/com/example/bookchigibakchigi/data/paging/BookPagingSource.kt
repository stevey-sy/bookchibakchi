package com.example.bookchigibakchigi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchigibakchigi.data.network.model.AladinBookItem
import com.example.bookchigibakchigi.data.repository.AladinBookRepository

class BookPagingSource(
    private val repository: AladinBookRepository,
    private val query: String
) : PagingSource<Int, AladinBookItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AladinBookItem> {
        return try {
            val page = params.key ?: 1
            val start = (page - 1) * 10 + 1

            val response = repository.fetchBooks(query, start, 10)
            val items = response.item ?: emptyList()

            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (items.size < 10) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, AladinBookItem>): Int? {
        TODO("Not yet implemented")
    }
}