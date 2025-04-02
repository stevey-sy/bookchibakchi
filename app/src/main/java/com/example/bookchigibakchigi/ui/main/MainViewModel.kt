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

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = MainViewUiState.Loading
            try {
                bookShelfRepository.getShelfItems().collect { books ->
//                    _books.value = books
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

    fun updateCurrentBook(newBook: BookEntity) {
        viewModelScope.launch {
            // asBookDetail() 확장 함수를 사용하여 더 간단하게 처리
            _uiState.value.asBookDetail()?.let { currentState ->
                try {
                    // 먼저 book만 업데이트
//                    _uiState.value = currentState.updateBookOnly(newBook)
                    
                    // 비동기로 photoCards 로드
                    val photoCards = photoCardRepository.getPhotoCardListByIsbn(newBook.isbn)
                    _uiState.value = currentState.copy(
                        currentBook = newBook,
                        photoCards = photoCards,
                        initialPosition = null
                    )
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(
                        currentBook = newBook,
                        error = e.message
                    )
                }
            }
        }
    }

    fun setBookDetailState(selectedBook: BookEntity) {
        viewModelScope.launch {
            try {
                bookShelfRepository.getShelfItems().collect { books ->
                    val position = books.indexOf(selectedBook)
                    val photoCards = photoCardRepository.getPhotoCardListByIsbn(selectedBook.isbn)
                    _uiState.value = MainViewUiState.BookDetail(
                        books = books,
                        currentBook = selectedBook,
                        photoCards = photoCards,
                        initialPosition = position
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MainViewUiState.BookDetail(
                    books = emptyList(),
                    currentBook = selectedBook,
                    photoCards = emptyList(),
                    error = e.message
                )
            }
        }
    }

    fun refreshShelf() {
        loadBooks()
    }

    fun updateUiState(newState: MainViewUiState) {
        viewModelScope.launch {
            _uiState.value = newState
        }
    }
}

sealed class MainViewUiState {
    data class MyLibrary(
        val books: List<BookEntity>,
        val isLoading: Boolean = false,
        val error: String? = null,
        val transitionName: String = ""
    ) : MainViewUiState()

    data class BookDetail(
        val books: List<BookEntity>,
        val currentBook: BookEntity,
        val initialPosition: Int? = null,
        val photoCards: List<PhotoCardWithTextContents>,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : MainViewUiState()

    data object Loading : MainViewUiState()
    data object Empty : MainViewUiState()
}

// sealed class 밖에 확장 함수로 정의
private fun MainViewUiState.BookDetail.updateBookOnly(newBook: BookEntity) = copy(
    currentBook = newBook
)

private fun MainViewUiState.asBookDetail(): MainViewUiState.BookDetail? =
    this as? MainViewUiState.BookDetail