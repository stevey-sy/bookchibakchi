package com.example.bookchigibakchigi.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.bookchigibakchigi.data.datasource.BookSearchRemoteDataSource
import com.example.bookchigibakchigi.data.network.model.AladinBookItem
import com.example.bookchigibakchigi.data.network.model.AladinBookSearchResponse
import com.example.bookchigibakchigi.data.paging.BookPagingSource
import kotlinx.coroutines.flow.Flow

class BookSearchRepository(private val remoteDataSource: BookSearchRemoteDataSource) {

    suspend fun searchBooks(query: String): List<AladinBookItem> {
        return remoteDataSource.searchBooks(query)
    }

    // 내부에서 PagingSource가 사용할 저수준 함수
    suspend fun fetchBooks(query: String, start: Int, maxResults: Int): AladinBookSearchResponse {
        return remoteDataSource.fetchBooks(query, start, maxResults)
    }

    // ViewModel 등 외부에서 호출할 고수준 함수 (Flow<PagingData>)
    fun getPagedBooks(query: String): Flow<PagingData<AladinBookItem>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { BookPagingSource(this, query) }
        ).flow
    }

    suspend fun getBookDetail(itemId: String) : List<AladinBookItem> {
        return remoteDataSource.getBookDetail(itemId)
    }

//    fun searchBooks(
//        ttbKey: String,
//        query: String,
//        onSuccess: (List<AladinBookItem>) -> Unit,
//        onError: (Throwable) -> Unit
//    ) {
//        apiService.searchBooks(ttbKey = ttbKey, query = query)
//            .enqueue(object : Callback<AladinBookResponse> {
//                override fun onResponse(
//                    call: Call<AladinBookResponse>,
//                    response: Response<AladinBookResponse>
//                ) {
//                    if (response.isSuccessful) {
//                        response.body()?.item?.let { books ->
//                            onSuccess(books)
//                        } ?: onError(Exception("No books found"))
//                    } else {
//                        onError(Exception("API call failed: ${response.code()}"))
//                    }
//                }
//
//                override fun onFailure(call: Call<AladinBookResponse>, t: Throwable) {
//                    onError(t)
//                }
//            })
//    }
}