package com.example.bookchigibakchigi.ui.main

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.PhotoCardWithTextContents
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import com.example.bookchigibakchigi.data.repository.PhotoCardRepository
import com.example.bookchigibakchigi.mapper.BookMapper
import com.example.bookchigibakchigi.model.BookUiModel
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

    private val _bookDetailFlow = MutableSharedFlow<BookUiModel>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val bookDetailFlow = _bookDetailFlow.asSharedFlow()

    private val _navigationEventFlow = MutableSharedFlow<NavigationEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val navigationEventFlow = _navigationEventFlow.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val sharedBooksFlow = filterType
        .flatMapLatest<BookFilterType, List<BookUiModel>> { filterType ->
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

    suspend fun updateCurrentBook(newBook: BookUiModel) {
        _uiState.value.asBookDetail()?.let { currentState ->
            try {
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

    fun navigateToBookDetail(selectedBook: BookUiModel, position: Int, sharedView: View?) {
        viewModelScope.launch {
            if (_uiState.value is MainViewUiState.BookDetail) {
                val currentState = _uiState.value as MainViewUiState.BookDetail
                if (currentState.currentBook.itemId == selectedBook.itemId) {
                    return@launch
                }
            }
            
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
            
            _navigationEventFlow.emit(
                NavigationEvent.NavigateToBookDetail(
                    book = selectedBook,
                    position = position,
                    transitionName = "sharedView_${selectedBook.itemId}"
                )
            )
        }
    }

    suspend fun deleteSelectedBooks(books: List<BookUiModel>) {
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

sealed class NavigationEvent {
    data class NavigateToBookDetail(
        val book: BookUiModel,
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
        val books: List<BookUiModel>,
        val isLoading: Boolean = false,
        val error: String? = null,
        val transitionName: String = ""
    ) : MainViewUiState()

    data class BookDetail(
        val books: List<BookUiModel>,
        val currentBook: BookUiModel,
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