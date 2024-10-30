package com.example.bookchigibakchigi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.network.model.NaverBookResponse
import com.example.bookchigibakchigi.repository.BookSearchRepository
import kotlinx.coroutines.launch

class BookViewModel(private val repository: BookSearchRepository) : ViewModel() {

    // Nullable 타입으로 변경
    val bookSearchResults: LiveData<NaverBookResponse?> get() = repository.bookSearchResults
    val errorMessage: LiveData<String> get() = repository.errorMessage

    fun searchBooks(query: String) {
        viewModelScope.launch {
            repository.searchBooks(query)
        }
    }
}