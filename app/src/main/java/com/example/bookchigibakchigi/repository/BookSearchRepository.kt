package com.example.bookchigibakchigi.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bookchigibakchigi.network.model.NaverBookResponse
import com.example.bookchigibakchigi.network.service.NaverBookService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookSearchRepository(private val naverBookService: NaverBookService) {

    private val _bookSearchResults = MutableLiveData<NaverBookResponse?>()
    val bookSearchResults: LiveData<NaverBookResponse?> get() = _bookSearchResults

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // 페이지 번호와 검색어를 인자로 추가
    suspend fun searchBooks(query: String, start: Int) {
        withContext(Dispatchers.IO) {
            try {
                val response = naverBookService.searchBooks(query, start)
                _bookSearchResults.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.postValue("책 검색 중 오류가 발생했습니다.")
                _bookSearchResults.postValue(null)
            }
        }
    }
}