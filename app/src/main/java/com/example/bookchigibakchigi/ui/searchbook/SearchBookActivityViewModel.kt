package com.example.bookchigibakchigi.ui.searchbook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.network.model.AladinBookItem
import com.example.bookchigibakchigi.repository.AladinBookRepository
import kotlinx.coroutines.launch

class SearchBookActivityViewModel(private val repository: AladinBookRepository) : ViewModel() {
    private val _bookSearchResults = MutableLiveData<List<AladinBookItem>>()
    val bookSearchResults: LiveData<List<AladinBookItem>> get() = _bookSearchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // AladinBookSearchRepository의 searchBooks 메서드 호출
    fun searchBooks(query: String) {
        // 로딩 상태 시작
        _isLoading.value = true

        // 코루틴 실행
        viewModelScope.launch {
            try {
                val books = repository.searchBooks(query)
                if (books.isEmpty()) {
                    // 검색 결과가 없을 때
                    _errorMessage.value = "검색 결과가 없습니다."
                } else {
                    // 검색 결과가 있을 때
                    _bookSearchResults.value = books
                }
            } catch (e: Exception) {
                // 예외 발생 시 에러 메시지 업데이트
                _errorMessage.value = "검색 중 오류가 발생했습니다: ${e.message}"
            } finally {
                // 로딩 상태 종료
                _isLoading.value = false
            }
        }
    }
}