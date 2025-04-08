package com.example.bookchigibakchigi.ui.main

import android.util.Log
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
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bookShelfRepository: BookShelfRepository,
    private val photoCardRepository: PhotoCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainViewUiState>(MainViewUiState.Loading)
    val uiState: StateFlow<MainViewUiState> = _uiState.asStateFlow()

    // Channel 추가
    private val _bookDetailChannel = Channel<BookEntity>()
    val bookDetailChannel = _bookDetailChannel.receiveAsFlow()

    // 공유된 핫 흐름 생성
    private val sharedBooksFlow = bookShelfRepository.getShelfItems()
        .shareIn(
            scope = viewModelScope,
            replay = 1,
            started = SharingStarted.WhileSubscribed()
        )

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            sharedBooksFlow.collect { books ->
                // _uiState.value 로그로 찍기
                Log.d("loadBooks TEST ", "_uiState.value: ${_uiState.value}")

                if (_uiState.value is MainViewUiState.BookDetail) {
                    return@collect
                }

                if (books.isEmpty()) {
                    _uiState.value = MainViewUiState.Empty
                } else {
                    _uiState.value = MainViewUiState.MyLibrary(books = books)
                }
            }
        }
    }

    suspend fun updateCurrentBook(newBook: BookEntity) {
        // asBookDetail() 확장 함수를 사용하여 더 간단하게 처리
        _uiState.value.asBookDetail()?.let { currentState ->
            try {
                // 먼저 book만 업데이트
//                _uiState.value = currentState.updateBookOnly(newBook)
                
                // 비동기로 photoCards 로드
                val photoCards = photoCardRepository.getPhotoCardListByIsbn(newBook.isbn)
                _uiState.value = currentState.copy(
                    currentBook = newBook,
                    photoCards = photoCards,
                    initialPosition = null
                )
                Log.d("TEST TEST TEST ", "updateCurrentBook: ")
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    currentBook = newBook,
                    error = e.message
                )
            }
        }
    }

    fun setBookDetailState(selectedBook: BookEntity) {
        viewModelScope.launch {
            sharedBooksFlow.collect { books ->
                val position = books.indexOf(selectedBook)
                val photoCards = photoCardRepository.getPhotoCardListByIsbn(selectedBook.isbn)
                _uiState.value = MainViewUiState.BookDetail(
                    books = books,
                    currentBook = selectedBook,
                    initialPosition = position,
                    photoCards = photoCards
                )
                // Channel에 이벤트 전송
                _bookDetailChannel.send(selectedBook)
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