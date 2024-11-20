package com.example.bookchigibakchigi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.network.model.NaverBookResponse
import com.example.bookchigibakchigi.repository.BookSearchRepository
import kotlinx.coroutines.launch

class BookViewModel(private val repository: BookSearchRepository) : ViewModel() {

    val bookSearchResults: LiveData<NaverBookResponse?> get() = repository.bookSearchResults
    val errorMessage: LiveData<String> get() = repository.errorMessage

    private var currentQuery: String = ""
    private var currentStart: Int = 1
    private var totalResults: Int = 0

    // 첫 번째 검색
    fun searchBooks(query: String) {
        currentQuery = query
        currentStart = 1  // 처음 검색할 때는 1페이지부터
        viewModelScope.launch {
            repository.searchBooks(query, currentStart)
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