package com.example.bookchigibakchigi.data.repository

import com.example.bookchigibakchigi.network.model.AladinBookItem
import com.example.bookchigibakchigi.network.service.AladinBookApiService

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