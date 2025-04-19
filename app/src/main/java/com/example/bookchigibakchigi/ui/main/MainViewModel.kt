package com.example.bookchigibakchigi.ui.main

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.PhotoCardWithTextContents
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import com.example.bookchigibakchigi.data.repository.PhotoCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bookShelfRepository: BookShelfRepository,
    private val photoCardRepository: PhotoCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainViewUiState>(MainViewUiState.Loading)
    val uiState: StateFlow<MainViewUiState> = _uiState.asStateFlow()

    private val _filterType = MutableStateFlow<BookFilterType>(BookFilterType.All)
    val filterType: StateFlow<BookFilterType> = _filterType.asStateFlow()

    // Channel을 SharedFlow로 변경
    private val _bookDetailFlow = MutableSharedFlow<BookEntity>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val bookDetailFlow = _bookDetailFlow.asSharedFlow()

    // 네비게이션 이벤트를 위한 SharedFlow 추가
    private val _navigationEventFlow = MutableSharedFlow<NavigationEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val navigationEventFlow = _navigationEventFlow.asSharedFlow()

    // filterType의 변경을 감지하는 Flow
    @OptIn(ExperimentalCoroutinesApi::class)
    private val sharedBooksFlow = filterType
        .flatMapLatest<BookFilterType, List<BookEntity>> { filterType ->
            bookShelfRepository.getShelfItems(filterType)
        }
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
        _uiState.value.asBookDetail()?.let { currentState ->
            try {
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

    fun refreshShelf() {
        loadBooks()
    }

    fun updateUiState(newState: MainViewUiState) {
        viewModelScope.launch {
            _uiState.value = newState
        }
    }

    fun navigateToBookDetail(selectedBook: BookEntity, position: Int, sharedView: View?) {
        viewModelScope.launch {
            // 현재 상태가 이미 BookDetail이고 같은 책을 선택한 경우 중복 호출 방지
            if (_uiState.value is MainViewUiState.BookDetail) {
                val currentState = _uiState.value as MainViewUiState.BookDetail
                if (currentState.currentBook.itemId == selectedBook.itemId) {
                    return@launch
                }
            }
            
            // 현재 상태에서 books를 가져옴
            val currentBooks = when (val currentState = _uiState.value) {
                is MainViewUiState.MyLibrary -> currentState.books
                is MainViewUiState.BookDetail -> currentState.books
                else -> emptyList()
            }
            
            val photoCards = photoCardRepository.getPhotoCardListByIsbn(selectedBook.isbn)
            _uiState.value = MainViewUiState.BookDetail(
                books = currentBooks,
                currentBook = selectedBook,
                initialPosition = position,
                photoCards = photoCards
            )
            
            // 네비게이션 이벤트 전송
            _navigationEventFlow.emit(
                NavigationEvent.NavigateToBookDetail(
                    book = selectedBook,
                    position = position,
                    transitionName = "sharedView_${selectedBook.itemId}"
                )
            )
        }
    }

    suspend fun deleteSelectedBooks(books: List<BookEntity>) {
        books.forEach { book ->
            bookShelfRepository.deleteBook(book)
        }
        refreshShelf()
    }

    fun updateFilterType(newFilterType: BookFilterType) {
        viewModelScope.launch {
            _filterType.value = newFilterType
        }
    }
}

// 네비게이션 이벤트를 위한 sealed class 추가
sealed class NavigationEvent {
    data class NavigateToBookDetail(
        val book: BookEntity,
        val position: Int,
        val transitionName: String
    ) : NavigationEvent()
}

sealed class BookFilterType {
    object Reading : BookFilterType()
    object Finished : BookFilterType()
    object All : BookFilterType()
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

private fun MainViewUiState.asBookDetail(): MainViewUiState.BookDetail? =
    this as? MainViewUiState.BookDetail