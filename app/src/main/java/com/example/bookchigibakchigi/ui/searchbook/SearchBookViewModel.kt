package com.example.bookchigibakchigi.ui.searchbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.bookchigibakchigi.data.paging.BookPagingSource
import com.example.bookchigibakchigi.data.repository.SearchBookRepository
import com.example.bookchigibakchigi.mapper.BookMapper
import com.example.bookchigibakchigi.model.SearchBookUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SearchBookViewModel @Inject constructor(
    private val repository: SearchBookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchBookUiState>(SearchBookUiState.Empty)
    val uiState: StateFlow<SearchBookUiState> = _uiState.asStateFlow()

    fun onSearchClick(query: String) {
        if (query.isNotEmpty()) {
            searchBooks(query)
        }
    }

    fun onSearchAction(query: String): Boolean {
        if (query.isNotBlank()) {
            searchBooks(query)
            return true
        }
        return false
    }

    fun setNoResult() {
        _uiState.value = SearchBookUiState.NoResult
    }

    fun searchBooks(query: String): Flow<PagingData<SearchBookUiModel>> {
        if (query.isBlank()) {
            _uiState.value = SearchBookUiState.Empty
            return Pager(
                config = PagingConfig(pageSize = 10),
                pagingSourceFactory = {
                    BookPagingSource(repository, query)
                }
            ).flow
                .map { pagingData -> pagingData.map { BookMapper.toSearchBookUiModel(it) } }
                .cachedIn(viewModelScope)
        }

        _uiState.value = SearchBookUiState.Loading
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = {
                BookPagingSource(repository, query)
            }
        ).flow
            .map { pagingData -> 
                val mappedData = pagingData.map { BookMapper.toSearchBookUiModel(it) }
                _uiState.value = SearchBookUiState.Success(mappedData)
                mappedData
            }
            .cachedIn(viewModelScope)
    }
}

sealed class SearchBookUiState {
    data object Loading : SearchBookUiState()
    data class Success(val books: PagingData<SearchBookUiModel>) : SearchBookUiState()
    data class Error(val message: String) : SearchBookUiState()
    data object Empty : SearchBookUiState()
    data object NoResult : SearchBookUiState()
}