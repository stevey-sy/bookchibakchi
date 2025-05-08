package com.example.bookchigibakchigi.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bookchigibakchigi.data.network.model.NaverBookResponse
import com.example.bookchigibakchigi.data.network.service.NaverBookService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookSearchRepository(private val naverBookService: NaverBookService) {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // 데이터 반환하도록 수정
    suspend fun searchBooks(query: String, start: Int): NaverBookResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = naverBookService.searchBooks(query)
                response // 성공적으로 데이터를 반환
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.postValue("책 검색 중 오류가 발생했습니다.")
                null // 실패 시 null 반환
            }
        }
    }
}