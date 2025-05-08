package com.example.bookchigibakchigi.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.bookchigibakchigi.data.network.model.AladinBookItem
import com.example.bookchigibakchigi.data.network.model.AladinBookSearchResponse
import com.example.bookchigibakchigi.data.network.service.AladinBookApiService
import com.example.bookchigibakchigi.data.paging.BookPagingSource
import kotlinx.coroutines.flow.Flow

class AladinBookRepository(private val apiService: AladinBookApiService) {

    suspend fun searchBooks(
        query: String
    ): List<AladinBookItem> {
        // Retrofit의 suspend 지원 메서드로 API 호출
        val response = apiService.searchBooks(query = query)
        if (response.isSuccessful) {
            return response.body()?.item ?: emptyList()
        } else {
            throw Exception("API call failed: ${response.code()}")
        }
    }

    // 내부에서 PagingSource가 사용할 저수준 함수
    suspend fun fetchBooks(query: String, start: Int, maxResults: Int): AladinBookSearchResponse {
        val response = apiService.searchBooks(query = query, start = start, maxResults = maxResults)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("빈 응답")
        } else {
            throw Exception("API 실패: ${response.code()}")
        }
    }

    // ViewModel 등 외부에서 호출할 고수준 함수 (Flow<PagingData>)
    fun getPagedBooks(query: String): Flow<PagingData<AladinBookItem>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { BookPagingSource(this, query) }
        ).flow
    }


    suspend fun getBookDetail(
        itemId: String
    ) : List<AladinBookItem> {
        // Retrofit의 suspend 지원 메서드로 API 호출
        val response = apiService.getBookDetail(itemId = itemId)
        if (response.isSuccessful) {
            return response.body()?.item ?: emptyList()
        } else {
            throw Exception("API call failed: ${response.code()}")
        }
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