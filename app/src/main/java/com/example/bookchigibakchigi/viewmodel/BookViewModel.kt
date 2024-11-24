package com.example.bookchigibakchigi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.network.model.BookItem
import com.example.bookchigibakchigi.network.model.NaverBookResponse
import com.example.bookchigibakchigi.repository.BookSearchRepository
import kotlinx.coroutines.launch

class BookViewModel(private val repository: BookSearchRepository) : ViewModel() {

    private val _bookSearchResults = MutableLiveData<List<BookItem>>()
    val bookSearchResults: LiveData<List<BookItem>> get() = _bookSearchResults
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = repository.errorMessage
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var currentQuery: String = ""
    private var currentStart: Int = 1
    private var totalResults: Int = 0

    // 첫 번째 검색
    fun searchBooks(query: String) {
        currentQuery = query
        currentStart = 1  // 처음 검색할 때는 1페이지부터
        viewModelScope.launch {
            _isLoading.value = true // 로딩 상태 시작
            try {
                // Repository에서 데이터 요청
                val result = repository.searchBooks(query, currentStart)
                result?.let {
                    _bookSearchResults.value = it.items // 검색 결과 반영
                } ?: run {
                    _errorMessage.value = "검색 결과가 없습니다."
                }
            } catch (e: Exception) {
                // 에러 처리
                _errorMessage.value = "검색 중 문제가 발생했습니다."
            } finally {
                _isLoading.value = false // 로딩 상태 종료
            }
        }
    }

    // 다음 페이지 검색 (스크롤이 끝에 도달할 때 호출)
    fun loadNextPage() {
        if (currentStart <= totalResults) {  // 아직 불러올 데이터가 있을 때만 호출
            currentStart += 10  // Naver API에서 10개씩 로드
            viewModelScope.launch {
                repository.searchBooks(currentQuery, currentStart)
            }
        }
    }

    // ViewModel에서 total 업데이트 (LiveData 관찰 후 ViewModel에서 값을 추출할 수 있음)
    fun updateTotalResults(total: Int) {
        totalResults = total
    }
}