package com.example.bookchigibakchigi.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.PhotoCardWithTextContents
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import com.example.bookchigibakchigi.data.repository.PhotoCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bookShelfRepository: BookShelfRepository,
    private val photoCardRepository: PhotoCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainViewUiState>(MainViewUiState.Loading)
    val uiState: StateFlow<MainViewUiState> = _uiState.asStateFlow()

    private val _books = MutableStateFlow<List<BookEntity>>(emptyList())
    val books: StateFlow<List<BookEntity>> = _books.asStateFlow()

    private val _currentBook = MutableStateFlow<BookEntity?>(null)
    val currentBook: StateFlow<BookEntity?> = _currentBook.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = MainViewUiState.Loading
            try {
                bookShelfRepository.getShelfItems().collect { books ->
                    _books.value = books
                    _uiState.value = if (books.isEmpty()) {
                        MainViewUiState.Empty
                    } else {
                        MainViewUiState.MyLibrary(books = books)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MainViewUiState.MyLibrary(
                    books = emptyList(),
                    error = e.message
                )
            }
        }
    }

    fun setBookDetailState(book: BookEntity) {
        viewModelScope.launch {
//            _uiState.value = MainViewUiState.Loading
            try {
                _currentBook.value = book
                val photoCards = photoCardRepository.getPhotoCardListByIsbn(book.isbn)
                _uiState.value = MainViewUiState.BookDetail(
                    books = _books.value,
                    currentBook = book,
                    photoCards = photoCards
                )
            } catch (e: Exception) {
                _uiState.value = MainViewUiState.BookDetail(
                    books = _books.value,
                    currentBook = book,
                    photoCards = emptyList(),
                    error = e.message
                )
            }
        }
    }

    fun setCurrentBook(book: BookEntity) {
        viewModelScope.launch {
//            _uiState.value = MainViewUiState.Loading
            try {
                _currentBook.value = book
                val photoCards = photoCardRepository.getPhotoCardListByIsbn(book.isbn)
                _uiState.value = MainViewUiState.BookDetail(
                    books = _books.value,
                    currentBook = book,
                    photoCards = photoCards
                )
            } catch (e: Exception) {
                _uiState.value = MainViewUiState.BookDetail(
                    books = _books.value,
                    currentBook = book,
                    photoCards = emptyList(),
                    error = e.message
                )
            }
        }
    }

    fun refreshShelf() {
        loadBooks()
    }
}

sealed class MainViewUiState {
    data class MyLibrary(
        val books: List<BookEntity>,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : MainViewUiState()

    data class BookDetail(
        val books: List<BookEntity>,
        val currentBook: BookEntity,
        val photoCards: List<PhotoCardWithTextContents>,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : MainViewUiState()

    data object Loading : MainViewUiState()
    data object Empty : MainViewUiState()
}