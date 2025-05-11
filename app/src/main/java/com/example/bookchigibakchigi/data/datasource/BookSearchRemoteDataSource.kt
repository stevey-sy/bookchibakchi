package com.example.bookchigibakchigi.data.datasource

import com.example.bookchigibakchigi.data.network.model.AladinBookItem
import com.example.bookchigibakchigi.data.network.model.AladinBookSearchResponse
import com.example.bookchigibakchigi.data.network.service.AladinBookApiService

class BookSearchRemoteDataSource(private val apiService: AladinBookApiService) {

    suspend fun searchBooks(query: String): List<AladinBookItem> {
        val response = apiService.searchBooks(query = query)
        if (response.isSuccessful) {
            return response.body()?.item ?: emptyList()
        } else {
            throw Exception("API call failed: ${response.code()}")
        }
    }

    suspend fun fetchBooks(query: String, start: Int, maxResults: Int): AladinBookSearchResponse {
        val response = apiService.searchBooks(query = query, start = start, maxResults = maxResults)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("빈 응답")
        } else {
            throw Exception("API 실패: ${response.code()}")
        }
    }

    suspend fun getBookDetail(itemId: String): List<AladinBookItem> {
        val response = apiService.getBookDetail(itemId = itemId)
        if (response.isSuccessful) {
            return response.body()?.item ?: emptyList()
        } else {
            throw Exception("API call failed: ${response.code()}")
        }
    }
} 