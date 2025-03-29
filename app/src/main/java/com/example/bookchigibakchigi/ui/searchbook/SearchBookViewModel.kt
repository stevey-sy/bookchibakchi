package com.example.bookchigibakchigi.ui.searchbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.network.model.AladinBookItem
import com.example.bookchigibakchigi.data.repository.AladinBookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchBookViewModel @Inject constructor(
    private val repository: AladinBookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchBookUiState>(SearchBookUiState.Empty)
    val uiState: StateFlow<SearchBookUiState> = _uiState.asStateFlow()

    fun searchBooks(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchBookUiState.Loading
            try {
                val books = repository.searchBooks(query)
                if (books.isEmpty()) {
                    _uiState.value = SearchBookUiState.Empty
                } else {
                    _uiState.value = SearchBookUiState.Success(books)
                }
            } catch (e: Exception) {
                _uiState.value = SearchBookUiState.Error("검색 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }
}

sealed class SearchBookUiState {
    data object Loading : SearchBookUiState()
    data class Success(val books: List<AladinBookItem>) : SearchBookUiState()
    data class Error(val message: String) : SearchBookUiState()
    data object Empty : SearchBookUiState()
}