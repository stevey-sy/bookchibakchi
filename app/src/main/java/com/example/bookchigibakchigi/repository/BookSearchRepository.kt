package com.example.bookchigibakchigi.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bookchigibakchigi.network.model.NaverBookResponse
import com.example.bookchigibakchigi.network.service.NaverBookService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookSearchRepository(private val naverBookService: NaverBookService) {

    // MutableLiveData를 Nullable 타입으로 변경
    private val _bookSearchResults = MutableLiveData<NaverBookResponse?>()
    val bookSearchResults: LiveData<NaverBookResponse?> get() = _bookSearchResults

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // 책 검색 메서드
    suspend fun searchBooks(query: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = naverBookService.searchBooks(query)
                _bookSearchResults.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
                // 에러 메시지 업데이트
                _errorMessage.postValue("책 검색 중 오류가 발생했습니다.")
                _bookSearchResults.postValue(null) // 여기도 Nullable 허용
            }
        }
    }
}